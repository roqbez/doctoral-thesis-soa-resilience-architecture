package br.ufsc.gsigma.servicediscovery.support;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufsc.gsigma.catalog.services.model.ConnectableComponent;
import br.ufsc.gsigma.infrastructure.util.lpsolve.LpSolveHelper;
import br.ufsc.gsigma.servicediscovery.model.DiscoveredService;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute.QoSValueAggregationType;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute.QoSValueUtilityDirection;
import br.ufsc.gsigma.servicediscovery.model.QoSConstraint;
import br.ufsc.gsigma.servicediscovery.model.QoSInformation;
import br.ufsc.gsigma.servicediscovery.model.QoSLevel;
import br.ufsc.gsigma.servicediscovery.model.QoSValueComparisionType;
import br.ufsc.gsigma.servicediscovery.model.ServiceQoSInformationItem;
import br.ufsc.gsigma.servicediscovery.support.ProcessStructureParser.Cycle;
import br.ufsc.gsigma.servicediscovery.support.struct.TaskStructure;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public abstract class ServiceQoSUtil {

	private static final int LP_SOLVE_OPTIMAL_TIMEOUT = (int) (60 * 1);

	private static final int LP_SOLVE_TIMEOUT = (int) (60 * 0.5);

	private static final int MAX_OPTIMAL_SELECTION_DURATION = 60000 * 3;

	private static final Logger logger = LoggerFactory.getLogger(ServiceQoSUtil.class);

	public static final boolean DEBUG_LPSOLVE = false;

	public static double calculateCompositionUtility(Map<String, Double> agregatedQoS, Map<String, QoSAttribute> qoSAttributes, Map<String, Double> maxQoSAggMap, Map<String, Double> minQoSAggMap, Map<String, Double> mapQoSWeights) {

		double u = 0;

		for (Entry<String, Double> e : agregatedQoS.entrySet()) {

			String qoSKey = e.getKey();

			QoSAttribute qoSAtt = qoSAttributes.get(qoSKey);

			Double q = e.getValue();

			Double qMaxAgg = maxQoSAggMap.get(qoSKey);
			Double qMinAgg = minQoSAggMap.get(qoSKey);

			if (qMaxAgg != null && qMinAgg != null) {

				double qDelta = qMaxAgg - qMinAgg;
				double w = mapQoSWeights.containsKey(qoSKey) ? mapQoSWeights.get(qoSKey) : 1D;

				if (qoSAtt.getValueUtilityDirection() == QoSValueUtilityDirection.NEGATIVE)
					u += ((qMaxAgg - q) / qDelta) * w;
				else if (qoSAtt.getValueUtilityDirection() == QoSValueUtilityDirection.POSITIVE)
					u += ((q - qMinAgg) / qDelta) * w;
			}
		}

		return u;
	}

	public static Double calculateServiceUtility(DiscoveredService service, Map<String, QoSAttribute> qoSAttributes, QoSMinMax qoSMinMax, Map<String, Double> qoSWeights) {

		double utility = 0;

		String serviceClassification = service.getServiceClassification();

		for (ServiceQoSInformationItem q : service.getQoSInformation()) {

			String qoSKey = q.getQoSKey();

			Double qValue = q.getQoSValue();

			QoSAttribute qoSAtt = qoSAttributes.get(qoSKey);

			if (qoSAtt == null)
				continue;

			Double qMax = qoSMinMax.getQoSMaxValue(serviceClassification, qoSKey);
			Double qMin = qoSMinMax.getQoSMinValue(serviceClassification, qoSKey);
			Double globalQoSDelta = qoSMinMax.getGlobalQoSDeltaValue(qoSKey);

			Double u = calculateQoSAttributeUtility(qoSAtt, qValue, qMax, qMin, globalQoSDelta, qoSWeights);

			if (u != null)
				utility += u;
		}

		return utility;

	}

	public static Double calculateQoSAttributeUtility(QoSAttribute qoSAtt, Double qValue, Double qMax, Double qMin, Double globalQoSDelta, Map<String, Double> qoSWeights) {

		if (qoSAtt == null)
			throw new IllegalArgumentException("QoSAttribute is null");

		if (qMax != null && qMin != null) {

			String qoSKey = qoSAtt.getKey();

			double qDelta = globalQoSDelta != null ? globalQoSDelta : qMax - qMin;

			double w = qoSWeights != null && qoSWeights.containsKey(qoSKey) ? qoSWeights.get(qoSKey) : 1D;

			double v = qValue;

			if (qoSAtt.getValueUtilityDirection() == QoSValueUtilityDirection.NEGATIVE)
				return ((qMax - v) / qDelta) * w;
			else if (qoSAtt.getValueUtilityDirection() == QoSValueUtilityDirection.POSITIVE)
				return ((v - qMin) / qDelta) * w;

		}
		return null;
	}

	private static Map<String, List<QoSLevel>> selectBestQoSLevels(Map<TaskStructure, Map<String, NavigableMap<Integer, List<QoSLevel>>>> qoSLevelsByTaskStructure, Map<String, QoSAttribute> qoSAttributes,
			Map<TaskStructure, Map<String, Integer>> mapSelectedNumberOfLevels, QoSInformation qoSInformation) throws LpSolveException {

		// Default to sequential
		Map<String, QoSValueAggregationType> mapQoSAggType = new HashMap<String, QoSAttribute.QoSValueAggregationType>();

		for (QoSAttribute qoSAtt : qoSAttributes.values()) {
			mapQoSAggType.put(qoSAtt.getKey(), qoSAtt.getSequentialAggregationType());
		}

		return selectBestQoSLevels(qoSLevelsByTaskStructure, qoSAttributes, mapQoSAggType, mapSelectedNumberOfLevels, qoSInformation);

	}

	@SuppressWarnings("unchecked")
	public static Map<String, List<QoSLevel>> selectBestQoSLevels(Map<TaskStructure, Map<String, NavigableMap<Integer, List<QoSLevel>>>> qoSLevelsByTaskStructure, Map<String, QoSAttribute> qoSAttributes,
			Map<String, QoSValueAggregationType> mapQoSAggType, Map<TaskStructure, Map<String, Integer>> mapSelectedNumberOfLevels, QoSInformation qoSInformation) throws LpSolveException {

		long start = System.currentTimeMillis();

		int varNumber = 1;

		boolean noLevelsLeft = true;

		List<QoSLevel> allLevels = new LinkedList<QoSLevel>();

		Map<String, TaskStructure> taxonomyToTask = new HashMap<String, TaskStructure>();

		for (Entry<TaskStructure, Map<String, NavigableMap<Integer, List<QoSLevel>>>> e : qoSLevelsByTaskStructure.entrySet()) {

			TaskStructure taskStructure = e.getKey();

			Map<String, NavigableMap<Integer, List<QoSLevel>>> qoSAttrs = e.getValue();

			taxonomyToTask.put(taskStructure.getTaxonomyClassification(), taskStructure);

			Map<String, Integer> qoSAttSelectedNumberOfLevels = mapSelectedNumberOfLevels.get(taskStructure);

			if (qoSAttSelectedNumberOfLevels == null) {
				qoSAttSelectedNumberOfLevels = new HashMap<String, Integer>();
				mapSelectedNumberOfLevels.put(taskStructure, qoSAttSelectedNumberOfLevels);
			}

			for (Entry<String, NavigableMap<Integer, List<QoSLevel>>> e2 : qoSAttrs.entrySet()) {

				String qoSKey = e2.getKey();

				QoSValueAggregationType aggType = mapQoSAggType.get(qoSKey);

				if (aggType == null)
					continue;

				NavigableMap<Integer, List<QoSLevel>> groupLevels = e2.getValue();

				Integer currentSelectedNumberOfLevels = qoSAttSelectedNumberOfLevels.get(qoSKey);

				if (currentSelectedNumberOfLevels == null) {
					currentSelectedNumberOfLevels = groupLevels.firstKey();
					qoSAttSelectedNumberOfLevels.put(qoSKey, currentSelectedNumberOfLevels);
					noLevelsLeft = false;
				} else {

					SortedMap<Integer, List<QoSLevel>> tailMap = groupLevels.tailMap(currentSelectedNumberOfLevels, false);
					if (!tailMap.isEmpty()) {
						currentSelectedNumberOfLevels = tailMap.firstKey();
						qoSAttSelectedNumberOfLevels.put(qoSKey, currentSelectedNumberOfLevels);
						noLevelsLeft = false;
					} else {
						noLevelsLeft = noLevelsLeft && true;
					}
				}

				// if (!noLevelsLeft)
				// logger.info("[" + attemptCount + "] Using " + currentSelectedNumberOfLevels + " QoS levels for serviceClassification="
				// + serviceClassification + ", QoSKey=" + qoSKey);

				List<QoSLevel> levels = groupLevels.get(currentSelectedNumberOfLevels);

				for (QoSLevel l : levels) {
					l.setVarNumber(varNumber++);
				}
				allLevels.addAll(levels);

			}
		}

		if (noLevelsLeft) {
			logger.info("Can't use more levels for QoS selections, aborting...\n\tselectedNumberOfLevels" + mapSelectedNumberOfLevels);
			return Collections.EMPTY_MAP;
		}

		int numberOfVariables = allLevels.size();

		LpSolve lpSolve = LpSolveHelper.makeLp(0, numberOfVariables);
		lpSolve.setAddRowmode(true);
		lpSolve.setTimeout(LP_SOLVE_TIMEOUT);

		Map<Integer, QoSLevel> mapVarQoSLevel = new LinkedHashMap<Integer, QoSLevel>();

		Map<String, List<QoSLevel>> levelsByQoSAttribute = new LinkedHashMap<String, List<QoSLevel>>();

		int[] objcolno = new int[numberOfVariables];
		double[] objfun = new double[numberOfVariables];

		int w = 0;
		for (QoSLevel l : allLevels) {

			String qoSKey = l.getQoSKey();

			QoSAttribute qoSAtt = qoSAttributes.get(qoSKey);

			QoSConstraint constraint = qoSInformation != null ? qoSInformation.getQoSConstraint(qoSKey) : null;

			boolean isNegative = constraint == null ? qoSAtt.getValueUtilityDirection() == QoSValueUtilityDirection.NEGATIVE : constraint.getComparisionType() == QoSValueComparisionType.LE;

			// isNegative = qoSAtt.getValueUtilityDirection() == QoSValueUtilityDirection.NEGATIVE;

			objcolno[w] = l.getVarNumber();
			objfun[w] = Math.log(isNegative ? l.getNegativeUtility() : l.getPositiveUtility());
			w++;
		}

		// Maximizar funcao
		lpSolve.setObjFnex(numberOfVariables, objfun, objcolno);
		lpSolve.setMaxim();

		int maxLevels = 0;

		// Iterando classes de servicos
		for (Entry<TaskStructure, Map<String, NavigableMap<Integer, List<QoSLevel>>>> e : qoSLevelsByTaskStructure.entrySet()) {

			TaskStructure taskStructure = e.getKey();

			// Iterando tipos de atributos de QoS
			for (Entry<String, NavigableMap<Integer, List<QoSLevel>>> e1 : e.getValue().entrySet()) {

				String qoSKey = e1.getKey();

				int k = mapSelectedNumberOfLevels.get(taskStructure).get(qoSKey);

				maxLevels = Math.max(maxLevels, k);

				List<QoSLevel> levels = e1.getValue().get(k);

				QoSConstraint constraint = qoSInformation != null ? qoSInformation.getQoSConstraint(qoSKey) : null;

				Double c = constraint != null ? constraint.getQoSValue() : null;

				QoSAttribute qoSAtt = qoSAttributes.get(qoSKey);

				QoSValueAggregationType aggType = mapQoSAggType.get(qoSKey);

				if (aggType == null)
					continue;

				boolean isNegativeDirection = constraint != null && constraint.getComparisionType() != null ? constraint.getComparisionType() == QoSValueComparisionType.LE : qoSAtt.getValueUtilityDirection() == QoSValueUtilityDirection.NEGATIVE;

				boolean isMinConstraint = c != null && aggType == QoSValueAggregationType.MIN;
				boolean isMaxConstraint = c != null && aggType == QoSValueAggregationType.MAX;

				int n = levels.size();
				int[] colno = new int[n];

				double[] row1 = new double[n];

				double[] row2 = isMinConstraint || isMaxConstraint ? new double[n] : null;

				int cycleMultiplier = taskStructure.getClass() == TaskStructure.class ? taskStructure.getRepeatCount() + 1 : 1;

				// Iterando niveis de QoS
				int i = 0;
				for (QoSLevel l : levels) {

					List<QoSLevel> gLevels = levelsByQoSAttribute.get(l.getQoSKey());

					if (gLevels == null) {
						gLevels = new LinkedList<QoSLevel>();
						levelsByQoSAttribute.put(l.getQoSKey(), gLevels);
					}

					gLevels.add(l);

					mapVarQoSLevel.put(l.getVarNumber(), l);

					colno[i] = l.getVarNumber();
					row1[i] = 1;

					if (row2 != null) {
						row2[i] = l.getValue() * cycleMultiplier; // multiplier if inside a cycle
					}

					lpSolve.setBinary(l.getVarNumber(), true);

					if (DEBUG_LPSOLVE)
						lpSolve.setColName(l.getVarNumber(), "[" + l.getServiceClassification() + "][" + l.getQoSKey() + "][" + l.getNumber() + "]");

					i++;
				}

				// Constraint que obriga a escolher um level de QoS (somente um) para cada tipo de atributo de QoS
				lpSolve.addConstraintex(n, row1, colno, LpSolve.EQ, 1);

				// Constraints do tipo MIN e MAX
				if (isMaxConstraint) {

					int compareType = isNegativeDirection ? LpSolve.LE : LpSolve.GE;

					logger.info("Max constraint on QoS attribute '" + qoSKey + "', numberOfLevels=" + n + ", task='" + taskStructure.getName() + "', serviceClassification='" + taskStructure.getTaxonomyClassification() + "' --> "
							+ (compareType == LpSolve.LE ? "LE " : "GE ") + c);

					lpSolve.addConstraintex(n, row2, colno, compareType, c);

				} else if (isMinConstraint) {

					int compareType = isNegativeDirection ? LpSolve.LE : LpSolve.GE;

					logger.info("Min constraint on QoS attribute '" + qoSKey + "', numberOfLevels=" + n + ", task='" + taskStructure.getName() + ", serviceClassification='" + taskStructure.getTaxonomyClassification() + "' --> "
							+ (compareType == LpSolve.LE ? "LE " : "GE ") + c);

					lpSolve.addConstraintex(n, row2, colno, compareType, c);
				}

			}
		}

		// Agrupamento
		for (Entry<String, List<QoSLevel>> e : levelsByQoSAttribute.entrySet()) {

			String qoSKey = e.getKey();

			QoSConstraint constraint = qoSInformation != null ? qoSInformation.getQoSConstraint(qoSKey) : null;

			Double c = constraint != null ? constraint.getQoSValue() : null;

			QoSAttribute qoSAtt = qoSAttributes.get(qoSKey);

			boolean isNegativeDirection = constraint != null && constraint.getComparisionType() != null ? constraint.getComparisionType() == QoSValueComparisionType.LE : qoSAtt.getValueUtilityDirection() == QoSValueUtilityDirection.NEGATIVE;

			QoSValueAggregationType aggType = mapQoSAggType.get(qoSKey);

			if (c != null) {

				List<QoSLevel> levels = e.getValue();

				int n = levels.size();
				int[] colno = new int[n];

				double[] row1 = new double[n];

				int i = 0;

				for (QoSLevel l : levels) {

					TaskStructure taskStructure = taxonomyToTask.get(l.getServiceClassification());
					int cycleMultiplier = taskStructure.getClass() == TaskStructure.class ? taskStructure.getRepeatCount() + 1 : 1;

					colno[i] = l.getVarNumber();

					// Multiplier for cycle
					if (aggType == QoSValueAggregationType.SUM) {
						row1[i] = l.getValue() * cycleMultiplier;
					} else if (aggType == QoSValueAggregationType.PRODUCT)
						row1[i] = Math.log(Math.pow(l.getValue(), cycleMultiplier));

					i++;
				}

				int compareType = isNegativeDirection ? LpSolve.LE : LpSolve.GE;

				// Constraints globais do tipo somatario
				if (aggType == QoSValueAggregationType.SUM) {

					logger.info("Sum constraint on QoS attribute '" + qoSKey + "' --> " + (compareType == LpSolve.LE ? "LE " : "GE ") + c);

					lpSolve.addConstraintex(n, row1, colno, compareType, c);

					// Constraints globais do tipo produtorio
				} else if (aggType == QoSValueAggregationType.PRODUCT) {

					double log = Math.log(c);

					logger.info("Product constraint on QoS attribute '" + qoSKey + "' --> " + (compareType == LpSolve.LE ? "LE " : "GE ") + log);

					lpSolve.addConstraintex(n, row1, colno, compareType, log);
				}

			}
		}

		lpSolve.setAddRowmode(false);

		if (DEBUG_LPSOLVE)
			lpSolve.writeLp("model-hybrid.lp");

		lpSolve.setVerbose(LpSolve.IMPORTANT);

		long d = System.currentTimeMillis() - start;

		logger.info("LpSolve model for QoS Levels selection prepared in " + d + " ms");

		try {
			logger.info("Trying to solve LpSolve model for " + maxLevels + " levels (variables=" + lpSolve.getNcolumns() + ", constraints=" + lpSolve.getNrows() + ")");
			start = System.currentTimeMillis();
			lpSolve.solve();
			d = System.currentTimeMillis() - start;
			logger.info("LpSolve model solved in " + d + " ms");

			Map<String, List<QoSLevel>> result = new LinkedHashMap<String, List<QoSLevel>>();

			double[] var = lpSolve.getPtrVariables();
			for (int i = 0; i < var.length; i++) {

				if (var[i] == 1D) {

					int varNum = i + 1;

					QoSLevel level = mapVarQoSLevel.get(varNum);

					List<QoSLevel> levels = result.get(level.getServiceClassification());

					if (levels == null) {
						levels = new LinkedList<QoSLevel>();
						result.put(level.getServiceClassification(), levels);
					}

					levels.add(level);
				}

			}

			if (result.isEmpty())
				return selectBestQoSLevels(qoSLevelsByTaskStructure, qoSAttributes, mapSelectedNumberOfLevels, qoSInformation);

			return result;

		} finally {
			lpSolve.deleteLp();
		}
	}

	private static int getCycleMultiplierForOptimalSelection(List<Cycle> cycles, ConnectableComponent c) {

		for (Cycle cycle : cycles) {
			if (cycle.containsComponent(c)) {
				return cycle.repeatCount + 1;
			}
		}

		return 1;
	}

	public static List<DiscoveredService> selectOptimalServices(List<List<ConnectableComponent>> executionsPaths, List<List<ConnectableComponent>> executionsRoutes, List<Cycle> cycles, Iterator<DiscoveredService> services,
			Map<String, ServiceClassificationInfo> mapServiceClassificationInfo, Map<String, QoSAttribute> qoSAttributes, List<QoSConstraint> globalQoSConstraints, Map<String, List<QoSConstraint>> localQoSConstraints, Map<String, Double> qoSWeights,
			QoSMinMax qoSMinMax, Map<String, Integer> maxServicesPerServiceClassification, Map<String, Double> mapServiceUtility) throws LpSolveException {

		logger.info("Starting optimal selection");

		long start = System.currentTimeMillis();

		Map<String, Collection<DiscoveredService>> mapServicesByClassification = new LinkedHashMap<String, Collection<DiscoveredService>>();

		Map<Integer, DiscoveredService> mapVarService = new HashMap<Integer, DiscoveredService>();
		Map<String, Integer> mapServiceVar = new HashMap<String, Integer>();

		int l = 0;
		int serviceVar = 1;

		try {

			while (services.hasNext()) {

				DiscoveredService s = services.next();

				Collection<DiscoveredService> categoryServices = mapServicesByClassification.get(s.getServiceClassification());

				if (categoryServices == null) {
					categoryServices = new LinkedList<DiscoveredService>();
					mapServicesByClassification.put(s.getServiceClassification(), categoryServices);
				}
				categoryServices.add(s);

				mapVarService.put(serviceVar, s);
				mapServiceVar.put(s.getServiceKey(), serviceVar);

				serviceVar++;
				l++;
			}

		} finally {
			if (services instanceof Closeable)
				try {
					((Closeable) services).close();
				} catch (IOException e1) {
				}
		}

		logger.info("Optimal selection will be done for " + l + " services");

		LpSolve lpSolve = LpSolveHelper.makeLp(0, l);
		lpSolve.setAddRowmode(true);
		lpSolve.setTimeout(LP_SOLVE_OPTIMAL_TIMEOUT);

		int[] objfuncolno = new int[l];
		double[] objfunrow = new double[l];

		for (List<ConnectableComponent> executionsRoute : executionsRoutes) {

			for (ConnectableComponent c : executionsRoute) {

				TaskStructure t = (TaskStructure) c;

				String serviceClassification = t.getTaxonomyClassification();

				if (serviceClassification != null) {

					Collection<DiscoveredService> svcs = mapServicesByClassification.get(serviceClassification);

					ServiceClassificationInfo serviceClassificationInfo = mapServiceClassificationInfo.get(serviceClassification);

					for (DiscoveredService s : svcs) {

						int varNumber = mapServiceVar.get(s.getServiceKey());

						int i = varNumber - 1;
						double v = objfunrow[i];

						v += getServiceQoSMultiplierForOptimalSelection(s, serviceClassificationInfo, qoSAttributes, qoSWeights);

						objfuncolno[i] = varNumber;
						objfunrow[i] = v;

						if (!lpSolve.isBinary(varNumber))
							lpSolve.setBinary(varNumber, true);

					}
				}
			}
		}

		List<QoSAttribute> minMaxQoSAttsConstraints = new LinkedList<QoSAttribute>();

		for (QoSConstraint constraint : globalQoSConstraints) {

			String qoSKey = constraint.getQoSKey();
			QoSAttribute qoSAtt = qoSAttributes.get(qoSKey);

			boolean isNegativeDirection = constraint != null && constraint.getComparisionType() != null ? constraint.getComparisionType() == QoSValueComparisionType.LE : qoSAtt.getValueUtilityDirection() == QoSValueUtilityDirection.NEGATIVE;

			if (qoSAtt.getSequentialAggregationType() == QoSValueAggregationType.MAX || qoSAtt.getSequentialAggregationType() == QoSValueAggregationType.MIN) {
				minMaxQoSAttsConstraints.add(qoSAtt);

			} else if (qoSAtt.getSequentialAggregationType() == QoSValueAggregationType.SUM || qoSAtt.getSequentialAggregationType() == QoSValueAggregationType.PRODUCT) {

				// Execution Paths
				for (List<ConnectableComponent> executionPath : executionsPaths) {

					int[] colno = new int[l];
					double[] row = new double[l];

					int i = 0;

					for (ConnectableComponent c : executionPath) {

						int cycleMultiplier = getCycleMultiplierForOptimalSelection(cycles, c);

						TaskStructure t = (TaskStructure) c;

						String serviceClassification = t.getTaxonomyClassification();

						if (serviceClassification != null) {

							Collection<DiscoveredService> svcs = mapServicesByClassification.get(serviceClassification);

							for (DiscoveredService s : svcs) {

								Double v = s.getQoSValue(qoSKey);

								colno[i] = mapServiceVar.get(s.getServiceKey());

								if (qoSAtt.getSequentialAggregationType() == QoSValueAggregationType.SUM)
									row[i] = v * cycleMultiplier;
								else if (qoSAtt.getSequentialAggregationType() == QoSValueAggregationType.PRODUCT)
									row[i] = Math.log(Math.pow(v, cycleMultiplier));

								i++;
							}
						}
					}

					int compareType = isNegativeDirection ? LpSolve.LE : LpSolve.GE;

					double constr = constraint.getQoSValue();

					if (qoSAtt.getSequentialAggregationType() == QoSValueAggregationType.SUM) {
						logger.info("Sum constraint on execution path " + executionPath + " with QoS attribute '" + qoSKey + " --> " + (compareType == LpSolve.LE ? "LE " : "GE ") + constr);
						lpSolve.addConstraintex(i, row, colno, compareType, constr);

					} else if (qoSAtt.getSequentialAggregationType() == QoSValueAggregationType.PRODUCT) {
						double log = Math.log(constr);
						logger.info("Sum constraint on execution path " + executionPath + " with QoS attribute '" + qoSKey + " --> " + (compareType == LpSolve.LE ? "LE " : "GE ") + log);
						lpSolve.addConstraintex(i, row, colno, compareType, log);
					}
				}

				// Execution Routes
				for (List<ConnectableComponent> executionRoute : executionsRoutes) {

					int[] colno = new int[l];
					double[] row = new double[l];

					int i = 0;

					for (ConnectableComponent c : executionRoute) {

						int cycleMultiplier = getCycleMultiplierForOptimalSelection(cycles, c);

						TaskStructure t = (TaskStructure) c;

						String serviceClassification = t.getTaxonomyClassification();

						if (serviceClassification != null) {

							Collection<DiscoveredService> svcs = mapServicesByClassification.get(serviceClassification);

							for (DiscoveredService s : svcs) {

								Double v = s.getQoSValue(qoSKey);

								colno[i] = mapServiceVar.get(s.getServiceKey());

								if (qoSAtt.getSequentialAggregationType() == QoSValueAggregationType.SUM)
									row[i] = v * cycleMultiplier;
								else if (qoSAtt.getSequentialAggregationType() == QoSValueAggregationType.PRODUCT)
									row[i] = Math.log(Math.pow(v, cycleMultiplier));

								i++;
							}
						}
					}

					int compareType = isNegativeDirection ? LpSolve.LE : LpSolve.GE;

					double constr = constraint.getQoSValue();

					if (qoSAtt.getSequentialAggregationType() == QoSValueAggregationType.SUM) {
						logger.info("Sum constraint on execution route " + executionRoute + " with QoS attribute '" + qoSKey + " --> " + (compareType == LpSolve.LE ? "LE " : "GE ") + constr);
						lpSolve.addConstraintex(i, row, colno, compareType, constr);

					} else if (qoSAtt.getSequentialAggregationType() == QoSValueAggregationType.PRODUCT) {
						double log = Math.log(constr);
						logger.info("Sum constraint on execution route " + executionRoute + " with QoS attribute '" + qoSKey + " --> " + (compareType == LpSolve.LE ? "LE " : "GE ") + log);
						lpSolve.addConstraintex(i, row, colno, compareType, log);
					}
				}
			}

		}

		// Iterando service classes
		for (Entry<String, Collection<DiscoveredService>> e : mapServicesByClassification.entrySet()) {

			String serviceClassification = e.getKey();
			Collection<DiscoveredService> svcs = e.getValue();

			List<QoSConstraint> localConstraints = localQoSConstraints.get(serviceClassification);

			int n = svcs.size();
			int nc = minMaxQoSAttsConstraints.size();
			int[] colno = new int[n];

			double[] row1 = new double[n];

			double[][] row2 = !minMaxQoSAttsConstraints.isEmpty() ? new double[nc][n] : null;

			double[][] row3 = localConstraints != null ? new double[localConstraints.size()][n] : null;

			int i = 0;

			for (DiscoveredService s : svcs) {

				colno[i] = mapServiceVar.get(s.getServiceKey());
				row1[i] = 1;

				if (row2 != null) {
					int k = 0;
					for (QoSAttribute q : minMaxQoSAttsConstraints) {
						row2[k++][i] = s.getQoSValue(q.getKey());
					}
				}

				if (row3 != null) {
					int k = 0;
					for (QoSConstraint c : localConstraints) {
						row3[k++][i] = s.getQoSValue(c.getQoSKey());
					}
				}

				i++;
			}

			// Constraint que obriga a escolher somente um servico para cada service class
			lpSolve.addConstraintex(colno.length, row1, colno, LpSolve.EQ, 1);

			// Constraints do tipo min e max
			int k = 0;
			for (QoSAttribute qoSAtt : minMaxQoSAttsConstraints) {

				String qoSKey = qoSAtt.getKey();

				QoSConstraint constraint = QoSInformation.getQoSConstraint(globalQoSConstraints, qoSKey);

				Double c = constraint != null ? constraint.getQoSValue() : null;

				boolean isNegativeDirection = constraint != null && constraint.getComparisionType() != null ? constraint.getComparisionType() == QoSValueComparisionType.LE : qoSAtt.getValueUtilityDirection() == QoSValueUtilityDirection.NEGATIVE;

				if (qoSAtt.getSequentialAggregationType() == QoSValueAggregationType.MAX) {

					int compareType = isNegativeDirection ? LpSolve.GE : LpSolve.LE;

					logger.info("Max constraint on QoS attribute '" + qoSKey + "', serviceClassification='" + serviceClassification + "' --> " + (compareType == LpSolve.LE ? "LE " : "GE ") + c);

					lpSolve.addConstraintex(colno.length, row2[k], colno, compareType, c);

				} else if (qoSAtt.getSequentialAggregationType() == QoSValueAggregationType.MIN) {

					int compareType = isNegativeDirection ? LpSolve.LE : LpSolve.GE;

					logger.info("Min constraint on QoS attribute '" + qoSKey + "', serviceClassification='" + serviceClassification + "' --> " + (compareType == LpSolve.LE ? "LE " : "GE ") + c);

					lpSolve.addConstraintex(colno.length, row2[k], colno, compareType, c);
				}

				k++;
			}

			// Local Constraints
			k = 0;

			if (localConstraints != null && !localConstraints.isEmpty()) {

				for (QoSConstraint constraint : localConstraints) {

					String qoSKey = constraint.getQoSKey();

					QoSAttribute qoSAtt = qoSAttributes.get(qoSKey);

					Double c = constraint != null ? constraint.getQoSValue() : null;

					boolean isNegativeDirection = constraint != null && constraint.getComparisionType() != null ? constraint.getComparisionType() == QoSValueComparisionType.LE : qoSAtt.getValueUtilityDirection() == QoSValueUtilityDirection.NEGATIVE;

					int compareType = isNegativeDirection ? LpSolve.LE : LpSolve.GE;

					logger.info("Local constraint on QoS attribute '" + qoSKey + "', serviceClassification='" + serviceClassification + "' --> " + (compareType == LpSolve.LE ? "LE " : "GE ") + c);

					lpSolve.addConstraintex(colno.length, row3[k], colno, compareType, c);

				}
			}
		}

		lpSolve.setAddRowmode(false);

		// Maximizar funcao
		lpSolve.setObjFnex(l, objfunrow, objfuncolno);
		lpSolve.setMaxim();

		if (DEBUG_LPSOLVE)
			lpSolve.writeLp("model-optimal.lp");
		lpSolve.setVerbose(LpSolve.IMPORTANT);

		double d = System.currentTimeMillis() - start;

		logger.info("LpSolve model for optimal otimization prepared in " + d + " ms");

		int maxIterations = 1;

		Map<String, Set<DiscoveredService>> mapDiscoveredServicesPerServiceClass = new HashMap<String, Set<DiscoveredService>>();

		if (maxServicesPerServiceClassification != null) {
			for (Entry<String, Integer> e : maxServicesPerServiceClassification.entrySet()) {

				String serviceClassification = e.getKey();
				Integer n = e.getValue();

				maxIterations = Math.max(maxIterations, n);
				mapDiscoveredServicesPerServiceClass.put(serviceClassification, new HashSet<DiscoveredService>(n));
			}
		}

		List<DiscoveredService> result = new LinkedList<DiscoveredService>();

		try {

			int k = 0;

			while (k++ < maxIterations) {

				logger.info("Trying to solve model with LpSolve (variables=" + (lpSolve.getNcolumns()) + ", constraints=" + lpSolve.getNrows() + ")");
				start = System.currentTimeMillis();
				lpSolve.solve();
				d = System.currentTimeMillis() - start;
				logger.info("Model solved in " + d + " ms");

				List<DiscoveredService> svcs = new LinkedList<DiscoveredService>();

				double[] var = lpSolve.getPtrVariables();
				for (int i = 0; i < var.length; i++) {
					if (var[i] == 1D) {

						int varNum = i + 1;

						DiscoveredService s = mapVarService.get(varNum);

						if (s != null) {
							if (mapServiceUtility != null) {
								double utility = calculateServiceUtility(s, qoSAttributes, qoSMinMax, qoSWeights);
								mapServiceUtility.put(s.getServiceKey(), utility);
							}

							svcs.add(s);

							mapDiscoveredServicesPerServiceClass.get(s.getServiceClassification()).add(s);
						}
					}
				}

				if (!svcs.isEmpty()) {

					for (DiscoveredService s : svcs) {
						// Poderia ser uma estrutura mais eficiente
						if (!result.contains(s))
							result.add(s);
					}

					if (k < maxIterations) {

						lpSolve.setAddRowmode(true);

						for (DiscoveredService s : svcs) {

							String serviceClassification = s.getServiceClassification();

							int maxNumberOfServices = maxServicesPerServiceClassification != null ? maxServicesPerServiceClassification.get(serviceClassification) : 1;

							Set<DiscoveredService> alreadyChoosed = mapDiscoveredServicesPerServiceClass.get(serviceClassification);

							int currentNumberOfDiscoveredServices = alreadyChoosed.size();

							if (currentNumberOfDiscoveredServices < maxNumberOfServices) {
								// Nao podemos escolher os que ja foram
								for (DiscoveredService s1 : alreadyChoosed) {
									lpSolve.addConstraintex(1, new double[] { 1 }, new int[] { mapServiceVar.get(s1.getServiceKey()) }, LpSolve.EQ, 0);
									if (logger.isDebugEnabled())
										logger.debug("The service '" + s1.getServiceKey() + "' of category " + serviceClassification + " can't be choosed again");
								}
							} else {
								// Fixa-se nos que ja foram escolhidos (somatorio deles tem que ser 1)

								double[] one = new double[currentNumberOfDiscoveredServices];
								Arrays.fill(one, 1);

								int[] vars = new int[currentNumberOfDiscoveredServices];
								int i = 0;
								for (DiscoveredService s1 : alreadyChoosed) {
									vars[i++] = mapServiceVar.get(s1.getServiceKey());
								}
								lpSolve.addConstraintex(1, one, vars, LpSolve.EQ, 1);
							}
						}

						lpSolve.setAddRowmode(false);
					}
				}

				if (d > MAX_OPTIMAL_SELECTION_DURATION) {
					logger.info("The optimal optimization is taking too long to finish (" + d + " ms). Aborting the remaning iterations");
					break;
				}

			}

		} finally {
			lpSolve.deleteLp();
		}

		return result;
	}

	private static double getServiceQoSMultiplierForOptimalSelection(DiscoveredService s, ServiceClassificationInfo serviceClassificationInfo, Map<String, QoSAttribute> qoSAttributes, Map<String, Double> mapQoSWeights) {

		double multiplier = 0;

		for (ServiceQoSInformationItem q : s.getQoSInformation()) {

			String qoSKey = q.getQoSKey();

			QoSAttribute qoSAtt = qoSAttributes.get(qoSKey);

			if (qoSAtt == null)
				continue;

			Double qMaxAgg = serviceClassificationInfo.getQoSMaxValue(qoSKey);
			Double qMinAgg = serviceClassificationInfo.getQoSMinValue(qoSKey);

			if (qMaxAgg != null && qMinAgg != null) {

				double qDelta = qMaxAgg - qMinAgg;
				double w = mapQoSWeights.containsKey(qoSKey) ? mapQoSWeights.get(qoSKey) : 1D;

				double v = q.getQoSValue();

				if (qoSAtt.getValueUtilityDirection() == QoSValueUtilityDirection.NEGATIVE)
					multiplier += ((qMaxAgg - v) / qDelta) * w;
				else if (qoSAtt.getValueUtilityDirection() == QoSValueUtilityDirection.POSITIVE)
					multiplier += ((v - qMinAgg) / qDelta) * w;
			}
		}

		// for (ServiceQoSInformationItem q : s.getQoSInformation()) {
		//
		// String qoSKey = q.getQoSKey();
		//
		// QoSAttribute qoSAtt = qoSAttributes.get(qoSKey);
		//
		// if (qoSAtt == null)
		// continue;
		//
		// Double qAvg = serviceClassificationInfo.getAvgQoSValue().get(qoSKey);
		//
		// Double qSd = serviceClassificationInfo.getSdQoSValue().get(qoSKey);
		//
		// if (qAvg != null && qSd != null) {
		//
		// double w = mapQoSWeights.containsKey(qoSKey) ? mapQoSWeights.get(qoSKey) : 1D;
		//
		// double v = q.getQoSValue();
		//
		// if (qoSAtt.getValueUtilityDirection() == QoSValueUtilityDirection.NEGATIVE)
		// multiplier += (1 - ((v - qAvg) / qSd)) * w;
		//
		// else if (qoSAtt.getValueUtilityDirection() == QoSValueUtilityDirection.POSITIVE)
		// multiplier += ((v - qAvg) / qSd) * w;
		// }
		// }

		return multiplier;
	}

}

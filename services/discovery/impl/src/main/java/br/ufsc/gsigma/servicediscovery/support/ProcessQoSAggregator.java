package br.ufsc.gsigma.servicediscovery.support;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute.QoSValueAggregationType;
import br.ufsc.gsigma.servicediscovery.model.QoSInformation;
import br.ufsc.gsigma.servicediscovery.model.QoSLevel;
import br.ufsc.gsigma.servicediscovery.model.QoSValueComparisionType;
import br.ufsc.gsigma.servicediscovery.support.ProcessStructureParser.ProcessStructureAggregator;
import br.ufsc.gsigma.servicediscovery.support.struct.TaskStructure;
import br.ufsc.gsigma.servicediscovery.support.struct.VirtualTaskStructure;
import br.ufsc.gsigma.servicediscovery.support.struct.VirtualTaskStructure.TaskAggregationType;
import lpsolve.LpSolveException;

public class ProcessQoSAggregator implements ProcessStructureAggregator {

	private static final Logger logger = LoggerFactory.getLogger(ProcessQoSAggregator.class);

	public static final String PARAM_AGGREGATE_QOS_LEVELS = "AGGREGATE_QOS_LEVELS";

	public static final String PARAM_AGGREGATE_QOS_VALUES = "AGGREGATE_QOS_VALUES";

	private Map<String, QoSValueAggregationType> sequentialAggregationType;

	private Map<String, QoSValueAggregationType> parallelAggregationType;

	private Map<String, QoSValueAggregationType> conditionalAggregationType;

	private Map<String, QoSAttribute> qoSAttributes;

	private QoSInformation qoSInformation;

	Map<TaskStructure, Map<String, Integer>> mapSelectedNumberOfLevels = new HashMap<TaskStructure, Map<String, Integer>>();

	public ProcessQoSAggregator(Map<String, QoSAttribute> qoSAttributes) {
		this(qoSAttributes, null);
	}

	public ProcessQoSAggregator(Map<String, QoSAttribute> qoSAttributes, QoSInformation qoSInformation) {

		int n = qoSAttributes.size();

		this.sequentialAggregationType = new HashMap<String, QoSAttribute.QoSValueAggregationType>(n);
		this.parallelAggregationType = new HashMap<String, QoSAttribute.QoSValueAggregationType>(n);
		this.conditionalAggregationType = new HashMap<String, QoSAttribute.QoSValueAggregationType>(n);

		for (Entry<String, QoSAttribute> e : qoSAttributes.entrySet()) {

			String qoSAtt = e.getKey();
			QoSAttribute q = e.getValue();

			sequentialAggregationType.put(qoSAtt, q.getSequentialAggregationType());
			parallelAggregationType.put(qoSAtt, q.getParallelAggregationType());
			conditionalAggregationType.put(qoSAtt, q.getConditionalAggregationType());
		}

		this.qoSAttributes = qoSAttributes;
		this.qoSInformation = qoSInformation;

	}

	public int getCurrentNumberOfQoSLevels(String serviceClassification) {

		int max = 0;

		Map<String, Integer> m = mapSelectedNumberOfLevels.get(serviceClassification);

		if (m != null) {
			for (Integer n : m.values())
				max = Math.max(max, n);
		}

		return max;
	}

	public void selectQoSLevels(VirtualTaskStructure vTask) {
		try {
			selectQoSLevelsInternal(vTask.getTasks(), qoSInformation, sequentialAggregationType);
		} catch (LpSolveException e) {
			throw new RuntimeException(e);
		}
	}

	private void selectQoSLevelsInternal(List<TaskStructure> tasks, QoSInformation qoSInformation, Map<String, QoSValueAggregationType> aggType) throws LpSolveException {

		if (logger.isInfoEnabled()) {

			StringBuilder sb = new StringBuilder();
			for (TaskStructure t : tasks) {
				sb.append("\n\t" + (t instanceof VirtualTaskStructure ? ((VirtualTaskStructure) t).getAggregationType() + "|" : "") + t.getName());
			}

			logger.info("Going to select QoS Levels with qosInfo=" + qoSInformation + " for tasks:" + sb);
		}

		Map<TaskStructure, Map<String, NavigableMap<Integer, List<QoSLevel>>>> qoSLevelsByTaskStructure = new LinkedHashMap<TaskStructure, Map<String, NavigableMap<Integer, List<QoSLevel>>>>();

		for (TaskStructure t : tasks) {
			qoSLevelsByTaskStructure.put(t, t.getQoSLevels());
		}

		Map<String, List<QoSLevel>> bestQoSLevels = ServiceQoSUtil.selectBestQoSLevels(qoSLevelsByTaskStructure, qoSAttributes, aggType, mapSelectedNumberOfLevels, qoSInformation);

		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();

			for (Entry<String, List<QoSLevel>> e : bestQoSLevels.entrySet()) {

				sb.append(e.getKey() + "\n");
				for (QoSLevel l : e.getValue())
					sb.append("\t" + l + "\n");

				logger.debug("bestQoSLevels ->\n" + sb);
			}
		}

		for (TaskStructure t : tasks) {

			List<QoSLevel> levels = bestQoSLevels.get(t.getTaxonomyClassification());

			if (levels != null && !levels.isEmpty()) {

				for (QoSLevel l : levels)
					t.setChoosenQoSLevel(l);

				if (t instanceof VirtualTaskStructure) {

					VirtualTaskStructure vTask = (VirtualTaskStructure) t;

					QoSInformation qInfo = new QoSInformation();

					for (String qoSKey : qoSInformation.getQoSConstraintsKeys()) {
						QoSAttribute qoSAtt = qoSAttributes.get(qoSKey);

						if (qoSAtt == null)
							continue;

						QoSLevel choosenQoSLevel = t.getChoosenQoSLevel(qoSKey);

						if (choosenQoSLevel == null)
							continue;

						double qoSValue = choosenQoSLevel.getValue();
						QoSValueComparisionType comparisionType = qoSInformation.getQoSConstraint(qoSKey).getComparisionType();
						qInfo.addQoSConstraint(qoSKey, comparisionType, qoSValue);
					}

					Map<String, QoSValueAggregationType> qAggType = null;

					if (vTask.getAggregationType() == TaskAggregationType.SEQUENTIAL)
						qAggType = sequentialAggregationType;

					else if (vTask.getAggregationType() == TaskAggregationType.PARALLEL)
						qAggType = parallelAggregationType;

					else if (vTask.getAggregationType() == TaskAggregationType.CONDITIONAL)
						qAggType = conditionalAggregationType;

					List<TaskStructure> taskChilds = vTask.getTasks();

					boolean isCycleAgg = vTask.getAggregationType() == TaskAggregationType.CYCLE;

					if (isCycleAgg) {
						qAggType = sequentialAggregationType;
					}

					selectQoSLevelsInternal(taskChilds, qInfo, qAggType);

				}
			} else {
				logger.warn("Can't determine QoS Levels for task '" + t.getName() + "'");
			}

		}

	}

	@Override
	public VirtualTaskStructure groupSequentialTasks(List<TaskStructure> tasks) {
		VirtualTaskStructure vTask = new VirtualTaskStructure(getCompoundTaskName(tasks), TaskAggregationType.SEQUENTIAL, tasks);
		return vTask;
	}

	@Override
	public VirtualTaskStructure groupCycleTasks(List<TaskStructure> tasks, int repeatCount) {
		VirtualTaskStructure vTask = new VirtualTaskStructure(getCompoundTaskName(tasks), TaskAggregationType.CYCLE, tasks, repeatCount);

		for (TaskStructure t : vTask.getLeafTasks()) {
			t.setRepeatCount(repeatCount);
		}
		return vTask;
	}

	@Override
	public VirtualTaskStructure groupParallelTasks(List<TaskStructure> tasks) {
		VirtualTaskStructure vTask = new VirtualTaskStructure(getCompoundTaskName(tasks), TaskAggregationType.PARALLEL, tasks);
		return vTask;
	}

	@Override
	public VirtualTaskStructure groupConditionalTasks(List<TaskStructure> tasks) {
		VirtualTaskStructure vTask = new VirtualTaskStructure(getCompoundTaskName(tasks), TaskAggregationType.CONDITIONAL, tasks);
		return vTask;
	}

	@Override
	public void visitVirtualTask(VirtualTaskStructure vTask, Map<String, Boolean> params) {
		boolean processQosLevels = params != null ? BooleanUtils.isTrue(params.get(PARAM_AGGREGATE_QOS_LEVELS)) : false;
		boolean processQoSValues = params != null ? BooleanUtils.isTrue(params.get(PARAM_AGGREGATE_QOS_VALUES)) : false;
		calculateQoS(vTask, processQosLevels, processQoSValues);
	}

	public void groupQoSLevels(VirtualTaskStructure vTask) {
		vTask.visitChilds(this, getParams(true, false));
	}

	public void groupQoSValues(VirtualTaskStructure vTask) {
		vTask.visitChilds(this, getParams(false, true));
	}

	public static Map<String, Boolean> getParams(boolean processQosLevels, boolean processQoSValues) {
		Map<String, Boolean> params = new HashMap<String, Boolean>();
		params.put(PARAM_AGGREGATE_QOS_LEVELS, processQosLevels);
		params.put(PARAM_AGGREGATE_QOS_VALUES, processQoSValues);
		return params;
	}

	private void calculateQoS(VirtualTaskStructure vTask, boolean processQosLevels, boolean processQoSValues) {

		if (!processQosLevels && !processQoSValues)
			return;

		String compoundTaskName = vTask.getName();

		Map<String, QoSValueAggregationType> mapAggregationType = //
				vTask.getAggregationType() == TaskAggregationType.SEQUENTIAL ? sequentialAggregationType : //
						vTask.getAggregationType() == TaskAggregationType.PARALLEL ? parallelAggregationType : //
								vTask.getAggregationType() == TaskAggregationType.CONDITIONAL ? conditionalAggregationType : null;

		Map<String, Double> qoSValues = vTask.getQoSValues();
		Map<String, NavigableMap<Integer, List<QoSLevel>>> qoSLevels = vTask.getQoSLevels();

		List<TaskStructure> taskChilds = vTask.getTasks();

		boolean isCycleAgg = vTask.getAggregationType() == TaskAggregationType.CYCLE;

		if (isCycleAgg) {
			mapAggregationType = sequentialAggregationType;
		}

		for (TaskStructure t : taskChilds) {

			// QoS Values
			if (processQoSValues) {
				for (Entry<String, Double> e : t.getQoSValues().entrySet()) {

					String qoSKey = e.getKey();

					QoSValueAggregationType aggType = mapAggregationType.get(qoSKey);

					if (aggType == null)
						continue;

					int cycleMultiplier = t.getClass() == TaskStructure.class ? t.getRepeatCount() + 1 : 1;

					Double v = e.getValue();

					if (aggType == QoSValueAggregationType.SUM) {
						v = v * cycleMultiplier;
					} else if (aggType == QoSValueAggregationType.PRODUCT) {
						v = Math.pow(v, cycleMultiplier);
					}

					Double qv = qoSValues.get(qoSKey);

					if (qv == null) {
						qv = v;
					} else if (aggType == QoSValueAggregationType.SUM) {
						qv = qv + v;
					} else if (aggType == QoSValueAggregationType.PRODUCT) {
						qv = qv * v;
					} else if (aggType == QoSValueAggregationType.MIN) {
						qv = Math.min(qv, v);
					} else if (aggType == QoSValueAggregationType.MAX) {
						qv = Math.max(qv, v);
					}

					qoSValues.put(qoSKey, qv);
				}
			}

			// QoS Levels
			if (processQosLevels) {
				for (Entry<String, NavigableMap<Integer, List<QoSLevel>>> e : t.getQoSLevels().entrySet()) {

					String qoSKey = e.getKey();

					QoSValueAggregationType aggType = mapAggregationType.get(qoSKey);

					if (aggType == null)
						continue;

					NavigableMap<Integer, List<QoSLevel>> mapLevels = qoSLevels.get(qoSKey);
					if (mapLevels == null) {
						mapLevels = new TreeMap<Integer, List<QoSLevel>>();
						qoSLevels.put(qoSKey, mapLevels);
					}

					for (Entry<Integer, List<QoSLevel>> e2 : e.getValue().entrySet()) {

						Integer numberOfLevels = e2.getKey();

						List<QoSLevel> levels = mapLevels.get(numberOfLevels);

						if (levels == null) {
							levels = new ArrayList<QoSLevel>(numberOfLevels);
							mapLevels.put(numberOfLevels, levels);
						}

						int i = 0;

						for (QoSLevel l : e2.getValue()) {

							QoSLevel lp = levels.size() > i ? levels.get(i) : null;

							int cycleMultiplier = t.getClass() == TaskStructure.class ? t.getRepeatCount() + 1 : 1;

							double v = l.getValue();

							if (aggType == QoSValueAggregationType.SUM) {
								v = v * cycleMultiplier;
							} else if (aggType == QoSValueAggregationType.PRODUCT) {
								v = Math.pow(v, cycleMultiplier);
							}

							if (lp == null) {
								lp = new QoSLevel(compoundTaskName, qoSKey, l.getNumber(), numberOfLevels, v, l.getNegativeUtility(), l.getPositiveUtility());
								levels.add(lp);

							} else {

								lp.setNegativeUtility(Math.min(lp.getNegativeUtility(), l.getNegativeUtility()));
								lp.setPositiveUtility(Math.min(lp.getPositiveUtility(), l.getPositiveUtility()));

								if (aggType == QoSValueAggregationType.SUM) {
									lp.setValue(lp.getValue() + v);
								} else if (aggType == QoSValueAggregationType.PRODUCT) {
									lp.setValue(lp.getValue() * v);
								} else if (aggType == QoSValueAggregationType.MIN) {
									lp.setValue(Math.min(lp.getValue(), v));
								} else if (aggType == QoSValueAggregationType.MAX) {
									lp.setValue(Math.max(lp.getValue(), v));
								}
							}

							i++;
						}
					}
				}
			}
		}

		if (logger.isDebugEnabled())

		{

			StringBuilder childSb = new StringBuilder();

			for (TaskStructure t : taskChilds) {
				childSb.append("\tChild: " + t.getName() + ("(" + t.getTaxonomyClassification() + ")") + "\n");
				for (Entry<String, Double> e : t.getQoSValues().entrySet()) {
					if (mapAggregationType.containsKey(e.getKey()))
						childSb.append("\t\t" + e.getKey() + "=" + e.getValue() + "\n");
				}
			}

			StringBuilder aggSb = new StringBuilder();

			for (Entry<String, Double> e : vTask.getQoSValues().entrySet()) {
				aggSb.append("\t\t" + e.getKey() + "=" + e.getValue() + "\n");
			}

			logger.debug("QoS Values Aggregate of type " + vTask.getAggregationType() + " on " + vTask.getName() + "\n" + childSb + "\tAgg:\n" + aggSb);

		}

	}

	private String getCompoundTaskName(List<TaskStructure> tasks) {

		StringBuilder sb = new StringBuilder();

		sb.append("VT(");

		int i = 0;

		for (TaskStructure t : tasks) {

			// Name
			sb.append(t.getName().replaceAll(" ", "_"));

			// InvokeSequence
			if (t.getInvokeSequence() > 1) {
				sb.append("_" + t.getInvokeSequence());
			}

			if (i++ < tasks.size() - 1)
				sb.append(":");

		}
		sb.append(")");
		return sb.toString();
	}

	public void printQoSLevels(String compoundTaskName, Map<String, NavigableMap<Integer, List<QoSLevel>>> qoSLevels) {

		StringWriter sw = new StringWriter();

		PrintWriter pw = new PrintWriter(sw);

		pw.println("*" + compoundTaskName);
		for (Entry<String, NavigableMap<Integer, List<QoSLevel>>> e : qoSLevels.entrySet()) {
			pw.println(e.getKey());
			for (Entry<Integer, List<QoSLevel>> e2 : e.getValue().entrySet()) {
				pw.println("\t" + e2.getKey());
				for (QoSLevel l : e2.getValue()) {
					pw.println("\t\t" + l.getValue());
				}
			}
		}

		logger.info(sw.toString());
	}
}
package br.ufsc.gsigma.catalog.services.persistence.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import br.ufsc.gsigma.catalog.services.model.ConnectableComponent;
import br.ufsc.gsigma.catalog.services.model.Connection;
import br.ufsc.gsigma.catalog.services.model.Document;
import br.ufsc.gsigma.catalog.services.model.EndEvent;
import br.ufsc.gsigma.catalog.services.model.FlowControlComponent;
import br.ufsc.gsigma.catalog.services.model.InputBranch;
import br.ufsc.gsigma.catalog.services.model.InputContactPoint;
import br.ufsc.gsigma.catalog.services.model.OutputBranch;
import br.ufsc.gsigma.catalog.services.model.OutputContactPoint;
import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.catalog.services.model.ProcessCategory;
import br.ufsc.gsigma.catalog.services.model.ProcessStandard;
import br.ufsc.gsigma.catalog.services.model.ProcessTreeDescription;
import br.ufsc.gsigma.catalog.services.model.StartEvent;
import br.ufsc.gsigma.catalog.services.model.Task;
import br.ufsc.gsigma.catalog.services.model.TaskParticipant;
import br.ufsc.gsigma.catalog.services.model.TaskResource;
import br.ufsc.gsigma.catalog.services.persistence.interfaces.CatalogPersistenceService;
import br.ufsc.gsigma.catalog.services.persistence.output.ProcessInfo;

public class CatalogPersistenceServiceImpl implements CatalogPersistenceService {

	private static final Logger logger = LoggerFactory.getLogger(CatalogPersistenceServiceImpl.class);

	@PersistenceContext
	private EntityManager em;

	@Override
	public boolean ping() {
		return true;
	}

	@Override
	@Transactional(readOnly = true)
	public Document getCatalogDocumentById(String id) {
		return em.find(Document.class, id);
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional
	public void removeAllProcesses() {

		List<Integer> ids = (List<Integer>) em.createQuery("SELECT id FROM Process").getResultList();

		for (Integer id : ids)
			em.remove(em.getReference(Process.class, id));

	}

	@Override
	@Transactional
	public Integer addOrUpdateProcess(Integer processCategoryParentId, Process process) {

		Integer existingId;

		try {
			existingId = (Integer) em
					.createQuery("SELECT id FROM Process " + //
							"WHERE processTreeDescription.id = :processCategoryParentId " + //
							"AND name LIKE :name")//
					.setParameter("processCategoryParentId", processCategoryParentId)//
					.setParameter("name", process.getName().trim())//
					.getSingleResult();

			em.remove(em.getReference(Process.class, existingId));

		} catch (Exception e) {
		}

		Map<String, ConnectableComponent> mapConnectableComponent = new HashMap<String, ConnectableComponent>();

		// Fixing cyclic relations

		for (StartEvent e : process.getStartEvents()) {
			e.setProcess(process);
			for (InputContactPoint i : e.getInputContactPoints())
				i.setConnectableComponent(e);
			for (OutputContactPoint o : e.getOutputContactPoints())
				o.setConnectableComponent(e);

			mapConnectableComponent.put(e.getName(), e);
		}

		for (EndEvent e : process.getEndEvents()) {
			e.setProcess(process);
			for (InputContactPoint i : e.getInputContactPoints())
				i.setConnectableComponent(e);
			for (OutputContactPoint o : e.getOutputContactPoints())
				o.setConnectableComponent(e);

			mapConnectableComponent.put(e.getName(), e);
		}

		List<FlowControlComponent> listFlowControlComponent = new ArrayList<FlowControlComponent>();
		listFlowControlComponent.addAll(process.getDecisions());
		listFlowControlComponent.addAll(process.getForks());
		listFlowControlComponent.addAll(process.getJunctions());
		listFlowControlComponent.addAll(process.getMerges());

		for (FlowControlComponent f : listFlowControlComponent) {
			f.setProcess(process);
			for (InputBranch i : f.getInputBranches()) {
				List<InputContactPoint> inputContactPoints = new ArrayList<InputContactPoint>();
				for (InputContactPoint icp : i.getInputContactPoints())
					inputContactPoints.add(f.getInputContactPointByName(icp.getName()));
				i.getInputContactPoints().clear();
				i.getInputContactPoints().addAll(inputContactPoints);
				i.setFlowControlComponent(f);
			}
			for (OutputBranch o : f.getOutputBranches()) {
				List<OutputContactPoint> outputContactPoints = new ArrayList<OutputContactPoint>();
				for (OutputContactPoint ocp : o.getOutputContactPoints())
					outputContactPoints.add(f.getOutputContactPointByName(ocp.getName()));
				o.getOutputContactPoints().clear();
				o.getOutputContactPoints().addAll(outputContactPoints);
				o.setFlowControlComponent(f);
			}
			for (InputContactPoint i : f.getInputContactPoints())
				i.setConnectableComponent(f);
			for (OutputContactPoint o : f.getOutputContactPoints())
				o.setConnectableComponent(f);

			mapConnectableComponent.put(f.getName(), f);
		}

		for (Task t : process.getTasks()) {

			t.setProcess(process);

			for (InputContactPoint i : t.getInputContactPoints())
				i.setConnectableComponent(t);

			for (OutputContactPoint o : t.getOutputContactPoints())
				o.setConnectableComponent(t);

			for (TaskParticipant tp : t.getParticipants())
				tp.setTask(t);

			for (TaskResource tr : t.getResources())
				tr.setTask(t);

			// for (ServiceAssociation s : t.getServiceAssociations())
			// s.setTask(t);
			//
			// for (QoSCriterion q : t.getQoSCriterions())
			// q.setTask(t);

			mapConnectableComponent.put(t.getName(), t);
		}

		List<Connection> newConnectionList = new ArrayList<Connection>();

		for (Connection c : process.getConnections()) {

			OutputContactPoint output = mapConnectableComponent.get(c.getOutput().getConnectableComponentName()).getOutputContactPointByName(c.getOutput().getName());
			InputContactPoint input = mapConnectableComponent.get(c.getInput().getConnectableComponentName()).getInputContactPointByName(c.getInput().getName());
			newConnectionList.add(new Connection(process, output, input));
		}

		process.getConnections().clear();
		process.getConnections().addAll(newConnectionList);

		process.setProcessTreeDescription(em.getReference(ProcessTreeDescription.class, processCategoryParentId));

		em.persist(process);

		return process.getId();

	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<ProcessCategory> getProcessCategories() {

		try {
			return em.createQuery("SELECT p FROM ProcessCategory p").getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ArrayList<ProcessCategory>();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<ProcessStandard> getListProcessStandard() {

		try {
			List<ProcessStandard> result = em.createQuery("SELECT p FROM ProcessStandard p").getResultList();

			return result;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ArrayList<ProcessStandard>();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<ProcessTreeDescription> getListProcessTreeDescription(Integer categoryId) {

		try {
			List<ProcessTreeDescription> result = em.createQuery("SELECT p FROM ProcessTreeDescription p WHERE p.parent IS NULL AND p.category.id = :categoryId").setParameter("categoryId", categoryId)
					.getResultList();

			return result;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ArrayList<ProcessTreeDescription>();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<ProcessInfo> getListProcessInfo(Integer processTreeDescriptionId) {

		try {
			List<ProcessInfo> result = em
					.createQuery(
							"SELECT new br.ufsc.gsigma.catalog.services.persistence.output.ProcessInfo(p.id, p.name, '') FROM Process p WHERE p.processTreeDescription.id = :processTreeDescriptionId ORDER BY p.name")
					.setParameter("processTreeDescriptionId", processTreeDescriptionId).getResultList();

			return result;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ArrayList<ProcessInfo>();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<ProcessInfo> findListProcessInfoByName(String processName) {

		try {
			List<ProcessInfo> result = em
					.createQuery("SELECT new br.ufsc.gsigma.catalog.services.persistence.output.ProcessInfo(p.id, p.name, '') " //
							+ "FROM Process p WHERE p.name LIKE :processName ORDER BY p.name") //
					.setParameter("processName", "%" + processName + "%") //
					.getResultList();

			return result;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ArrayList<ProcessInfo>();
		}

	}

	@Override
	@Transactional(readOnly = true)
	public Process getProcessById(Integer id) {
		return em.find(Process.class, id);
	}

	@Override
	@Transactional(readOnly = true)
	public Process getProcessByName(String name) {

		try {
			return (Process) em.createQuery("SELECT p FROM Process p WHERE p.name = :name") //
					.setParameter("name", name) //
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public boolean checkIfProcessNameIsAvailable(Integer processTreeDescriptionId, String processName) {
		return ((Number) em.createQuery("SELECT COUNT(*) FROM Process WHERE processTreeDescription.id = :processTreeDescriptionId AND name LIKE :processName")
				.setParameter("processTreeDescriptionId", processTreeDescriptionId)//
				.setParameter("processName", processName.trim())//
				.getSingleResult()).intValue() > 0;
	}

}

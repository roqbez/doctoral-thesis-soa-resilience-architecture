package br.ufsc.gsigma.servicediscovery.support;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufsc.gsigma.catalog.services.model.ConnectableComponent;
import br.ufsc.gsigma.catalog.services.model.Connection;
import br.ufsc.gsigma.catalog.services.model.Decision;
import br.ufsc.gsigma.catalog.services.model.EndEvent;
import br.ufsc.gsigma.catalog.services.model.Fork;
import br.ufsc.gsigma.catalog.services.model.InputContactPoint;
import br.ufsc.gsigma.catalog.services.model.Merge;
import br.ufsc.gsigma.catalog.services.model.OutputBranch;
import br.ufsc.gsigma.catalog.services.model.OutputContactPoint;
import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.catalog.services.model.StartEvent;
import br.ufsc.gsigma.catalog.services.model.Task;
import br.ufsc.gsigma.infrastructure.util.collections.ParallelList;
import br.ufsc.gsigma.servicediscovery.support.struct.BranchEndStructure;
import br.ufsc.gsigma.servicediscovery.support.struct.CycleStructure;
import br.ufsc.gsigma.servicediscovery.support.struct.EndStructure;
import br.ufsc.gsigma.servicediscovery.support.struct.GroupStructure;
import br.ufsc.gsigma.servicediscovery.support.struct.SequentialStructure;
import br.ufsc.gsigma.servicediscovery.support.struct.SequentialTaskStructure;
import br.ufsc.gsigma.servicediscovery.support.struct.SplitJoinStructure;
import br.ufsc.gsigma.servicediscovery.support.struct.Structure;
import br.ufsc.gsigma.servicediscovery.support.struct.TaskStructure;
import br.ufsc.gsigma.servicediscovery.support.struct.VirtualTaskStructure;
import br.ufsc.gsigma.servicediscovery.support.struct.VirtualTaskStructure.TaskAggregationType;

public class ProcessStructureParser {

	private static final Logger logger = LoggerFactory.getLogger(ProcessStructureParser.class);

	private Map<Integer, InputContactPoint> inputContactPointMap = new HashMap<Integer, InputContactPoint>();

	private Map<Integer, OutputContactPoint> outputContactPointMap = new HashMap<Integer, OutputContactPoint>();

	private Map<ConnectableComponent, Set<ConnectableComponent>> mapComponentPredecessor = new HashMap<ConnectableComponent, Set<ConnectableComponent>>();

	private Map<ConnectableComponent, Set<ConnectableComponent>> mapComponentSuccessors = new HashMap<ConnectableComponent, Set<ConnectableComponent>>();

	private Map<String, ConnectableComponent> components = new HashMap<String, ConnectableComponent>();

	private List<List<ConnectableComponent>> executionPaths;

	private List<Cycle> cycles;

	private List<List<ConnectableComponent>> taskExecutionPaths;

	private List<List<ConnectableComponent>> executionRoutes;

	private List<List<ConnectableComponent>> taskExecutionRoutes;

	private SequentialStructure sequentialPath;

	private Process process;

	public ProcessStructureParser(Process process) {

		this.process = process;

		List<ConnectableComponent> listConnectableComponent = new ArrayList<ConnectableComponent>();

		listConnectableComponent.addAll(process.getStartEvents());
		listConnectableComponent.addAll(process.getEndEvents());
		listConnectableComponent.addAll(process.getDecisions());
		listConnectableComponent.addAll(process.getJunctions());
		listConnectableComponent.addAll(process.getForks());
		listConnectableComponent.addAll(process.getMerges());

		for (Task t : process.getTasks())
			listConnectableComponent.add(new TaskStructure(t));

		for (ConnectableComponent c : listConnectableComponent) {
			for (InputContactPoint inputContactPoint : c.getInputContactPoints()) {
				inputContactPoint.setConnectableComponent(c);
				inputContactPointMap.put(inputContactPoint.getId(), inputContactPoint);
			}

			for (OutputContactPoint outputContactPoint : c.getOutputContactPoints()) {
				outputContactPoint.setConnectableComponent(c);
				outputContactPointMap.put(outputContactPoint.getId(), outputContactPoint);
			}
		}

		// Set inputs and outputs for the connectables components
		for (Connection connection : process.getConnections()) {

			OutputContactPoint outputContactPoint = outputContactPointMap.get(connection.getOutput().getId());
			InputContactPoint inputContactPoint = inputContactPointMap.get(connection.getInput().getId());

			ConnectableComponent outputComponent = (outputContactPoint != null) ? outputContactPoint.getConnectableComponent() : null;
			ConnectableComponent inputComponent = (inputContactPoint != null) ? inputContactPoint.getConnectableComponent() : null;

			components.put(outputComponent.getName(), outputComponent);
			components.put(inputComponent.getName(), inputComponent);

			// Predecessors
			Set<ConnectableComponent> predecessors = mapComponentPredecessor.get(inputComponent);
			if (predecessors == null) {
				predecessors = new LinkedHashSet<ConnectableComponent>();
				mapComponentPredecessor.put(inputComponent, predecessors);
			}
			predecessors.add(outputComponent);

			// Successors
			Set<ConnectableComponent> successors = mapComponentSuccessors.get(outputComponent);
			if (successors == null) {
				successors = new LinkedHashSet<ConnectableComponent>();
				mapComponentSuccessors.put(outputComponent, successors);
			}
			successors.add(inputComponent);

			connection.getOutput().setConnectableComponent(outputComponent);
			connection.getInput().setConnectableComponent(inputComponent);

		}

		this.cycles = new ArrayList<Cycle>(100);

		// Yu, T., et al. (2007). "Efficient algorithms for Web services selection with end-to-end QoS constraints." ACM Trans. Web 1(1): 6.

		this.executionPaths = identifyExecutionPaths(cycles);

		// for (List<ConnectableComponent> l : this.executionPaths) {
		// System.out.println("\nExecution Path: " + l + "\n");
		// }

		this.taskExecutionPaths = onlyTasks(this.executionPaths);

		// for (List<ConnectableComponent> l : this.taskExecutionPaths) {
		// System.out.println("\nTask Execution Path: " + l);
		// }
		// System.out.println("\n");

		List<List<ConnectableComponent>> lExecutionPaths = replaceComponents(this.executionPaths);

		// boolean joinOnEndIfNecessary = false;

		boolean joinOnEndIfNecessary = true;

		processSplitJoins(lExecutionPaths, joinOnEndIfNecessary, this.cycles);

		// for (List<ConnectableComponent> l : lExecutionPaths) {
		// System.out.println("Execution Path (after split join): " + l);
		// }
		// System.out.println("\n");

		groupSequentialTasks(lExecutionPaths);

		// for (List<ConnectableComponent> l : lExecutionPaths) {
		// System.out.println("Execution Path: " + l);
		// }
		// System.out.println("\n");

		if (!lExecutionPaths.isEmpty()) {

			this.sequentialPath = new SequentialStructure(lExecutionPaths.get(0));

			// System.out.println(this.sequentialPath);

			this.executionRoutes = identifyExecutionRoutes(this.sequentialPath);

			// for (List<ConnectableComponent> l : this.executionRoutes) {
			// System.out.println("Execution Route: " + l + "\n");
			// }
			// System.out.println("\n");

			this.taskExecutionRoutes = onlyTasks(this.executionRoutes);
		}
	}

	// @SuppressWarnings("unused")
	// private List<List<ConnectableComponent>> expandExecutionPathCycles(List<List<ConnectableComponent>> executionPaths, List<Cycle> cycles) {
	//
	// if (cycles.isEmpty()) {
	// return executionPaths;
	// }
	//
	// Map<ConnectableComponent, Set<Cycle>> groupCyclesByStart = new HashMap<ConnectableComponent, Set<Cycle>>();
	//
	// for (int i = 0; i < cycles.size(); i++) {
	// for (int i2 = 0; i2 < cycles.size(); i2++) {
	// Cycle cycle = cycles.get(i);
	// Cycle cycle2 = cycles.get(i2);
	//
	// if (i != i2 && cycle.isSameStart(cycle2)) {
	// Set<Cycle> l = groupCyclesByStart.get(cycle.startComponent);
	// if (l == null) {
	// l = new HashSet<Cycle>();
	// groupCyclesByStart.put(cycle.startComponent, l);
	// }
	// l.add(cycle);
	// l.add(cycle2);
	// }
	// }
	// }
	//
	// // TODO: improve
	// int cycleRepeatNumber = 1;
	//
	// List<List<ConnectableComponent>> expandedExecutionPaths = new LinkedList<List<ConnectableComponent>>();
	// expandedExecutionPaths.addAll(executionPaths);
	//
	// for (Entry<ConnectableComponent, Set<Cycle>> e : groupCyclesByStart.entrySet()) {
	//
	// ConnectableComponent cycleStartComponent = e.getKey();
	//
	// List<List<Cycle>> cyclePermutations = PermutationUtil.permutation(e.getValue(), cycleRepeatNumber);
	//
	// for (List<ConnectableComponent> executionPath : executionPaths) {
	//
	// int idx = executionPath.indexOf(cycleStartComponent);
	//
	// if (idx != -1) {
	//
	// for (List<Cycle> cyclePermutation : cyclePermutations) {
	//
	// List<ConnectableComponent> expandedExecutionPath = new LinkedList<ConnectableComponent>(executionPath);
	//
	// List<ConnectableComponent> cycleExecutionPath = cyclePermutation.stream() //
	// .map(c -> c.executionPath) //
	// .flatMap(ep -> ep.stream()) //
	// .collect(Collectors.toList());
	//
	// expandedExecutionPath.addAll(idx, cycleExecutionPath);
	//
	// Map<String, Integer> mapTaskInvokeSequence = new HashMap<String, Integer>();
	//
	// ListIterator<ConnectableComponent> it = expandedExecutionPath.listIterator();
	//
	// while (it.hasNext()) {
	//
	// ConnectableComponent c = it.next();
	//
	// if (c.getClass() == TaskStructure.class) {
	// Integer invokeSequence = mapTaskInvokeSequence.get(c.getName());
	// if (invokeSequence == null) {
	// invokeSequence = 1;
	// } else {
	// invokeSequence++;
	//
	// c = new TaskStructure(c.getName(), ((TaskStructure) c).getTaxonomyClassification());
	// ((TaskStructure) c).setInvokeSequence(invokeSequence);
	//
	// it.remove();
	// it.add(c);
	// }
	// mapTaskInvokeSequence.put(c.getName(), invokeSequence);
	// }
	// }
	//
	// expandedExecutionPaths.add(expandedExecutionPath);
	// }
	// }
	// }
	//
	// }
	//
	// return expandedExecutionPaths;
	// }

	private ConnectableComponent getTargetComponentFromInputContactPointName(String contactSourceName) {

		for (Connection connection : process.getConnections()) {

			if (contactSourceName.equals(connection.getOutput().getName())) {
				InputContactPoint inputContactPoint = inputContactPointMap.get(connection.getInput().getId());
				ConnectableComponent inputComponent = (inputContactPoint != null) ? inputContactPoint.getConnectableComponent() : null;
				return inputComponent;
			}
		}

		return null;
	}

	private List<List<ConnectableComponent>> onlyTasks(List<List<ConnectableComponent>> input) {

		List<List<ConnectableComponent>> result = new ArrayList<List<ConnectableComponent>>(input.size());

		for (List<ConnectableComponent> ep : input) {

			List<ConnectableComponent> executionPath = new LinkedList<ConnectableComponent>(ep);

			ListIterator<ConnectableComponent> it = executionPath.listIterator();

			while (it.hasNext()) {

				ConnectableComponent c = it.next();

				if (!(c instanceof TaskStructure))
					it.remove();

			}
			result.add(executionPath);
		}

		return result;
	}

	public void printStructure() {
		printStructure(sequentialPath.getChilds());
	}

	public void printStructure(List<ConnectableComponent> components) {
		printStructure(components, 0, null);
	}

	private void printStructure(List<ConnectableComponent> components, int level, String prefix) {

		StringWriter sw = new StringWriter();

		PrintWriter pw = new PrintWriter(sw);

		for (ConnectableComponent c : components) {

			for (int i = 0; i < level; i++) {
				pw.print("\t");
			}

			if (prefix != null)
				pw.print(prefix);

			if (c instanceof SplitJoinStructure) {
				pw.println("SplitStructure");

				int k = 1;

				for (List<ConnectableComponent> c3 : ((SplitJoinStructure) c).getBranches()) {
					printStructure(c3, level + 1, k++ + " - ");
				}

			} else {
				pw.println(c);
			}
		}

		logger.info(sw.toString());
	}

	public VirtualTaskStructure getVirtualTask(ProcessStructureAggregator visitor) {
		return getVirtualTask(getSequentialPath(), visitor);
	}

	public VirtualTaskStructure getVirtualTask(SequentialStructure sequentialStructure, ProcessStructureAggregator visitor) {

		transformIntoSequentialStructure(sequentialStructure.getChilds(), visitor);

		VirtualTaskStructure vTask = new VirtualTaskStructure("Root Virtual Task", TaskAggregationType.SEQUENTIAL);

		for (ConnectableComponent c : sequentialStructure.getChilds()) {
			if (c instanceof TaskStructure)
				vTask.addTask((TaskStructure) c);
		}

		return vTask;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void transformIntoSequentialStructure(List<ConnectableComponent> components, ProcessStructureAggregator visitor) {

		ListIterator<ConnectableComponent> it = components.listIterator();

		while (it.hasNext()) {

			ConnectableComponent c = it.next();

			if (c instanceof SequentialTaskStructure) {

				TaskStructure taskStructure = null;

				List<ConnectableComponent> childs = ((SequentialTaskStructure) c).getChilds();

				if (childs.size() > 1)
					taskStructure = visitor.groupSequentialTasks((List) childs);
				else
					taskStructure = (TaskStructure) childs.get(0);

				it.remove();
				it.add(taskStructure);

			} else if (c instanceof CycleStructure) {

				List<ConnectableComponent> childs = ((CycleStructure) c).getChilds();

				transformIntoSequentialStructure(childs, visitor);

				List<TaskStructure> tasks = new ArrayList<TaskStructure>(childs.size());

				List<TaskStructure> sequentialTasks = new LinkedList<TaskStructure>();

				for (ConnectableComponent c2 : childs) {
					if (c2 instanceof TaskStructure) {
						sequentialTasks.add((TaskStructure) c2);
					} else if (c2 instanceof SequentialTaskStructure) {
						sequentialTasks.addAll(((SequentialTaskStructure) c2).getTasks());
					}
				}

				if (sequentialTasks.size() > 1) {
					tasks.add(visitor.groupSequentialTasks(sequentialTasks));
				} else if (sequentialTasks.size() == 1) {
					tasks.add(sequentialTasks.get(0));
				}

				TaskStructure taskStructure = visitor.groupCycleTasks(tasks, ((CycleStructure) c).getRepeatCount());

				it.remove();
				it.add(taskStructure);

			} else if (c instanceof SplitJoinStructure) {

				boolean conditional = ((SplitJoinStructure) c).isConditional();

				Collection<List<ConnectableComponent>> branches = ((SplitJoinStructure) c).getBranches();

				List<TaskStructure> tasks = new ArrayList<TaskStructure>(branches.size());

				for (List<ConnectableComponent> b : branches) {

					transformIntoSequentialStructure(b, visitor);

					List<TaskStructure> sequentialTasks = new LinkedList<TaskStructure>();

					for (ConnectableComponent c2 : b) {
						if (c2 instanceof TaskStructure) {
							sequentialTasks.add((TaskStructure) c2);
						} else if (c2 instanceof SequentialTaskStructure) {
							sequentialTasks.addAll(((SequentialTaskStructure) c2).getTasks());
						}
					}
					if (sequentialTasks.size() > 1) {
						tasks.add(visitor.groupSequentialTasks(sequentialTasks));
					} else if (sequentialTasks.size() == 1) {
						tasks.add(sequentialTasks.get(0));
					}
				}

				TaskStructure taskStructure = conditional ? visitor.groupConditionalTasks(tasks) : visitor.groupParallelTasks(tasks);

				it.remove();
				it.add(taskStructure);
			}

		}

	}

	private void processSplitJoins(List<List<ConnectableComponent>> executionPaths, boolean joinOnEndIfNecessary, List<Cycle> cycles) {

		Set<CycleBounds> cyclesBounds = new HashSet<CycleBounds>();

		for (Cycle c : cycles) {
			cyclesBounds.add(c.getBounds());
		}

		// cycles.stream().map(x -> x.getBounds()).collect(Collectors.toSet());

		Set<ConnectableComponent> handledCycleEndComponentes = new HashSet<ConnectableComponent>();

		processSplitJoinsInternal(executionPaths, joinOnEndIfNecessary, cyclesBounds, handledCycleEndComponentes);

		List<ConnectableComponent> executionPath = executionPaths.get(0);

		ListIterator<ConnectableComponent> it = executionPath.listIterator();

		CycleStructure cycleStructure = null;

		while (it.hasNext()) {

			ConnectableComponent c = it.next();

			if (c instanceof CycleStart) {
				it.remove();
				cycleStructure = new CycleStructure();
				it.add(cycleStructure);
			} else if (c instanceof CycleEnd) {
				it.remove();
				cycleStructure = null;
			} else if (cycleStructure != null) {
				cycleStructure.getChilds().add(c);
				it.remove();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void processSplitJoinsInternal(List<List<ConnectableComponent>> executionPaths, boolean joinOnEndIfNecessary, Set<CycleBounds> cyclesBounds, Set<ConnectableComponent> handledCycleEndComponents) {

		SplitJoin splitJoin = identifySplitJoin(executionPaths, joinOnEndIfNecessary);

		while (splitJoin != null) {

			List<ConnectableComponent> firstExecutionPath = executionPaths.get(0);

			boolean taskSplit = splitJoin.splitComponent instanceof TaskStructure || splitJoin.splitComponent instanceof SequentialTaskStructure;

			List<ConnectableComponent> before = firstExecutionPath.subList(0, taskSplit ? splitJoin.splitIndex + 1 : splitJoin.splitIndex);

			Set<List<ConnectableComponent>> branches = new LinkedHashSet<List<ConnectableComponent>>();
			Set<List<ConnectableComponent>> after = new LinkedHashSet<List<ConnectableComponent>>();

			int i = 0;
			for (List<ConnectableComponent> l : executionPaths) {

				int w = splitJoin.joinIndexes[i++];

				branches.add(l.subList(splitJoin.splitIndex + 1, w));

				ConnectableComponent c = l.get(w);

				if (c instanceof SequentialTaskStructure)
					after.add(l.subList(w, l.size()));
				else
					after.add(l.subList(w + 1, l.size()));
			}

			executionPaths.clear();

			for (List<ConnectableComponent> l : after) {

				List<ConnectableComponent> executionPath = new ArrayList<ConnectableComponent>(before.size() + 1 + l.size());

				for (ConnectableComponent c : before) {

					// // TODO: improve
					for (CycleBounds cycleBounds : cyclesBounds) {
						if (cycleBounds.isStartComponent(c)) {
							executionPath.add(new CycleStart(cycleBounds));
						} else if (cycleBounds.isEndComponent(c)) {
							executionPath.add(new CycleEnd(cycleBounds));
						}
					}

					if (c instanceof SequentialTaskStructure || c instanceof SplitJoinStructure || c instanceof CycleStart || c instanceof CycleEnd) {
						executionPath.add(c);
					}
				}

				Map<ConnectableComponent, Collection<List<ConnectableComponent>>> groupByFirst = new LinkedHashMap<ConnectableComponent, Collection<List<ConnectableComponent>>>();

				for (List<ConnectableComponent> b : branches) {
					if (!b.isEmpty()) {
						ConnectableComponent first = b.get(0);
						Collection<List<ConnectableComponent>> g = groupByFirst.get(first);
						if (g == null) {
							g = new ArrayList<List<ConnectableComponent>>(branches.size());
							groupByFirst.put(first, g);
						}
						g.add(b);
					}
				}

				boolean isConditional = splitJoin.splitComponent instanceof Decision;

				SplitJoinStructure splitJoinStruct = new SplitJoinStructure(groupByFirst.values(), isConditional, splitJoin.splitComponent, splitJoin.joinComponent);

				executionPath.add(splitJoinStruct);

				// TODO: improve
				for (List<ConnectableComponent> b : branches) {
					for (ConnectableComponent c : new ArrayList<ConnectableComponent>(b)) {
						c = c instanceof SequentialTaskStructure && ((SequentialTaskStructure) c).getChilds().size() == 1 ? ((SequentialTaskStructure) c).getFirst() : c;

						for (CycleBounds cycleBounds : cyclesBounds) {
							if (cycleBounds.isEndComponent(c) && !handledCycleEndComponents.contains(c)) {

								handledCycleEndComponents.add(c);

								boolean containsCycleStart = false;

								for (ConnectableComponent cc : executionPath) {
									if (cc instanceof CycleStart) {
										containsCycleStart = true;
										continue;
									}
								}

								CycleEnd cycleEnd = new CycleEnd(cycleBounds);

								if (containsCycleStart) {
									executionPath.add(cycleEnd);

									// Alterado para funcionar com o ciclo do Billing with Credit Note (12/09/2018)
								} else {

									List<ConnectableComponent> currentBranch = new ArrayList<ConnectableComponent>(b);
									removeForksMergesAndDecisions(Collections.singleton(currentBranch));
									groupSequentialTasks(Collections.singleton(currentBranch));

									splitJoinStruct.updateBranches();

									List<List<ConnectableComponent>> cyclesExecutionPaths = new LinkedList<List<ConnectableComponent>>();

									for (Cycle cycle : cycles) {
										if (cycle.getBounds().equals(cycleEnd.cycleBounds)) {
											cyclesExecutionPaths.add(cycle.executionPath);
										}
									}

									// Ugly hack
									// cyclesExecutionPaths = (List<List<ConnectableComponent>>) SerializationUtils.clone((Serializable) cyclesExecutionPaths);

									List<List<ConnectableComponent>> cyclesExecutionPathsCopy = new LinkedList<List<ConnectableComponent>>();

									for (List<ConnectableComponent> cep : cyclesExecutionPaths) {
										cyclesExecutionPathsCopy.add(new LinkedList<ConnectableComponent>(cep));
									}

									cyclesExecutionPaths = cyclesExecutionPathsCopy;

									removeForksMergesAndDecisions(cyclesExecutionPaths);

									cyclesExecutionPaths = replaceComponents(cyclesExecutionPaths);

									processSplitJoins(cyclesExecutionPaths, true, new ArrayList<Cycle>());
									groupSequentialTasks(cyclesExecutionPaths);

									List<ConnectableComponent> cycleExecutionPath = cyclesExecutionPaths.get(0);

									ConnectableComponent branchComponents = currentBranch.get(0);

									List<ConnectableComponent> cycleComponents = new LinkedList<ConnectableComponent>();
									cycleComponents.add(branchComponents);

									for (ConnectableComponent cc : cycleExecutionPath) {
										if (!cc.equals(branchComponents)) {
											cycleComponents.add(cc);
										}
									}

									b.clear();
									b.add(new CycleStructure(cycleComponents, 1));
									splitJoinStruct.updateBranches();
								}
							}
						}
					}
				}

				executionPath.addAll(l);

				if (GroupStructure.class.isAssignableFrom(executionPaths.getClass()))
					executionPaths.add(new GroupStructure(executionPath));
				else
					executionPaths.add(executionPath);
			}

			splitJoin = identifySplitJoin(executionPaths, joinOnEndIfNecessary);
		}

		// Recursive
		for (List<ConnectableComponent> executionPath : executionPaths) {

			for (ConnectableComponent c : executionPath) {

				if (c instanceof SplitJoinStructure) {
					for (ConnectableComponent s : ((SplitJoinStructure) c).getChilds()) {
						if (((GroupStructure) s).size() > 1)
							processSplitJoinsInternal((List<List<ConnectableComponent>>) s, true, cyclesBounds, handledCycleEndComponents);
					}
				}
			}
		}
	}

	private boolean allEquals(List<ConnectableComponent> l) {

		ConnectableComponent current = null;
		for (ConnectableComponent c : l) {
			if (current == null)
				current = c;
			else {
				if (!current.equals(c))
					return false;
				current = c;
			}
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public Set<ConnectableComponent> getComponentPredecessors(ConnectableComponent component) {

		if (component instanceof SequentialStructure)
			component = ((SequentialStructure) component).getFirst();

		Set<ConnectableComponent> l = mapComponentPredecessor.get(component);
		if (l == null)
			l = Collections.EMPTY_SET;
		return l;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set<ConnectableComponent> getComponentSuccessors(ConnectableComponent component) {

		if (component instanceof SequentialStructure)
			component = ((SequentialStructure) component).getLast();

		Set<ConnectableComponent> l = mapComponentSuccessors.get(component);
		if (l == null) {

			boolean end = component instanceof EndEvent || component instanceof EndStructure;

			if (!end)
				l = (Set) Collections.singleton(EndStructure.INSTANCE);
			else
				l = Collections.EMPTY_SET;
		}
		return l;
	}

	public ConnectableComponent getConnectableComponent(Class<? extends ConnectableComponent> clazz) {

		List<ConnectableComponent> l = getConnectableComponents(clazz);

		if (l.size() == 1)
			return l.get(0);
		else
			return null;
	}

	public List<ConnectableComponent> getConnectableComponents(Class<? extends ConnectableComponent> clazz) {

		List<ConnectableComponent> result = new LinkedList<ConnectableComponent>();

		for (ConnectableComponent c : components.values()) {
			if (clazz.isAssignableFrom(c.getClass()))
				result.add(c);
		}
		return result;
	}

	public TaskStructure getTaskByName(String name) {
		for (ConnectableComponent c : components.values()) {
			if (c instanceof TaskStructure && name.equals(c.getName()))
				return (TaskStructure) c;
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<TaskStructure> getTasks() {
		return (List) getConnectableComponents(TaskStructure.class);
	}

	public List<List<ConnectableComponent>> getExecutionsPaths() {
		return executionPaths;
	}

	public List<List<ConnectableComponent>> getExecutionRoutes() {
		return executionRoutes;
	}

	public List<List<ConnectableComponent>> getTaskExecutionRoutes() {
		return taskExecutionRoutes;
	}

	public List<Cycle> getCycles() {
		return cycles;
	}

	public List<List<ConnectableComponent>> getTaskExecutionPaths() {
		return taskExecutionPaths;
	}

	public SequentialStructure getSequentialPath() {
		return sequentialPath;
	}

	private ConnectableComponent getFirstCommonComponent(List<List<ConnectableComponent>> lists, int start, int[] joinIndexes) {

		int w = 0;

		for (List<ConnectableComponent> l : lists) {

			for (int z = start; z < l.size(); z++) {

				ConnectableComponent c = l.get(z);

				boolean allContains = true;

				int w1 = 0;

				for (List<ConnectableComponent> l1 : lists) {
					if (w1 != w) {
						boolean contains = false;
						for (int z1 = start; z1 < l1.size(); z1++) {
							ConnectableComponent c1 = l1.get(z1);
							if (c.equals(c1)) {
								contains = true;
								joinIndexes[w1] = z1;
								break;
							}
						}
						allContains = allContains && contains;
					}
					w1++;
				}

				if (allContains) {
					joinIndexes[w] = z;
					return c;
				}
			}

			w++;
		}

		return null;
	}

	public ConnectableComponent getInputComponentForConnection(Connection connection) {
		InputContactPoint inputContactPoint = inputContactPointMap.get(connection.getInput().getId());
		return (inputContactPoint != null) ? inputContactPoint.getConnectableComponent() : null;
	}

	public ConnectableComponent getOutputComponentForConnection(Connection connection) {
		OutputContactPoint outputContactPoint = outputContactPointMap.get(connection.getOutput().getId());
		return (outputContactPoint != null) ? outputContactPoint.getConnectableComponent() : null;
	}

	public Process getProcess() {
		return process;
	}

	private List<List<ConnectableComponent>> replaceComponents(List<List<ConnectableComponent>> executionPaths) {

		List<List<ConnectableComponent>> result = new ArrayList<List<ConnectableComponent>>(executionPaths.size());

		for (List<ConnectableComponent> executionPath : executionPaths) {

			executionPath = new LinkedList<ConnectableComponent>(executionPath);

			ListIterator<ConnectableComponent> it = executionPath.listIterator();

			// Replace Task with SequentialTask
			EndStructure end = EndStructure.INSTANCE;
			while (it.hasNext()) {
				ConnectableComponent c = it.next();
				if (c instanceof TaskStructure)
					it.set(new SequentialTaskStructure((TaskStructure) c));
				else if (c instanceof EndEvent) {
					it.set(end);
				}
			}

			result.add(executionPath);
		}
		return result;
	}

	private void removeForksMergesAndDecisions(Collection<List<ConnectableComponent>> executionPaths) {

		for (List<ConnectableComponent> executionPath : executionPaths) {

			ListIterator<ConnectableComponent> it = executionPath.listIterator();

			while (it.hasNext()) {
				ConnectableComponent c = it.next();
				if (c instanceof Fork || c instanceof Decision || c instanceof Merge) {
					it.remove();
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void groupSequentialTasks(Collection<List<ConnectableComponent>> executionPaths) {

		for (List<ConnectableComponent> executionPath : executionPaths) {

			// Trying to group (min of 2)
			if (executionPath.size() < 2)
				continue;

			ListIterator<ConnectableComponent> it = executionPath.listIterator();

			while (it.hasNext()) {

				ConnectableComponent c = it.next();

				boolean isTask = c instanceof SequentialTaskStructure;

				// Trying to detect if is a sequence of tasks
				if (isTask) {

					Set<ConnectableComponent> successors = getComponentSuccessors(c);

					ConnectableComponent f = !successors.isEmpty() ? successors.iterator().next() : null;

					if (successors.size() == 1 && f instanceof TaskStructure && getComponentPredecessors(f).size() == 1) {

						ConnectableComponent next = f;

						SequentialTaskStructure seq = new SequentialTaskStructure();
						seq.addTask((SequentialTaskStructure) c);
						seq.addTask((TaskStructure) next);

						while (true) {

							successors = getComponentSuccessors(next);

							f = !successors.isEmpty() ? successors.iterator().next() : null;

							if (successors.size() == 1 && f instanceof TaskStructure && getComponentPredecessors(f).size() == 1) {
								next = f;
								seq.addTask((TaskStructure) next);
							} else {

								it.remove();

								for (int w = 0; w < seq.getChilds().size() - 1; w++) {
									it.next();
									it.remove();
								}

								it.add(seq);
								seq = null;
								break;
							}
						}
					}

					// Recursive
				} else if (c instanceof SplitJoinStructure) {
					List<ConnectableComponent> childs = ((SplitJoinStructure) c).getChilds();
					List paths = new ArrayList(childs.size());
					for (ConnectableComponent s : childs) {
						if (s instanceof GroupStructure) {
							paths.add(((GroupStructure) s).getChilds());
						}
					}
					groupSequentialTasks(paths);

				} else if (c instanceof GroupStructure) {
					List singlePath = new ArrayList(1);
					singlePath.add(((GroupStructure) c).getChilds());
					groupSequentialTasks(singlePath);
				}
			}

		}
	}

	private ConnectableComponent getStartComponent() {
		ConnectableComponent startEvent = getConnectableComponent(StartEvent.class);

		if (startEvent == null) {
			for (ConnectableComponent c : components.values()) {

				Set<ConnectableComponent> predecessors = getComponentPredecessors(c);

				if (predecessors == null || predecessors.isEmpty()) {
					startEvent = c;
					break;
				}
			}
		}
		return startEvent;
	}

	private List<List<ConnectableComponent>> identifyExecutionRoutes(SequentialStructure root) {
		return identifyExecutionRoutesInternal(root.getChilds(), true);
	}

	@SuppressWarnings({ "unchecked" })
	private List<List<ConnectableComponent>> identifyExecutionRoutesInternal(List<ConnectableComponent> rootChilds, boolean processExecutionPaths) {

		List<ConnectableComponent> firstPath = new LinkedList<ConnectableComponent>();

		for (ConnectableComponent c0 : rootChilds) {

			List<ConnectableComponent> l = new LinkedList<ConnectableComponent>();
			Queue<ConnectableComponent> q = (Queue<ConnectableComponent>) l;
			q.add(c0);

			while (!q.isEmpty()) {

				ConnectableComponent c = q.poll();

				if (!(c instanceof Structure)) {
					continue;
				}

				List<ConnectableComponent> childs = ((Structure) c).getChilds();

				if (c instanceof GroupStructure || c instanceof CycleStructure) {
					l.addAll(0, childs);
					continue;

				} else if (c instanceof SplitJoinStructure) {

					boolean conditional = ((SplitJoinStructure) c).isConditional();
					Collection<List<ConnectableComponent>> branches = ((SplitJoinStructure) c).getBranches();

					if (conditional) {

						Collection<Collection<List<ConnectableComponent>>> branchesCopy = new ArrayList<Collection<List<ConnectableComponent>>>(branches.size());

						for (List<ConnectableComponent> b : branches) {
							branchesCopy.add(identifyExecutionRoutesInternal(b, false));

						}
						firstPath.add(new SplitJoinStructure(branchesCopy, true, ((SplitJoinStructure) c).getSplitComponent(), ((SplitJoinStructure) c).getJoinComponent()));

					} else {

						ParallelList<ConnectableComponent> pList = new ParallelList<ConnectableComponent>(branches);
						for (List<ConnectableComponent> l2 : pList) {
							for (ConnectableComponent c2 : l2)
								if (c2 != null)
									q.add(c2);
						}
					}

				} else {
					firstPath.addAll(childs);
				}

			}
		}

		List<List<ConnectableComponent>> executionRoutes = new LinkedList<List<ConnectableComponent>>();
		executionRoutes.add(new LinkedList<ConnectableComponent>(firstPath));

		if (processExecutionPaths) {

			ListIterator<List<ConnectableComponent>> it = executionRoutes.listIterator();

			while (it.hasNext()) {

				List<ConnectableComponent> path = it.next();

				int i = 0;

				for (ConnectableComponent c : path) {

					if (c instanceof SplitJoinStructure) {

						Collection<List<ConnectableComponent>> branches = ((SplitJoinStructure) c).getBranches();
						int n = branches.size();

						List<List<ConnectableComponent>> newExecutionRoutes = new ArrayList<List<ConnectableComponent>>(n);

						List<ConnectableComponent> before = path.subList(0, i);
						List<ConnectableComponent> after = i < path.size() ? path.subList(i + 1, path.size()) : null;

						it.remove();

						for (int k = 1; k <= n; k++) {
							List<ConnectableComponent> er = new LinkedList<ConnectableComponent>(before);
							newExecutionRoutes.add(er);
							it.add(er);
						}

						int k = 0;
						for (List<ConnectableComponent> b : branches) {
							List<ConnectableComponent> ner = newExecutionRoutes.get(k++);
							for (ConnectableComponent c2 : b) {
								if (c2 instanceof SequentialTaskStructure)
									ner.addAll(((SequentialTaskStructure) c2).getChilds());
								else
									ner.add(c2);

								// if (c2 instanceof SplitJoinStructure)
								// it.previous();

							}
						}

						for (List<ConnectableComponent> ner : newExecutionRoutes) {

							it.previous();

							if (after != null)
								ner.addAll(after);
						}

						// for (List<ConnectableComponent> tmp : executionRoutes)
						// System.out.println(tmp);
						// System.out.println();

						break;
					}
					i++;
				}
			}
		}

		return executionRoutes;
	}

	private List<List<ConnectableComponent>> identifyExecutionPaths(List<Cycle> cycles) {

		List<List<ConnectableComponent>> executionPaths = new LinkedList<List<ConnectableComponent>>();

		ConnectableComponent startEvent = getStartComponent();

		LinkedList<ConnectableComponent> current = new LinkedList<ConnectableComponent>();
		executionPaths.add(current);

		visitExecutionPaths(startEvent, current, executionPaths, null, cycles);

		ListIterator<List<ConnectableComponent>> it = executionPaths.listIterator();

		while (it.hasNext()) {
			LinkedList<ConnectableComponent> ep = (LinkedList<ConnectableComponent>) it.next();
			if (checkDecisionWithoutTask(ep, cycles)) {
				it.remove();
			}
		}

		// for (List<ConnectableComponent> l : executionPaths) {
		// System.out.println("\nExecution Path (before expand cycles): " + l + "\n");
		// }

		// executionPaths = expandExecutionPathCycles(executionPaths, cycles);

		return executionPaths;
	}

	private ConnectableComponent unwrap(ConnectableComponent c) {

		if (c instanceof SequentialTaskStructure)
			return ((SequentialTaskStructure) c).getFirst();
		else
			return c;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean visitExecutionPaths(ConnectableComponent c, LinkedList<ConnectableComponent> current, List<List<ConnectableComponent>> executionsPathsOrRoutes, ConnectableComponent stopComponent, List<Cycle> cycles) {

		// Detected a cycle. Trying to identify the last conditional branch
		if (current.contains(c)) {

			Cycle cycle = new Cycle(current, c);
			cycles.add(cycle);

			outer_loop: for (int i = current.size() - 1; i >= 0; i--) {
				ConnectableComponent previous = current.get(i);
				if (previous instanceof Decision && i < current.size() - 1) {

					ConnectableComponent afterDecisionComponent = current.get(i + 1);

					for (OutputBranch ob : ((Decision) previous).getOutputBranches()) {

						for (OutputContactPoint oc : ob.getOutputContactPoints()) {

							ConnectableComponent targetComponent = getTargetComponentFromInputContactPointName(oc.getName());

							if (afterDecisionComponent.equals(targetComponent)) {
								cycle.branchProbabilities.add(ob.getProbabilityPercentage());
								break outer_loop;
							}
						}
					}
				}
			}

			return false;
		}

		current.add(c);

		Set<ConnectableComponent> componentSuccessors = unwrap(c) != unwrap(stopComponent) ? getComponentSuccessors(c) : null;

		if (componentSuccessors != null && !componentSuccessors.isEmpty()) {

			int n = componentSuccessors.size();

			LinkedList[] newExecutionPaths = null;

			if (n > 1) {
				newExecutionPaths = new LinkedList[n - 1];

				for (int i = 0; i < n - 1; i++) {
					newExecutionPaths[i] = new LinkedList<ConnectableComponent>(current);
					executionsPathsOrRoutes.add(newExecutionPaths[i]);
				}
			}

			int i = 0;

			// // A decision starts a new execution path or execution route
			//
			for (ConnectableComponent s : componentSuccessors) {

				// New execution path or route
				if (i > 0) {
					current = newExecutionPaths[i - 1];

					// Ajuste para funcionar com Billing Process with Credit Note (12/09/2018)
					if (c instanceof Fork) {
						executionsPathsOrRoutes.add(current);
					}

				}

				visitExecutionPaths(s, current, executionsPathsOrRoutes, stopComponent, cycles);

				// boolean isCycle = !visitExecutionPaths(s, current, executionsPathsOrRoutes, stopComponent, cycles);
				//
				// if (isCycle) {
				// executionsPathsOrRoutes.remove(executionsPathsOrRoutes.size() - 1);
				// }

				i++;
			}

		}

		return true;

	}

	private boolean checkDecisionWithoutTask(LinkedList<ConnectableComponent> current, List<Cycle> cycles) {
		// Discard paths where the last decision doesn't have a task successor
		Iterator<ConnectableComponent> it = current.descendingIterator();

		int taskIdx = -1;
		int decisionIdx = -1;

		int w = current.size() - 1;

		while (it.hasNext()) {

			if (taskIdx > 0 && decisionIdx > 0) {
				break;
			}

			ConnectableComponent obj = it.next();

			if (obj instanceof TaskStructure) {
				taskIdx = w;

			} else if (obj instanceof Decision) {
				decisionIdx = w;

				if (decisionIdx == current.size() - 1) {
					for (Cycle c : cycles) {
						if (c.getEndComponent().equals(obj)) {
							decisionIdx = -1;
							break;
						}
					}
				}

			}
			w--;
		}

		boolean isDecisionWithoutTask = decisionIdx > 0 && decisionIdx > taskIdx;
		return isDecisionWithoutTask;
	}

	private SplitJoin identifySplitJoin(List<List<ConnectableComponent>> executionPaths, boolean joinOnEndIfNecessary) {

		if (executionPaths.size() < 2)
			return null;

		ParallelList<ConnectableComponent> parallelList = new ParallelList<ConnectableComponent>(executionPaths);

		ConnectableComponent splitComponent = null;

		Integer splitComponentIndex = null;

		Iterator<List<ConnectableComponent>> it = parallelList.iterator();

		int i = 0;

		// Finding the split component
		while (it.hasNext()) {
			List<ConnectableComponent> l = it.next();
			if (!allEquals(l)) {
				break;
			} else {
				splitComponentIndex = i;
				splitComponent = l.get(0);
			}
			i++;
		}

		if (splitComponent == null)
			return null;

		int[] joinIndexes = new int[executionPaths.size()];

		ConnectableComponent joinComponent = getFirstCommonComponent(executionPaths, splitComponentIndex + 1, joinIndexes);

		if (joinComponent == null) {
			if (!joinOnEndIfNecessary)
				return null;

			int w = 0;

			BranchEndStructure branchEnd = new BranchEndStructure();

			for (List<ConnectableComponent> l : executionPaths) {
				joinIndexes[w++] = l.size();
				l.add(branchEnd);
			}

			joinComponent = branchEnd;

		}

		return new SplitJoin(splitComponent, splitComponentIndex, joinComponent, joinIndexes);

	}

	private class SplitJoin {

		ConnectableComponent splitComponent;
		ConnectableComponent joinComponent;

		int splitIndex;

		int[] joinIndexes;

		SplitJoin(ConnectableComponent splitComponent, int splitIndex, ConnectableComponent joinComponent, int[] joinIndexes) {
			this.splitComponent = splitComponent;
			this.splitIndex = splitIndex;
			this.joinComponent = joinComponent;
			this.joinIndexes = joinIndexes;
		}

		@Override
		public String toString() {

			Set<Integer> s = new HashSet<Integer>(joinIndexes.length);

			for (int i : joinIndexes)
				s.add(i);

			return "SplitJoin[split->" + splitComponent + " (i=" + splitIndex + ", join->" + joinComponent + " (i=" + s + ")]";
		}
	}

	private class CycleStart extends Structure {

		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unused")
		CycleBounds cycleBounds;

		CycleStart(CycleBounds cycleBounds) {
			this.cycleBounds = cycleBounds;
		}
	}

	private class CycleEnd extends Structure {

		private static final long serialVersionUID = 1L;

		CycleBounds cycleBounds;

		CycleEnd(CycleBounds cycleBounds) {
			this.cycleBounds = cycleBounds;
		}
	}

	private class CycleBounds {

		@SuppressWarnings("unused")
		Cycle cycle;

		ConnectableComponent startComponent;

		ConnectableComponent endComponent;

		public CycleBounds(Cycle cycle) {
			this.cycle = cycle;
		}

		boolean isStartComponent(ConnectableComponent component) {
			return startComponent.equals(component);
		}

		boolean isEndComponent(ConnectableComponent component) {
			return endComponent.equals(component);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((endComponent == null) ? 0 : endComponent.hashCode());
			result = prime * result + ((startComponent == null) ? 0 : startComponent.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CycleBounds other = (CycleBounds) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (endComponent == null) {
				if (other.endComponent != null)
					return false;
			} else if (!endComponent.equals(other.endComponent))
				return false;
			if (startComponent == null) {
				if (other.startComponent != null)
					return false;
			} else if (!startComponent.equals(other.startComponent))
				return false;
			return true;
		}

		private ProcessStructureParser getOuterType() {
			return ProcessStructureParser.this;
		}

	}

	class Cycle {

		CycleBounds bounds = new CycleBounds(this);

		List<ConnectableComponent> executionPath;

		List<Float> branchProbabilities = new ArrayList<Float>();

		int repeatCount = CycleStructure.DEFAULT_REPEAT_COUNT;

		Cycle(List<ConnectableComponent> executionPath, ConnectableComponent start) {

			this.executionPath = new ArrayList<ConnectableComponent>(executionPath.size());

			this.bounds.startComponent = start;
			this.bounds.endComponent = executionPath.get(executionPath.size() - 1);

			boolean shouldAdd = false;

			for (ConnectableComponent c : executionPath) {
				if (shouldAdd || c.equals(start)) {
					shouldAdd = true;
					if (!this.executionPath.isEmpty() && c.equals(start)) {
						break;
					} else {
						this.executionPath.add(c);
					}
				}
			}
		}

		public ConnectableComponent getStartComponent() {
			return this.bounds.startComponent;
		}

		public ConnectableComponent getEndComponent() {
			return this.bounds.endComponent;
		}

		public CycleBounds getBounds() {
			return bounds;
		}

		boolean containsComponent(ConnectableComponent c) {
			return executionPath.contains(c);
		}

		boolean isSameStartEnd(Cycle c) {
			return isSameStart(c) && isSameEnd(c);
		}

		boolean isSameStart(Cycle c) {
			return //
			this.executionPath.get(0).equals(c.executionPath.get(0));
		}

		boolean isSameEnd(Cycle c) {
			return this.executionPath.get(this.executionPath.size() - 1).equals(c.executionPath.get(c.executionPath.size() - 1));
		}

		@Override
		public String toString() {
			return this.executionPath.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((executionPath == null) ? 0 : executionPath.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Cycle other = (Cycle) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (executionPath == null) {
				if (other.executionPath != null)
					return false;
			} else if (!executionPath.equals(other.executionPath))
				return false;
			return true;
		}

		private ProcessStructureParser getOuterType() {
			return ProcessStructureParser.this;
		}

	}

	public interface ProcessStructureAggregator {

		public VirtualTaskStructure groupSequentialTasks(List<TaskStructure> tasks);

		public VirtualTaskStructure groupParallelTasks(List<TaskStructure> tasks);

		public VirtualTaskStructure groupConditionalTasks(List<TaskStructure> tasks);

		public void visitVirtualTask(VirtualTaskStructure vTask, Map<String, Boolean> params);

		public VirtualTaskStructure groupCycleTasks(List<TaskStructure> tasks, int repeatCount);

	}

}

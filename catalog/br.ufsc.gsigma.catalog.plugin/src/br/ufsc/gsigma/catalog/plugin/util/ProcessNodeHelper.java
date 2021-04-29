package br.ufsc.gsigma.catalog.plugin.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.ibm.btools.blm.ui.navigation.manager.util.BLMEditorInput;
import com.ibm.btools.blm.ui.navigation.manager.util.BLMManagerUtil;
import com.ibm.btools.blm.ui.navigation.model.AbstractLibraryChildNode;
import com.ibm.btools.blm.ui.navigation.model.AbstractNode;
import com.ibm.btools.blm.ui.navigation.model.NavigationProcessCatalogNode;
import com.ibm.btools.blm.ui.navigation.model.NavigationProcessNode;
import com.ibm.btools.blm.ui.navigation.model.NavigationProcessesNode;
import com.ibm.btools.blm.ui.navigation.model.NavigationProjectNode;
import com.ibm.btools.blm.ui.navigation.model.NavigationRoot;
import com.ibm.btools.bom.model.processes.activities.StructuredActivityNode;
import com.ibm.btools.ui.framework.BToolsEditor;

public abstract class ProcessNodeHelper {

	public static List<StructuredActivityNode> getProcessTasks(StructuredActivityNode node) {

		List<StructuredActivityNode> result = new ArrayList<StructuredActivityNode>(node.getNodeContents().size());

		for (Object o : node.getNodeContents()) {
			if (o instanceof StructuredActivityNode) {
				result.add((StructuredActivityNode) o);
			}
		}
		return result;
	}

	public static NavigationProjectNode[] getExistingProjectNodes() {
		List<NavigationProjectNode> result = new ArrayList<NavigationProjectNode>();

		for (NavigationProjectNode n : getNavigationRoot().getProjectNodes())
			if (!BLMManagerUtil.isPredefinedProject(n))
				result.add(n);

		return result.toArray(new NavigationProjectNode[result.size()]);
	}

	public static String[] getExistingProjects() {
		NavigationProjectNode[] projecNodes = getExistingProjectNodes();

		String[] result = new String[projecNodes.length];

		int i = 0;
		for (NavigationProjectNode n : projecNodes)
			result[i++] = n.getLabel();

		return result;
	}

	public static NavigationProcessNode getProcess(String projectName, String processName) {

		for (NavigationProcessNode p : getProcesses(projectName)) {
			if (p.getLabel().equals(processName))
				return p;
		}
		return null;
	}

	public static List<NavigationProcessNode> getProcesses(String projectName) {

		List<NavigationProcessNode> result = new LinkedList<NavigationProcessNode>();

		for (NavigationProjectNode projectNode : getExistingProjectNodes()) {

			if (projectNode.getLabel().equals(projectName)) {

				for (NavigationProcessCatalogNode processCatalog : projectNode.getLibraryNode().getProcessCatalogsNodes().getProcessCatalogNodes()) {
					NavigationProcessesNode processesNode = processCatalog.getProcessesNode();
					result.addAll(processesNode.getProcessNodes());
				}
			}
		}

		return result;
	}

	private static NavigationRoot getNavigationRoot() {
		return BLMManagerUtil.getNavigationRoot();
	}

	private static AbstractNode getCurrentlySelectedNode() {

		if (BLMManagerUtil.getNavigationTreeViewer() != null) {

			ISelection selection = BLMManagerUtil.getNavigationTreeViewer().getSelection();

			if (selection.isEmpty()) {

				for (NavigationProjectNode n : getNavigationRoot().getProjectNodes())
					if (!BLMManagerUtil.isPredefinedProject(n))
						return n;

			}
			if (selection instanceof StructuredSelection)
				return (AbstractNode) ((StructuredSelection) selection).getFirstElement();
		} else {
			IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			if (activeEditor instanceof BToolsEditor) {
				IEditorInput editorInput = (BLMEditorInput) ((BToolsEditor) activeEditor).getEditorInput();
				return ((BLMEditorInput) editorInput).getNode();
			}
		}
		return null;
	}

	public static String getCurrentProjectSelection() {

		AbstractNode selectedNode = getCurrentlySelectedNode();

		if (selectedNode instanceof NavigationProjectNode)
			return ((NavigationProjectNode) selectedNode).getLabel();

		else if (selectedNode instanceof AbstractLibraryChildNode)
			return ((AbstractLibraryChildNode) selectedNode).getProjectNode().getLabel();

		return null;

	}
	// public static String getCurrentProjectSelection() {
	//
	// Object selectedNode = getCurrentlySelectedNode();
	//
	// if (selectedNode instanceof NavigationProjectNode)
	// return ((NavigationProjectNode) selectedNode).getLabel();
	//
	// else if (selectedNode instanceof AbstractChildLeafNode)
	// return ((AbstractChildLeafNode) selectedNode).getProjectNode().getLabel();
	//
	// else if (selectedNode instanceof AbstractLibraryChildNode)
	// return ((AbstractLibraryChildNode) selectedNode).getProjectNode().getLabel();
	//
	// else if (selectedNode instanceof NavigationBusinessGroupsNode)
	// return ((NavigationBusinessGroupsNode) selectedNode).getProjectNode().getLabel();
	//
	// else if (selectedNode instanceof NavigationBusinessGroupsNode)
	// return ((NavigationBusinessGroupNode) selectedNode).getProjectNode().getLabel();
	//
	// else if (selectedNode instanceof AbstractBusinessGroupsChildNode)
	// return ((AbstractBusinessGroupsChildNode) selectedNode).getProjectNode().getLabel();
	//
	// else if (selectedNode instanceof NavigationLibraryNode)
	// return ((NavigationLibraryNode) selectedNode).getProjectNode().getLabel();
	//
	// return null;
	// }

}

package utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JTree;

import com.apstex.gui.core.kernel.Kernel;
import com.apstex.gui.core.model.applicationmodel.ApplicationModelNode;
import com.apstex.gui.core.model.selectionmodel.SelectionModelListener;
import com.apstex.step.core.ClassInterface;

/**
 * 
 * @author Marcel Stepien
 *
 */
public class CoreUtilities {

	public static ArrayList<ClassInterface> getSelectedInstances(){
		ArrayList<ClassInterface> result = new ArrayList<ClassInterface>();
		List<ApplicationModelNode> nodeList = Kernel.getApplicationModelRoot().getNodes();
		for(ApplicationModelNode node : nodeList) {
			result.addAll(((ApplicationModelNode)node).getSelectionModel().getSelectedObjects(ClassInterface.class));
	    }
	    return result;
	}
	
	public static void setSelectedInstances(ArrayList<ClassInterface> ifcInstances) {
		//Get root node
		ApplicationModelNode node = Kernel.getApplicationModelRoot().getNode(0);

		//Clear selection 
		node.getSelectionModel().clearSelection(new SelectionModelListener() {
			@Override
			public void objectsSelected(Collection<Object> arg0, boolean arg1) {
				//DO NOTHING SPECIAL IF SELECTED
			}
		}, ClassInterface.class);
		
		//Select Ifc Objects
		node.getSelectionModel().select(new SelectionModelListener() {
			@Override
			public void objectsSelected(Collection<Object> arg0, boolean arg1) {
				//DO NOTHING SPECIAL IF SELECTED
			}
		}, ifcInstances);
	}
	
	/**
	 * Unfold or Expand all entries in a JTree
	 * 
	 * @param tree
	 * @param startingIndex
	 * @param rowCount
	 */
	public static void expandAllNodes(JTree tree, int startingIndex, int rowCount){
	    for(int i=startingIndex;i<rowCount;++i){
	        tree.expandRow(i);
	    }

	    /*if(tree.getRowCount()!=rowCount){
	        expandAllNodes(tree, rowCount, tree.getRowCount());
	    }*/
	}
}

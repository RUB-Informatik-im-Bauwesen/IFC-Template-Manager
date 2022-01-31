package components.ifc;

import java.util.ArrayList;
import java.util.Map;

import com.apstex.gui.core.model.applicationmodel.ApplicationModelNode;
import com.apstex.gui.ifc.controller.IfcLoadManager;
import com.apstex.ifctoolbox.ifcmodel.IfcModel;
import com.apstex.step.model.StepModel;

public class CustomIfcLoaderManager extends IfcLoadManager {
	 
    private static CustomIfcLoaderManager instance = null;
	private static ArrayList<IReloadableView> reloadables = null;
    
    public static CustomIfcLoaderManager getInstance() {
    	if(instance==null) {
			instance = new CustomIfcLoaderManager();
		}	
		return instance;
	}
	
	private CustomIfcLoaderManager() {
		reloadables = new ArrayList<>();
	}
	
	
	@Override
	protected ApplicationModelNode createApplicationModelNode(StepModel stepModel, Map<String, Object> arg1, Object arg2) {
		ApplicationModelNode node = super.createApplicationModelNode(stepModel, arg1, arg2);
		
		if(arg1 != null) {
			if(arg1.containsKey("MODEL_FILE_NAME")) {
				node.setModelName(arg1.get("MODEL_FILE_NAME").toString());
			}
		}
		
		if(stepModel instanceof IfcModel) {
			//((IfcModel)stepModel).addIfcModelListener();
		}
		
//		Transform3D ifcTransform = new Transform3D();
//		ifcTransform.setTranslation(new Vector3d
//				(0, 
//				 0, 
//				 0));
//		node.getCadObjectModel().getRootBranchGroup().setTransform(ifcTransform);
//		
		
		return node;
	}

	/**
	 * call to reload registered views
	 */
	public void notifyAllReloadables() {
		for(IReloadableView reloadable : reloadables) {
			reloadable.reload();
		}
	}
	
	public void addReloadable(IReloadableView reloadable) {
		this.reloadables.add(reloadable);
	}
	
}

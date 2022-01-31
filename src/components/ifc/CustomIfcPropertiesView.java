package components.ifc;

import java.awt.Component;
import java.util.Collection;

import com.apstex.gui.core.kernel.Kernel;
import com.apstex.gui.core.model.applicationmodel.ApplicationModelNode;
import com.apstex.gui.core.model.selectionmodel.SelectionModel;
import com.apstex.gui.core.model.selectionmodel.SelectionModelListener;
import com.apstex.gui.core.util.multiviewpane.MultiViewPane;
import com.apstex.gui.ifc.views.propertiesview.PropertiesView;

public class CustomIfcPropertiesView extends PropertiesView implements IReloadableView{
	
	private MultiViewPane multiViewPane = null;
	
	public CustomIfcPropertiesView() {
		super();
		CustomIfcLoaderManager.getInstance().addReloadable(this);
	
		for(Component comp : this.getComponents()) {
			if(comp instanceof MultiViewPane) {
				this.multiViewPane = (MultiViewPane)comp;
			}
		}
	}

	@Override
	public void reload() {		
		multiViewPane.nodesRemoved(Kernel.getApplicationModelRoot().getNodes());
		multiViewPane.nodesAdded(Kernel.getApplicationModelRoot().getNodes());
		
		//reselect all
		for(ApplicationModelNode mNode : Kernel.getApplicationModelRoot().getNodes()) {
			SelectionModel sModel = mNode.getSelectionModel();
			sModel.setSelected(new SelectionModelListener() {
				@Override
				public void objectsSelected(Collection<Object> arg0, boolean arg1) {
					//DO NOTHING
				}
			}, sModel.getSelectedObjects(), true);
		}
		
		this.updateUI();
		multiViewPane.revalidate();
		multiViewPane.updateUI();
	}
}

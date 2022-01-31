package components.templating;

import java.io.IOException;

import javax.swing.JPopupMenu;

import com.apstex.ifctoolbox.ifc.IfcProjectLibrary;
import com.apstex.ifctoolbox.ifc.IfcPropertySetTemplate;

import components.actions.ApplicationActions;
import components.actions.ExportMvdAction;
import components.actions.ExportToCsvAction;
import javafx.scene.control.TreeItem;
import utils.ApplicationUtilities;

/**
 * Creates a popup menu when right-clicking the template project tree.
 *  
 * @author Marcel Stepien
 *
 */
public class TemplateProjectPopupMenu extends JPopupMenu{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3338293517145854675L;

	/**
	 * Constructor
	 * 
	 * @param title
	 * @param treeItem
	 * @throws IOException
	 */
	public TemplateProjectPopupMenu(String title, TreeItem<ItemContainer<?>> treeItem) throws IOException {
		super(title);
	
		ItemContainer<?> container = treeItem.getValue();
		
		//add action bindings
		this.add(ApplicationUtilities.createMenuItem("Add new Template", ApplicationActions.createNewTemplate, this.getClass().getResourceAsStream("../../icons/new.png")));
		this.add(ApplicationUtilities.createMenuItem("Add new Library", ApplicationActions.createNewLibrary, this.getClass().getResourceAsStream("../../icons/new.png")));
		
		this.addSeparator();

		this.add(ApplicationUtilities.createMenuItem("Remove", new ApplicationActions.RemoveAction(treeItem), null)); //this.getClass().getResourceAsStream("../../icons/delete.gif")));
		this.add(ApplicationUtilities.createMenuItem("Edit", ApplicationActions.editAction, null)); // this.getClass().getResourceAsStream("../../icons/preferences.gif")));
		
		this.addSeparator();
		
		this.add(ApplicationUtilities.createMenuItem("Append to Selected", ApplicationActions.appendTemplateToSelection, this.getClass().getResourceAsStream("../../icons/preference.png")));
		
		//DEPRECATED
		//this.add(ApplicationUtilities.createMenuItem("Append to ...", ApplicationActions.appendTemplateToModel, this.getClass().getResourceAsStream("../../icons/maximize.gif")));
	
		//this.addSeparator();
		//this.add(ApplicationUtilities.createMenuItem("Remove Properties", ApplicationActions.removeTemplateFromModel, null)); // this.getClass().getResourceAsStream("../../icons/open.gif")));
		
		// Der Kontext beim rechts-klick eines IfcPropertySetTemplate.Ifc4
		if(container.getItem() instanceof IfcPropertySetTemplate.Ifc4) {
			this.addSeparator();

			this.add(ApplicationUtilities.createMenuItem("Export PSD", new ApplicationActions.ExportPSDAction
					((ItemContainer<IfcPropertySetTemplate.Ifc4>) container), 
					this.getClass().getResourceAsStream("../../icons/export.png")));
			this.add(ApplicationUtilities.createMenuItem("Export MVD", 
					new ExportMvdAction(treeItem), 
					this.getClass().getResourceAsStream("../../icons/export.png")));
		
		}

		// Der Kontext beim rechts-klick eines IfcProjectLibrary.Ifc4
		if(container.getItem() instanceof IfcProjectLibrary.Ifc4) {
			this.addSeparator();

			this.add(ApplicationUtilities.createMenuItem("Import PSD", new ApplicationActions.ImportPSDAction(treeItem), this.getClass().getResourceAsStream("../../icons/import.png")));
			this.add(ApplicationUtilities.createMenuItem("export Library to PSD ", new ApplicationActions.ExportPSDLibraryAction(treeItem), this.getClass().getResourceAsStream("../../icons/export.png")));
			this.add(ApplicationUtilities.createMenuItem("export Library to CSV ", new ExportToCsvAction(treeItem), this.getClass().getResourceAsStream("../../icons/export.png")));
			this.add(ApplicationUtilities.createMenuItem("export Library to MVD",
					new ExportMvdAction(treeItem),
					this.getClass().getResourceAsStream("../../icons/export.png")));
		}
	}
}

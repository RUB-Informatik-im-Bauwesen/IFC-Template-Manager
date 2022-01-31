package components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import components.actions.ApplicationActions;
import components.managers.enumeration.ManageEnumerationsFrame;
import utils.ApplicationUtilities;

/**
 * Contains the menubar content.
 * 
 * @author Marcel Stepien
 *
 */
public class ApplicationMenuBar extends JMenuBar
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ApplicationMenuBar(final ApplicationFrame editor) throws IOException
	{
		// Creates the file menu
		JMenu menu = add(new JMenu("File"));

		menu.add(ApplicationUtilities.createMenuItem("Create new Template File", ApplicationActions.createNewProject, this.getClass().getResourceAsStream("../icons/new.png")));
		
		menu.addSeparator();
		
		menu.add(ApplicationUtilities.createMenuItem("Open IFC Template File", ApplicationActions.openTemplateFile, this.getClass().getResourceAsStream("../icons/import.png")));
		menu.add(ApplicationUtilities.createMenuItem("Save IFC Template File", ApplicationActions.saveTemplateFile, this.getClass().getResourceAsStream("../icons/save.png")));

		menu.addSeparator();

		menu.add(ApplicationUtilities.createMenuItem("Import IFC Model", ApplicationActions.openModelFile, null)); // this.getClass().getResourceAsStream("../icons/ifc.png")));
		menu.add(ApplicationUtilities.createMenuItem("Export IFC Model", ApplicationActions.saveModelFile, null)); //  this.getClass().getResourceAsStream("../icons/ifc.png")));

		//Create the edit menu
		JMenu edit = add(new JMenu("Edit"));
		
		edit.add(ApplicationUtilities.createMenuItem("Add new Template", ApplicationActions.createNewTemplate, this.getClass().getResourceAsStream("../icons/new.png")));
		edit.add(ApplicationUtilities.createMenuItem("Add new Library", ApplicationActions.createNewLibrary, this.getClass().getResourceAsStream("../icons/new.png")));
		
		edit.addSeparator();

		//add action bindings
		edit.add(ApplicationUtilities.createMenuItem("Append to Selected", ApplicationActions.appendTemplateToSelection, null)); //  this.getClass().getResourceAsStream("../icons/maximize.gif")));
		edit.add(ApplicationUtilities.createMenuItem("Append to ...", ApplicationActions.appendTemplateToModel, null)); //  this.getClass().getResourceAsStream("../icons/maximize.gif")));

		edit.addSeparator();

		edit.add(ApplicationUtilities.createMenuItem("Manage Enumerations", new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ManageEnumerationsFrame enumerationsFrame = new ManageEnumerationsFrame(ApplicationUtilities.template, null);
				
			}
		}, this.getClass().getResourceAsStream("../icons/preferences.png")));
		
		edit.addSeparator();

		edit.add(ApplicationUtilities.createMenuItem("Remove Properties", ApplicationActions.removeTemplateFromModel, null)); //  this.getClass().getResourceAsStream("../icons/open.gif")));
		
	}
	
};
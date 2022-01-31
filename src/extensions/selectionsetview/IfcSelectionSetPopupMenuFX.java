package extensions.selectionsetview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JPopupMenu;

import com.apstex.gui.core.kernel.Kernel;
import com.apstex.gui.core.model.applicationmodel.ApplicationModelNode;
import com.apstex.gui.core.model.cadobjectmodel.CadObject;
import com.apstex.step.core.ClassInterface;

import utils.ApplicationUtilities;


/**
 * Contains the PopUp-Menu Content if a template is right clicked.
 *  
 * @author Marcel Stepien
 *
 */
public class IfcSelectionSetPopupMenuFX extends JPopupMenu{
	
	private Object item;

	public IfcSelectionSetPopupMenuFX(String title, Object item) {
		super(title);
		this.item = item;
	}	
	
	private ArrayList<ClassInterface> retriveIfcObjects(Object item){
		ArrayList<ClassInterface> ifcObjs = new ArrayList<ClassInterface>();
		if(item instanceof SelectionSet) {
			ifcObjs.addAll(((SelectionSet)item).getSelection());
		}
		if(item instanceof ArrayList<?>) {
			for(Object innerItem : ((ArrayList<?>)item)) {
				if(innerItem instanceof SelectionSet) {
					ifcObjs.addAll(((SelectionSet)innerItem).getSelection());
				}
				if(innerItem instanceof ArrayList<?>) {
					ifcObjs.addAll((ArrayList)innerItem);
				}
				if(innerItem instanceof ClassInterface) {
					ifcObjs.add((ClassInterface)innerItem);
				}
			}
		}
		if(item instanceof ClassInterface) {
			ifcObjs.add((ClassInterface)item);
		}
		return ifcObjs;
	}
	
	public void inititializeContent() throws IOException {
		//add action bindings
		this.add(ApplicationUtilities.createMenuItem(
				"Select", 
				new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						for(ApplicationModelNode node : Kernel.getApplicationModelRoot().getNodes()) {
							node.getSelectionModel().select(null, retriveIfcObjects(item));
						}
					}
				}, 
				this.getClass().getResourceAsStream("../../icons/add.png"))
		);
	
		this.add(ApplicationUtilities.createMenuItem(
				"Deselect", 
				new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						for(ApplicationModelNode node : Kernel.getApplicationModelRoot().getNodes()) {
							node.getSelectionModel().deselect(null, retriveIfcObjects(item));
						}
					}
				},
				this.getClass().getResourceAsStream("../../icons/del.png"))
		);
	
		this.addSeparator();

		this.add(ApplicationUtilities.createMenuItem(
				"Hide", 
				new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						for(ApplicationModelNode node : Kernel.getApplicationModelRoot().getNodes()) {

							for(ClassInterface ifcObj : retriveIfcObjects(item)) {			
								CadObject cadObj = node.getCadObjectModel().getCadObject(ifcObj);
								if(cadObj != null) {
									node.getVisibilityModel().setVisible(
										cadObj,
										false, 
										null
									);
								}
							}
			
						}
					}
				},
				this.getClass().getResourceAsStream("../../icons/hide.png"))
		);
		
		this.add(ApplicationUtilities.createMenuItem(
				"Show", 
				new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						for(ApplicationModelNode node : Kernel.getApplicationModelRoot().getNodes()) {

							for(ClassInterface ifcObj : retriveIfcObjects(item)) {			
								CadObject cadObj = node.getCadObjectModel().getCadObject(ifcObj);
								if(cadObj != null) {
									node.getVisibilityModel().setVisible(
										cadObj,  
										true, 
										null
									);
								}
							}
			
						}
					}
				},
				this.getClass().getResourceAsStream("../../icons/show.png"))
		);

		this.add(ApplicationUtilities.createMenuItem(
				"Show Only", 
				new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						
						for(ApplicationModelNode node : Kernel.getApplicationModelRoot().getNodes()) {
							node.getVisibilityModel().setVisible(
									node.getCadObjectModel().getCadObjects(), 
									false, 
									null
							);
						}
						
						for(ApplicationModelNode node : Kernel.getApplicationModelRoot().getNodes()) {

							for(ClassInterface ifcObj : retriveIfcObjects(item)) {
								CadObject cadObj = node.getCadObjectModel().getCadObject(ifcObj);
								if(cadObj != null) {
									node.getVisibilityModel().setVisible(
											cadObj, 
											true, 
											null
									);
								}
								
							}
			
						}
					}
				}, 
				this.getClass().getResourceAsStream("../../icons/show_only.png"))
		);
		
	}

}

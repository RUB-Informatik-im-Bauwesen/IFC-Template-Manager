package components.editors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;

import com.apstex.ifctoolbox.ifc.IfcPropertyEnumeration;
import com.apstex.ifctoolbox.ifc.IfcStateEnum;
import com.apstex.ifctoolbox.ifc.IfcUnit;

import components.managers.enumeration.ManageEnumerationsFrame;
import components.managers.unit.ManageUnitsFrame;
import javafx.scene.control.TreeItem;
import utils.ApplicationUtilities;

/**
 * Contains the PopUp-Menu Content if a template is right clicked.
 * 
 * @author Marcel Stepien
 *
 */
public class CreatePropertySetPopupMenuFX extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private TreeItem<Object> item = null;
	private CreatePropertySetFrameFX owner = null;

	public CreatePropertySetPopupMenuFX(CreatePropertySetFrameFX owner, String title, TreeItem<Object> item) {
		super(title);
		this.owner = owner;
		this.item = item;
	}

	public void inititializeContent() throws IOException {

		// add action bindings
		if (item.getValue() instanceof Object[]) {
			Object[] data = (Object[])item.getValue();
			if(data[2].equals("P_ENUMERATEDVALUE")) {
				
				this.add(ApplicationUtilities.createMenuItem("Set Enumeration", new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						
						ManageEnumerationsFrame enumerationsFrame = new ManageEnumerationsFrame(ApplicationUtilities.template, data);
						enumerationsFrame.addWindowListener(new WindowAdapter() {
							@Override
							public void windowClosed(WindowEvent e) {
								if(data[5] instanceof IfcPropertyEnumeration.Ifc4 && data[4] instanceof IfcUnit.Ifc4) {
									((IfcPropertyEnumeration.Ifc4)data[5]).setUnit((IfcUnit.Ifc4)data[4]);
									//System.out.println("Unit added to enum!");
								}//Set unit to enumeration if both are defined
								
								System.out.println("Refreshing Template Table");
								owner.refreshTemplateTable();
								super.windowClosed(e);
							}
						});
						
					}
				}, this.getClass().getResourceAsStream("../icons/preferences.gif")));
				
				this.add(ApplicationUtilities.createMenuItem("Remove Enumeration", new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						data[5] = null;
						System.out.println("Refreshing Template Table");
						owner.refreshTemplateTable();
					}
				}, this.getClass().getResourceAsStream("../icons/delete.gif")));
				
			}
		
			this.add(ApplicationUtilities.createMenuItem("Set Unit", new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					
					ManageUnitsFrame unitFrame = new ManageUnitsFrame(ApplicationUtilities.template, data);
					unitFrame.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosed(WindowEvent e) {
							if(data[5] instanceof IfcPropertyEnumeration.Ifc4 && data[4] instanceof IfcUnit.Ifc4) {
								((IfcPropertyEnumeration.Ifc4)data[5]).setUnit((IfcUnit.Ifc4)data[4]);
								//System.out.println("Unit added to enum!");
							}//Set unit to enumeration if both are defined
						    
							System.out.println("Refreshing Template Table");
							owner.refreshTemplateTable();
							super.windowClosed(e);
						}
					});
					
				}
			}, this.getClass().getResourceAsStream("../icons/preferences.gif")));
			
			this.add(ApplicationUtilities.createMenuItem("Remove Unit", new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					data[4] = null;
					System.out.println("Refreshing Template Table");
					owner.refreshTemplateTable();
				}
			}, this.getClass().getResourceAsStream("../icons/delete.gif")));
			
			this.addSeparator();
			
			String lower = "";
			String secondaryType = "";
			
			if(data[7] != null) {
				lower = data[7].toString();
			}
			
			if(data[8] != null) {
				secondaryType = data[8].toString();
			}

			/*
			this.add(new JLabel("Lower Bound:"));
			this.add(ApplicationUtilities.createTextbox(lower, new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent e) {
					if(e.getSource() instanceof JTextField) {
						data[7] = ((JTextField)e.getSource()).getText();
					}
				}
			}));

			this.addSeparator();
			*/
			
			this.add(new JLabel("Secondary Measure Type:"));
			this.add(ApplicationUtilities.createTextbox(secondaryType, new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent e) {
					if(e.getSource() instanceof JTextField) {
						data[8] = ((JTextField)e.getSource()).getText();
					}
				}
			}));

			this.addSeparator();
			
			JMenu accessStateMenu = new JMenu("Set AccessState");
			
			ActionListener al = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					data[6] = IfcStateEnum.Ifc4.IfcStateEnum_internal.valueOf(((JRadioButtonMenuItem)e.getSource()).getText());
				}
			};
			JRadioButtonMenuItem itemA = new JRadioButtonMenuItem(IfcStateEnum.Ifc4.IfcStateEnum_internal.LOCKED.name());
			itemA.addActionListener(al);
			if(((IfcStateEnum.Ifc4.IfcStateEnum_internal)data[6]).name().equals(itemA.getText())) {
				itemA.setSelected(true);
			}
			
			JRadioButtonMenuItem itemB = new JRadioButtonMenuItem(IfcStateEnum.Ifc4.IfcStateEnum_internal.READONLY.name());
			itemB.addActionListener(al);
			if(((IfcStateEnum.Ifc4.IfcStateEnum_internal)data[6]).name().equals(itemB.getText())) {
				itemB.setSelected(true);
			}
			
			JRadioButtonMenuItem itemC = new JRadioButtonMenuItem(IfcStateEnum.Ifc4.IfcStateEnum_internal.READONLYLOCKED.name());
			itemC.addActionListener(al);
			if(((IfcStateEnum.Ifc4.IfcStateEnum_internal)data[6]).name().equals(itemC.getText())) {
				itemC.setSelected(true);
			}
			
			JRadioButtonMenuItem itemD = new JRadioButtonMenuItem(IfcStateEnum.Ifc4.IfcStateEnum_internal.READWRITE.name());
			itemD.addActionListener(al);
			if(((IfcStateEnum.Ifc4.IfcStateEnum_internal)data[6]).name().equals(itemD.getText())) {
				itemD.setSelected(true);
			}
			
			JRadioButtonMenuItem itemE = new JRadioButtonMenuItem(IfcStateEnum.Ifc4.IfcStateEnum_internal.READWRITELOCKED.name());
			itemE.addActionListener(al);
			if(((IfcStateEnum.Ifc4.IfcStateEnum_internal)data[6]).name().equals(itemE.getText())) {
				itemE.setSelected(true);
			}
			
	        ButtonGroup bgroup = new ButtonGroup();
	        bgroup.add(itemA);
	        bgroup.add(itemB);
	        bgroup.add(itemC);
	        bgroup.add(itemD);
	        bgroup.add(itemE);
	        
	        accessStateMenu.add(itemA);
	        accessStateMenu.add(itemB);
	        accessStateMenu.add(itemC);
	        accessStateMenu.add(itemD);
	        accessStateMenu.add(itemE);
			
			this.add(accessStateMenu);
			

			
		}

	}

}

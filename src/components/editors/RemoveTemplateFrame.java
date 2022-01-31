package components.editors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.apstex.ifctoolbox.ifc.IfcElementQuantity;
import com.apstex.ifctoolbox.ifc.IfcObject;
import com.apstex.ifctoolbox.ifc.IfcObjectDefinition;
import com.apstex.ifctoolbox.ifc.IfcPropertySet;
import com.apstex.ifctoolbox.ifc.IfcPropertySetDefinition;
import com.apstex.ifctoolbox.ifc.IfcPropertySetDefinitionSelect;
import com.apstex.ifctoolbox.ifc.IfcRelDefinesByProperties;
import com.apstex.ifctoolbox.ifcmodel.IfcModel;

import components.actions.ApplicationActions;
import components.ifc.CustomIfcLoaderManager;
import components.io.ModelTransfer;
import components.templating.ItemContainer;
import utils.ApplicationUtilities;

/**
 * A context menu for removing attached properties from objects of the model.
 * 
 * @author Marcel Stepien
 *
 */
public class RemoveTemplateFrame extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2823522318254812865L;
	
	private JTable objectDefTable = null;
	private JTable propertieTable = null;
	
	private JPanel objectDefPanel = null;
	private JPanel propertiePanel = null;
	
	private String[] headerObjectDef = null;
	private String[] headerPropertie = null;
	
	/**
	 * Constructor
	 */
	public RemoveTemplateFrame() {
		super();
		
		this.setTitle("Remove Properties from Object-Definitions");
	
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.setSize(1000, 700);
		getContentPane().setLayout(new BorderLayout());
		
		JPanel headPanel = new JPanel();
		headPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
		getContentPane().add(headPanel, BorderLayout.NORTH);
		
		JLabel lblFillInAll = new JLabel("Select all instances that should be cleared from all properties.");
		headPanel.add(lblFillInAll);
		
		JPanel templatePanelMarginBorder = new JPanel();
		templatePanelMarginBorder.setBorder(new EmptyBorder(2, 2, 2, 2));
		getContentPane().add(templatePanelMarginBorder, BorderLayout.CENTER);
		templatePanelMarginBorder.setLayout(new BorderLayout(0, 0));
			
		
		//Adding object definition table
		objectDefPanel = new JPanel();
		objectDefPanel.setBorder(new LineBorder(Color.GRAY));
		//templatePanelMarginBorder.add(objectDefPanel, BorderLayout.CENTER);
		objectDefPanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane centerScrollSouthPane = new JScrollPane();
		objectDefPanel.add(centerScrollSouthPane);
		
		String[] temp2 = {"OBJECT DEFINITION", "SELECT"};
		headerObjectDef = temp2;

		DefaultTableModel tableModel2 = new DefaultTableModel(new Object[0][headerObjectDef.length], headerObjectDef);
		objectDefTable = new JTable(tableModel2);
		objectDefTable.getSelectionModel().addListSelectionListener(new ApplicationActions.TableRowSelectionHandler(objectDefTable));
		objectDefTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				for(int row : objectDefTable.getSelectedRows()) {
					Object ob = objectDefTable.getValueAt(row, 0);
					if(ob instanceof ItemContainer) {
						
						ItemContainer<?> iContainer = ((ItemContainer<?>)objectDefTable.getValueAt(row, 0));
						if(iContainer.getItem() instanceof IfcObject.Ifc4) {
							createPropertieOptions((IfcObject.Ifc4)iContainer.getItem());
						}
						if(iContainer.getItem() instanceof IfcObject.Ifc2x3) {
							createPropertieOptions((IfcObject.Ifc2x3)iContainer.getItem());
						}
						
					}	
				}
		 	}
			
		});
		
		objectDefTable.setDefaultRenderer(Object.class, new ApplicationUtilities.TooltipCellRenderer());
		
		objectDefTable.getColumn("SELECT").setCellRenderer(new ApplicationUtilities.CheckboxRenderer());
		objectDefTable.getColumn("SELECT").setCellEditor(new ApplicationUtilities.CheckboxEditor());
		
		centerScrollSouthPane.setViewportView(objectDefTable);

		//create property panel
		propertiePanel = new JPanel();
		propertiePanel.setBorder(new LineBorder(Color.GRAY));
		//templatePanelMarginBorder.add(templatePanel, BorderLayout.NORTH);
		propertiePanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane centerScrollPane = new JScrollPane();
		propertiePanel.add(centerScrollPane);
		
		String[] temp = {"PROPERTIESET NAME", "DESCRIPTION", "RELATIONSHIP", "GLOBALID", "SELECT"};
		headerPropertie = temp;
		
		DefaultTableModel tableModel = new DefaultTableModel(new Object[0][headerPropertie.length], headerPropertie);
		propertieTable = new JTable(tableModel);
		
		propertieTable.setDefaultRenderer(Object.class, new ApplicationUtilities.TooltipCellRenderer());
		
		propertieTable.getColumn("SELECT").setCellRenderer(new ApplicationUtilities.CheckboxRenderer());
		propertieTable.getColumn("SELECT").setCellEditor(new ApplicationUtilities.CheckboxEditor());
		
		centerScrollPane.setViewportView(propertieTable);
		
		//Add to frame
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, objectDefPanel, propertiePanel);
		
		JPanel bottomFooterPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) bottomFooterPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		propertiePanel.add(bottomFooterPanel, BorderLayout.SOUTH);
		
		JButton btnRemoveProp = new JButton("Remove selected Property-Sets");
		bottomFooterPanel.add(btnRemoveProp);
		btnRemoveProp.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				TableColumn nameCol = objectDefTable.getColumn("OBJECT DEFINITION");
				int columnNameIndex = nameCol.getModelIndex();
				
				int selectedRowIndex =  objectDefTable.getSelectedRow();
				
				IfcObject ifcItem = null;
				
				Object obj = objectDefTable.getValueAt(selectedRowIndex, columnNameIndex);
				if(obj instanceof ItemContainer) {
					ItemContainer<?> iContainer = (ItemContainer<?>)obj;
					
					Object item = iContainer.getItem();
					if(item instanceof IfcObject) {
						ifcItem = (IfcObject)item;
					}
				}
				
				ArrayList<IfcRelDefinesByProperties> propMap = createPropertyValueMap();
				
				for(IfcRelDefinesByProperties prop : propMap) {
					new ModelTransfer().clearPropertieSet(ifcItem, prop);
				}
				
				//reload content view
				//ApplicationFrame.getContentView().init(ApplicationUtilities.model);
				
				//reload after deletion
				CustomIfcLoaderManager.getInstance().notifyAllReloadables();
				
				dispose();
			}
		});

		//Footer with buttons
		JPanel topFooterPanel = new JPanel();
		objectDefPanel.add(topFooterPanel, BorderLayout.SOUTH);
		FlowLayout fl_topFooterPanel = (FlowLayout) topFooterPanel.getLayout();
		fl_topFooterPanel.setAlignment(FlowLayout.RIGHT);
		
		JButton btnRemoveObjDef = new JButton("Remove all from selected");
		topFooterPanel.add(btnRemoveObjDef);
		btnRemoveObjDef.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				HashMap<String, IfcObjectDefinition> objDefMap = createObjectDefValueMap(); 	
				new ModelTransfer().clearAllPropertieSet(objDefMap);
				
				//reload content view
				//ApplicationFrame.getContentView().init(ApplicationUtilities.model);
				
				//reload after deletion
				CustomIfcLoaderManager.getInstance().notifyAllReloadables();
				
				dispose();
			}
		});
		
		//Setting divider location
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	splitPane.setDividerLocation(splitPane.getSize().height /2);
            }
        });
		templatePanelMarginBorder.add(splitPane, BorderLayout.CENTER);
		this.setLocationRelativeTo(null);
		
		this.setVisible(true);
	}

	/**
	 * Gibt eine HashMap zurueck, welche Instancen von Typ IfcObjektDefinitionen(Value) nach Name(Key) beinhaltet.
	 * 
	 * @return {@link HashMap}
	 */
	private HashMap<String, IfcObjectDefinition> createObjectDefValueMap() {
		HashMap<String, IfcObjectDefinition> valueMap = new HashMap<>();

		TableColumn nameCol = objectDefTable.getColumn("OBJECT DEFINITION");
		int columnNameIndex = nameCol.getModelIndex();
		
		TableColumn valCol = objectDefTable.getColumn("SELECT");
		int columnValueIndex = valCol.getModelIndex();
		
		for(int i = 0; i < objectDefTable.getRowCount(); i++) {
			
			String sObj = (String)objectDefTable.getValueAt(i, columnValueIndex);
			
			if(sObj.toLowerCase().equals("true")) {
				Object obj = objectDefTable.getValueAt(i, columnNameIndex);
				if(obj instanceof ItemContainer) {
					ItemContainer<?> iContainer = (ItemContainer<?>)obj;
					
					Object item = iContainer.getItem();
					if(item instanceof IfcObjectDefinition.Ifc4) {
						IfcObjectDefinition.Ifc4 ifcItem = (IfcObjectDefinition.Ifc4)item;
						valueMap.put(ifcItem.toString(), ifcItem);
					}
					if(item instanceof IfcObjectDefinition.Ifc2x3) {
						IfcObjectDefinition.Ifc2x3 ifcItem = (IfcObjectDefinition.Ifc2x3)item;
						valueMap.put(ifcItem.toString(), ifcItem);
					}
				}
			}
		}
		
		return valueMap;
	}
	
	/**
	 * Return a list of realtion objects. A relation is of the type IfcRelDefinesByProperties.
	 * 
	 * @return {@link ArrayList}
	 */
	private ArrayList<IfcRelDefinesByProperties> createPropertyValueMap() {
		
		ArrayList<IfcRelDefinesByProperties> valueMap = new ArrayList<>();
		
		TableColumn relCol = propertieTable.getColumn("RELATIONSHIP");
		int relIndex = relCol.getModelIndex();
		
		TableColumn valCol = propertieTable.getColumn("SELECT");
		int columnValueIndex = valCol.getModelIndex();
		
		for(int i = 0; i < propertieTable.getRowCount(); i++) {
			
			String sObj = (String)propertieTable.getValueAt(i, columnValueIndex);
			
			if(sObj.toLowerCase().equals("true")) {
				
				Object obj = propertieTable.getValueAt(i, relIndex);
				
				if(obj instanceof ItemContainer) {
					ItemContainer<?> iContainer = (ItemContainer<?>)obj;
					
					Object item = iContainer.getItem();
					System.out.println(item.toString()); // REMOVE ME
					
					if(item instanceof IfcRelDefinesByProperties) {						
						valueMap.add((IfcRelDefinesByProperties)item);					
					}
				}
			}
		}
		
		return valueMap;
	}
	
	/**
	 * Fills the table of object definitions based on a passed IfcModel.
	 * 
	 * @param model
	 */
	public void createObjDefOptions(IfcModel model) {
		
		if(model != null) {
			//get all IfcObjectDefinition instances from model
			Collection<IfcObjectDefinition> objDefs = model.getCollection(IfcObjectDefinition.class);
			
			//Get current table model
			DefaultTableModel tm = (DefaultTableModel)objectDefTable.getModel();
					
			//Delete content of current table model
			tm.setNumRows(0);
			

			int index = 0;
			for(IfcObjectDefinition obj : objDefs) {
				
				ItemContainer<IfcObjectDefinition> container = new ItemContainer<IfcObjectDefinition>(
						obj, 
						obj.toString()
				);
			
				Object[] rowData = {
						container,
						"false"
				};	

				tm.insertRow(index, rowData);
				index++;
			}
		}
				
		objectDefPanel.updateUI();
	}

	/**
	 * Fills the table of properties using a passed IfcObject.
	 * 
	 * @param model
	 */
	private void createPropertieOptions(IfcObject objDef) {
		
		if(objDef != null) {
			
			//Get current table model
			DefaultTableModel tm = (DefaultTableModel)propertieTable.getModel();
					
			//Delete content of current table model
			tm.setNumRows(0);
			
			if(objDef instanceof IfcObject.Ifc4) {
				if(((IfcObject.Ifc4)objDef).getIsDefinedBy_Inverse() != null) {
					int index = 0;
					for(Object rel : ((IfcObject.Ifc4)objDef).getIsDefinedBy_Inverse()) {
						
						if(rel instanceof IfcRelDefinesByProperties.Ifc4) {
							IfcRelDefinesByProperties.Ifc4 ifcRel = (IfcRelDefinesByProperties.Ifc4)rel;
							
							IfcPropertySetDefinitionSelect.Ifc4 propSet = ifcRel.getRelatingPropertyDefinition();

							String name = "";
							String description = "";
							String guid = "";
							
							if(propSet instanceof IfcPropertySet.Ifc4) {
								IfcPropertySet.Ifc4 psObj = (IfcPropertySet.Ifc4)propSet;

								if(psObj.getName() != null) {
									name = psObj.getName().toString();
								}
								
								if(psObj.getDescription() != null) {
									description = psObj.getDescription().toString();
								}
								
								if(psObj.getGlobalId() != null) {
									guid = psObj.getGlobalId().toString();
								}
							}
							
							if(propSet instanceof IfcElementQuantity.Ifc4) {
								IfcElementQuantity.Ifc4 psObj = (IfcElementQuantity.Ifc4)propSet;
								
								if(psObj.getName() != null) {
									name = psObj.getName().toString();
								}
								
								if(psObj.getDescription() != null) {
									description = psObj.getDescription().toString();
								}
								
								if(psObj.getGlobalId() != null) {
									guid = psObj.getGlobalId().toString();
								}
							}

							
							Object[] rowData = {
									name,
									description,
									new ItemContainer<IfcRelDefinesByProperties>(ifcRel, ifcRel.toString()),
									guid,
									""
							};
							

							tm.insertRow(index, rowData);
							index++;
						}
					}
				}
			}
			if(objDef instanceof IfcObject.Ifc2x3) {
				if(((IfcObject.Ifc2x3)objDef).getIsDefinedBy_Inverse() != null) {
					int index = 0;
					for(Object rel : ((IfcObject.Ifc2x3)objDef).getIsDefinedBy_Inverse()) {
						
						if(rel instanceof IfcRelDefinesByProperties.Ifc2x3) {
							IfcRelDefinesByProperties.Ifc2x3 ifcRel = (IfcRelDefinesByProperties.Ifc2x3)rel;
							
							IfcPropertySetDefinition.Ifc2x3 propSet = ifcRel.getRelatingPropertyDefinition();

							String name = "";
							String description = "";
							String guid = "";
							
							if(propSet instanceof IfcPropertySet.Ifc2x3) {
								IfcPropertySet.Ifc2x3 psObj = (IfcPropertySet.Ifc2x3)propSet;

								if(psObj.getName() != null) {
									name = psObj.getName().toString();
								}
								
								if(psObj.getDescription() != null) {
									description = psObj.getDescription().toString();
								}
								
								if(psObj.getGlobalId() != null) {
									guid = psObj.getGlobalId().toString();
								}
							}
							
							if(propSet instanceof IfcElementQuantity.Ifc2x3) {
								IfcElementQuantity.Ifc2x3 psObj = (IfcElementQuantity.Ifc2x3)propSet;
								
								if(psObj.getName() != null) {
									name = psObj.getName().toString();
								}
								
								if(psObj.getDescription() != null) {
									description = psObj.getDescription().toString();
								}
								
								if(psObj.getGlobalId() != null) {
									guid = psObj.getGlobalId().toString();
								}
							}

							
							Object[] rowData = {
									name,
									description,
									new ItemContainer<IfcRelDefinesByProperties>(ifcRel, ifcRel.toString()),
									guid,
									""
							};
							

							tm.insertRow(index, rowData);
							index++;
						}
					}
				}
			}
			
		}
				
		propertieTable.updateUI();
	}
}

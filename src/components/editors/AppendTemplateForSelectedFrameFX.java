package components.editors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.apstex.ifctoolbox.ifc.IfcObjectDefinition;
import com.apstex.ifctoolbox.ifc.IfcPropertyEnumeration;
import com.apstex.ifctoolbox.ifc.IfcPropertySetTemplate;
import com.apstex.ifctoolbox.ifc.IfcPropertySetTemplateTypeEnum;
import com.apstex.ifctoolbox.ifc.IfcSimplePropertyTemplate;
import com.apstex.step.core.ClassInterface;

import components.ifc.CustomIfcLoaderManager;
import components.io.ModelTransfer;
import components.templating.ItemContainer;
import components.templating.PropertyItem;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import utils.ApplicationUtilities;
import utils.CustomMixedTableCell;

/**
 * Frame for appending the instance of a Template to the Model.
 * 
 * @author Marcel Stepien
 *
 */
public class AppendTemplateForSelectedFrameFX extends JFrame {

	private TreeTableView<PropertyItem> templateTable = null;
	private HashMap<String, IfcObjectDefinition> objDefMap = null;

	private JFXPanel templatePanel = null;

	private IfcPropertySetTemplate.Ifc4 propTemp = null;
	
	private JCheckBoxMenuItem sharedCheckBox = null;
	
	/**
	 * Constructor
	 */
	public AppendTemplateForSelectedFrameFX(ItemContainer usrObj) {
		super();

		String title = "Appending Property: ";
		Object item = ((ItemContainer) usrObj).getItem();
		if (item instanceof IfcPropertySetTemplate.Ifc4) {
			propTemp = (IfcPropertySetTemplate.Ifc4) item;
			title = title + propTemp.getName().toString();
		}
		this.setTitle(title);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.setSize(1250, 700);
		getContentPane().setLayout(new BorderLayout());

		
		sharedCheckBox = new JCheckBoxMenuItem("Append as shared PropertySet");
		sharedCheckBox.setSelected(false);
		
		JMenu menu = new JMenu("Settings");
		menu.add(sharedCheckBox);
		
		JMenuBar menubar = new JMenuBar();
		menubar.add(menu);
		
		this.setJMenuBar(menubar);
		
		
		JPanel headPanel = new JPanel();
		headPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
		getContentPane().add(headPanel, BorderLayout.NORTH);

		JLabel lblFillInAll = new JLabel("Fill in all information of the selected Template.");
		headPanel.add(lblFillInAll);

		JPanel templatePanelMarginBorder = new JPanel();
		templatePanelMarginBorder.setBorder(new EmptyBorder(2, 2, 2, 2));
		getContentPane().add(templatePanelMarginBorder, BorderLayout.CENTER);
		templatePanelMarginBorder.setLayout(new BorderLayout(0, 0));

		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// Adding template propertie table
				templatePanel = new JFXPanel();
				templatePanel.setBorder(new LineBorder(Color.GRAY));
				// templatePanelMarginBorder.add(templatePanel, BorderLayout.NORTH);
				templatePanel.setLayout(new BorderLayout(0, 0));

				templateTable = new TreeTableView<>();
				templateTable.setShowRoot(false);
				templateTable.setEditable(true);
				
				
				TreeTableColumn<PropertyItem, String> nameColumn = new TreeTableColumn<PropertyItem, String>("NAME");
				nameColumn.setCellValueFactory(
						(TreeTableColumn.CellDataFeatures<PropertyItem, String> param) -> new ReadOnlyStringWrapper(
								param.getValue().getValue().getName()));
				nameColumn.setPrefWidth(225);
				nameColumn.setSortable(false);
				templateTable.getColumns().add(nameColumn);

				TreeTableColumn<PropertyItem, String> descColumn = new TreeTableColumn<PropertyItem, String>(
						"DESCRIPTION");
				descColumn.setCellValueFactory(
						(TreeTableColumn.CellDataFeatures<PropertyItem, String> param) -> new ReadOnlyStringWrapper(
								param.getValue().getValue().getDiscription()));
				descColumn.setPrefWidth(450);
				descColumn.setSortable(false);
				templateTable.getColumns().add(descColumn);

				TreeTableColumn<PropertyItem, String> propTypeColumn = new TreeTableColumn<PropertyItem, String>("PROP_TYPE");
				propTypeColumn.setCellValueFactory(
						(TreeTableColumn.CellDataFeatures<PropertyItem, String> param) -> new ReadOnlyStringWrapper(
								param.getValue().getValue().getPropertyType()));
				propTypeColumn.setPrefWidth(150);
				propTypeColumn.setSortable(false);
				templateTable.getColumns().add(propTypeColumn);
				
				String[] itemsProps = {
						//IfcSimpleValues
						"IfcInteger", 	
						"IfcReal", 	
						"IfcBoolean", 	
						"IfcIdentifier", 	
						"IfcText", 	
						"IfcLabel", 	
						"IfcLogical", 	
						"IfcDateTime", 	
						"IfcDate", 	
						"IfcTime", 	
						"IfcDuration", 	
						"IfcTimeStamp", 	
						"IfcPositiveInteger", 	
						"IfcBinary",
						//IfcMeasureValues
					    "IfcAmountOfSubstanceMeasure",
					    "IfcAreaMeasure",
					    "IfcContextDependentMeasure",
					    "IfcCountMeasure",
					    "IfcDescriptiveMeasure",
					    "IfcElectricCurrentMeasure",
					    "IfcLengthMeasure",
					    "IfcLuminousIntensityMeasure",
					    "IfcMassMeasure",
					    "IfcNormalisedRatioMeasure",
					    "IfcNumericMeasure",
					    "IfcParameterValue",
					    "IfcPlaneAngleMeasure",
					    "IfcPositiveLengthMeasure",
					    "IfcPositivePlaneAngleMeasure",
					    "IfcPositiveRatioMeasure",
					    "IfcRatioMeasure",
					    "IfcSolidAngleMeasure",
					    "IfcThermodynamicTemperatureMeasure",
					    "IfcTimeMeasure",
					    "IfcVolumeMeasure",
					    //IfcDerivedMeasureValues
					 	"IfcVolumetricFlowRateMeasure",
					 	//"IfcTimeStamp",
					 	"IfcThermalTransmittanceMeasure",
					 	"IfcThermalResistanceMeasure",
					 	"IfcThermalAdmittanceMeasure",
					 	"IfcPressureMeasure",
					 	"IfcPowerMeasure",
					 	"IfcMassFlowRateMeasure",
					 	"IfcMassDensityMeasure",
					 	"IfcLinearVelocityMeasure",
					 	"IfcKinematicViscosityMeasure",
					 	"IfcIntegerCountRateMeasure",
					 	"IfcHeatFluxDensityMeasure",
					 	"IfcFrequencyMeasure",
					 	"IfcEnergyMeasure",
					 	"IfcElectricVoltageMeasure",
					 	"IfcDynamicViscosityMeasure",
					 	"IfcCompoundPlaneAngleMeasure",
					 	"IfcAngularVelocityMeasure",
					 	"IfcThermalConductivityMeasure",
					 	"IfcMolecularWeightMeasure",
					 	"IfcVaporPermeabilityMeasure",
					 	"IfcMoistureDiffusivityMeasure",
					 	"IfcIsothermalMoistureCapacityMeasure",
					 	"IfcSpecificHeatCapacityMeasure",
					 	"IfcMonetaryMeasure",
					 	"IfcMagneticFluxDensityMeasure",
					 	"IfcMagneticFluxMeasure",
					 	"IfcLuminousFluxMeasure",
					 	"IfcForceMeasure",
					 	"IfcInductanceMeasure",
					 	"IfcIlluminanceMeasure",
					 	"IfcElectricResistanceMeasure",
					 	"IfcElectricConductanceMeasure",
					 	"IfcElectricChargeMeasure",
					 	"IfcDoseEquivalentMeasure",
					 	"IfcElectricCapacitanceMeasure",
					 	"IfcAbsorbedDoseMeasure",
					 	"IfcRadioActivityMeasure",
					 	"IfcRotationalFrequencyMeasure",
					 	"IfcTorqueMeasure",
					 	"IfcAccelerationMeasure",
					 	"IfcLinearForceMeasure",
					 	"IfcLinearStiffnessMeasure",
					 	"IfcModulusOfSubgradeReactionMeasure",
					 	"IfcModulusOfElasticityMeasure",
					 	"IfcMomentOfInertiaMeasure",
					 	"IfcPlanarForceMeasure",
					 	"IfcRotationalStiffnessMeasure",
					 	"IfcShearModulusMeasure",
					 	"IfcLinearMomentMeasure"
				};
				Arrays.sort(itemsProps);
				
				String[] itemsQuans = {
						"IfcLengthMeasure", 	
						"IfcAreaMeasure", 	
						"IfcVolumeMeasure", 	
						"IfcCountMeasure", 	
						"IfcMassMeasure", 	
						"IfcTimeMeasure"
				};
				Arrays.sort(itemsQuans);
				
				TreeTableColumn<PropertyItem,  String> valueTypeColumn = new TreeTableColumn<PropertyItem,  String>("VALUE_TYPE");
				valueTypeColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<PropertyItem,  String>, //
				        ObservableValue< String>>() {
				 
				            @Override
				            public ObservableValue< String> call(TreeTableColumn.CellDataFeatures<PropertyItem,  String> param) {
				                TreeItem<PropertyItem> treeItem = param.getValue();
				                PropertyItem emp = treeItem.getValue();

				                String valueType = emp.getValueType();
				                
				                return new SimpleObjectProperty<String>(valueType);
				            }
				        });
				
				
				valueTypeColumn.setCellFactory(new Callback<TreeTableColumn<PropertyItem,  String>, TreeTableCell<PropertyItem, String>>() {
					
					@Override
					public TreeTableCell<PropertyItem,  String> call(TreeTableColumn<PropertyItem,  String> param) {
						
						if(//ModelTransfer.getQuantityValueTypeOf(param.) != null
						   propTemp.getTemplateType().getValue().equals(IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.QTO_OCCURRENCEDRIVEN) ||
						   propTemp.getTemplateType().getValue().equals(IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.QTO_TYPEDRIVENONLY) ||
						   propTemp.getTemplateType().getValue().equals(IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.QTO_TYPEDRIVENOVERRIDE)) {
							
							ComboBoxTreeTableCell<PropertyItem, String> com = new ComboBoxTreeTableCell<PropertyItem, String>(itemsQuans);
							com.setEditable(false); //FIX ME - currently also not editable if the set containes Pset properties
							return com;
							
						}else {
							
							ComboBoxTreeTableCell<PropertyItem, String> combItem = new ComboBoxTreeTableCell<PropertyItem, String>(itemsProps);
							
							return combItem;
						}
						
					}
				});				
				
				
				valueTypeColumn.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<PropertyItem, String>>() {
					 
		            @Override
		            public void handle(TreeTableColumn.CellEditEvent<PropertyItem, String> event) {
		                TreeItem<PropertyItem> item = event.getRowValue();
		                PropertyItem emp = item.getValue();
		                String newValue = event.getNewValue();
		                emp.setValueType(newValue);
		                
		                System.out.println("Set value type of " + emp.getName() + " to: " + newValue);
		            }
		        });
				
				//=====================================================
				valueTypeColumn.setEditable(false); //Comment out to enable edit of value type during appending
				//=====================================================
				
				valueTypeColumn.setPrefWidth(150);
				valueTypeColumn.setSortable(false);
				templateTable.getColumns().add(valueTypeColumn);
				

				TreeTableColumn<PropertyItem, Object> valueColumn = new TreeTableColumn<PropertyItem, Object>("VALUE");
				//valueColumn.setCellValueFactory(
				//	(TreeTableColumn.CellDataFeatures<PropertyItem, String> param) -> new ReadOnlyStringWrapper(
				//		param.getValue().getValue().getValue()));
				valueColumn.setPrefWidth(125);
				
				
				valueColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<PropertyItem, Object>, ObservableValue<Object>>() {
		 
		            @Override
		            public ObservableValue<Object> call(TreeTableColumn.CellDataFeatures<PropertyItem, Object> param) {
		                TreeItem<PropertyItem> treeItem = param.getValue();
		                PropertyItem emp = treeItem.getValue();
		                
		                if(emp.getPropertyType().equals("P_ENUMERATEDVALUE")) {
		                	if(emp.getAdditionalData() instanceof IfcPropertyEnumeration.Ifc4) {
		                		return new SimpleObjectProperty<Object>(emp.getAdditionalData());
		                	}
		                }
		                
		                if(emp.getPropertyType().equals("P_REFERENCEVALUE")) {
		                	Class<?> c = null;
							try {
								c = Class.forName("com.apstex.ifctoolbox.ifc." + emp.getValueType());
							} catch (ClassNotFoundException e) {
								System.err.println("(P_REFERENCEVALUE error) The referenced class " + emp.getValueType() + " does not exist.");
							}
							return new SimpleObjectProperty<Object>(ApplicationUtilities.model.getCollection(c));
		                }
		                
		                Object value = emp.getValue();
		                return new SimpleObjectProperty<Object>(value);				                	
		                
		            }
		        });
				
				valueColumn.setCellFactory(new Callback<TreeTableColumn<PropertyItem,Object>, TreeTableCell<PropertyItem,Object>>() {
					@Override
					public TreeTableCell<PropertyItem, Object> call(TreeTableColumn<PropertyItem, Object> param) {
						
						return new CustomMixedTableCell<PropertyItem>();
						//return new CustomTextFieldTableCell<PropertyItem>();
					}
				});
				
				valueColumn.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<PropertyItem, Object>>() {
					 
		            @Override
		            public void handle(TreeTableColumn.CellEditEvent<PropertyItem, Object> event) {
		                TreeItem<PropertyItem> item = event.getRowValue();
		                PropertyItem emp = item.getValue();
		                Object newValue = event.getNewValue();		                
		                emp.setValue(newValue);
		                
		                System.out.println("Single column commit. new Value:" + newValue);
		            }
		        });

				valueColumn.setSortable(false);
				templateTable.getColumns().add(valueColumn);
				
				VBox vBox = new VBox();
				vBox.getChildren().setAll(templateTable);
				VBox.setVgrow(templateTable, Priority.ALWAYS);

				Scene scene = new Scene(vBox);

				templatePanel.setScene(scene);

				templatePanelMarginBorder.add(templatePanel, BorderLayout.CENTER);
				templatePanelMarginBorder.updateUI();
			}
		});

		// Footer with buttons
		JPanel footerPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) footerPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(footerPanel, BorderLayout.SOUTH);

		JButton btnNext = new JButton("Append");
		btnNext.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String propSetName = propTemp.getName().toString();

				String propType = "NOTDEFINED";
				if (propTemp.getTemplateType() != null) {
					propType = propTemp.getTemplateType().getValue().name();
				}

				ArrayList<PropertyItem> templateValueMap = createTemplateValueMap();

				String discription = propTemp.getDescription().toString();

				new ModelTransfer().appendToModel(propSetName, propType, discription, templateValueMap, objDefMap, sharedCheckBox.isSelected());

				// reload content view
				//ApplicationFrame.getContentView().init(ApplicationUtilities.model);
				
				//reload Model
				CustomIfcLoaderManager.getInstance().notifyAllReloadables();

				dispose();
			}
		});
		footerPanel.add(btnNext);
		this.setLocationRelativeTo(null);

		this.setVisible(true);
	}

	/**
	 * Retrives a ArrayList containing Propertyitem objects with names, type, discription and values.
	 * 
	 * @return {@link ArrayList}
	 */
	private ArrayList<PropertyItem> createTemplateValueMap() {
		ArrayList<PropertyItem> valueMap = new ArrayList();

		int count = templateTable.getExpandedItemCount();

		for (TreeItem<PropertyItem> row : templateTable.getRoot().getChildren()) {

			String name = row.getValue().getName().toString();
			String discription = row.getValue().getDiscription().toString();
			String type = row.getValue().getPropertyType().toString();
			String valueType = row.getValue().getValueType().toString();
			
			Object value = null;
			if(row.getValue().getValue() != null) {
				value = row.getValue().getValue(); 
			}
			
			PropertyItem pItem = new PropertyItem(name, discription, value, type, valueType);
			
			if(row.getValue().getAdditionalData() != null) {
				pItem.setAdditionalData(row.getValue().getAdditionalData());
			}

			if(row.getValue().getUnit() != null) {
				pItem.setUnit(row.getValue().getUnit());
			}
			
			valueMap.add(pItem);
		}

		return valueMap;
	}

	/**
	 * Read a ItemContainer to create the property value table.
	 * 
	 * @param usrObj
	 */
	public void createTemplateOptions(ItemContainer usrObj) {

		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				TreeItem<PropertyItem> root = new TreeItem<PropertyItem>();

				if (propTemp != null) {
					for (Object sP : propTemp.getHasPropertyTemplates()) {
						if (sP instanceof IfcSimplePropertyTemplate) {
							IfcSimplePropertyTemplate ifcSP = (IfcSimplePropertyTemplate) sP;

							PropertyItem item = new PropertyItem(
									ifcSP.getName().toString(),
									ifcSP.getDescription().toString(), 
									"", 
									ifcSP.getTemplateType().toString(), 
									usrObj.getPSDType(ifcSP.getGlobalId().getDecodedValue())
								);

							item.setAdditionalData(ifcSP.getEnumerators()); //Set Enumerator
							item.setUnit(ifcSP.getPrimaryUnit());
							
							root.getChildren().add(new TreeItem<PropertyItem>(item));
						}
					}
				}

				root.setExpanded(true);
				templateTable.setRoot(root);
				templateTable.refresh();

				templatePanel.updateUI();
			}
		});

	}

	/**
	 * Set the list of object definitions to append to.
	 * ClassInstance will be checked. Only IfcObjectDefinition types will be used for appending.
	 * 
	 * @param objDefs
	 */
	public void createObjDefOptions(ArrayList<ClassInterface> objDefs) {
		objDefMap = new HashMap<>();
		for(ClassInterface c : objDefs) {
			if(c instanceof IfcObjectDefinition.Ifc4) {
				objDefMap.put(c.toString(), (IfcObjectDefinition.Ifc4)c);
			}
			if(c instanceof IfcObjectDefinition.Ifc2x3) {
				objDefMap.put(c.toString(), (IfcObjectDefinition.Ifc2x3)c);
			}
		}
	}

}

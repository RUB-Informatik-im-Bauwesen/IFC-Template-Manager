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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.apstex.ifctoolbox.ifc.IfcMaterial;
import com.apstex.ifctoolbox.ifc.IfcObjectDefinition;
import com.apstex.ifctoolbox.ifc.IfcPropertySetTemplate;
import com.apstex.ifctoolbox.ifc.IfcSimplePropertyTemplate;
import com.apstex.ifctoolbox.ifcmodel.IfcModel;

import components.ifc.CustomIfcLoaderManager;
import components.io.ModelTransfer;
import components.templating.ItemContainer;
import components.templating.MaterialItem;
import components.templating.PropertyItem;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import utils.CustomTextFieldTableCell;

/**
 * Frame for appending the instance of a Template to the Model.
 * 
 * @author Marcel Stepien
 *
 */
public class AppendTemplateFrameFX extends JFrame {

	private TreeTableView<PropertyItem> templateTable = null;
	private TreeTableView<ItemContainer> objectDefTable = null;

	private JFXPanel templatePanel = null;
	private JFXPanel objectDefPanel = null;
	
	private JFXPanel materialSelectPanel = null;
	private TreeTableView materialSelectTable = null;

	private IfcPropertySetTemplate.Ifc4 propTemp = null;
	
	private JCheckBoxMenuItem sharedCheckBox = null;

	/**
	 * Constructor
	 */
	public AppendTemplateFrameFX(ItemContainer usrObj) {
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
				templateTable.getColumns().add(nameColumn);

				TreeTableColumn<PropertyItem, String> descColumn = new TreeTableColumn<PropertyItem, String>(
						"DESCRIPTION");
				descColumn.setCellValueFactory(
						(TreeTableColumn.CellDataFeatures<PropertyItem, String> param) -> new ReadOnlyStringWrapper(
								param.getValue().getValue().getDiscription()));
				descColumn.setPrefWidth(450);
				templateTable.getColumns().add(descColumn);

				TreeTableColumn<PropertyItem, String> propTypeColumn = new TreeTableColumn<PropertyItem, String>("PROP_TYPE");
				propTypeColumn.setCellValueFactory(
						(TreeTableColumn.CellDataFeatures<PropertyItem, String> param) -> new ReadOnlyStringWrapper(
								param.getValue().getValue().getPropertyType()));
				propTypeColumn.setPrefWidth(150);
				templateTable.getColumns().add(propTypeColumn);
				
				String[] items = {
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
				
				TreeTableColumn<PropertyItem, String> valueTypeColumn = new TreeTableColumn<PropertyItem, String>("VALUE_TYPE");
				valueTypeColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<PropertyItem, String>, //
				        ObservableValue<String>>() {
				 
				            @Override
				            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<PropertyItem, String> param) {
				                TreeItem<PropertyItem> treeItem = param.getValue();
				                PropertyItem emp = treeItem.getValue();
				                Object value = emp.getValueType();
				                if(emp.getValueType() != null && emp.getValueType() != "") {
				                	value = emp.getValueType();
				                }
				                
				                return new SimpleObjectProperty<String>(value.toString());
				            }
				        });

				valueTypeColumn.setCellFactory(ComboBoxTreeTableCell.forTreeTableColumn(items));
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
				valueTypeColumn.setPrefWidth(150);
				templateTable.getColumns().add(valueTypeColumn);

				TreeTableColumn<PropertyItem, String> valueColumn = new TreeTableColumn<PropertyItem, String>("VALUE");
				//valueColumn.setCellValueFactory(
				//	(TreeTableColumn.CellDataFeatures<PropertyItem, String> param) -> new ReadOnlyStringWrapper(
				//		param.getValue().getValue().getValue()));
				valueColumn.setPrefWidth(125);
				
				
				valueColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<PropertyItem, String>, //
				        ObservableValue<String>>() {
				 
				            @Override
				            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<PropertyItem, String> param) {
				                TreeItem<PropertyItem> treeItem = param.getValue();
				                PropertyItem emp = treeItem.getValue();
				                Object value = emp.getValue();
				                return new SimpleObjectProperty<String>(value.toString());
				            }
				        });
				valueColumn.setCellFactory(new Callback<TreeTableColumn<PropertyItem,String>, TreeTableCell<PropertyItem,String>>() {
					@Override
					public TreeTableCell<PropertyItem, String> call(TreeTableColumn<PropertyItem, String> param) {
						return new CustomTextFieldTableCell<PropertyItem>();
					}
				});
				valueColumn.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<PropertyItem, String>>() {
					 
		            @Override
		            public void handle(TreeTableColumn.CellEditEvent<PropertyItem, String> event) {
		                TreeItem<PropertyItem> item = event.getRowValue();
		                PropertyItem emp = item.getValue();
		                String newValue = event.getNewValue();
		                emp.setValue(newValue);
		                
		                System.out.println("Single column commit. new Value:" + newValue);
		            }
		        });
				
				templateTable.getColumns().add(valueColumn);

				VBox vBox = new VBox();
				vBox.getChildren().setAll(templateTable);
				VBox.setVgrow(templateTable, Priority.ALWAYS);

				Scene scene = new Scene(vBox);

				templatePanel.setScene(scene);

				// Adding object definition table
				objectDefPanel = new JFXPanel();
				objectDefPanel.setBorder(new LineBorder(Color.GRAY));
				// templatePanelMarginBorder.add(objectDefPanel, BorderLayout.CENTER);
				objectDefPanel.setLayout(new BorderLayout(0, 0));

				objectDefTable = new TreeTableView<>();
				objectDefTable.setShowRoot(false);
				objectDefTable.setEditable(true);

				TreeTableColumn<ItemContainer, String> objDefColumn = new TreeTableColumn<ItemContainer, String>(
						"OBJECT DEFINITION");
				objDefColumn.setCellValueFactory(
						(TreeTableColumn.CellDataFeatures<ItemContainer, String> param) -> new ReadOnlyStringWrapper(
								param.getValue().getValue().getDisplay()));
				objDefColumn.setPrefWidth(525);				
				objectDefTable.getColumns().add(objDefColumn);
				
				
				TreeTableColumn<ItemContainer, Boolean> selectColumn = new TreeTableColumn<ItemContainer, Boolean>(
						"SELECT");
				selectColumn.setPrefWidth(75);
				
				selectColumn.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(selectColumn));
				
				selectColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<ItemContainer, Boolean>, ObservableValue<Boolean>>() {

					@Override
					public ObservableValue<Boolean> call(
							TreeTableColumn.CellDataFeatures<ItemContainer, Boolean> param) {

						TreeItem<ItemContainer> treeItem = param.getValue();
						ItemContainer emp = treeItem.getValue();
						SimpleBooleanProperty booleanProp = new SimpleBooleanProperty(emp.isSelected());
						
						if(treeItem.getChildren() != null) {
							//Listender for group selection
							booleanProp.addListener(new ChangeListener<Boolean>() {

								@Override
								public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
										Boolean newValue) {
							
									for(TreeItem<ItemContainer> child : treeItem.getChildren()) {
										child.getValue().setSelected(newValue);
									}

									objectDefTable.refresh();
									objectDefPanel.updateUI();
								}
							});

							
						}
						
						// Note: singleCol.setOnEditCommit(): Not work for
						// CheckBoxTreeTableCell.
						// When "Single?" column change.
						booleanProp.addListener(new ChangeListener<Boolean>() {

							@Override
							public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
									Boolean newValue) {
								emp.setSelected(newValue);
							}
						});
						
						return booleanProp;
					}
				});
			
				objectDefTable.getColumns().add(selectColumn);

				VBox vBox2 = new VBox();
				vBox2.getChildren().setAll(objectDefTable);
				vBox2.setVgrow(objectDefTable, Priority.ALWAYS);

				Scene scene2 = new Scene(vBox2);

				objectDefPanel.setScene(scene2);

				materialSelectPanel = new JFXPanel();
				materialSelectPanel.setBorder(new LineBorder(Color.GRAY));
				materialSelectPanel.setLayout(new BorderLayout(0, 0));
				materialSelectPanel.setBackground(Color.BLUE);
				
				materialSelectTable = new TreeTableView<>();
				materialSelectTable.setShowRoot(false);
				materialSelectTable.setEditable(true);
				
				
				TreeTableColumn<Object, String> materialDefColumn = new TreeTableColumn<Object, String>(
						"Material");
				materialDefColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Object,String>, ObservableValue<String>>() {
					
					@Override
					public ObservableValue<String> call(CellDataFeatures<Object, String> param) {
						
						if (param.getValue().getValue() instanceof MaterialItem) {
							MaterialItem materialItem = (MaterialItem) param.getValue().getValue();
							return new SimpleStringProperty(materialItem.getMaterial().toString());		
						}
						
						else return new SimpleStringProperty(param.getValue().getValue().toString());
					}
				} );
				materialDefColumn.setPrefWidth(525);
				materialSelectTable.getColumns().add(materialDefColumn);
				
				TreeTableColumn<MaterialItem, Boolean> materialSelectColumn = new TreeTableColumn<MaterialItem, Boolean>(
						"SELECT");
				materialSelectColumn.setPrefWidth(75);
				
				materialSelectColumn.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(materialSelectColumn));
				
				materialSelectColumn.setCellValueFactory(
						new Callback<TreeTableColumn.CellDataFeatures<MaterialItem, Boolean>, ObservableValue<Boolean>>() {

							@Override
							public ObservableValue<Boolean> call(
									TreeTableColumn.CellDataFeatures<MaterialItem, Boolean> param) {
								TreeItem<MaterialItem> treeItem = param.getValue();
								MaterialItem materialItem = treeItem.getValue();
								SimpleBooleanProperty booleanProp = new SimpleBooleanProperty(materialItem.isSelected());

								booleanProp.addListener(new ChangeListener<Boolean>() {

									@Override
									public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
											Boolean newValue) {
										materialItem.setSelected(newValue);
									}
								});
								return booleanProp;
							}
						});
				
				materialSelectTable.getColumns().add(materialSelectColumn);
				
				VBox vBox3 = new VBox();
				vBox3.getChildren().setAll(materialSelectTable);
				vBox3.setVgrow(materialSelectTable, Priority.ALWAYS);
				Scene scene3 = new Scene(vBox3);
				materialSelectPanel.setScene(scene3);
				
				JSplitPane bottomSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, objectDefPanel, materialSelectPanel);
				JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, templatePanel, bottomSplitPane);


				splitPane.setDividerLocation(templatePanelMarginBorder.getSize().height / 2);


				templatePanelMarginBorder.add(splitPane, BorderLayout.CENTER);
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

				ArrayList<PropertyItem> templateValueMap = createTemplateValueMap(usrObj);
				HashMap<String, IfcObjectDefinition> objDefMap = createObjectDefValueMap();

				String discription = propTemp.getDescription().toString();

				new ModelTransfer().appendToModel(propSetName, propType, discription, templateValueMap, objDefMap, sharedCheckBox.isSelected());
				
				
				Collection<IfcMaterial> selectedMaterials = new ArrayList<>();
				for (int i = 0; i < materialSelectTable.getExpandedItemCount(); i++) {

					Object obj = materialSelectTable.getTreeItem(i).getValue();
					
					if (obj instanceof MaterialItem) {
						MaterialItem materialItem = (MaterialItem) obj;
					    if (materialItem.isSelected()) {
					    	selectedMaterials.add(materialItem.getMaterial());
					    }
					}
				}
				new ModelTransfer().appendToMaterials(propSetName, propType, discription, templateValueMap, selectedMaterials);

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
	 * Retrives a ArrayList containing Propertyitem objects with names, type,
	 * discription and values.
	 * 
	 * @return {@link ArrayList}
	 */
	private ArrayList<PropertyItem> createTemplateValueMap(ItemContainer usrObj) {
		ArrayList<PropertyItem> valueMap = new ArrayList();

		int count = templateTable.getExpandedItemCount();

		for (int i = 0; i < count; i++) {

			String name = templateTable.getColumns().get(0).getCellData(i).toString();
			String discription = templateTable.getColumns().get(1).getCellData(i).toString();
			String type = templateTable.getColumns().get(2).getCellData(i).toString();
			String valueType = templateTable.getColumns().get(3).getCellData(i).toString();
			String value = templateTable.getColumns().get(4).getCellData(i).toString();
			
			//usrObj.getPSDType(guid)
			//String psdTypeValue = psdTypeMap.get(key);
			
//			PropertyItem pItem = null;
//			
//			switch(valueType.toUpperCase()) {
//				case "IFCINTEGER": pItem = new PropertyItem<Integer>(name, discription, new Integer(value), type); break;
//				case "IFCREAL": pItem = new PropertyItem<Double>(name, discription, new Double(value), type); break;
//				case "IFCBOOLEAN": pItem = new PropertyItem<Boolean>(name, discription, new Boolean(value), type); break;
//				case "IFCLOGICAL": pItem = new PropertyItem<Boolean>(name, discription, new Boolean(value), type); break;
//				case "IFCIDENTIFIER": pItem = new PropertyItem<String>(name, discription, value, type); break;
//				case "IFCLABEL": pItem = new PropertyItem<String>(name, discription, value, type); break;
//				case "IFCTEXT": pItem = new PropertyItem<String>(name, discription, value, type); break;
//				case "IFCDATETIME": pItem = new PropertyItem<String>(name, discription, value, type); break;
//				case "IFCDATE": pItem = new PropertyItem<String>(name, discription, value, type); break;
//				case "IFCTIME": pItem = new PropertyItem<String>(name, discription, value, type); break;
//				case "IFCDURATION": pItem = new PropertyItem<String>(name, discription, value, type); break; 	
//				case "IFCTIMESTAMP": pItem = new PropertyItem<Integer>(name, discription, new Integer(value), type); break;	
//				case "IFCPOSITIVEINTEGER": pItem = new PropertyItem<Integer>(name, discription, new Integer(value), type); break; 	
//				case "IFCBINARY": pItem = new PropertyItem<String>(name, discription, value, type); break;	
//				default: pItem = new PropertyItem<String>(name, discription, value, type); break;
//			}
//						
//			pItem.setValueType(valueType);
			
			PropertyItem pItem = new PropertyItem<String>(name, discription, value, type, valueType);
			
			valueMap.add(pItem);
		}

		return valueMap;
	}

	/**
	 * Retrives a HashMap containig object definition names and instances.
	 * 
	 * @return {@link HashMap}
	 */
	private HashMap<String, IfcObjectDefinition> createObjectDefValueMap() {
		HashMap<String, IfcObjectDefinition> valueMap = new HashMap<>();

		int count = objectDefTable.getExpandedItemCount();

		for (int k = 0; k < count; k++) {
			for (TreeItem<ItemContainer> treeItem : objectDefTable.getTreeItem(k).getChildren()) {
				ItemContainer iContainer = treeItem.getValue();
				if (iContainer.isSelected()) {
					
					Object item = iContainer.getItem();
					if (item instanceof IfcObjectDefinition.Ifc4) {
						IfcObjectDefinition.Ifc4 ifcItem = (IfcObjectDefinition.Ifc4) item;
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
	 * Read a ItemContainer to create the property value table.
	 * 
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
									usrObj.getPSDType(ifcSP.getGlobalId().getDecodedValue()));

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
	 * Read the model to create the object definition selection table.
	 * 
	 * @param model
	 */
	public void createObjDefOptions(IfcModel model) {
		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (model != null) {
					// get all IfcObjectDefinition instances from model
					Collection<IfcObjectDefinition> objDefs = model.getCollection(IfcObjectDefinition.class);

					HashMap<String, ArrayList<IfcObjectDefinition>> sortedMap = new HashMap<>();
					for (IfcObjectDefinition obj : objDefs) {
						ArrayList<IfcObjectDefinition> list = sortedMap.get(obj.getClassName());
						if(list == null) {
							list = new ArrayList<>();
							sortedMap.put(obj.getClassName(), list);
						}
						list.add(obj);
					}
					

					TreeItem<ItemContainer> root = new TreeItem<ItemContainer>();
					
					int index = 0;

					for (String className : sortedMap.keySet()) {
						ItemContainer<IfcObjectDefinition> container = new ItemContainer<IfcObjectDefinition>(
								null, 
								className
						);
						TreeItem<ItemContainer> parent = new TreeItem<ItemContainer>(container);
						root.getChildren().add(parent);
						
						for (IfcObjectDefinition obj : sortedMap.get(className)) {

							ItemContainer<IfcObjectDefinition> subContainer = new ItemContainer<IfcObjectDefinition>(
									obj, 
									obj.toString()
							);

							parent.getChildren().add(new TreeItem<ItemContainer>(subContainer));
						}
					}
					
					root.setExpanded(true);
					objectDefTable.setRoot(root);
					objectDefTable.refresh();

					objectDefPanel.updateUI();
				}
			}
		});

	}
	
	public void createMaterialOptions(IfcModel model) {
		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (model != null) {
				
					Collection<IfcMaterial> materials = model.getCollection(IfcMaterial.class);

					TreeItem<Object> root = new TreeItem<Object>("Root");

					for (IfcMaterial material : materials) {
						root.getChildren().add(new TreeItem<Object>(new MaterialItem(material)));
					}

					root.setExpanded(true);
					materialSelectTable.setRoot(root);
					materialSelectTable.refresh();

					materialSelectPanel.updateUI();
				}
			}
		});
	}

}

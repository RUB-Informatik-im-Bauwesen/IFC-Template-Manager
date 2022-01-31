package components.managers.enumeration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import com.apstex.ifctoolbox.ifc.IfcLabel;
import com.apstex.ifctoolbox.ifc.IfcPropertyEnumeration;
import com.apstex.ifctoolbox.ifc.IfcValue;
import com.apstex.ifctoolbox.ifcmodel.IfcModel;
import com.apstex.step.core.LIST;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import utils.ApplicationUtilities;
import utils.CustomTextFieldTableCell;

/**
 * Frame for appending the instance of a Template to the Model.
 * 
 * @author Marcel Stepien
 *
 */
public class ManageEnumerationsFrame extends JFrame {
	
	private IfcModel templateModel = null;

	private TreeTableView<Object> enumTable = null;  //IfcPropertyEnumeration
	private TreeTableView<Object> valueTable = null; //ArrayList<IfcValue>

	private ManageEnumerationsFrame self = null;
	
	class ButtonDividerUI extends BasicSplitPaneUI {
	   protected JButton buttonA;
	   protected JButton buttonB;
	   
	   protected int thickness = 45;
	  
	   public ButtonDividerUI(JButton buttonA, JButton buttonB) {
	      this.buttonA = buttonA;
	      this.buttonB = buttonB;
	   }
	  
	   public BasicSplitPaneDivider createDefaultDivider() {
	      BasicSplitPaneDivider divider = new BasicSplitPaneDivider(this) {
	    	  public int getDividerSize() {
	    		  return thickness + 4;
	    	  }
	      };
	    		 
	      buttonA.setPreferredSize(new Dimension(thickness, thickness));
	      buttonB.setPreferredSize(new Dimension(thickness, thickness));
	      
	      divider.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
	      divider.add(buttonA);
	      divider.add(buttonB);
	  
	      return divider;
	   }
	   
	}
	
	/**
	 * Constructor
	 */
	public ManageEnumerationsFrame(IfcModel templateModel, Object[] templateRawData) {
		super();
		self = this;
		this.templateModel = templateModel;

		String title = "Manage Enumerations";
		this.setTitle(title);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.setSize(800, 600);
		getContentPane().setLayout(new BorderLayout());
		
		JPanel propertyPanel = new JPanel();
		propertyPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
		propertyPanel.setLayout(new BorderLayout(0, 0));
		getContentPane().add(propertyPanel, BorderLayout.CENTER);

		JPanel propertyValuePanel = new JPanel();
		propertyValuePanel.setPreferredSize(new Dimension(10, 200));
		propertyValuePanel.setMaximumSize(new Dimension(32767, 200));
		propertyValuePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Enumeration View",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		propertyValuePanel.setLayout(new BorderLayout(0, 0));
		propertyPanel.add(propertyValuePanel, BorderLayout.CENTER);
		

		//LEFT SIDE
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		
		JScrollPane centerScrollPane = new JScrollPane();
		leftPanel.add(centerScrollPane, BorderLayout.CENTER);
		
		//Toolbox Left
		JPanel toolboxPanel = new JPanel();
		toolboxPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		toolboxPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		leftPanel.add(toolboxPanel, BorderLayout.NORTH);
		
		try {
			JButton addButton = ApplicationUtilities.createButton("Add Enumeration", new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					javafx.application.Platform.runLater(new Runnable() {
						@Override
						public void run() {
							
							TextInputDialog dialog = new TextInputDialog("Naming Enumeration");
							dialog.setTitle("Naming Enumeration");
							dialog.setHeaderText("Enter the name of the Enumeration");
							dialog.setContentText("Name:");

							// Traditional way to get the response value.
							Optional<String> result = dialog.showAndWait();
							if (result.isPresent()){
								
							    String name = result.get();
							    
							    IfcPropertyEnumeration.Ifc4 propEnum = new IfcPropertyEnumeration.Ifc4.Instance();
							    propEnum.setName(new IfcLabel.Ifc4(name, true));
							    templateModel.addObject(propEnum);
							    propEnum.setEnumerationValues(new LIST<IfcValue>());
							    
							    enumTable.getRoot().getChildren().add(new TreeItem<Object>(propEnum));
							    enumTable.refresh();
							}

						}

					});

				}
			}, new ImageIcon(
					ImageIO.read(
							ManageEnumerationsFrame.class.getResource("../../../icons/add.png")
					).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
				)
			);
			addButton.setPressedIcon(new ImageIcon(
					ImageIO.read(
							ManageEnumerationsFrame.class.getResource("../../../icons/add_hover.png")
					).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
				)
			);
			addButton.setRolloverIcon(new ImageIcon(
					ImageIO.read(
							ManageEnumerationsFrame.class.getResource("../../../icons/add_hover.png")
					).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
				)
			);
			toolboxPanel.add(addButton);
			
			JButton removeButton = ApplicationUtilities.createButton("Remove Enumeration", new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					javafx.application.Platform.runLater(new Runnable() {
						@Override
						public void run() {
							
							if(enumTable.getSelectionModel().getSelectedItem() != null) {
								TreeItem<Object> selectedItem = enumTable.getSelectionModel().getSelectedItem();
								Object selectedItemValue = selectedItem.getValue();
								
								enumTable.getRoot().getChildren().remove(selectedItem);
								if(selectedItemValue instanceof IfcPropertyEnumeration.Ifc4) {
									templateModel.removeObject(((IfcPropertyEnumeration.Ifc4)selectedItemValue));
							    }
							}
							enumTable.getSelectionModel().clearSelection();
							
							valueTable.setRoot(new TreeItem<Object>());
							valueTable.refresh();
							
						}

					});

				}
			}, new ImageIcon(
					ImageIO.read(
							ManageEnumerationsFrame.class.getResource("../../../icons/del.png")
					).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
				)
			);
			removeButton.setRolloverIcon(new ImageIcon(
					ImageIO.read(
							ManageEnumerationsFrame.class.getResource("../../../icons/del_hover.png")
					).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
				)
			);
			removeButton.setPressedIcon(new ImageIcon(
					ImageIO.read(
							ManageEnumerationsFrame.class.getResource("../../../icons/del_hover.png")
					).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
				)
			);
			toolboxPanel.add(removeButton);
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//RIGHT SIDE
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		
		JScrollPane centerScrollPane2 = new JScrollPane();
		rightPanel.add(centerScrollPane2, BorderLayout.CENTER);
		
		//Toolbox Left
		JPanel rightToolboxPanel = new JPanel();
		rightToolboxPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		rightToolboxPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		rightPanel.add(rightToolboxPanel, BorderLayout.NORTH);
		
		try {
			JButton addButton = ApplicationUtilities.createButton("Add Value to Enum", new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					javafx.application.Platform.runLater(new Runnable() {
						@Override
						public void run() {
							
							if(enumTable.getSelectionModel().getSelectedItem() == null) {
								Alert alert = new Alert(AlertType.INFORMATION);
								alert.setContentText("Select a Enumeration first!");
								alert.showAndWait();
							}else {
								TextInputDialog dialog = new TextInputDialog("Adding Value to Enumeration");
								dialog.setTitle("Adding Value to Enum");
								dialog.setHeaderText("Enter the value to be added into the Enumeration");
								dialog.setContentText("Value:");

								// Traditional way to get the response value.
								Optional<String> result = dialog.showAndWait();
								if (result.isPresent()){
									
								    String value = result.get();
								    IfcLabel.Ifc4 newValue = new IfcLabel.Ifc4(value, true);
								    
								    if(enumTable.getSelectionModel().getSelectedItem().getValue() instanceof IfcPropertyEnumeration.Ifc4) {
								    	((IfcPropertyEnumeration.Ifc4)enumTable.getSelectionModel().getSelectedItem().getValue()).addEnumerationValues(newValue);
								    }
								    
								    valueTable.getRoot().getChildren().add(
								    	new TreeItem<Object>(
								    		newValue
								    	)
								    );
								    valueTable.refresh();
								}
							}
							
						}

					});

				}
			}, new ImageIcon(
					ImageIO.read(
							ManageEnumerationsFrame.class.getResource("../../../icons/add.png")
					).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
				)
			);
			addButton.setPressedIcon(new ImageIcon(
					ImageIO.read(
							ManageEnumerationsFrame.class.getResource("../../../icons/add_hover.png")
					).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
				)
			);
			addButton.setRolloverIcon(new ImageIcon(
					ImageIO.read(
							ManageEnumerationsFrame.class.getResource("../../../icons/add_hover.png")
					).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
				)
			);
			rightToolboxPanel.add(addButton);
			
			JButton removeButton = ApplicationUtilities.createButton("Remove Value from Enum", new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					javafx.application.Platform.runLater(new Runnable() {
						@Override
						public void run() {
							
							if(enumTable.getSelectionModel().getSelectedItem() != null) {
								TreeItem<Object> selectedItem = enumTable.getSelectionModel().getSelectedItem();
								Object selectedItemValue = selectedItem.getValue();
								
								if(selectedItemValue instanceof IfcPropertyEnumeration.Ifc4) {
									IfcPropertyEnumeration.Ifc4 propEnum = ((IfcPropertyEnumeration.Ifc4)selectedItemValue);
									
									if(valueTable.getSelectionModel().getSelectedItem() != null) {
										Object enumValue = valueTable.getSelectionModel().getSelectedItem().getValue();
										if(enumValue instanceof IfcValue) {											
											propEnum.removeEnumerationValues((IfcValue)enumValue);
										}
									}
									valueTable.getRoot().getChildren().remove(valueTable.getSelectionModel().getSelectedItem());
									valueTable.getSelectionModel().clearSelection();

							    }
							}else {
								Alert alert = new Alert(AlertType.ERROR);
								alert.setContentText("Referenced Enumeration is unselected!");
								alert.showAndWait();
							}
							
						}

					});

				}
			}, new ImageIcon(
					ImageIO.read(
							ManageEnumerationsFrame.class.getResource("../../../icons/del.png")
					).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
				)
			);
			removeButton.setRolloverIcon(new ImageIcon(
					ImageIO.read(
							ManageEnumerationsFrame.class.getResource("../../../icons/del_hover.png")
					).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
				)
			);
			removeButton.setPressedIcon(new ImageIcon(
					ImageIO.read(
							ManageEnumerationsFrame.class.getResource("../../../icons/del_hover.png")
					).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
				)
			);
			rightToolboxPanel.add(removeButton);
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
		
	    //divider.setDividerLocation(splitPane, (int)(splitPane.getWidth() * 0.5));
	    SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
            	splitPane.setDividerLocation(0.5);
            }
        });
	    
		propertyValuePanel.add(splitPane, BorderLayout.CENTER);
		

		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// Adding template propertie table
				JFXPanel fxPanel = new JFXPanel();
				fxPanel.setBorder(new LineBorder(Color.GRAY));
				// templatePanelMarginBorder.add(templatePanel, BorderLayout.NORTH);
				fxPanel.setLayout(new BorderLayout(0, 0));

				//enumTable.addMouseListener(new PopUpMouseHandler());

				enumTable = new TreeTableView();
				enumTable.setShowRoot(false);
				enumTable.setRoot(new TreeItem<Object>());
				enumTable.setEditable(true);
				
				enumTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Object>>() {
					@Override
					public void changed(ObservableValue<? extends TreeItem<Object>> observable, TreeItem<Object> oldValue, TreeItem<Object> newValue) {
					
						if(observable.getValue() != null) {
							if(observable.getValue().getValue() instanceof IfcPropertyEnumeration.Ifc4) {
								IfcPropertyEnumeration.Ifc4 propEnum = (IfcPropertyEnumeration.Ifc4)observable.getValue().getValue();
							
								TreeItem<Object> rootItem = new TreeItem<Object>();
								for(IfcValue value : propEnum.getEnumerationValues()) {
									if(value instanceof IfcLabel.Ifc4) {
										rootItem.getChildren().add(new TreeItem<Object>(value));
									}
								}
								valueTable.setRoot(rootItem);
								valueTable.refresh();
							}
						}
						
					}
				});
				
				TreeTableColumn<Object, String> linenumberColumn = new TreeTableColumn<Object, String>("LINENUMBER");
				linenumberColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Object, String>, //
						ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object, String> param) {
						TreeItem<Object> treeItem = param.getValue();
						Object emp = treeItem.getValue();
						
						if(emp instanceof IfcPropertyEnumeration.Ifc4) {
							return new SimpleObjectProperty<String>("(#" + ((IfcPropertyEnumeration.Ifc4)emp).getStepLineNumber() + ")");
						}
					
						return new SimpleObjectProperty<String>("-1");
					}
					
				});
				linenumberColumn.setEditable(false);
				linenumberColumn.setPrefWidth(100);
				enumTable.getColumns().add(linenumberColumn);

				TreeTableColumn<Object, String> nameColumn = new TreeTableColumn<Object, String>("NAME");
				nameColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Object, String>, //
						ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object, String> param) {
						TreeItem<Object> treeItem = param.getValue();
						Object emp = treeItem.getValue();
						
						if(emp instanceof IfcPropertyEnumeration.Ifc4) {
							return new SimpleObjectProperty<String>(
									((IfcPropertyEnumeration.Ifc4)emp).getName().getDecodedValue()
							);
						}
					
						return new SimpleObjectProperty<String>("UNRECOGNIZED");
					}
					
				});
				
				nameColumn.setCellFactory(new Callback<TreeTableColumn<Object,String>, TreeTableCell<Object,String>>() {
					@Override
					public TreeTableCell<Object, String> call(TreeTableColumn<Object, String> param) {
						return new CustomTextFieldTableCell<Object>();
					}
				});
				
				nameColumn.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Object, String>>() {

					@Override
					public void handle(TreeTableColumn.CellEditEvent<Object, String> event) {
						if(event.getTreeTablePosition() != null) {
							TreeItem<Object> item = event.getRowValue();
							if(item != null) {
								Object emp = item.getValue();
								
								String newValue = event.getNewValue();
								if(emp instanceof IfcPropertyEnumeration.Ifc4) {
									((IfcPropertyEnumeration.Ifc4)emp).setName(new IfcLabel.Ifc4(newValue, true));
								}
							}
						}
					}
					
				});
				
				nameColumn.setPrefWidth(200);
				enumTable.getColumns().add(nameColumn);

				VBox vBox = new VBox();
				vBox.getChildren().setAll(enumTable);
				VBox.setVgrow(enumTable, Priority.ALWAYS);
				
				//Setting initial values to table
				for(IfcPropertyEnumeration propEnum : templateModel.getCollection(IfcPropertyEnumeration.class)) {				
					enumTable.getRoot().getChildren().add(new TreeItem<Object>(propEnum));
				}	
				enumTable.refresh();
				
				Scene scene = new Scene(vBox);

				fxPanel.setScene(scene);

				centerScrollPane.setViewportView(fxPanel);
			}
		});
		

		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// Adding template propertie table
				JFXPanel fxPanel = new JFXPanel();
				fxPanel.setBorder(new LineBorder(Color.GRAY));
				// templatePanelMarginBorder.add(templatePanel, BorderLayout.NORTH);
				fxPanel.setLayout(new BorderLayout(0, 0));

				//enumTable.addMouseListener(new PopUpMouseHandler());

				valueTable = new TreeTableView();
				valueTable.setShowRoot(false);
				valueTable.setRoot(new TreeItem<Object>());
				valueTable.setEditable(true);

				TreeTableColumn<Object, String> nameColumn = new TreeTableColumn<Object, String>("VALUES");
				nameColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Object, String>, //
						ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object, String> param) {
						TreeItem<Object> treeItem = param.getValue();
						Object emp = treeItem.getValue();
						
						String name = "UNRECOGNIZED";
						if(emp instanceof IfcLabel.Ifc4) {
							name = ((IfcLabel.Ifc4)emp).getDecodedValue();
						}
					
						return new SimpleObjectProperty<String>(name);
					
					}
					
				});
				
				nameColumn.setCellFactory(new Callback<TreeTableColumn<Object,String>, TreeTableCell<Object,String>>() {
					@Override
					public TreeTableCell<Object, String> call(TreeTableColumn<Object, String> param) {
						return new CustomTextFieldTableCell<Object>();
					}
				});
				
				nameColumn.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Object, String>>() {

					@Override
					public void handle(TreeTableColumn.CellEditEvent<Object, String> event) {
						TreeItem<Object> item = event.getRowValue();
						if(item != null) {
							Object emp = item.getValue();
							
							if(emp instanceof IfcLabel.Ifc4) {
								((IfcLabel.Ifc4)emp).setDecodedValue(event.getNewValue());
							}
						}
					}
					
				});

				nameColumn.setPrefWidth(200);
				valueTable.getColumns().add(nameColumn);

				VBox vBox = new VBox();
				vBox.getChildren().setAll(valueTable);
				VBox.setVgrow(valueTable, Priority.ALWAYS);

				Scene scene = new Scene(vBox);

				fxPanel.setScene(scene);

				centerScrollPane2.setViewportView(fxPanel);
			}
		});

		if(templateRawData != null) {
			JPanel buttonPanel = new JPanel();
			buttonPanel.setBorder(new LineBorder(Color.LIGHT_GRAY));
			buttonPanel.setMaximumSize(new Dimension(32767, 60));
			getContentPane().add(buttonPanel, BorderLayout.SOUTH);

			JButton applyButton = new JButton("Apply Selected Enumeration");
			applyButton.setHorizontalTextPosition(SwingConstants.CENTER);
			applyButton.setFont(new Font("Tahoma", Font.PLAIN, 13));

			applyButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					
					//TODO
					
					javafx.application.Platform.runLater(new Runnable() {
						
						@Override
						public void run() {
							
							if(enumTable.getSelectionModel().getSelectedItem() != null) {								
								templateRawData[5] = enumTable.getSelectionModel().getSelectedItem().getValue(); //index 5 is reserved for selected enumeration
							}else {
								Alert alert = new Alert(AlertType.INFORMATION);
								alert.setContentText("Please select a enumeration from the list first.");
								alert.showAndWait();
							}
							
							dispose();
						}
					});
				}
			});

			buttonPanel.add(applyButton);
		}
		

		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	public IfcPropertyEnumeration.Ifc4 getSelected(){
		
		//TODO
		
		return null;
	}
	
	private void reloadTables() {
		//enumTable.setRoot(enumTable.getRoot());
		enumTable.refresh();
		
		//valueTable.setRoot(valueTable.getRoot());
		valueTable.refresh();
	}
	
}


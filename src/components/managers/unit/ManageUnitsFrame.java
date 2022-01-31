package components.managers.unit;

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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.apstex.ifctoolbox.ifc.IfcConversionBasedUnit;
import com.apstex.ifctoolbox.ifc.IfcConversionBasedUnitWithOffset;
import com.apstex.ifctoolbox.ifc.IfcDerivedUnit;
import com.apstex.ifctoolbox.ifc.IfcMonetaryUnit;
import com.apstex.ifctoolbox.ifc.IfcNamedUnit;
import com.apstex.ifctoolbox.ifc.IfcPropertyEnumeration;
import com.apstex.ifctoolbox.ifc.IfcSIUnit;
import com.apstex.ifctoolbox.ifc.IfcUnit;
import com.apstex.ifctoolbox.ifcmodel.IfcModel;
import com.apstex.step.core.RootInterface;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import utils.ApplicationUtilities;

/**
 * Frame for appending the instance of a Template to the Model.
 * 
 * @author Marcel Stepien
 *
 */
public class ManageUnitsFrame extends JFrame {
	
	private IfcModel templateModel = null;

	private TreeTableView<Object> table = null;  //IfcPropertyEnumeration

	private ManageUnitsFrame self = null;
	
	/**
	 * Constructor
	 */
	public ManageUnitsFrame(IfcModel templateModel, Object[] templateRawData) {
		super();
		self = this;
		this.templateModel = templateModel;

		String title = "Manage Units";
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
		propertyValuePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Unit View",
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
			
			String[] list = {
					IfcSIUnit.class.getSimpleName(),
					IfcConversionBasedUnit.class.getSimpleName(),
					IfcConversionBasedUnitWithOffset.class.getSimpleName(),
					IfcDerivedUnit.class.getSimpleName(),
					IfcMonetaryUnit.class.getSimpleName()
			};
			JComboBox<String> unitCombobox = new JComboBox<String>(list); 
			unitCombobox.setSelectedItem(list[0]);
			toolboxPanel.add(unitCombobox);
	
			JButton addButton = ApplicationUtilities.createButton("Add Unit", new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					javafx.application.Platform.runLater(new Runnable() {
						@Override
						public void run() {
							
							if(unitCombobox.getSelectedItem().equals(IfcSIUnit.class.getSimpleName())) {
								DialogIfcSiUnit dialog = new DialogIfcSiUnit(templateModel);
								
								Optional<Object[]> result = dialog.showAndWait();

								if (result.isPresent()){
									Object[] content = result.get();
								    IfcSIUnit siUnit = (IfcSIUnit)dialog.getUnit(content);
								    TreeItem<Object> item = new TreeItem<Object>(siUnit);
									if(siUnit.getDimensions() != null) {
										item.getChildren().add(new TreeItem<Object>(siUnit.getDimensions()));
									}
									table.getRoot().getChildren().add(item);
								}
								System.out.println("Created IfcSiUnit");
							}

							if(unitCombobox.getSelectedItem().equals(IfcConversionBasedUnit.class.getSimpleName())) {
								DialogIfcConventionalBaseUnit dialog = new DialogIfcConventionalBaseUnit(templateModel, templateModel.getCollection(IfcUnit.class));
								
								Optional<Object[]> result = dialog.showAndWait();

								if (result.isPresent()){
									Object[] content = result.get();
									IfcConversionBasedUnit unit = (IfcConversionBasedUnit)dialog.getUnit(content);
									TreeItem<Object> item = new TreeItem<Object>(unit);
									if(unit.getConversionFactor() != null) {
										item.getChildren().add(new TreeItem<Object>(unit.getConversionFactor()));
									}
									if(unit.getDimensions() != null) {
										item.getChildren().add(new TreeItem<Object>(unit.getDimensions()));
									}
									table.getRoot().getChildren().add(item);
								}
								System.out.println("Created IfcConventionalBaseUnit");
							
							}
							
							if(unitCombobox.getSelectedItem().equals(IfcConversionBasedUnitWithOffset.class.getSimpleName())) {
								DialogIfcConventionalBaseUnitWithOffset dialog = new DialogIfcConventionalBaseUnitWithOffset(templateModel, templateModel.getCollection(IfcUnit.class));
								
								Optional<Object[]> result = dialog.showAndWait();

								if (result.isPresent()){
									Object[] content = result.get();
									IfcConversionBasedUnitWithOffset unit = (IfcConversionBasedUnitWithOffset)dialog.getUnit(content);
									TreeItem<Object> item = new TreeItem<Object>(unit);
									if(unit.getConversionFactor() != null) {
										item.getChildren().add(new TreeItem<Object>(unit.getConversionFactor()));
									}
									if(unit.getDimensions() != null) {
										item.getChildren().add(new TreeItem<Object>(unit.getDimensions()));
									}
									table.getRoot().getChildren().add(item);
								}
								System.out.println("Created IfcConversionBasedUnitWithOffset");
							}

							if(unitCombobox.getSelectedItem().equals(IfcDerivedUnit.class.getSimpleName())) {
								
								DialogIfcDerivedUnit dialog = new DialogIfcDerivedUnit(templateModel, templateModel.getCollection(IfcNamedUnit.class));
								
								Optional<Object[]> result = dialog.showAndWait();

								if (result.isPresent()){
									Object[] content = result.get();
									IfcDerivedUnit unit = (IfcDerivedUnit)dialog.getUnit(content);
								    TreeItem<Object> item = new TreeItem<Object>(unit);
									table.getRoot().getChildren().add(item);
								}
								System.out.println("Created IfcDerivedUnit");
							
							}

							if(unitCombobox.getSelectedItem().equals(IfcMonetaryUnit.class.getSimpleName())) {
								
								DialogIfcMonetaryUnit dialog = new DialogIfcMonetaryUnit(templateModel);
								
								Optional<Object[]> result = dialog.showAndWait();

								if (result.isPresent()){
									Object[] content = result.get();
									IfcMonetaryUnit siUnit = (IfcMonetaryUnit)dialog.getUnit(content);
								    TreeItem<Object> item = new TreeItem<Object>(siUnit);
									table.getRoot().getChildren().add(item);
								}
								System.out.println("Created IfcMonetaryUnit");
							
							}
							
							table.refresh();
						}

					});

				}
			}, new ImageIcon(
					ImageIO.read(
							ManageUnitsFrame.class.getResource("../../../icons/add.png")
					).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
				)
			);
			
			addButton.setPressedIcon(new ImageIcon(
					ImageIO.read(
							ManageUnitsFrame.class.getResource("../../../icons/add_hover.png")
					).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
				)
			);
			addButton.setRolloverIcon(new ImageIcon(
					ImageIO.read(
							ManageUnitsFrame.class.getResource("../../../icons/add_hover.png")
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
							
							if(table.getSelectionModel().getSelectedItem() != null) {
								TreeItem<Object> selectedItem = table.getSelectionModel().getSelectedItem();
								Object selectedItemValue = selectedItem.getValue();
								
								table.getRoot().getChildren().remove(selectedItem);
								
								//Remove handle for deifferent unit types
								if(selectedItemValue instanceof IfcSIUnit) {
									System.out.println("REMOVED: " + selectedItemValue.toString());
									DialogIfcSiUnit.removeUnit(templateModel, (IfcSIUnit)selectedItemValue);
								}
								
								if(selectedItemValue instanceof IfcConversionBasedUnit) {
									System.out.println("REMOVED: " + selectedItemValue.toString());
									DialogIfcConventionalBaseUnit.removeUnit(templateModel, (IfcConversionBasedUnit)selectedItemValue);
								}
								
								if(selectedItemValue instanceof IfcDerivedUnit) {
									System.out.println("REMOVED: " + selectedItemValue.toString());
									DialogIfcDerivedUnit.removeUnit(templateModel, (IfcDerivedUnit)selectedItemValue);
								}
								
								if(selectedItemValue instanceof IfcMonetaryUnit) {
									System.out.println("REMOVED: " + selectedItemValue.toString());
									DialogIfcMonetaryUnit.removeUnit(templateModel, (IfcMonetaryUnit)selectedItemValue);
								}
								
								//if(selectedItemValue instanceof IfcUnit.Ifc4) {
								//	templateModel.removeObject((ClassInterface)selectedItemValue);
								//}
							    
							}
							table.getSelectionModel().clearSelection();
							
						}

					});

				}
			}, new ImageIcon(
					ImageIO.read(
							ManageUnitsFrame.class.getResource("../../../icons/del.png")
					).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
				)
			);
			removeButton.setRolloverIcon(new ImageIcon(
					ImageIO.read(
							ManageUnitsFrame.class.getResource("../../../icons/del_hover.png")
					).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
				)
			);
			removeButton.setPressedIcon(new ImageIcon(
					ImageIO.read(
							ManageUnitsFrame.class.getResource("../../../icons/del_hover.png")
					).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
				)
			);
			toolboxPanel.add(removeButton);
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		propertyValuePanel.add(leftPanel, BorderLayout.CENTER);
		
		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// Adding template propertie table
				JFXPanel fxPanel = new JFXPanel();
				fxPanel.setBorder(new LineBorder(Color.GRAY));
				// templatePanelMarginBorder.add(templatePanel, BorderLayout.NORTH);
				fxPanel.setLayout(new BorderLayout(0, 0));

				//table.addMouseListener(new PopUpMouseHandler());

				table = new TreeTableView();
				table.setShowRoot(false);
				table.setRoot(new TreeItem<Object>());
				table.setEditable(false);
				
				TreeTableColumn<Object, String> nameColumn = new TreeTableColumn<Object, String>("Unit Definition");
				nameColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Object, String>, //
						ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object, String> param) {
						TreeItem<Object> treeItem = param.getValue();
						Object emp = treeItem.getValue();
						
						if(emp instanceof RootInterface) {
							return new SimpleObjectProperty<String>(
									((RootInterface)emp).getStepLine()
							);
						}
						if(emp != null) {
							return new SimpleObjectProperty<String>(emp.toString());
						}
						
						return new SimpleObjectProperty<String>("UNRECOGNIZED");
					}
					
				});
				
				nameColumn.setPrefWidth(450);
				table.getColumns().add(nameColumn);

				VBox vBox = new VBox();
				vBox.getChildren().setAll(table);
				VBox.setVgrow(table, Priority.ALWAYS);
				
				//Setting initial values to table
				for(IfcUnit unit : templateModel.getCollection(IfcUnit.class)) {
					TreeItem<Object> item = new TreeItem<Object>(unit);
					table.getRoot().getChildren().add(item);
					
					if(unit instanceof IfcSIUnit) {
						if(((IfcSIUnit)unit).getDimensions() != null) {
							item.getChildren().add(new TreeItem<Object>(((IfcSIUnit)unit).getDimensions()));
						}
					}
					
					if(unit instanceof IfcConversionBasedUnit) {
						if(((IfcConversionBasedUnit)unit).getConversionFactor() != null) {
							item.getChildren().add(new TreeItem<Object>(((IfcConversionBasedUnit)unit).getConversionFactor()));
						}
						if(((IfcConversionBasedUnit)unit).getDimensions() != null) {
							item.getChildren().add(new TreeItem<Object>(((IfcConversionBasedUnit)unit).getDimensions()));
						}
					}
					
					if(unit instanceof IfcDerivedUnit) {
						System.out.println("TODO");
					}
					
				}
				table.refresh();
				
				Scene scene = new Scene(vBox);

				fxPanel.setScene(scene);

				centerScrollPane.setViewportView(fxPanel);
			}
		});
		

		if(templateRawData != null) {
			JPanel buttonPanel = new JPanel();
			buttonPanel.setBorder(new LineBorder(Color.LIGHT_GRAY));
			buttonPanel.setMaximumSize(new Dimension(32767, 60));
			getContentPane().add(buttonPanel, BorderLayout.SOUTH);

			JButton applyButton = new JButton("Apply Selected Unit");
			applyButton.setHorizontalTextPosition(SwingConstants.CENTER);
			applyButton.setFont(new Font("Tahoma", Font.PLAIN, 13));

			applyButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					javafx.application.Platform.runLater(new Runnable() {
						
						@Override
						public void run() {
							TreeItem sItem = table.getSelectionModel().getSelectedItem();
							if(sItem != null) {
								if(sItem.getValue() instanceof IfcUnit) {
									templateRawData[4] = sItem.getValue(); //index 4 is reserved for selected unit
									dispose();
									return;
								}
							}
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.setContentText("Please select a unit from the list first.");
							alert.showAndWait();	
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
		
		//TODO - Returns the selected Enumeration
		
		return null;
	}
	
	private void reloadTables() {
		//table.setRoot(table.getRoot());
		table.refresh();
	}
	
}


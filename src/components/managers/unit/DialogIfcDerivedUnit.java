package components.managers.unit;

import java.util.Collection;

import com.apstex.ifctoolbox.ifc.IfcDerivedUnit;
import com.apstex.ifctoolbox.ifc.IfcDerivedUnitEnum;
import com.apstex.ifctoolbox.ifc.IfcLabel;
import com.apstex.ifctoolbox.ifc.IfcNamedUnit;
import com.apstex.ifctoolbox.ifc.IfcUnit;
import com.apstex.ifctoolbox.ifcmodel.IfcModel;
import com.apstex.step.core.INTEGER;
import com.apstex.step.core.RootInterface;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import utils.CustomTextFieldTableCell;

/**
 * 
 * @author Marcel Stepien
 *
 */
public class DialogIfcDerivedUnit extends Dialog<Object[]> {
	
	private String stylingCheckbox = "-fx-pref-width: 250;";
	private IfcModel templateModel;
	
	public DialogIfcDerivedUnit(IfcModel model, Collection<IfcNamedUnit> units) {
		this.setTitle("Creating IfcDerivedUnit");
		this.setHeaderText("Fill in the Form");
		this.templateModel = model;
		
		
		ButtonType createButtonType = new ButtonType("Create", ButtonData.OK_DONE);
		this.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

		// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		
		TextField textFieldName = new TextField();
		

		ObservableList<IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal> optionsC = 
			    FXCollections.observableArrayList(
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.ACCELERATIONUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.ANGULARVELOCITYUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.AREADENSITYUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.COMPOUNDPLANEANGLEUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.CURVATUREUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.DYNAMICVISCOSITYUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.HEATFLUXDENSITYUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.HEATINGVALUEUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.INTEGERCOUNTRATEUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.IONCONCENTRATIONUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.ISOTHERMALMOISTURECAPACITYUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.KINEMATICVISCOSITYUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.LINEARFORCEUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.LINEARMOMENTUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.LINEARSTIFFNESSUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.LINEARVELOCITYUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.LUMINOUSINTENSITYDISTRIBUTIONUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.MASSDENSITYUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.MASSFLOWRATEUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.MASSPERLENGTHUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.MODULUSOFELASTICITYUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.MODULUSOFLINEARSUBGRADEREACTIONUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.MODULUSOFSUBGRADEREACTIONUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.MOISTUREDIFFUSIVITYUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.MOLECULARWEIGHTUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.MOMENTOFINERTIAUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.PHUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.PLANARFORCEUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.ROTATIONALFREQUENCYUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.ROTATIONALMASSUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.ROTATIONALSTIFFNESSUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.SECTIONAREAINTEGRALUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.SECTIONMODULUSUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.SHEARMODULUSUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.SOUNDPOWERLEVELUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.SOUNDPOWERUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.SOUNDPRESSURELEVELUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.SOUNDPRESSUREUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.SPECIFICHEATCAPACITYUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.TEMPERATUREGRADIENTUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.TEMPERATURERATEOFCHANGEUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.THERMALADMITTANCEUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.THERMALCONDUCTANCEUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.THERMALEXPANSIONCOEFFICIENTUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.THERMALRESISTANCEUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.THERMALTRANSMITTANCEUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.TORQUEUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.USERDEFINED,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.VAPORPERMEABILITYUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.VOLUMETRICFLOWRATEUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.WARPINGCONSTANTUNIT,
			    		IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.WARPINGMOMENTUNIT
			    );
		ComboBox<IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal> comboboxC = new ComboBox<IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal>(optionsC); 
		comboboxC.setValue(IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal.ACCELERATIONUNIT);
		comboboxC.setStyle(stylingCheckbox);
		
	
		TreeTableView<Object> table = new TreeTableView<Object>();
		table.setShowRoot(false);
		table.setRoot(new TreeItem<Object>());
		table.setEditable(true);
		
		ObservableList<IfcNamedUnit> optionsUnits = 
			    FXCollections.observableArrayList(units);
		ComboBox<IfcNamedUnit> comboboxUnits = new ComboBox<IfcNamedUnit>(optionsUnits); 
		comboboxUnits.setStyle(stylingCheckbox);
		
		
		TreeTableColumn<Object, String> nameColumn = new TreeTableColumn<Object, String>("Unit Definition");
		nameColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Object, String>, //
				ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object, String> param) {
				TreeItem<Object> treeItem = param.getValue();
				Object emp = treeItem.getValue();
				
				if(emp instanceof Object[]) {
					if(((Object[])emp)[0] instanceof RootInterface) {
						return new SimpleObjectProperty<String>(
								((RootInterface)((Object[])emp)[0]).getStepLine()
						);
					}
				}
				if(emp != null) {
					return new SimpleObjectProperty<String>(emp.toString());
				}
				
				return new SimpleObjectProperty<String>("UNRECOGNIZED");
			}
			
		});
		nameColumn.setPrefWidth(450);
		table.getColumns().add(nameColumn);
		
		TreeTableColumn<Object, String> expoColumn = new TreeTableColumn<Object, String>("Exponent");
		expoColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Object, String>, //
				ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object, String> param) {
				TreeItem<Object> treeItem = param.getValue();
				Object emp = treeItem.getValue();
				
				if(emp instanceof Object[]) {
					if(((Object[])emp)[1] instanceof INTEGER) {
						return new SimpleObjectProperty<String>(
								Integer.toString(((INTEGER)((Object[])emp)[1]).getValue())
						);
					}
				}
				if(emp != null) {
					return new SimpleObjectProperty<String>(emp.toString());
				}
				
				return new SimpleObjectProperty<String>("UNRECOGNIZED");
			}
			
		});
		
		expoColumn.setCellFactory(new Callback<TreeTableColumn<Object,String>, TreeTableCell<Object,String>>() {
			@Override
			public TreeTableCell<Object, String> call(TreeTableColumn<Object, String> param) {
				return new CustomTextFieldTableCell<Object>();
			}
		});
		
		expoColumn.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Object, String>>() {

			@Override
			public void handle(TreeTableColumn.CellEditEvent<Object, String> event) {
				if(event.getTreeTablePosition() != null) {
					TreeItem<Object> item = event.getRowValue();
					if(item != null) {
						Object emp = item.getValue();
						
						String newValue = event.getNewValue();
						if(emp  instanceof Object[]) {
							((INTEGER)((Object[])emp)[1]).setValue(Integer.parseInt(newValue));
						}
					}
				}
			}
			
		});
		
		expoColumn.setPrefWidth(250);
		table.getColumns().add(expoColumn);
		
		
		Button buttonAdd = new Button("Add");
		buttonAdd.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent arg0) {
				Object[] pair = {comboboxUnits.getSelectionModel().getSelectedItem(), new INTEGER(1)};
				table.getRoot().getChildren().add(new TreeItem<Object>(pair));
			}
		});
		
		Button buttonRemove = new Button("Remove");
		buttonRemove.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		grid.add(new Label("User Defined Type:"), 0, 0);
		grid.add(textFieldName, 1, 0);
		grid.add(new Label("Unit Type:"), 0, 1);
		grid.add(comboboxC, 1, 1);
		
		Separator separator = new Separator();
		grid.add(separator, 0, 2, 2, 1);

		grid.add(new Label("Unit:"), 0, 4);
		grid.add(comboboxUnits, 1, 4);
		grid.add(buttonAdd, 2, 4);
		grid.add(buttonRemove, 3, 4);
		
		grid.add(table, 0, 5, 4, 3);
		
		this.getDialogPane().setContent(grid);

		// Request focus on the username field by default.
		//Platform.runLater(() -> username.requestFocus());

		// Convert the result to a username-password-pair when the login button is clicked.
		this.setResultConverter(dialogButton -> {
			if (dialogButton == createButtonType) {
				Object[] arr = {textFieldName.getText(), comboboxC.getValue(), null};
				return arr;
			}
			return null;
		});
	}
	
	public IfcUnit getUnit(Object[] content) {
		IfcDerivedUnit.Ifc4 ifcUnit = new IfcDerivedUnit.Ifc4.Instance();
		ifcUnit.setUserDefinedType(new IfcLabel.Ifc4((String)content[0]));
		ifcUnit.setUnitType(new IfcDerivedUnitEnum.Ifc4((IfcDerivedUnitEnum.Ifc4.IfcDerivedUnitEnum_internal)content[1]));
		ifcUnit.setElements(null);
		
		templateModel.addObject(ifcUnit);
		return (IfcUnit)ifcUnit;
	}
	
	public static void removeUnit(IfcModel model, IfcDerivedUnit content) {
		model.removeObject(content);
	}
}

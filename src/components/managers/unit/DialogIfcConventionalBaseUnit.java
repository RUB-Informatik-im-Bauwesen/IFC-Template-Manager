package components.managers.unit;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import com.apstex.ifctoolbox.ifc.IfcConversionBasedUnit;
import com.apstex.ifctoolbox.ifc.IfcDimensionalExponents;
import com.apstex.ifctoolbox.ifc.IfcLabel;
import com.apstex.ifctoolbox.ifc.IfcMeasureWithUnit;
import com.apstex.ifctoolbox.ifc.IfcUnit;
import com.apstex.ifctoolbox.ifc.IfcUnitEnum;
import com.apstex.ifctoolbox.ifc.IfcUnitEnum.Ifc4.IfcUnitEnum_internal;
import com.apstex.ifctoolbox.ifcmodel.IfcModel;

import components.io.ModelTransfer;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import utils.ApplicationUtilities;

/**
 * 
 * @author Marcel Stepien
 *
 */
public class DialogIfcConventionalBaseUnit extends Dialog<Object[]> {
	
	private String stylingCheckbox = "-fx-pref-width: 250;";
	private IfcModel templateModel;
	
	private IfcDimensionalExponents dimExpo = null;
	private Label dimExpoLbl = null;
	
	public DialogIfcConventionalBaseUnit(IfcModel model, Collection<IfcUnit> units) {
		this.setTitle("Creating IfcConventionalBaseUnit");
		this.setHeaderText("Fill in the Form");
		this.templateModel = model;
		
		Object[] defaultDimExpos = {"0","0","0","0","0","0","0"};
		dimExpo = new DialogIfcDimensionalExponents(templateModel).getUnit(defaultDimExpos);
		dimExpoLbl = new Label("(L:0,M:0,Ti:0,E:0,Th:0,A:0,Lu:0)");
		
		ButtonType createButtonType = new ButtonType("Create", ButtonData.OK_DONE);
		this.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

		// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		
		
		String[] ifcTypeList = ApplicationUtilities.getIfcValueTypeList();

		Arrays.sort(ifcTypeList);
		Collection<String> ifcTypeCollection = Arrays.asList(ifcTypeList);
		
		ObservableList<String> optionsValueTypes = 
			    FXCollections.observableArrayList(ifcTypeCollection);
		ComboBox<String> comboboxValueType = new ComboBox<String>(optionsValueTypes); 
		comboboxValueType.setValue(ifcTypeList[0]);
		comboboxValueType.setStyle(stylingCheckbox);

		TextField textFieldName = new TextField();
		TextField textFieldValue = new TextField();
		
		ObservableList<IfcUnit> optionsUnits = 
			    FXCollections.observableArrayList(units);
		ComboBox<IfcUnit> comboboxUnits = new ComboBox<IfcUnit>(optionsUnits); 
		comboboxUnits.setStyle(stylingCheckbox);
		
		ObservableList<IfcUnitEnum.Ifc4.IfcUnitEnum_internal> optionsC = 
			    FXCollections.observableArrayList(
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.ABSORBEDDOSEUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.AMOUNTOFSUBSTANCEUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.AREAUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.DOSEEQUIVALENTUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.ELECTRICCAPACITANCEUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.ELECTRICCHARGEUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.ELECTRICCONDUCTANCEUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.ELECTRICCURRENTUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.ELECTRICRESISTANCEUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.ELECTRICVOLTAGEUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.ENERGYUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.FORCEUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.FREQUENCYUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.ILLUMINANCEUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.INDUCTANCEUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.LENGTHUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.LUMINOUSFLUXUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.LUMINOUSINTENSITYUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.MAGNETICFLUXDENSITYUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.MAGNETICFLUXUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.MASSUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.PLANEANGLEUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.POWERUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.PRESSUREUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.RADIOACTIVITYUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.SOLIDANGLEUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.THERMODYNAMICTEMPERATUREUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.TIMEUNIT,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.USERDEFINED,
			    		IfcUnitEnum.Ifc4.IfcUnitEnum_internal.VOLUMEUNIT
			    );
		ComboBox<IfcUnitEnum.Ifc4.IfcUnitEnum_internal> comboboxC = new ComboBox<IfcUnitEnum.Ifc4.IfcUnitEnum_internal>(optionsC); 
		comboboxC.setValue(IfcUnitEnum.Ifc4.IfcUnitEnum_internal.ABSORBEDDOSEUNIT);
		comboboxC.setStyle(stylingCheckbox);
		
		Button dimExpoButton = new Button("Set Dimensoinal Exponents");
		dimExpoButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	
		    	DialogIfcDimensionalExponents dialog = new DialogIfcDimensionalExponents(templateModel, dimExpo);
		    	
				Optional<Object[]> result = dialog.showAndWait();

				if (result.isPresent()){
					//DialogIfcDimensionalExponents.removeUnit(templateModel, dimExpo);

					Object[] content = result.get();
					dimExpo = (IfcDimensionalExponents)dialog.getUnit(content);
					dimExpoLbl.setText("(L:" + dimExpo.getLengthExponent().value + 
							",M:" + dimExpo.getMassExponent().value + 
							",Ti:" + dimExpo.getTimeExponent().value + 
							",E:" + dimExpo.getElectricCurrentExponent().value +
							",Th:" + dimExpo.getThermodynamicTemperatureExponent().value + 
							",A:" + dimExpo.getAmountOfSubstanceExponent().value + 
							",Lu:" + dimExpo.getLuminousIntensityExponent().value + ")");
				}
				
		    
		    }
		});
		
		grid.add(new Label("UnitName:"), 0, 0);
		grid.add(textFieldName, 1, 0);
		grid.add(new Label("Conversion Type:"), 0, 1);
		grid.add(comboboxValueType, 1, 1);
		grid.add(new Label("Conversion Value:"), 0, 2);
		grid.add(textFieldValue, 1, 2);
		grid.add(new Label("Conversion Unit:"), 0, 3);
		grid.add(comboboxUnits, 1, 3);
		grid.add(new Label("UnitEnum:"), 0, 4);
		grid.add(comboboxC, 1, 4);
		grid.add(new Label("Dim.Expo.:"), 0, 5);
		grid.add(dimExpoButton, 1, 5);
		grid.add(dimExpoLbl, 1, 6);

		// Enable/Disable login button depending on whether a username was entered.
		//Node loginButton = this.getDialogPane().lookupButton(createButtonType);
		//loginButton.setDisable(true);

		// Do some validation (using the Java 8 lambda syntax).
		/*username.textProperty().addListener((observable, oldValue, newValue) -> {
			loginButton.setDisable(newValue.trim().isEmpty());
		});
		*/
		
		this.getDialogPane().setContent(grid);

		// Request focus on the username field by default.
		//Platform.runLater(() -> username.requestFocus());

		// Convert the result to a username-password-pair when the login button is clicked.
		this.setResultConverter(dialogButton -> {
			if (dialogButton == createButtonType) {
				Object[] arr = {textFieldName.getText(), comboboxValueType.getValue(), textFieldValue.getText(), comboboxUnits.getValue(), comboboxC.getValue(), dimExpo};
				return arr;
			}
			return null;
		});
	}
	
	public IfcUnit getUnit(Object[] content) {
		IfcConversionBasedUnit ifcUnit = new IfcConversionBasedUnit.Ifc4.Instance();
		ifcUnit.setName(new IfcLabel.Ifc4((String)content[0]));
		IfcMeasureWithUnit measureUnit = new IfcMeasureWithUnit.Ifc4.Instance(
				new ModelTransfer().convertType((String)content[2], (String)content[1], true), 
				(IfcUnit)content[3]
		);
		ifcUnit.setConversionFactor(measureUnit);
		ifcUnit.setUnitType(new IfcUnitEnum.Ifc4((IfcUnitEnum_internal)content[4]));
		IfcDimensionalExponents de = (IfcDimensionalExponents)content[5];
		ifcUnit.setDimensions(de);
		templateModel.addObject(de);
		
		templateModel.addObject(measureUnit);	
		templateModel.addObject(ifcUnit);
		return (IfcUnit)ifcUnit;
	}
	
	public static void removeUnit(IfcModel model, IfcConversionBasedUnit content) {
		if(content.getConversionFactor() != null) {
			model.removeObject(content.getConversionFactor());
		}
		if(content.getDimensions() != null) {
			DialogIfcDimensionalExponents.removeUnit(model, content.getDimensions());
		}
		model.removeObject(content);
	}
}

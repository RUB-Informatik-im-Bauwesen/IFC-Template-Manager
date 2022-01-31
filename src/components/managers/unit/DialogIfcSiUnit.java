package components.managers.unit;

import java.util.Optional;

import com.apstex.ifctoolbox.ifc.IfcDimensionalExponents;
import com.apstex.ifctoolbox.ifc.IfcSIPrefix;
import com.apstex.ifctoolbox.ifc.IfcSIPrefix.Ifc4.IfcSIPrefix_internal;
import com.apstex.ifctoolbox.ifc.IfcSIUnit;
import com.apstex.ifctoolbox.ifc.IfcSIUnitName;
import com.apstex.ifctoolbox.ifc.IfcSIUnitName.Ifc4.IfcSIUnitName_internal;
import com.apstex.ifctoolbox.ifc.IfcUnit;
import com.apstex.ifctoolbox.ifc.IfcUnitEnum;
import com.apstex.ifctoolbox.ifc.IfcUnitEnum.Ifc4.IfcUnitEnum_internal;
import com.apstex.ifctoolbox.ifcmodel.IfcModel;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * 
 * @author Marcel Stepien
 *
 */
public class DialogIfcSiUnit extends Dialog<Object[]> {
	
	private String stylingCheckbox = "-fx-pref-width: 250;";
	private IfcModel templateModel;

	private IfcDimensionalExponents dimExpo = null;
	private Label dimExpoLbl = null;
	private CheckBox prefixCheckBox = null;
	
	public DialogIfcSiUnit(IfcModel model) {
		this.setTitle("Creating IfcSiUnit");
		this.setHeaderText("Fill in the Form");
		this.templateModel = model;
		
		//Object[] defaultDimExpos = {"0","0","0","0","0","0","0"};
		//dimExpo = new DialogIfcDimensionalExponents(templateModel).getUnit(defaultDimExpos);
		//dimExpoLbl = new Label("(L:0,M:0,Ti:0,E:0,Th:0,A:0,Lu:0)");
		
		
		ButtonType createButtonType = new ButtonType("Create", ButtonData.OK_DONE);
		this.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

		// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		
		ObservableList<IfcSIUnitName.Ifc4.IfcSIUnitName_internal> optionsA = 
			    FXCollections.observableArrayList(
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.AMPERE,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.BECQUEREL,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.CANDELA,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.COULOMB,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.CUBIC_METRE,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.DEGREE_CELSIUS,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.FARAD,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.GRAM,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.GRAY,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.HENRY,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.HERTZ,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.JOULE,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.KELVIN,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.LUMEN,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.LUX,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.METRE,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.MOLE,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.NEWTON,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.OHM,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.PASCAL,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.RADIAN,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.SECOND,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.SIEMENS,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.SIEVERT,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.SQUARE_METRE,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.STERADIAN,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.TESLA,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.VOLT,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.WATT,
			    		IfcSIUnitName.Ifc4.IfcSIUnitName_internal.WEBER
			    );
		ComboBox<IfcSIUnitName.Ifc4.IfcSIUnitName_internal> comboboxA = new ComboBox<IfcSIUnitName.Ifc4.IfcSIUnitName_internal>(optionsA); 
		comboboxA.setValue(IfcSIUnitName.Ifc4.IfcSIUnitName_internal.AMPERE);
		comboboxA.setStyle(stylingCheckbox);
		
		ObservableList<IfcSIPrefix.Ifc4.IfcSIPrefix_internal> optionsB = 
			    FXCollections.observableArrayList(
			    		IfcSIPrefix.Ifc4.IfcSIPrefix_internal.ATTO,
			    		IfcSIPrefix.Ifc4.IfcSIPrefix_internal.CENTI,
			    		IfcSIPrefix.Ifc4.IfcSIPrefix_internal.DECA,
			    		IfcSIPrefix.Ifc4.IfcSIPrefix_internal.DECI,
			    		IfcSIPrefix.Ifc4.IfcSIPrefix_internal.EXA,
			    		IfcSIPrefix.Ifc4.IfcSIPrefix_internal.FEMTO,
			    		IfcSIPrefix.Ifc4.IfcSIPrefix_internal.GIGA,
			    		IfcSIPrefix.Ifc4.IfcSIPrefix_internal.HECTO,
			    		IfcSIPrefix.Ifc4.IfcSIPrefix_internal.KILO,
			    		IfcSIPrefix.Ifc4.IfcSIPrefix_internal.MEGA,
			    		IfcSIPrefix.Ifc4.IfcSIPrefix_internal.MICRO,
			    		IfcSIPrefix.Ifc4.IfcSIPrefix_internal.MILLI,
			    		IfcSIPrefix.Ifc4.IfcSIPrefix_internal.NANO,
			    		IfcSIPrefix.Ifc4.IfcSIPrefix_internal.PETA,
			    		IfcSIPrefix.Ifc4.IfcSIPrefix_internal.PICO,
			    		IfcSIPrefix.Ifc4.IfcSIPrefix_internal.TERA
			    );
		ComboBox<IfcSIPrefix.Ifc4.IfcSIPrefix_internal> comboboxB = new ComboBox<IfcSIPrefix.Ifc4.IfcSIPrefix_internal>(optionsB); 
		comboboxB.setValue(IfcSIPrefix.Ifc4.IfcSIPrefix_internal.ATTO);
		comboboxB.setStyle(stylingCheckbox);
		
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
		
		prefixCheckBox = new CheckBox();
		prefixCheckBox.setSelected(true);
		prefixCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
		   
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean newValue) {
				//prefixCheckBox.setSelected(!newValue);
				comboboxB.setDisable(!newValue);
				
			}
		});
		
		grid.add(new Label("UnitName:"), 0, 0);
		grid.add(comboboxA, 1, 0);
		grid.add(new Label("SIPrefix:"), 0, 1);
		grid.add(comboboxB, 1, 1);
		grid.add(prefixCheckBox, 2, 1);
		grid.add(new Label("UnitEnum:"), 0, 2);
		grid.add(comboboxC, 1, 2);
		//grid.add(new Label("Dim.Expo.:"), 0, 3);
		//grid.add(dimExpoButton, 1, 3);
		//grid.add(dimExpoLbl, 1, 4);
		
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
				Object[] arr = {comboboxA.getValue(), comboboxB.getValue(), comboboxC.getValue(), dimExpo};
				return arr;
			}
			return null;
		});
	}
	
	public IfcUnit getUnit(Object[] content) {
		IfcSIUnit ifcSIUnit = new IfcSIUnit.Ifc4.Instance();
		ifcSIUnit.setName(new IfcSIUnitName.Ifc4((IfcSIUnitName_internal)content[0]));
		if(prefixCheckBox.isSelected()) {			
			ifcSIUnit.setPrefix(new IfcSIPrefix.Ifc4((IfcSIPrefix_internal)content[1]));
		}
		ifcSIUnit.setUnitType(new IfcUnitEnum.Ifc4((IfcUnitEnum_internal)content[2]));
		
		//IfcDimensionalExponents de = (IfcDimensionalExponents)content[3];
		//ifcSIUnit.setDimensions(de);
		//templateModel.addObject(de);
										
		templateModel.addObject(ifcSIUnit);
		return (IfcUnit)ifcSIUnit;
	}
	
	public static void removeUnit(IfcModel model, IfcSIUnit content) {
		if(content.getDimensions() != null) {
			DialogIfcDimensionalExponents.removeUnit(model, content.getDimensions());
		}
		model.removeObject(content);
	}

}

package components.managers.unit;

import com.apstex.ifctoolbox.ifc.IfcDimensionalExponents;
import com.apstex.ifctoolbox.ifcmodel.IfcModel;
import com.apstex.step.core.INTEGER;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * 
 * @author Marcel Stepien
 *
 */
public class DialogIfcDimensionalExponents extends Dialog<Object[]> {
	
	private String stylingCheckbox = "-fx-pref-width: 250;";
	private IfcModel templateModel;

	public DialogIfcDimensionalExponents(IfcModel model) {
		this(model, new IfcDimensionalExponents.Ifc4.Instance(
				new INTEGER(0),new INTEGER(0),new INTEGER(0),new INTEGER(0),new INTEGER(0),new INTEGER(0),new INTEGER(0)));
	}
	
	public DialogIfcDimensionalExponents(IfcModel model, IfcDimensionalExponents dimExpo) {
		this.setTitle("Creating IfcDimensionalExponents");
		this.setHeaderText("Fill in the Form");
		this.templateModel = model;
		
		ButtonType createButtonType = new ButtonType("Create", ButtonData.OK_DONE);
		this.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

		// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		
		
		TextField n1 = this.createNumberField(dimExpo.getLengthExponent().value);
		TextField n2 = this.createNumberField(dimExpo.getMassExponent().value);
		TextField n3 = this.createNumberField(dimExpo.getTimeExponent().value);
		TextField n4 = this.createNumberField(dimExpo.getElectricCurrentExponent().value);
		TextField n5 = this.createNumberField(dimExpo.getThermodynamicTemperatureExponent().value);
		TextField n6 = this.createNumberField(dimExpo.getAmountOfSubstanceExponent().value);
		TextField n7 = this.createNumberField(dimExpo.getLuminousIntensityExponent().value);
		
		
		grid.add(new Label("LengthExponent:"), 0, 0);
		grid.add(n1, 1, 0);
		grid.add(new Label("MassExponent:"), 0, 1);
		grid.add(n2, 1, 1);
		grid.add(new Label("TimeExponent:"), 0, 2);
		grid.add(n3, 1, 2);
		grid.add(new Label("ElectricCurrentExponent:"), 0, 3);
		grid.add(n4, 1, 3);
		grid.add(new Label("ThermodynamicTemperatureExponent:"), 0, 4);
		grid.add(n5, 1, 4);
		grid.add(new Label("AmountOfSubstanceExponent:"), 0, 5);
		grid.add(n6, 1, 5);
		grid.add(new Label("LuminousIntensityExponent:"), 0, 6);
		grid.add(n7, 1, 6);

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
				Object[] arr = {
						n1.getText(),
						n2.getText(),
						n3.getText(),
						n4.getText(),
						n5.getText(),
						n6.getText(),
						n7.getText()
				};
				return arr;
			}
			return null;
		});
	}
	
	private TextField createNumberField(int value) {
		TextField numberField = new TextField();
		numberField.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		        if (!newValue.matches("\\d*")) {
		        	numberField.setText(newValue.replaceAll("[^\\d]", ""));
		        }
		    }
		});
		numberField.setText(Integer.toString(value));
		return numberField;
	}
	
	public IfcDimensionalExponents getUnit(Object[] content) {
		
		IfcDimensionalExponents dimExpo = new IfcDimensionalExponents.Ifc4.Instance();
		dimExpo.setLengthExponent(new INTEGER(Integer.parseInt((String)content[0])));
		dimExpo.setMassExponent(new INTEGER(Integer.parseInt((String)content[1])));
		dimExpo.setTimeExponent(new INTEGER(Integer.parseInt((String)content[2])));
		dimExpo.setElectricCurrentExponent(new INTEGER(Integer.parseInt((String)content[3])));
		dimExpo.setThermodynamicTemperatureExponent(new INTEGER(Integer.parseInt((String)content[4])));
		dimExpo.setAmountOfSubstanceExponent(new INTEGER(Integer.parseInt((String)content[5])));
		dimExpo.setLuminousIntensityExponent(new INTEGER(Integer.parseInt((String)content[6])));
										
		//templateModel.addObject(dimExpo);
		return dimExpo;
	}
	
	public static void removeUnit(IfcModel model, IfcDimensionalExponents content) {
		model.removeObject(content);
	}

}

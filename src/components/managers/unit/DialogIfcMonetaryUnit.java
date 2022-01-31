package components.managers.unit;

import com.apstex.ifctoolbox.ifc.IfcLabel;
import com.apstex.ifctoolbox.ifc.IfcMonetaryUnit;
import com.apstex.ifctoolbox.ifc.IfcUnit;
import com.apstex.ifctoolbox.ifcmodel.IfcModel;

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
public class DialogIfcMonetaryUnit extends Dialog<Object[]> {

	private String stylingCheckbox = "-fx-pref-width: 250;";
	private IfcModel templateModel;

	public DialogIfcMonetaryUnit(IfcModel model) {
		this.setTitle("Creating IfcMonetaryUnit");
		this.setHeaderText("Fill in the Form");
		this.templateModel = model;

		ButtonType createButtonType = new ButtonType("Create", ButtonData.OK_DONE);
		this.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

		// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField textField = new TextField();
		textField.setText("EUR");
		textField.setStyle(stylingCheckbox);

		grid.add(new Label("Currency:"), 0, 0);
		grid.add(textField, 1, 0);

		this.getDialogPane().setContent(grid);

		// Convert the result to a username-password-pair 
		// when the login button is clicked.
		this.setResultConverter(dialogButton -> {
			if (dialogButton == createButtonType) {
				Object[] arr = { textField.getText() };
				return arr;
			}
			return null;
		});
	}

	public IfcUnit getUnit(Object[] content) {
		IfcMonetaryUnit.Ifc4 ifcSIUnit = new IfcMonetaryUnit.Ifc4.Instance();
		ifcSIUnit.setCurrency(new IfcLabel.Ifc4((String) content[0]));

		templateModel.addObject(ifcSIUnit);
		return (IfcUnit) ifcSIUnit;
	}

	public static void removeUnit(IfcModel model, IfcMonetaryUnit content) {
		model.removeObject(content);
	}

}

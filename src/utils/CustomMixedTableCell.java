package utils;

import java.util.ArrayList;
import java.util.Collection;

import com.apstex.ifctoolbox.ifc.IfcLabel;
import com.apstex.ifctoolbox.ifc.IfcPropertyEnumeration;
import com.apstex.ifctoolbox.ifc.IfcValue;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCode;

public class CustomMixedTableCell<T> extends TreeTableCell<T, Object> {

	private ComboBox combobox;
	private Object currentlySelectedItem = "";
	
	private TextField textField;
	
	public CustomMixedTableCell() {
		Tooltip tp = new Tooltip("");
		tp.setWrapText(true);
		this.setTooltip(tp);
	}

	@Override
	public void startEdit() {
		if (!isEmpty()) {
			super.startEdit();
			
			if(getItem() instanceof IfcPropertyEnumeration.Ifc4) {
				ArrayList<String> list = new ArrayList<String>();
				for(IfcValue.Ifc4 val : ((IfcPropertyEnumeration.Ifc4)getItem()).getEnumerationValues()) {
					if(val instanceof IfcLabel.Ifc4) {
						list.add(((IfcLabel.Ifc4)val).getDecodedValue());
					}
				}
				createComboBox(list);

				setText(null);
				setGraphic(combobox);
				combobox.requestFocus();
			}else if(getItem() instanceof Collection<?>) {
				createComboBox((Collection<?>)getItem());

				setText(null);
				setGraphic(combobox);
				combobox.requestFocus();
			}else {
				createTextField();

				setText(null);
				setGraphic(textField);
				textField.requestFocus();
			}
			
		}
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		if(getItem() != null) {
			setText(getItem().toString());
		}
		setGraphic(null);
	}
	
	@Override
	public void updateItem(Object item, boolean empty) {
		super.updateItem(item, empty);

		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			if (isEditing()) {
				setText(null);
				
				if(combobox != null) {
					setGraphic(combobox);
				}
				
				if(textField != null) {
					if (textField != null) {
						textField.setText(getString());
					}
					setGraphic(textField);
				}
				
			} else {
				setText(getString());
				setGraphic(null);

				this.getTooltip().setText(getString());
			}
		}
	}

	private void createComboBox(Collection<?> itemList) {
		combobox = new ComboBox(FXCollections.observableArrayList(itemList));
		combobox.setOnAction(evt -> { // enable ENTER commit
			currentlySelectedItem = combobox.getSelectionModel().getSelectedItem();
			commitEdit(combobox.getSelectionModel().getSelectedItem());
		});

		combobox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
		combobox.getSelectionModel().select(currentlySelectedItem);
		
		ChangeListener<? super Boolean> changeListener = (observable, oldSelection, newSelection) -> {
			if (!newSelection) {
				commitEdit(combobox.getSelectionModel().getSelectedItem());
			}
		};
		combobox.focusedProperty().addListener(changeListener);

		combobox.setOnKeyPressed((ke) -> {
			if (ke.getCode().equals(KeyCode.ESCAPE)) {
				combobox.focusedProperty().removeListener(changeListener);
				cancelEdit();
			}
		});
	}
	
	private void createTextField() {
		textField = new TextField(getString());
		textField.setOnAction(evt -> { // enable ENTER commit
			commitEdit(textField.getText());
		});

		textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);

		ChangeListener<? super Boolean> changeListener = (observable, oldSelection, newSelection) -> {
			if (!newSelection) {
				commitEdit(textField.getText());
			}
		};
		textField.focusedProperty().addListener(changeListener);

		textField.setOnKeyPressed((ke) -> {
			if (ke.getCode().equals(KeyCode.ESCAPE)) {
				textField.focusedProperty().removeListener(changeListener);
				cancelEdit();
			}
		});
	}

	private String getString() {
		if(getItem() instanceof IfcPropertyEnumeration.Ifc4) {
			return currentlySelectedItem.toString();
		}if(getItem() instanceof Collection<?>) {
			return currentlySelectedItem.toString();
		}
		return getItem() == null ? "" : getItem().toString();
	}

	
	@Override
	public void commitEdit(Object item) {

		if (isEditing()) {
			super.commitEdit(item);
		} else {
			final TreeTableView<T> table = getTreeTableView();
			if (table != null) {
				TreeTablePosition position = new TreeTablePosition(
						getTreeTableView(), 
						getTreeTableRow().getIndex(),
						getTableColumn()
				);
				CellEditEvent editEvent = new CellEditEvent(
						table, 
						position, 
						TreeTableColumn.editCommitEvent(), 
						item
				);
				Event.fireEvent(getTableColumn(), editEvent);
			}
			updateItem(item, false);
			if (table != null) {
				table.edit(-1, null);
			}
		}
	}

}

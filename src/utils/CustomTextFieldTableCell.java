package utils;

import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCode;

public class CustomTextFieldTableCell<T> extends TreeTableCell<T, String> {

	private TextField textField;
	
	public CustomTextFieldTableCell() {
		Tooltip tp = new Tooltip("");
		tp.setWrapText(true);
		this.setTooltip(tp);
	}

	@Override
	public void startEdit() {
		if (!isEmpty()) {
			super.startEdit();
			createTextField();
			setText(null);
			setGraphic(textField);
			textField.selectAll();
			textField.requestFocus();
		}
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();

		setText(getItem());
		setGraphic(null);
	}

	@Override
	public void updateItem(String item, boolean empty) {
		super.updateItem(item, empty);

		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			if (isEditing()) {
				if (textField != null) {
					textField.setText(getString());
				}
				setText(null);
				setGraphic(textField);
				
				this.getTooltip().setText(textField.getText());
			} else {
				setText(getString());
				setGraphic(null);

				this.getTooltip().setText(getString());
			}
		}
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
		return getItem() == null ? "" : getItem().toString();
	}

	@Override
	public void commitEdit(String item) {

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

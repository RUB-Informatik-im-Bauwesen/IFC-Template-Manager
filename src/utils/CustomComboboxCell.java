package utils;

import java.util.Arrays;
import java.util.Collection;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCode;

public class CustomComboboxCell<T> extends TreeTableCell<T, Object> {

	private ComboBox<Object> combobox;
	private Object currentlySelectedItem = "";
	
	private Collection<String> psdTypes = null;
	private Collection<String> psdTypesForReferences = null;
	
	public CustomComboboxCell() {
		Tooltip tp = new Tooltip("");
		tp.setWrapText(true);
		this.setTooltip(tp);
		
		String[] psdItems = ApplicationUtilities.getIfcValueTypeList();

		Arrays.sort(psdItems);
		psdTypes = Arrays.asList(psdItems);
		
		String[] psdItemsForReferencevalue = { "IfcMaterialDefinition", "IfcPerson", "IfcOrganization", "IfcPersonAndOrganization", "IfcExternalReference", "IfcTimeSeries", "IfcAddress", "IfcAppliedValue", "IfcTable" };

		Arrays.sort(psdItemsForReferencevalue);
		psdTypesForReferences = Arrays.asList(psdItemsForReferencevalue);
	}

	@Override
	public void startEdit() {
		if (!isEmpty()) {
			super.startEdit();

			Object[] rowItems = (Object[])this.getTreeTableRow().getItem();
			if("P_REFERENCEVALUE".equals(rowItems[2])) {
				setText(null);			
				createComboBox(psdTypesForReferences);
				combobox.getSelectionModel().select(getItem());
				setGraphic(combobox);
				combobox.requestFocus();
			} else {
				setText(null);
				createComboBox(psdTypes);
				combobox.getSelectionModel().select(getItem());
				setGraphic(combobox);
				combobox.requestFocus();
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

		combobox.getSelectionModel().select(currentlySelectedItem);
		
		ChangeListener<? super Boolean> changeListener = (observable, oldSelection, newSelection) -> {
			if (!newSelection) {
				commitEdit(combobox.getSelectionModel().getSelectedItem());
			}
		};
		combobox.focusedProperty().addListener(changeListener);
		//combobox.setStyle(arg0);
		
		combobox.setOnKeyPressed((ke) -> {
			if (ke.getCode().equals(KeyCode.ESCAPE)) {
				combobox.focusedProperty().removeListener(changeListener);
				cancelEdit();
			}
		});
	}
	

	private String getString() {
		if(getItem() instanceof Collection<?>) {
			return currentlySelectedItem.toString();
		}
		return getItem() == null ? "" : getItem().toString();
		//return getItem() == null ? "" : getItem().toString();
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

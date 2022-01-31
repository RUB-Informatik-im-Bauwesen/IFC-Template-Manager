package components.templating;

import java.util.HashMap;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * A container class for wrapping Ifc-Types with a specific displayable text. 
 * Mainly needed for passing values and displaying own context. 
 * 
 * @author Marcel Stepien
 *
 * @param <T>
 */
public class ItemContainer<T> {
	
	private T item = null;
	private String display = null;
	private boolean selected = false;
	private HashMap<String, String> psdTypeMap;
	
	
	public ItemContainer(T item, String display) {
		this.item = item;
		this.display = display;
		this.psdTypeMap = new HashMap<>();
	}

	public final T getItem() {
		return item;
	}
	
	@Override
	public String toString() {
		return display;
	}
	
	public StringProperty nameProperty() { 
        if (display == null) {
        	return new SimpleStringProperty(this, "name");
        }
        return new SimpleStringProperty(this, display); 
    }
	
	public void setSelected(boolean select) {
		this.selected = select;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public String getPSDType(String guid) {
		String psdType = psdTypeMap.get(guid);
		if(psdType == null) {
			return "IfcLabel";
		}
		return psdType;
	}
	
	public void addPSDType(String guid, String type) {
		psdTypeMap.put(guid, type);
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public HashMap<String, String> getPsdTypeMap() {
		return psdTypeMap;
	}

	public void setPsdTypeMap(HashMap<String, String> psdTypeMap) {
		this.psdTypeMap = psdTypeMap;
	}

	public void setItem(T item) {
		this.item = item;
	}
}

package components.templating;

import com.apstex.ifctoolbox.ifc.IfcMaterial;

public class MaterialItem {
	
	private IfcMaterial material;
	private boolean selected=false;
	
	public MaterialItem(IfcMaterial material) {
		this.material = material;
	}
	
	public IfcMaterial getMaterial() {
		return material;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public boolean isSelected() {
		return selected;
	}

}

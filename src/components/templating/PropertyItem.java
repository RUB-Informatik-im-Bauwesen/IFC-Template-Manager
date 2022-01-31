package components.templating;

/**
 * 
 * @author Marcel Stepien
 *
 * @param <T>
 */
public class PropertyItem<T> {

	private String name;
	private String discription;
	private T value;
	private String valueType = "IfcLabel"; //Default type
	private String propertyType;
	
	private String lowerBound = "-1";
	private String upperBound = "-1";
	
	//private String primaryMeasureType = "";
	private String secondaryMeasureType = "";
	
	private Object additionalData = null;
	private Object unit = null;
	private Object accessState = null;

	public PropertyItem(String name, String discription, T value, String propertyType, String valueType) {
		this.name = name;
		this.discription = discription;
		this.value = value;
		this.propertyType = propertyType;
		this.valueType = valueType;
	}

	public String getPrimaryMeasureType() {
		return valueType;
	}

	public String getSecondaryMeasureType() {
		return secondaryMeasureType;
	}
	
	/*
	public void setPrimaryMeasureType(String primaryMeasureType) {
		this.primaryMeasureType = primaryMeasureType;
	}
	*/
	
	public void setSecondaryMeasureType(String secondaryMeasureType) {
		this.secondaryMeasureType = secondaryMeasureType;
	}
	
	public String getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(String lowerBound) {
		this.lowerBound = lowerBound;
	}

	public String getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(String upperBound) {
		this.upperBound = upperBound;
	}

	public Object getAccessState() {
		return accessState;
	}

	public void setAccessState(Object accessState) {
		this.accessState = accessState;
	}

	public Object getAdditionalData() {
		return additionalData;
	}

	public void setAdditionalData(Object additionalData) {
		this.additionalData = additionalData;
	}

	public Object getUnit() {
		return unit;
	}

	public void setUnit(Object unit) {
		this.unit = unit;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDiscription() {
		return discription;
	}

	public void setDiscription(String discription) {
		this.discription = discription;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public String getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(String propertyType) {
		this.propertyType = propertyType;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}
	
	@Override
	public String toString() {
		return name;
	}
}

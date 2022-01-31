package components.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import com.apstex.ifctoolbox.ifc.*;
import com.apstex.ifctoolbox.ifcmodel.IfcModel;
import com.apstex.step.core.LIST;
import com.apstex.step.core.SET;
import com.apstex.step.guidcompressor.GuidCompressor;

import components.templating.PropertyItem;
import utils.ApplicationUtilities;

/**
 * Toolkit for appending and removing PropertySets to the model
 * 
 * @author Marcel Stepien
 *
 */
public class ModelTransfer {
	
	// map of binded property enumerations (mixed IFC4 and IFC2x3 in the form of key_VERSION)
	private static HashMap<String, IfcPropertyEnumeration> propertyEnumMap;
	private IfcModel model; 
	
	public ModelTransfer(IfcModel model) {
		this.model = model;
	}
	
	public ModelTransfer() {
		this.model = ApplicationUtilities.model;
	}
	
	public void appendToMaterials(String propName, String propType, String description,
			ArrayList<PropertyItem> singleValues, Collection<IfcMaterial> materials) {
		
		
		materials.forEach(material ->{
			
			if(material instanceof IfcMaterial.Ifc4) {
				
				IfcMaterialProperties.Ifc4 ifcMaterialProperties = new IfcMaterialProperties.Ifc4.Instance();
				model.addObject(ifcMaterialProperties);
				
				ifcMaterialProperties.setName(new IfcIdentifier.Ifc4(propName, true));
				ifcMaterialProperties.setDescription(new IfcText.Ifc4(description, true));
				ifcMaterialProperties.setMaterial((IfcMaterialDefinition.Ifc4) material);
				
				SET<IfcProperty> properties = new SET<>();
				singleValues.forEach(item->{
					
					IfcProperty prop = new IfcPropertySingleValue.Ifc4.Instance(
							new IfcIdentifier.Ifc4(item.getName(), true),
							new IfcText.Ifc4(item.getName(), true), 
							convertType((String)item.getValue(), item.getValueType(), true),
							null
						); 
					model.addObject(prop);
					properties.add(prop);
					
				});
				
				ifcMaterialProperties.setProperties(properties);
				
				
			}
			
		});
		
//		if(o instanceof IfcObject.Ifc4) {
//			if(((IfcObject.Ifc4)o).getIsTypedBy_Inverse() != null) {
//				for(IfcRelDefinesByType.Ifc4 rel : ((IfcObject.Ifc4)o).getIsTypedBy_Inverse()) {
//					if(rel.getRelatingType() instanceof IfcTypeObject.Ifc4) {
//						newPropertySet(propName, discription, singleValues, objDef);
//					}else {
//						throw new Exception("PSET_TYPEDRIVENONLY check failed: " + objDef.getName() + " not a type of IfcTypeObject!");
//					}
//				}
//				
//			}else {
//				throw new Exception(o.toString() + " has no type definition. NULLPOINTER: therefore not a type of IfcTypeObject.");
//			}
//		}
		
	}

	/**
	 * Append IfcPropertySetTemplate instance by given information. The type to append will be
	 * choosen by propType. If the type is not existing with one of the types listed in 
	 * IfcPropertySetTemplateTypeEnum a default IfcPropertySet will be created.  
	 * 
	 * @param propName
	 * @param propType
	 * @param discription
	 * @param singleValues
	 * @param objDefs
	 */
	public void appendToModel(String propName, String propType, String discription,
			ArrayList<PropertyItem> singleValues, HashMap<String, IfcObjectDefinition> objDefs, boolean shared) {
		
		boolean isIfc4 = false;
		Object propset = null;

		SET<IfcObjectDefinition> oSetIfc4 = new SET<>();
		SET<IfcObject> oSetIfc2x3 = new SET<>();
		
		if(propertyEnumMap == null) { //comment out for enumeration duplication
			propertyEnumMap = new HashMap<String, IfcPropertyEnumeration>();			
		}
		
		try {
			Set<String> keys = objDefs.keySet();
			for (String key : keys) {
				// TODO switch types!! Replace Default
				isIfc4 = objDefs.get(key) instanceof IfcObjectDefinition.Ifc4;
				boolean pSetObjectCheck = false;
				boolean qtoObjectCheck = false;

				switch (propType) {
				case "PSET_TYPEDRIVENONLY":
					pSetObjectCheck = is_PSET_TYPEDRIVENONLY(objDefs.get(key));
					break;
				case "PSET_TYPEDRIVENOVERRIDE":
					pSetObjectCheck = is_PSET_TYPEDRIVENOVERRIDE(objDefs.get(key));
					break;
				case "PSET_OCCURRENCEDRIVEN":
					pSetObjectCheck = is_PSET_OCCURRENCEDRIVEN(objDefs.get(key));
					break;
				case "PSET_PERFORMANCEDRIVEN":
					pSetObjectCheck = is_PSET_PERFORMANCEDRIVEN(objDefs.get(key));
					break;
				case "QTO_TYPEDRIVENONLY":
					qtoObjectCheck = is_QTO_TYPEDRIVENONLY(objDefs.get(key));
					break;
				case "QTO_TYPEDRIVENOVERRIDE":
					qtoObjectCheck = is_QTO_TYPEDRIVENOVERRIDE(objDefs.get(key));
					break;
				case "QTO_OCCURRENCEDRIVEN":
					qtoObjectCheck = is_QTO_OCCURRENCEDRIVEN(objDefs.get(key));
					break;
				default:
					pSetObjectCheck = is_DEFAULT(objDefs.get(key));
					break;
				}
				
				if(!shared) {
					if(pSetObjectCheck || qtoObjectCheck) {
						if(pSetObjectCheck) {							
							propset = newPropertySet(propName, discription, singleValues, isIfc4);
						}
						
						if(qtoObjectCheck) {
							propset = newElementQuantity(propName, discription, singleValues, isIfc4); 
						}
					
						// Creating relation with object definition
						if(isIfc4) {
							SET<IfcObjectDefinition> oSetIfc4Intern = new SET<>();
							oSetIfc4Intern.add(objDefs.get(key));
							
							applyRelationIfc4(propName, (IfcPropertySetDefinitionSelect)propset, oSetIfc4Intern);
						}else {
							SET<IfcObject> oSetIfc2x3Intern = new SET<>();
							oSetIfc2x3Intern.add((IfcObject)objDefs.get(key));
							
							applyRelationIfc2x3(propName, (IfcPropertySetDefinition)propset, oSetIfc2x3Intern);
						}
		
						System.out.println("APPENDED " + propName + " TO: " + key);
					}
				}else {
					if(propset == null) {
						if(pSetObjectCheck) {							
							propset = newPropertySet(propName, discription, singleValues, isIfc4);
						}
						
						if(qtoObjectCheck) {							
							propset = newElementQuantity(propName, discription, singleValues, isIfc4); 
						}	
					}
					
					if(pSetObjectCheck || qtoObjectCheck) {
						if(isIfc4) {							
							oSetIfc4.add(objDefs.get(key));
						}else {
							oSetIfc2x3.add((IfcObject)objDefs.get(key));
						}
					}
				}
			}
			
			if(shared && propset != null) {
				// Creating relation with object definition
				if(isIfc4) {
					applyRelationIfc4(propName, (IfcPropertySetDefinitionSelect)propset, oSetIfc4);
				}else {
					applyRelationIfc2x3(propName, (IfcPropertySetDefinition)propset, oSetIfc2x3);
				}
				System.out.println("APPENDED " + propName + " TO SET OF " + objDefs.values().size() + " OBJECTS");
			}
			
		}catch (Exception e) {
			System.err.println("Error occured during creation of property set.");
			e.printStackTrace();
		}
		
	}
	
	private IfcRelDefinesByProperties applyRelationIfc4(String name, IfcPropertySetDefinitionSelect propset, SET<IfcObjectDefinition> oSet) {
		IfcRelDefinesByProperties rel = new IfcRelDefinesByProperties.Ifc4.Instance(
				new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true),
				model.getIfcProject().getOwnerHistory(), 
				new IfcLabel.Ifc4(name, true),
				new IfcText.Ifc4("", true), 
				oSet, 
				propset
		);
		model.addObject(rel);
		return rel;
	}
	
	private IfcRelDefinesByProperties applyRelationIfc2x3(String name, IfcPropertySetDefinition propset, SET<IfcObject> oSet) {
		IfcRelDefinesByProperties rel = new IfcRelDefinesByProperties.Ifc2x3.Instance(
				new IfcGloballyUniqueId.Ifc2x3(GuidCompressor.getNewIfcGloballyUniqueId(), true),
				model.getIfcProject().getOwnerHistory(), 
				new IfcLabel.Ifc2x3(name, true),
				new IfcText.Ifc2x3("", true), 
				oSet, 
				propset
		);
		model.addObject(rel);
		return rel;
	}
	
	private boolean is_PSET_TYPEDRIVENONLY(IfcObjectDefinition objDef) throws Exception {
		IfcObject o = (IfcObject)objDef;
		
		if(o instanceof IfcObject.Ifc4) {
			if(((IfcObject.Ifc4)o).getIsTypedBy_Inverse() != null) {
				for(IfcRelDefinesByType.Ifc4 rel : ((IfcObject.Ifc4)o).getIsTypedBy_Inverse()) {
					if(rel.getRelatingType() instanceof IfcTypeObject.Ifc4) {
						return true;
					}else {
						throw new Exception("PSET_TYPEDRIVENONLY check failed: " + objDef.getName() + " not a type of IfcTypeObject!");
					}
				}
				
			}else {
				throw new Exception(o.toString() + " has no type definition. NULLPOINTER: therefore not a type of IfcTypeObject.");
			}
		}
		if(o instanceof IfcObject.Ifc2x3) {
			if(((IfcObject.Ifc2x3)o).getIsDefinedBy_Inverse() != null) {
				
				for(IfcRelDefines.Ifc2x3 def : ((IfcObject.Ifc2x3)o).getIsDefinedBy_Inverse()) {
					if(def instanceof IfcRelDefinesByType.Ifc2x3) {
						IfcRelDefinesByType.Ifc2x3 rel = (IfcRelDefinesByType.Ifc2x3)def;
						if(rel.getRelatingType() instanceof IfcTypeObject.Ifc2x3) {
							return true;
						}else {
							throw new Exception("PSET_TYPEDRIVENONLY check failed: " + objDef.getName() + " not a type of IfcTypeObject!");
						}
					}
				}
				
			}else {
				throw new Exception(o.toString() + " has no type definition. NULLPOINTER: therefore not a type of IfcTypeObject.");
			}
		}
		return false;
	}

	private boolean is_PSET_TYPEDRIVENOVERRIDE(IfcObjectDefinition objDef) throws Exception {
		
		IfcObject o = (IfcObject)objDef;
		
		if(o instanceof IfcObject.Ifc4) {
			if(((IfcObject.Ifc4)o).getIsTypedBy_Inverse() != null) {
				for(IfcRelDefinesByType.Ifc4 rel : ((IfcObject.Ifc4)o).getIsTypedBy_Inverse()) {
					if(rel.getRelatingType() instanceof IfcTypeObject.Ifc4) {
						return true;
					}else {
						throw new Exception("PSET_TYPEDRIVENOVERRIDE check failed: " + objDef.getName() + " not a type of IfcTypeObject!");
					}
				}
				
			}else {
				throw new Exception(o.toString() + " has no type definition. NULLPOINTER: therefore not a type of IfcTypeObject.");
			}
		}
		if(o instanceof IfcObject.Ifc2x3) {
			if(((IfcObject.Ifc2x3)o).getIsDefinedBy_Inverse() != null) {
				
				for(IfcRelDefines.Ifc2x3 def : ((IfcObject.Ifc2x3)o).getIsDefinedBy_Inverse()) {
					if(def instanceof IfcRelDefinesByType.Ifc2x3) {
						IfcRelDefinesByType.Ifc2x3 rel = (IfcRelDefinesByType.Ifc2x3)def;
						if(rel.getRelatingType() instanceof IfcTypeObject.Ifc2x3) {
							return true;
						}else {
							throw new Exception("PSET_TYPEDRIVENOVERRIDE check failed: " + objDef.getName() + " not a type of IfcTypeObject!");
						}
					}
				}
				
			}else {
				throw new Exception(o.toString() + " has no type definition. NULLPOINTER: therefore not a type of IfcTypeObject.");
			}
		}
		return false;
	}

	private boolean is_PSET_OCCURRENCEDRIVEN(IfcObjectDefinition objDef) throws Exception {
		if(objDef instanceof IfcObject) {
			return true;
		}else {
			throw new Exception("PSET_OCCURRENCEDRIVEN check failed: " + objDef.getName() + " not a type of IfcObject!");
		}
	}

	private boolean is_PSET_PERFORMANCEDRIVEN(IfcObjectDefinition objDef) throws Exception {
		if(objDef instanceof IfcPerformanceHistory) {
			return true;
		}else {
			throw new Exception("PSET_PERFORMANCEDRIVEN check failed: " + objDef.getName() + " not a type of IfcPerformanceHistory!");
		}
	}

	private boolean is_QTO_TYPEDRIVENONLY(IfcObjectDefinition objDef) throws Exception {
		
		IfcObject o = (IfcObject)objDef;
		
		if(o instanceof IfcObject.Ifc4) {
			if(((IfcObject.Ifc4)o).getIsTypedBy_Inverse() != null) {
				for(IfcRelDefinesByType.Ifc4 rel : ((IfcObject.Ifc4)o).getIsTypedBy_Inverse()) {
					if(rel.getRelatingType() instanceof IfcTypeObject.Ifc4) {
						return true;
					}else {
						throw new Exception("QTO_TYPEDRIVENONLY check failed: " + objDef.getName() + " not a type of IfcObject!");
					}
				}
				
			}else {
				throw new Exception(o.toString() + " has no type definition. NULLPOINTER: therefore not a type of IfcTypeObject.");
			}
		}
		if(o instanceof IfcObject.Ifc2x3) {
			if(((IfcObject.Ifc2x3)o).getIsDefinedBy_Inverse() != null) {
				
				for(IfcRelDefines.Ifc2x3 def : ((IfcObject.Ifc2x3)o).getIsDefinedBy_Inverse()) {
					if(def instanceof IfcRelDefinesByType.Ifc2x3) {
						IfcRelDefinesByType.Ifc2x3 rel = (IfcRelDefinesByType.Ifc2x3)def;
						if(rel.getRelatingType() instanceof IfcTypeObject.Ifc2x3) {
							return true;
						}else {
							throw new Exception("QTO_TYPEDRIVENONLY check failed: " + objDef.getName() + " not a type of IfcObject!");
						}
					}
				}
				
			}else {
				throw new Exception(o.toString() + " has no type definition. NULLPOINTER: therefore not a type of IfcTypeObject.");
			}
		}
		return false;
	}

	private boolean is_QTO_TYPEDRIVENOVERRIDE(IfcObjectDefinition objDef) throws Exception {
		
		IfcObject o = (IfcObject)objDef;
		
		if(o instanceof IfcObject.Ifc4) {
			if(((IfcObject.Ifc4)o).getIsTypedBy_Inverse() != null) {
				for(IfcRelDefinesByType.Ifc4 rel : ((IfcObject.Ifc4)o).getIsTypedBy_Inverse()) {
					if(rel.getRelatingType() instanceof IfcTypeObject.Ifc4) {
						return true;
					}else {
						throw new Exception("QTO_TYPEDRIVENOVERRIDE check failed: " + objDef.getName() + " not a type of IfcObject!");
					}
				}
				
			}else {
				throw new Exception(o.toString() + " has no type definition. NULLPOINTER: therefore not a type of IfcTypeObject.");
			}
		}
		if(o instanceof IfcObject.Ifc2x3) {
			if(((IfcObject.Ifc2x3)o).getIsDefinedBy_Inverse() != null) {
				
				for(IfcRelDefines.Ifc2x3 def : ((IfcObject.Ifc2x3)o).getIsDefinedBy_Inverse()) {
					if(def instanceof IfcRelDefinesByType.Ifc2x3) {
						IfcRelDefinesByType.Ifc2x3 rel = (IfcRelDefinesByType.Ifc2x3)def;
						if(rel.getRelatingType() instanceof IfcTypeObject.Ifc2x3) {
							return true;
						}else {
							throw new Exception("QTO_TYPEDRIVENOVERRIDE check failed: " + objDef.getName() + " not a type of IfcObject!");
						}
					}
				}
				
			}else {
				throw new Exception(o.toString() + " has no type definition. NULLPOINTER: therefore not a type of IfcTypeObject.");
			}
		}
		return false;
	}

	private boolean is_QTO_OCCURRENCEDRIVEN(IfcObjectDefinition objDef) throws Exception {
		if(objDef instanceof IfcObject) {
			return true;
		}else {
			throw new Exception("QTO_OCCURRENCEDRIVEN check failed: " + objDef.getName() + " not a type of IfcObject!");
		}
	}

	/**
	 * Default Case
	 * 
	 * @param propName
	 * @param singleValues
	 * @param objDef
	 */
	private boolean is_DEFAULT(IfcObjectDefinition objDef) {
		return true;
	}

	/**
	 * Create a new IfcPropertySet that will be appended to the model.
	 * 
	 * @param propName
	 * @param discription
	 * @param singleValues
	 * @param objDef
	 */
	private IfcPropertySet newPropertySet(String propName, String discription, ArrayList<PropertyItem> singleValues,
			boolean isIfc4) {
		
		// Create Property Set
		ArrayList<IfcProperty> propertySetList = new ArrayList<>();

		for (PropertyItem item : singleValues) {

			IfcProperty prop = createProperty(model, item, isIfc4);
			propertySetList.add(prop);

		}
		SET<IfcProperty> properties = new SET<>(propertySetList);

		IfcPropertySet propertySet = isIfc4 ? 
			new IfcPropertySet.Ifc4.Instance(
				new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true),
				model.getIfcProject().getOwnerHistory(), new IfcLabel.Ifc4(propName, true),
				new IfcText.Ifc4(discription, true), properties
			) :
			new IfcPropertySet.Ifc2x3.Instance(
				new IfcGloballyUniqueId.Ifc2x3(GuidCompressor.getNewIfcGloballyUniqueId(), true),
				model.getIfcProject().getOwnerHistory(), new IfcLabel.Ifc2x3(propName, true),
				new IfcText.Ifc2x3(discription, true), properties
			);
		model.addObject(propertySet);

		return propertySet;
	}

	
	public IfcValue convertType(String value, String type, boolean isIfc4) {
		
		switch(type.toUpperCase()) {
			case "IFCINTEGER": return isIfc4 ? new IfcInteger.Ifc4(new Integer(value).intValue()) : new IfcInteger.Ifc2x3(new Integer(value).intValue());
			case "IFCREAL": return isIfc4 ? new IfcReal.Ifc4(new Double(value).doubleValue()) : new IfcReal.Ifc2x3(new Double(value).doubleValue());
			case "IFCBOOLEAN": return isIfc4 ? new IfcBoolean.Ifc4(new Boolean(value).booleanValue()) : new IfcBoolean.Ifc2x3(new Boolean(value).booleanValue());
			case "IFCLOGICAL": return isIfc4 ? new IfcLogical.Ifc4(new Boolean(value).booleanValue()) : new IfcLogical.Ifc2x3(new Boolean(value).booleanValue());
			case "IFCIDENTIFIER": return isIfc4 ? new IfcIdentifier.Ifc4((String)value, true) : new IfcIdentifier.Ifc2x3((String)value, true);
			case "IFCLABEL": return isIfc4 ? new IfcLabel.Ifc4((String)value, true) : new IfcLabel.Ifc2x3((String)value, true);
			case "IFCTEXT": return isIfc4 ? new IfcText.Ifc4((String)value, true) : new IfcText.Ifc2x3((String)value, true);	
			case "IFCDATETIME": return isIfc4 ? new IfcDateTime.Ifc4((String)value, true) : new IfcText.Ifc2x3((String)value, true);
			case "IFCDATE": return isIfc4 ? new IfcDate.Ifc4((String)value, true) : new IfcText.Ifc2x3((String)value, true);
			case "IFCTIME": return isIfc4 ? new IfcTime.Ifc4((String)value, true) : new IfcText.Ifc2x3((String)value, true);
			case "IFCDURATION": return isIfc4 ? new IfcDuration.Ifc4((String)value, true) : new IfcText.Ifc2x3((String)value, true);
			case "IFCTIMESTAMP": return isIfc4 ? new IfcTimeStamp.Ifc4(new Integer(value).intValue()) : new IfcTimeStamp.Ifc2x3(new Integer(value).intValue());
			case "IFCPOSITIVEINTEGER": return isIfc4 ? new IfcPositiveInteger.Ifc4(new Integer(value).intValue()) : new IfcInteger.Ifc2x3(new Integer(value).intValue()); 	
			case "IFCBINARY": return isIfc4 ? new IfcBinary.Ifc4(value) : new IfcText.Ifc2x3((String)value, true);

			//IfcMeasureValues
			case "IFCAMOUNTOFSUBSTANCEMEASURE": return isIfc4 ? new IfcAmountOfSubstanceMeasure.Ifc4(new Double(value).doubleValue()) : new IfcAmountOfSubstanceMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCAREAMEASURE": return isIfc4 ? new IfcAreaMeasure.Ifc4(new Double(value).doubleValue()) : new IfcAreaMeasure.Ifc2x3(new Double(value).doubleValue());
			//case "IFCCOMPLEXNUMBER": return isIfc4 ? new IfcComplexNumber.Ifc4(new Integer(value).intValue()) : new IfcComplexNumber.Ifc2x3(new Integer(value).intValue());
			case "IFCCONTEXTDEPENDENTMEASURE": return isIfc4 ? new IfcContextDependentMeasure.Ifc4(new Double(value).doubleValue()) : new IfcContextDependentMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCCOUNTMEASURE": return isIfc4 ? new IfcCountMeasure.Ifc4(new Double(value).doubleValue()) : new IfcCountMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCDESCRIPTIVEMEASURE": return isIfc4 ? new IfcDescriptiveMeasure.Ifc4((String)value, true) : new IfcDescriptiveMeasure.Ifc2x3((String)value, true);
			case "IFCELECTRICCURRENTMEASURE": return isIfc4 ? new IfcElectricCurrentMeasure.Ifc4(new Double(value).doubleValue()) : new IfcElectricCurrentMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCLENGTHMEASURE": return isIfc4 ? new IfcLengthMeasure.Ifc4(new Double(value).doubleValue()) : new IfcLengthMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCLUMINOUSINTENSITYMEASURE": return isIfc4 ? new IfcLuminousIntensityMeasure.Ifc4(new Double(value).doubleValue()) : new IfcLuminousIntensityMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCMASSMEASURE": return isIfc4 ? new IfcMassMeasure.Ifc4(new Double(value).doubleValue()) : new IfcMassMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCNONNEGATIVELENGTHMEASURE": return isIfc4 ? new IfcNonNegativeLengthMeasure.Ifc4(new Double(value).doubleValue()) : new IfcLengthMeasure.Ifc2x3(new Double(value).doubleValue()); //ONLY IFC4
			case "IFCNORMALISEDRATIOMEASURE": return isIfc4 ? new IfcNormalisedRatioMeasure.Ifc4(new Double(value).doubleValue()) : new IfcNormalisedRatioMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCNUMERICMEASURE": return isIfc4 ? new IfcNumericMeasure.Ifc4(new Double(value).doubleValue()) : new IfcNumericMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCPARAMETERVALUE": return isIfc4 ? new IfcParameterValue.Ifc4(new Double(value).doubleValue()) : new IfcParameterValue.Ifc2x3(new Double(value).doubleValue());
			case "IFCPLANEANGLEMEASURE": return isIfc4 ? new IfcPlaneAngleMeasure.Ifc4(new Double(value).doubleValue()) : new IfcPlaneAngleMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCPOSITIVELENGTHMEASURE": return isIfc4 ? new IfcPositiveLengthMeasure.Ifc4(new Double(value).doubleValue()) : new IfcPositiveLengthMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCPOSITIVEPLANEANGLEMEASURE": return isIfc4 ? new IfcPositivePlaneAngleMeasure.Ifc4(new Double(value).doubleValue()) : new IfcPositivePlaneAngleMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCPOSITIVERATIOMEASURE": return isIfc4 ? new IfcPositiveRatioMeasure.Ifc4(new Double(value).doubleValue()) : new IfcPositiveRatioMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCRATIOMEASURE": return isIfc4 ? new IfcRatioMeasure.Ifc4(new Double(value).doubleValue()) : new IfcRatioMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCSOLIDANGLEMEASURE": return isIfc4 ? new IfcSolidAngleMeasure.Ifc4(new Double(value).doubleValue()) : new IfcSolidAngleMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCTHERMODYNAMICTEMPERATUREMEASURE": return isIfc4 ? new IfcThermodynamicTemperatureMeasure.Ifc4(new Double(value).doubleValue()) : new IfcThermodynamicTemperatureMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCTIMEMEASURE": return isIfc4 ? new IfcTimeMeasure.Ifc4(new Double(value).doubleValue()) : new IfcTimeMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCVOLUMEMEASURE": return isIfc4 ? new IfcVolumeMeasure.Ifc4(new Double(value).doubleValue()) : new IfcVolumeMeasure.Ifc2x3(new Double(value).doubleValue());
			
			//IfcDerivedMeasureValues
			case "IFCABSORBEDDOSEMEASURE": return isIfc4 ? new IfcAbsorbedDoseMeasure.Ifc4(new Double(value).doubleValue()) : new IfcAbsorbedDoseMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCACCELERATIONMEASURE": return isIfc4 ? new IfcAccelerationMeasure.Ifc4(new Double(value).doubleValue()) : new IfcAccelerationMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCANGULARVELOCITYMEASURE": return isIfc4 ? new IfcAngularVelocityMeasure.Ifc4(new Double(value).doubleValue()) : new IfcAngularVelocityMeasure.Ifc2x3(new Double(value).doubleValue());
			//case "IFCCOMPOUNDPLANEANGLEMEASURE": return isIfc4 ? new IfcCompoundPlaneAngleMeasure.Ifc4(new Double(value).doubleValue()) : new IfcCompoundPlaneAngleMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCDOSEEQUIVALENTMEASURE": return isIfc4 ? new IfcDoseEquivalentMeasure.Ifc4(new Double(value).doubleValue()) : new IfcDoseEquivalentMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCDYNAMICVISCOSITYMEASURE": return isIfc4 ? new IfcDynamicViscosityMeasure.Ifc4(new Double(value).doubleValue()) : new IfcDynamicViscosityMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCELECTRICCAPACITANCEMEASURE": return isIfc4 ? new IfcElectricCapacitanceMeasure.Ifc4(new Double(value).doubleValue()) : new IfcElectricCapacitanceMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCELECTRICCHARGEMEASURE": return isIfc4 ? new IfcElectricChargeMeasure.Ifc4(new Double(value).doubleValue()) : new IfcElectricChargeMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCELECTRICCONDUCTANCEMEASURE": return isIfc4 ? new IfcElectricConductanceMeasure.Ifc4(new Double(value).doubleValue()) : new IfcElectricConductanceMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCELECTRICRESISTANCEMEASURE": return isIfc4 ? new IfcElectricResistanceMeasure.Ifc4(new Double(value).doubleValue()) : new IfcElectricResistanceMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCELECTRICVOLTAGEMEASURE": return isIfc4 ? new IfcElectricVoltageMeasure.Ifc4(new Double(value).doubleValue()) : new IfcElectricVoltageMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCENERGYMEASURE": return isIfc4 ? new IfcEnergyMeasure.Ifc4(new Double(value).doubleValue()) : new IfcEnergyMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCFORCEMEASURE": return isIfc4 ? new IfcForceMeasure.Ifc4(new Double(value).doubleValue()) : new IfcForceMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCFREQUENCYMEASURE": return isIfc4 ? new IfcFrequencyMeasure.Ifc4(new Double(value).doubleValue()) : new IfcFrequencyMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCHEATFLUXDENSITYMEASURE": return isIfc4 ? new IfcHeatFluxDensityMeasure.Ifc4(new Double(value).doubleValue()) : new IfcHeatFluxDensityMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCILLUMINANCEMEASURE": return isIfc4 ? new IfcIlluminanceMeasure.Ifc4(new Double(value).doubleValue()) : new IfcIlluminanceMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCINDUCTANCEMEASURE": return isIfc4 ? new IfcInductanceMeasure.Ifc4(new Double(value).doubleValue()) : new IfcInductanceMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCINTEGERCOUNTRATEMEASURE": return isIfc4 ? new IfcIntegerCountRateMeasure.Ifc4(new Integer(value).intValue()) : new IfcIntegerCountRateMeasure.Ifc2x3(new Integer(value).intValue());
			case "IFCISOTHERMALMOISTURECAPACITYMEASURE": return isIfc4 ? new IfcIsothermalMoistureCapacityMeasure.Ifc4(new Double(value).doubleValue()) : new IfcIsothermalMoistureCapacityMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCKINEMATICVISCOSITYMEASURE": return isIfc4 ? new IfcKinematicViscosityMeasure.Ifc4(new Double(value).doubleValue()) : new IfcKinematicViscosityMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCLINEARFORCEMEASURE": return isIfc4 ? new IfcLinearForceMeasure.Ifc4(new Double(value).doubleValue()) : new IfcLinearForceMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCLINEARMOMENTMEASURE": return isIfc4 ? new IfcLinearMomentMeasure.Ifc4(new Double(value).doubleValue()) : new IfcLinearMomentMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCLINEARSTIFFNESSMEASURE": return isIfc4 ? new IfcLinearStiffnessMeasure.Ifc4(new Double(value).doubleValue()) : new IfcLinearStiffnessMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCLINEARVELOCITYMEASURE": return isIfc4 ? new IfcLinearVelocityMeasure.Ifc4(new Double(value).doubleValue()) : new IfcLinearVelocityMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCLUMINOUSFLUXMEASURE": return isIfc4 ? new IfcLuminousFluxMeasure.Ifc4(new Double(value).doubleValue()) : new IfcLuminousFluxMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCMAGNETICFLUXDENSITYMEASURE": return isIfc4 ? new IfcMagneticFluxDensityMeasure.Ifc4(new Double(value).doubleValue()) : new IfcMagneticFluxDensityMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCMAGNETICFLUXMEASURE": return isIfc4 ? new IfcMagneticFluxMeasure.Ifc4(new Double(value).doubleValue()) : new IfcMagneticFluxMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCMASSDENSITYMEASURE": return isIfc4 ? new IfcMassDensityMeasure.Ifc4(new Double(value).doubleValue()) : new IfcMassDensityMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCMASSFLOWRATEMEASURE": return isIfc4 ? new IfcMassFlowRateMeasure.Ifc4(new Double(value).doubleValue()) : new IfcMassFlowRateMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCMODULUSOFELASTICITYMEASURE": return isIfc4 ? new IfcModulusOfElasticityMeasure.Ifc4(new Double(value).doubleValue()) : new IfcModulusOfElasticityMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCMODULUSOFSUBGRADEREACTIONMEASURE": return isIfc4 ? new IfcModulusOfSubgradeReactionMeasure.Ifc4(new Double(value).doubleValue()) : new IfcModulusOfSubgradeReactionMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCMOISTUREDIFFUSIVITYMEASURE": return isIfc4 ? new IfcMoistureDiffusivityMeasure.Ifc4(new Double(value).doubleValue()) : new IfcMoistureDiffusivityMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCMOLECULARWEIGHTMEASURE": return isIfc4 ? new IfcMolecularWeightMeasure.Ifc4(new Double(value).doubleValue()) : new IfcMolecularWeightMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCMOMENTOFINERTIAMEASURE": return isIfc4 ? new IfcMomentOfInertiaMeasure.Ifc4(new Double(value).doubleValue()) : new IfcMomentOfInertiaMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCMONETARYMEASURE": return isIfc4 ? new IfcMonetaryMeasure.Ifc4(new Double(value).doubleValue()) : new IfcMonetaryMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCPLANARFORCEMEASURE": return isIfc4 ? new IfcPlanarForceMeasure.Ifc4(new Double(value).doubleValue()) : new IfcPlanarForceMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCPOWERMEASURE": return isIfc4 ? new IfcPowerMeasure.Ifc4(new Double(value).doubleValue()) : new IfcPowerMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCPRESSUREMEASURE": return isIfc4 ? new IfcPressureMeasure.Ifc4(new Double(value).doubleValue()) : new IfcPressureMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCRADIOACTIVITYMEASURE": return isIfc4 ? new IfcRadioActivityMeasure.Ifc4(new Double(value).doubleValue()) : new IfcRadioActivityMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCROTATIONALFREQUENCYMEASURE": return isIfc4 ? new IfcRotationalFrequencyMeasure.Ifc4(new Double(value).doubleValue()) : new IfcRotationalFrequencyMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCROTATIONALSTIFFNESSMEASURE": return isIfc4 ? new IfcRotationalStiffnessMeasure.Ifc4(new Double(value).doubleValue()) : new IfcRotationalStiffnessMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCSHEARMODULUSMEASURE": return isIfc4 ? new IfcShearModulusMeasure.Ifc4(new Double(value).doubleValue()) : new IfcShearModulusMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCSPECIFICHEATCAPACITYMEASURE": return isIfc4 ? new IfcSpecificHeatCapacityMeasure.Ifc4(new Double(value).doubleValue()) : new IfcSpecificHeatCapacityMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCTHERMALADMITTANCEMEASURE": return isIfc4 ? new IfcThermalAdmittanceMeasure.Ifc4(new Double(value).doubleValue()) : new IfcThermalAdmittanceMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCTHERMALCONDUCTIVITYMEASURE": return isIfc4 ? new IfcThermalConductivityMeasure.Ifc4(new Double(value).doubleValue()) : new IfcThermalConductivityMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCTHERMALRESISTANCEMEASURE": return isIfc4 ? new IfcThermalResistanceMeasure.Ifc4(new Double(value).doubleValue()) : new IfcThermalResistanceMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCTHERMALTRANSMITTANCEMEASURE": return isIfc4 ? new IfcThermalTransmittanceMeasure.Ifc4(new Double(value).doubleValue()) : new IfcThermalTransmittanceMeasure.Ifc2x3(new Double(value).doubleValue());
			//case "IFCTIMESTAMP": return isIfc4 ? new IfcTimeStamp.Ifc4(new Integer(value).intValue()) : new IfcTimeStamp.Ifc2x3(new Integer(value).intValue());
			case "IFCTORQUEMEASURE": return isIfc4 ? new IfcTorqueMeasure.Ifc4(new Double(value).doubleValue()) : new IfcTorqueMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCVAPORPERMEABILITYMEASURE": return isIfc4 ? new IfcVaporPermeabilityMeasure.Ifc4(new Double(value).doubleValue()) : new IfcVaporPermeabilityMeasure.Ifc2x3(new Double(value).doubleValue());
			case "IFCVOLUMETRICFLOWRATEMEASURE": return isIfc4 ? new IfcVolumetricFlowRateMeasure.Ifc4(new Double(value).doubleValue()) : new IfcVolumetricFlowRateMeasure.Ifc2x3(new Double(value).doubleValue());
			default: break;
		}
		
		return isIfc4 ? new IfcLabel.Ifc4((String)value, true) : new IfcLabel.Ifc2x3((String)value, true);
	};
	
	public String getQuantityValueTypeOf(String propType) {
		switch(propType) {
		case "Q_LENGTH": return "IfcLengthMeasure"; 
		case "Q_AREA": return "IfcAreaMeasure"; 
		case "Q_VOLUME": return "IfcVolumeMeasure"; 
		case "Q_COUNT": return "IfcCountMeasure"; 
		case "Q_WEIGHT": return "IfcMassMeasure"; 
		case "Q_TIME": return "IfcTimeMeasure"; 
		default: break;
		}
		return null;
	}
	
	private IfcUnit convertUnit(IfcModel model, Object unitObject, boolean isIfc4) {
		IfcUnit unit = null;
		if(unitObject != null) {
			unit = (IfcUnit)unitObject;
			
			//FOR IFCSIUNIT
			if(unit instanceof IfcSIUnit) {
				IfcSIUnit siUnit = (IfcSIUnit)unit;
				IfcDimensionalExponents dimExpo = siUnit.getDimensions();
				
				IfcDimensionalExponents newDimExpo = null; 
//				if(dimExpo != null) {
//					newDimExpo = isIfc4 ? 
//							new IfcDimensionalExponents.Ifc4.Instance(
//									dimExpo.getLengthExponent(), 
//									dimExpo.getMassExponent(), 
//									dimExpo.getTimeExponent(), 
//									dimExpo.getElectricCurrentExponent(), 
//									dimExpo.getThermodynamicTemperatureExponent(), 
//									dimExpo.getAmountOfSubstanceExponent(), 
//									dimExpo.getLuminousIntensityExponent()) :
//							new IfcDimensionalExponents.Ifc2x3.Instance(
//									dimExpo.getLengthExponent(), 
//									dimExpo.getMassExponent(), 
//									dimExpo.getTimeExponent(), 
//									dimExpo.getElectricCurrentExponent(), 
//									dimExpo.getThermodynamicTemperatureExponent(), 
//									dimExpo.getAmountOfSubstanceExponent(), 
//									dimExpo.getLuminousIntensityExponent()
//							);
//					model.addObject(newDimExpo);
//				}
				
				
				IfcUnitEnum unitEnum = null;
				if(siUnit.getUnitType() != null) {
					unitEnum = isIfc4 ? 
							new IfcUnitEnum.Ifc4(siUnit.getUnitType().getValue().name()) : 
							new IfcUnitEnum.Ifc2x3(siUnit.getUnitType().getValue().name());
				}
				

				IfcSIPrefix unitPraefix = null;
				if(siUnit.getPrefix() != null) {
					unitPraefix = isIfc4 ? 
							new IfcSIPrefix.Ifc4(siUnit.getPrefix().getValue().name()) : 
							new IfcSIPrefix.Ifc2x3(siUnit.getPrefix().getValue().name());
				}
				
				
				IfcSIUnitName unitSiName = null;
				if(siUnit.getName() != null) {
					unitSiName = isIfc4 ? 
							new IfcSIUnitName.Ifc4(siUnit.getName().getValue().name()) : 
							new IfcSIUnitName.Ifc2x3(siUnit.getName().getValue().name());
				}		
				
				
				IfcSIUnit newUnit = isIfc4 ? 
						(IfcSIUnit)new IfcSIUnit.Ifc4.Instance(
								newDimExpo,
								unitEnum,
								unitPraefix,
								unitSiName
								
						) : 
						(IfcSIUnit)new IfcSIUnit.Ifc2x3.Instance(
								newDimExpo,
								unitEnum,
								unitPraefix,
								unitSiName
						);

				model.addObject(newUnit);
				return (IfcUnit)newUnit;
				
			}

			//FOR IfcConversionBasedUnit and IfcConversionBasedUnitWithOffset
			if(unit instanceof IfcConversionBasedUnit) {
				IfcConversionBasedUnit baseUnit = (IfcConversionBasedUnit)unit;
				IfcDimensionalExponents dimExpo = baseUnit.getDimensions();
				IfcMeasureWithUnit measureWithUnit = baseUnit.getConversionFactor();
				
				IfcDimensionalExponents newDimExpo = null; 
				if(dimExpo != null) {
					newDimExpo = isIfc4 ? 
							new IfcDimensionalExponents.Ifc4.Instance(
									dimExpo.getLengthExponent(), 
									dimExpo.getMassExponent(), 
									dimExpo.getTimeExponent(), 
									dimExpo.getElectricCurrentExponent(), 
									dimExpo.getThermodynamicTemperatureExponent(), 
									dimExpo.getAmountOfSubstanceExponent(), 
									dimExpo.getLuminousIntensityExponent()) :
							new IfcDimensionalExponents.Ifc2x3.Instance(
									dimExpo.getLengthExponent(), 
									dimExpo.getMassExponent(), 
									dimExpo.getTimeExponent(), 
									dimExpo.getElectricCurrentExponent(), 
									dimExpo.getThermodynamicTemperatureExponent(), 
									dimExpo.getAmountOfSubstanceExponent(), 
									dimExpo.getLuminousIntensityExponent()
							);
					model.addObject(newDimExpo);
				}
				
				IfcMeasureWithUnit newMeasureWithUnit = null;
				if(measureWithUnit != null) {
					newMeasureWithUnit = isIfc4 ? 
							new IfcMeasureWithUnit.Ifc4.Instance(
									convertType(
											measureWithUnit.getValueComponent().toString(),
											measureWithUnit.getValueComponent().getClassName(), isIfc4), 
									convertUnit(model, measureWithUnit.getUnitComponent(), isIfc4)) :
							new IfcMeasureWithUnit.Ifc2x3.Instance(
									convertType(
											measureWithUnit.getValueComponent().toString(),
											measureWithUnit.getValueComponent().getClassName(), isIfc4), 
									convertUnit(model, measureWithUnit.getUnitComponent(), isIfc4));
					
					model.addObject(newMeasureWithUnit);
				}
				
				
				IfcUnitEnum unitEnum = null;
				if(baseUnit.getUnitType() != null) {
					unitEnum = isIfc4 ? 
							new IfcUnitEnum.Ifc4(baseUnit.getUnitType().getValue().name()) : 
							new IfcUnitEnum.Ifc2x3(baseUnit.getUnitType().getValue().name());
				}
			
				IfcLabel unitName = null;
				if(baseUnit.getName() != null) {
					unitName = isIfc4 ? 
						new IfcLabel.Ifc4(baseUnit.getName().getDecodedValue()) : 
						new IfcLabel.Ifc2x3(baseUnit.getName().getDecodedValue());
				}	
							
				IfcConversionBasedUnit newUnit = isIfc4 ? 
						new IfcConversionBasedUnit.Ifc4.Instance(
								newDimExpo,
								unitEnum,
								unitName,
								newMeasureWithUnit
						) :
						new IfcConversionBasedUnit.Ifc2x3.Instance(
								newDimExpo,
								unitEnum,
								unitName,
								newMeasureWithUnit
						);
								
				if(unit instanceof IfcConversionBasedUnitWithOffset && isIfc4) {
					//Does not exist in Ifc2x3
					newUnit = (IfcConversionBasedUnit)new IfcConversionBasedUnitWithOffset.Ifc4.Instance(
								newDimExpo,
								unitEnum,
								unitName,
								newMeasureWithUnit,
								new IfcReal.Ifc4(((IfcConversionBasedUnitWithOffset)baseUnit).getConversionOffset().getValue())
					);
				}
								
				model.addObject(newUnit);
				return (IfcUnit)newUnit;
				
			}
			
			
			if(unit instanceof IfcDerivedUnit) {
				
				//TODO - Handle IfcDerivedUnit
				
				System.out.println("ModelTransfer/Export for IfcDerivedUnit not implemented yet!");
			}
			
			
			//For IfcMonetaryUnit
			if(unit instanceof IfcMonetaryUnit) {
				IfcMonetaryUnit.Ifc4 monUnit = (IfcMonetaryUnit.Ifc4)unit;
				
				IfcMonetaryUnit newUnit = isIfc4 ? 
						(IfcMonetaryUnit)new IfcMonetaryUnit.Ifc4.Instance(
								new IfcLabel.Ifc4(monUnit.getCurrency().getDecodedValue())
						) : //In Ifc2x3 replaced by Enumeration
						(IfcMonetaryUnit)new IfcMonetaryUnit.Ifc2x3.Instance(
								new IfcCurrencyEnum.Ifc2x3(IfcCurrencyEnum.Ifc2x3.IfcCurrencyEnum_internal.valueOf(
										monUnit.getCurrency().getDecodedValue())
								)
						);

				model.addObject(newUnit);
				return (IfcUnit)newUnit;
				
			}
			
		}
		return null;
	};

	/**
	 * Reads a PropertyItem and create a IfcProperty.
	 * 
	 * @param item
	 * @return
	 */
	private IfcProperty createProperty(IfcModel model, PropertyItem item, boolean isIfc4) {
		IfcProperty prop = null;

		switch (item.getPropertyType()) {
			case "P_SINGLEVALUE":
			{

				IfcUnit unit = convertUnit(model, item.getUnit(), isIfc4);
				
				prop = isIfc4 ? 
					(IfcProperty)new IfcPropertySingleValue.Ifc4.Instance(
						new IfcIdentifier.Ifc4(item.getName(), true),
						new IfcText.Ifc4(item.getName(), true), 
						convertType((String)item.getValue(), item.getValueType(), isIfc4),
						unit
					) : 
					(IfcProperty)new IfcPropertySingleValue.Ifc2x3.Instance(
						new IfcIdentifier.Ifc2x3(item.getName(), true),
						new IfcText.Ifc2x3(item.getName(), true), 
						convertType((String)item.getValue(), item.getValueType(), isIfc4),
						unit
					); //Unit
			}	
			break;
			case "P_ENUMERATEDVALUE":
			{
				IfcPropertyEnumeration propEnum = null;
				if(item.getAdditionalData() instanceof IfcPropertyEnumeration.Ifc4) {
					
					IfcPropertyEnumeration.Ifc4 templateEnum = (IfcPropertyEnumeration.Ifc4)item.getAdditionalData();
					if(isIfc4) {								
						propEnum = propertyEnumMap.get(templateEnum.getName() + "_IFC4");
					}else {
						propEnum = propertyEnumMap.get(templateEnum.getName() + "_IFC2x3");	
					}
					
					if(propEnum == null) {
						LIST<IfcValue> valueList = new LIST<IfcValue>();
						for(IfcValue.Ifc4 val : templateEnum.getEnumerationValues()) {
							if(isIfc4) {
								valueList.add(val);							
							}else {
								if(val instanceof IfcLabel.Ifc4) { //ONLY IFCLABEL!?						
									valueList.add(new IfcLabel.Ifc2x3(((IfcLabel.Ifc4)val).getDecodedValue(), true));
								}
							}
							
						}
						
						IfcUnit unit = convertUnit(model, item.getUnit(), isIfc4);
						
						propEnum = isIfc4 ? 
							(IfcPropertyEnumeration)new IfcPropertyEnumeration.Ifc4.Instance(
								new IfcLabel.Ifc4(templateEnum.getName().getDecodedValue(), true), 
								valueList, 
								unit) : 
							(IfcPropertyEnumeration)new IfcPropertyEnumeration.Ifc2x3.Instance(
								new IfcLabel.Ifc2x3(templateEnum.getName().getDecodedValue(), true), 
								valueList, 
								unit
							);
						
						//create binding to filter out duplicates persistent
						if(isIfc4) {
							propertyEnumMap.put(templateEnum.getName() + "_IFC4", propEnum);
						}else {
							propertyEnumMap.put(templateEnum.getName() + "_IFC2x3", propEnum);	
						}
						model.addObject(propEnum);
					}
					//System.out.println(templateEnum.getName() + "_#" + templateEnum.getStepLineNumber());
				}
				
				// Expect the value to be a String that can be splitted by ','
				LIST<IfcValue> list = new LIST<>();
				for (String s : item.getValue().toString().split(",")) {
					list.add(convertType(s, item.getValueType(), isIfc4));
				}
	
				prop = isIfc4 ? 
					(IfcProperty)new IfcPropertyEnumeratedValue.Ifc4.Instance(
						new IfcIdentifier.Ifc4(item.getName(), true),
						new IfcText.Ifc4(item.getName(), true), 
						list, 
						propEnum
					) :
					(IfcProperty)new IfcPropertyEnumeratedValue.Ifc2x3.Instance(
						new IfcIdentifier.Ifc2x3(item.getName(), true),
						new IfcText.Ifc2x3(item.getName(), true), 
						list, 
						propEnum
					);
						
			}
			break;
			case "P_BOUNDEDVALUE":	
			{
				
				IfcValue lowerBoundValue = convertType(item.getLowerBound(), item.getValueType(), isIfc4);
				IfcValue upperBoundValue = convertType(item.getUpperBound(), item.getSecondaryMeasureType(), isIfc4);
				IfcValue boundValue = convertType(item.getValue().toString(), item.getValueType(), isIfc4);
	
				IfcUnit unit = convertUnit(model, item.getUnit(), isIfc4);
				
				prop = isIfc4 ? 
					(IfcProperty) new IfcPropertyBoundedValue.Ifc4.Instance(
						new IfcIdentifier.Ifc4(item.getName(), true),
						new IfcText.Ifc4(item.getName(), true), 
						upperBoundValue,
						lowerBoundValue,
						unit, //Unit
						boundValue
					) : 
					new IfcPropertyBoundedValue.Ifc2x3.Instance(
						new IfcIdentifier.Ifc2x3(item.getName(), true),
						new IfcText.Ifc2x3(item.getName(), true), 
						upperBoundValue,
						lowerBoundValue, 
						unit //Unit
						//new IfcText.Ifc2x3(setPointValue, true) //missing in IFC2x3!
					);
			}	
			break;	
			case "P_LISTVALUE":
			{
				// Expect the value to be a String that can be splitted by ','
				LIST<IfcValue> list = new LIST<>();
				for (String s : item.getValue().toString().split(",")) {
					list.add(convertType(s, item.getValueType(), isIfc4));
				}

				IfcUnit unit = convertUnit(model, item.getUnit(), isIfc4);
				
				prop = isIfc4 ? 
					(IfcProperty)new IfcPropertyListValue.Ifc4.Instance(
						new IfcIdentifier.Ifc4(item.getName(), true),
						new IfcText.Ifc4(item.getName(), true), 
						list, 
						unit //Unit
					) : 
					(IfcProperty)new IfcPropertyListValue.Ifc2x3.Instance(
							new IfcIdentifier.Ifc2x3(item.getName(), true),
							new IfcText.Ifc2x3(item.getName(), true), 
							list, 
							unit //Unit
					);
			}
			break;	
			case "P_TABLEVALUE":
			{
				// Expect the value to be a String that can be splitted by ',' and ';'
				LIST<IfcValue> definingValues = new LIST<>();
				LIST<IfcValue> definedValues = new LIST<>();
				
				IfcCurveInterpolationEnum.Ifc4.IfcCurveInterpolationEnum_internal interpolationEnum = 
						IfcCurveInterpolationEnum.Ifc4.IfcCurveInterpolationEnum_internal.NOTDEFINED;
				
				String[] parts = item.getValue().toString().split(";");
				for (int i = 0; i < parts.length; i++) {
					switch (i) {
					case 0:
						for (String s3 : parts[i].split(",")) {
							definingValues.add(convertType(s3, item.getValueType(), isIfc4));
						}
						break;
					case 1:
						for (String s3 : parts[i].split(",")) {
							definedValues.add(convertType(s3, item.getValueType(), isIfc4));
						}
						break;
					case 2:
						switch(parts[i].toUpperCase()) {
						case "LINEAR" :  
							interpolationEnum = IfcCurveInterpolationEnum.Ifc4.IfcCurveInterpolationEnum_internal.LINEAR;
							break;
						case "LOG_LINEAR" :  
							interpolationEnum = IfcCurveInterpolationEnum.Ifc4.IfcCurveInterpolationEnum_internal.LOG_LINEAR;
							break;
						case "LOG_LOG" :  
							interpolationEnum = IfcCurveInterpolationEnum.Ifc4.IfcCurveInterpolationEnum_internal.LOG_LOG;
							break;
						default:
							interpolationEnum = IfcCurveInterpolationEnum.Ifc4.IfcCurveInterpolationEnum_internal.NOTDEFINED;
							break;
						}
						break;
					default:
						break;
					}
				}

				IfcUnit unit = convertUnit(model, item.getUnit(), isIfc4);
				
				prop = isIfc4 ? 
					(IfcProperty)new IfcPropertyTableValue.Ifc4.Instance(
						new IfcIdentifier.Ifc4(item.getName(), true),
						new IfcText.Ifc4(item.getDiscription(), true), 
						definingValues,
						definedValues,
						null, //Expression 
						unit, //UNIT
						unit, //UNIT
						new IfcCurveInterpolationEnum.Ifc4(interpolationEnum)
					) : 
					(IfcProperty)new IfcPropertyTableValue.Ifc2x3.Instance(
						new IfcIdentifier.Ifc2x3(item.getName(), true),
						new IfcText.Ifc2x3(item.getDiscription(), true), 
						definingValues,
						definedValues,
						null, //Expression 
						unit, //UNIT
						unit //UNIT
						//new IfcCurveInterpolationEnum.Ifc4(interpolationEnum) //MISSING in Ifc2x3!
					);
			}
			break;	
			case "P_REFERENCEVALUE":
			{
				//String lineRef = item.getValue().toString();				
				//ClassInterface obj = model.getObjectByEntityInstanceName(new Integer(lineRef).intValue());
				
				Object obj = item.getValue();
				
				IfcObjectReferenceSelect ref = null;
				
				if(obj instanceof IfcObjectReferenceSelect) {
					ref = (IfcObjectReferenceSelect)obj;
				}
				
				if(ref != null) {
					prop = isIfc4 ?
						(IfcProperty) new IfcPropertyReferenceValue.Ifc4.Instance(
							new IfcIdentifier.Ifc4(item.getName(), true),
							new IfcText.Ifc4(item.getName(), true), 
							new IfcText.Ifc4(item.getDiscription(), true),
							ref
						) : 
						(IfcProperty) new IfcPropertyReferenceValue.Ifc2x3.Instance(
							new IfcIdentifier.Ifc2x3(item.getName(), true),
							new IfcText.Ifc2x3(item.getName(), true), 
							new IfcLabel.Ifc2x3(item.getDiscription(), true), //is IfcLabel in IFC2x3
							ref
						);
				}
				
			}
			break;
			default:
			{
				IfcUnit unit = convertUnit(model, item.getUnit(), isIfc4);
				
				prop = isIfc4 ? 
					(IfcProperty)new IfcPropertySingleValue.Ifc4.Instance(
						new IfcIdentifier.Ifc4(item.getName(), true),
						new IfcText.Ifc4(item.getName(), true), 
						convertType((String)item.getValue(), item.getValueType(), isIfc4),
						unit
					) : 
					(IfcProperty)new IfcPropertySingleValue.Ifc2x3.Instance(
						new IfcIdentifier.Ifc2x3(item.getName(), true),
						new IfcText.Ifc2x3(item.getName(), true), 
						convertType((String)item.getValue(), item.getValueType(), isIfc4),
						unit
					);
			}
			break;	
		}
		
		if(prop != null) {
			prop.setDescription(isIfc4 ? new IfcText.Ifc4(item.getDiscription(), true) : new IfcText.Ifc2x3(item.getDiscription(), true));
			model.addObject(prop);	
		}else {
			System.err.println("Property " + item.getName() + " was not created. Insufficient information entered.");
		}
		
		return prop;
	}
	
	/**
	 * Create a new IfcElementQuantity that will be appended to the model.
	 * 
	 * @param propName
	 * @param discription
	 * @param singleValues
	 * @param objDef
	 */
	public IfcElementQuantity newElementQuantity(String propName, String discription, ArrayList<PropertyItem> singleValues, boolean isIfc4) {
		// Create Quantitie Set
		ArrayList<IfcPhysicalQuantity> propertySetList = new ArrayList<>();
		for (PropertyItem item : singleValues) {

			IfcPhysicalQuantity prop = createQuantity(item, isIfc4);
			propertySetList.add(prop);

			model.addObject(prop);
		}
		SET<IfcPhysicalQuantity> properties = new SET<>(propertySetList);

		
		// Create Element Quantity
		IfcElementQuantity elementQuantity = isIfc4 ? 
			new IfcElementQuantity.Ifc4.Instance( 
				new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(),true),
				model.getIfcProject().getOwnerHistory(), new
				IfcLabel.Ifc4(propName, true), 
				new IfcText.Ifc4("", true), 
				new IfcLabel.Ifc4("", true),
				properties 
			) : 
			new IfcElementQuantity.Ifc2x3.Instance( 
				new IfcGloballyUniqueId.Ifc2x3(GuidCompressor.getNewIfcGloballyUniqueId(),true),
				model.getIfcProject().getOwnerHistory(), new
				IfcLabel.Ifc2x3(propName, true), 
				new IfcText.Ifc2x3("", true), 
				new IfcLabel.Ifc2x3("", true),
				properties 
			); 
		model.addObject(elementQuantity);
		
		return elementQuantity;
	}
	
	/**
	 * Reads a PropertyItem and create a IfcPhysicalQuantity.
	 * 
	 * @param item
	 * @return
	 */
	private IfcPhysicalQuantity createQuantity(PropertyItem item, boolean isIfc4) {
		IfcPhysicalQuantity out = null;

		switch (item.getPropertyType()) {
			case "Q_LENGTH": 
			{
				IfcQuantityLength quan = isIfc4 ? 
					new IfcQuantityLength.Ifc4.Instance(
						new IfcLabel.Ifc4(item.getName(), true), //Name
						new IfcText.Ifc4(item.getDiscription(), true), //Description 
						null, //IfcNameUnit
						//convertType((String)item.getValue(), item.getValueType(), isIfc4),
						new IfcLengthMeasure.Ifc4(new Double(item.getValue().toString())), //Value
						null //Formula
					) : 
					new IfcQuantityLength.Ifc2x3.Instance(
						new IfcLabel.Ifc2x3(item.getName(), true), //Name
						new IfcText.Ifc2x3(item.getDiscription(), true), //Description 
						null, //IfcNameUnit
						new IfcLengthMeasure.Ifc2x3(new Double(item.getValue().toString())) //Value
						//null //Formula missing in IFC2x3
					);
				
				out = (IfcPhysicalQuantity)quan;
			}
			break;
			case "Q_AREA": 
			{
				IfcQuantityArea quan = isIfc4 ?
					new IfcQuantityArea.Ifc4.Instance(
						new IfcLabel.Ifc4(item.getName(), true), //Name
						new IfcText.Ifc4(item.getDiscription(), true), //Description 
						null, //IfcNameUnit
						new IfcAreaMeasure.Ifc4(new Double(item.getValue().toString())), //Value 
						null //Formula
					) : 
					new IfcQuantityArea.Ifc2x3.Instance(
						new IfcLabel.Ifc2x3(item.getName(), true), //Name
						new IfcText.Ifc2x3(item.getDiscription(), true), //Description 
						null, //IfcNameUnit
						new IfcAreaMeasure.Ifc2x3(new Double(item.getValue().toString())) //Value 
						//null //Formula missing in IFC2x3
					);
			
				out = (IfcPhysicalQuantity)quan;
			}
			break;
			case "Q_VOLUME":
			{
				IfcQuantityVolume quan = isIfc4 ? 
					new IfcQuantityVolume.Ifc4.Instance(
						new IfcLabel.Ifc4(item.getName(), true), //Name
						new IfcText.Ifc4(item.getDiscription(), true), //Description 
						null, //IfcNameUnit
						new IfcVolumeMeasure.Ifc4(new Double(item.getValue().toString())), //Value 
						null //Formula
					) : 
					new IfcQuantityVolume.Ifc2x3.Instance(
						new IfcLabel.Ifc2x3(item.getName(), true), //Name
						new IfcText.Ifc2x3(item.getDiscription(), true), //Description 
						null, //IfcNameUnit
						new IfcVolumeMeasure.Ifc2x3(new Double(item.getValue().toString())) //Value 
						//null //Formula missing in IFC2x3
					);
			
				out = (IfcPhysicalQuantity)quan;
			}
			break;
			case "Q_COUNT": 
			{
				IfcQuantityCount quan = isIfc4 ? 
					new IfcQuantityCount.Ifc4.Instance(
						new IfcLabel.Ifc4(item.getName(), true), //Name
						new IfcText.Ifc4(item.getDiscription(), true), //Description 
						null, //IfcNameUnit
						new IfcCountMeasure.Ifc4(new Double(item.getValue().toString())), //Value 
						null //Formula
					) : new IfcQuantityCount.Ifc2x3.Instance(
						new IfcLabel.Ifc2x3(item.getName(), true), //Name
						new IfcText.Ifc2x3(item.getDiscription(), true), //Description 
						null, //IfcNameUnit
						new IfcCountMeasure.Ifc2x3(new Double(item.getValue().toString())) //Value 
						//null //Formula missing in IFC2x3
				    );
			
				out = (IfcPhysicalQuantity)quan;
			}
			break;
			case "Q_WEIGHT": 
			{
				IfcQuantityWeight quan = isIfc4 ? 
					new IfcQuantityWeight.Ifc4.Instance(
						new IfcLabel.Ifc4(item.getName(), true), //Name
						new IfcText.Ifc4(item.getDiscription(), true), //Description 
						null, //IfcNameUnit
						new IfcMassMeasure.Ifc4(new Double(item.getValue().toString())), //Value 
						null //Formula
					) : 
					new IfcQuantityWeight.Ifc2x3.Instance(
						new IfcLabel.Ifc2x3(item.getName(), true), //Name
						new IfcText.Ifc2x3(item.getDiscription(), true), //Description 
						null, //IfcNameUnit
						new IfcMassMeasure.Ifc2x3(new Double(item.getValue().toString())) //Value 
						//null //Formula missing in IFC2x3
					);
			
				out = (IfcPhysicalQuantity)quan;
			}
			break;
			case "Q_TIME": 	
			{
				IfcQuantityTime quan = isIfc4 ? 
					new IfcQuantityTime.Ifc4.Instance(
						new IfcLabel.Ifc4(item.getName(), true), //Name
						new IfcText.Ifc4(item.getDiscription(), true), //Description 
						null, //IfcNameUnit
						new IfcTimeMeasure.Ifc4(new Double(item.getValue().toString())), //Value 
						null //Formula
					) : 
					new IfcQuantityTime.Ifc2x3.Instance(
						new IfcLabel.Ifc2x3(item.getName(), true), //Name
						new IfcText.Ifc2x3(item.getDiscription(), true), //Description 
						null, //IfcNameUnit
						new IfcTimeMeasure.Ifc2x3(new Double(item.getValue().toString())) //Value 
						//null //Formula missing in IFC2x3
					);
			
				out = (IfcPhysicalQuantity)quan;
			}
			break;
			default: 
			{
				IfcQuantityLength quan = isIfc4 ? 
					new IfcQuantityLength.Ifc4.Instance(
						new IfcLabel.Ifc4(item.getName(), true), //Name
						new IfcText.Ifc4(item.getDiscription(), true), //Description 
						null, //IfcNameUnit
						new IfcLengthMeasure.Ifc4(new Double(item.getValue().toString())), //Value
						null //Formula
					) : 
					new IfcQuantityLength.Ifc2x3.Instance(
							new IfcLabel.Ifc2x3(item.getName(), true), //Name
							new IfcText.Ifc2x3(item.getDiscription(), true), //Description 
							null, //IfcNameUnit
							new IfcLengthMeasure.Ifc2x3(new Double(item.getValue().toString())) //Value
							//null //Formula missing in IFC2x3
					);
				
				out = (IfcPhysicalQuantity)quan;
			}
			break;
		}
		
		return out;
	}
	

	/**
	 * Clear all PropertySets of a Map object
	 * 
	 * @param objDef
	 */
	public void clearAllPropertieSet(HashMap<String, IfcObjectDefinition> objDefs) {
		for (String key : objDefs.keySet()) {
			if (objDefs.get(key) instanceof IfcObject) {
				clearAllPropertieSet((IfcObject)objDefs.get(key));
			}
		}
	}

	/**
	 * Clear all PropertySets of a object
	 * 
	 * @param objDef
	 */
	public void clearAllPropertieSet(IfcObject objDef) {
		
		boolean isIfc4 = objDef instanceof IfcObject.Ifc4;
		
		if (objDef != null) {
			if(isIfc4) {
				if (((IfcObject.Ifc4)objDef).getIsDefinedBy_Inverse() != null) {

					for (Object rel : ((IfcObject.Ifc4)objDef).getIsDefinedBy_Inverse()) {
						if (rel instanceof IfcRelDefinesByProperties.Ifc4) {
							IfcRelDefinesByProperties ifcRel = (IfcRelDefinesByProperties.Ifc4) rel;		
							clearPropertieSet(((IfcObject.Ifc4)objDef), ifcRel);						
						}
					}
				}
			}else {
				if (((IfcObject.Ifc2x3)objDef).getIsDefinedBy_Inverse() != null) {

					for (Object rel : ((IfcObject.Ifc2x3)objDef).getIsDefinedBy_Inverse()) {
						if (rel instanceof IfcRelDefinesByProperties.Ifc2x3) {
							IfcRelDefinesByProperties ifcRel = (IfcRelDefinesByProperties.Ifc2x3) rel;		
							clearPropertieSet(((IfcObject.Ifc2x3)objDef), ifcRel);						
						}
					}
				}
			}
		}
	}
	
	/**
	 * delete PropertySet
	 * 
	 * @param objDef
	 */
	public void clearPropertieSet(IfcObject defObj, IfcRelDefinesByProperties ifcPropRel) {
		
		boolean isIfc4 = ifcPropRel instanceof IfcRelDefinesByProperties.Ifc4;
		
		if(isIfc4) {

			IfcPropertySetDefinitionSelect propSet = ((IfcRelDefinesByProperties.Ifc4)ifcPropRel).getRelatingPropertyDefinition();
			
			if(defObj instanceof IfcObjectDefinition.Ifc4) {				
				((IfcRelDefinesByProperties.Ifc4)ifcPropRel).removeRelatedObjects((IfcObjectDefinition.Ifc4)defObj);;
				System.out.println("Disconnected " + defObj.getName() + " from " + ifcPropRel.getName());
			}
			
			if(((IfcRelDefinesByProperties.Ifc4)ifcPropRel).getRelatedObjects().size() == 0) {
				if (propSet instanceof IfcPropertySet.Ifc4) {
					IfcPropertySet.Ifc4 psObj = (IfcPropertySet.Ifc4) propSet;

					for (IfcProperty.Ifc4 prop : psObj.getHasProperties()) {
						if(prop.getPartOfPset_Inverse().size() <= 1) {
							model.removeObject(prop);
							//model.removeObject(prop);
							System.out.println("Removed: " + prop.toString());
						}
					}
					model.removeObject(psObj);
					System.out.println("Removed: " + psObj.toString());
				}
				
				if (propSet instanceof IfcElementQuantity.Ifc4) {
					IfcElementQuantity.Ifc4 psObj = (IfcElementQuantity.Ifc4) propSet;

					for (IfcPhysicalQuantity.Ifc4 prop : psObj.getQuantities()) {
						if(prop.getPartOfComplex_Inverse() != null) {
							if(prop.getPartOfComplex_Inverse().size() <= 1) {
								continue;
							}
						}
						model.removeObject(prop);
						System.out.println("Removed: " + prop.toString());
						
					}
					model.removeObject(psObj);
					System.out.println("Removed: " + psObj.toString());
				}
				model.removeObject(ifcPropRel);
				System.out.println("Removed: " + ifcPropRel.toString());
			}
			
		}else {
			
			IfcPropertySetDefinition propSet = ((IfcRelDefinesByProperties.Ifc2x3)ifcPropRel).getRelatingPropertyDefinition();
			
			if(defObj instanceof IfcObject.Ifc2x3) {				
				((IfcRelDefinesByProperties.Ifc2x3)ifcPropRel).removeRelatedObjects((IfcObject.Ifc2x3)defObj);;
				System.out.println("Disconnected " + defObj.getName().getDecodedValue() + " from " + ifcPropRel.getName().getDecodedValue());
			}
			
			if(((IfcRelDefinesByProperties.Ifc2x3)ifcPropRel).getRelatedObjects().size() == 0) {
				if (propSet instanceof IfcPropertySet.Ifc2x3) {
					IfcPropertySet.Ifc2x3 psObj = (IfcPropertySet.Ifc2x3) propSet;

					for (IfcProperty.Ifc2x3 prop : psObj.getHasProperties()) {
						
						if(prop.getPartOfComplex_Inverse().size() <= 1) {
							model.removeObject(prop);
							//model.removeObject(prop);
							System.out.println("Removed: " + prop.toString());
						}
					}
					model.removeObject(psObj);
					System.out.println("Removed: " + psObj.toString());
				}
				
				if (propSet instanceof IfcElementQuantity.Ifc2x3) {
					IfcElementQuantity.Ifc2x3 psObj = (IfcElementQuantity.Ifc2x3) propSet;

					for (IfcPhysicalQuantity.Ifc2x3 prop : psObj.getQuantities()) {
						if(prop.getPartOfComplex_Inverse() != null) {
							if(prop.getPartOfComplex_Inverse().size() <= 1) {
								continue;
							}
						}
						model.removeObject(prop);
						System.out.println("Removed: " + prop.toString());
						
					}
					model.removeObject(psObj);
					System.out.println("Removed: " + psObj.toString());
				}
				model.removeObject(ifcPropRel);
				System.out.println("Removed: " + ifcPropRel.toString());
			}
		}
	}
}
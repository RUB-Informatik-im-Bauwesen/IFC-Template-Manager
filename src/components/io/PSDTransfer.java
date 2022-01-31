package components.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import com.apstex.ifctoolbox.ifc.IfcConversionBasedUnit;
import com.apstex.ifctoolbox.ifc.IfcDefinitionSelect;
import com.apstex.ifctoolbox.ifc.IfcGloballyUniqueId;
import com.apstex.ifctoolbox.ifc.IfcIdentifier;
import com.apstex.ifctoolbox.ifc.IfcLabel;
import com.apstex.ifctoolbox.ifc.IfcMonetaryUnit;
import com.apstex.ifctoolbox.ifc.IfcProjectLibrary;
import com.apstex.ifctoolbox.ifc.IfcPropertyEnumeration;
import com.apstex.ifctoolbox.ifc.IfcPropertySetTemplate;
import com.apstex.ifctoolbox.ifc.IfcPropertySetTemplateTypeEnum;
import com.apstex.ifctoolbox.ifc.IfcPropertyTemplate;
import com.apstex.ifctoolbox.ifc.IfcRelDeclares;
import com.apstex.ifctoolbox.ifc.IfcSIUnit;
import com.apstex.ifctoolbox.ifc.IfcSimplePropertyTemplate;
import com.apstex.ifctoolbox.ifc.IfcSimplePropertyTemplateTypeEnum;
import com.apstex.ifctoolbox.ifc.IfcText;
import com.apstex.ifctoolbox.ifc.IfcUnit;
import com.apstex.ifctoolbox.ifc.IfcUnitEnum;
import com.apstex.ifctoolbox.ifc.IfcValue;
import com.apstex.ifctoolbox.ifcmodel.IfcModel;
import com.apstex.step.core.LIST;
import com.apstex.step.core.SET;
import com.apstex.step.guidcompressor.GuidCompressor;

import components.templating.ItemContainer;
import io.PSDExporter;
import psd.DataType;
import psd.ObjectFactory;
import psd.PropertyDef;
import psd.PropertyDef.DefinitionAliases;
import psd.PropertyDef.DefinitionAliases.DefinitionAlias;
import psd.PropertyDef.NameAliases;
import psd.PropertyDef.NameAliases.NameAlias;
import psd.PropertySetDef;
import psd.PropertySetDef.ApplicableClasses;
import psd.PropertySetDef.IfcVersion;
import psd.PropertySetDef.PropertyDefs;
import psd.PropertyType;
import psd.PropertyType.TypePropertyBoundedValue;
import psd.PropertyType.TypePropertyBoundedValue.ValueRangeDef;
import psd.PropertyType.TypePropertyBoundedValue.ValueRangeDef.LowerBoundValue;
import psd.PropertyType.TypePropertyBoundedValue.ValueRangeDef.UpperBoundValue;
import psd.PropertyType.TypePropertyEnumeratedValue;
import psd.PropertyType.TypePropertyEnumeratedValue.EnumList;
import psd.PropertyType.TypePropertyListValue;
import psd.PropertyType.TypePropertyListValue.ListValue;
import psd.PropertyType.TypePropertyReferenceValue;
import psd.PropertyType.TypePropertySingleValue;
import psd.PropertyType.TypePropertyTableValue;
import psd.UnitType;
import utils.ApplicationUtilities;

public class PSDTransfer {
	
	private String languageHint = "de";

	public void writeFromIFC(String folderPath, ItemContainer<IfcPropertySetTemplate.Ifc4> container)
			throws IOException {

		IfcPropertySetTemplate.Ifc4 template = container.getItem();
		
		//Do not parse QTO Type PropertySets (ignoring this check enables the export)
		//================================================
		if( template.getTemplateType().getValue().equals(IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.QTO_OCCURRENCEDRIVEN) ||
			template.getTemplateType().getValue().equals(IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.QTO_TYPEDRIVENONLY) ||
			template.getTemplateType().getValue().equals(IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.QTO_TYPEDRIVENOVERRIDE)) {
			throw new IOException("QTO Type PropertySets cant be exported! See PropertySet: " + template.getName().getDecodedValue());
		}
		//================================================
		

		ObjectFactory factory = new ObjectFactory();
		PropertySetDef propSetDef = factory.createPropertySetDef();

		propSetDef.setIfdguid(template.getGlobalId().getDecodedValue());
		propSetDef.setName(template.getName().getDecodedValue());
		propSetDef.setDefinition(template.getDescription().getDecodedValue());

		if(template.getApplicableEntity() != null) {
			ApplicableClasses applicableClasses = new ApplicableClasses();
			applicableClasses.getClassName().addAll(Arrays.asList(template.getApplicableEntity().getDecodedValue().replace(" ", "").split(",")));
			
			propSetDef.setApplicableClasses(applicableClasses);
			propSetDef.setApplicableTypeValue(template.getApplicableEntity().getDecodedValue()); //Wrong, should not be set this way
		}

		IfcVersion version = new IfcVersion();
		version.setSchema("IFC4x1");

		propSetDef.setIfcVersion(version);

		PropertyDefs propertyDefs = factory.createPropertySetDefPropertyDefs();
		for (IfcPropertyTemplate.Ifc4 propTemp : template.getHasPropertyTemplates()) {

			if (propTemp instanceof IfcSimplePropertyTemplate.Ifc4) {
				IfcSimplePropertyTemplate.Ifc4 simpleProp = (IfcSimplePropertyTemplate.Ifc4) propTemp;

				PropertyDef propDef = factory.createPropertyDef();
				
				propDef.setIfdguid(simpleProp.getGlobalId().getDecodedValue());

				propDef.getNameOrValueDefOrDefinition().add(new JAXBElement<String>(new QName("Name"), String.class,
						simpleProp.getName().getDecodedValue()));

				propDef.getNameOrValueDefOrDefinition().add(new JAXBElement<String>(new QName("Definition"),
						String.class, simpleProp.getDescription().getDecodedValue()));
				
				UnitType unitType = null;
				if(simpleProp.getPrimaryUnit() instanceof IfcMonetaryUnit.Ifc4) {
					unitType = new UnitType();
					unitType.setCurrencytype(((IfcMonetaryUnit.Ifc4)simpleProp.getPrimaryUnit()).getCurrency().getDecodedValue());
				}
				if(simpleProp.getPrimaryUnit() instanceof IfcSIUnit.Ifc4) {
					unitType = new UnitType();
					unitType.setType(((IfcSIUnit.Ifc4)simpleProp.getPrimaryUnit()).getUnitType().getValue().name());
				}
				if(simpleProp.getPrimaryUnit() instanceof IfcConversionBasedUnit) {
					unitType = new UnitType();
					unitType.setType(((IfcConversionBasedUnit)simpleProp.getPrimaryUnit()).getUnitType().getValue().name());
				}
				
				PropertyType propType = factory.createPropertyType();
				DataType dataType = new DataType();

				switch (simpleProp.getTemplateType().getValue().name()) {
				case "P_SINGLEVALUE":
					TypePropertySingleValue singleValue = new TypePropertySingleValue();
					dataType.setType(container.getPSDType(simpleProp.getGlobalId().getDecodedValue()));
					singleValue.setDataType(dataType);
					singleValue.setUnitType(unitType);
					propType.setTypePropertySingleValue(singleValue);
					break;
				case "P_ENUMERATEDVALUE":
					TypePropertyEnumeratedValue enumeratedValue = new TypePropertyEnumeratedValue();
					propType.setTypePropertyEnumeratedValue(enumeratedValue);
					
					IfcPropertyEnumeration.Ifc4 ifcEnumeration = simpleProp.getEnumerators();
					
					if(ifcEnumeration != null) {
						EnumList enumList = new EnumList();
						enumList.setName(ifcEnumeration.getName().getDecodedValue());
						
						for(IfcValue.Ifc4 enumVal : ifcEnumeration.getEnumerationValues()) {
							if(enumVal instanceof IfcLabel.Ifc4) {
								enumList.getEnumItem().add(((IfcLabel.Ifc4) enumVal).getDecodedValue());
							}
						}
						enumeratedValue.setEnumList(enumList);
					}
					
					break;
				case "P_BOUNDEDVALUE":
					TypePropertyBoundedValue boundedValue = new TypePropertyBoundedValue();
					dataType.setType(container.getPSDType(simpleProp.getGlobalId().getDecodedValue()));
					
					ValueRangeDef rangeDef = new ValueRangeDef();
					
					LowerBoundValue lowerBoundValue = new LowerBoundValue();
					lowerBoundValue.setValue(simpleProp.getPrimaryMeasureType().getValue());
					rangeDef.setLowerBoundValue(lowerBoundValue);
					
					UpperBoundValue upperBoundValue = new UpperBoundValue();
					upperBoundValue.setValue(simpleProp.getSecondaryMeasureType().getValue());
					rangeDef.setUpperBoundValue(upperBoundValue);
					
					boundedValue.setValueRangeDef(rangeDef);
					
					boundedValue.setDataType(dataType);
					boundedValue.setUnitType(unitType);
					propType.setTypePropertyBoundedValue(boundedValue);
					break;
				case "P_LISTVALUE":
					TypePropertyListValue listValue = new TypePropertyListValue();
					
					ListValue listValue2 = new ListValue();
					dataType.setType(container.getPSDType(simpleProp.getGlobalId().getDecodedValue()));
					listValue2.setDataType(dataType);
					listValue2.setUnitType(unitType);
					listValue.setListValue(listValue2);
					
					propType.setTypePropertyListValue(listValue);
					break;
				case "P_TABLEVALUE":
					TypePropertyTableValue tableValue = new TypePropertyTableValue();
					dataType.setType(container.getPSDType(simpleProp.getGlobalId().getDecodedValue()));
					propType.setTypePropertyTableValue(tableValue);
					break;
				case "P_REFERENCEVALUE":
					TypePropertyReferenceValue referenceValue = new TypePropertyReferenceValue();
					//dataType.setType(container.getPSDType(simpleProp.getGlobalId().getDecodedValue()));
					//referenceValue.setDataType(dataType);
					referenceValue.setReftype(container.getPSDType(simpleProp.getGlobalId().getDecodedValue()));
					propType.setTypePropertyReferenceValue(referenceValue);
					break;
				default:
					TypePropertySingleValue singleValue2 = new TypePropertySingleValue();
					dataType.setType(container.getPSDType(simpleProp.getGlobalId().getDecodedValue()));
					singleValue2.setDataType(dataType);
					singleValue2.setUnitType(unitType);
					propType.setTypePropertySingleValue(singleValue2);
					break;
				}

				propDef.getNameOrValueDefOrDefinition()
						.add(new JAXBElement<PropertyType>(new QName("PropertyType"), PropertyType.class, propType));

				NameAliases nAliases = new NameAliases();
				NameAlias nAlias = new NameAlias();
				nAlias.setLang("de");
				nAlias.setValue(simpleProp.getName().getDecodedValue());
				nAliases.getNameAlias().add(nAlias);
				propDef.getNameOrValueDefOrDefinition()
						.add(new JAXBElement<NameAliases>(new QName("NameAliases"), NameAliases.class, nAliases));

				DefinitionAliases dAliases = new DefinitionAliases();
				DefinitionAlias dAlias = new DefinitionAlias();
				dAlias.setLang("de");
				dAlias.setValue(simpleProp.getDescription().getDecodedValue());
				dAliases.getDefinitionAlias().add(dAlias);
				propDef.getNameOrValueDefOrDefinition().add(new JAXBElement<DefinitionAliases>(
						new QName("DefinitionAliases"), DefinitionAliases.class, dAliases));

				propertyDefs.getPropertyDef().add(propDef);
			}

		}
		propSetDef.setPropertyDefs(propertyDefs);

		
		String path = folderPath + "//" + template.getName().getDecodedValue() + ".xml";
		
//		try {
//			marshallPSD(path, propSetDef);
//		} catch (JAXBException e) {
//			e.printStackTrace();
//		}
		
		new PSDExporter().saveToFile(path, propSetDef);
	}

	public ArrayList<ItemContainer<IfcPropertySetTemplate.Ifc4>> loadFromPSD(String file,
			ItemContainer<IfcProjectLibrary.Ifc4> libCon) {
		return loadFromPSD(file, libCon, languageHint);
	}

	public ArrayList<ItemContainer<IfcPropertySetTemplate.Ifc4>> loadFromPSD(String file,
			ItemContainer<IfcProjectLibrary.Ifc4> libCon, String languageHint) {
		PropertySetDef[] psds = null;
		try {
			psds = unmarshallPSD(new File(file));
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		IfcModel templateModel = ApplicationUtilities.template;
		HashMap<String, IfcPropertyEnumeration.Ifc4> enumDuplicateFilterMap = new HashMap<String, IfcPropertyEnumeration.Ifc4>();
		
		ArrayList<ItemContainer<IfcPropertySetTemplate.Ifc4>> containers = new ArrayList<>();
		for(PropertySetDef psd : psds) {
			if (psd != null && templateModel.containsObject(libCon.getItem())) {
				IfcPropertySetTemplate.Ifc4 item = new IfcPropertySetTemplate.Ifc4.Instance();
				item.setName(new IfcLabel.Ifc4(psd.getName(), true));
				item.setDescription(new IfcText.Ifc4(psd.getDefinition(), true));
		
				//Set do undefined, this information will not be exported to PSD!
				String classes = "IfcObjectDefinition";
				if(psd.getApplicableClasses() != null) {					
					classes = Arrays.toString(psd.getApplicableClasses().getClassName().toArray()).replace("[", "").replace("]", "");
				}
				item.setApplicableEntity(new IfcIdentifier.Ifc4(classes, true));
				item.setTemplateType(new IfcPropertySetTemplateTypeEnum.Ifc4(IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.NOTDEFINED));
				
				
				item.setGlobalId(new IfcGloballyUniqueId.Ifc4(psd.getIfdguid(), true));

				HashMap<String, String> psdPropertieTypeMap = new HashMap<>();
				SET<IfcPropertyTemplate> propertyTemplates = new SET<>();
				for (PropertyDef def : psd.getPropertyDefs().getPropertyDef()) {

					IfcSimplePropertyTemplate propertyTemplate = new IfcSimplePropertyTemplate.Ifc4.Instance();
					propertyTemplate.setGlobalId(new IfcGloballyUniqueId.Ifc4(def.getIfdguid(), true));

					for (JAXBElement<?> element : def.getNameOrValueDefOrDefinition()) {
						
						Object obj = element.getValue();
						
						if(element.getName().toString().toLowerCase().equals("name")) {
							propertyTemplate.setName(new IfcLabel.Ifc4((String)obj, true));
						}
						
						if(element.getName().toString().toLowerCase().equals("definition")) {
							propertyTemplate.setDescription(new IfcText.Ifc4((String)obj, true));
						}

						//replace with alias if language hint is found
						if (obj instanceof NameAliases) {
							NameAliases nAliases = (NameAliases) obj;

							for (NameAlias alias : nAliases.getNameAlias()) {
								if (alias.getLang().equals(languageHint)) {
									propertyTemplate.setName(new IfcLabel.Ifc4(alias.getValue(), true));
								}
							}

						}
						
						//replace with alias if language hint is found
						if (obj instanceof DefinitionAliases) {
							DefinitionAliases dAliases = (DefinitionAliases) obj;

							for (DefinitionAlias alias : dAliases.getDefinitionAlias()) {
								if (alias.getLang().equals(languageHint)) {
									propertyTemplate.setDescription(new IfcText.Ifc4(alias.getValue(), true));
								}
							}

						}
						

						if (obj instanceof PropertyType) {
							PropertyType pType = (PropertyType) obj;
							
							if (pType.getTypeComplexProperty() != null) {
								propertyTemplate.setTemplateType(new IfcSimplePropertyTemplateTypeEnum.Ifc4(
										IfcSimplePropertyTemplateTypeEnum.Ifc4.IfcSimplePropertyTemplateTypeEnum_internal.P_SINGLEVALUE));

								//TODO handle content of getTypeComplexProperty
							}

							if (pType.getTypePropertyBoundedValue() != null) {
								propertyTemplate.setTemplateType(new IfcSimplePropertyTemplateTypeEnum.Ifc4(
										IfcSimplePropertyTemplateTypeEnum.Ifc4.IfcSimplePropertyTemplateTypeEnum_internal.P_BOUNDEDVALUE));
								psdPropertieTypeMap.put(def.getIfdguid(), pType.getTypePropertyBoundedValue().getDataType().getType());
								
								if(pType.getTypePropertyBoundedValue().getUnitType() != null) {
									String typeValue = pType.getTypePropertyBoundedValue().getUnitType().getType();
									String currencyValue = pType.getTypePropertyBoundedValue().getUnitType().getCurrencytype();
									if(typeValue != null) {
										IfcSIUnit siUnit = new IfcSIUnit.Ifc4.Instance();
										siUnit.setUnitType(new IfcUnitEnum.Ifc4(IfcUnitEnum.Ifc4.IfcUnitEnum_internal.valueOf(typeValue)));
										propertyTemplate.setPrimaryUnit((IfcUnit)siUnit);
										templateModel.addObject(siUnit);
									}else {
										if(currencyValue != null) {
											IfcMonetaryUnit monUnit = new IfcMonetaryUnit.Ifc4.Instance(new IfcLabel.Ifc4(currencyValue));
											propertyTemplate.setPrimaryUnit((IfcUnit)monUnit);
											templateModel.addObject(monUnit);
										}
									}
								}
								
								propertyTemplate.setPrimaryMeasureType(new IfcLabel.Ifc4("IfcLabel")); //Default Value
								propertyTemplate.setSecondaryMeasureType(new IfcLabel.Ifc4("IfcLabel")); //Default Value
								
								/*
								if(pType.getTypePropertyBoundedValue().getValueRangeDef() != null) {
									propertyTemplate.setPrimaryMeasureType(new IfcLabel.Ifc4(
											pType.getTypePropertyBoundedValue().getValueRangeDef().getLowerBoundValue().getValue()
									));
									
									propertyTemplate.setSecondaryMeasureType(new IfcLabel.Ifc4(
											pType.getTypePropertyBoundedValue().getValueRangeDef().getUpperBoundValue().getValue()
									));
								}*/
								
							}

							if (pType.getTypePropertyEnumeratedValue() != null) {
								propertyTemplate.setTemplateType(new IfcSimplePropertyTemplateTypeEnum.Ifc4(
										IfcSimplePropertyTemplateTypeEnum.Ifc4.IfcSimplePropertyTemplateTypeEnum_internal.P_ENUMERATEDVALUE));
								
								//psdPropertieTypeMap.put(def.getIfdguid(), "IfcLabel"); //Enumeration is always type IfcLabel (in our case)
								
								
								EnumList enumList = pType.getTypePropertyEnumeratedValue().getEnumList();
								if(enumList != null) {
									String key = enumList.getName() + "_" + enumList.getEnumItem().toString();
									
									if(enumDuplicateFilterMap.get(key) == null) {
										if(enumList != null) {
											LIST<IfcValue> enumValues = new LIST<IfcValue>();
											for(String enumListVal : enumList.getEnumItem()) {
												enumValues.add(new IfcLabel.Ifc4(enumListVal, true));
											}
											
											IfcPropertyEnumeration.Ifc4 ifcPropEnum = new IfcPropertyEnumeration.Ifc4.Instance(
													new IfcLabel.Ifc4(enumList.getName(), true), enumValues, null
											);
											templateModel.addObject(ifcPropEnum);

											propertyTemplate.setEnumerators(ifcPropEnum);
											enumDuplicateFilterMap.put(key, ifcPropEnum);
										}
									}else {
										propertyTemplate.setEnumerators(enumDuplicateFilterMap.get(key));
									}
								}
								
								propertyTemplate.setPrimaryMeasureType(new IfcLabel.Ifc4("IfcLabel")); //Default Value
								
							}

							if (pType.getTypePropertyListValue() != null) {
								propertyTemplate.setTemplateType(new IfcSimplePropertyTemplateTypeEnum.Ifc4(
										IfcSimplePropertyTemplateTypeEnum.Ifc4.IfcSimplePropertyTemplateTypeEnum_internal.P_LISTVALUE));
								
								if(pType.getTypePropertyListValue().getListValue() != null) {
									psdPropertieTypeMap.put(def.getIfdguid(), pType.getTypePropertyListValue().getListValue().getDataType().getType());
									
									if(pType.getTypePropertyListValue().getListValue().getUnitType() != null) {
										String typeValue = pType.getTypePropertyListValue().getListValue().getUnitType().getType();
										String currencyValue = pType.getTypePropertyListValue().getListValue().getUnitType().getCurrencytype();
										if(typeValue != null) {
											IfcSIUnit siUnit = new IfcSIUnit.Ifc4.Instance();
											siUnit.setUnitType(new IfcUnitEnum.Ifc4(IfcUnitEnum.Ifc4.IfcUnitEnum_internal.valueOf(typeValue)));
											propertyTemplate.setPrimaryUnit((IfcUnit)siUnit);
											templateModel.addObject(siUnit);
										}else {
											if(currencyValue != null) {
												IfcMonetaryUnit monUnit = new IfcMonetaryUnit.Ifc4.Instance(new IfcLabel.Ifc4(currencyValue));
												propertyTemplate.setPrimaryUnit((IfcUnit)monUnit);
												templateModel.addObject(monUnit);
											}
										}
									}
								}
								

								propertyTemplate.setPrimaryMeasureType(new IfcLabel.Ifc4("IfcLabel"));
							}

							if (pType.getTypePropertyReferenceValue() != null) {
								propertyTemplate.setTemplateType(new IfcSimplePropertyTemplateTypeEnum.Ifc4(
										IfcSimplePropertyTemplateTypeEnum.Ifc4.IfcSimplePropertyTemplateTypeEnum_internal.P_REFERENCEVALUE));
								psdPropertieTypeMap.put(def.getIfdguid(), pType.getTypePropertyReferenceValue().getReftype());
								
								propertyTemplate.setPrimaryMeasureType(new IfcLabel.Ifc4(pType.getTypePropertyReferenceValue().getReftype()));
							}

							if (pType.getTypePropertySingleValue() != null) {
								propertyTemplate.setTemplateType(new IfcSimplePropertyTemplateTypeEnum.Ifc4(
										IfcSimplePropertyTemplateTypeEnum.Ifc4.IfcSimplePropertyTemplateTypeEnum_internal.P_SINGLEVALUE));
								psdPropertieTypeMap.put(def.getIfdguid(), pType.getTypePropertySingleValue().getDataType().getType());
								
								
								if(pType.getTypePropertySingleValue().getUnitType() != null) {
									String typeValue = pType.getTypePropertySingleValue().getUnitType().getType();
									String currencyValue = pType.getTypePropertySingleValue().getUnitType().getCurrencytype();
									if(typeValue != null) {
										IfcSIUnit siUnit = new IfcSIUnit.Ifc4.Instance();
										siUnit.setUnitType(new IfcUnitEnum.Ifc4(IfcUnitEnum.Ifc4.IfcUnitEnum_internal.valueOf(typeValue)));
										propertyTemplate.setPrimaryUnit((IfcUnit)siUnit);
										templateModel.addObject(siUnit);
									}else {
										if(currencyValue != null) {
											IfcMonetaryUnit monUnit = new IfcMonetaryUnit.Ifc4.Instance(new IfcLabel.Ifc4(currencyValue));
											propertyTemplate.setPrimaryUnit((IfcUnit)monUnit);
											templateModel.addObject(monUnit);
										}
									}
								}
								
								propertyTemplate.setPrimaryMeasureType(new IfcLabel.Ifc4(pType.getTypePropertySingleValue().getDataType().getType()));

							}

							if (pType.getTypePropertyTableValue() != null) {
								propertyTemplate.setTemplateType(new IfcSimplePropertyTemplateTypeEnum.Ifc4(
										IfcSimplePropertyTemplateTypeEnum.Ifc4.IfcSimplePropertyTemplateTypeEnum_internal.P_TABLEVALUE));


								propertyTemplate.setPrimaryMeasureType(new IfcLabel.Ifc4("IfcLabel"));
								propertyTemplate.setSecondaryMeasureType(new IfcLabel.Ifc4("IfcLabel"));
								
								//TODO handle content of getTypePropertyTableValue
							}
						}
					}
					propertyTemplates.add((IfcPropertyTemplate)propertyTemplate);
					templateModel.addObject(propertyTemplate);
				}

				item.setHasPropertyTemplates(propertyTemplates);
				templateModel.addObject(item);
				
				SET<IfcDefinitionSelect.Ifc4> set = new SET<>();
				set.add(item);

				IfcRelDeclares.Ifc4 declares = new IfcRelDeclares.Ifc4.Instance();
				declares.setGlobalId(new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true));
				declares.setOwnerHistory(ApplicationUtilities.template.getIfcProject().getOwnerHistory());
				declares.setName(new IfcLabel.Ifc4("Library Relation of " + libCon.getDisplay(), true));
				declares.setRelatedDefinitions(set);
				declares.setRelatingContext((IfcProjectLibrary.Ifc4) libCon.getItem());
				templateModel.addObject(declares);

				ItemContainer<IfcPropertySetTemplate.Ifc4> container = new ItemContainer<IfcPropertySetTemplate.Ifc4>(
						item,
						item.getName().toString() + " - (#" + item.getStepLineNumber() + ")"
				);
				container.setPsdTypeMap(psdPropertieTypeMap);
				
				containers.add(container);
			}
		}
	
		return containers;
	}

	private void marshallPSD(File exportTo, PropertySetDef propSetDef) throws JAXBException {

		JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);

		Marshaller jaxbMarshaller = context.createMarshaller();

		jaxbMarshaller.marshal(propSetDef, exportTo);
	}

	private PropertySetDef[] unmarshallPSD(File file) throws JAXBException {

		JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);

		Unmarshaller jaxbUnmarshaller = context.createUnmarshaller();

		Object obj = jaxbUnmarshaller.unmarshal(file);
		
		PropertySetDef[] propSetDef = null;
		if(obj instanceof PropertySetDef) {
			propSetDef = new PropertySetDef[1];
			propSetDef[0] = (PropertySetDef)obj;
		}
		
		//TODO import multiple roots elements  
		//if(obj instanceof PropertySetDef[]) {
		//	propSetDef = (PropertySetDef[])obj;
		//}

		return propSetDef;
	}

}

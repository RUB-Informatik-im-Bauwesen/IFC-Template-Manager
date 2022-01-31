package utils;

import org.buildingsmart_tech.mvd.xml._1.AttributeRule;
import org.buildingsmart_tech.mvd.xml._1.ConceptTemplate;

import com.apstex.ifctoolbox.ifc.IfcSimplePropertyTemplate;

import components.templating.ItemContainer;
import factory.TemplateFactory;


public class MvdUtilities {

	public static ConceptTemplate createConceptTemplate(
			String name, String schema, String applicableEntity) {
		ConceptTemplate conceptTemplate = TemplateFactory.createConceptTemplate(
				name, 
				schema, 
				applicableEntity
				);
		
		AttributeRule relatingPropertyDefinitionRule = 
				TemplateFactory.createRelatingPropertyDefinitionRuleTree(
						conceptTemplate.getRules());
		AttributeRule hasPropertiesRule = TemplateFactory.defaultPropertySetRuleTree(
				relatingPropertyDefinitionRule);
		
		TemplateFactory.assignPropertyRequirement(hasPropertiesRule, 
				"IfcPropertySingleValue", "Name", "PropName", 
				"NominalValue", "PropNominalValue", "IfcIdentifier");
		TemplateFactory.assignPropertyRequirement(hasPropertiesRule, 
				"IfcPropertyEnumeratedValue", "Name", "EnumPropName", 
				"EnumerationValues", "EnumPropValue", null);
		TemplateFactory.assignPropertyRequirement(hasPropertiesRule, 
				"IfcPropertyListValue", "Name", "ListPropName", 
				"ListValues", "ListPropValue", null);
		
		return conceptTemplate;
	}
	
	public static String createTemplateRuleParameters(
			ItemContainer<?> container,
			IfcSimplePropertyTemplate.Ifc4 ifcSimplePropertyTemplate) {

		String parameters = null;

		String templateType = ifcSimplePropertyTemplate.getTemplateType().getValue().toString();

		switch (templateType) {

		case "P_SINGLEVALUE":

			parameters = "PropName[Value]='" + ifcSimplePropertyTemplate.getName() + "'";

			parameters += " AND PropNominalValue[Exists]=TRUE";

			parameters += " AND PropNominalValue[Type]='"
					+ container.getPSDType(ifcSimplePropertyTemplate.getGlobalId().getDecodedValue()) + "'";
//				container.getPSDType(ifcSimplePropertyTemplate.getGlobalId().getDecodedValue());

			break;

		case "P_ENUMERATEDVALUE":

			parameters = "EnumPropName[Value]='" + ifcSimplePropertyTemplate.getName() + "'";

			parameters += " AND EnumPropValue[Exists]=TRUE";

//			  parameters+=" AND EnumPropValue[Type]='"+
//						container.getPSDType(ifcSimplePropertyTemplate.getGlobalId().getDecodedValue())+"'";
//		
			break;

		case "P_LISTVALUE":

			parameters = "ListPropName[Value]='" + ifcSimplePropertyTemplate.getName() + "'";

			parameters += " AND ListPropValue[Exists]=TRUE";

			parameters += " AND ListPropValue[Size]>0";

//			  parameters+=" AND EnumPropValue[Type]='"+
//						container.getPSDType(ifcSimplePropertyTemplate.getGlobalId().getDecodedValue())+"'";
//		
			break;

		// TODO implementation for other value types

		default:
			break;
		}

		return parameters;
	}
	
}

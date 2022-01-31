package components.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.buildingsmart_tech.mvd.xml._1.Applicability;
import org.buildingsmart_tech.mvd.xml._1.Concept;
import org.buildingsmart_tech.mvd.xml._1.ConceptRoot;
import org.buildingsmart_tech.mvd.xml._1.ConceptTemplate;
import org.buildingsmart_tech.mvd.xml._1.ModelView;
import org.buildingsmart_tech.mvd.xml._1.MvdXML;
import org.buildingsmart_tech.mvd.xml._1.ModelView.ExchangeRequirements.ExchangeRequirement;
import org.buildingsmart_tech.mvd.xml._1.MvdXML.Templates;
import org.buildingsmart_tech.mvd.xml._1.MvdXML.Views;
import org.buildingsmart_tech.mvd.xml._1.Requirements.Requirement;
import org.buildingsmart_tech.mvd.xml._1.TemplateRules;
import org.buildingsmart_tech.mvd.xml._1.TemplateRules.TemplateRule;

import com.apstex.ifctoolbox.ifc.IfcProjectLibrary;
import com.apstex.ifctoolbox.ifc.IfcPropertySetTemplate;
import factory.ModelViewFactory;
import io.MvdExporter;

import com.apstex.ifctoolbox.ifc.IfcPropertyTemplate;
import com.apstex.ifctoolbox.ifc.IfcSimplePropertyTemplate;
import com.apstex.ifctoolbox.ifc.IfcValue;
import com.apstex.step.core.STRING;

import components.templating.ItemContainer;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import utils.MvdUtilities;

/**
 * 
 * An action to perform export of selected elements in the TemplateTree to MVD.
 * 
 * @author Marcel Stepien
 *
 */
public class ExportMvdAction implements ActionListener {
	
	private TreeItem<ItemContainer<?>> treeItem;
	
	private ItemContainer<?> container;
	
	public ExportMvdAction(TreeItem<ItemContainer<?>> treeItem){
		
		this.treeItem = treeItem;
		container = treeItem.getValue();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		CountDownLatch doneLatch = new CountDownLatch(1);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("MVD Export");
				fileChooser.getExtensionFilters().addAll(new ExtensionFilter("MVD", "*.mvdXML"));

				File selectedFile = fileChooser.showSaveDialog(null);
				
				MvdXML mvdXML = new MvdXML();
				mvdXML.setUuid(UUID.randomUUID().toString());
				Templates templates = new Templates();
				mvdXML.setTemplates(templates);
				
				ArrayList<ItemContainer<?>> propertySetTemplateList = new ArrayList<ItemContainer<?>>();
	
				String conceptTemplateName = "";
				String modelViewName = "";
				String exchangeRequirementName = "";
				
				Set<String> applicableEntities = new TreeSet<String>();
				
				if (container.getItem() instanceof IfcPropertySetTemplate.Ifc4) {
					
					IfcPropertySetTemplate.Ifc4 propertySetTemplate = (IfcPropertySetTemplate.Ifc4) container.getItem();
					
					conceptTemplateName = "Concept Template for "+propertySetTemplate.getName().getDecodedValue();
					modelViewName = "Model View for "+propertySetTemplate.getName().getDecodedValue();
					exchangeRequirementName = "Exchange Requirement for "+propertySetTemplate.getName().getDecodedValue();

					applicableEntities.add(propertySetTemplate.getApplicableEntity().getDecodedValue());
					propertySetTemplateList.add(container);
					
				} else if (container.getItem() instanceof IfcProjectLibrary.Ifc4) {
					
					IfcProjectLibrary.Ifc4 projectLibrary = (IfcProjectLibrary.Ifc4) container.getItem();
					
					conceptTemplateName = "Concept Template for "+projectLibrary.getName().getDecodedValue();
					modelViewName = "Model View for "+projectLibrary.getName().getDecodedValue();
					exchangeRequirementName = "Exchange Requirement for "+projectLibrary.getName().getDecodedValue();
		
					
					treeItem.getChildren().forEach(c -> {
						IfcPropertySetTemplate.Ifc4 propertySetTemplate = 
								(IfcPropertySetTemplate.Ifc4 ) c.getValue().getItem();
						applicableEntities.add(propertySetTemplate.getApplicableEntity().getDecodedValue());
						propertySetTemplateList.add(c.getValue());
					});
					
					
				}	//end if
				
				
				Map<String, ConceptTemplate> conceptTemlatesByApplicableEntity = new TreeMap<String, ConceptTemplate>();
				
				for (String applicableEntity: applicableEntities) {
					ConceptTemplate conceptTemplate = MvdUtilities.createConceptTemplate(
							conceptTemplateName, 
							"IFC4", applicableEntity);
					templates.getConceptTemplate().add(conceptTemplate);
					conceptTemlatesByApplicableEntity.put(applicableEntity, conceptTemplate);
				}

				Views views = new Views();
				mvdXML.setViews(views);
				
				ModelView modelView = ModelViewFactory.createModelView(
						modelViewName, 
						"IFC4");	
				views.getModelView().add(modelView);
				
				ExchangeRequirement exchangeRequirement = ModelViewFactory.createExchangeRequirement(
						"ERM1", 
						exchangeRequirementName, 
						Applicability.EXPORT);
				modelView.getExchangeRequirements().getExchangeRequirement().add(exchangeRequirement);
				
														
				//Creating Concept Root for each PropertySetTemplate
				for (ItemContainer<?> itemContainer : propertySetTemplateList) {
					
					IfcPropertySetTemplate.Ifc4 propertySetTemplateListItem = 
							(IfcPropertySetTemplate.Ifc4) itemContainer.getItem() ;
					
					ConceptRoot conceptRoot = ModelViewFactory.createConceptRoot(
							"Concept Root for "+propertySetTemplateListItem.getName().getDecodedValue(),
							propertySetTemplateListItem.getApplicableEntity().getDecodedValue(),
							conceptTemlatesByApplicableEntity.get(
									propertySetTemplateListItem.getApplicableEntity().getDecodedValue()
									));
					
					conceptRoot.getApplicability().getTemplateRules().setOperator("and");
					
					conceptRoot.getApplicability().getTemplateRules().
						getTemplateRulesOrTemplateRule().add(
							ModelViewFactory.newTemplateRule("SetName[Value]='"+propertySetTemplateListItem.getName().getDecodedValue()+"'"));
					modelView.getRoots().getConceptRoot().add(conceptRoot);
				    
					
					Concept concept = ModelViewFactory.createConcept(
							"Concept for "+propertySetTemplateListItem.getName().getDecodedValue(), 
							conceptTemlatesByApplicableEntity.get(
									propertySetTemplateListItem.getApplicableEntity().getDecodedValue()
									));
					concept.getTemplateRules().setOperator("and");
					conceptRoot.getConcepts().getConcept().add(concept);
					
					Requirement requirement = ModelViewFactory.createRequirement(
							Applicability.EXPORT, exchangeRequirement, "mandatory");
					concept.getRequirements().getRequirement().add(requirement);
					
					
					for(IfcPropertyTemplate.Ifc4 ifcPropertyTemplate : propertySetTemplateListItem.getHasPropertyTemplates()) {
						if (ifcPropertyTemplate instanceof IfcSimplePropertyTemplate) {
						  IfcSimplePropertyTemplate.Ifc4 ifcSimplePropertyTemplate =
								  (IfcSimplePropertyTemplate.Ifc4) ifcPropertyTemplate;
						  
						  
						  String parameters = MvdUtilities.createTemplateRuleParameters(
								  container, ifcSimplePropertyTemplate);
						  
						  
						  if(parameters!=null) { 
							  concept.getTemplateRules().getTemplateRulesOrTemplateRule().add(
									  ModelViewFactory.newTemplateRule(parameters));
						  }
						  
						  if(ifcSimplePropertyTemplate.getTemplateType().getValue().toString()
								  .equals("P_ENUMERATEDVALUE")){
							  
							 boolean condition = ifcSimplePropertyTemplate.getEnumerators() !=null 
									 && ifcSimplePropertyTemplate.getEnumerators().getEnumerationValues() != null
									 && ifcSimplePropertyTemplate.getEnumerators().getEnumerationValues().size()>0;
							  
							  if (condition) {
								  TemplateRules templateRulesEnumrationValues = new TemplateRules();
								  concept.getTemplateRules().getTemplateRulesOrTemplateRule()
								  		.add(templateRulesEnumrationValues);
								  
							      templateRulesEnumrationValues.setOperator("or");

								  for(IfcValue.Ifc4 enumValue 
										  : ifcSimplePropertyTemplate.getEnumerators().getEnumerationValues()) {
									  STRING enumValueSTRING = (STRING) enumValue;

									  String enumValueTemplateRuleParams = "EnumPropName[Value]='" + ifcSimplePropertyTemplate.getName() + "'";
									  enumValueTemplateRuleParams+= " AND "+ "EnumPropValue[Value]='"+enumValueSTRING.getDecodedValue()+"'";
									  
									  TemplateRule enumValueTemplateRule = new TemplateRule();
									  enumValueTemplateRule.setParameters(enumValueTemplateRuleParams);
									  templateRulesEnumrationValues.getTemplateRulesOrTemplateRule().add(enumValueTemplateRule);
									
								  } 
							  } //endif condition
 
						  } //endif P_ENUMERATEDVALUE
							
							 
						}else {
							
							//TODO ComplexPropertyTemplate
							
						}
					} //end for PropertyTemplate
					

					MvdExporter exporter  = new MvdExporter();
					String path=selectedFile.getAbsolutePath();
					if (!selectedFile.getName().toLowerCase().contains(".mvdxml"))
						path+="mvdXML";
				    exporter.saveToFile(path, mvdXML); 
				    
				    System.out.println("MVD successfully exported");
					
				} //endif null
				
				doneLatch.countDown();
			}
		});

		try {
			doneLatch.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
	}

}

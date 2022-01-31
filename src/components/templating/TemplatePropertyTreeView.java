package components.templating;

import java.util.ArrayList;

import com.apstex.ifctoolbox.ifc.IfcPropertySetTemplate;
import com.apstex.ifctoolbox.ifc.IfcSimplePropertyTemplate;
import com.apstex.ifctoolbox.ifc.IfcSimplePropertyTemplateTypeEnum;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

/**
 * Displays the content of a ItemContainer containing a IfcPropertySetTemplate.
 * 
 * @author Marcel Stepien
 *
 */
@SuppressWarnings("rawtypes")
public class TemplatePropertyTreeView extends TreeTableView{
	
	private static TemplatePropertyTreeView self;
	
	public static TemplatePropertyTreeView getInstance() {
		if(self == null) {
			self = new TemplatePropertyTreeView();
		}
		return self;
	}
	
	private TemplatePropertyTreeView() {
		TreeTableColumn<PropertyItem, String> column = new TreeTableColumn<>("Name");
	    column.setCellValueFactory(
	            (TreeTableColumn.CellDataFeatures<PropertyItem, String> param) -> 
	            new ReadOnlyStringWrapper(param.getValue().getValue().getName())
	    );
	    column.setPrefWidth(150);
	    
	    this.getColumns().add(column);
	    
	    
	    TreeTableColumn<PropertyItem, String> column2 = new TreeTableColumn<>("Value");
	    column2.setCellValueFactory(
	            (TreeTableColumn.CellDataFeatures<PropertyItem, String> param) -> 
	            new ReadOnlyStringWrapper(param.getValue().getValue().getValue().toString())
	    );
	    column2.setPrefWidth(150);
	    
	    this.getColumns().add(column2);
	    
	    
	    TreeTableColumn<PropertyItem, String> column3 = new TreeTableColumn<>("Type");
	    column3.setCellValueFactory(
	            (TreeTableColumn.CellDataFeatures<PropertyItem, String> param) -> 
	            new ReadOnlyStringWrapper(param.getValue().getValue().getPropertyType())
	    );
	    column3.setPrefWidth(100);
	    
	    this.getColumns().add(column3);
	}
	

	public TreeItem<PropertyItem> createTree(ArrayList<PropertyItem> properties){
		TreeItem<PropertyItem> top = new TreeItem<PropertyItem>(new PropertyItem("Properties", "", "Count: " + properties.size(), "", ""));
		top.setExpanded(true);
		
		for(PropertyItem prop : properties) {
			TreeItem<PropertyItem> item = new TreeItem<PropertyItem>(prop);
			top.getChildren().add(item);
			
			if(prop.getValue() instanceof ArrayList) {
				for(PropertyItem innerProp : ((ArrayList<PropertyItem>)prop.getValue())) {
					TreeItem<PropertyItem> innerItem = new TreeItem<PropertyItem>(innerProp);
					item.getChildren().add(innerItem);
				}
			}
		}
		

		return top;
	}
	
	public ArrayList<PropertyItem> createItems(ItemContainer<?> container){
		
		ArrayList<PropertyItem> items = new ArrayList<>();
		
		//if(container == null ? false : container.getItem() instanceof IfcPropertySetTemplate.Ifc4) {
		if(container.getItem() instanceof IfcPropertySetTemplate.Ifc4) {
					
			IfcPropertySetTemplate.Ifc4 pSet = (IfcPropertySetTemplate.Ifc4)container.getItem();
			
			/*
			PropertyItem<String> item1 = new PropertyItem<String>(
					"NAME", 
					"", 
					pSet.getName().toString(), 
					pSet.getName().getClassName()
			);
			
			PropertyItem<Integer> item2 = new PropertyItem<Integer>(
					"LINENUMBER", 
					"", 
					new Integer(pSet.getStepLineNumber()), 
					"Integer"
			);
			
			PropertyItem<String> item3 = new PropertyItem<String>(
					"DESCRIPTION", 
					"", 
					pSet.getDescription().toString(), 
					pSet.getDescription().getClassName()
			);
			
			PropertyItem<String> item4 = new PropertyItem<String>(
					"CLASSNAME", 
					"", 
					pSet.getClassName().toString(), 
					pSet.getClassName().getClass().getSimpleName()
			);
			
			PropertyItem<String> item5 = new PropertyItem<String>(
					"GLOBALID", 
					"", 
					pSet.getGlobalId().toString(), 
					pSet.getGlobalId().getClassName()
			);
			

			PropertyItem<String> item6 = new PropertyItem<String>(
					"TEMPLATE TYPE", 
					"", 
					pSet.getTemplateType().toString(), 
					pSet.getTemplateType().getClassName()
			);
			*/
			
			ArrayList<PropertyItem> properties = new ArrayList<>();
			for(Object sP : pSet.getHasPropertyTemplates()) {
				if(sP instanceof IfcSimplePropertyTemplate) {
					IfcSimplePropertyTemplate propertie = (IfcSimplePropertyTemplate)sP;
					
					String className = propertie.getTemplateType().getClassName();
					if(className.equals(IfcSimplePropertyTemplateTypeEnum.class.getSimpleName())) {
						className = propertie.getTemplateType().getValue().name();
					}
					
					PropertyItem<String> propItem = new PropertyItem<String>(
							propertie.getName().getDecodedValue(), 
							"", 
							propertie.getDescription().toString(), 
							className,
							container.getPSDType(propertie.getGlobalId().getDecodedValue())
					);
					//properties.add(propItem);
					items.add(propItem);
				}				
			}
			
			/*
			PropertyItem<ArrayList> item7 = new PropertyItem<ArrayList>(
					"PROPERTIES", 
					"", 
					properties, 
					properties.getClass().getSimpleName(),
					""
			);*/
			
			//items.add(item1);
			//items.add(item2);
			//items.add(item3);
			//items.add(item4);
			//items.add(item5);
			//items.add(item6);
			//items.add(item7);
			
		}
		
		return items;
		
	}
	
}

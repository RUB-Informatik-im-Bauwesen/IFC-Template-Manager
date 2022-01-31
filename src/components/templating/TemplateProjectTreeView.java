package components.templating;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;

import com.apstex.ifctoolbox.ifc.IfcDefinitionSelect;
import com.apstex.ifctoolbox.ifc.IfcLibraryReference;
import com.apstex.ifctoolbox.ifc.IfcProject;
import com.apstex.ifctoolbox.ifc.IfcProjectLibrary;
import com.apstex.ifctoolbox.ifc.IfcPropertySetTemplate;
import com.apstex.ifctoolbox.ifc.IfcPropertyTemplate;
import com.apstex.ifctoolbox.ifc.IfcRelAssociatesLibrary;
import com.apstex.ifctoolbox.ifc.IfcRelDeclares;
import com.apstex.ifctoolbox.ifc.IfcRoot;
import com.apstex.ifctoolbox.ifc.IfcSimplePropertyTemplate;
import com.apstex.ifctoolbox.ifcmodel.IfcModel;
import com.apstex.step.core.ClassInterface;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;
import utils.IfcUtilities;

/**
 * Build and display the Template-Editor-Tree.
 * 
 * @author Marcel Stepien
 *
 */
@SuppressWarnings("rawtypes")
public class TemplateProjectTreeView extends TreeTableView {
	
	private HashMap<String, TreeItem<ItemContainer>> libByGuidMap;
	private HashMap<String, ItemContainer> propByGuidMap;
	
	private static TemplateProjectTreeView self;
	
	public static TemplateProjectTreeView getInstance() {
		if(self == null) {
			self = new TemplateProjectTreeView();
		}
		return self;
	}
	
	private TemplateProjectTreeView() {
		TreeTableColumn<ItemContainer, String> column = new TreeTableColumn<>("Project Explorer");
	    
	    column.setCellValueFactory(
	            (TreeTableColumn.CellDataFeatures<ItemContainer, String> param) -> 
	            new ReadOnlyStringWrapper(param.getValue().getValue().getDisplay())
	    );
	    column.setPrefWidth(235);
	    this.getColumns().add(column);
	    
	    
	    TreeTableColumn<ItemContainer, String> ifcTypeColumn = new TreeTableColumn<>("IFC Type");
	    
	    ifcTypeColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<ItemContainer,String>, ObservableValue<String>>() {
			
			@Override
			public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<ItemContainer, String> param) {
				
				if(param.getValue().getValue().getItem() instanceof IfcRoot) {
					return new ReadOnlyStringWrapper(((IfcRoot)param.getValue().getValue().getItem()).getClassName());
				}
				   
				return new ReadOnlyStringWrapper(param.getValue().getValue().getItem().getClass().getSimpleName());
			}
		});
	    ifcTypeColumn.setPrefWidth(165);
	    
	    this.getColumns().add(ifcTypeColumn);
	}
	
	/**
	 * Initialize new content for the editor tree.
	 * 
	 * @param model - stepfile containing ifc template information
	 */
	public TreeItem<ItemContainer> createTemplate(IfcModel model) {
		String value = "Workplace";

		libByGuidMap = new HashMap<>();
		propByGuidMap = new HashMap<>();
		
		TreeItem<ItemContainer> top = new TreeItem<ItemContainer>(new ItemContainer<String>(value, value));
		top.setExpanded(true);
		
		IfcProject.Ifc4 pro = (IfcProject.Ifc4) model.getIfcProject();
		if(pro != null) {
			ItemContainer<IfcProject.Ifc4> libItem = new ItemContainer<IfcProject.Ifc4>(pro,
					pro.getName().toString() + " - (#" + pro.getStepLineNumber() + ")");

			TreeItem<ItemContainer> subNode = new TreeItem<ItemContainer>(libItem);
			subNode.setExpanded(true);
			top.getChildren().add(subNode);
			
			//Create and add catalog to tree
			HashMap<String, ArrayList<IfcPropertySetTemplate.Ifc4>> map = new HashMap<>();
			HashMap<String, Object> libMap = new HashMap<>();
			
			createCatalogByLib(map, libMap, model.getCollection(IfcProjectLibrary.Ifc4.class));
			createCatalogByAssociation(map, libMap, model.getCollection(IfcRelAssociatesLibrary.Ifc4.class));
			
			appendToTree(
					subNode, 
					map,
					libMap
			);
			
		}else {
			System.err.println("Project is empty. Create a new one!");
		}
		
		return top;
	}

	/**
	 * Append a created hashmap of templates mapped by library names to the editor tree.
	 * 
	 * @param assoLib
	 * @return
	 */
	private void appendToTree(
			TreeItem<ItemContainer> parent,
			HashMap<String, ArrayList<IfcPropertySetTemplate.Ifc4>> assoMap,
			HashMap<String, Object> libMap) {

		// Sort parent keys
		String[] assoMapKeys = new String[assoMap.keySet().size()];
		assoMap.keySet().toArray(assoMapKeys);
		Arrays.sort(assoMapKeys, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});

		for (String key : assoMapKeys) {

			Object library = libMap.get(key);
			ItemContainer<Object> libItem = new ItemContainer<Object>(libMap.get(key), key);

			TreeItem<ItemContainer> subSubNode = new TreeItem<ItemContainer>(libItem);
			parent.getChildren().add(subSubNode);

			if(library instanceof IfcProjectLibrary) {				
				libByGuidMap.put(((IfcProjectLibrary)library).getGlobalId().getDecodedValue(), subSubNode);
			}
			
			if(library instanceof IfcRelAssociatesLibrary) {				
				libByGuidMap.put(((IfcRelAssociatesLibrary)library).getGlobalId().getDecodedValue(), subSubNode);
			}
			
			HashMap<String, ItemContainer> map = new HashMap<>();
			for (IfcPropertySetTemplate.Ifc4 temp : assoMap.get(key)) {

				// get information for tree
				if (temp != null) {
					ItemContainer<IfcPropertySetTemplate.Ifc4> item = new ItemContainer<IfcPropertySetTemplate.Ifc4>(
							temp,
							temp.getName().toString() + " - (#" + temp.getStepLineNumber() + ")");
					System.out.println(temp.getName().toString() + " - (#" + temp.getStepLineNumber() + ")");
					for(IfcPropertyTemplate prop : temp.getHasPropertyTemplates()) {
						if(prop instanceof IfcSimplePropertyTemplate) {
							
							String type = "IfcLabel";
							if(((IfcSimplePropertyTemplate) prop).getPrimaryMeasureType() != null) {
								type = ((IfcSimplePropertyTemplate) prop).getPrimaryMeasureType().getDecodedValue();
							}
							
							item.addPSDType(
									((IfcSimplePropertyTemplate) prop).getGlobalId().getDecodedValue(), 
									type
							);
						}
						
					}

					propByGuidMap.put(temp.getGlobalId().getDecodedValue(), item);
					
					map.put(item.getDisplay(), item);
				}
			}

			// Sort child keys
			String[] children = new String[map.keySet().size()];
			map.keySet().toArray(children);
			Arrays.sort(children, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});

			// add items to tree
			for (String cKey : children) {
				TreeItem<ItemContainer> child = new TreeItem<ItemContainer>(map.get(cKey));
				subSubNode.getChildren().add(child);
			}

		}

	}
	
	/**
	 * Read a collection of IfcProjectLibrary objects and builds a 
	 * hashmap of templates mapped to regarded library. 
	 * 
	 * @param assoLib
	 * @return
	 */
	private void createCatalogByLib(
			HashMap<String, ArrayList<IfcPropertySetTemplate.Ifc4>> assoMap,
			HashMap<String, Object> libMap,
			Collection<IfcProjectLibrary.Ifc4> proLib
		) {
		
		//Project Library
		if (proLib != null) {
			for (IfcProjectLibrary.Ifc4 pro : proLib) {
				
				String name = ""; // EMPTY NAME
				if (pro.getName() != null) {
					name = pro.getName().toString();
				}
				
				ArrayList<IfcPropertySetTemplate.Ifc4> assoLibs = assoMap.get(name);
				if (assoLibs == null) {
					assoLibs = new ArrayList<>();
				}

				if(pro.getDeclares_Inverse() != null) {
					for(IfcRelDeclares.Ifc4 rel : pro.getDeclares_Inverse()) {

						for (IfcDefinitionSelect.Ifc4 temp : rel.getRelatedDefinitions()) {
							
							if (temp == null || IfcUtilities.isTemporaryRemoved((ClassInterface)temp)) {
								continue;
							}

							if(temp instanceof IfcPropertySetTemplate.Ifc4) {
								assoLibs.add((IfcPropertySetTemplate.Ifc4)temp);
							}

						}
					}
				}
				assoMap.put(name, assoLibs);
				libMap.put(name, pro);
			}
		}
		
	}

	/**
	 * Read a collection of IfcRelAssociatesLibrary objects and builds a 
	 * hashmap of templates mapped to regarded library. 
	 * 
	 * @param assoLib
	 * @return
	 */
	private void createCatalogByAssociation(
			HashMap<String, ArrayList<IfcPropertySetTemplate.Ifc4>> assoMap,
			HashMap<String, Object> libMap,
			Collection<IfcRelAssociatesLibrary.Ifc4> assoLib
		) {

		//Associate Library
		if (assoLib != null) {
			for (IfcRelAssociatesLibrary.Ifc4 rel : assoLib) {
				String name = ""; // EMPTY NAME
				if (rel.getRelatingLibrary() instanceof IfcLibraryReference.Ifc4) {
					if (((IfcLibraryReference.Ifc4)rel.getRelatingLibrary()).getName() != null) {
						name = ((IfcLibraryReference.Ifc4)rel.getRelatingLibrary()).getName().toString();
					}
				}
				
				ArrayList<IfcPropertySetTemplate.Ifc4> assoLibs = assoMap.get(name);
				if (assoLibs == null) {
					assoLibs = new ArrayList<>();
				}
				
				for (IfcDefinitionSelect.Ifc4 temp : rel.getRelatedObjects()) {
					
					if (temp == null || IfcUtilities.isTemporaryRemoved((ClassInterface)temp)) {
						continue;
					}

					if(temp instanceof IfcPropertySetTemplate.Ifc4) {
						if (((IfcPropertySetTemplate.Ifc4) temp).getHasAssociations_Inverse() == null) {
							continue;
						}
						assoLibs.add((IfcPropertySetTemplate.Ifc4)temp);
					}
				}
				
				assoMap.put(name, assoLibs);
				libMap.put(name, rel);
				
			}
		}
	}
	
	public TreeItem<ItemContainer> getLibByGuid(String guid) {
		return libByGuidMap.get(guid);
	}
	
	public ItemContainer getPropByGuid(String guid) {
		return propByGuidMap.get(guid);
	}
	
	
}

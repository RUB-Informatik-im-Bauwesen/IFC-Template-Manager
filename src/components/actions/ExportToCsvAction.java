package components.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.apstex.ifctoolbox.ifc.IfcPropertySetTemplate;
import com.apstex.ifctoolbox.ifc.IfcPropertyTemplate;
import com.apstex.ifctoolbox.ifc.IfcSimplePropertyTemplate;

import components.templating.ItemContainer;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * An action to perform export of selected elements in the TemplateTree to CSV.
 * 
 * @author Marcel Stepien
 *
 */
public class ExportToCsvAction implements ActionListener {

	private TreeItem<ItemContainer<?>> treeItem;
	
	public ExportToCsvAction(TreeItem<ItemContainer<?>> treeItem){
		this.treeItem = treeItem;
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		CountDownLatch doneLatch = new CountDownLatch(1);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("CSV Export");
				fileChooser.getExtensionFilters().addAll(new ExtensionFilter("CSV-File", "*.csv"));

				File selectedFile = fileChooser.showSaveDialog(null);
				
				List<String[]> dataLines = new ArrayList<>();
				
				int idIndex = 1;
				for (TreeItem<?> pSetTemplateTreeItem : treeItem.getChildren()) {
					if(pSetTemplateTreeItem.getValue() instanceof ItemContainer<?>) {
						ItemContainer<?> pSetTemplateItemContainer = (ItemContainer<?>) pSetTemplateTreeItem.getValue();
						if(pSetTemplateItemContainer.getItem() instanceof IfcPropertySetTemplate.Ifc4) {
							IfcPropertySetTemplate.Ifc4 psetTemplate = 
									(IfcPropertySetTemplate.Ifc4)pSetTemplateItemContainer.getItem();

							for (IfcPropertyTemplate propertyTemplate : 
								psetTemplate.getHasPropertyTemplates()) {
								
								String[] propertyLine = new String[7];
								dataLines.add(propertyLine);

								propertyLine[0] = Integer.toString(idIndex);
								idIndex++;
								
								propertyLine[1] = psetTemplate.getApplicableEntity().getDecodedValue();
								propertyLine[2] = psetTemplate.getName().getDecodedValue();
								propertyLine[3] = propertyTemplate.getName().getDecodedValue();
								
								if(propertyTemplate instanceof IfcSimplePropertyTemplate) {
									IfcSimplePropertyTemplate.Ifc4 simplePropertyTemplate = 
											(IfcSimplePropertyTemplate.Ifc4) propertyTemplate;
									
									propertyLine[4]=simplePropertyTemplate.getTemplateType().getValue().name();
									
									
									String psdType ="";
									
									switch (simplePropertyTemplate.getTemplateType().getValue().name()) {
									case "P_SINGLEVALUE":
										psdType = pSetTemplateItemContainer.getPSDType(
												simplePropertyTemplate.getGlobalId().getDecodedValue());
										break;
										
									case "P_ENUMERATEDVALUE":
										psdType = pSetTemplateItemContainer.getPSDType(
												simplePropertyTemplate.getGlobalId().getDecodedValue());
										break;
									case "P_LISTVALUE":
										psdType = pSetTemplateItemContainer.getPSDType(
												simplePropertyTemplate.getGlobalId().getDecodedValue());
										break;
									default:
										break;
									}
									
									
									propertyLine[5] = psdType;

									if(simplePropertyTemplate.getEnumerators() != null) {
										propertyLine[6] = simplePropertyTemplate.getEnumerators().getEnumerationValues().toString();
									}else {
										propertyLine[6] = "";
									}
								
								}//end if
								
							} //end for
							
						} //endif IfcPropertySetTemplate
					} //endif ItemContainer
				} //end for
				
				try (PrintWriter pw = new PrintWriter(selectedFile)) {
					
					pw.println("ID;Applicable Entity;PropertySet.Name;Property.Name;Property.Type;PSD DataType;Value Range");
					
			        for (String [] line : dataLines) {
			        	for(int i = 0; i<line.length-1; i++) {
			        		pw.print(line[i]+";");
			        	}
			        	pw.println(line[line.length-1]);
			        }
			        pw.close();
			    } catch (FileNotFoundException e) {
			    	
					// TODO Auto-generated catch block
			    	
					e.printStackTrace();
				}

				doneLatch.countDown();
			}
		});

		try {
			doneLatch.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
	}//actionPerformed
	
}

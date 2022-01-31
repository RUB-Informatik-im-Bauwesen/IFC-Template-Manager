package components.actions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.apstex.gui.core.j3d.model.cadobjectmodel.CadObjectJ3D;
import com.apstex.gui.core.kernel.Kernel;
import com.apstex.gui.core.model.applicationmodel.ApplicationModelNode;
import com.apstex.gui.core.model.cadobjectmodel.CadObject;
import com.apstex.gui.core.model.selectionmodel.SelectionModelListener;
import com.apstex.ifctoolbox.ifc.IfcProjectLibrary;
import com.apstex.ifctoolbox.ifc.IfcPropertySetTemplate;
import com.apstex.ifctoolbox.ifc.IfcRoot;
import com.apstex.ifctoolbox.ifcmodel.IfcModel;
import com.apstex.step.core.ClassInterface;

import components.ApplicationFrame;
import components.editors.AppendTemplateForSelectedFrameFX;
import components.editors.AppendTemplateFrameFX;
import components.editors.CreateLibraryFrame;
import components.editors.CreateProjectFrame;
import components.editors.CreatePropertySetFrameFX;
import components.editors.RemoveTemplateFrame;
import components.ifc.CustomIfcLoaderManager;
import components.io.PSDTransfer;
import components.templating.ItemContainer;
import components.templating.PropertyItem;
import components.templating.TemplateProjectPopupMenu;
import components.templating.TemplateProjectTreeView;
import components.templating.TemplatePropertyTreeView;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import utils.ApplicationUtilities;
import utils.CoreUtilities;
import utils.IfcUtilities;

/**
 * A selection of actions performed across the application, such as open and closing of files.
 * 
 * @author Marcel Stepien
 *
 */
@SuppressWarnings("rawtypes")
public class ApplicationActions {
	
	/**
	 * An action to load an IFC model.
	 */
	public static ActionListener openModelFile = new ActionListener() {		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			//final String sessionID = UUID.randomUUID().toString();
			CountDownLatch doneLatch = new CountDownLatch(1);

			Platform.runLater(new Runnable() {

				@Override
				public void run() {

					FileChooser fileChooser = new FileChooser();
					fileChooser.setTitle("Choose a IFC Model file");
					fileChooser.getExtensionFilters().addAll(new ExtensionFilter("IFC-File", "*.ifc"));

					File selectedFile = fileChooser.showOpenDialog(null);

					if (selectedFile != null) {
						
						try {

							ApplicationUtilities.model = new IfcModel(false);
							ApplicationUtilities.model.readStepFile(selectedFile);
							
							Kernel.getApplicationModelRoot().clearNodes();
							
							HashMap<String, Object> userProperties = new HashMap<>();
							userProperties.put("MODEL_FILE_NAME", selectedFile.getName());
							
							//Loading in Ifc3DView
							CustomIfcLoaderManager.getInstance().setUseThreads(false);
							CustomIfcLoaderManager.getInstance().loadStepModel(ApplicationUtilities.model, false, userProperties);
							
							//Add Enumeration from model to template
							/*
							for(IfcPropertyEnumeration enumeration : ApplicationUtilities.model.getCollection(IfcPropertyEnumeration.class)) {								
								IfcUtilities.addEnumerationToTemplate(enumeration);
							}
							*/
							
						} catch (Exception exp) {
							exp.printStackTrace();
						}	
					}

					doneLatch.countDown();
				}
			});

			try {
				doneLatch.await();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	};
	
	
	/**
	 * An action to load an IFC property template file.
	 */
	public static ActionListener openTemplateFile = new ActionListener() {		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			CountDownLatch doneLatch = new CountDownLatch(1);

			Platform.runLater(new Runnable() {

				@Override
				public void run() {

					FileChooser fileChooser = new FileChooser();
					fileChooser.setTitle("Choose a IFC Template file");
					fileChooser.getExtensionFilters().addAll(new ExtensionFilter("IFC-File", "*.ifc"));

					File selectedFile = fileChooser.showOpenDialog(null);

					if (selectedFile != null) {
						
						try {
							
							ApplicationUtilities.template = new IfcModel(true);
							ApplicationUtilities.template.readStepFile(selectedFile);
							
							TemplateProjectTreeView applicationTemplateTree = TemplateProjectTreeView.getInstance();
							TreeItem<ItemContainer> root = applicationTemplateTree.createTemplate(ApplicationUtilities.template);
							ApplicationFrame.reloadProjectTree(root);
							
						} catch (Exception exp) {
							exp.printStackTrace();
						}	
					}

					doneLatch.countDown();
				}
			});

			try {
				doneLatch.await();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	};

	/**
	 * An action to save an edtited IFC model.
	 */
	public static ActionListener saveModelFile = new ActionListener() {		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			if(ApplicationUtilities.model != null){
				CountDownLatch doneLatch = new CountDownLatch(1);

				Platform.runLater(new Runnable() {

					@Override
					public void run() {

						FileChooser fileChooser = new FileChooser();
						fileChooser.setTitle("Select a folder and filename to Export");
						fileChooser.getExtensionFilters().addAll(new ExtensionFilter("IFC-File", "*.ifc"));

						File selectedFile = fileChooser.showSaveDialog(null);

						if (selectedFile != null) {
							
							try {
								
								ApplicationUtilities.model.writeStepFile(selectedFile);
								System.out.println("File written: " + selectedFile.getAbsolutePath());
								
							} catch (Exception exp) {
								exp.printStackTrace();
							}	
						}

						doneLatch.countDown();
					}
				});

				try {
					doneLatch.await();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
			}else{
				
				JDialog dialog = new JDialog(new Frame(), "Exporter Message");
				dialog.setBounds(
						(int)((Toolkit.getDefaultToolkit().getScreenSize().width/2)-275/2), 
						(int)((Toolkit.getDefaultToolkit().getScreenSize().height/2)-45/2), 
						275, 
						90);
				dialog.setResizable(false);

				JPanel panel = new JPanel();	
				JLabel exportlbl = new JLabel("Could not find Model");
				exportlbl.setFont(new Font("Arial", 20, 20));
				panel.add(exportlbl, BorderLayout.CENTER);
				
				dialog.getContentPane().add(panel);
				dialog.setVisible(true);
			}
			
		}

	};

	/**
	 * An action to save an edtited IFC property template file.
	 */
	public static ActionListener saveTemplateFile = new ActionListener() {		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(ApplicationUtilities.template != null){
				
				CountDownLatch doneLatch = new CountDownLatch(1);

				Platform.runLater(new Runnable() {

					@Override
					public void run() {

						FileChooser fileChooser = new FileChooser();
						fileChooser.setTitle("Select a folder and filename to Export");
						fileChooser.getExtensionFilters().addAll(new ExtensionFilter("IFC-File", "*.ifc"));

						File selectedFile = fileChooser.showSaveDialog(null);

						if (selectedFile != null) {
							
							try {
								
								ApplicationUtilities.template.writeStepFile(selectedFile);
								System.out.println("File written: " + selectedFile.getAbsolutePath());
								
							} catch (Exception exp) {
								exp.printStackTrace();
							}	
						}

						doneLatch.countDown();
					}
				});

				try {
					doneLatch.await();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			
			}else{
				
				JDialog dialog = new JDialog(new Frame(), "Exporter Message");
				dialog.setBounds(
						(int)((Toolkit.getDefaultToolkit().getScreenSize().width/2)-275/2), 
						(int)((Toolkit.getDefaultToolkit().getScreenSize().height/2)-45/2), 
						275, 
						90);
				dialog.setResizable(false);

				JPanel panel = new JPanel();	
				JLabel exportlbl = new JLabel("Create template first!");
				exportlbl.setFont(new Font("Arial", 20, 20));
				panel.add(exportlbl, BorderLayout.CENTER);
				
				dialog.getContentPane().add(panel);
				dialog.setVisible(true);
			}

		}
	};

	/**
	 * An action that creates and initialize a new IFC property template file. 
	 */
	public static ActionListener createNewProject = new ActionListener() {		
		@Override
		public void actionPerformed(ActionEvent e) {
			new CreateProjectFrame();
		}
	};
	
	/**
	 * An action to create a new library.
	 */
	public static ActionListener createNewLibrary = new ActionListener() {		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(ApplicationUtilities.template != null) {
				new CreateLibraryFrame();
			}else {
				System.err.println("No context to append to loaded or created");
			}
		}
	};
	
	/**
	 * An action that creates a new PropertySet-template.
	 */
	public static ActionListener createNewTemplate = new ActionListener() {		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(ApplicationUtilities.template != null) {
				try {
					new CreatePropertySetFrameFX();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}else {
				System.err.println("No context to append to loaded or created");
			}
		}
	};


	/**
	 * An action to remove a selected item from the TemplateTree.
	 */
	public static class RemoveAction implements ActionListener {
		
		private TreeItem<ItemContainer<?>> treeItem;
		
		public RemoveAction(TreeItem<ItemContainer<?>> treeItem){
			this.treeItem = treeItem;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			if(treeItem.getValue() instanceof ItemContainer<?>) {
				ItemContainer container = (ItemContainer)treeItem.getValue();
					
				if(container.getItem() instanceof ClassInterface) {
					IfcUtilities.removeFromModel(ApplicationUtilities.template, (ClassInterface)container.getItem());
				}
				
				TreeItem<ItemContainer<?>> parent = treeItem.getParent();
				
				if(parent == null) {
					System.out.println("Cant delete Root item!");
					return;
				}
				
				parent.getChildren().remove(treeItem);
				
				TemplateProjectTreeView.getInstance().refresh();
				TemplateProjectTreeView.getInstance().getSelectionModel().select(parent);
				
				ArrayList<PropertyItem> items = TemplatePropertyTreeView.getInstance().createItems((ItemContainer)parent.getValue());
				TreeItem<PropertyItem> root = TemplatePropertyTreeView.getInstance().createTree(items);
				ApplicationFrame.reloadPropertyTree(root);
			}
			
			
		}
	};
	
	/**
	 * An action to load and edit either a PropertySet- or Library-Template.
	 */
	public static ActionListener editAction = new ActionListener() {		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			for(TreeItem<?> node : ApplicationFrame.getTemplateTree().getSelectionModel().getSelectedItems()) {
				
				if(node.getValue() instanceof ItemContainer<?>) {
					ItemContainer container = (ItemContainer)node.getValue();
					
					if(container.getItem() instanceof ClassInterface) {
					
						if(container.getItem() instanceof IfcProjectLibrary.Ifc4) {
							CreateLibraryFrame frame = new CreateLibraryFrame();
							frame.replaceInstance((IfcProjectLibrary.Ifc4)container.getItem());
						}
						
						if(container.getItem() instanceof IfcPropertySetTemplate.Ifc4) {
							//CreatePropertyFrame frame = new CreatePropertyFrame();
							CreatePropertySetFrameFX frame;
							try {
								frame = new CreatePropertySetFrameFX();
								frame.replaceInstance(container);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
						
					}
					
				}
			}
		}
	};
	
	
	/**
	 * An action that adjusts the color of entries in the template project tree when hovering.
	 */
	public static MouseListener hoverMarker = new MouseListener() {
		
		private Color temp = null;
		private Color borderColor = new Color(200, 200, 200);
		
		@Override
		public void mouseReleased(MouseEvent e) { }
		
		@Override
		public void mousePressed(MouseEvent e) { }
		
		@Override
		public void mouseExited(MouseEvent e) {
			Object source = e.getSource();
			if(source instanceof JComponent && temp != null) {
				JComponent comp = (JComponent)source;
				comp.setBorder(new LineBorder(temp, 2));
				temp = null;
			}
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			Object source = e.getSource();
			if(source instanceof JComponent && temp == null) {
				JComponent comp = (JComponent)source;
				if(comp.getBorder() instanceof LineBorder) {
					temp = ((LineBorder)comp.getBorder()).getLineColor();
				}
				comp.setBorder(new LineBorder(borderColor, 2));
			}
		}
		
		@Override
		public void mouseClicked(MouseEvent e) { }
	};
	
	/**
	 * An action to select rows in JTable objects.
	 * 
	 * @author Marcel Stepien
	 */
	public static class TableRowSelectionHandler implements ListSelectionListener {
		
		private JTable table = null;
		
		public TableRowSelectionHandler(JTable table) {
			this.table = table;
		}
		
		@Override
		public void valueChanged(ListSelectionEvent e) {
			
			//Selection for IfcViewer
			//Get new selection List from table
			ArrayList<ClassInterface> objectList = new ArrayList<>();
			for(int row : table.getSelectedRows()) {
				Object ob = table.getValueAt(row, 0);
				if(ob instanceof ItemContainer) {
					
					ItemContainer iCon = ((ItemContainer)table.getValueAt(row, 0));
					if(iCon.getItem() instanceof ClassInterface) {
						ClassInterface ifcClass = (ClassInterface)iCon.getItem();
						objectList.add(ifcClass);
					}
				}
			}
			CoreUtilities.setSelectedInstances(objectList);
		}
	};

	
	/**
	 * Opens a popup menu when pressing the right mouse button on the template project tree.
	 * 
	 * @author Marcel Stepien
	 */
	public static class TemplateTreeMouseHandler implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {

				TreeItem<?> node = ApplicationFrame.getTemplateTree().getSelectionModel().getSelectedItem();
				if(node != null) {
					Object usrObj = node.getValue();
					if(usrObj instanceof ItemContainer) {
				
						try {
							TemplateProjectPopupMenu popupMenu = new TemplateProjectPopupMenu("Project Tree Edit", (TreeItem<ItemContainer<?>>)node);
							popupMenu.show(e.getComponent(), e.getX(), e.getY());
							
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					
					}
				}
		        
		    }
			
			if(SwingUtilities.isLeftMouseButton(e)) {
				TreeItem<?> node = ApplicationFrame.getTemplateTree().getSelectionModel().getSelectedItem();
				
				if(node != null) {
					Object usrObj = node.getValue();
					if(usrObj instanceof ItemContainer) {
						
						ArrayList<PropertyItem> items = TemplatePropertyTreeView.getInstance().createItems((ItemContainer)usrObj);
						TreeItem<PropertyItem> root = TemplatePropertyTreeView.getInstance().createTree(items);
						ApplicationFrame.reloadPropertyTree(root);
						
					}
				}
			}
			
			if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
				editAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
			}
			
		}
		
		
		@Override
		public void mouseEntered(MouseEvent e) { }

		@Override
		public void mouseExited(MouseEvent e) { }

		@Override
		public void mousePressed(MouseEvent e) { }

		@Override
		public void mouseReleased(MouseEvent e) { }
	};
	
	/**
	 * An action to open the menu for attaching properties to an IFC model based on the templates.
	 */
	public static ActionListener appendTemplateToModel = new ActionListener() {		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			TreeItem<?> item = ApplicationFrame.getTemplateTree().getSelectionModel().getSelectedItem();
			
			if(item != null) {
				if(item.getValue() instanceof ItemContainer) {
					AppendTemplateFrameFX frame = new AppendTemplateFrameFX((ItemContainer)item.getValue());

					frame.createTemplateOptions((ItemContainer)item.getValue());
					frame.createObjDefOptions(ApplicationUtilities.model);
					frame.createMaterialOptions(ApplicationUtilities.model);

				
				}
			}
			
		}
	};
	

	/**
	 * An action to open the menu for attaching properties to an IFC model based on the templates.
	 * (Only targeted at selected objects)
	 */
	public static ActionListener appendTemplateToSelection = new ActionListener() {		
		@Override
		public void actionPerformed(ActionEvent e) {
			ArrayList<ClassInterface> objDefs = CoreUtilities.getSelectedInstances();
			
			TreeItem<?> item = ApplicationFrame.getTemplateTree().getSelectionModel().getSelectedItem();
			
			if(item != null) {
				if(item.getValue() instanceof ItemContainer) {
					AppendTemplateForSelectedFrameFX frame = new AppendTemplateForSelectedFrameFX((ItemContainer)item.getValue());

					frame.createTemplateOptions((ItemContainer)item.getValue());
					frame.createObjDefOptions(objDefs);	
					
				}
			}
			
			
		}
	};

	/**
	 * An action to delete attached properties from the IFC model.
	 */
	public static ActionListener removeTemplateFromModel = new ActionListener() {		
		@Override
		public void actionPerformed(ActionEvent e) {

			RemoveTemplateFrame frame = new RemoveTemplateFrame();
			frame.createObjDefOptions(ApplicationUtilities.model);
			
		}
	};
	
	/**
	 * A class to define an action to export IfcPropertySetTemplate as PSD.
	 */
	public static class ExportPSDAction implements ActionListener {
		
		private ItemContainer<IfcPropertySetTemplate.Ifc4> container;
		
		/**
		 * Creates a new ExportPSDAction.
		 * 
		 * @param container - an ItemContainer with an IfcPropertySetTemplate.Ifc4 object as content
		 */
		public ExportPSDAction(ItemContainer<IfcPropertySetTemplate.Ifc4> container){
			this.container = container;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			CountDownLatch doneLatch = new CountDownLatch(1);

			Platform.runLater(new Runnable() {

				@Override
				public void run() {

					DirectoryChooser dirChooser = new DirectoryChooser();
					dirChooser.setTitle("PSD Export");
					File selectedFile = dirChooser.showDialog(null);

					if (selectedFile != null) {
						
						try {
							
							PSDTransfer psdp = new PSDTransfer();
							psdp.writeFromIFC(selectedFile.getAbsolutePath(), container);
			
							System.out.println("PSD: " + container.getDisplay() + "  Exported To: " + selectedFile.getAbsolutePath());
						} catch (IOException exp) {
							System.err.println(exp.getMessage());
						}
						
					}

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
	

	public static class HighlightItemsAction implements ActionListener {
		
		private ArrayList<IfcRoot> items;

		public HighlightItemsAction(ArrayList<IfcRoot> items){
			this.items = items;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			for(ApplicationModelNode node : Kernel.getApplicationModelRoot().getNodes()) {
				for(CadObject cadObj : node.getCadObjectModel().getCadObjects()) {
					if(cadObj instanceof CadObjectJ3D) {
						((CadObjectJ3D)cadObj).setTransparency(0.5f);
					}
				}
				node.getSelectionModel().deselect(new SelectionModelListener() {
					@Override
					public void objectsSelected(Collection<Object> arg0, boolean arg1) {
						//nothing
					}
				}, node.getSelectionModel().getSelectedObjects());
			}
			
			for(ApplicationModelNode node : Kernel.getApplicationModelRoot().getNodes()) {
				for(IfcRoot ifcItem : items) {
					CadObject cadObj = node.getCadObjectModel().getCadObject(ifcItem);
					if(cadObj instanceof CadObjectJ3D) {
						((CadObjectJ3D)cadObj).setTransparency(1f);
					}
					node.getSelectionModel().setSelected(new SelectionModelListener() {
						@Override
						public void objectsSelected(Collection<Object> arg0, boolean arg1) {
							//NOTHING
						}
					}, items, true);
				}
			}
		}
	}
	
	/**
	 * A class to define an action to export IfcProjectLibraries as PSD. 
	 * A folder is exported which contains the PSD files.
	 */
	public static class ExportPSDLibraryAction implements ActionListener {
		
		private TreeItem<ItemContainer<?>> treeItem;
		
		/**
		 * Creates a new ExportPSDLibraryAction.
		 * 
		 * @param container - a TreeItem with an ItemContainer<IfcProjectLibrary.Ifc4> object as content
		 */
		public ExportPSDLibraryAction(TreeItem<ItemContainer<?>> treeItem){
			this.treeItem = treeItem;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			CountDownLatch doneLatch = new CountDownLatch(1);

			Platform.runLater(new Runnable() {

				@Override
				public void run() {

					DirectoryChooser dirChooser = new DirectoryChooser();
					dirChooser.setTitle("PSD Library Export");
					File selectedFile = dirChooser.showDialog(null);

					if (selectedFile != null) {
						
						ItemContainer<?> container = treeItem.getValue();
						
						File directory = new File(selectedFile.getAbsolutePath() + "/" + container.getDisplay());
						
						boolean success = directory.exists();
						if(!success) {
							success = directory.mkdirs();
						}
						
						if(success) {
							for(TreeItem<ItemContainer<?>> childTreeitem : treeItem.getChildren()) {
								
								ItemContainer<?> childValue = childTreeitem.getValue();
								if(childValue.getItem() instanceof IfcPropertySetTemplate.Ifc4) {
									try {
										ItemContainer<IfcPropertySetTemplate.Ifc4> propItem = (ItemContainer<IfcPropertySetTemplate.Ifc4>)childValue;
										
										PSDTransfer psdp = new PSDTransfer();
										psdp.writeFromIFC(selectedFile.getAbsolutePath() + "/" + container.getDisplay(), propItem);
						
										System.out.println("PSD: " + propItem.getDisplay() + " Exported To: " + selectedFile.getAbsolutePath() + "/" + container.getDisplay());
										
									} catch (IOException exp) {
										System.err.println(exp.getMessage());
									}
								}
								
							}
							System.out.println("All PSD Exported To: " + selectedFile.getAbsolutePath() + "/" + container.getDisplay());
						}else {
							System.err.println("Folder " + container.getDisplay() + " could not be created in: " + selectedFile.getAbsolutePath());
						}
						
					}

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
	
	/**
	 * A class to define an action to import PSD as IfcPropertySetTemplate.
	 */
	public static class ImportPSDAction implements ActionListener {
		
		private TreeItem<ItemContainer<?>> treeItem;
		
		/**
		 * Creates a new ExportPSDLibraryAction.
		 * 
		 * @param container - a TreeItem with an ItemContainer<IfcProjectLibrary.Ifc4> object as content
		 */
		public ImportPSDAction(TreeItem<ItemContainer<?>> treeItem){
			this.treeItem = treeItem;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			CountDownLatch doneLatch = new CountDownLatch(1);

			Platform.runLater(new Runnable() {

				@Override
				public void run() {

					FileChooser fileChooser = new FileChooser();
					fileChooser.setTitle("PSD Import");
					fileChooser.getExtensionFilters().addAll(new ExtensionFilter("PSD-File", "*.xml"));

					List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);

					if (selectedFiles != null) {
						for(File psdFile : selectedFiles) {
							if(psdFile.getAbsolutePath().toLowerCase().endsWith(".xml")) {
								PSDTransfer psdp = new PSDTransfer();
								
								ItemContainer<IfcProjectLibrary.Ifc4> container = (ItemContainer<IfcProjectLibrary.Ifc4>)treeItem.getValue();
								
								ArrayList<ItemContainer<IfcPropertySetTemplate.Ifc4>> newItemList = psdp.loadFromPSD(
										psdFile.getAbsolutePath(), 
										container
								);
								
								for(ItemContainer<IfcPropertySetTemplate.Ifc4> newItem : newItemList) {								
									treeItem.getChildren().add(new TreeItem<ItemContainer<?>>(newItem));
								}
							}
						}
					}

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

}

package components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingConstants;

import com.apstex.gui.core.dockable.DockableComponent;
import com.apstex.gui.core.j3d.views.view3d.Ifc3DViewJ3D;
import com.apstex.gui.core.views.modelexplorer.ModelExplorer;
import com.apstex.gui.core.views.stepfileview.StepFileView;
import com.apstex.gui.core.views.view3d.ModelViewerToolbar;
import com.apstex.gui.ifc.controller.IfcLoadManager;
import com.apstex.gui.ifc.views.propertiesview.PropertiesView;
import com.apstex.gui.ifc.views.spatialview.IfcSpatialStructureView;
import com.apstex.gui.ifc.views.typeview.IfcTypeView;
import com.apstex.gui.toolbar.IfcModelViewerToolbar;
import com.vldocking.swing.docking.DockableState;
import com.vldocking.swing.docking.DockingConstants;
import com.vldocking.swing.docking.DockingDesktop;
import com.vldocking.swing.docking.DockingPreferences;

import components.actions.ApplicationActions;
import components.ifc.CustomIfcPropertiesView;
import components.templating.ItemContainer;
import components.templating.PropertyItem;
import components.templating.TemplateProjectTreeView;
import components.templating.TemplatePropertyTreeView;
import extensions.filtering.FilterElementFrame;
import extensions.selectionsetview.IfcSelectionSetViewFX;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import utils.ApplicationUtilities;

/**
 * Mainframe of the Application. 
 * 
 * @author Marcel Stepien
 *
 */
public class ApplicationFrame extends JFrame{
	
	protected static JFrame selfReference = null;
	
	protected static Ifc3DViewJ3D ifcScene3d = null;
	protected static IfcTypeView typeView = null;
	protected static IfcSpatialStructureView spatialStructureView = null;
	protected static IfcModelViewerToolbar modelViewerToolbar = null;
	protected static ModelExplorer modelExplorer = null;
	protected static StepFileView stepFileView = null;
	protected static PropertiesView propView = null;

	protected static JFXPanel jfxPanel = new JFXPanel();
	protected static TreeTableView<ItemContainer> projectTreeTable = null;
	
	protected static JFXPanel propertyJfxPanel = new JFXPanel();
	protected static TreeTableView<PropertyItem> propertyView = null;
	
	protected DockingDesktop grid = null;
	
	protected static TemplatePropertyTreeView templateProperty = null;
	
	public ApplicationFrame(int width, int height) {
		this.setTitle("IFC Template Manager");
		this.setSize(new Dimension(width, height));
		this.setLocationRelativeTo(null);
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void init() throws IOException {

		//init menu bar
		this.setJMenuBar(new ApplicationMenuBar(this));
		
		//init center
		DockingPreferences.initHeavyWeightUsage();
		grid = new DockingDesktop();

		javafx.application.Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
					
			    projectTreeTable = TemplateProjectTreeView.getInstance();
			    
		        VBox vBox = new VBox();
		        vBox.getChildren().setAll(projectTreeTable);
		        VBox.setVgrow(projectTreeTable, Priority.ALWAYS);
		 
		        Scene scene = new Scene(vBox);
				
				jfxPanel.setScene(scene);
				
				jfxPanel.addKeyListener(new KeyListener() {
					
					@Override
					public void keyTyped(KeyEvent e) { }
					
					@Override
					public void keyReleased(KeyEvent e) { }
					
					@Override
					public void keyPressed(KeyEvent e) {
						if(e.getKeyCode() == KeyEvent.VK_DELETE) {
							new ApplicationActions.RemoveAction(
									(TreeItem<ItemContainer<?>>)TemplateProjectTreeView.getInstance().getSelectionModel().getSelectedItem()
							).actionPerformed(null);
						}
					}
				});
			}
		});
		jfxPanel.addMouseListener(new ApplicationActions.TemplateTreeMouseHandler());
		
		DockableComponent templateDocFX = new DockableComponent(jfxPanel, "Template View");
		
		propertyJfxPanel = new JFXPanel();
		DockableComponent propertyDoc = new DockableComponent(propertyJfxPanel, "Template Property");
		
		javafx.application.Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
					
			    propertyView = TemplatePropertyTreeView.getInstance();
			    
		        VBox vBox = new VBox();
		        vBox.getChildren().setAll(propertyView);
		        VBox.setVgrow(propertyView, Priority.ALWAYS);
		 
		        Scene scene = new Scene(vBox);
				
		        propertyJfxPanel.setScene(scene);
			}
		});
		
		ApplicationFrame.ifcScene3d = new Ifc3DViewJ3D();
		DockableComponent ifcViewer = new DockableComponent(ApplicationFrame.ifcScene3d, "IFC 3D View");
		
		spatialStructureView = new IfcSpatialStructureView();
		DockableComponent projectView = new DockableComponent(spatialStructureView, "Structure View");
		
		modelExplorer = new ModelExplorer();
		DockableComponent explorerViewDoc = new DockableComponent(modelExplorer, "Model Explorer");
		
		typeView = new IfcTypeView();
		DockableComponent typeViewDoc = new DockableComponent(typeView, "Type View");
		
		stepFileView = new StepFileView();
		DockableComponent stepFileViewDoc = new DockableComponent(stepFileView, "Stepfile View");
		
		ModelViewerToolbar modelViewerToolbar = new ModelViewerToolbar();
		modelViewerToolbar.addSeparator();
		
		JButton filterButton = new JButton("Filter Elements", new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream("../icons/filter.png")).getScaledInstance(26, 26, Image.SCALE_SMOOTH)));
		filterButton.setVerticalTextPosition(SwingConstants.BOTTOM);
		filterButton.setHorizontalTextPosition(SwingConstants.CENTER);
		filterButton.setFont(ModelViewerToolbar.buttonFont);
		filterButton.setMargin(ModelViewerToolbar.buttonInsets);
		
		filterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FilterElementFrame.getInstance();
			}
		});
		
		modelViewerToolbar.add(filterButton);

		
		this.add(modelViewerToolbar, BorderLayout.NORTH);
		
		propView = new CustomIfcPropertiesView();
		DockableComponent propViewDoc = new DockableComponent(propView, "Property View");
		
		ifcViewer.getDockKey().setResizeWeight(1.0f);
		grid.addDockable(ifcViewer);
		
		grid.split(ifcViewer, templateDocFX, DockingConstants.SPLIT_LEFT);
		grid.setDockableWidth(templateDocFX, 0.25);		
		grid.createTab(ifcViewer, stepFileViewDoc, 1);
		
		DockableComponent selectionSet = new DockableComponent(
				IfcSelectionSetViewFX.getInstance(), 
				"SelectionSet"
		);
		grid.createTab(templateDocFX, selectionSet, 1);
		
		grid.split(templateDocFX, propertyDoc, DockingConstants.SPLIT_BOTTOM);
		grid.setDockableHeight(templateDocFX, 0.5);
		
		grid.split(ifcViewer, projectView, DockingConstants.SPLIT_RIGHT);
		grid.setDockableWidth(projectView, 0.3);		
		grid.createTab(projectView, typeViewDoc, 1);
		
		grid.split(projectView, explorerViewDoc, DockingConstants.SPLIT_BOTTOM);
		grid.setDockableHeight(projectView, 0.5);
		grid.createTab(explorerViewDoc, propViewDoc, 1);


		this.add(grid, BorderLayout.CENTER);
		initDisableClosingDockWindows();
		
		//load default model
		IfcLoadManager.getInstance().loadStepModel(ApplicationUtilities.model);
		
		ApplicationFrame.selfReference = this;
	}
	
	private void initDisableClosingDockWindows()
	{
		for (DockableState ds : grid.getDockables())
		{
			ds.getDockable().getDockKey().setCloseEnabled(false);
		}
	}
	
	public static void reloadProjectTree(TreeItem<ItemContainer> root) {
		
		javafx.application.Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				projectTreeTable.setRoot(root);
				projectTreeTable.refresh();
			}
		});
		
	}
	
	public static void reloadPropertyTree(TreeItem<PropertyItem> root) {
		
		javafx.application.Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				propertyView.setRoot(root);
				propertyView.refresh();
			}
		});
		
	}
	
	public static TreeTableView<?> getTemplateTree() {
		return ApplicationFrame.projectTreeTable;
	}
	
	
	public static JFrame getApplicationFrame() {
		return ApplicationFrame.selfReference;
	}

	public static IfcSpatialStructureView getSpatialStructureView() {
		return spatialStructureView;
	}

	public static IfcTypeView getTypeView() {
		return typeView;
	}

	public static ModelExplorer getModelExplorer() {
		return modelExplorer;
	}

	public static StepFileView getStepFileView() {
		return stepFileView;
	}

	public static TreeTableView<PropertyItem> getTemplatePropertyView() {
		return propertyView;
	}
	
	public static PropertiesView getIfcPropertyView() {
		return propView;
	}
}

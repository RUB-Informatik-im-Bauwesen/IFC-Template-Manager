import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;

import components.ApplicationFrame;
//import bibliothek.gui.DockController;
import javafx.application.Platform;
import utils.IfcUtilities;

public class Run {

	public static void main(String[] args) {

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		// Needed in order to resize the 3D Scene! IMPORTANT
		Platform.setImplicitExit(false); 

		//Disable Docking Frames Warning Window and Introduction
		//DockController.disableCoreWarning();
      
		
		ApplicationFrame app = new ApplicationFrame(
				(int)dim.getWidth() - 100, 
				(int)dim.getHeight() - 100
		);
		

		try {
			app.init();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		app.setVisible(true);
		
		//Load default template project
		IfcUtilities.createDefaultProject();
		
		//Create wall and load to Template Manager
		//IfcModel model = IfcUtilities.createWallFile("WallModel.ifc");
		//ApplicationUtilities.model = model;
		//HashMap<String, Object> userProperties = new HashMap<>();
		//userProperties.put("MODEL_FILE_NAME", "WALL EXAMPLE");

		//Kernel.getApplicationModelRoot().clearNodes();
		
		//Loading in Ifc3DView
		//CustomIfcLoaderManager.getInstance().setUseThreads(false);
		//CustomIfcLoaderManager.getInstance().loadStepModel(model, false, userProperties);
		
	}

}

package components.editors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.apstex.ifctoolbox.ifc.IfcActorRole;
import com.apstex.ifctoolbox.ifc.IfcAddress;
import com.apstex.ifctoolbox.ifc.IfcApplication;
import com.apstex.ifctoolbox.ifc.IfcChangeActionEnum;
import com.apstex.ifctoolbox.ifc.IfcContext;
import com.apstex.ifctoolbox.ifc.IfcDefinitionSelect;
import com.apstex.ifctoolbox.ifc.IfcGloballyUniqueId;
import com.apstex.ifctoolbox.ifc.IfcIdentifier;
import com.apstex.ifctoolbox.ifc.IfcLabel;
import com.apstex.ifctoolbox.ifc.IfcOrganization;
import com.apstex.ifctoolbox.ifc.IfcOwnerHistory;
import com.apstex.ifctoolbox.ifc.IfcPerson;
import com.apstex.ifctoolbox.ifc.IfcPersonAndOrganization;
import com.apstex.ifctoolbox.ifc.IfcProject;
import com.apstex.ifctoolbox.ifc.IfcRelDeclares;
import com.apstex.ifctoolbox.ifc.IfcText;
import com.apstex.ifctoolbox.ifc.IfcTimeStamp;
import com.apstex.ifctoolbox.ifcmodel.IfcModel;
import com.apstex.step.core.LIST;
import com.apstex.step.core.SET;
import com.apstex.step.guidcompressor.GuidCompressor;

import components.ApplicationFrame;
import components.templating.ItemContainer;
import components.templating.TemplateProjectTreeView;
import javafx.scene.control.TreeItem;
import utils.ApplicationUtilities;

/**
 * Frame for appending the instance of a Template to the Model.
 * 
 * @author Marcel Stepien
 *
 */
public class CreateProjectFrame extends JFrame{
	
	private Dimension maxSizeDim = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	private Dimension labelSizeDim = new Dimension(150, 16);
	private JTextField projectTitelTextField;
	private JTextArea descriptionTextArea;
	private JTextField nameTextField;
	private JTextField familyNameTextField;
	private JTextField identificationTextField;
	private JTextField organizationNameTextField;
	private JTextArea organDescriptionTextArea;
	private JTextField organIdentTextField;
	
	/**
	 * Constructor
	 */
	public CreateProjectFrame() {
		super();
		
		String title = "Create a new Template Project";
		this.setTitle(title);
			
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.setSize(750, 550);
		getContentPane().setLayout(new BorderLayout());
		
		JScrollPane contentScrollPane = new JScrollPane();
		contentScrollPane.setBorder(new EmptyBorder(3, 3, 3, 3));
		getContentPane().add(contentScrollPane, BorderLayout.CENTER);
		
		JPanel contentPanel = new JPanel();
		contentPanel.setBorder(null);
		contentScrollPane.setViewportView(contentPanel);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		
		JPanel projectPanel = new JPanel();
		projectPanel.setBorder(new TitledBorder(null, "General Project Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPanel.add(projectPanel);
		projectPanel.setLayout(new BoxLayout(projectPanel, BoxLayout.Y_AXIS));
		
		Box boxA = Box.createHorizontalBox();
		projectPanel.add(boxA);
		boxA.setBorder(new EmptyBorder(3, 3, 3, 3));
		
		JLabel lblProjectTitel = new JLabel("Project Titel:");
		lblProjectTitel.setPreferredSize(new Dimension(90, 16));
		boxA.add(lblProjectTitel);
		
		projectTitelTextField = new JTextField();
		projectTitelTextField.setMaximumSize(new Dimension(2147483647, 22));
		boxA.add(projectTitelTextField);
		projectTitelTextField.setColumns(10);
		
		Box boxB = Box.createHorizontalBox();
		boxB.setMaximumSize(new Dimension(32767, 100));
		boxB.setBorder(new EmptyBorder(3, 3, 3, 3));
		projectPanel.add(boxB);
		
		JLabel lblProjectDescription = new JLabel("Description:");
		lblProjectDescription.setVerticalAlignment(SwingConstants.TOP);
		lblProjectDescription.setPreferredSize(new Dimension(90, 90));
		boxB.add(lblProjectDescription);
		
		descriptionTextArea = new JTextArea();
		descriptionTextArea.setBorder(new LineBorder(Color.LIGHT_GRAY));
		
		descriptionTextArea.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent arg0) { }
			
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				descriptionTextArea.setToolTipText("<html><header></header><body>" + descriptionTextArea.getText() + "</body></html>");
			}
			
			@Override
			public void changedUpdate(DocumentEvent arg0) { }
		});
		

		JScrollPane scrollDescription = new JScrollPane(
				descriptionTextArea, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
		);
		scrollDescription.setPreferredSize(new Dimension(4, 90));
		scrollDescription.setMinimumSize(new Dimension(4, 90));
		
		boxB.add(scrollDescription);
		
		JPanel ownerPanel = new JPanel();
		ownerPanel.setBorder(new TitledBorder(null, "Owner Information (optional)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPanel.add(ownerPanel);
		ownerPanel.setLayout(new BoxLayout(ownerPanel, BoxLayout.Y_AXIS));
		
		Box boxFA = Box.createHorizontalBox();
		boxFA.setBorder(new EmptyBorder(3, 3, 3, 3));
		ownerPanel.add(boxFA);
		
		JLabel namelbl = new JLabel("Name:");
		namelbl.setPreferredSize(new Dimension(90, 16));
		boxFA.add(namelbl);
		
		nameTextField = new JTextField();
		nameTextField.setMaximumSize(new Dimension(2147483647, 22));
		nameTextField.setColumns(10);
		boxFA.add(nameTextField);
		
		Box boxFB = Box.createHorizontalBox();
		boxFB.setBorder(new EmptyBorder(3, 3, 3, 3));
		ownerPanel.add(boxFB);
		
		JLabel familyNamelbl = new JLabel("Family Name:");
		boxFB.add(familyNamelbl);
		familyNamelbl.setPreferredSize(new Dimension(90, 16));
		
		familyNameTextField = new JTextField();
		boxFB.add(familyNameTextField);
		familyNameTextField.setMaximumSize(new Dimension(2147483647, 22));
		familyNameTextField.setColumns(10);
		
		Box boxFC = Box.createHorizontalBox();
		boxFC.setBorder(new EmptyBorder(3, 3, 3, 3));
		ownerPanel.add(boxFC);
		
		JLabel identificationlbl = new JLabel("Ident. ID:");
		identificationlbl.setPreferredSize(new Dimension(90, 16));
		boxFC.add(identificationlbl);
		
		identificationTextField = new JTextField();
		identificationTextField.setMaximumSize(new Dimension(2147483647, 22));
		identificationTextField.setColumns(10);
		boxFC.add(identificationTextField);
		
		JPanel organizationPanel = new JPanel();
		organizationPanel.setBorder(new TitledBorder(null, "Organization (optional)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPanel.add(organizationPanel);
		organizationPanel.setLayout(new BoxLayout(organizationPanel, BoxLayout.Y_AXIS));
		
		Box boxOA = Box.createHorizontalBox();
		boxOA.setBorder(new EmptyBorder(3, 3, 3, 3));
		organizationPanel.add(boxOA);
		
		JLabel organizationNamelbl = new JLabel("Name:");
		organizationNamelbl.setPreferredSize(new Dimension(90, 16));
		boxOA.add(organizationNamelbl);
		
		organizationNameTextField = new JTextField();
		organizationNameTextField.setMaximumSize(new Dimension(2147483647, 22));
		organizationNameTextField.setColumns(10);
		boxOA.add(organizationNameTextField);
		
		Box boxOB = Box.createHorizontalBox();
		boxOB.setMaximumSize(new Dimension(32767, 100));
		boxOB.setBorder(new EmptyBorder(3, 3, 3, 3));
		organizationPanel.add(boxOB);
		
		JLabel organDescriptionlbl = new JLabel("Description:");
		organDescriptionlbl.setVerticalAlignment(SwingConstants.TOP);
		organDescriptionlbl.setPreferredSize(new Dimension(90, 90));
		boxOB.add(organDescriptionlbl);
		
		organDescriptionTextArea = new JTextArea();
		organDescriptionTextArea.setBorder(new LineBorder(Color.LIGHT_GRAY));
		
		organDescriptionTextArea.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent arg0) { }
			
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				organDescriptionTextArea.setToolTipText("<html><header></header><body>" + organDescriptionTextArea.getText() + "</body></html>");
			}
			
			@Override
			public void changedUpdate(DocumentEvent arg0) { }
		});
		

		JScrollPane scrollDescription2 = new JScrollPane(
				organDescriptionTextArea, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
		);
		scrollDescription2.setPreferredSize(new Dimension(4, 90));
		scrollDescription2.setMinimumSize(new Dimension(4, 90));
		
		
		boxOB.add(scrollDescription2);
		
		Box boxOC = Box.createHorizontalBox();
		boxOC.setBorder(new EmptyBorder(3, 3, 3, 3));
		organizationPanel.add(boxOC);
		
		JLabel organIdentlbl = new JLabel("Ident. ID:");
		organIdentlbl.setPreferredSize(new Dimension(90, 16));
		boxOC.add(organIdentlbl);
		
		organIdentTextField = new JTextField();
		organIdentTextField.setMaximumSize(new Dimension(2147483647, 22));
		organIdentTextField.setColumns(10);
		boxOC.add(organIdentTextField);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(new LineBorder(Color.LIGHT_GRAY));
		buttonPanel.setMaximumSize(new Dimension(32767, 60));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		JButton btnCreate = new JButton("Create");
		btnCreate.setPreferredSize(new Dimension(100, 25));
		btnCreate.setMinimumSize(new Dimension(100, 25));
		btnCreate.setMaximumSize(new Dimension(100, 25));
		
		btnCreate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean flag = createProject();
				
				if(flag) {
					
					TemplateProjectTreeView applicationTemplateTree = TemplateProjectTreeView.getInstance();
					TreeItem<ItemContainer> root = applicationTemplateTree.createTemplate(ApplicationUtilities.template);
					ApplicationFrame.reloadProjectTree(root);
					
					dispose();
				}
				
			}
		});
		
		buttonPanel.add(btnCreate);
		
		
		//TODO
		
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/**
	 * Check if input is valid.
	 * 
	 * @return flag
	 */
	private boolean checkInput() {
		boolean flag = true;
		if(projectTitelTextField.getText().length() == 0) {
			projectTitelTextField.setBackground(Color.orange);
			projectTitelTextField.setToolTipText("Project name is required.");
			flag = false;
		}else {
			projectTitelTextField.setBackground(Color.white);
			projectTitelTextField.setToolTipText("");
		}
		
		if(descriptionTextArea.getText().length() == 0) {
			descriptionTextArea.setBackground(Color.orange);
			descriptionTextArea.setToolTipText("Project description is required.");
			flag = false;
		}else {
			descriptionTextArea.setBackground(Color.white);
			descriptionTextArea.setToolTipText("");
		}
		return flag;
	}
	
	/**
	 * Read information from frame and create the IfcModel for template content. 
	 */
	private boolean createProject() {
		
		//check if input is valid
		boolean flag = checkInput();
		if(!flag) {
			return flag;
		}
		
		//create template stepmodel
		IfcModel model = new IfcModel(true);
		
		IfcPerson person = new IfcPerson.Ifc4.Instance(
				new IfcIdentifier.Ifc4(identificationTextField.getText(), true), //Identification
				new IfcLabel.Ifc4(familyNameTextField.getText(), true), //FamilyName
				new IfcLabel.Ifc4(nameTextField.getText(), true), //GivenName
				new LIST<IfcLabel>(), //MiddleNames
				new LIST<IfcLabel>(), //PrefixTitels
				new LIST<IfcLabel>(), //SuffixTitles
				new LIST<IfcActorRole>(), //Roles
				new LIST<IfcAddress>() //Addresses
		);
		model.addObject(person);
		
		IfcOrganization organization = new IfcOrganization.Ifc4.Instance(
				new IfcIdentifier.Ifc4(organIdentTextField.getText(), true), //Identification
				new IfcLabel.Ifc4(organizationNameTextField.getText(), true), //Name
				new IfcText.Ifc4(organDescriptionTextArea.getText(), true), //Description
				new LIST<IfcActorRole>(), //Roles
				new LIST<IfcAddress>() //Addresses
		);
		model.addObject(organization);
		
		IfcPersonAndOrganization.Ifc4 personAndorganization = new IfcPersonAndOrganization.Ifc4.Instance(
				person, //The Person
				organization, //The Organization
				new LIST<IfcActorRole>() //Roles
		);
		model.addObject(personAndorganization);
		
		IfcApplication application = new IfcApplication.Ifc4.Instance(
				organization, //Application Developer
				new IfcLabel.Ifc4("", true), //Version
				new IfcLabel.Ifc4("", true), //Application Full Name
				new IfcIdentifier.Ifc4("", true) //Application Identifier
		);
		model.addObject(application);
		
		IfcOwnerHistory history = new IfcOwnerHistory.Ifc4.Instance(
				personAndorganization, 
				application, 
				null, 
				new IfcChangeActionEnum.Ifc4(IfcChangeActionEnum.Ifc4.IfcChangeActionEnum_internal.NOTDEFINED), 
				null,
				null, 
				null, 
				new IfcTimeStamp.Ifc4((int)new Date().getTime()) 
		);
		model.addObject(history);
		
		if(projectTitelTextField.getText().length() == 0) {
			projectTitelTextField.setBackground(Color.orange);
			return false;
		}else {
			projectTitelTextField.setBackground(Color.white);
		}
		
		if(descriptionTextArea.getText().length() == 0) {
			descriptionTextArea.setBackground(Color.orange);
			return false;
		}else {
			descriptionTextArea.setBackground(Color.white);
		}
		
		IfcProject project = new IfcProject.Ifc4.Instance();
		project.setGlobalId(new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true));
		project.setOwnerHistory(history);
		project.setName(new IfcLabel.Ifc4(projectTitelTextField.getText(), true));
		project.setDescription(new IfcText.Ifc4(descriptionTextArea.getText(), true));
		model.addObject(project);
		
//		IfcProjectLibrary defLibrary = new IfcProjectLibrary.Ifc4.Instance();
//		defLibrary.setOwnerHistory(history);
//		defLibrary.setGlobalId(new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true));
//		defLibrary.setDescription(new IfcText.Ifc4("This is a default library that contains all unassigned templates.", true));
//		defLibrary.setName(new IfcLabel.Ifc4(ApplicationUtilities.DEFLIBNAME, true));
//		model.addObject(defLibrary);
		
		IfcRelDeclares relDeclares = new IfcRelDeclares.Ifc4.Instance();
		relDeclares.setGlobalId(new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true));
		relDeclares.setDescription(new IfcText.Ifc4("Relation of project and default library.", true));
		relDeclares.setName(new IfcLabel.Ifc4("Default Library Relation", true));
		relDeclares.setRelatingContext((IfcContext)project);
		
		SET<IfcDefinitionSelect> selects = new SET<>();
//		selects.add((IfcDefinitionSelect)defLibrary);
	
		relDeclares.setRelatedDefinitions(selects);
		model.addObject(relDeclares);
		
//		ApplicationUtilities.defLibrary = defLibrary;
		ApplicationUtilities.template = model;
		return true;
	}

}

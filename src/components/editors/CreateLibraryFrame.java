package components.editors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.apstex.ifctoolbox.ifc.IfcContext;
import com.apstex.ifctoolbox.ifc.IfcDefinitionSelect;
import com.apstex.ifctoolbox.ifc.IfcGloballyUniqueId;
import com.apstex.ifctoolbox.ifc.IfcLabel;
import com.apstex.ifctoolbox.ifc.IfcProjectLibrary;
import com.apstex.ifctoolbox.ifc.IfcRelDeclares;
import com.apstex.ifctoolbox.ifc.IfcText;
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
public class CreateLibraryFrame extends JFrame{
	
	private Dimension maxSizeDim = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	private Dimension labelSizeDim = new Dimension(150, 16);
	private JTextField templateTitelTextField;
	private JTextArea descriptionTextArea;
	private JButton btnAdd;
	
	private IfcProjectLibrary.Ifc4 oldLibrary = null;
	
	/**
	 * Constructor
	 */
	public CreateLibraryFrame() {
		super();
		
		String title = "Create a new Library";
		this.setTitle(title);
			
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.setSize(750, 250);
		getContentPane().setLayout(new BorderLayout());
		
		JScrollPane contentScrollPane = new JScrollPane();
		contentScrollPane.setBorder(new EmptyBorder(3, 3, 3, 3));
		getContentPane().add(contentScrollPane, BorderLayout.CENTER);
		
		JPanel contentPanel = new JPanel();
		contentPanel.setBorder(null);
		contentScrollPane.setViewportView(contentPanel);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		
		JPanel templatePanel = new JPanel();
		templatePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "General Template Information", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		contentPanel.add(templatePanel);
		templatePanel.setLayout(new BoxLayout(templatePanel, BoxLayout.Y_AXIS));
		
		Box boxA = Box.createHorizontalBox();
		templatePanel.add(boxA);
		boxA.setBorder(new EmptyBorder(3, 3, 3, 3));
		
		JLabel lblTemplateTitel = new JLabel("Library Name:");
		lblTemplateTitel.setPreferredSize(new Dimension(90, 16));
		boxA.add(lblTemplateTitel);
		
		templateTitelTextField = new JTextField();
		templateTitelTextField.setMaximumSize(new Dimension(2147483647, 22));
		boxA.add(templateTitelTextField);
		templateTitelTextField.setColumns(10);
		
		Box boxB = Box.createHorizontalBox();
		boxB.setMaximumSize(new Dimension(32767, 100));
		boxB.setBorder(new EmptyBorder(3, 3, 3, 3));
		templatePanel.add(boxB);
		
		JLabel lblTemplateDescription = new JLabel("Description:");
		lblTemplateDescription.setVerticalAlignment(SwingConstants.TOP);
		lblTemplateDescription.setPreferredSize(new Dimension(90, 90));
		boxB.add(lblTemplateDescription);
		
		descriptionTextArea = new JTextArea();
		descriptionTextArea.setBorder(new LineBorder(Color.LIGHT_GRAY));
					
		JScrollPane scrollDescription = new JScrollPane(
				descriptionTextArea, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
		);
		
		
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
		
		
		scrollDescription.setPreferredSize(new Dimension(4, 90));
		scrollDescription.setMinimumSize(new Dimension(4, 90));
		boxB.add(scrollDescription);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(new LineBorder(Color.LIGHT_GRAY));
		buttonPanel.setMaximumSize(new Dimension(32767, 60));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		btnAdd = new JButton("Add");
		btnAdd.setPreferredSize(new Dimension(100, 25));
		btnAdd.setMinimumSize(new Dimension(100, 25));
		btnAdd.setMaximumSize(new Dimension(100, 25));
		
		btnAdd.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				
				boolean flag = false;
				
				if(oldLibrary == null) {
					flag = createLibrary();
				}else {
					flag = updateLibrary();
				}
				
				if(flag) {
					
					TemplateProjectTreeView applicationTemplateTree = TemplateProjectTreeView.getInstance();
					TreeItem<ItemContainer> root = applicationTemplateTree.createTemplate(ApplicationUtilities.template);
					ApplicationFrame.reloadProjectTree(root);
					
					
					dispose();
				}
				
			}
		});
		
		buttonPanel.add(btnAdd);
		
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/**
	 * Check if input is valid.
	 * 
	 * @return
	 */
	private boolean checkInput() {
		boolean flag = true;
		if(templateTitelTextField.getText().length() == 0) {
			templateTitelTextField.setBackground(Color.orange);
			templateTitelTextField.setToolTipText("Name is required.");
			flag = false;
		}else {
			templateTitelTextField.setBackground(Color.white);
			templateTitelTextField.setToolTipText("");
		}
		
		if(descriptionTextArea.getText().length() == 0) {
			descriptionTextArea.setBackground(Color.orange);
			descriptionTextArea.setToolTipText("Description is required.");
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
	private boolean createLibrary() {
		
		//check if input is valid
		boolean flag = checkInput();
		if(!flag) {
			return flag;
		}
		
		
		IfcProjectLibrary.Ifc4 library = new IfcProjectLibrary.Ifc4.Instance();
		library.setGlobalId(new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true));
		library.setDescription(new IfcText.Ifc4(descriptionTextArea.getText(), true));
		library.setName(new IfcLabel.Ifc4(templateTitelTextField.getText(), true));
		//library.setOwnerHistory(((IfcLibraryReference.Ifc4)libCon.getItem()).getOwnerHistory());
		ApplicationUtilities.template.addObject(library);
		
		IfcRelDeclares.Ifc4 relDec = new IfcRelDeclares.Ifc4.Instance();
		relDec.setGlobalId(new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true));
		relDec.setName(new IfcLabel.Ifc4(templateTitelTextField.getText() + "_LibRel", true));
		relDec.setDescription(new IfcText.Ifc4("Relation between Project and " + templateTitelTextField.getText() + " Library", true));
		
		if(ApplicationUtilities.template.getIfcProject() != null) {
			relDec.setRelatingContext((IfcContext.Ifc4)ApplicationUtilities.template.getIfcProject());
		}
		
		SET<IfcDefinitionSelect.Ifc4> set = new SET<>();
		set.add(library);
		relDec.setRelatedDefinitions(set);
		ApplicationUtilities.template.addObject(relDec);
		
		return true;
	}
	
	private boolean updateLibrary() {
		
		//check if input is valid
		boolean flag = checkInput();
		if(!flag) {
			return flag;
		}
		
		this.oldLibrary.setName(new IfcLabel.Ifc4(templateTitelTextField.getText(), true));
		this.oldLibrary.setDescription(new IfcText.Ifc4(descriptionTextArea.getText(), true));
		
		for(IfcRelDeclares.Ifc4 declares : this.oldLibrary.getHasContext_Inverse()) {
			declares.setName(new IfcLabel.Ifc4(templateTitelTextField.getText() + "_LibRel", true));
			declares.setDescription(new IfcText.Ifc4("Relation between Project and " + templateTitelTextField.getText() + " Library", true));
		}
		
		return true;
	}
	
	/**
	 * Sets the predefined context for the frame to replace a existing library.
	 * 
	 * @param oldLibrary
	 */
	public void replaceInstance(IfcProjectLibrary.Ifc4 oldLibrary) {
		this.oldLibrary = oldLibrary;
		
		descriptionTextArea.setText(oldLibrary.getDescription().getDecodedValue());
		templateTitelTextField.setText(oldLibrary.getName().getDecodedValue());
		btnAdd.setText("Update");
		
		this.repaint();
	}
	
}

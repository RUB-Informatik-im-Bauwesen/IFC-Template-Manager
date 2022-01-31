package components.editors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.apstex.ifctoolbox.ifc.IfcContext;
import com.apstex.ifctoolbox.ifc.IfcDefinitionSelect;
import com.apstex.ifctoolbox.ifc.IfcGloballyUniqueId;
import com.apstex.ifctoolbox.ifc.IfcIdentifier;
import com.apstex.ifctoolbox.ifc.IfcLabel;
import com.apstex.ifctoolbox.ifc.IfcLibraryReference;
import com.apstex.ifctoolbox.ifc.IfcProjectLibrary;
import com.apstex.ifctoolbox.ifc.IfcPropertyEnumeration;
import com.apstex.ifctoolbox.ifc.IfcPropertySetTemplate;
import com.apstex.ifctoolbox.ifc.IfcPropertySetTemplateTypeEnum;
import com.apstex.ifctoolbox.ifc.IfcPropertyTemplate;
import com.apstex.ifctoolbox.ifc.IfcRelAssociatesLibrary;
import com.apstex.ifctoolbox.ifc.IfcRelDeclares;
import com.apstex.ifctoolbox.ifc.IfcSimplePropertyTemplate;
import com.apstex.ifctoolbox.ifc.IfcSimplePropertyTemplateTypeEnum;
import com.apstex.ifctoolbox.ifc.IfcStateEnum;
import com.apstex.ifctoolbox.ifc.IfcText;
import com.apstex.ifctoolbox.ifc.IfcUnit;
import com.apstex.step.core.SET;
import com.apstex.step.guidcompressor.GuidCompressor;

import components.ApplicationFrame;
import components.templating.ItemContainer;
import components.templating.PropertyItem;
import components.templating.TemplateProjectTreeView;
import components.templating.TemplatePropertyTreeView;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import utils.ApplicationUtilities;
import utils.CustomComboboxCell;
import utils.CustomTextFieldTableCell;
import utils.IfcUtilities;

/**
 * Frame for appending the instance of a Template to the Model.
 * 
 * @author Marcel Stepien
 *
 */
public class CreatePropertySetFrameFX extends JFrame {

	private Dimension maxSizeDim = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	private Dimension labelSizeDim = new Dimension(150, 16);

	private JTextField templateTitelTextField;
	private JTextArea descriptionTextArea;
	private JTextField applicableTypeTextField;
	
	private JComboBox typeComboBox;
	private JComboBox libraryComboBox;

	private String defaultTypeValue = "P_SINGLEVALUE";
	private String defaultpsdTypeValue = "IfcLabel";
	private HashMap<String, ItemContainer> libraryMap;
	private JButton btnAdd;

	private TreeTableView<Object[]> templateTable = null;

	private ItemContainer<IfcPropertySetTemplate.Ifc4> oldContainer = null;

	private boolean updateMode = false;
	private CreatePropertySetFrameFX self = null;
	
	/**
	 * Open the Popup menu if right mouse button has been clicked.
	 * 
	 * @author Marcel Stepien
	 *
	 */
	public class PopUpMouseHandler extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {

				ArrayList<TreeItem> selecteditems = new ArrayList<>();

				for (Object o : templateTable.getSelectionModel().getSelectedCells()) {
					if (o instanceof TreeTablePosition<?, ?>) {
						TreeTablePosition tablePos = (TreeTablePosition) o;
						selecteditems.add(tablePos.getTreeItem());
					}
				}

				CreatePropertySetPopupMenuFX popupMenu = null;
				if (selecteditems.size() == 1) {
					popupMenu = new CreatePropertySetPopupMenuFX(self, "Selection Set PopUp", selecteditems.get(0));
					try {
						popupMenu.inititializeContent();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}
	};


	/**
	 * Constructor
	 * @throws IOException 
	 */
	public CreatePropertySetFrameFX() throws IOException {
		super();
		initialize();
		self = this;

		if (libraryMap.isEmpty()) {

			CountDownLatch doneLatch = new CountDownLatch(1);
			Platform.runLater(new Runnable() {

				@Override
				public void run() {

					Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle("Create New Property Alert");
					alert.setHeaderText("No Library found!");
					alert.setContentText("Please create a new Library first.");
					alert.showAndWait();

					doneLatch.countDown();
				}
			});

			return;
		}

		String title = "Create a new Template Project";
		this.setTitle("Create a new Template");

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.setSize(800, 600);
		getContentPane().setLayout(new BorderLayout());
		this.setLocationRelativeTo(null);

		JScrollPane contentScrollPane = new JScrollPane();
		contentScrollPane.setBorder(new EmptyBorder(3, 3, 3, 3));
		getContentPane().add(contentScrollPane, BorderLayout.CENTER);

		JPanel contentPanel = new JPanel();
		contentPanel.setBorder(null);
		contentScrollPane.setViewportView(contentPanel);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

		JPanel templatePanel = new JPanel();
		templatePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
				"General Template Information", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		contentPanel.add(templatePanel);
		templatePanel.setLayout(new BoxLayout(templatePanel, BoxLayout.Y_AXIS));

		Box boxA = Box.createHorizontalBox();
		templatePanel.add(boxA);
		boxA.setBorder(new EmptyBorder(3, 3, 3, 3));

		JLabel lblTemplateTitel = new JLabel("Temp. Name:");
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

		Box boxC = Box.createHorizontalBox();
		boxC.setBorder(new EmptyBorder(3, 3, 3, 3));
		templatePanel.add(boxC);

		JLabel typelbl = new JLabel("Type:");
		typelbl.setPreferredSize(new Dimension(90, 16));
		boxC.add(typelbl);

		typeComboBox = new JComboBox();
		typeComboBox
				.addItem(new ItemContainer<IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal>(
						IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.NOTDEFINED,
						IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.NOTDEFINED.name()));
		typeComboBox
				.addItem(new ItemContainer<IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal>(
						IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.PSET_OCCURRENCEDRIVEN,
						IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.PSET_OCCURRENCEDRIVEN
								.name()));
		typeComboBox
				.addItem(new ItemContainer<IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal>(
						IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.PSET_PERFORMANCEDRIVEN,
						IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.PSET_PERFORMANCEDRIVEN
								.name()));
		typeComboBox
				.addItem(new ItemContainer<IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal>(
						IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.PSET_TYPEDRIVENONLY,
						IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.PSET_TYPEDRIVENONLY
								.name()));
		typeComboBox
				.addItem(new ItemContainer<IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal>(
						IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.PSET_TYPEDRIVENOVERRIDE,
						IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.PSET_TYPEDRIVENOVERRIDE
								.name()));
		typeComboBox
				.addItem(new ItemContainer<IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal>(
						IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.QTO_OCCURRENCEDRIVEN,
						IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.QTO_OCCURRENCEDRIVEN
								.name()));
		typeComboBox
				.addItem(new ItemContainer<IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal>(
						IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.QTO_TYPEDRIVENONLY,
						IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.QTO_TYPEDRIVENONLY
								.name()));
		typeComboBox
				.addItem(new ItemContainer<IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal>(
						IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.QTO_TYPEDRIVENOVERRIDE,
						IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal.QTO_TYPEDRIVENOVERRIDE
								.name()));

		boxC.add(typeComboBox);
		
		Box boxApplicableType = Box.createHorizontalBox();
		templatePanel.add(boxApplicableType);
		boxApplicableType.setBorder(new EmptyBorder(3, 3, 3, 3));

		JLabel lblApplicableType = new JLabel("Applicable Classes:");
		lblApplicableType.setPreferredSize(new Dimension(90, 16));
		boxApplicableType.add(lblApplicableType);

		applicableTypeTextField = new JTextField("IfcObjectDefinition");
		applicableTypeTextField.setMaximumSize(new Dimension(2147483647, 22));
		boxApplicableType.add(applicableTypeTextField);
		applicableTypeTextField.setColumns(10);

		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.setBorder(new EmptyBorder(3, 3, 3, 3));
		templatePanel.add(horizontalBox);

		JLabel librarylbl = new JLabel("Library:");
		librarylbl.setPreferredSize(new Dimension(90, 16));
		horizontalBox.add(librarylbl);

		libraryComboBox = new JComboBox();

		// Sort parent keys
		String[] assoMapKeys = new String[libraryMap.keySet().size()];
		libraryMap.keySet().toArray(assoMapKeys);
		Arrays.sort(assoMapKeys, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});

		for (String key : assoMapKeys) {
			libraryComboBox.addItem(libraryMap.get(key));
		}

		horizontalBox.add(libraryComboBox);

		JPanel propertyPanel = new JPanel();
		propertyPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Property Information",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		contentPanel.add(propertyPanel);
		propertyPanel.setLayout(new BorderLayout(0, 0));

		JPanel propertyValuePanel = new JPanel();
		propertyValuePanel.setPreferredSize(new Dimension(10, 200));
		propertyValuePanel.setMaximumSize(new Dimension(32767, 200));
		propertyPanel.add(propertyValuePanel, BorderLayout.CENTER);
		propertyValuePanel.setLayout(new BorderLayout());

		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// Adding template propertie table
				JFXPanel templatePanel = new JFXPanel();
				templatePanel.setBorder(new LineBorder(Color.GRAY));
				// templatePanelMarginBorder.add(templatePanel, BorderLayout.NORTH);
				templatePanel.setLayout(new BorderLayout(0, 0));

				templatePanel.addMouseListener(new PopUpMouseHandler());

				templateTable = new TreeTableView();
				templateTable.setShowRoot(false);
				templateTable.setRoot(new TreeItem<Object[]>());
				templateTable.setEditable(true);

				TreeTableColumn<Object[], String> nameColumn = new TreeTableColumn<Object[], String>("NAME");
				nameColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Object[], String>, //
						ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object[], String> param) {
						TreeItem<Object[]> treeItem = param.getValue();
						Object[] emp = treeItem.getValue();
						Object value = emp[0];
						return new SimpleObjectProperty<String>(value.toString());
					}
					
				});
				nameColumn.setCellFactory(new Callback<TreeTableColumn<Object[],String>, TreeTableCell<Object[],String>>() {
					@Override
					public TreeTableCell<Object[], String> call(TreeTableColumn<Object[], String> param) {
						return new CustomTextFieldTableCell<Object[]>();
					}
				});
				nameColumn.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Object[], String>>() {

					@Override
					public void handle(TreeTableColumn.CellEditEvent<Object[], String> event) {
						TreeItem<Object[]> item = event.getRowValue();
						Object[] emp = item.getValue();
						String newValue = event.getNewValue();
						emp[0] = newValue;

						//System.out.println("Single column commit. new Value:" + newValue);
					}
					
				});

				nameColumn.setPrefWidth(200);
				templateTable.getColumns().add(nameColumn);

				TreeTableColumn<Object[], String> descColumn = new TreeTableColumn<Object[], String>("DESCRIPTION");				
				descColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Object[], String>, //
						ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object[], String> param) {
						TreeItem<Object[]> treeItem = param.getValue();
						Object[] emp = treeItem.getValue();
						Object value = emp[1];
						
						//Prevent the Nullpointer that sometimes occurs
						if(value == null) {
							emp[1] = "";
							value = "";
						}
						
						return new SimpleObjectProperty<String>(value.toString());
					}
				});
				descColumn.setCellFactory(new Callback<TreeTableColumn<Object[],String>, TreeTableCell<Object[],String>>() {
					@Override
					public TreeTableCell<Object[], String> call(TreeTableColumn<Object[], String> param) {
						return new CustomTextFieldTableCell<Object[]>();
					}
				});
				//descColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
				descColumn.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Object[], String>>() {

					@Override
					public void handle(TreeTableColumn.CellEditEvent<Object[], String> event) {
						TreeItem<Object[]> item = event.getRowValue();
						Object[] emp = item.getValue();
						String newValue = event.getNewValue();
						emp[1] = newValue;

						//System.out.println("Single column commit. new Value:" + newValue);
					}
				});
				descColumn.setOnEditCancel(new EventHandler<TreeTableColumn.CellEditEvent<Object[], String>>() {

					@Override
					public void handle(TreeTableColumn.CellEditEvent<Object[], String> event) {
						if(event.getNewValue() == null) {
							return;
						}
						
						TreeItem<Object[]> item = event.getRowValue();
						Object[] emp = item.getValue();
						String newValue = event.getNewValue();
						emp[1] = newValue;

						//System.out.println("Single column commit. new Value:" + newValue);
					}
				});


				descColumn.setPrefWidth(200);
				templateTable.getColumns().add(descColumn);

				
				TreeTableColumn<Object[], Object> psdTypeColumn = new TreeTableColumn<Object[], Object>("PSD TYPE");
				psdTypeColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Object[], Object>, //
						ObservableValue<Object>>() {

					@Override
					public ObservableValue<Object> call(TreeTableColumn.CellDataFeatures<Object[], Object> param) {
						TreeItem<Object[]> treeItem = param.getValue();
						Object[] emp = treeItem.getValue();
						Object value = emp[3];
						return new SimpleObjectProperty<Object>(value.toString());
					}
				});

				//psdTypeColumn.setCellFactory(ComboBoxTreeTableCell.forTreeTableColumn(psdItems));
				
				psdTypeColumn.setCellFactory(new Callback<TreeTableColumn<Object[],Object>, TreeTableCell<Object[],Object>>() {
					@Override
					public TreeTableCell<Object[], Object> call(TreeTableColumn<Object[], Object> param) {
						CustomComboboxCell comboboxCell = new CustomComboboxCell<Object[]>();
						return (TreeTableCell<Object[], Object>)comboboxCell;
					}
				});
				
				
				psdTypeColumn.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Object[], Object>>() {

					@Override
					public void handle(TreeTableColumn.CellEditEvent<Object[], Object> event) {
						TreeItem<Object[]> item = event.getRowValue();
						Object[] emp = item.getValue();
						String newValue = event.getNewValue().toString();
						emp[3] = newValue;
						
						Object typeObj = emp[2];
						if(typeObj != null) {
							if(IfcSimplePropertyTemplateTypeEnum.Ifc4.IfcSimplePropertyTemplateTypeEnum_internal.valueOf(typeObj.toString())
									.equals(IfcSimplePropertyTemplateTypeEnum.Ifc4.IfcSimplePropertyTemplateTypeEnum_internal.P_ENUMERATEDVALUE)) {									
								emp[3] = "IfcLabel";
							}
						}
						
						refreshTemplateTable();

						//System.out.println("Single column commit. new Value:" + newValue);
					}
				});
				

				String[] items = { "P_SINGLEVALUE", "P_ENUMERATEDVALUE", "P_BOUNDEDVALUE", "P_LISTVALUE",
						"P_TABLEVALUE", "P_REFERENCEVALUE", "Q_LENGTH", "Q_AREA", "Q_VOLUME", "Q_COUNT", "Q_WEIGHT",
						"Q_TIME" };

				TreeTableColumn<Object[], String> valueTypeColumn = new TreeTableColumn<Object[], String>("TYPE");
				valueTypeColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Object[], String>, //
						ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object[], String> param) {
						TreeItem<Object[]> treeItem = param.getValue();
						Object[] emp = treeItem.getValue();
						Object value = emp[2];
						return new SimpleObjectProperty<String>(value.toString());
					}
				});

				valueTypeColumn.setCellFactory(ComboBoxTreeTableCell.forTreeTableColumn(items));
				valueTypeColumn.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Object[], String>>() {

					@Override
					public void handle(TreeTableColumn.CellEditEvent<Object[], String> event) {
						if(event.getNewValue() == null) {
							return;
						}
						
						TreeItem<Object[]> item = event.getRowValue();
						Object[] emp = item.getValue();
						String newValue = event.getNewValue();
						emp[2] = newValue;
						
						if(event.getNewValue().equals("P_REFERENCEVALUE")) {
							emp[3] = "IfcPerson";
						}else {
							emp[3] = "IfcLabel";
						}
						
						if(IfcSimplePropertyTemplateTypeEnum.Ifc4.IfcSimplePropertyTemplateTypeEnum_internal.valueOf(newValue)
								.equals(IfcSimplePropertyTemplateTypeEnum.Ifc4.IfcSimplePropertyTemplateTypeEnum_internal.P_ENUMERATEDVALUE)) {									
							emp[3] = "IfcLabel";
						}
						
						refreshTemplateTable();

						//System.out.println("Single column commit. new Value:" + newValue);
					}
				});
				
				valueTypeColumn.setPrefWidth(100);
				templateTable.getColumns().add(valueTypeColumn);

				psdTypeColumn.setPrefWidth(100);
				templateTable.getColumns().add(psdTypeColumn);


				TreeTableColumn<Object[], String> unitColumn = new TreeTableColumn<Object[], String>("Unit");
				unitColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Object[], String>, //
						ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object[], String> param) {
						TreeItem<Object[]> treeItem = param.getValue();
						Object[] emp = treeItem.getValue();
						Object value = emp[4];
						if(value != null) {
							return new SimpleObjectProperty<String>(value.toString());
						}
						return new SimpleObjectProperty<String>("");
					}
				});
				unitColumn.setPrefWidth(150);
				templateTable.getColumns().add(unitColumn);
				
				TreeTableColumn<Object[], String> additionalDataColumn = new TreeTableColumn<Object[], String>("Additional Data");
				additionalDataColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Object[], String>, //
						ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object[], String> param) {
						TreeItem<Object[]> treeItem = param.getValue();
						Object[] emp = treeItem.getValue();
						Object value = emp[5];
						if(value != null) {
							Object typeObj = emp[2];
							if(typeObj != null) {
								if(IfcSimplePropertyTemplateTypeEnum.Ifc4.IfcSimplePropertyTemplateTypeEnum_internal.valueOf(typeObj.toString())
										.equals(IfcSimplePropertyTemplateTypeEnum.Ifc4.IfcSimplePropertyTemplateTypeEnum_internal.P_ENUMERATEDVALUE)) {									
									return new SimpleObjectProperty<String>(value.toString());
								}
							}
							
						}
						return new SimpleObjectProperty<String>("");
					}
				});
				additionalDataColumn.setPrefWidth(150);
				templateTable.getColumns().add(additionalDataColumn);

				VBox vBox = new VBox();
				vBox.getChildren().setAll(templateTable);
				VBox.setVgrow(templateTable, Priority.ALWAYS);

				Scene scene = new Scene(vBox);

				templatePanel.setScene(scene);

				propertyValuePanel.add(templatePanel, BorderLayout.CENTER);
				
				self.setVisible(true); //set visible after tree table was created
			}
		});

		JPanel propertyMenuPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) propertyMenuPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		propertyPanel.add(propertyMenuPanel, BorderLayout.SOUTH);

		JButton removeButton = new JButton("");
		removeButton.setBorderPainted(false);
		removeButton.setBorder(null);
		removeButton.setDisabledIcon(
				new ImageIcon(
						ImageIO.read(
								CreatePropertySetFrameFX.class.getResource("/icons/del.png")
						).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
					)
				);
		
		removeButton
				.setRolloverIcon(
					new ImageIcon(
						ImageIO.read(
								CreatePropertySetFrameFX.class.getResource("/icons/del_hover.png")
						).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
					)
				);
		removeButton
				.setPressedIcon(
						new ImageIcon(
								ImageIO.read(
										CreatePropertySetFrameFX.class.getResource("/icons/del_hover.png")
								).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
							)
						);
		removeButton.setIcon(
					new ImageIcon(
						ImageIO.read(
								CreatePropertySetFrameFX.class.getResource("/icons/del.png")
						).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
					)
				);
		removeButton.setContentAreaFilled(false);
		removeButton.setHorizontalTextPosition(SwingConstants.CENTER);
		removeButton.setMargin(new Insets(0, 0, 0, 0));
		removeButton.setIconTextGap(0);
		removeButton.setPreferredSize(new Dimension(25, 25));
		removeButton.setMinimumSize(new Dimension(25, 25));
		removeButton.setMaximumSize(new Dimension(25, 25));
		removeButton.setFont(new Font("Tahoma", Font.PLAIN, 13));

		removeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				removePropertyFromTable();
			}
		});

		propertyMenuPanel.add(removeButton);

		JButton addButton = new JButton("");
		addButton.setBorder(null);
		addButton.setBorderPainted(false);
		addButton.setDisabledIcon(
					new ImageIcon(
						ImageIO.read(
								CreatePropertySetFrameFX.class.getResource("/icons/add.png")
						).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
					)
				);
		addButton.setPressedIcon(
					new ImageIcon(
						ImageIO.read(
								CreatePropertySetFrameFX.class.getResource("/icons/add_hover.png")
						).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
					)
				);
		addButton.setRolloverIcon(
					new ImageIcon(
						ImageIO.read(
								CreatePropertySetFrameFX.class.getResource("/icons/add_hover.png")
						).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
					)
				);
		addButton.setIcon(
					new ImageIcon(
						ImageIO.read(
								CreatePropertySetFrameFX.class.getResource("/icons/add.png")
						).getScaledInstance(26, 26, Image.SCALE_SMOOTH)
					)
				);
		addButton.setContentAreaFilled(false);
		addButton.setMargin(new Insets(0, 0, 0, 0));
		addButton.setHorizontalTextPosition(SwingConstants.CENTER);
		addButton.setIconTextGap(0);
		addButton.setMinimumSize(new Dimension(25, 25));
		addButton.setMaximumSize(new Dimension(25, 25));
		addButton.setPreferredSize(new Dimension(25, 25));

		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addPropertyToTable();
			}
		});

		propertyMenuPanel.add(addButton);

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

				if (oldContainer != null) {
					IfcUtilities.removeFromModel(ApplicationUtilities.template, oldContainer.getItem());
				}

				ItemContainer<IfcPropertySetTemplate.Ifc4> templateContainer = createTemplate();
				if(templateContainer == null) {
					System.err.println("Fill in all needed informations (Marked Yellow)");
					return;
				}
				
				TreeItem selectionTreeItem = null;
						
				if (updateMode) {
					oldContainer.setItem(templateContainer.getItem());
					oldContainer.setPsdTypeMap(templateContainer.getPsdTypeMap());
					oldContainer.setDisplay(templateContainer.getDisplay());

				} else {
					
					ItemContainer<Object> libCon = ((ItemContainer<Object>) libraryComboBox.getSelectedItem());

					if (libCon.getItem() instanceof IfcProjectLibrary.Ifc4) {

						TreeItem treeitem = TemplateProjectTreeView.getInstance().getLibByGuid(
								((IfcProjectLibrary.Ifc4) libCon.getItem()).getGlobalId().getDecodedValue());
						selectionTreeItem = new TreeItem<ItemContainer>(templateContainer);
						treeitem.getChildren().add(selectionTreeItem);
					
						//TemplateProjectTreeView.getInstance().getSelectionModel().select(treeitem);
					}

					if (libCon.getItem() instanceof IfcRelAssociatesLibrary.Ifc4) {

						TreeItem treeitem = TemplateProjectTreeView.getInstance().getLibByGuid(
								((IfcRelAssociatesLibrary.Ifc4) libCon.getItem()).getGlobalId().getDecodedValue());
						selectionTreeItem = new TreeItem<ItemContainer>(templateContainer);
						treeitem.getChildren().add(selectionTreeItem);

					}
					
					//libCon.setPsdTypeMap(templateContainer.getPsdTypeMap());

				}
				
				//Refresh in each case
				TemplateProjectTreeView.getInstance().refresh();
				
				if(selectionTreeItem != null) {					
					TemplateProjectTreeView.getInstance().getSelectionModel().select(selectionTreeItem);
				
				}
				
				
				ArrayList<PropertyItem> items = TemplatePropertyTreeView.getInstance().createItems((ItemContainer)templateContainer);
				TreeItem<PropertyItem> root = TemplatePropertyTreeView.getInstance().createTree(items);
				ApplicationFrame.reloadPropertyTree(root);
					
				dispose();
			}

		});

		buttonPanel.add(btnAdd);
	}

	private void initialize() {
		libraryMap = new HashMap<>();
		if (ApplicationUtilities.template != null) {

			// By first method: search for library
			for (IfcProjectLibrary.Ifc4 lib : ApplicationUtilities.template
					.getCollection(IfcProjectLibrary.Ifc4.class)) {
				if (lib.getName() == null) {
					continue;
				}
				String name = lib.getName().toString();

				ItemContainer arr = libraryMap.get(name);
				if (arr != null) {
					continue;
				}

				arr = new ItemContainer<IfcProjectLibrary.Ifc4>(lib, name);
				libraryMap.put(name, arr);
			}

			// By second method: search for reference to library
			for (IfcLibraryReference.Ifc4 libRef : ApplicationUtilities.template
					.getCollection(IfcLibraryReference.Ifc4.class)) {

				if (libRef.getName() == null) {
					continue;
				}
				String name = libRef.getName().toString();

				ItemContainer arr = libraryMap.get(name);
				if (arr != null) {
					continue;
				}

				arr = new ItemContainer<IfcLibraryReference.Ifc4>(libRef, name);
				libraryMap.put(name, arr);

			}
		}
	}

	/**
	 * Check if input is valid.
	 * 
	 * @return
	 */
	private boolean checkInput() {
		boolean flag = true;
		if (templateTitelTextField.getText().length() == 0) {
			templateTitelTextField.setBackground(Color.orange);
			templateTitelTextField.setToolTipText("Name is required.");
			flag = false;
		} else {
			templateTitelTextField.setBackground(Color.white);
			templateTitelTextField.setToolTipText("");
		}

		if (descriptionTextArea.getText().length() == 0) {
			descriptionTextArea.setBackground(Color.orange);
			descriptionTextArea.setToolTipText("Description is required.");
			flag = false;
		} else {
			descriptionTextArea.setBackground(Color.white);
			descriptionTextArea.setToolTipText("");
		}
		return flag;
	}

	/**
	 * Read information from frame and create the IfcModel for template content.
	 */
	private ItemContainer<IfcPropertySetTemplate.Ifc4> createTemplate() {
		
		ItemContainer<IfcPropertySetTemplate.Ifc4> itemContainer = new ItemContainer<IfcPropertySetTemplate.Ifc4>(null, "");

		// check if input is valid
		boolean flag = checkInput();
		if (!flag) {
			return null;
		}

		// create template
		ArrayList<PropertyItem> items = createPropertyListFromTable();

		SET<IfcPropertyTemplate.Ifc4> propertyTemplates = new SET<>();
		for (PropertyItem item : items) {

			// Case of IfcSimpleProperty and IfcPhysicalSimpleQuantity
			if (item.getPropertyType().toUpperCase().contains("P_") || item.getPropertyType().toUpperCase().contains("Q_")) {
				IfcSimplePropertyTemplate simplePropertyTemplate = new IfcSimplePropertyTemplate.Ifc4.Instance();
				simplePropertyTemplate.setGlobalId(new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true));
				simplePropertyTemplate.setDescription(new IfcText.Ifc4(item.getDiscription(), true));
				simplePropertyTemplate.setName(new IfcLabel.Ifc4(item.getName(), true));
				simplePropertyTemplate.setTemplateType(new IfcSimplePropertyTemplateTypeEnum.Ifc4(item.getPropertyType()));
				simplePropertyTemplate.setPrimaryUnit((IfcUnit)item.getUnit());
				simplePropertyTemplate.setAccessState(new IfcStateEnum.Ifc4((IfcStateEnum.Ifc4.IfcStateEnum_internal)item.getAccessState()));
				
				//simplePropertyTemplate.setPrimaryMeasureType(new IfcLabel.Ifc4(item.getPrimaryMeasureType()));

				simplePropertyTemplate.setPrimaryMeasureType(new IfcLabel.Ifc4(item.getValueType()));
				System.out.println("Test: " + item.getValueType());
				simplePropertyTemplate.setSecondaryMeasureType(new IfcLabel.Ifc4(item.getSecondaryMeasureType()));
				
				if(simplePropertyTemplate.getTemplateType().getValue()
						.equals(IfcSimplePropertyTemplateTypeEnum.Ifc4.IfcSimplePropertyTemplateTypeEnum_internal.P_ENUMERATEDVALUE)) {
					simplePropertyTemplate.setEnumerators((IfcPropertyEnumeration)item.getAdditionalData());
				}
								
				propertyTemplates.add((IfcPropertyTemplate.Ifc4) simplePropertyTemplate);
				ApplicationUtilities.template.addObject(simplePropertyTemplate);
				
				itemContainer.addPSDType(simplePropertyTemplate.getGlobalId().getDecodedValue(), item.getValueType());
			}
		}

		IfcPropertySetTemplate.Ifc4 template = new IfcPropertySetTemplate.Ifc4.Instance();
		template.setGlobalId(new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true));
		template.setName(new IfcLabel.Ifc4(templateTitelTextField.getText(), true));
		template.setDescription(new IfcText.Ifc4(descriptionTextArea.getText(), true));
		template.setApplicableEntity(new IfcIdentifier.Ifc4(applicableTypeTextField.getText(), true));

		Object iObj = typeComboBox.getSelectedItem();
		if (iObj instanceof ItemContainer) {
			template.setTemplateType(new IfcPropertySetTemplateTypeEnum.Ifc4(((ItemContainer<IfcPropertySetTemplateTypeEnum.Ifc4.IfcPropertySetTemplateTypeEnum_internal>)iObj).getItem()));
		}
		
		template.setOwnerHistory(ApplicationUtilities.template.getIfcProject().getOwnerHistory());
		template.setHasPropertyTemplates(propertyTemplates);
		ApplicationUtilities.template.addObject(template);
		itemContainer.setItem(template);
		itemContainer.setDisplay(template.getName().toString() + " - (#" + template.getStepLineNumber() + ")");
		
		ItemContainer<Object> libCon = ((ItemContainer<Object>) libraryComboBox.getSelectedItem());

		// Check if a relationship already exists
		/*
		 * if(libCon.getItem().getHasContext_Inverse() == null) {
		 * SET<IfcDefinitionSelect.Ifc4> set = new SET<>(); set.add(template);
		 * 
		 * IfcRelDeclares.Ifc4 declares = new IfcRelDeclares.Ifc4.Instance();
		 * declares.setGlobalId(new
		 * IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true));
		 * declares.setOwnerHistory(ApplicationUtilities.template.getIfcProject().
		 * getOwnerHistory()); declares.setName(new IfcLabel.Ifc4("Library Relation of "
		 * + libCon.getDisplayText(), true)); declares.setRelatedDefinitions(set);
		 * declares.setRelatingContext(libCon.getItem());
		 * ApplicationUtilities.template.addObject(declares);
		 * 
		 * }else { for(IfcRelDeclares.Ifc4 rel :
		 * libCon.getItem().getHasContext_Inverse()) {
		 * rel.addRelatedDefinitions(template); } }
		 */

		if (libCon.getItem() instanceof IfcProjectLibrary.Ifc4) {

			SET<IfcDefinitionSelect.Ifc4> set = new SET<>();
			set.add(template);

			IfcRelDeclares.Ifc4 declares = new IfcRelDeclares.Ifc4.Instance();
			declares.setGlobalId(new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true));
			declares.setOwnerHistory(ApplicationUtilities.template.getIfcProject().getOwnerHistory());
			declares.setName(new IfcLabel.Ifc4("Library Relation of " + libCon.getDisplay(), true));
			declares.setRelatedDefinitions(set);
			declares.setRelatingContext((IfcProjectLibrary.Ifc4) libCon.getItem());
			ApplicationUtilities.template.addObject(declares);

		}

		if (libCon.getItem() instanceof IfcLibraryReference.Ifc4) {

			SET<IfcDefinitionSelect.Ifc4> set = new SET<>();
			set.add(template);

			IfcProjectLibrary.Ifc4 library = new IfcProjectLibrary.Ifc4.Instance();
			library.setGlobalId(new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true));
			library.setDescription(((IfcLibraryReference.Ifc4) libCon.getItem()).getDescription());
			library.setName(((IfcLibraryReference.Ifc4) libCon.getItem()).getName());
			// library.setOwnerHistory(((IfcLibraryReference.Ifc4)libCon.getItem()).getOwnerHistory());
			ApplicationUtilities.template.addObject(library);

			IfcRelDeclares.Ifc4 declares = new IfcRelDeclares.Ifc4.Instance();
			declares.setGlobalId(new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true));
			declares.setOwnerHistory(ApplicationUtilities.template.getIfcProject().getOwnerHistory());
			declares.setName(new IfcLabel.Ifc4("Library Relation of " + libCon.getDisplay(), true));
			declares.setRelatedDefinitions(set);
			declares.setRelatingContext(library);
			ApplicationUtilities.template.addObject(declares);

			// Relationship to Project
			IfcRelDeclares.Ifc4 relDec = new IfcRelDeclares.Ifc4.Instance();
			relDec.setGlobalId(new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true));
			relDec.setName(new IfcLabel.Ifc4(templateTitelTextField.getText() + "_LibRel", true));
			relDec.setDescription(new IfcText.Ifc4(
					"Relation between Project and " + templateTitelTextField.getText() + " Library", true));

			if (ApplicationUtilities.template.getIfcProject() != null) {
				relDec.setRelatingContext((IfcContext.Ifc4) ApplicationUtilities.template.getIfcProject());
			}

			SET<IfcDefinitionSelect.Ifc4> libSet = new SET<>();
			set.add(library);
			relDec.setRelatedDefinitions(libSet);
			ApplicationUtilities.template.addObject(relDec);
		}

		return itemContainer;
	}

	/**
	 * Retrives a ArrayList containing Propertyitem objects with names, type,
	 * discription and values.
	 * 
	 * @return {@link ArrayList}
	 */
	private ArrayList<PropertyItem> createPropertyListFromTable() {
		ArrayList<PropertyItem> valueMap = new ArrayList();
		
		for (TreeItem<Object[]> row : templateTable.getRoot().getChildren()) {

			String name = row.getValue()[0].toString();
			String discription = row.getValue()[1].toString();
			String type = row.getValue()[2].toString();
			String psdType = row.getValue()[3].toString();
			Object unitData = row.getValue()[4]; 
			Object aditionalData = row.getValue()[5]; 
			Object accessState = row.getValue()[6]; 
			Object primaryMeasureType = row.getValue()[7]; 
			Object secondaryMeasureType = row.getValue()[8]; 
			
			PropertyItem pItem = new PropertyItem<String>(name, discription, "", type, psdType);
			pItem.setUnit(unitData);
			if(aditionalData != null) {
				pItem.setAdditionalData(aditionalData);
			}
			
			if(accessState != null) {
				pItem.setAccessState(accessState);
			}
			
			/*
			if(primaryMeasureType != null) {
				pItem.setPrimaryMeasureType(primaryMeasureType.toString());
			}
			 */
			
			if(secondaryMeasureType != null) {
				pItem.setSecondaryMeasureType(secondaryMeasureType.toString());
			} 
			
			valueMap.add(pItem);
		}

		return valueMap;
	}

	private void addPropertyToTable() {
		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				templateTable.getRoot().getChildren().add(new TreeItem<Object[]>(new Object[] { "", "", defaultTypeValue, defaultpsdTypeValue, null, null, IfcStateEnum.Ifc4.IfcStateEnum_internal.READWRITE, "", "" }));
			}
		});
	}

	private void removePropertyFromTable() {
		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				ObservableList<TreeTablePosition<Object[], ?>> rows = templateTable.getSelectionModel().getSelectedCells();

				if (templateTable.getRoot().getChildren().size() > 0) {
					for (TreeTablePosition<Object[], ?> row : rows) {
						templateTable.getRoot().getChildren().remove(row.getTreeItem());
					}
				}
			}
		});
	}

	/**
	 * Sets the predefined context for the frame to replace a existing template.
	 * 
	 * @param oldLibrary
	 */
	public void replaceInstance(ItemContainer<IfcPropertySetTemplate.Ifc4> container) {

		this.oldContainer = container;
		IfcPropertySetTemplate.Ifc4 oldTemplate = (IfcPropertySetTemplate.Ifc4) container.getItem();

		descriptionTextArea.setText(oldTemplate.getDescription().getDecodedValue());
		templateTitelTextField.setText(oldTemplate.getName().getDecodedValue());
		
		if(oldTemplate.getApplicableEntity() != null) {
			applicableTypeTextField.setText(oldTemplate.getApplicableEntity().getDecodedValue());
		}

		for (int i = 0; i < typeComboBox.getItemCount(); i++) {
			if (typeComboBox.getItemAt(i) instanceof ItemContainer<?>) {

				if (((ItemContainer<?>) typeComboBox.getItemAt(i)).getItem()
						.equals(oldTemplate.getTemplateType().value)) {
					typeComboBox.setSelectedItem(typeComboBox.getItemAt(i));
				}

			}
		}

		IfcRelDeclares.Ifc4 rel = oldTemplate.getHasContext_Inverse().get(0);
		libraryComboBox.setSelectedItem(libraryMap.get(rel.getRelatingContext().getName().toString()));

		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				
				for (IfcPropertyTemplate temp : oldTemplate.getHasPropertyTemplates()) {

					if (temp instanceof IfcSimplePropertyTemplate.Ifc4) {
						String name = ((IfcSimplePropertyTemplate.Ifc4) temp).getName().getDecodedValue();
						String description = ((IfcSimplePropertyTemplate.Ifc4) temp).getDescription().getDecodedValue();
						String type = ((IfcSimplePropertyTemplate.Ifc4) temp).getTemplateType().toString();
						String psdType = container.getPSDType(temp.getGlobalId().getDecodedValue());
						
						Object additionalData = null;
						if(type.equals("P_ENUMERATEDVALUE")) {
							additionalData = ((IfcSimplePropertyTemplate.Ifc4)temp).getEnumerators();
						}
						
						Object unit = ((IfcSimplePropertyTemplate.Ifc4)temp).getPrimaryUnit();
						
						Object accessState = IfcStateEnum.Ifc4.IfcStateEnum_internal.READWRITE;
						if(((IfcSimplePropertyTemplate.Ifc4)temp).getAccessState() != null) {
							accessState = ((IfcSimplePropertyTemplate.Ifc4)temp).getAccessState().getValue();
						}
						
						String primaryMeasureType = "";
						String secondaryMeasureType = "";
						
						if(((IfcSimplePropertyTemplate.Ifc4)temp).getPrimaryMeasureType() != null) {
							primaryMeasureType = ((IfcSimplePropertyTemplate.Ifc4)temp).getPrimaryMeasureType().getValue();
						}
						
						if(((IfcSimplePropertyTemplate.Ifc4)temp).getSecondaryMeasureType() != null) {
							secondaryMeasureType = ((IfcSimplePropertyTemplate.Ifc4)temp).getSecondaryMeasureType().getValue();
						}
						
						templateTable.getRoot().getChildren()
								.add(new TreeItem<Object[]>(new Object[] { name, description, type, psdType, unit, additionalData, accessState, primaryMeasureType, secondaryMeasureType}));
					}
				}
				
			}
		});
		
		updateMode = true;
		btnAdd.setText("Update");

		this.repaint();
	}

	public void refreshTemplateTable() {
		templateTable.refresh();
	}

	public JTextField getTemplateTitelTextField() {
		return templateTitelTextField;
	}

	public JTextArea getDescriptionTextArea() {
		return descriptionTextArea;
	}

	public JTextField getApplicableTypeTextField() {
		return applicableTypeTextField;
	}

	public TreeTableView<Object[]> getTemplateTable() {
		return templateTable;
	}

}

package extensions.filtering;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.apstex.gui.core.kernel.Kernel;
import com.apstex.gui.core.model.applicationmodel.ApplicationModelNode;
import com.apstex.gui.core.model.cadobjectmodel.CadObject;
import com.apstex.gui.core.model.selectionmodel.SelectionModelListener;
import com.apstex.ifctoolbox.ifc.IfcProperty;
import com.apstex.ifctoolbox.ifc.IfcPropertySet;
import com.apstex.ifctoolbox.ifc.IfcPropertySingleValue;
import com.apstex.ifctoolbox.ifc.IfcRelDefinesByProperties;
import com.apstex.step.core.ClassInterface;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

/**
 * 
 * @author Marcel Stepien
 *
 */
public class FilterElementFrame extends JFrame{
	
	public enum OPERATOR {
		GLEICH, KLEINER, GROESSER, NICHT	
	};
	
	private class FilterObject{
		private ArrayList<IfcPropertySet> propSet;
		private String prop;
		private OPERATOR operator;
		private String value;
		
		public FilterObject() { }

		public ArrayList<IfcPropertySet> getPropSet() {
			return propSet;
		}

		public String getProp() {
			return prop;
		}

		public OPERATOR getOperator() {
			return operator;
		}

		public String getValue() {
			return value;
		}

		public void setPropSet(ArrayList<IfcPropertySet> propSet) {
			this.propSet = propSet;
		}

		public void setProp(String prop) {
			this.prop = prop;
		}

		public void setOperator(OPERATOR operator) {
			this.operator = operator;
		}

		public void setValue(String value) {
			this.value = value;
		}
		
	}
	
	private static FilterElementFrame self = null;
	private JFXPanel contentPanel = null;
	private FilterObject filterObject = null;
	
	private HashMap<String, ArrayList<IfcPropertySet>> propSetMap = null;  
	
	private JCheckBoxMenuItem showOnlyCheckBox = null;
	
	public static FilterElementFrame getInstance() {
		if(self == null) {
			self = new FilterElementFrame();
		}
		self.setVisible(true);
		return self;
	}
	
	private FilterElementFrame() {	
		this.setTitle("Filter Elements By Properties");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(500, 225);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		
		this.filterObject = new FilterObject();
		
		showOnlyCheckBox = new JCheckBoxMenuItem("Show Only Filtered");
		showOnlyCheckBox.setSelected(false);
		
		JMenu menu = new JMenu("Settings");
		menu.add(showOnlyCheckBox);
		
		JMenuBar menubar = new JMenuBar();
		menubar.add(menu);
		
		this.setJMenuBar(menubar);
		
		
		contentPanel = new JFXPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		//JScrollPane filterScrollPaneFX = new JScrollPane(jfxFilterPanel);
		
		javafx.application.Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				fillInternalMaps();
				
				Label lblLevel1 = new Label("Level 1 (By Property Set):");
				
				ComboBox<String> comboBox1 = new ComboBox<String>(
						new SortedList<>(
								FXCollections.observableArrayList(propSetMap.keySet()),
								new Comparator<String>() {
									@Override
									public int compare(String o1, String o2) {
										return o1.compareTo(o2);
									}
								}
				        )
				);
				comboBox1.setStyle("-fx-pref-width: 310;");
				
				Label lblLevel2 = new Label("Level 2 (By Property):");
				ComboBox<String> comboBox2 = new ComboBox<String>();
				comboBox2.setDisable(true);
				comboBox2.setStyle("-fx-pref-width: 310;");
				
				comboBox1.valueProperty().addListener(new ChangeListener<String>() {
			        @Override 
			        public void changed(ObservableValue ov, String oldVal, String newVal) {
			        	comboBox2.getItems().clear();
			        	
			        	ArrayList<String> newItemList = new ArrayList<>();
			        	for(IfcPropertySet propSet : propSetMap.get(newVal)) {
			        		for(IfcProperty prop : findPropertyFilter(propSet)) {
			        			if(!newItemList.contains(prop.getName().getDecodedValue())) {
			        				newItemList.add(prop.getName().getDecodedValue());
			        			}
			        		}
			        	}
			        	
			        	comboBox2.getItems().addAll(
			        		new SortedList<>(
								FXCollections.observableArrayList(newItemList),
								new Comparator<String>() {
									@Override
									public int compare(String o1, String o2) {
										return o1.compareTo(o2);
									}
								}
			        		)
			        	);
			        	
			        	filterObject.setPropSet(propSetMap.get(newVal));
			        	filterObject.setProp(null);
			        	filterObject.setValue(null);
			        	
			        	comboBox2.setDisable(false);
			        }
			    });
				
				Label lblLevel3 = new Label("Level 3 (By Value):");
				TextField textField3 = new TextField();
				textField3.setDisable(true);
				ComboBox<OPERATOR> comboBox3 = new ComboBox<OPERATOR>(
					FXCollections.observableArrayList(
							OPERATOR.KLEINER,
							OPERATOR.GROESSER,
							OPERATOR.GLEICH,
							OPERATOR.NICHT
					)
				);
				comboBox3.getSelectionModel().select(OPERATOR.GLEICH);
				comboBox3.setDisable(true);
				comboBox3.setConverter(new StringConverter<OPERATOR>() {
					
					@Override
					public String toString(OPERATOR object) {
						return object.name();
					}
					
					@Override
					public OPERATOR fromString(String string) {
						return comboBox3.getItems().stream().filter(ap -> 
			            ap.name().equals(string)).findFirst().orElse(null);
					}
				});
				comboBox3.setStyle("-fx-pref-width: 115;");
				
				comboBox2.valueProperty().addListener(new ChangeListener<String>() {
			        @Override 
			        public void changed(ObservableValue ov, String oldVal, String newVal) {
			        	filterObject.setProp(newVal);
			        	filterObject.setValue(null);
			        	
			        	
			        	comboBox3.setDisable(false);
			        	textField3.setDisable(false);
			        }
			    });
				
				comboBox3.valueProperty().addListener(new ChangeListener<OPERATOR>() {
			        @Override 
			        public void changed(ObservableValue ov, OPERATOR oldVal, OPERATOR newVal) {
			        	filterObject.setOperator(newVal);
			        }
			    });
				filterObject.setOperator(OPERATOR.GLEICH); //Default Option
				
				textField3.textProperty().addListener(new ChangeListener<String>() {
			        @Override 
			        public void changed(ObservableValue ov, String oldVal, String newVal) {
			        	filterObject.setValue(newVal);
			        }
			    });
				
				
				GridPane gridPane = new GridPane();
				gridPane.setHgap(5.0);
				gridPane.setVgap(5.0);
				
			    gridPane.add(lblLevel1, 1, 0);
			    gridPane.add(comboBox1, 2, 0, 2, 1);
			    gridPane.add(lblLevel2, 1, 1);
			    gridPane.add(comboBox2, 2, 1, 2, 1);
			    gridPane.add(lblLevel3, 1, 2);
			    gridPane.add(comboBox3, 2, 2);
			    gridPane.add(textField3, 3, 2);
			    
			    Scene scene = new Scene(gridPane);
				
		        contentPanel.setScene(scene);
			}
		});
		//jfxFilterPanel.addMouseListener(new ApplicationActions.FilterTreeMouseHandler());
		
		this.add(contentPanel, BorderLayout.CENTER);
		
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(new LineBorder(Color.LIGHT_GRAY));
		buttonPanel.setMaximumSize(new Dimension(32767, 60));
		this.add(buttonPanel, BorderLayout.SOUTH);

		JButton btnFilter = new JButton("Filter");
		btnFilter.setPreferredSize(new Dimension(100, 25));
		btnFilter.setMinimumSize(new Dimension(100, 25));
		btnFilter.setMaximumSize(new Dimension(100, 25));

		btnFilter.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				filter(filterObject);
			}

		});

		buttonPanel.add(btnFilter);
	}
	
	private void fillInternalMaps(){
		propSetMap = new HashMap<>();
		
		for(ApplicationModelNode node : Kernel.getApplicationModelRoot().getNodes()) {
			Collection<IfcPropertySet> propSets = node.getStepModel().getCollection(IfcPropertySet.class);
			
			for(IfcPropertySet propSet : propSets) {
			
				ArrayList arrListPropSets = propSetMap.get(propSet.getName().getDecodedValue());
				if(arrListPropSets == null) {
					arrListPropSets = new ArrayList();
					propSetMap.put(propSet.getName().getDecodedValue(), arrListPropSets);
				}
				
				arrListPropSets.add(propSet);
			}
		}
		
		//SortedList<ClassInterface> sortedList = new SortedList<>(arg0);
	}
	
	private ArrayList<IfcProperty> findPropertyFilter(IfcPropertySet propSet){
		return new ArrayList<IfcProperty>(propSet.getHasProperties());
	}
	
	@Override
	public void dispose() {
		this.remove(contentPanel);
		self = null;
		super.dispose();
	}
	
	private void filter(FilterObject obj) {
		//System.out.println(obj.getPropSet());
		//System.out.println(obj.getProp());
		//System.out.println(obj.getOperator());
		//System.out.println(obj.getValue());
		
		if(obj == null) {
			return;
		}
		
		if(obj.getPropSet() == null) {
			return;
		}
		
		ArrayList<ClassInterface> filterd = new ArrayList<ClassInterface>();
		for(IfcPropertySet propSet : obj.getPropSet()) {

			ArrayList<ClassInterface> temp = new ArrayList<ClassInterface>();
			boolean flag = false;
			
			if(propSet instanceof IfcPropertySet.Ifc2x3) {
				
				for(IfcRelDefinesByProperties.Ifc2x3 rel : ((IfcPropertySet.Ifc2x3)propSet).getPropertyDefinitionOf_Inverse()) {					
					temp.addAll(rel.getRelatedObjects());
				}
				
				for(IfcProperty.Ifc2x3 prop : ((IfcPropertySet.Ifc2x3)propSet).getHasProperties()) {
					
					if(obj.getProp() == null) {
						flag = true;
						continue;
					}
					
					if(prop.getName().getDecodedValue().equals(obj.getProp())) {
						
						if(obj.getOperator() == null || obj.getValue() == null) {
							flag = true;
							continue;
						}
						
						if(prop instanceof IfcPropertySingleValue.Ifc2x3) {
							if(checkValues(obj.getValue(), ((IfcPropertySingleValue.Ifc2x3)prop).getNominalValue().toString(), obj.getOperator())) {
								flag = true;
							}
						}
						
					}
				}
				
			}
			
			if(propSet instanceof IfcPropertySet.Ifc4) {
				
				for(IfcRelDefinesByProperties.Ifc4 rel : ((IfcPropertySet.Ifc4)propSet).getDefinesOccurrence_Inverse()) {					
					temp.addAll(rel.getRelatedObjects());
				}
				
				for(IfcProperty.Ifc4 prop : ((IfcPropertySet.Ifc4)propSet).getHasProperties()) {
					
					if(obj.getProp() == null) {
						flag = true;
						continue;
					}
					
					if(prop.getName().getDecodedValue().equals(obj.getProp())) {
						
						if(obj.getOperator() == null || obj.getValue() == null) {
							flag = true;
							continue;
						}
						
						if(prop instanceof IfcPropertySingleValue.Ifc4) {
							if(checkValues(obj.getValue(), ((IfcPropertySingleValue.Ifc4)prop).getNominalValue().toString(), obj.getOperator())) {
								flag = true;
							}
						}
						
					}
				}
			}
			
			if(flag) {				
				filterd.addAll(temp);
			}
		}
	
		//System.out.println(filterd.toString());
		selectFiltered(filterd);
		
		if(showOnlyCheckBox.isSelected()) {
			for(ApplicationModelNode node : Kernel.getApplicationModelRoot().getNodes()) {
				Collection<CadObject> cads = node.getCadObjectModel().getCadObjects();
				
				ArrayList<CadObject> sCads = new ArrayList<>();
				for(Object sObj : node.getSelectionModel().getSelectedObjects()) {
					if(sObj instanceof ClassInterface) {						
						sCads.add(node.getCadObjectModel().getCadObject((ClassInterface)sObj));
					}
				}
				//cads.removeAll(node.getSelectionModel().getSelectedObjects());
					
				
				node.getVisibilityModel().setVisible(cads, false, null);
				node.getVisibilityModel().setVisible(sCads, true, null);
				
			}
			
			
		}
	}
	
	private void selectFiltered(ArrayList<ClassInterface> filtered) {
		for(ApplicationModelNode node : Kernel.getApplicationModelRoot().getNodes()) {
			if(!node.getSelectionModel().getSelectedObjects().isEmpty()) {
				node.getSelectionModel().deselect(new SelectionModelListener() {
					
					@Override
					public void objectsSelected(Collection<Object> arg0, boolean arg1) {
						//Do Nothing	
					}
				}, node.getSelectionModel().getSelectedObjects());
			}
			
			node.getSelectionModel().setSelected(new SelectionModelListener() {
				
				@Override
				public void objectsSelected(Collection<Object> arg0, boolean arg1) {
					//Do Nothing
				}
			}, filtered, true);
		}
	}
	
	public boolean checkValues(String val1, String val2, OPERATOR operator) {
		switch(operator) {
		case GLEICH: if(val1.equals(val2)) { return true; } break;
		case GROESSER: return checkBigger_AsNumericValues(val1, val2);
		case KLEINER: return checkSmaler_AsNumericValues(val1, val2);
		case NICHT: if(!val1.equals(val2)) { return true; } break;
		default: break;
		}
		
		return false;
	}
	
	private boolean checkBigger_AsNumericValues(String val1, String val2) {
		try {
		
			Double dVal1 = new Double(val1.replace(",", "."));
			Double dVal2 = new Double(val2.replace(",", "."));
			
			return dVal1.doubleValue() <= dVal2.doubleValue(); 
			
		}catch (Exception e) {
			System.err.println(val1 + " or " + val2 + " could not be cast to number.");
		}
		
		return false;
	}
	
	private boolean checkSmaler_AsNumericValues(String val1, String val2) {

		try {
			
			Double dVal1 = new Double(val1.replace(",", "."));
			Double dVal2 = new Double(val2.replace(",", "."));
			
			return dVal1.doubleValue() >= dVal2.doubleValue(); 
			
		}catch (Exception e) {
			System.err.println(val1 + " or " + val2 + " could not be cast to number.");
		}
		
		return false;
	}
}

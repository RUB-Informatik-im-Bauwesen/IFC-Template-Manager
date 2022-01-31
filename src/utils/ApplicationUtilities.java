package utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.apstex.ifctoolbox.ifcmodel.IfcModel;

/**
 * Some general needed functions.
 * 
 * @author Marcel Stepien
 *
 */
public class ApplicationUtilities {
	
	public static IfcModel model = new IfcModel(true); //default empty model
	public static IfcModel template = null;
	private static int ICONSIZE = 20;
	
	public static String[] getIfcValueTypeList() {
		String[] valueTypes = { "IfcAmountOfSubstanceMeasure", "IfcAreaMeasure", "IfcComplexNumber",
			"IfcContextDependentMeasure", "IfcCountMeasure", "IfcDescriptiveMeasure",
			"IfcElectricCurrentMeasure", "IfcLengthMeasure", "IfcLuminousIntensityMeasure",
			"IfcMassMeasure", "IfcNonNegativeLengthMeasure", "IfcNormalisedRatioMeasure",
			"IfcNumericMeasure", "IfcParameterValue", "IfcPlaneAngleMeasure", "IfcPositiveLengthMeasure",
			"IfcPositivePlaneAngleMeasure", "IfcPositiveRatioMeasure", "IfcRatioMeasure",
			"IfcSolidAngleMeasure", "IfcThermodynamicTemperatureMeasure", "IfcTimeMeasure",
			"IfcVolumeMeasure", "IfcBoolean", "IfcDate", "IfcDateTime", "IfcDuration", "IfcIdentifier",
			"IfcInteger", "IfcLabel", "IfcLogical", "IfcReal", "IfcText", "IfcTime", "IfcPositiveInteger", "IfcTimeStamp",
			"IfcAbsorbedDoseMeasure", "IfcAccelerationMeasure", "IfcAngularVelocityMeasure",
			"IfcAreaDensityMeasure", "IfcCompoundPlaneAngleMeasure", "IfcCurvatureMeasure",
			"IfcDoseEquivalentMeasure", "IfcDynamicViscosityMeasure", "IfcElectricCapacitanceMeasure",
			"IfcElectricChargeMeasure", "IfcElectricConductanceMeasure", "IfcElectricResistanceMeasure",
			"IfcElectricVoltageMeasure", "IfcEnergyMeasure", "IfcForceMeasure", "IfcFrequencyMeasure",
			"IfcHeatFluxDensityMeasure", "IfcHeatingValueMeasure", "IfcIlluminanceMeasure",
			"IfcInductanceMeasure", "IfcIntegerCountRateMeasure", "IfcIonConcentrationMeasure",
			"IfcIsothermalMoistureCapacityMeasure", "IfcKinematicViscosityMeasure", "IfcLinearForceMeasure",
			"IfcLinearMomentMeasure", "IfcLinearStiffnessMeasure", "IfcLinearVelocityMeasure",
			"IfcLuminousFluxMeasure", "IfcLuminousIntensityDistributionMeasure",
			"IfcMagneticFluxDensityMeasure", "IfcMagneticFluxMeasure", "IfcMassDensityMeasure",
			"IfcMassFlowRateMeasure", "IfcMassPerLengthMeasure", "IfcModulusOfElasticityMeasure",
			"IfcModulusOfLinearSubgradeReactionMeasure", "IfcModulusOfRotationalSubgradeReactionMeasure",
			"IfcModulusOfSubgradeReactionMeasure", "IfcMoistureDiffusivityMeasure",
			"IfcMolecularWeightMeasure", "IfcMomentOfInertiaMeasure", "IfcMonetaryMeasure", "IfcPHMeasure",
			"IfcPlanarForceMeasure", "IfcPowerMeasure", "IfcPressureMeasure", "IfcRadioActivityMeasure",
			"IfcRotationalFrequencyMeasure", "IfcRotationalMassMeasure", "IfcRotationalStiffnessMeasure",
			"IfcSectionModulusMeasure", "IfcSectionalAreaIntegralMeasure", "IfcShearModulusMeasure",
			"IfcSoundPowerLevelMeasure", "IfcSoundPowerMeasure", "IfcSoundPressureLevelMeasure",
			"IfcSoundPressureMeasure", "IfcSpecificHeatCapacityMeasure", "IfcTemperatureGradientMeasure",
			"IfcTemperatureRateOfChangeMeasure", "IfcThermalAdmittanceMeasure",
			"IfcThermalConductivityMeasure", "IfcThermalExpansionCoefficientMeasure",
			"IfcThermalResistanceMeasure", "IfcThermalTransmittanceMeasure", "IfcTorqueMeasure",
			"IfcVaporPermeabilityMeasure", "IfcVolumetricFlowRateMeasure", "IfcWarpingConstantMeasure",
			"IfcWarpingMomentMeasure" };
		return valueTypes;
	}
	
	public static JMenuItem createMenuItem(String text, ActionListener listener, InputStream icon) throws IOException {
		JMenuItem item = null;
		if(icon == null) {
			item = new JMenuItem(text);
		}else {			
			item = new JMenuItem(text, new ImageIcon(ImageIO.read(icon).getScaledInstance(ICONSIZE, ICONSIZE, Image.SCALE_SMOOTH)));
		}
		
		item.addActionListener(listener);
		return item;
	}
	
	public static JTextField createTextbox(String text, PropertyChangeListener listener) throws IOException {
		JTextField item = new JTextField(text);
		item.addPropertyChangeListener(listener);
		//item.addActionListener(listener);
		return item;
	}

	public static JButton createButton(String text, ActionListener listener, ImageIcon icon) throws IOException {
		int sizeParam = ICONSIZE;
		int sizeParamOffset = 6;
		
		JButton item = null;
		if(icon == null) {
			item = new JButton();
		}else {			
			ImageIcon imIcon = icon;
			sizeParam = (int)((imIcon.getIconWidth() + imIcon.getIconHeight())/2) + sizeParamOffset;
			item = new JButton(imIcon);
		}
		
		item.setPreferredSize(new Dimension(sizeParam, sizeParam));
		
		item.addActionListener(listener);
		return item;
	}
	
	
	/**
	 * Table cell renderer for displaying content as tooltip.
	 */
	public static class TooltipCellRenderer extends DefaultTableCellRenderer {
		
		public TooltipCellRenderer() {
			//extend the timout of tooltips
			ToolTipManager.sharedInstance().setDismissDelay(60000);
		}
		
	    public Component getTableCellRendererComponent(
	                        JTable table, Object value,
	                        boolean isSelected, boolean hasFocus,
	                        int row, int column) {

	        JLabel c = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	        if(value != null) {
	        	String pathValue = value.toString();
		        c.setToolTipText(pathValue);
		    }
	        return c;
	    }
	}
	

	public static class TextAreaRenderer extends JPanel implements TableCellRenderer {
	   JTextField textarea;
	  
	   public TextAreaRenderer() {
	      textarea = new JTextField();
	      setLayout(new BorderLayout());
	      add(textarea, BorderLayout.CENTER);
	   }
	  
	   public Component getTableCellRendererComponent(JTable table, Object value,
	                                  boolean isSelected, boolean hasFocus,
	                                  int row, int column)
	   {
	      if (isSelected) {
	         setForeground(table.getSelectionForeground());
	         setBackground(table.getSelectionBackground());
	         textarea.setForeground(table.getSelectionForeground());
	         textarea.setBackground(table.getSelectionBackground());
	      } else {
	         setForeground(table.getForeground());
	         setBackground(table.getBackground());
	         textarea.setForeground(table.getForeground());
	         textarea.setBackground(table.getBackground());
	      }
	  
	      textarea.setText((String) value);
	      textarea.setCaretPosition(0);
	      return this;
	   }
	}
	  
	public static class TextAreaEditor extends DefaultCellEditor {
	   protected JTextField textarea;
	  
	   public TextAreaEditor() {
	      super(new JCheckBox());
	      textarea = new JTextField();
	   }
	  
	   public Component getTableCellEditorComponent(JTable table, Object value,
	                                   boolean isSelected, int row, int column) {
		  textarea = new JTextField();
		  textarea.setText((String) value);
		  textarea.getDocument().addDocumentListener(new DocumentListener() {
			
			  public void changedUpdate(DocumentEvent e) { }
			  
			  public void removeUpdate(DocumentEvent e) { 
				  table.setValueAt(textarea.getText(), row, column);					    
			  }
			  
			  public void insertUpdate(DocumentEvent e) {
				  table.setValueAt(textarea.getText(), row, column);
			  }
		  });
		      
	      
	      return textarea;
	   }
	  
	   public Object getCellEditorValue() {
	      return textarea.getText();
	   }
	}

	
	public static class CheckboxRenderer extends JPanel implements TableCellRenderer {
	   JCheckBox checkBox;
	  
	   public CheckboxRenderer() {
		   checkBox = new JCheckBox();
	       setLayout(new BorderLayout());
	       add(checkBox, BorderLayout.CENTER);
	   }
	  
	   public Component getTableCellRendererComponent(JTable table, Object value,
	                                  boolean isSelected, boolean hasFocus,
	                                  int row, int column)
	   {
	      if (isSelected) {
	         setForeground(table.getSelectionForeground());
	         setBackground(table.getSelectionBackground());
	         checkBox.setForeground(table.getSelectionForeground());
	         checkBox.setBackground(table.getSelectionBackground());
	      } else {
	         setForeground(table.getForeground());
	         setBackground(table.getBackground());
	         checkBox.setForeground(table.getForeground());
	         checkBox.setBackground(table.getBackground());
	      }
	      
	      Boolean bVal = new Boolean((String)value);
		  checkBox.setSelected(bVal.booleanValue());  
	      checkBox.setText("Select this Object?");
	      
	      return this;
	   }
	}
	  
	public static class CheckboxEditor extends DefaultCellEditor {
	   protected JCheckBox checkbox;
	  
	   public CheckboxEditor() {
	      super(new JCheckBox());
	      checkbox = new JCheckBox();
	   }
	  
	   public Component getTableCellEditorComponent(JTable table, Object value,
	                                   boolean isSelected, int row, int column) {
		    Boolean bVal = new Boolean((String)value);
		    checkbox = new JCheckBox();
		    checkbox.setSelected(bVal.booleanValue());
		    checkbox.setText("Select this Object?");
		    checkbox.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {					
					table.setValueAt(new Boolean(checkbox.isSelected()).toString(), row, column);
				}
			});
		    
	        return checkbox;
	   }
	  
	   public Object getCellEditorValue() {
	      return new Boolean(checkbox.isSelected()).toString();
	   }
	}

	
}

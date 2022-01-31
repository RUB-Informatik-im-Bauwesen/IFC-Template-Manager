package extensions.coloring;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;

import extensions.coloring.CreateColorRuleFrame.OPERATOR;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import utils.ApplicationUtilities;

/**
 * Contains the PopUp-Menu Content if a template is right clicked.
 * 
 * @author Marcel Stepien
 *
 */
public class ColorSchemaPopupMenuFX extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private TreeItem<Object> item = null;
	private JFrame owner = null;

	public ColorSchemaPopupMenuFX(JFrame owner, String title, TreeItem<Object> item) {
		super(title);
		this.owner = owner;
		this.item = item;
	}

	public void inititializeContent() throws IOException {

		// add action bindings
		if (item.getValue() instanceof ColorSchema) {
			this.add(ApplicationUtilities.createMenuItem("Hinzufuegen", new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					CreateColorRuleFrame colorRuleFrame = new CreateColorRuleFrame(owner);
					colorRuleFrame.setVisible(true);

					ColorRule rule = colorRuleFrame.getColorRule();
					if (rule != null) {
						((ColorSchema) item.getValue()).addRule(rule);
						item.getChildren().add(new TreeItem<Object>(rule));
					}

				}
			}, this.getClass().getResourceAsStream("icons/add.png")));
		}

		this.add(ApplicationUtilities.createMenuItem("Bearbeiten", new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (item.getValue() instanceof ColorSchema) {
					javafx.application.Platform.runLater(new Runnable() {

						@Override
						public void run() {

							TextInputDialog dialog = new TextInputDialog(((ColorSchema) item.getValue()).getTitel());
							dialog.setTitle("Erstelle neues Farbschema");
							dialog.setHeaderText("Bitte Titel des Farbschemas eingeben");
							dialog.setContentText("Titel Eingeben:");
							dialog.setWidth(250);

							// Traditional way to get the response value.
							Optional<String> result = dialog.showAndWait();
							if (result.isPresent()) {
								ColorSchema schema = (ColorSchema) item.getValue();
								schema.setTitel(result.get());
								ColorSchemaManager.getInstance().notifyComboboxes();
								ColorSchemaManager.getInstance().refresh();
							}

						}
					});

				}
				if (item.getValue() instanceof ColorRule) {
					ColorRule oldRule = (ColorRule) item.getValue();

					CreateColorRuleFrame colorRuleFrame = new CreateColorRuleFrame(owner, oldRule.getTitel(),
							oldRule.getPropertySetName(), oldRule.getPropertyName(), OPERATOR.valueOf(oldRule.getOperator()),
							oldRule.getValue(), oldRule.getColor());
					colorRuleFrame.setVisible(true);

					// waiting for input
					ColorRule newRule = colorRuleFrame.getColorRule();
					if (newRule != null) {
						oldRule.setColor(newRule.getColor());
						oldRule.setTitel(newRule.getTitel());
						oldRule.setPropertySetName(newRule.getPropertySetName());
						oldRule.setPropertyName(newRule.getPropertyName());
						oldRule.setOperator(newRule.getOperator());
						oldRule.setValue(newRule.getValue());

						// ColorSchemaManager.getInstance().notifyComboboxes();
						ColorSchemaManager.getInstance().refresh();
					}

				}
			}
		}, this.getClass().getResourceAsStream("icons/add.png")));

		this.add(ApplicationUtilities.createMenuItem("Entfernen", new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (item.getValue() instanceof ColorRule) {
					((ColorSchema)item.getParent().getValue()).getRules().remove(item.getValue());
					item.getParent().getChildren().remove(item);
				}
				if (item.getValue() instanceof ColorSchema) {
					ColorSchemaManager.getInstance().removeColorSchema((ColorSchema)item.getValue());
				}

				ColorSchemaManager.getInstance().refresh();
			}
		}, this.getClass().getResourceAsStream("icons/add.png")));

	}

}

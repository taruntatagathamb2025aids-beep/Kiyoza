package kiyoza.ui;

import kiyoza.dao.IngredientDAO;
import kiyoza.model.Ingredient;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

/** Admin tab: ingredient records with allergen and supplier. */
class IngredientsPanel extends RefreshablePanel {
    private final IngredientDAO dao = new IngredientDAO();

    private final DefaultTableModel model = UiUtil.readOnlyModel("ID", "Name", "Allergen", "Supplier");
    private final JTable table = new JTable(model);
    private List<Ingredient> items = new ArrayList<>();

    private final JTextField nameField = new JTextField(20);
    private final JComboBox<String> allergenBox = new JComboBox<>(new String[]{
            "", "Gluten", "Dairy", "Egg", "Peanuts", "Tree nuts", "Soy", "Fish", "Shellfish", "Sesame"});
    private final JTextField supplierField = new JTextField(20);

    IngredientsPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        allergenBox.setEditable(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedIntoForm();
        });

        JPanel form = new JPanel(new GridBagLayout());
        UiUtil.addRow(form, 0, "Name", nameField);
        UiUtil.addRow(form, 1, "Allergen (blank if none)", allergenBox);
        UiUtil.addRow(form, 2, "Supplier", supplierField);

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");
        addButton.addActionListener(e -> onAdd());
        updateButton.addActionListener(e -> onUpdate());
        deleteButton.addActionListener(e -> onDelete());
        clearButton.addActionListener(e -> clearForm());

        JPanel right = new JPanel(new BorderLayout(6, 6));
        right.add(form, BorderLayout.NORTH);
        right.add(UiUtil.buttonRow(addButton, updateButton, deleteButton, clearButton), BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), right);
        split.setResizeWeight(0.6);
        add(split, BorderLayout.CENTER);
    }

    @Override
    void refresh() {
        try {
            items = dao.findAll();
            model.setRowCount(0);
            for (Ingredient i : items) {
                model.addRow(new Object[]{i.getId(), i.getName(),
                        i.getAllergenType() == null ? "-" : i.getAllergenType(),
                        i.getSupplier() == null ? "" : i.getSupplier()});
            }
            clearForm();
        } catch (Exception e) {
            UiUtil.showError(this, e);
        }
    }

    private void loadSelectedIntoForm() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= items.size()) return;
        Ingredient i = items.get(row);
        nameField.setText(i.getName());
        allergenBox.setSelectedItem(i.getAllergenType() == null ? "" : i.getAllergenType());
        supplierField.setText(i.getSupplier() == null ? "" : i.getSupplier());
    }

    private void clearForm() {
        table.clearSelection();
        nameField.setText("");
        allergenBox.setSelectedIndex(0);
        supplierField.setText("");
    }

    private Ingredient readForm() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Name is required.");
        Ingredient i = new Ingredient();
        i.setName(name);
        i.setAllergenType(UiUtil.text(allergenBox));
        i.setSupplier(supplierField.getText().trim());
        return i;
    }

    private int selectedId() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= items.size()) {
            throw new IllegalStateException("Select an ingredient in the table first.");
        }
        return items.get(row).getId();
    }

    private void onAdd() {
        try {
            dao.add(readForm());
            refresh();
        } catch (Exception e) {
            UiUtil.showError(this, e);
        }
    }

    private void onUpdate() {
        try {
            Ingredient i = readForm();
            i.setId(selectedId());
            dao.update(i);
            refresh();
        } catch (Exception e) {
            UiUtil.showError(this, e);
        }
    }

    private void onDelete() {
        try {
            int id = selectedId();
            if (!UiUtil.confirm(this, "Delete this ingredient?\n"
                    + "It will also be removed from dishes, stock and freshness logs.")) return;
            dao.delete(id);
            refresh();
        } catch (Exception e) {
            UiUtil.showError(this, e);
        }
    }
}

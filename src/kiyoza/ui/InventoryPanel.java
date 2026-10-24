package kiyoza.ui;

import kiyoza.dao.IngredientDAO;
import kiyoza.dao.InventoryDAO;
import kiyoza.model.Ingredient;
import kiyoza.model.Inventory;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** Admin tab: stock levels and expiry dates per ingredient. */
class InventoryPanel extends RefreshablePanel {
    private final InventoryDAO inventoryDao = new InventoryDAO();
    private final IngredientDAO ingredientDao = new IngredientDAO();

    private final DefaultTableModel model =
            UiUtil.readOnlyModel("Ingredient", "Quantity", "Unit", "Expiry", "Last updated", "Status");
    private final JTable table = new JTable(model);
    private List<Inventory> items = new ArrayList<>();

    private final JComboBox<Ingredient> ingredientBox = new JComboBox<>();
    private final JTextField quantityField = new JTextField(10);
    private final JComboBox<String> unitBox = new JComboBox<>(new String[]{"kg", "g", "L", "ml", "pcs"});
    private final JTextField expiryField = new JTextField(10);

    InventoryPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        unitBox.setEditable(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedIntoForm();
        });
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                           boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                if (!selected) {
                    c.setForeground("OK".equals(String.valueOf(value))
                            ? new Color(0x2E7D32) : new Color(0xC62828));
                }
                return c;
            }
        });

        JPanel form = new JPanel(new GridBagLayout());
        UiUtil.addRow(form, 0, "Ingredient", ingredientBox);
        UiUtil.addRow(form, 1, "Quantity", quantityField);
        UiUtil.addRow(form, 2, "Unit", unitBox);
        UiUtil.addRow(form, 3, "Expiry (yyyy-MM-dd)", expiryField);

        JButton saveButton = new JButton("Save / Update Stock");
        JButton deleteButton = new JButton("Delete Record");
        JButton clearButton = new JButton("Clear");
        saveButton.addActionListener(e -> onSave());
        deleteButton.addActionListener(e -> onDelete());
        clearButton.addActionListener(e -> clearForm());

        JPanel right = new JPanel(new BorderLayout(6, 6));
        right.add(form, BorderLayout.NORTH);
        right.add(UiUtil.buttonRow(saveButton, deleteButton, clearButton), BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), right);
        split.setResizeWeight(0.6);
        add(split, BorderLayout.CENTER);
    }

    @Override
    void refresh() {
        try {
            ingredientBox.removeAllItems();
            for (Ingredient i : ingredientDao.findAll()) ingredientBox.addItem(i);
            items = inventoryDao.findAll();
            model.setRowCount(0);
            for (Inventory inv : items) {
                model.addRow(new Object[]{inv.getIngredientName(),
                        String.format("%.2f", inv.getQuantity()), inv.getUnit(),
                        inv.getExpiryDate() == null ? "" : inv.getExpiryDate().toString(),
                        UiUtil.fmt(inv.getLastUpdated()), inv.getStatus()});
            }
            clearForm();
        } catch (Exception e) {
            UiUtil.showError(this, e);
        }
    }

    private void loadSelectedIntoForm() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= items.size()) return;
        Inventory inv = items.get(row);
        for (int i = 0; i < ingredientBox.getItemCount(); i++) {
            if (ingredientBox.getItemAt(i).getId() == inv.getIngredientId()) {
                ingredientBox.setSelectedIndex(i);
                break;
            }
        }
        quantityField.setText(String.valueOf(inv.getQuantity()));
        unitBox.setSelectedItem(inv.getUnit());
        expiryField.setText(inv.getExpiryDate() == null ? "" : inv.getExpiryDate().toString());
    }

    private void clearForm() {
        table.clearSelection();
        quantityField.setText("");
        unitBox.setSelectedIndex(0);
        expiryField.setText("");
    }

    private void onSave() {
        try {
            Ingredient ingredient = (Ingredient) ingredientBox.getSelectedItem();
            if (ingredient == null) throw new IllegalStateException("Add an ingredient first.");

            double quantity;
            try {
                quantity = Double.parseDouble(quantityField.getText().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Quantity must be a number.");
            }
            if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative.");

            String unit = UiUtil.text(unitBox);
            if (unit.isEmpty()) unit = "kg";

            LocalDate expiry = null;
            String expiryText = expiryField.getText().trim();
            if (!expiryText.isEmpty()) {
                try {
                    expiry = LocalDate.parse(expiryText);
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Expiry must look like 2026-12-31.");
                }
            }

            inventoryDao.save(new Inventory(ingredient.getId(), ingredient.getName(), quantity, unit, expiry, null));
            refresh();
        } catch (Exception e) {
            UiUtil.showError(this, e);
        }
    }

    private void onDelete() {
        try {
            int row = table.getSelectedRow();
            if (row < 0 || row >= items.size()) {
                throw new IllegalStateException("Select a stock record in the table first.");
            }
            if (!UiUtil.confirm(this, "Delete the stock record for "
                    + items.get(row).getIngredientName() + "?")) return;
            inventoryDao.delete(items.get(row).getIngredientId());
            refresh();
        } catch (Exception e) {
            UiUtil.showError(this, e);
        }
    }
}

package kiyoza.ui;

import kiyoza.dao.FoodItemDAO;
import kiyoza.dao.IngredientDAO;
import kiyoza.model.FoodItem;
import kiyoza.model.Ingredient;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Admin tab: create, read, update and delete menu items and choose their ingredients. */
class MenuItemsPanel extends RefreshablePanel {
    private final FoodItemDAO foodDao = new FoodItemDAO();
    private final IngredientDAO ingredientDao = new IngredientDAO();

    private final DefaultTableModel model =
            UiUtil.readOnlyModel("ID", "Name", "Category", "Price", "Available", "Ingredients");
    private final JTable table = new JTable(model);
    private List<FoodItem> items = new ArrayList<>();

    private final JTextField nameField = new JTextField(20);
    private final JComboBox<String> categoryBox =
            new JComboBox<>(new String[]{"Starter", "Main", "Dessert", "Beverage"});
    private final JTextField priceField = new JTextField(10);
    private final JTextArea prepArea = new JTextArea(4, 20);
    private final JCheckBox availableBox = new JCheckBox("Available on the menu", true);
    private final DefaultListModel<Ingredient> ingredientModel = new DefaultListModel<>();
    private final JList<Ingredient> ingredientList = new JList<>(ingredientModel);

    MenuItemsPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        categoryBox.setEditable(true);
        prepArea.setLineWrap(true);
        prepArea.setWrapStyleWord(true);
        ingredientList.setVisibleRowCount(6);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedIntoForm();
        });

        JPanel form = new JPanel(new GridBagLayout());
        UiUtil.addRow(form, 0, "Name", nameField);
        UiUtil.addRow(form, 1, "Category", categoryBox);
        UiUtil.addRow(form, 2, "Price (Rs.)", priceField);
        UiUtil.addRow(form, 3, "Preparation", new JScrollPane(prepArea));
        UiUtil.addRow(form, 4, "<html>Ingredients<br>(Ctrl+click<br>for several)</html>",
                new JScrollPane(ingredientList));
        UiUtil.addRow(form, 5, "", availableBox);

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
            ingredientModel.clear();
            for (Ingredient i : ingredientDao.findAll()) ingredientModel.addElement(i);
            items = foodDao.findAll(false);
            model.setRowCount(0);
            for (FoodItem f : items) {
                String names = f.getIngredients().stream().map(Ingredient::getName)
                        .collect(Collectors.joining(", "));
                model.addRow(new Object[]{f.getId(), f.getName(), f.getCategory(), f.getPrice(),
                        f.isAvailable() ? "Yes" : "No", names});
            }
            clearForm();
        } catch (Exception e) {
            UiUtil.showError(this, e);
        }
    }

    private void loadSelectedIntoForm() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= items.size()) return;
        FoodItem f = items.get(row);
        nameField.setText(f.getName());
        categoryBox.setSelectedItem(f.getCategory());
        priceField.setText(f.getPrice().toPlainString());
        prepArea.setText(f.getPrepDetails() == null ? "" : f.getPrepDetails());
        availableBox.setSelected(f.isAvailable());

        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < ingredientModel.size(); i++) {
            for (Ingredient ing : f.getIngredients()) {
                if (ing.getId() == ingredientModel.get(i).getId()) selected.add(i);
            }
        }
        ingredientList.setSelectedIndices(selected.stream().mapToInt(Integer::intValue).toArray());
    }

    private void clearForm() {
        table.clearSelection();
        nameField.setText("");
        categoryBox.setSelectedIndex(0);
        priceField.setText("");
        prepArea.setText("");
        availableBox.setSelected(true);
        ingredientList.clearSelection();
    }

    private FoodItem readForm() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Name is required.");
        String category = UiUtil.text(categoryBox);
        if (category.isEmpty()) throw new IllegalArgumentException("Category is required.");
        BigDecimal price;
        try {
            price = new BigDecimal(priceField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Price must be a number, for example 250 or 249.50.");
        }
        if (price.signum() < 0) throw new IllegalArgumentException("Price cannot be negative.");

        FoodItem f = new FoodItem();
        f.setName(name);
        f.setCategory(category);
        f.setPrice(price);
        f.setPrepDetails(prepArea.getText().trim());
        f.setAvailable(availableBox.isSelected());
        return f;
    }

    private List<Integer> selectedIngredientIds() {
        return ingredientList.getSelectedValuesList().stream().map(Ingredient::getId)
                .collect(Collectors.toList());
    }

    private int selectedId() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= items.size()) {
            throw new IllegalStateException("Select a menu item in the table first.");
        }
        return items.get(row).getId();
    }

    private void onAdd() {
        try {
            foodDao.add(readForm(), selectedIngredientIds());
            refresh();
        } catch (Exception e) {
            UiUtil.showError(this, e);
        }
    }

    private void onUpdate() {
        try {
            FoodItem f = readForm();
            f.setId(selectedId());
            foodDao.update(f, selectedIngredientIds());
            refresh();
        } catch (Exception e) {
            UiUtil.showError(this, e);
        }
    }

    private void onDelete() {
        try {
            int id = selectedId();
            if (!UiUtil.confirm(this, "Delete this menu item permanently?\n"
                    + "(To hide it without deleting, untick \"Available\" and press Update.)")) return;
            foodDao.delete(id);
            refresh();
        } catch (Exception e) {
            UiUtil.showError(this, e);
        }
    }
}

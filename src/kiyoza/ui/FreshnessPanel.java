package kiyoza.ui;

import kiyoza.dao.FreshnessLogDAO;
import kiyoza.dao.IngredientDAO;
import kiyoza.model.Admin;
import kiyoza.model.FreshnessLog;
import kiyoza.model.FreshnessStatus;
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

/** Admin tab: record and review freshness checks. */
class FreshnessPanel extends RefreshablePanel {
    private final FreshnessLogDAO logDao = new FreshnessLogDAO();
    private final IngredientDAO ingredientDao = new IngredientDAO();
    private final Admin admin;

    private final DefaultTableModel model =
            UiUtil.readOnlyModel("Log ID", "Ingredient", "Status", "Checked by", "Checked at", "Remarks");
    private final JTable table = new JTable(model);
    private List<FreshnessLog> logs = new ArrayList<>();

    private final JComboBox<Ingredient> ingredientBox = new JComboBox<>();
    private final JComboBox<FreshnessStatus> statusBox = new JComboBox<>(FreshnessStatus.values());
    private final JTextField remarksField = new JTextField(20);

    FreshnessPanel(Admin admin) {
        this.admin = admin;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel form = new JPanel(new GridBagLayout());
        UiUtil.addRow(form, 0, "Ingredient", ingredientBox);
        UiUtil.addRow(form, 1, "Result", statusBox);
        UiUtil.addRow(form, 2, "Remarks", remarksField);

        JButton recordButton = new JButton("Record Check");
        JButton deleteButton = new JButton("Delete Selected Log");
        recordButton.addActionListener(e -> onRecord());
        deleteButton.addActionListener(e -> onDelete());

        JPanel right = new JPanel(new BorderLayout(6, 6));
        right.add(form, BorderLayout.NORTH);
        right.add(UiUtil.buttonRow(recordButton, deleteButton), BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), right);
        split.setResizeWeight(0.65);
        add(split, BorderLayout.CENTER);
    }

    @Override
    void refresh() {
        try {
            ingredientBox.removeAllItems();
            for (Ingredient i : ingredientDao.findAll()) ingredientBox.addItem(i);
            logs = logDao.findAll();
            model.setRowCount(0);
            for (FreshnessLog l : logs) {
                model.addRow(new Object[]{l.getId(), l.getIngredientName(), l.getStatus().getLabel(),
                        l.getCheckedBy() == null ? "-" : l.getCheckedBy(),
                        UiUtil.fmt(l.getCheckedAt()), l.getRemarks() == null ? "" : l.getRemarks()});
            }
            remarksField.setText("");
        } catch (Exception e) {
            UiUtil.showError(this, e);
        }
    }

    private void onRecord() {
        try {
            Ingredient ingredient = (Ingredient) ingredientBox.getSelectedItem();
            if (ingredient == null) throw new IllegalStateException("Add an ingredient first.");
            FreshnessStatus status = (FreshnessStatus) statusBox.getSelectedItem();
            String remarks = remarksField.getText().trim();
            logDao.add(ingredient.getId(), admin.getId(), status, remarks.isEmpty() ? null : remarks);
            refresh();
        } catch (Exception e) {
            UiUtil.showError(this, e);
        }
    }

    private void onDelete() {
        try {
            int row = table.getSelectedRow();
            if (row < 0 || row >= logs.size()) {
                throw new IllegalStateException("Select a log entry in the table first.");
            }
            if (!UiUtil.confirm(this, "Delete this freshness log entry?")) return;
            logDao.delete(logs.get(row).getId());
            refresh();
        } catch (Exception e) {
            UiUtil.showError(this, e);
        }
    }
}

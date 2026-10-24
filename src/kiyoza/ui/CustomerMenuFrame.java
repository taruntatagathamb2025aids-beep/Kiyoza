package kiyoza.ui;

import kiyoza.dao.FoodItemDAO;
import kiyoza.dao.FreshnessLogDAO;
import kiyoza.dao.RestaurantDAO;
import kiyoza.model.FoodItem;
import kiyoza.model.FreshnessLog;
import kiyoza.model.FreshnessStatus;
import kiyoza.model.Ingredient;
import kiyoza.model.Restaurant;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Customer-facing digital menu: allergens, preparation and the latest freshness checks. */
public class CustomerMenuFrame extends JFrame {
    private static final String NO_FILTER = "None";
    private static final int AUTO_REFRESH_MS = 10_000;

    private final FoodItemDAO foodDao = new FoodItemDAO();
    private final FreshnessLogDAO logDao = new FreshnessLogDAO();
    private final RestaurantDAO restaurantDao = new RestaurantDAO();

    private Restaurant restaurant = new Restaurant(1, "Kiyoza", "", "");
    private Map<Integer, FreshnessLog> latestChecks = new HashMap<>();
    private List<FoodItem> shown = new ArrayList<>();
    private boolean updatingFilter = false;

    private final JLabel titleLabel = new JLabel("Kiyoza");
    private final JLabel addressLabel = new JLabel(" ");
    private final JComboBox<String> allergenBox = new JComboBox<>();
    private final DefaultTableModel model = UiUtil.readOnlyModel("Dish", "Category", "Price", "Freshness");
    private final JTable table = new JTable(model);
    private final JEditorPane detailPane = new JEditorPane("text/html", "");
    private final JLabel statusLabel = new JLabel(" ");
    private final javax.swing.Timer timer = new javax.swing.Timer(AUTO_REFRESH_MS, e -> loadData(true));

    /** @param standalone true when opened by a guest/customer (closing exits the app). */
    public CustomerMenuFrame(boolean standalone) {
        super("Kiyoza - Digital Menu");
        setDefaultCloseOperation(standalone ? EXIT_ON_CLOSE : DISPOSE_ON_CLOSE);
        setSize(980, 580);
        setLocationRelativeTo(null);

        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        JPanel heading = new JPanel(new BorderLayout());
        heading.add(titleLabel, BorderLayout.NORTH);
        heading.add(addressLabel, BorderLayout.SOUTH);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadData(false));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controls.add(new JLabel("Avoid allergen:"));
        controls.add(allergenBox);
        controls.add(refreshButton);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));
        top.add(heading, BorderLayout.WEST);
        top.add(controls, BorderLayout.EAST);

        allergenBox.addActionListener(e -> {
            if (!updatingFilter) applyFilter();
        });

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetail();
        });
        detailPane.setEditable(false);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(table), new JScrollPane(detailPane));
        split.setResizeWeight(0.45);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 12, 4, 12));

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                timer.stop();
            }
        });

        loadData(false);
        timer.start();
    }

    /** Reloads everything from the database. The timer calls this quietly every few seconds. */
    private void loadData(boolean silent) {
        try {
            Restaurant r = restaurantDao.getDefault();
            for (FoodItem f : foodDao.findAll(true)) r.addItem(f);
            latestChecks = logDao.latestByIngredient();
            restaurant = r;

            titleLabel.setText(r.getName());
            String address = r.getAddress() == null ? "" : r.getAddress();
            String phone = (r.getPhone() == null || r.getPhone().isBlank()) ? "" : "  |  " + r.getPhone();
            addressLabel.setText(address.isEmpty() && phone.isEmpty() ? " " : address + phone);

            refreshAllergenChoices();
            applyFilter();
            statusLabel.setText("Last updated "
                    + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        } catch (Exception e) {
            if (silent) {
                statusLabel.setText("Could not refresh: " + e.getMessage());
            } else {
                UiUtil.showError(this, e);
            }
        }
    }

    private void refreshAllergenChoices() {
        updatingFilter = true;
        Object previous = allergenBox.getSelectedItem();
        Set<String> all = new TreeSet<>();
        for (FoodItem f : restaurant.getMenu()) all.addAll(f.getAllergens());
        allergenBox.removeAllItems();
        allergenBox.addItem(NO_FILTER);
        for (String a : all) allergenBox.addItem(a);
        boolean stillValid = previous != null && (NO_FILTER.equals(previous) || all.contains(previous));
        allergenBox.setSelectedItem(stillValid ? previous : NO_FILTER);
        updatingFilter = false;
    }

    private void applyFilter() {
        int keepId = selectedFoodId();
        Object choice = allergenBox.getSelectedItem();
        String allergen = (choice == null || NO_FILTER.equals(choice)) ? null : choice.toString();

        shown = restaurant.getItemsWithoutAllergen(allergen);
        model.setRowCount(0);
        for (FoodItem f : shown) {
            model.addRow(new Object[]{f.getName(), f.getCategory(), "Rs. " + f.getPrice(), overallFreshness(f)});
        }

        int row = -1;
        for (int i = 0; i < shown.size(); i++) {
            if (shown.get(i).getId() == keepId) row = i;
        }
        if (row >= 0) {
            table.setRowSelectionInterval(row, row);
        } else {
            showDetail();
        }
    }

    private int selectedFoodId() {
        int row = table.getSelectedRow();
        return (row >= 0 && row < shown.size()) ? shown.get(row).getId() : -1;
    }

    private String overallFreshness(FoodItem f) {
        if (f.getIngredients().isEmpty()) return "No data";
        boolean unchecked = false;
        boolean nearExpiry = false;
        for (Ingredient i : f.getIngredients()) {
            FreshnessLog log = latestChecks.get(i.getId());
            if (log == null) {
                unchecked = true;
            } else if (log.getStatus() == FreshnessStatus.SPOILED) {
                return "Ingredient issue";
            } else if (log.getStatus() == FreshnessStatus.NEAR_EXPIRY) {
                nearExpiry = true;
            }
        }
        if (nearExpiry) return "Use soon";
        if (unchecked) return "Not yet checked";
        return "All fresh";
    }

    private void showDetail() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= shown.size()) {
            detailPane.setText("<html><body style='font-family:sans-serif'><p><i>Select a dish to see its "
                    + "ingredients, allergens and freshness checks.</i></p></body></html>");
            return;
        }
        FoodItem f = shown.get(row);
        StringBuilder sb = new StringBuilder("<html><body style='font-family:sans-serif'>");
        sb.append("<h2>").append(esc(f.getName())).append("</h2>");
        sb.append("<p><b>Category:</b> ").append(esc(f.getCategory()))
          .append("<br><b>Price:</b> Rs. ").append(f.getPrice()).append("</p>");

        String prep = (f.getPrepDetails() == null || f.getPrepDetails().isBlank())
                ? "Not provided" : f.getPrepDetails();
        sb.append("<p><b>How it is prepared</b><br>").append(esc(prep)).append("</p>");

        Set<String> allergens = f.getAllergens();
        sb.append("<p><b>Allergens:</b> ");
        if (allergens.isEmpty()) {
            sb.append("none listed");
        } else {
            sb.append("<font color='#C62828'>").append(esc(String.join(", ", allergens))).append("</font>");
        }
        sb.append("</p>");

        sb.append("<p><b>Ingredients and latest freshness check</b></p><table cellpadding='3'>");
        for (Ingredient i : f.getIngredients()) {
            FreshnessLog log = latestChecks.get(i.getId());
            String status;
            String color;
            if (log == null) {
                status = "Not yet checked";
                color = "#757575";
            } else {
                status = log.getStatus().getLabel() + " (" + UiUtil.fmt(log.getCheckedAt()) + ")";
                color = log.getStatus() == FreshnessStatus.FRESH ? "#2E7D32"
                        : log.getStatus() == FreshnessStatus.NEAR_EXPIRY ? "#E65100" : "#C62828";
            }
            sb.append("<tr><td>").append(esc(i.getName()));
            if (i.hasAllergen()) sb.append(" <i>(").append(esc(i.getAllergenType())).append(")</i>");
            sb.append("</td><td><font color='").append(color).append("'>")
              .append(esc(status)).append("</font></td></tr>");
        }
        sb.append("</table></body></html>");

        detailPane.setText(sb.toString());
        detailPane.setCaretPosition(0);
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

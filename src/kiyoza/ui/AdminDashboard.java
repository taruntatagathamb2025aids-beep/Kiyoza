package kiyoza.ui;

import kiyoza.model.Admin;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

public class AdminDashboard extends JFrame {

    public AdminDashboard(Admin admin) {
        super("Kiyoza - Admin Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 680);
        setLocationRelativeTo(null);

        MenuItemsPanel menuPanel = new MenuItemsPanel();
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Menu Items", menuPanel);
        tabs.addTab("Ingredients", new IngredientsPanel());
        tabs.addTab("Inventory", new InventoryPanel());
        tabs.addTab("Freshness Checks", new FreshnessPanel(admin));

        // Reload a tab from the database each time it is opened.
        tabs.addChangeListener(e -> {
            Component selected = tabs.getSelectedComponent();
            if (selected instanceof RefreshablePanel) ((RefreshablePanel) selected).refresh();
        });

        JButton customerView = new JButton("Open Customer Menu");
        customerView.addActionListener(e -> new CustomerMenuFrame(false).setVisible(true));
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        top.add(new JLabel("Logged in as " + admin.getName()), BorderLayout.WEST);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttons.add(customerView);
        buttons.add(logout);
        top.add(buttons, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        menuPanel.refresh();
    }
}

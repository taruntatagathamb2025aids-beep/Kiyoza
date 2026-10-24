package kiyoza.ui;

import kiyoza.dao.UserDAO;
import kiyoza.model.Admin;
import kiyoza.model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagLayout;

public class LoginFrame extends JFrame {
    private final UserDAO userDao = new UserDAO();
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);

    public LoginFrame() {
        super("Kiyoza - Smart Food Transparency System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel title = new JLabel("Kiyoza", JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        JLabel subtitle = new JLabel("Know what is on your plate", JLabel.CENTER);

        JPanel header = new JPanel(new BorderLayout());
        header.add(title, BorderLayout.CENTER);
        header.add(subtitle, BorderLayout.SOUTH);

        JPanel form = new JPanel(new GridBagLayout());
        UiUtil.addRow(form, 0, "Email", emailField);
        UiUtil.addRow(form, 1, "Password", passwordField);

        JButton loginButton = new JButton("Staff Login");
        JButton guestButton = new JButton("View Menu as Guest");
        loginButton.addActionListener(e -> onLogin());
        guestButton.addActionListener(e -> {
            dispose();
            new CustomerMenuFrame(true).setVisible(true);
        });

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(UiUtil.buttonRow(loginButton, guestButton), BorderLayout.SOUTH);
        setContentPane(root);

        getRootPane().setDefaultButton(loginButton);
        pack();
        setLocationRelativeTo(null);
    }

    private void onLogin() {
        try {
            User user = userDao.authenticate(emailField.getText(), new String(passwordField.getPassword()));
            if (user == null) {
                UiUtil.info(this, "Invalid email or password.");
                return;
            }
            dispose();
            if (user instanceof Admin) {
                new AdminDashboard((Admin) user).setVisible(true);
            } else {
                new CustomerMenuFrame(true).setVisible(true);
            }
        } catch (Exception e) {
            UiUtil.showError(this, e);
        }
    }
}

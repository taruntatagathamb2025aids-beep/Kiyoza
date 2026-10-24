package kiyoza.ui;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Small helpers shared by the Swing screens. */
final class UiUtil {
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private UiUtil() { }

    static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    static void addRow(JPanel panel, int row, String label, JComponent field) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.gridy = row;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.gridx = 0;
        panel.add(new JLabel(label), g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, g);
    }

    static JPanel buttonRow(JButton... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JButton b : buttons) p.add(b);
        return p;
    }

    /** Text of a (possibly editable) combo box, never null. */
    static String text(JComboBox<?> box) {
        Object o = box.getSelectedItem();
        return o == null ? "" : o.toString().trim();
    }

    static String fmt(LocalDateTime t) {
        return t == null ? "" : t.format(DT);
    }

    static void showError(Component parent, Exception e) {
        String msg = e.getMessage() == null ? e.toString() : e.getMessage();
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Kiyoza", JOptionPane.INFORMATION_MESSAGE);
    }

    static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Please confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}

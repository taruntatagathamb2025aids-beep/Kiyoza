package kiyoza.ui;

import javax.swing.JPanel;

/** A tab that reloads its data from the database whenever it is shown. */
abstract class RefreshablePanel extends JPanel {
    abstract void refresh();
}

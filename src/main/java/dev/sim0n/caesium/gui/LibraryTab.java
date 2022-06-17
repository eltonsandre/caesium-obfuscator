package dev.sim0n.caesium.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import dev.sim0n.caesium.PreRuntime;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class LibraryTab extends JPanel {

    private JTextField librariesField;
    private JButton addButton;
    private JButton removeButton;
    private JPanel contentPanel;
    private JList<String> libList;

    protected DefaultListModel<String> dependenciesListModel;

    private String currentProfile = "user.home";

    public LibraryTab() {
        init();
    }

    void init() {
        dependenciesListModel = new DefaultListModel<>();
        PreRuntime.libraries.forEach(dependenciesListModel::addElement);

        libList.setModel(dependenciesListModel);
        addButton.addActionListener(e -> {
            if (librariesField.getText().length() > 1) {
                String path = librariesField.getText();
                if (!addDependencyPath(path)) {
                    JOptionPane.showMessageDialog(contentPanel, "Could not locate dependency.", "Caesium Dependency",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                final JFileChooser chooser = new JFileChooser(currentProfile);
                final FileNameExtensionFilter filter = new FileNameExtensionFilter("Java File", "jar", "jmod");
                chooser.setFileFilter(filter);
                chooser.setMultiSelectionEnabled(true);
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int result = chooser.showOpenDialog(librariesField);

                if (result == 0) {
                    currentProfile = chooser.getSelectedFile().getParent();
                    File[] libs = chooser.getSelectedFiles();
                    for (File lib : libs) {
                         addDependencyPath(lib.toString());
                    }
                }
            }
        });

        removeButton.addActionListener(e ->
                libList.getSelectedValuesList()
                        .forEach(dependency -> {
                            PreRuntime.libraries.remove(dependency);
                            dependenciesListModel.removeElement(dependency);
                        }));
    }

    protected boolean addDependencyPath(final String path) {
        File file = new File(path);
        if (file.exists()) {
            PreRuntime.libraries.add(path);
            if (!dependenciesListModel.contains(path)) {
                dependenciesListModel.addElement(path);
            }
            librariesField.setText(null);
            return true;
        }
        return false;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        librariesField = new JTextField();
        contentPanel.add(librariesField, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        addButton = new JButton();
        addButton.setText("Add");
        contentPanel.add(addButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        removeButton = new JButton();
        removeButton.setText("Remove");
        contentPanel.add(removeButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(panel1, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        libList = new JList();
        scrollPane1.setViewportView(libList);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

}

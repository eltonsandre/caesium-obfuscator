/*
 * Created by JFormDesigner on Thu Nov 19 22:55:38 CET 2020
 */

package dev.sim0n.caesium.gui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import dev.sim0n.caesium.Caesium;
import dev.sim0n.caesium.PreRuntime;
import dev.sim0n.caesium.exception.CaesiumException;
import dev.sim0n.caesium.manager.MutatorManager;
import dev.sim0n.caesium.mutator.impl.ClassFolderMutator;
import dev.sim0n.caesium.mutator.impl.ControlFlowMutator;
import dev.sim0n.caesium.mutator.impl.LineNumberMutator;
import dev.sim0n.caesium.mutator.impl.LocalVariableMutator;
import dev.sim0n.caesium.mutator.impl.NumberMutator;
import dev.sim0n.caesium.mutator.impl.PolymorphMutator;
import dev.sim0n.caesium.mutator.impl.ReferenceMutator;
import dev.sim0n.caesium.mutator.impl.ShuffleMutator;
import dev.sim0n.caesium.mutator.impl.StringMutator;
import dev.sim0n.caesium.mutator.impl.TrimMutator;
import dev.sim0n.caesium.mutator.impl.crasher.BadAnnotationMutator;
import dev.sim0n.caesium.mutator.impl.crasher.ImageCrashMutator;
import dev.sim0n.caesium.util.Dictionary;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * This entire thing is a mess because it was automatically generated with
 * JFormDesigner
 */

public class CGui {

    private JPanel contentPane;
    private MainPanel mainPanel;
    private MutatorPanel mutatorPanel;
    private ExclusionsPanel exclusionsPanel;

    private LibraryTab libraryTab;

    private JButton runMutateButton;
    private JButton loadProfileButton;
    private JButton saveProfileButton;
    private JLabel configProfileLabel;

    private static String currentProfile = "user.home";
    public CGui() {
        initComponents();
    }

    public static void main(String[] args) throws HeadlessException, IOException {
        PreRuntime.loadJavaRuntime();
        JFrame appFrame = new JFrame("Caesium Obfuscator");
        FlatDarculaLaf.setup();
//        FlatLightLaf.setup();
        appFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        appFrame.setContentPane(new CGui().contentPane);
        appFrame.pack();
        appFrame.setLocationRelativeTo(appFrame.getOwner());
        appFrame.setVisible(true);
    }

    private void initComponents() {
        runMutateButton.setIcon(Icons.loadIconSvgByTheme("runAll"));
        runMutateButton.addActionListener(l -> {
            Caesium caesium = new Caesium();

            File input = new File(mainPanel.inputField.getText());
            try {
                PreRuntime.loadInput(mainPanel.inputField.getText());
            } catch (CaesiumException e1) {
                e1.printStackTrace();
            }

            PreRuntime.loadClassPath();
            PreRuntime.buildInheritance();
            if (!input.exists()) {
                JOptionPane.showMessageDialog(contentPane, "Unable to find input file", "", JOptionPane.WARNING_MESSAGE);
                return;
            }

            File parent = new File(input.getParent());
            File output = new File(mainPanel.outputField.getText());

            if (output.exists()) {
                // we do it this way so we don't have to loop through a specified x amount of times
                for (int i = 0; i < parent.listFiles().length; i++) {
                    String filePath = String.format("%s.BACKUP-%d", output.getAbsoluteFile(), i);
                    File file = new File(filePath);

                    if (!file.exists() && output.renameTo(new File(filePath))) {
                        output = new File(mainPanel.outputField.getText());
                        break;
                    }
                }
            }

            try {
                caesium.setDictionary(Dictionary.values()[mainPanel.dictionaryComboBox.getSelectedIndex()]);

                MutatorManager mutatorManager = caesium.getMutatorManager();
                // string
                StringMutator stringMutator = mutatorManager.getMutator(StringMutator.class);

                stringMutator.setEnabled(mutatorPanel.stringLiteralcheckBox.isSelected());


                Enumeration<String> elements = exclusionsPanel.exclusionStringsModel.elements();

                while (elements.hasMoreElements()) {
                    stringMutator.getExclusions().add(elements.nextElement());
                }

                mutatorManager.getMutator(BadAnnotationMutator.class).setEnabled(mutatorPanel.imageCrashMutatorCheckBox.isSelected());

                mutatorManager.getMutator(ControlFlowMutator.class).setEnabled(mutatorPanel.controlFlowMutatorCheckBox.isSelected());
                mutatorManager.getMutator(NumberMutator.class).setEnabled(mutatorPanel.numberMutatorCheckBox.isSelected());

                mutatorManager.getMutator(PolymorphMutator.class).setEnabled(mutatorPanel.polymorphMutatorCheckBox.isSelected());

                mutatorManager.getMutator(ImageCrashMutator.class).setEnabled(mutatorPanel.imageCrashMutatorCheckBox.isSelected());
                mutatorManager.getMutator(ClassFolderMutator.class).setEnabled(mutatorPanel.classFolderMutatorCheckBox.isSelected());

                mutatorManager.getMutator(TrimMutator.class).setEnabled(mutatorPanel.trimMutatorCheckBox.isSelected());
                mutatorManager.getMutator(ShuffleMutator.class).setEnabled(mutatorPanel.shuffleMutatorCheckBox.isSelected());

                int referenceMutatorIndex = mutatorPanel.referenceMutatorComboBox.getSelectedIndex();
                if (referenceMutatorIndex > 0) {
                    ReferenceMutator mutator = mutatorManager.getMutator(ReferenceMutator.class);
                    mutator.setEnabled(true);
                }

                int lineNumberMutatorIndex = mutatorPanel.lineNumberMutatorComboBox.getSelectedIndex();
                if (lineNumberMutatorIndex > 0) {
                    LineNumberMutator mutator = mutatorManager.getMutator(LineNumberMutator.class);
                    mutator.setType(lineNumberMutatorIndex - 1);
                    mutator.setEnabled(true);
                }

                int localVariableMutatorIndex = mutatorPanel.localVariableMutatorComboBox.getSelectedIndex();
                if (localVariableMutatorIndex > 0) {
                    LocalVariableMutator mutator = mutatorManager.getMutator(LocalVariableMutator.class);
                    mutator.setType(localVariableMutatorIndex - 1);
                    mutator.setEnabled(true);
                }

                if (caesium.run(input, output) != 0) {
                    Caesium.getLogger().warn("Exited with non default exit code.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        loadProfileButton.setToolTipText("Load a profile");
        loadProfileButton.setIcon(Icons.loadIconSvgByTheme("outgoingChangesOn"));
        loadProfileButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(currentProfile);
            FileFilter configLoad = new FileNameExtensionFilter("Properties File", "properties");
            chooser.setFileFilter(configLoad);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            int response = chooser.showOpenDialog(loadProfileButton);

            if (response == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                currentProfile = file.getAbsolutePath();
                try (Reader reader = new FileReader(file)) {
                    Properties properties = new Properties();
                    properties.load(reader);

                    loadProfile(properties);
                    configProfileLabel.setText("<html><i>" + file.getName());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        saveProfileButton.setToolTipText("Save current profile");
        saveProfileButton.setIcon(Icons.loadIconSvgByTheme("menu-saveall"));
        saveProfileButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(".");
            FileFilter jarFileFilter = new FileNameExtensionFilter("Properties File", "properties");
            chooser.setFileFilter(jarFileFilter);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            int response = chooser.showSaveDialog(saveProfileButton);
            if (response == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try (Writer writer = new FileWriter(file)) {
                    Properties properties = new Properties();
                    saveConfigProfile(properties);
                    properties.store(writer, "Caesium Profile");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

        });
    }

    private void saveConfigProfile(final Properties properties) throws IOException {
        properties.put("main.input", mainPanel.inputField.getText());
        properties.put("main.output", mainPanel.outputField.getText());
        properties.put("main.applicationType", mainPanel.applicationTypeComboBox.getSelectedItem().toString());
        properties.put("main.dictionary", mainPanel.dictionaryComboBox.getSelectedItem().toString());

        properties.put("mutator.stringLiteralMutation", String.valueOf(mutatorPanel.stringLiteralcheckBox.isSelected()));
        properties.put("mutator.controlFlowMutation", String.valueOf(mutatorPanel.controlFlowMutatorCheckBox.isSelected()));
        properties.put("mutator.numberMutation", String.valueOf(mutatorPanel.numberMutatorCheckBox.isSelected()));

        properties.put("mutator.referenceMutation", mutatorPanel.referenceMutatorComboBox.getSelectedItem().toString());
        properties.put("mutator.localVariableTables", mutatorPanel.localVariableMutatorComboBox.getSelectedItem().toString());
        properties.put("mutator.lineNumberTables", mutatorPanel.lineNumberMutatorComboBox.getSelectedItem().toString());
        properties.put("mutator.polymorph", String.valueOf(mutatorPanel.polymorphMutatorCheckBox.isSelected()));
        properties.put("mutator.crasher", String.valueOf(mutatorPanel.imageCrashMutatorCheckBox.isSelected()));
        properties.put("mutator.classFolder", String.valueOf(mutatorPanel.classFolderMutatorCheckBox.isSelected()));
        properties.put("mutator.trimmer", String.valueOf(mutatorPanel.trimMutatorCheckBox.isSelected()));
        properties.put("mutator.shufflerMembers", String.valueOf(mutatorPanel.shuffleMutatorCheckBox.isSelected()));

        properties.put("exclusion.strings", joinString(exclusionsPanel.exclusionStringsModel.elements()));

        properties.put("dependencies.paths", joinString(libraryTab.dependenciesListModel.elements()));
    }

    String joinString(Enumeration<String> enumeration) {
        StringBuilder stringbuffer = new StringBuilder();
        while (enumeration.hasMoreElements()) {
            stringbuffer.append(enumeration.nextElement());
            if (enumeration.hasMoreElements()) {
                stringbuffer.append(",");
            }
        }
        return stringbuffer.toString();
    }

    private void loadProfile(Properties properties) {
        Optional.ofNullable(properties.get("main.input"))
                .ifPresent(value -> mainPanel.inputField.setText((String) value));
        Optional.ofNullable(properties.get("main.output"))
                .ifPresent(value -> mainPanel.outputField.setText((String) value));
        Optional.ofNullable(properties.get("main.applicationType"))
                .ifPresent(value -> mainPanel.applicationTypeComboBox.setSelectedItem(value));
        Optional.ofNullable(properties.get("main.dictionary"))
                .ifPresent(value -> mainPanel.dictionaryComboBox.setSelectedItem(value));

        Optional.ofNullable(properties.get("mutator.stringLiteralMutation"))
                .ifPresent(value -> mutatorPanel.stringLiteralcheckBox.setSelected(Boolean.parseBoolean(value.toString())));
        Optional.ofNullable(properties.get("mutator.controlFlowMutation"))
                .ifPresent(value -> mutatorPanel.controlFlowMutatorCheckBox.setSelected(Boolean.parseBoolean(value.toString())));
        Optional.ofNullable(properties.get("mutator.numberMutation"))
                .ifPresent(value -> mutatorPanel.numberMutatorCheckBox.setSelected(Boolean.parseBoolean(value.toString())));
        Optional.ofNullable(properties.get("mutator.referenceMutation"))
                .ifPresent(value -> mutatorPanel.referenceMutatorComboBox.setSelectedItem(value));
        Optional.ofNullable(properties.get("mutator.localVariableTables"))
                .ifPresent(value -> mutatorPanel.localVariableMutatorComboBox.setSelectedItem(value));
        Optional.ofNullable(properties.get("mutator.lineNumberTables"))
                .ifPresent(value -> mutatorPanel.lineNumberMutatorComboBox.setSelectedItem(value));
        Optional.ofNullable(properties.get("mutator.polymorph"))
                .ifPresent(value -> mutatorPanel.polymorphMutatorCheckBox.setSelected(Boolean.parseBoolean(value.toString())));
        Optional.ofNullable(properties.get("mutator.crasher"))
                .ifPresent(value -> mutatorPanel.imageCrashMutatorCheckBox.setSelected(Boolean.parseBoolean(value.toString())));
        Optional.ofNullable(properties.get("mutator.classFolder"))
                .ifPresent(value -> mutatorPanel.classFolderMutatorCheckBox.setSelected(Boolean.parseBoolean(value.toString())));
        Optional.ofNullable(properties.get("mutator.trimmer"))
                .ifPresent(value -> mutatorPanel.trimMutatorCheckBox.setSelected(Boolean.parseBoolean(value.toString())));
        Optional.ofNullable(properties.get("mutator.shufflerMembers"))
                .ifPresent(value -> mutatorPanel.shuffleMutatorCheckBox.setSelected(Boolean.parseBoolean(value.toString())));

        Optional.ofNullable(properties.get("exclusion.strings"))
                .ifPresent(value -> Arrays.stream(((String) value).split(","))
                        .filter(Objects::nonNull)
                        .filter(it -> !it.isEmpty())
                        .filter(it -> libraryTab.dependenciesListModel.indexOf(it) == -1)
                        .forEach(exclusionsPanel.exclusionStringsModel::addElement));

        Optional.ofNullable(properties.get("dependencies.paths"))
                .ifPresent(value -> Arrays.stream(((String) value).split(","))
                        .filter(Objects::nonNull)
                        .filter(it -> !it.isEmpty())
                        .filter(it -> libraryTab.dependenciesListModel.indexOf(it) == -1)
                        .forEach(libraryTab::addDependencyPath));
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
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 5, new Insets(5, 5, 5, 5), -1, -1));
        contentPane.setPreferredSize(new Dimension(600, 450));
        final JTabbedPane tabbedPane1 = new JTabbedPane();
        contentPane.add(tabbedPane1, new GridConstraints(0, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        tabbedPane1.addTab("Main", panel1);
        mainPanel = new MainPanel();
        panel1.add(mainPanel.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        tabbedPane1.addTab("Mutator", panel2);
        mutatorPanel = new MutatorPanel();
        panel2.add(mutatorPanel.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        tabbedPane1.addTab("Exclusions", panel3);
        exclusionsPanel = new ExclusionsPanel();
        panel3.add(exclusionsPanel.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        tabbedPane1.addTab("Dependencies", panel4);
        libraryTab = new LibraryTab();
        panel4.add(libraryTab.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        runMutateButton = new JButton();
        runMutateButton.setText("");
        contentPane.add(runMutateButton, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loadProfileButton = new JButton();
        loadProfileButton.setText("");
        contentPane.add(loadProfileButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveProfileButton = new JButton();
        saveProfileButton.setText("");
        contentPane.add(saveProfileButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        configProfileLabel = new JLabel();
        configProfileLabel.setText("");
        contentPane.add(configProfileLabel, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        contentPane.add(spacer1, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}

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
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

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
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(5, 5, 5, 5), -1, -1));
        contentPane.setPreferredSize(new Dimension(600, 450));
        final JTabbedPane tabbedPane1 = new JTabbedPane();
        contentPane.add(tabbedPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
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
        final LibraryTab nestedForm1 = new LibraryTab();
        panel4.add(nestedForm1.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        runMutateButton = new JButton();
        runMutateButton.setText("Button");
        contentPane.add(runMutateButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}

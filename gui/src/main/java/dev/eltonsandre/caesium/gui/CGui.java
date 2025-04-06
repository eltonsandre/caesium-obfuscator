/*
 * Created by JFormDesigner on Thu Nov 19 22:55:38 CET 2020
 */

package dev.eltonsandre.caesium.gui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import dev.eltonsandre.caesium.Caesium;
import dev.eltonsandre.caesium.CaesiumConfig;
import dev.eltonsandre.caesium.MutatorRunner;
import dev.eltonsandre.caesium.PreRuntime;
import dev.eltonsandre.caesium.SynchronizedByteArrayOutputStreamWrapper;
import dev.eltonsandre.caesium.TextAreaAppender;
import dev.eltonsandre.caesium.exception.CaesiumException;
import dev.eltonsandre.caesium.util.Dictionary;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.layout.PatternLayout;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This entire thing is a mess because it was automatically generated with
 * JFormDesigner
 */
@Log4j2
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
    private ClassPathPanel classpathPanel;
    private LoggerPanel loggerPanel;
    private JTabbedPane tabbedPane;
    private JScrollPane loggerScrollPane;
    private JButton stopButton;
    private static JFrame appFrame;

    private String currentProfile = "/home/elton/dados/develop/Caesium/.dev/obfuscation-settings.caesium";
    //    private String currentProfile = "user.home";
    private Thread runThread;

    public CGui() {
        initComponents();
        startLog();
    }

    public static void main(String[] args) throws HeadlessException, IOException {
        try {
            PreRuntime.loadJavaRuntime();
        } catch (CaesiumException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Load Java Runtime Error", JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> {
            appFrame = new JFrame("Caesium Obfuscator");
            URL resource = CGui.class.getClassLoader().getResource("icons/logo.png");
            if (resource != null) {
                appFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(resource));
            }
            initTheme();

            appFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            appFrame.setContentPane(new CGui().contentPane);
            appFrame.pack();
            appFrame.setLocationRelativeTo(appFrame.getOwner());
            appFrame.setVisible(true);

        });
    }

    private void startLog() {
        PatternLayout.Builder layoutbuilder = PatternLayout.newBuilder().withPattern("%d{HH:mm:ss.SSS} %-5level - %msg%n");
        // "%-5level
        // %logger{36} - %msg%n"
        TextAreaAppender appender = new TextAreaAppender("textLog", null, layoutbuilder.build(), 200, false, null);
        appender.setTextArea(loggerPanel.getLoggerTextArea());
        Logger logger = (Logger) LogManager.getRootLogger();
        appender.start();
        logger.addAppender(appender);

        SynchronizedByteArrayOutputStreamWrapper rawout = new SynchronizedByteArrayOutputStreamWrapper();
        // Set new stream for System.out
        System.setOut(new PrintStream(rawout, true));
        // Console thread
        Thread consoleThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                String pendingConsoleOutput = new String(rawout.readEmpty());
                loggerPanel.getLoggerTextArea().append(pendingConsoleOutput);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        });
        consoleThread.start();
    }

    private static void initTheme() {
        AtomicBoolean themeLigth = new AtomicBoolean(false);
        File file = new File("caesium.properties");
        try (Reader reader = new FileReader(file)) {
            Properties properties = new Properties();
            properties.load(reader);
            Optional.ofNullable(properties.get("theme.light"))
                    .ifPresent(value -> themeLigth.set(Boolean.parseBoolean(properties.getProperty("theme.light"))));
        } catch (Exception ignored) {
        }

        JFrame.setDefaultLookAndFeelDecorated(true);
        if (themeLigth.get()) {
            FlatLightLaf.setup();
        } else {
            FlatDarculaLaf.setup();
        }
    }

    private void initComponents() {
        stopButton.setVisible(false);
        stopButton.setText("Stop");
        stopButton.setToolTipText("Stop");
        stopButton.setIcon(Icons.loadIconSvgByTheme("stop"));

        runMutateButton.setText("Run mutate");
        runMutateButton.setToolTipText("Run mutate");
        runMutateButton.setActionCommand("Running...");
        runMutateButton.setIcon(Icons.loadIconSvgByTheme("runAll"));
        runMutateButton.addActionListener(l -> {
                    tabbedPane.setSelectedIndex(5);
                    loggerPanel.getLoggerTextArea().requestFocus();

                    changeRunMutatorButton(false, "Running...");

                    SwingUtilities.invokeLater(() -> {
                        try {
                            runThread = new Thread(() -> {
                                try {
                                    runMutate();
                                } finally {
                                    changeRunMutatorButton(true, "Run mutate");
                                }
                            });
                            runThread.start();
                        } catch (Exception exception) {
                            changeRunMutatorButton(true, "Run mutate");
                        }
                    });
                }
        );
        stopButton.addActionListener(l -> {
            Caesium.STOPED_MUTATOR.set(true);
            log.info("Stoped by user.");
        });

        loadProfileButton.setText("Load a profile");
        loadProfileButton.setToolTipText("Load a profile");
        loadProfileButton.setIcon(Icons.loadIconSvgByTheme("outgoingChangesOn"));
        loadProfileButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(currentProfile);
            FileFilter configLoad = new FileNameExtensionFilter("Caesium files", "caesium", "config", "properties");
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

        saveProfileButton.setText("Save profile");
        saveProfileButton.setToolTipText("Save current profile");
        saveProfileButton.setIcon(Icons.loadIconSvgByTheme("menu-saveall"));
        saveProfileButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(currentProfile);
            FileFilter jarFileFilter = new FileNameExtensionFilter("Caesium flies", "caesium", "config", "properties");
            chooser.setFileFilter(jarFileFilter);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            int response = chooser.showSaveDialog(saveProfileButton);
            if (response == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try (Writer writer = new FileWriter(file)) {
                    Properties properties = new Properties();
                    saveConfigProfile(properties);
                    properties.store(writer, "Caesium Profile");
                    currentProfile = file.getAbsolutePath();
                    configProfileLabel.setText("<html><i>" + file.getName());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

        });
    }

    private void changeRunMutatorButton(final boolean enabled, final String text) {
        runMutateButton.setEnabled(enabled);
        runMutateButton.setText(text);
        stopButton.setVisible(!enabled);

        JScrollBar vertical = loggerScrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    private void runMutate() {
        val configBuilder = CaesiumConfig.builder()
                .input(mainPanel.inputField.getText())
                .output(mainPanel.outputField.getText())
                .applicationType(mainPanel.applicationTypeComboBox.getSelectedItem().toString())

                .dictionary(Dictionary.values()[mainPanel.dictionaryComboBox.getSelectedIndex()]);

        val mutator = CaesiumConfig.MutatorConfig.builder()
                .classFolder(mutatorPanel.classFolderMutatorCheckBox.isSelected())
                .controlFlow(mutatorPanel.controlFlowMutatorCheckBox.isSelected())
                .crasher(mutatorPanel.imageCrashMutatorCheckBox.isSelected())
                .number(mutatorPanel.numberMutatorCheckBox.isSelected())
                .polymorph(mutatorPanel.polymorphMutatorCheckBox.isSelected())
                .shufflerMembers(mutatorPanel.shuffleMutatorCheckBox.isSelected())
                .stringLiteral(mutatorPanel.stringLiteralcheckBox.isSelected())
                .trimmer(mutatorPanel.trimMutatorCheckBox.isSelected())

                .referenceMutation(mutatorPanel.referenceMutatorComboBox.getSelectedIndex())
                .lineNumberTables(mutatorPanel.lineNumberMutatorComboBox.getSelectedIndex())
                .localVariableTables(mutatorPanel.localVariableMutatorComboBox.getSelectedIndex());

        configBuilder.mutator(mutator.build());

        Enumeration<String> elements = exclusionsPanel.exclusionStringsModel.elements();
        Set<String> exclusions = new HashSet<>();
        while (elements.hasMoreElements()) {
            exclusions.add(elements.nextElement());
        }

        configBuilder.exclusions(exclusions)
                .dependencies(joinString(libraryTab.dependenciesListModel.elements()))
                .classpath(joinString(classpathPanel.classPathListModel.elements()))
                .build();

        try {
            MutatorRunner.run(configBuilder.build());
        } catch (CaesiumException e) {
            JOptionPane.showMessageDialog(contentPane, e.getMessage(), "", JOptionPane.WARNING_MESSAGE);
        }

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
        properties.put("classpath.paths", joinString(classpathPanel.classPathListModel.elements()));
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

        Optional.ofNullable(properties.get("classpath.paths"))
                .ifPresent(value -> Arrays.stream(((String) value).split(","))
                        .filter(Objects::nonNull)
                        .filter(it -> !it.isEmpty())
                        .filter(it -> classpathPanel.classPathListModel.indexOf(it) == -1)
                        .forEach(classpathPanel::addClasspath));
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
        contentPane.setLayout(new GridLayoutManager(2, 6, new Insets(5, 5, 5, 5), -1, -1));
        contentPane.setPreferredSize(new Dimension(600, 450));
        tabbedPane = new JTabbedPane();
        contentPane.add(tabbedPane, new GridConstraints(0, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        tabbedPane.addTab("Main", panel1);
        mainPanel = new MainPanel();
        panel1.add(mainPanel.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        tabbedPane.addTab("Mutator", panel2);
        mutatorPanel = new MutatorPanel();
        panel2.add(mutatorPanel.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        tabbedPane.addTab("Exclusions", panel3);
        exclusionsPanel = new ExclusionsPanel();
        panel3.add(exclusionsPanel.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        tabbedPane.addTab("Dependencies", panel4);
        libraryTab = new LibraryTab();
        panel4.add(libraryTab.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        tabbedPane.addTab("Classpath", panel5);
        classpathPanel = new ClassPathPanel();
        panel5.add(classpathPanel.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Log", panel6);
        loggerScrollPane = new JScrollPane();
        panel6.add(loggerScrollPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        loggerPanel = new LoggerPanel();
        loggerScrollPane.setViewportView(loggerPanel.$$$getRootComponent$$$());
        runMutateButton = new JButton();
        runMutateButton.setText("");
        contentPane.add(runMutateButton, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        stopButton = new JButton();
        stopButton.setText("");
        contentPane.add(stopButton, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}

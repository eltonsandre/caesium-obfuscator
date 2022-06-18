package dev.sim0n.caesium;

import dev.sim0n.caesium.exception.CaesiumException;
import dev.sim0n.caesium.util.OSUtil;
import dev.sim0n.caesium.util.classwriter.ClassTree;
import dev.sim0n.caesium.util.wrapper.impl.ClassWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PreRuntime {

    private static final Map<String, ClassWrapper> classPath = new HashMap<>();
    private static final Map<String, ClassWrapper> classes = new HashMap<>();
    private static final Map<String, ClassTree> hierarchy = new HashMap<>();
    public static final Set<String> libraries = new LinkedHashSet<>();
    public static final Set<String> classPaths = new LinkedHashSet<>();

    public static void loadJavaRuntime() throws HeadlessException, IOException {
        loadJavaRuntime(null);
    }

    public static void loadJavaRuntime(Component component) throws HeadlessException, IOException {

        if (Double.parseDouble(System.getProperty("java.vm.specification.version")) > 1.8) {
            File jmods = new File(System.getProperty("java.home") + "/jmods");
            if (jmods.exists() && jmods.listFiles() != null) {
                classPaths.add(jmods.getAbsolutePath());
                return;
            }
        }

        String path = System.getProperty("sun.boot.class.path");
        switch (OSUtil.getCurrentOS()) {
            case WINDOWS:
                if (path != null) {
                    String[] pathFiles = path.split(";");
                    for (String lib : pathFiles) {
                        if (lib.endsWith(".jar") || lib.endsWith(".jmod")) {
                            libraries.add(lib);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(component, "rt.jar was not found, you need to add it manually.",
                            "Runtime Error", JOptionPane.ERROR_MESSAGE);
                }
                break;

            case UNIX:
            case MAC:
                if (path != null) {
                    String[] pathFiles = path.split(":");
                    for (String lib : pathFiles) {
                        if (lib.endsWith(".jar") || lib.endsWith(".jmod")) {
                            libraries.add(lib);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(component, "rt.jar was not found, you need to add it manually.",
                            "Runtime Error", JOptionPane.ERROR_MESSAGE);
                }
                break;
            default:
                break;
        }
    }

    public static void loadInput(String inputFile) throws CaesiumException {
        File input = new File(inputFile);
        if (input.exists()) {
            // Logger.info(String.format("Loading input \"%s\".", input.getAbsolutePath()));
            try {
                ZipFile zipFile = new ZipFile(input);
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (!entry.isDirectory()) {
                        if (entry.getName().endsWith(".class")) {
                            try {
                                ClassReader cr = new ClassReader(zipFile.getInputStream(entry));
                                ClassNode classNode = new ClassNode();
                                cr.accept(classNode, ClassReader.SKIP_FRAMES);
                                ClassWrapper classWrapper = new ClassWrapper(classNode, false);
                                classPath.put(classWrapper.originalName, classWrapper);
                                classes.put(classWrapper.originalName, classWrapper);
                            } catch (Throwable ignored) {

                            }
                        }
                    }
                }
                zipFile.close();
            } catch (ZipException e) {

                throw new CaesiumException(
                        String.format("Input file \"%s\" could not be opened as a zip file.", input.getAbsolutePath()),
                        e);
            } catch (IOException e) {
                throw new CaesiumException(String.format(
                        "IOException happened while trying to load classes from \"%s\".", input.getAbsolutePath()), e);
            }
        } else {
            throw new CaesiumException(String.format("Unable to find \"%s\".", input.getAbsolutePath()), null);
        }
    }

    public static void loadClassPath() {
        Set<String> dependencies = new LinkedHashSet<>();
        dependencies.addAll(libraries);
        dependencies.addAll(loadJarAndJmodInClasspaths());

        for (String s : dependencies) {
            File file = new File(s);
            if (file.exists()) {
                System.out.printf("Loading library \"%s\".%n", file.getAbsolutePath());
                try {
                    ZipFile zipFile = new ZipFile(file);
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                            try {
                                ClassReader cr = new ClassReader(zipFile.getInputStream(entry));
                                ClassNode classNode = new ClassNode();
                                cr.accept(classNode,
                                        ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                                ClassWrapper classWrapper = new ClassWrapper(classNode, true);

                                classPath.put(classWrapper.originalName, classWrapper);
                            } catch (final Exception ignored) {
                                // Don't care.
                            }
                        }
                    }
                    zipFile.close();
                } catch (ZipException e) {
                    // Logger.info(
                    // String.format("Library \"%s\" could not be opened as a zip file.",
                    // file.getAbsolutePath()));
                    e.printStackTrace();
                } catch (IOException e) {
                    // Logger.info(String.format("IOException happened while trying to load classes
                    // from \"%s\".",
                    // file.getAbsolutePath()));
                    e.printStackTrace();
                }
            } else {
                // Logger.info(String.format("Library \"%s\" could not be found and will be
                // ignored.",
                // file.getAbsolutePath()));
            }

        }
    }

    private static Set<String> loadJarAndJmodInClasspaths() {
        final Set<String> dependencies = new LinkedHashSet<>();
        for (String path : classPaths) {
            try (Stream<Path> walk = Files.walk(Paths.get(path))) {
                dependencies.addAll( walk.map(Path::toString)
                        .filter(file -> file.endsWith(".jar") || file.endsWith(".jmod"))
                        .collect(Collectors.toSet()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dependencies;
    }

    public static void buildHierarchy(ClassWrapper classWrapper, ClassWrapper sub) throws CaesiumException {
        if (hierarchy.get(classWrapper.node.name) == null) {
            ClassTree tree = new ClassTree(classWrapper);
            if (classWrapper.node.superName != null) {
                tree.parentClasses.add(classWrapper.node.superName);
                ClassWrapper superClass = classPath.get(classWrapper.node.superName);
                if (superClass == null)
                    throw new CaesiumException(classWrapper.node.superName + " is missing in the classpath.", null);
                buildHierarchy(superClass, classWrapper);
            }
            if (classWrapper.node.interfaces != null && !classWrapper.node.interfaces.isEmpty()) {
                for (String s : classWrapper.node.interfaces) {
                    tree.parentClasses.add(s);
                    ClassWrapper interfaceClass = classPath.get(s);
                    if (interfaceClass == null)
                        throw new CaesiumException(s + " is missing in the classpath.", null);

                    buildHierarchy(interfaceClass, classWrapper);
                }
            }
            hierarchy.put(classWrapper.node.name, tree);
        }
        if (sub != null) {
            hierarchy.get(classWrapper.node.name).subClasses.add(sub.node.name);
        }
    }

    public static void buildInheritance() {
        classes.values().forEach(classWrapper -> {
            try {
                buildHierarchy(classWrapper, null);
            } catch (CaesiumException e) {
                e.printStackTrace();
            }
        });
    }

    public static Map<String, ClassWrapper> getClassPath() {
        return classPath;
    }

    public static Map<String, ClassWrapper> getClasses() {
        return classes;
    }

    public static Map<String, ClassTree> getHierarchy() {
        return hierarchy;
    }

}

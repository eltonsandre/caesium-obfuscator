package dev.eltonsandre.caesium.manager;

import com.google.common.io.ByteStreams;
import dev.eltonsandre.caesium.Caesium;
import dev.eltonsandre.caesium.exception.CaesiumException;
import dev.eltonsandre.caesium.mutator.impl.ClassFolderMutator;
import dev.eltonsandre.caesium.mutator.impl.crasher.ImageCrashMutator;
import dev.eltonsandre.caesium.util.ByteUtil;
import dev.eltonsandre.caesium.util.classwriter.CaesiumClassWriter;
import dev.eltonsandre.caesium.util.wrapper.impl.ClassWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Log4j2
@Getter
@RequiredArgsConstructor
public class ClassManager {

    private final Caesium caesium = Caesium.getInstance();

    private final MutatorManager mutatorManager;//= caesium.getMutatorManager();

    private final Map<ClassWrapper, String> classes = new HashMap<>();
    private final Map<String, byte[]> resources = new HashMap<>();

    private final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();

    public void parseJar(File input) throws IOException {
        log.info("Loading classes...");

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(input.toPath()))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (Caesium.isStoped()) return;

                byte[] data = ByteStreams.toByteArray(zis);

                String name = entry.getName();

                if (name.endsWith(".class")) {
                    ClassNode classNode = ByteUtil.parseClassBytes(data);

                    classes.put(new ClassWrapper(classNode, false), name);
                } else {
                    if (name.equals("META-INF/MANIFEST.MF")) {
                        String manifest = new String(data);

                        // delete this line
                        manifest = manifest.substring(0, manifest.length() - 2);

                        // watermark the manifest
                        manifest += String.format("Obfuscated-By: Caesium %s\r%n", Caesium.VERSION);

                        data = manifest.getBytes();
                    }

                    resources.put(name, data);
                }
            }
        }

        log.info("Loaded {} classes for mutation", classes.size());
        log.info(Caesium.SEPARATOR);
    }

    public void handleMutation() throws IOException {
        try (ZipOutputStream out = new ZipOutputStream(outputBuffer)) {
            Optional<ImageCrashMutator> imageCrashMutator = Optional.ofNullable(mutatorManager.getMutator(ImageCrashMutator.class));
            Optional<ClassFolderMutator> classFolderMutator = Optional.ofNullable(mutatorManager.getMutator(ClassFolderMutator.class));

            AtomicBoolean hideClasses = new AtomicBoolean(classFolderMutator.isPresent() && classFolderMutator.get().isEnabled());

            imageCrashMutator.ifPresent(crasher -> {
                if (!crasher.isEnabled())
                    return;

                ClassWrapper wrapper = crasher.getCrashClass();

                classes.put(wrapper, String.format("%s.class", wrapper.node.name));
            });

            String styleInline = "width: auto;height:210px;border: 13px solid #bed5cd;overflow-x: scroll;overflow-y: hidden;white-space: nowrap;";
            classes.forEach((node, name) -> {
                if (Caesium.isStoped()) {
                    return;
                }

                mutatorManager.handleMutation(node);

                try {
                    if (hideClasses.get()) // turn them into folders
                        name += "/";

                    out.putNextEntry(new ZipEntry(name));

                    byte[] classBytes;
                    CaesiumClassWriter classWriter = null;
                    classWriter = ByteUtil.getClassWriter(node.node);
                    classBytes = classWriter.toByteArray();
                    out.write(classBytes);
                    if (!classWriter.getMissingInClasspath().isEmpty()) {
                        String message = "<html><b>Couldn't find referencies in classpath:</b><br><div style=" + styleInline + ">" +
                                classWriter.getMissingInClasspath().toString().replace(", ", "<br>") +
                                "</div></html>";

                        log.error(message);
//                        JOptionPane.showMessageDialog(null,message, "Error", JOptionPane.ERROR_MESSAGE);
                    }

                    if (hideClasses.get()) { // generate a bunch of fake classes
                        String finalName = name;
                        SecureRandom random = new SecureRandom();
                        IntStream.range(0, 1 + random.nextInt(10)).forEach(i -> {
                            try {
                                out.putNextEntry(new ZipEntry(String.format("%scaesium_%d.class", finalName, i ^ 27)));
                                out.write(new byte[]{0});
                            } catch (Exception e) {
                                log.error(e);
                            }
                        });
                    }
                } catch (Exception e) {
                    log.error(e);
                }
            });

            resources.forEach((name, data) -> {
                try {
                    out.putNextEntry(new ZipEntry(name));
                    out.write(data);
                } catch (IOException e) {
                    log.error(e);
                }
            });
        }

        mutatorManager.handleMutationFinish();
    }

    /**
     * Exports {@param output}
     *
     * @param output The obfuscated file to export
     * @throws CaesiumException If unable to write output data
     */
    public void exportJar(File output) throws CaesiumException {
        try (FileOutputStream fos = new FileOutputStream(output)) {
            fos.write(outputBuffer.toByteArray());

            log.info("Exported to {}", output.getAbsolutePath());
        } catch (IOException e) {
            throw new CaesiumException("Failed to write output data", e);
        }
    }

}

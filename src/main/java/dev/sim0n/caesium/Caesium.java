package dev.sim0n.caesium;

import com.google.common.base.Strings;
import dev.sim0n.caesium.exception.CaesiumException;
import dev.sim0n.caesium.manager.ClassManager;
import dev.sim0n.caesium.manager.MutatorManager;
import dev.sim0n.caesium.util.ByteUtil;
import dev.sim0n.caesium.util.Dictionary;
import dev.sim0n.caesium.util.VersionUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.var;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.StringFormatterMessageFactory;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
public class Caesium {
    @Getter
    private static final Logger logger = LogManager.getLogger();

    public static final String VERSION = VersionUtil.getVersion();

    private static final String SEPARATOR = Strings.repeat("-", 30);

    private final SecureRandom random = new SecureRandom();

    private static  Caesium instance;

    private final MutatorManager mutatorManager;
    private final ClassManager classManager;

    @Setter
    private Dictionary dictionary = Dictionary.NUMBERS;

    public Caesium() {
        instance =this;
        mutatorManager = new MutatorManager();
        classManager = new ClassManager();
    }

    public int run(File input, File output) throws IOException, CaesiumException {
        checkNotNull(input, "Input can't be null");
        checkNotNull(output, "Output can't be null");

        separator();
        logger.info("Caesium version {}", VERSION);
        separator();

        classManager.parseJar(input);
        classManager.handleMutation();
        classManager.exportJar(output);

        double inputKB = ByteUtil.bytesToKB(input.length());
        double outputKB = ByteUtil.bytesToKB(output.length());

        logger.info("Successfully obfuscated target jar. {}Kb -> {}Kb", inputKB, outputKB);

        return 0;
    }

    public void separator() {
        logger.info(SEPARATOR);
    }

    public static Caesium getInstance() {
        return Optional.of(instance).orElseThrow(() -> new IllegalStateException("Caesium instance is null"));
    }
}

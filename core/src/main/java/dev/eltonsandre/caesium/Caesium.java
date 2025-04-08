package dev.eltonsandre.caesium;

import com.google.common.base.Strings;
import dev.eltonsandre.caesium.exception.CaesiumException;
import dev.eltonsandre.caesium.manager.ClassManager;
import dev.eltonsandre.caesium.manager.MutatorManager;
import dev.eltonsandre.caesium.util.ByteUtil;
import dev.eltonsandre.caesium.util.Dictionary;
import dev.eltonsandre.caesium.util.VersionUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;

@Log4j2
@Getter
public class Caesium {
    @Getter
    private static final Logger logger = LogManager.getLogger();

    public static final String VERSION = VersionUtil.getVersion();

    public static final String SEPARATOR = Strings.repeat("-", 30);

    public final static AtomicBoolean STOPED_MUTATOR = new AtomicBoolean();

    private static Caesium instance;

    private final MutatorManager mutatorManager;
    private final ClassManager classManager;

    @Setter
    private Dictionary dictionary = Dictionary.NUMBERS;

    public Caesium() {
        instance = this;
        mutatorManager = new MutatorManager();
        classManager = new ClassManager(mutatorManager);
    }

    public synchronized static boolean isStoped() {
        return STOPED_MUTATOR.getAcquire();
    }

    public boolean run(File input, File output) throws IOException, CaesiumException {
        checkNotNull(input, "Input can't be null");
        checkNotNull(output, "Output can't be null");

        STOPED_MUTATOR.set(false);

        separator();
        log.info("Caesium version {}", VERSION);
        separator();

        classManager.parseJar(input);
        if (STOPED_MUTATOR.get()) return false;

        classManager.handleMutation();
        if (STOPED_MUTATOR.get()) return false;

        classManager.exportJar(output);

        PreRuntime.loadJavaRuntime(null);

        double inputKB = ByteUtil.bytesToKB(input.length());
        double outputKB = ByteUtil.bytesToKB(output.length());

        log.info("Successfully obfuscated target jar. {}Kb -> {}Kb\n\n", inputKB, outputKB);

        return true;
    }

    private void separator() {
        log.info(SEPARATOR);
    }

    public static Caesium getInstance() {
        return Optional.of(instance).orElseThrow(() -> new IllegalStateException("Caesium instance is null"));
    }
}

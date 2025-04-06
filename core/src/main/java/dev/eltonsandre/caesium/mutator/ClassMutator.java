package dev.eltonsandre.caesium.mutator;

import dev.eltonsandre.caesium.Caesium;
import dev.eltonsandre.caesium.util.StringUtil;
import dev.eltonsandre.caesium.util.trait.Finishable;
import dev.eltonsandre.caesium.util.wrapper.impl.ClassWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.objectweb.asm.Opcodes;

import java.security.SecureRandom;
import java.util.stream.IntStream;

@Log4j2
public abstract class ClassMutator implements Opcodes, Finishable {

    protected final Caesium caesium = Caesium.getInstance();

    protected final SecureRandom random = new SecureRandom();

    protected int counter;

    @Getter
    @Setter
    private boolean enabled = false;

    public abstract void handle(ClassWrapper wrapper);

    /**
     * Generates a random name using {@link SecureRandom#nextInt}
     *
     * @return A random name
     */
    public String getRandomName() {
        switch (caesium.getDictionary()) {
            case ABC_LOWERCASE:
                return StringUtil.getRandomString(3, randomRange(6, 8), false);

            case ABC:
                return StringUtil.getRandomString(3, 6, true);

            case III: {
                StringBuilder sb = new StringBuilder();

                IntStream.range(0, 20).forEach(i -> sb.append(random.nextBoolean() ? "I" : "l"));

                return sb.toString();
            }

            case NUMBERS:
                return String.valueOf(random.nextInt());

            case WACK: {
                StringBuilder sb = new StringBuilder();

                IntStream.range(0, 20).forEach(i -> {
                    if (random.nextBoolean())
                        sb.append("\n");

                    sb.append(random.nextBoolean() ? "I" : "l");
                });

                return sb.toString();
            }

            default:
                return "Unsupported";
        }

    }

    protected int randomRange(int low, int high) {
        return random.nextInt(high - low) + low;
    }

}

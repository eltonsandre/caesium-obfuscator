package dev.eltonsandre.caesium;

import dev.eltonsandre.caesium.util.Dictionary;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Data
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class CaesiumConfig {

    private final String input;
    private final String output;
    private final String applicationType;

    @Builder.Default
    private final Dictionary dictionary = Dictionary.NUMBERS;

    private final MutatorConfig mutator;

    private final String classpath;
    private final String dependencies;
    private final Set<String> exclusions;


    @Data
    @Builder(toBuilder = true)
    @RequiredArgsConstructor
    public static class MutatorConfig {

        private final boolean stringLiteral;
        private final boolean controlFlow;
        private final boolean number;

        private final int lineNumberTables;
        private final int localVariableTables;
        private final int referenceMutation;

        private final boolean polymorph;
        private final boolean crasher;
        private final boolean classFolder;
        private final boolean trimmer;
        private final boolean shufflerMembers;


    }

}

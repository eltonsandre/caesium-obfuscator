package dev.eltonsandre.caesium;

import dev.eltonsandre.caesium.util.Dictionary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CaesiumConfig {

    private String input;
    private String output;

    private boolean notOverrideInput;
    private boolean notOverrideOutput;

    private String applicationType;

    @Builder.Default
    private Dictionary dictionary = Dictionary.NUMBERS;

    private MutatorConfig mutator;

    private String classpath;
    private String dependencies;
    private Set<String> exclusions;


    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MutatorConfig {

        private boolean stringLiteral;
        private boolean controlFlow;
        private boolean number;

        private RemoveOrRename lineNumberTables;
        private RemoveOrRename localVariableTables;
        private ReferenceMutation referenceMutation;

        private boolean polymorph;
        private boolean crasher;
        private boolean classFolder;
        private boolean trimmer;
        private boolean shufflerMembers;

    }

    @Getter
    @RequiredArgsConstructor
    public enum ReferenceMutation {
        off(0),
        light(1),
        normal(2);
        
        private final int value;
    }

    @Getter
    @RequiredArgsConstructor
    public enum RemoveOrRename {
        off(0),
        remove(1),
        rename(2);

        private final int value;
    }

}

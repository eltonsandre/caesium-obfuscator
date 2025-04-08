package dev.eltonsandre.caesium;

import dev.eltonsandre.caesium.util.Dictionary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CaesiumConfig {

    private String loggerFile;
    private String jdkPath;
    private String input;
    private String output;

    private boolean notOverrideInput;
    private boolean notOverrideOutput;

    private String applicationType;

    @Builder.Default
    private Dictionary dictionary = Dictionary.NUMBERS;

    private MutatorConfig mutator;

    @Builder.Default
    private Set<String> classpath = new LinkedHashSet<>();
    @Builder.Default
    private Set<String> dependencies = new LinkedHashSet<>();
    @Builder.Default
    private Set<String> exclusions = new LinkedHashSet<>();


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

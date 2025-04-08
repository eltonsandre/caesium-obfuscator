package dev.eltonsandre.caesium;

import dev.eltonsandre.caesium.exception.CaesiumException;
import dev.eltonsandre.caesium.manager.MutatorManager;
import dev.eltonsandre.caesium.mutator.impl.ClassFolderMutator;
import dev.eltonsandre.caesium.mutator.impl.ControlFlowMutator;
import dev.eltonsandre.caesium.mutator.impl.LineNumberMutator;
import dev.eltonsandre.caesium.mutator.impl.LocalVariableMutator;
import dev.eltonsandre.caesium.mutator.impl.NumberMutator;
import dev.eltonsandre.caesium.mutator.impl.PolymorphMutator;
import dev.eltonsandre.caesium.mutator.impl.ReferenceMutator;
import dev.eltonsandre.caesium.mutator.impl.ShuffleMutator;
import dev.eltonsandre.caesium.mutator.impl.StringMutator;
import dev.eltonsandre.caesium.mutator.impl.TrimMutator;
import dev.eltonsandre.caesium.mutator.impl.crasher.BadAnnotationMutator;
import dev.eltonsandre.caesium.mutator.impl.crasher.ImageCrashMutator;
import lombok.extern.log4j.Log4j2;

import java.io.File;

@Log4j2
public class MutatorRunner {

    public static void run(final CaesiumConfig config) {
        log.info("Config: {}", config.toString().replaceAll(",", ",\n"));
        var input = new File(config.getInput());

        if (!input.exists()) {
            throw new CaesiumException("Unable to find input file");
        }

        if (config.isNotOverrideInput() && input.getParent().equals(config.getOutput())) {
            throw new CaesiumException("Input file and output file are the same. set notOverrideInput to false");
        }

        try {
            PreRuntime.loadInput(config.getInput());
        } catch (CaesiumException e1) {
            e1.printStackTrace();
        }

        PreRuntime.loadClassPath();
        PreRuntime.buildInheritance();

        var parent = new File(input.getParent());
        var output = new File(config.getOutput());

        if (config.isNotOverrideOutput() && output.exists()) {
            // we do it this way so we don't have to loop through a specified x amount of times
            for (int i = 0; i < parent.listFiles().length; i++) {
                String filePath = String.format("%s.BACKUP-%d", output.getAbsoluteFile(), i);
                File file = new File(filePath);

                if (!file.exists() && output.renameTo(new File(filePath))) {
                    output = new File(config.getOutput());
                    break;
                }
            }
        }
        var caesium = new Caesium();
        try {
            caesium.setDictionary(config.getDictionary());

            MutatorManager mutatorManager = caesium.getMutatorManager();
            // string
            StringMutator stringMutator = mutatorManager.getMutator(StringMutator.class);
            stringMutator.setEnabled(config.getMutator().isStringLiteral());

            if (config.getExclusions() != null) {
                stringMutator.getExclusions().addAll(config.getExclusions());
            }

            mutatorManager.getMutator(ClassFolderMutator.class).setEnabled(config.getMutator().isClassFolder());
            mutatorManager.getMutator(ControlFlowMutator.class).setEnabled(config.getMutator().isControlFlow());

            mutatorManager.getMutator(BadAnnotationMutator.class).setEnabled(config.getMutator().isCrasher());
            mutatorManager.getMutator(ImageCrashMutator.class).setEnabled(config.getMutator().isCrasher());

            mutatorManager.getMutator(NumberMutator.class).setEnabled(config.getMutator().isNumber());

            mutatorManager.getMutator(PolymorphMutator.class).setEnabled(config.getMutator().isPolymorph());

            mutatorManager.getMutator(ShuffleMutator.class).setEnabled(config.getMutator().isShufflerMembers());
            mutatorManager.getMutator(TrimMutator.class).setEnabled(config.getMutator().isTrimmer());

            var referenceMutatorIndex = config.getMutator().getReferenceMutation();
            if (referenceMutatorIndex != null && referenceMutatorIndex.getValue() > 0) {
                ReferenceMutator mutator = mutatorManager.getMutator(ReferenceMutator.class);
                mutator.setEnabled(true);
            }

            var lineNumberMutatorIndex = config.getMutator().getLineNumberTables();
            if (lineNumberMutatorIndex != null && lineNumberMutatorIndex.getValue() > 0) {
                LineNumberMutator mutator = mutatorManager.getMutator(LineNumberMutator.class);
                mutator.setType(lineNumberMutatorIndex.getValue() - 1);
                mutator.setEnabled(true);
            }

            var localVariableMutatorIndex = config.getMutator().getLocalVariableTables();
            if (localVariableMutatorIndex != null && localVariableMutatorIndex.getValue() > 0) {
                LocalVariableMutator mutator = mutatorManager.getMutator(LocalVariableMutator.class);
                mutator.setType(localVariableMutatorIndex.getValue() - 1);
                mutator.setEnabled(true);
            }

            if (!caesium.run(input, output)) {
                if (Caesium.isStoped()) log.warn("Stoped by user.");
                else log.warn("Exited with non default exit code.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

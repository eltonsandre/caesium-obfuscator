package dev.eltonsandre.caesium.mutator.impl;

import dev.eltonsandre.caesium.mutator.ClassMutator;
import dev.eltonsandre.caesium.util.wrapper.impl.ClassWrapper;

/**
 * This will turn all classes into directories by append a / to .class
 */
public class ClassFolderMutator extends ClassMutator {
    @Override
    public void handle(ClassWrapper wrapper) {
        ++counter;
    }

    @Override
    public void handleFinish() {
        logger.info("Turned {} classes into folders", counter);
    }
}

package dev.eltonsandre.caesium.mutator.impl;

import dev.eltonsandre.caesium.mutator.ClassMutator;
import dev.eltonsandre.caesium.util.wrapper.impl.ClassWrapper;
import lombok.extern.log4j.Log4j2;

/**
 * This will turn all classes into directories by append a / to .class
 */
@Log4j2
public class ClassFolderMutator extends ClassMutator {
    @Override
    public void handle(ClassWrapper wrapper) {
        ++counter;
    }

    @Override
    public void handleFinish() {
        log.info("Turned {} classes into folders", counter);
    }
}

package dev.eltonsandre.caesium.mutator.impl;

import dev.eltonsandre.caesium.mutator.ClassMutator;
import dev.eltonsandre.caesium.util.wrapper.impl.ClassWrapper;
import lombok.extern.log4j.Log4j2;

import java.util.Collections;

// This will shuffle class members, however this can break reflection
@Log4j2
public class ShuffleMutator extends ClassMutator {
    @Override
    public void handle(ClassWrapper wrapper) {
        Collections.shuffle(wrapper.node.fields, random);
        Collections.shuffle(wrapper.node.methods, random);

        counter += wrapper.fields.size() + wrapper.node.methods.size();
    }

    @Override
    public void handleFinish() {
        log.info("Shuffled {} members", counter);
    }
}

package dev.eltonsandre.caesium.mutator.impl;

import dev.eltonsandre.caesium.mutator.ClassMutator;
import dev.eltonsandre.caesium.util.wrapper.impl.ClassWrapper;
import org.objectweb.asm.tree.ClassNode;

public class AttributeMutator extends ClassMutator {
    public AttributeMutator() {
        setEnabled(true);
    }

    @Override
    public void handle(ClassWrapper wrapper) {
        ClassNode node = wrapper.node;

    }

    @Override
    public void handleFinish() {

    }
}

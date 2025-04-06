package dev.eltonsandre.caesium.util.wrapper.impl;

import dev.eltonsandre.caesium.util.wrapper.Wrapper;
import org.objectweb.asm.tree.FieldNode;

public class FieldWrapper implements Wrapper {
    public final FieldNode node;

    public FieldWrapper(FieldNode node) {
        this.node = node;
    }
}

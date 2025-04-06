package dev.eltonsandre.caesium.util.wrapper.impl;

import dev.eltonsandre.caesium.util.wrapper.Wrapper;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.commons.CodeSizeEvaluator;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

@RequiredArgsConstructor
public class MethodWrapper implements Wrapper {
    public final MethodNode node;

    public int getMaxSize() {
        CodeSizeEvaluator evaluator = new CodeSizeEvaluator(null);
        
        node.accept(evaluator);

        return evaluator.getMaxSize();
    }

    public boolean hasInstructions() {
        return node.instructions != null && node.instructions.size() > 0;
    }

    public InsnList getInstructions() {
        return node.instructions;
    }
}

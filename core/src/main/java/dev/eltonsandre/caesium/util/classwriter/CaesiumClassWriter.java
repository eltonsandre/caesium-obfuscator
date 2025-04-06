package dev.eltonsandre.caesium.util.classwriter;

import dev.eltonsandre.caesium.PreRuntime;
import dev.eltonsandre.caesium.exception.CaesiumException;
import dev.eltonsandre.caesium.exception.CaesiumMissingDependencyException;
import dev.eltonsandre.caesium.util.wrapper.impl.ClassWrapper;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

@Log4j2
public class CaesiumClassWriter extends ClassWriter {

    public static final String OBJECT_REFERENCE_NAME = "java/lang/Object";

    @Getter
    private final Set<String> missingInClasspath;

    public CaesiumClassWriter() {
        this(ClassWriter.COMPUTE_FRAMES);
    }

    public CaesiumClassWriter(int flags) {
        super(flags);
        missingInClasspath = new HashSet<>();
    }

    @Override
    protected String getCommonSuperClass(final String typeFirst, final String typeSecund) {
        if (OBJECT_REFERENCE_NAME.equals(typeFirst) || OBJECT_REFERENCE_NAME.equals(typeSecund))
            return OBJECT_REFERENCE_NAME;

        String first = null;
        try {
            first = deriveCommonSuperName(typeFirst, typeSecund);
        } catch (CaesiumMissingDependencyException e) {
            log.info(e.getMessage());
        }

        String second = null;
        try {
            second = deriveCommonSuperName(typeSecund, typeFirst);
        } catch (CaesiumMissingDependencyException e) {
            log.info(e.getMessage());
        }

        if (!OBJECT_REFERENCE_NAME.equals(first))
            return first;

        if (!OBJECT_REFERENCE_NAME.equals(second))
            return second;

        try {
            return getCommonSuperClass(returnClazz(typeFirst).superName, returnClazz(typeSecund).superName);
        } catch (final CaesiumException caesiumException) {
            log.info(caesiumException.getMessage());
        }

        return OBJECT_REFERENCE_NAME;
    }

    private String deriveCommonSuperName(String typeFirst, String typeSecund) throws CaesiumException {
        ClassNode first = returnClazz(typeFirst);
        ClassNode second = returnClazz(typeSecund);
        if (isAssignableFrom(typeFirst, typeSecund))
            return typeFirst;
        else if (isAssignableFrom(typeSecund, typeFirst))
            return typeSecund;
        else if (Modifier.isInterface(first.access) || Modifier.isInterface(second.access))
            return OBJECT_REFERENCE_NAME;
        else {
            do {
                typeFirst = first.superName;
                first = returnClazz(typeFirst);
            } while (!isAssignableFrom(typeFirst, typeSecund));
            return typeFirst;
        }
    }

    private ClassNode returnClazz(String ref) throws CaesiumMissingDependencyException {
        final ClassWrapper clazz = PreRuntime.getClassPath().get(ref);
        if (clazz == null) {
            missingInClasspath.add(ref);
            log.error("{} does not exist in classpath.", ref);
            throw new CaesiumMissingDependencyException(ref, " does not exist in classpath!");
        }

        return clazz.node;
    }

    private boolean isAssignableFrom(String type1, String type2) throws CaesiumException {
        if (OBJECT_REFERENCE_NAME.equals(type1))
            return true;

        if (type1.equals(type2))
            return true;

        returnClazz(type1);
        returnClazz(type2);
        ClassTree firstTree = getTree(type1);

        if (firstTree == null)
            throw new CaesiumException("Could not find " + type1 + " in the built class hierarchy", null);

        Set<String> allChildren = new HashSet<>();
        Deque<String> toProcess = new ArrayDeque<>(firstTree.subClasses);
        while (!toProcess.isEmpty()) {
            String s = toProcess.poll();
            if (allChildren.add(s)) {
                returnClazz(s);
                ClassTree tempTree = getTree(s);
                toProcess.addAll(tempTree.subClasses);
            }
        }
        return allChildren.contains(type2);
    }

    public ClassTree getTree(String ref) throws CaesiumException {
        if (!PreRuntime.getHierarchy().containsKey(ref)) {
            ClassWrapper wrapper = PreRuntime.getClassPath().get(ref);
            PreRuntime.buildHierarchy(wrapper, null);
        }

        return PreRuntime.getHierarchy().get(ref);
    }

}

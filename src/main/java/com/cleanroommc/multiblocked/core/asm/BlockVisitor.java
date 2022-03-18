package com.cleanroommc.multiblocked.core.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;


public class BlockVisitor implements Opcodes {
    public static final String TARGET_CLASS_NAME = "net/minecraft/block/Block";

    private static final String BLOCK_HOOKS_OWNER = "com/cleanroommc/multiblocked/core/asm/hooks/BlockHooks";
    private static final String BLOCK_HOOKS_SIGNATURE = "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/Boolean;";
    private static final String BLOCK_HOOKS_METHOD_NAME = "doesSideBlockRendering";

    public static ClassNode handleClassNode(ClassNode classNode) {
        for (MethodNode m : classNode.methods) {
            if (m.name.equals(BLOCK_HOOKS_METHOD_NAME)) {
                InsnList toAdd = new InsnList();
                toAdd.add(new VarInsnNode(ALOAD, 2)); // Load world
                toAdd.add(new VarInsnNode(ALOAD, 3)); // Load pos
                // Invoke hook
                toAdd.add(new MethodInsnNode(INVOKESTATIC, BLOCK_HOOKS_OWNER, BLOCK_HOOKS_METHOD_NAME, BLOCK_HOOKS_SIGNATURE, false));
                toAdd.add(new InsnNode(DUP)); // Copy value on stack, avoids need for local var
                toAdd.add(new JumpInsnNode(IFNULL, (LabelNode) m.instructions.getFirst())); // Check if return is null, if it is, jump to vanilla code
                toAdd.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false)); // Otherwise evaluate the bool
                toAdd.add(new InsnNode(IRETURN)); // And return it
                AbstractInsnNode first = m.instructions.getFirst(); // First vanilla instruction
                m.instructions.insertBefore(first, toAdd); // Put this before the first instruction (L1 label node)
                m.instructions.insert(first, new InsnNode(POP)); // Pop the extra value that vanilla doesn't need
                break;
            }
        }
        return classNode;
    }
}

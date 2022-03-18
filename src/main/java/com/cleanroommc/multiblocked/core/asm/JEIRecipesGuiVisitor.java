package com.cleanroommc.multiblocked.core.asm;

import com.cleanroommc.multiblocked.core.asm.util.ObfMapping;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class JEIRecipesGuiVisitor extends MethodVisitor implements Opcodes {
    public static final String TARGET_CLASS_NAME = "mezz/jei/gui/recipes/RecipesGui";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(
            TARGET_CLASS_NAME,
            "func_146274_d",
            "()V");
    private static final ObfMapping METHOD_HOOKS = new ObfMapping("com/cleanroommc/multiblocked/core/asm/hooks/JEIHooks",
            "handleMouseInput",
            "(Lmezz/jei/gui/recipes/RecipesGui;)Z");


    public JEIRecipesGuiVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitCode() {
        Label label = new Label();
        mv.visitVarInsn(ALOAD, 0);
        METHOD_HOOKS.visitMethodInsn(this, INVOKESTATIC);
        mv.visitJumpInsn(IFEQ, label);
        mv.visitInsn(RETURN);
        mv.visitLabel(label);
        super.visitCode();
    }
}

package com.cleanroommc.multiblocked.core.asm;

import com.cleanroommc.multiblocked.core.asm.util.ObfMapping;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CCLBlockRendererDispatcherVisitor extends MethodVisitor implements Opcodes {
    public static final String TARGET_CLASS_NAME = "codechicken/lib/render/block/CCBlockRendererDispatcher";
    public static final ObfMapping TARGET_METHOD_1 = new ObfMapping(
            TARGET_CLASS_NAME,
            "func_175020_a",
            "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/world/IBlockAccess;)V");
    public static final ObfMapping TARGET_METHOD_2 = new ObfMapping(
            TARGET_CLASS_NAME,
            "func_175018_a",
            "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/BufferBuilder;)Z");
    private static final ObfMapping METHOD_HOOKS_1 = new ObfMapping("com/cleanroommc/multiblocked/core/asm/hooks/CCLHooks",
            "renderBlockDamage",
            "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/world/IBlockAccess;)Z");
    private static final ObfMapping METHOD_HOOKS_2 = new ObfMapping("com/cleanroommc/multiblocked/core/asm/hooks/CCLHooks",
            "renderBlock",
            "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/BufferBuilder;)Z");

    int hook;
    public CCLBlockRendererDispatcherVisitor(MethodVisitor mv, int hook) {
        super(Opcodes.ASM5, mv);
        this.hook = hook;
    }

    @Override
    public void visitCode() {
        if (hook == 0) {
            Label label = new Label();
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 4);
            METHOD_HOOKS_1.visitMethodInsn(this, INVOKESTATIC);
            mv.visitJumpInsn(IFEQ, label);
            mv.visitInsn(RETURN);
            mv.visitLabel(label);
            super.visitCode();
        } else if (hook == 1) {
            Label label = new Label();
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 4);
            METHOD_HOOKS_2.visitMethodInsn(this, INVOKESTATIC);
            mv.visitJumpInsn(IFEQ, label);
            mv.visitInsn(ICONST_0);
            mv.visitInsn(IRETURN);
            mv.visitLabel(label);
            super.visitCode();
        }

    }
}

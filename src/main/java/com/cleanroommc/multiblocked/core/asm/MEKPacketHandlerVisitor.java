package com.cleanroommc.multiblocked.core.asm;

import com.cleanroommc.multiblocked.core.asm.util.ObfMapping;
import net.minecraft.tileentity.TileEntity;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MEKPacketHandlerVisitor extends MethodVisitor implements Opcodes {
    public static final String TARGET_CLASS_NAME = "mekanism/common/PacketHandler";
    public static final ObfMapping TARGET_METHOD_1 = new ObfMapping(
            TARGET_CLASS_NAME,
            "sendUpdatePacket",
            "(Lnet/minecraft/tileentity/TileEntity;)V");
    public static final ObfMapping TARGET_METHOD_2 = new ObfMapping(
            TARGET_CLASS_NAME,
            "sendToDimension",
            "(Lnet/minecraftforge/fml/common/network/simpleimpl/IMessage;I)V");
    private static final ObfMapping METHOD_HOOKS_1 = new ObfMapping("com/cleanroommc/multiblocked/core/asm/hooks/MEKHooks",
            "sendUpdatePacket",
            "(Lnet/minecraft/tileentity/TileEntity;)Z");
    private static final ObfMapping METHOD_HOOKS_2 = new ObfMapping("com/cleanroommc/multiblocked/core/asm/hooks/MEKHooks",
            "sendToDimension",
            "(I)Z");

    int hook;

    public MEKPacketHandlerVisitor(MethodVisitor mv, int hook) {
        super(Opcodes.ASM5, mv);
        this.hook = hook;
    }

    @Override
    public void visitCode() {
        if (hook == 0) {
            Label label = new Label();
            mv.visitVarInsn(ALOAD, 1);
            METHOD_HOOKS_1.visitMethodInsn(this, INVOKESTATIC);
            mv.visitJumpInsn(IFEQ, label);
            mv.visitInsn(RETURN);
            mv.visitLabel(label);
            super.visitCode();
        } else if (hook == 1) {
            Label label = new Label();
            mv.visitVarInsn(ILOAD, 2);
            METHOD_HOOKS_2.visitMethodInsn(this, INVOKESTATIC);
            mv.visitJumpInsn(IFEQ, label);
            mv.visitInsn(RETURN);
            mv.visitLabel(label);
            super.visitCode();
        }

    }
}

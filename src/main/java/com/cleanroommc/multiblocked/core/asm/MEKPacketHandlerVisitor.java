package com.cleanroommc.multiblocked.core.asm;

import com.cleanroommc.multiblocked.core.asm.util.ObfMapping;
import net.minecraft.tileentity.TileEntity;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MEKPacketHandlerVisitor extends MethodVisitor implements Opcodes {
    public static final String TARGET_CLASS_NAME = "mekanism/common/PacketHandler";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(
            TARGET_CLASS_NAME,
            "sendUpdatePacket",
            "(Lnet/minecraft/tileentity/TileEntity;)V");
    private static final ObfMapping METHOD_HOOKS = new ObfMapping("com/cleanroommc/multiblocked/core/asm/hooks/MEKHooks",
            "sendUpdatePacket",
            "(Lnet/minecraft/tileentity/TileEntity;)Z");

    public MEKPacketHandlerVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitCode() {
        Label label = new Label();
        mv.visitVarInsn(ALOAD, 1);
        METHOD_HOOKS.visitMethodInsn(this, INVOKESTATIC);
        mv.visitJumpInsn(IFEQ, label);
        mv.visitInsn(RETURN);
        mv.visitLabel(label);
        super.visitCode();
    }
}

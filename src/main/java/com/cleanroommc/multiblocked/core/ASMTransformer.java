package com.cleanroommc.multiblocked.core;

import com.cleanroommc.multiblocked.core.asm.CCLBlockRendererDispatcherVisitor;
import com.cleanroommc.multiblocked.core.asm.JEIRecipesGuiVisitor;
import com.cleanroommc.multiblocked.core.asm.MEKPacketHandlerVisitor;
import com.cleanroommc.multiblocked.core.asm.util.TargetClassVisitor;
import com.cleanroommc.multiblocked.core.asm.BlockVisitor;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class ASMTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        String internalClassName = transformedName.replace('.', '/');
        switch (internalClassName) {
            case BlockVisitor.TARGET_CLASS_NAME: {
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                ClassNode classNode = new ClassNode();
                classReader.accept(classNode, 0);
                BlockVisitor.handleClassNode(classNode).accept(classWriter);
                classWriter.toByteArray();
                return classWriter.toByteArray();
            }
            case JEIRecipesGuiVisitor.TARGET_CLASS_NAME: {
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classReader.accept(new TargetClassVisitor(classWriter, JEIRecipesGuiVisitor.TARGET_METHOD, JEIRecipesGuiVisitor::new), 0);
                return classWriter.toByteArray();
            }
            case MEKPacketHandlerVisitor.TARGET_CLASS_NAME: {
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classReader.accept(new TargetClassVisitor(classWriter, MEKPacketHandlerVisitor.TARGET_METHOD_1, mv -> new MEKPacketHandlerVisitor(mv, 0)), 0);

                classReader = new ClassReader(classWriter.toByteArray());
                classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classReader.accept(new TargetClassVisitor(classWriter, MEKPacketHandlerVisitor.TARGET_METHOD_2, mv -> new MEKPacketHandlerVisitor(mv, 1)), 0);

                return classWriter.toByteArray();
            }
            case CCLBlockRendererDispatcherVisitor.TARGET_CLASS_NAME: {
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classReader.accept(new TargetClassVisitor(classWriter, CCLBlockRendererDispatcherVisitor.TARGET_METHOD_1, mv -> new CCLBlockRendererDispatcherVisitor(mv, 0)), 0);

                classReader = new ClassReader(classWriter.toByteArray());
                classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classReader.accept(new TargetClassVisitor(classWriter, CCLBlockRendererDispatcherVisitor.TARGET_METHOD_2, mv -> new CCLBlockRendererDispatcherVisitor(mv, 1)), 0);

                return classWriter.toByteArray();
            }
        }
        return basicClass;
    }
}

package com.cleanroommc.multiblocked.core.asm;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.capability.trait.InterfaceUser;
import com.cleanroommc.multiblocked.api.tile.part.PartTileEntity;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author youyihj
 */
public class DynamicTileEntityGenerator implements Opcodes {
    private static final String PART_TILE_ENTITY_CLASS_NAME = "com/cleanroommc/multiblocked/api/tile/part/PartDynamicTileEntity";
    private static final String TRAIT_SETTERS_SIGNATURE_FORMAT = "Ljava/util/Map<Ljava/lang/String;Ljava/util/function/BiConsumer<L%s;Lcom/cleanroommc/multiblocked/api/capability/trait/CapabilityTrait;>;>;";
    private final List<CapabilityTrait> traits;
    private final List<Class<?>> interfaces = new ArrayList<>();
    private final String name;

    public DynamicTileEntityGenerator(String name, List<CapabilityTrait> traits) {
        this.name = name;
        this.traits = traits;
        for (CapabilityTrait trait : traits) {
            Class<?> clazz = trait.getClass().getAnnotation(InterfaceUser.class).value();
            if (!clazz.isInterface()) {
                throw new IllegalArgumentException("The value of InterfaceUser annotation must be an interface!");
            }
            interfaces.add(clazz);
        }
    }

    public Class<?> generateClass() {
        ClassWriter classWriter = new ClassWriter(0);
        List<String> interfaceNames = new ArrayList<>();
        StringBuilder signature = new StringBuilder("L").append(PART_TILE_ENTITY_CLASS_NAME).append("<L");
        String className = "com/cleanroommc/multiblocked/api/tile/part/dynamic/" + name;
        signature.append(className).append(";>;");
        for (Class<?> anInterface : interfaces) {
            interfaceNames.add(Type.getInternalName(anInterface));
            signature.append(Type.getDescriptor(anInterface));
        }
        classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, className, signature.toString(), PART_TILE_ENTITY_CLASS_NAME, interfaceNames.toArray(new String[0]));
        classWriter.visitSource(name + ".dynamic", null);
        classWriter.visitInnerClass("com/google/common/collect/ImmutableMap$Builder", "com/google/common/collect/ImmutableMap", "Builder", ACC_PUBLIC | ACC_STATIC);
        classWriter.visitInnerClass("java/lang/invoke/MethodHandles$Lookup", "java/lang/invoke/MethodHandles", "Lookup", ACC_PUBLIC | ACC_FINAL | ACC_STATIC);
        constructor(classWriter, className);
        fields(classWriter, className);
        int startLine = 17;
        for (int i = 0; i < interfaces.size(); i++) {
            startLine = methods(classWriter, interfaces.get(i), className, startLine, i);
        }
        classInitializer(classWriter, className);
        {
            MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PROTECTED, "getTraitSetters", "()Ljava/util/Map;", "()" + String.format(TRAIT_SETTERS_SIGNATURE_FORMAT, className), null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(startLine + 5, label0);
            methodVisitor.visitFieldInsn(GETSTATIC, className, "TRAIT_SETTERS", "Ljava/util/Map;");
            methodVisitor.visitInsn(ARETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "L" + className + ";", null, label0, label1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();
        byte[] bytecode = classWriter.toByteArray();
        CustomClassLoader.INSTANCE.bytecodes.put(className.replace("/", "."), bytecode);
        try {
            FileUtils.writeByteArrayToFile(new File("classes/" + className + ".class"), bytecode);
        } catch (IOException ignored) {

        }
        try {
            return CustomClassLoader.INSTANCE.findClass(className.replace("/", "."));
        } catch (ClassNotFoundException e) {
            Multiblocked.LOGGER.error("Failed to generate TE class for part {}", name, e);
            return PartTileEntity.PartSimpleTileEntity.class;
        }
    }

    private void constructor(ClassWriter classWriter, String className) {
        MethodVisitor methodVisitor;
        methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(13, label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, PART_TILE_ENTITY_CLASS_NAME, "<init>", "()V", false);
        methodVisitor.visitInsn(RETURN);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLocalVariable("this", "L" + className + ";", null, label0, label1, 0);
        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();
    }

    private void fields(ClassWriter classWriter, String className) {
        FieldVisitor fieldVisitor;
        fieldVisitor = classWriter.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, "TRAIT_SETTERS", "Ljava/util/Map;", String.format(TRAIT_SETTERS_SIGNATURE_FORMAT, className), null);
        fieldVisitor.visitEnd();
        for (CapabilityTrait trait : traits) {
            Class<?> anInterface = trait.getClass().getAnnotation(InterfaceUser.class).value();
            fieldVisitor = classWriter.visitField(ACC_PUBLIC, "trait_" + anInterface.getSimpleName(), Type.getDescriptor(anInterface), null, null);
            fieldVisitor.visitEnd();
        }
    }

    private int methods(ClassWriter classWriter, Class<?> anInterface, String className, int startLine, int index) {
        String fieldName = "trait_" + anInterface.getSimpleName();
        String fieldSignature = Type.getDescriptor(anInterface);
        MethodVisitor methodVisitor;
        for (Method method : anInterface.getMethods()) {
            // interface implementation
            if (Modifier.isAbstract(method.getModifiers()) || method.isDefault()) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                Class<?> returnType = method.getReturnType();
                List<String> exceptions = new ArrayList<>();
                for (Class<?> exceptionType : method.getExceptionTypes()) {
                    exceptions.add(Type.getInternalName(exceptionType));
                }
                methodVisitor = classWriter.visitMethod(ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(method), null, exceptions.toArray(new String[0]));
                methodVisitor.visitCode();
                Label label0 = new Label();
                methodVisitor.visitLabel(label0);
                methodVisitor.visitLineNumber(startLine, label0);
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitFieldInsn(GETFIELD, className, fieldName, fieldSignature);
                for (int i = 0; i < parameterTypes.length; i++) {
                    methodVisitor.visitVarInsn(ALOAD, i + 1);
                }
                methodVisitor.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(anInterface), method.getName(), Type.getMethodDescriptor(method), true);
                int returnCode = ARETURN;
                if (returnType == Integer.TYPE || returnType == Short.TYPE || returnType == Character.TYPE || returnType == Boolean.TYPE) {
                    returnCode = IRETURN;
                } else if (returnType == Long.TYPE) {
                    returnCode = LRETURN;
                } else if (returnType == Float.TYPE) {
                    returnCode = FRETURN;
                } else if (returnType == Double.TYPE) {
                    returnCode = DRETURN;
                } else if (returnType == Void.TYPE) {
                    returnCode = RETURN;
                }
                if (returnCode == RETURN) {
                    Label label1 = new Label();
                    startLine++;
                    methodVisitor.visitLineNumber(startLine, label1);
                }
                methodVisitor.visitInsn(returnCode);
                Label endLabel = new Label();
                methodVisitor.visitLabel(endLabel);
                methodVisitor.visitLocalVariable("this", "L" + className + ";", null, label0, endLabel, 0);
                for (int i = 0; i < parameterTypes.length; i++) {
                    methodVisitor.visitLocalVariable("param" + i, Type.getDescriptor(parameterTypes[i]), null, label0, endLabel, i + 1);
                }
                methodVisitor.visitMaxs(parameterTypes.length + 1, parameterTypes.length + 1);
                methodVisitor.visitEnd();
                startLine += 4;
            }
        }
        // trait setter lambda
        methodVisitor = classWriter.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, "lambda$static$" + index, "(L" + className + ";Lcom/cleanroommc/multiblocked/api/capability/trait/CapabilityTrait;)V", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitTypeInsn(CHECKCAST, Type.getInternalName(anInterface));
        methodVisitor.visitFieldInsn(PUTFIELD, className, "trait_" + anInterface.getSimpleName(), Type.getDescriptor(anInterface));
        methodVisitor.visitInsn(RETURN);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLocalVariable("te", "L" + className + ";", null, label0, label1, 0);
        methodVisitor.visitLocalVariable("trait", "Lcom/cleanroommc/multiblocked/api/capability/trait/CapabilityTrait;", null, label0, label1, 1);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();
        return startLine;
    }

    public void classInitializer(ClassWriter classWriter, String className) {
        MethodVisitor methodVisitor = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "com/google/common/collect/ImmutableMap", "builder", "()Lcom/google/common/collect/ImmutableMap$Builder;", false);
        for (int i = 0; i < interfaces.size(); i++) {
            Class<?> anInterface = interfaces.get(i);
            methodVisitor.visitLdcInsn(anInterface.getSimpleName());
            methodVisitor.visitInvokeDynamicInsn("accept", "()Ljava/util/function/BiConsumer;", new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false),
                    Type.getType("(Ljava/lang/Object;Ljava/lang/Object;)V"),
                    new Handle(
                            H_INVOKESTATIC, className,
                            "lambda$static$" + i,
                            "(L" + className + ";Lcom/cleanroommc/multiblocked/api/capability/trait/CapabilityTrait;)V",
                            false
                    ), Type.getType("(L" + className + ";Lcom/cleanroommc/multiblocked/api/capability/trait/CapabilityTrait;)V"));
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "com/google/common/collect/ImmutableMap$Builder", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMap$Builder;", false);
        }
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "com/google/common/collect/ImmutableMap$Builder", "build", "()Lcom/google/common/collect/ImmutableMap;", false);
        methodVisitor.visitFieldInsn(PUTSTATIC, className, "TRAIT_SETTERS", "Ljava/util/Map;");
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(3, 0);
        methodVisitor.visitEnd();
    }

    public static class CustomClassLoader extends ClassLoader {
        public static final CustomClassLoader INSTANCE = new CustomClassLoader(Multiblocked.class.getClassLoader());
        private final Map<String, byte[]> bytecodes = new HashMap<>();
        private final Map<String, Class<?>> classes = new HashMap<>();

        public CustomClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (classes.containsKey(name)) {
                return classes.get(name);
            }
            if (bytecodes.containsKey(name)) {
                byte[] bytecode = bytecodes.get(name);
                classes.put(name, defineClass(name, bytecode, 0, bytecode.length));
                return classes.get(name);
            }
            return super.findClass(name);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (classes.containsKey(name)) {
                return classes.get(name);
            }
            if (bytecodes.containsKey(name)) {
                byte[] bytecode = bytecodes.get(name);
                classes.put(name, defineClass(name, bytecode, 0, bytecode.length));
                return classes.get(name);
            }
            return super.loadClass(name);
        }
    }

}

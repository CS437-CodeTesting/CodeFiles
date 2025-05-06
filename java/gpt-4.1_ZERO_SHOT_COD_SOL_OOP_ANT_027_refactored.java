import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Handles the visitation and transformation of methods annotated with @JavaScriptBody.
 * Refactored for SOLID, OOP, and code quality.
 */
public final class JavaScriptBodyMethodTransformer extends MethodVisitor {
    private final String methodName;
    private final String methodDesc;
    private final int methodAccess;
    private final MethodContext context;
    private JavaScriptBodyAnnotationData jsBodyData;
    private boolean bodyGenerated;

    public JavaScriptBodyMethodTransformer(
            int access,
            String name,
            String desc,
            MethodVisitor mv,
            MethodContext context
    ) {
        super(Opcodes.ASM5, mv);
        this.methodAccess = access;
        this.methodName = name;
        this.methodDesc = desc;
        this.context = Objects.requireNonNull(context, "MethodContext required");
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (JavaScriptBodyAnnotationData.ANNOTATION_DESC.equals(desc)) {
            context.incrementFound();
            return new JavaScriptBodyAnnotationVisitor(this::onJavaScriptBodyAnnotationParsed);
        }
        return super.visitAnnotation(desc, visible);
    }

    private void onJavaScriptBodyAnnotationParsed(JavaScriptBodyAnnotationData data) {
        this.jsBodyData = data;
    }

    @Override
    public void visitCode() {
        if (jsBodyData == null) {
            return;
        }
        generateBody(true);
    }

    private boolean generateBody(boolean hasCode) {
        if (bodyGenerated) {
            return false;
        }
        bodyGenerated = true;

        if (mv != null) {
            // Re-emit the annotation for downstream tools
            AnnotationVisitor av = super.visitAnnotation(JavaScriptBodyAnnotationData.ANNOTATION_DESC, false);
            AnnotationVisitor argsArray = av.visitArray("args");
            for (String arg : jsBodyData.getArgs()) {
                argsArray.visit(null, arg);
            }
            argsArray.visitEnd();
            av.visit("javacall", jsBodyData.isJavaCall());
            av.visit("body", jsBodyData.getBody());
            av.visitEnd();
        }

        // Prepare JS body and argument list
        String jsBody = jsBodyData.isJavaCall() ? context.callback(jsBodyData.getBody()) : jsBodyData.getBody();
        List<String> args = new ArrayList<>(jsBodyData.getArgs());
        if (jsBodyData.isJavaCall()) {
            args.add("vm");
        }

        // Load or initialize Fn field
        String fnFieldName = context.getFnFieldName(methodName);
        super.visitFieldInsn(
                Opcodes.GETSTATIC, context.getClassName(),
                fnFieldName,
                context.getFnFieldDesc()
        );
        super.visitInsn(Opcodes.DUP);
        super.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                context.getFnClassInternalName(), "isValid",
                context.getFnIsValidDesc()
        );
        Label ifNotNull = new Label();
        super.visitJumpInsn(Opcodes.IFNE, ifNotNull);

        // Initialize Fn if null
        super.visitInsn(Opcodes.POP);
        super.visitLdcInsn(Type.getObjectType(context.getClassName()));
        super.visitInsn(jsBodyData.isKeepAlive() ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
        super.visitLdcInsn(jsBody);
        super.visitIntInsn(Opcodes.SIPUSH, args.size());
        super.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/String");
        for (int i = 0; i < args.size(); i++) {
            super.visitInsn(Opcodes.DUP);
            super.visitIntInsn(Opcodes.BIPUSH, i);
            super.visitLdcInsn(args.get(i));
            super.visitInsn(Opcodes.AASTORE);
        }
        super.visitMethodInsn(Opcodes.INVOKESTATIC,
                context.getFnClassInternalName(), "define",
                context.getFnDefineDesc()
        );

        // Preload resources if any
        Label noPresenter = new Label();
        super.visitInsn(Opcodes.DUP);
        super.visitJumpInsn(Opcodes.IFNULL, noPresenter);
        for (String resource : context.getResources()) {
            if (resource == null) continue;
            super.visitLdcInsn(Type.getObjectType(context.getClassName()));
            super.visitLdcInsn(resource);
            super.visitMethodInsn(Opcodes.INVOKESTATIC,
                    context.getFnClassInternalName(), "preload",
                    context.getFnPreloadDesc()
            );
        }
        super.visitInsn(Opcodes.DUP);
        super.visitFieldInsn(
                Opcodes.PUTSTATIC, context.getClassName(),
                fnFieldName,
                context.getFnFieldDesc()
        );
        // End of Fn init

        super.visitLabel(ifNotNull);

        // Prepare method arguments
        int offset = ((methodAccess & Opcodes.ACC_STATIC) == 0) ? 1 : 0;
        if (offset == 1) {
            super.visitIntInsn(Opcodes.ALOAD, 0);
        } else {
            super.visitInsn(Opcodes.ACONST_NULL);
        }
        super.visitIntInsn(Opcodes.SIPUSH, args.size());
        super.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");

        // Parse method signature and load arguments
        MethodSignatureInfo signatureInfo = MethodSignatureInfo.parse(methodDesc, offset);
        for (MethodSignatureInfo.Parameter param : signatureInfo.getParameters()) {
            super.visitInsn(Opcodes.DUP);
            super.visitIntInsn(Opcodes.SIPUSH, param.getIndex());
            param.emitLoadAndBox(super.mv);
            super.visitInsn(Opcodes.AASTORE);
        }

        // If "vm" argument is needed, load it
        if (args.contains("vm")) {
            super.visitInsn(Opcodes.DUP);
            super.visitIntInsn(Opcodes.SIPUSH, args.size() - 1);
            String jsCallbacks = context.getJsCallbacksInternalName();
            super.visitFieldInsn(Opcodes.GETSTATIC, jsCallbacks, "VM", "L" + jsCallbacks + ";");
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, jsCallbacks, "current", "()L" + jsCallbacks + ";");
            super.visitInsn(Opcodes.AASTORE);
        }

        // Call Fn.invoke or Fn.invokeLater
        if (jsBodyData.isWaitForJs()) {
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    context.getFnClassInternalName(), "invoke", context.getFnInvokeDesc()
            );
            signatureInfo.emitReturn(super.mv);
        } else {
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    context.getFnClassInternalName(), "invokeLater", context.getFnInvokeLaterDesc()
            );
            super.visitInsn(Opcodes.RETURN);
        }

        super.visitLabel(noPresenter);
        if (hasCode) {
            super.visitCode();
        } else {
            super.visitTypeInsn(Opcodes.NEW, "java/lang/IllegalStateException");
            super.visitInsn(Opcodes.DUP);
            super.visitLdcInsn("No presenter active. Use BrwsrCtx.execute!");
            super.visitMethodInsn(Opcodes.INVOKESPECIAL,
                    "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;)V"
            );
            super.visitInsn(Opcodes.ATHROW);
        }
        return true;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (jsBodyData != null) {
            if (generateBody(false)) {
                // Native method
                super.visitMaxs(1, 0);
            }
            context.defineFnField(methodName);
        }
    }

    // --- Supporting Classes ---

    /**
     * Holds context and configuration for the method transformation.
     */
    public static class MethodContext {
        private final String className;
        private final String[] resources;
        private int found = 0;

        public MethodContext(String className, String[] resources) {
            this.className = className;
            this.resources = resources != null ? resources : new String[0];
        }

        public String getClassName() {
            return
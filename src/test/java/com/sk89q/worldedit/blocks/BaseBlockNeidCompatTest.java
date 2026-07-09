package com.sk89q.worldedit.blocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Compatibility contract between {@link BaseBlock#internalSetId(int)} and the
 * NotEnoughIDs coremod.
 *
 * NEID's {@code WorldEditBaseBlock} ASM transformer patches
 * {@code com.sk89q.worldedit.blocks.BaseBlock#internalSetId} at class load. It
 * calls {@code AsmUtil.modifyIntConstantInMethod(method, 4095, 32767)} in its
 * NON optional form: when the method exists but contains no int constant 4095
 * (as ICONST_x, BIPUSH, SIPUSH or LDC), it throws an AsmTransformException,
 * the class fails to load and the whole server dies in preInit. It also
 * rewrites the first string constant containing "4095" so the exception
 * message shows the raised cap.
 *
 * A previous refactor replaced the literal with a read of the (non final)
 * {@code MAX_ID} field, which removed the constant from the bytecode and
 * crashed every NEID server on startup. These tests pin the bytecode shape so
 * that can never happen silently again.
 */
public class BaseBlockNeidCompatTest {

    private int previousMaxId;
    private int previousMaxData;

    @Before
    public void setUp() {
        previousMaxId = BaseBlock.MAX_ID;
        previousMaxData = BaseBlock.MAX_DATA;
    }

    @After
    public void tearDown() {
        BaseBlock.MAX_ID = previousMaxId;
        BaseBlock.MAX_DATA = previousMaxData;
    }

    /**
     * Mirrors the matcher of NEID's AsmUtil.modifyIntConstantInMethod
     */
    private static boolean matchesIntConstant(AbstractInsnNode insn, int value) {
        if (value >= 0 && value <= 5 && insn.getOpcode() == Opcodes.ICONST_0 + value) {
            return true;
        }
        if ((insn.getOpcode() == Opcodes.BIPUSH || insn.getOpcode() == Opcodes.SIPUSH)
            && ((IntInsnNode) insn).operand == value) {
            return true;
        }
        return insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof Integer
            && (Integer) ((LdcInsnNode) insn).cst == value;
    }

    @Test
    public void internalSetIdKeepsTheConstantsNeidPatches() throws Exception {
        InputStream in = BaseBlock.class.getResourceAsStream("/com/sk89q/worldedit/blocks/BaseBlock.class");
        assertNotNull("BaseBlock.class not found on the classpath", in);

        ClassNode classNode = new ClassNode();
        try {
            new ClassReader(in).accept(classNode, 0);
        } finally {
            in.close();
        }

        MethodNode internalSetId = null;
        for (Object m : classNode.methods) {
            if ("internalSetId".equals(((MethodNode) m).name)) {
                internalSetId = (MethodNode) m;
                break;
            }
        }
        assertNotNull("internalSetId must exist, NEID patches it by name", internalSetId);

        boolean hasIntConstant = false;
        boolean hasStringConstant = false;
        for (AbstractInsnNode insn = internalSetId.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (matchesIntConstant(insn, 4095)) {
                hasIntConstant = true;
            }
            if (insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof String
                && ((String) ((LdcInsnNode) insn).cst).contains("4095")) {
                hasStringConstant = true;
            }
        }

        assertTrue(
            "internalSetId must contain the int constant 4095 - NEID's transformer "
                + "throws a hard AsmTransformException without it and the server "
                + "cannot start",
            hasIntConstant);
        assertTrue(
            "internalSetId should carry the \"4095\" string constant NEID rewrites "
                + "so the error message shows the raised cap",
            hasStringConstant);
    }

    @Test
    public void vanillaCapIsEnforcedByDefault() {
        BaseBlock.MAX_ID = 4095;

        assertEquals(4095, new BaseBlock(4095).getId());
        try {
            new BaseBlock(4096);
            fail("expected IllegalArgumentException above the vanilla cap");
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void runtimeRaisedCapAcceptsNeidIds() {
        // ForgeWorldEdit raises MAX_ID when the neid mod is loaded; on a live
        // server NEID's transformer raises the literal in internalSetId the
        // same way, so both paths agree on the ceiling
        BaseBlock.MAX_ID = Short.MAX_VALUE;

        assertEquals(16529, new BaseBlock(16529).getId());
        assertEquals(Short.MAX_VALUE, new BaseBlock(Short.MAX_VALUE).getId());
        try {
            new BaseBlock(Short.MAX_VALUE + 1);
            fail("expected IllegalArgumentException above the raised cap");
        } catch (IllegalArgumentException expected) {}
    }
}

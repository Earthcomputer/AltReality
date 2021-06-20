package net.earthcomputer.altreality.engine;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.packet.handshake.HandshakePacket;
import net.minecraft.packet.login.LoginRequestPacket;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;

public class Constants {
    public static final String MOD_NAME = "AltReality";
    public static final int PROTOCOL_VERSION_MARKER = 1 << 30;
    public static final int PROTOCOL_VERSION = findProtocolVersion();

    private static int findProtocolVersion() {
        ClassNode cpnhClass;
        try {
            cpnhClass = MixinService.getService().getBytecodeProvider().getClassNode(ClientPlayNetworkHandler.class.getName().replace('.', '/'), false);
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException("Unable to find protocol version", e);
        }

        String wantedDesc = String.format("(%s)V", HandshakePacket.class.getName().replace('.', '/'));
        for (MethodNode method : cpnhClass.methods) {
            if (method.desc.equals(wantedDesc)) {
                int lastIntConstant = 0;
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn.getOpcode() >= Opcodes.ICONST_M1 && insn.getOpcode() <= Opcodes.ICONST_5) {
                        lastIntConstant = insn.getOpcode() - Opcodes.ICONST_0;
                    } else if (insn.getOpcode() == Opcodes.BIPUSH || insn.getOpcode() == Opcodes.SIPUSH) {
                        lastIntConstant = ((IntInsnNode) insn).operand;
                    } else if (insn.getOpcode() == Opcodes.LDC) {
                        Object cst = ((LdcInsnNode) insn).cst;
                        if (cst instanceof Integer) {
                            lastIntConstant = (Integer) cst;
                        }
                    } else if (insn.getOpcode() == Opcodes.INVOKESPECIAL) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (methodInsn.name.equals("<init>") && methodInsn.owner.equals(LoginRequestPacket.class.getName().replace('.', '/'))) {
                            return lastIntConstant;
                        }
                    }
                }
                break;
            }
        }

        throw new RuntimeException("Unable to find protocol version");
    }
}

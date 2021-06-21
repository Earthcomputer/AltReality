package net.earthcomputer.altreality.mixin.engine.auth;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.packet.handshake.HandshakePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Environment(EnvType.CLIENT)
@Mixin(HandshakePacket.class)
public class HandshakePacketMixin {
    @ModifyConstant(method = "read", constant = @Constant(intValue = 32))
    private int modifyMaxServerIdLength(int oldVal) {
        return 1024;
    }
}

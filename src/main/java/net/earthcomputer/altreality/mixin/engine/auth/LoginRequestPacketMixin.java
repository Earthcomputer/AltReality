package net.earthcomputer.altreality.mixin.engine.auth;

import net.earthcomputer.altreality.engine.Constants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.packet.login.LoginRequestPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(LoginRequestPacket.class)
public class LoginRequestPacketMixin {
    @ModifyVariable(method = "<init>(Ljava/lang/String;I)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private int modifyProtocolVersion(int protocolVersion) {
        return protocolVersion | Constants.PROTOCOL_VERSION_MARKER;
    }
}

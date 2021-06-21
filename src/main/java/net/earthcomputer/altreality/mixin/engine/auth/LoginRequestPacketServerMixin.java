package net.earthcomputer.altreality.mixin.engine.auth;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.packet.login.LoginRequestPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Environment(EnvType.SERVER)
@Mixin(LoginRequestPacket.class)
public class LoginRequestPacketServerMixin {
    @ModifyConstant(method = "read", constant = @Constant(intValue = 16))
    private int modifyNameLimit(int oldVal) {
        return 1024;
    }
}

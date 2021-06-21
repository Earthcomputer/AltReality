package net.earthcomputer.altreality.mixin.engine.auth;

import net.earthcomputer.altreality.engine.auth.IPlayerSpawnS2C;
import net.minecraft.packet.play.PlayerSpawnS2C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

@Mixin(PlayerSpawnS2C.class)
public class PlayerSpawnS2CMixin implements IPlayerSpawnS2C {
    @Unique private UUID uuid;

    @ModifyConstant(method = "read", constant = @Constant(intValue = 16))
    private int modifyMaxNameLength(int oldVal) {
        return 1024;
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void onRead(DataInputStream in, CallbackInfo ci) throws IOException {
        if (in.readBoolean()) {
            uuid = new UUID(in.readLong(), in.readLong());
        } else {
            uuid = null;
        }
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void onWrite(DataOutputStream out, CallbackInfo ci) throws IOException {
        if (uuid == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeLong(uuid.getMostSignificantBits());
            out.writeLong(uuid.getLeastSignificantBits());
        }
    }

    @Inject(method = "length", at = @At("RETURN"), cancellable = true)
    private void onLength(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValueI() + (uuid == null ? 1 : 17));
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}

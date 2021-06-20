package net.earthcomputer.altreality.mixin.engine.auth;

import net.earthcomputer.altreality.engine.auth.IPlayer;
import net.earthcomputer.altreality.engine.auth.IPlayerSpawnS2C;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.packet.AbstractPacket;
import net.minecraft.packet.play.PlayerSpawnS2C;
import net.minecraft.server.network.EntityTrackerEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.SERVER)
@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Shadow public Entity entity;

    @Inject(method = "createSpawnPacket", at = @At("RETURN"))
    private void addPlayerUuidToSpawnPacket(CallbackInfoReturnable<AbstractPacket> cir) {
        if (cir.getReturnValue() instanceof PlayerSpawnS2C) {
            ((IPlayerSpawnS2C) cir.getReturnValue()).setUuid(((IPlayer) entity).getGameProfile().getId());
        }
    }
}

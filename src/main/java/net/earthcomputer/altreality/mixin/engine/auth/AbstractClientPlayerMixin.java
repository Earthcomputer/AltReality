package net.earthcomputer.altreality.mixin.engine.auth;

import net.earthcomputer.altreality.engine.auth.IAuthedSession;
import net.earthcomputer.altreality.engine.auth.IPlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.Session;
import net.minecraft.entity.player.AbstractClientPlayer;
import net.minecraft.entity.player.Player;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player {
    public AbstractClientPlayerMixin(Level level) {
        super(level);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(Minecraft mc, Level level, Session session, int dimensionId, CallbackInfo ci) {
        skinUrl = null;
        ((IPlayer) this).setGameProfile(((IAuthedSession) session).getGameProfile());
    }
}

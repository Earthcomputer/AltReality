package net.earthcomputer.altreality.mixin.engine.auth;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractClientPlayer.class)
public interface AbstractClientPlayerAccessor {
    @Accessor
    Minecraft getMinecraft();
}

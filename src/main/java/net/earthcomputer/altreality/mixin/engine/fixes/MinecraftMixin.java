package net.earthcomputer.altreality.mixin.engine.fixes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "startLoginThread", at = @At("HEAD"), cancellable = true)
    private void onStartLoginThread(CallbackInfo ci) {
        ci.cancel();
    }
}

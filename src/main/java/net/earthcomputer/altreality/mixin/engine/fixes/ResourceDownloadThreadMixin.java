package net.earthcomputer.altreality.mixin.engine.fixes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.ResourceDownloadThread;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ResourceDownloadThread.class)
public class ResourceDownloadThreadMixin {
    @Inject(method = "run", at = @At("HEAD"), cancellable = true, remap = false)
    private void onRun(CallbackInfo ci) {
        ci.cancel();
    }
}
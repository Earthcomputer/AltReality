package net.earthcomputer.altreality.mixin.engine.resources;

import net.earthcomputer.altreality.engine.Constants;
import net.earthcomputer.altreality.engine.resources.SoundLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ResourceDownloadThread;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Environment(EnvType.CLIENT)
@Mixin(ResourceDownloadThread.class)
public abstract class ResourceDownloadThreadMixin {
    @Shadow private Minecraft field_138;

    @Shadow public abstract void method_107();

    @Inject(method = "run", at = @At("HEAD"), cancellable = true, remap = false)
    private void onRun(CallbackInfo ci) {
        try {
            SoundLoader.loadSounds();
        } catch (IOException e) {
            System.out.println("Exception loading " + Constants.MOD_NAME + " sounds");
            e.printStackTrace();
        }
        method_107();
        ci.cancel();
    }

    @Inject(method = "method_107", at = @At("RETURN"))
    private void onReloadSounds(CallbackInfo ci) {
        SoundLoader.reloadSounds(field_138);
    }
}

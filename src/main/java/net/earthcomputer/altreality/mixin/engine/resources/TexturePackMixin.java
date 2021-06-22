package net.earthcomputer.altreality.mixin.engine.resources;

import net.earthcomputer.altreality.AltReality;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.TexturePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;

@Environment(EnvType.CLIENT)
@Mixin(TexturePack.class)
public abstract class TexturePackMixin {
    @Shadow public abstract InputStream method_976(String name);

    @Inject(method = "method_976", at = @At(value = "INVOKE", target = "Ljava/lang/Class;getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;", remap = false), cancellable = true)
    private void onFindResource(String name, CallbackInfoReturnable<InputStream> cir) {
        if (!name.startsWith("/assets/") && name.startsWith("/")) {
            InputStream resource = method_976("/assets/minecraft" + name);
            if (resource != null) {
                cir.setReturnValue(resource);
                return;
            }
        }

        InputStream resource = AltReality.class.getResourceAsStream(name);
        if (resource != null) {
            cir.setReturnValue(resource);
        }
    }
}

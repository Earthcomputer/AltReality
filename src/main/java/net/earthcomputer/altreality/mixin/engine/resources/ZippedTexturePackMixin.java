package net.earthcomputer.altreality.mixin.engine.resources;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.TexturePack;
import net.minecraft.client.resource.ZippedTexturePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.InputStream;

@Environment(EnvType.CLIENT)
@Mixin(ZippedTexturePack.class)
public class ZippedTexturePackMixin extends TexturePack {
    @Redirect(method = "method_976", at = @At(value = "INVOKE", target = "Ljava/lang/Class;getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;", remap = false))
    private InputStream redirectGetResourceAsStream(Class<?> clazz, String name) {
        return super.method_976(name);
    }
}

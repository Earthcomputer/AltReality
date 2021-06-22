package net.earthcomputer.altreality.mixin.engine.resources;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.earthcomputer.altreality.AltReality;
import net.earthcomputer.altreality.engine.Constants;
import net.minecraft.client.resource.language.TranslationStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Mixin(TranslationStorage.class)
public class TranslationStorageMixin {
    @Shadow private Properties translations;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(CallbackInfo ci) {
        InputStream resource = AltReality.class.getResourceAsStream("/assets/" + Constants.MOD_ID + "/lang/en_us.json");
        if (resource == null) {
            throw new IllegalStateException("Could not find " + Constants.MOD_NAME + " language file");
        }
        JsonObject jsonObject = new Gson().fromJson(new InputStreamReader(resource, StandardCharsets.UTF_8), JsonObject.class);
        for (String key : jsonObject.keySet()) {
            translations.setProperty(key, jsonObject.get(key).getAsString());
        }
    }
}

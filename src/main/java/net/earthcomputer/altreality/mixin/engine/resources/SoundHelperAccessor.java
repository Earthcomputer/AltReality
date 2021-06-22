package net.earthcomputer.altreality.mixin.engine.resources;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_266;
import net.minecraft.client.sound.SoundHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(SoundHelper.class)
public interface SoundHelperAccessor {
    @Accessor
    class_266 getField_2668();

    @Accessor
    class_266 getField_2669();

    @Accessor
    class_266 getField_2670();
}

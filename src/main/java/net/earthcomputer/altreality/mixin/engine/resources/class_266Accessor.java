package net.earthcomputer.altreality.mixin.engine.resources;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_266;
import net.minecraft.class_267;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(class_266.class)
public interface class_266Accessor {
    @Accessor
    Map<String, List<class_267>> getField_1089();

    @Accessor
    List<class_267> getField_1090();
}

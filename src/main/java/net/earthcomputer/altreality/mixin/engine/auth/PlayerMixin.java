package net.earthcomputer.altreality.mixin.engine.auth;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.earthcomputer.altreality.engine.auth.ClientGlobals;
import net.earthcomputer.altreality.engine.auth.IPlayer;
import net.earthcomputer.altreality.engine.auth.SkinTextures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.ImageProcessorImpl;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.Player;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Environment(EnvType.CLIENT)
@Mixin(Player.class)
public class PlayerMixin extends LivingEntity implements IPlayer {
    @Shadow public String playerCloakUrl;

    @Unique private static final Executor SKIN_DOWNLOADER = Executors.newCachedThreadPool();
    @Unique private static final Map<UUID, SkinTextures> SKIN_TEXTURES_CACHE = new ConcurrentHashMap<>();

    @Unique private GameProfile gameProfile;
    @Unique private boolean hasInitializedSkin = false;
    @Unique private boolean shouldRegisterSkinTextures = false;

    public PlayerMixin(Level level) {
        super(level);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (shouldRegisterSkinTextures) {
            shouldRegisterSkinTextures = false;
            if (skinUrl != null) {
                ((AbstractClientPlayerAccessor) this).getMinecraft().textureManager.method_1094(skinUrl, new ImageProcessorImpl());
            }
            if (cloakUrl != null) {
                ((AbstractClientPlayerAccessor) this).getMinecraft().textureManager.method_1094(cloakUrl, new ImageProcessorImpl());
            }
        }
    }

    /**
     * @author Earthcomputer
     * @reason Needed replacing
     */
    @Environment(EnvType.CLIENT)
    @Overwrite
    @Override
    public void initCloak() {
        if (!hasInitializedSkin) {
            hasInitializedSkin = true;

            if (gameProfile.getId() == null) {
                return;
            }

            SkinTextures existingTextures = SKIN_TEXTURES_CACHE.get(gameProfile.getId());
            if (existingTextures != null) {
                skinUrl = existingTextures.skinUrl;
                playerCloakUrl = cloakUrl = existingTextures.capeUrl;
                return;
            }

            SKIN_DOWNLOADER.execute(() -> {
                Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = Collections.emptyMap();
                try {
                    textures = ClientGlobals.SESSION_SERVICE.getTextures(gameProfile, true);
                } catch (InsecureTextureException ignore) {
                }
                if (textures.isEmpty()) {
                    ClientGlobals.SESSION_SERVICE.fillProfileProperties(gameProfile, true);
                    try {
                        textures = ClientGlobals.SESSION_SERVICE.getTextures(gameProfile, true);
                    } catch (InsecureTextureException ignore) {
                    }
                }

                if (textures.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                    skinUrl = textures.get(MinecraftProfileTexture.Type.SKIN).getUrl();
                }
                if (textures.containsKey(MinecraftProfileTexture.Type.CAPE)) {
                    playerCloakUrl = cloakUrl = textures.get(MinecraftProfileTexture.Type.CAPE).getUrl();
                }
                SKIN_TEXTURES_CACHE.put(gameProfile.getId(), new SkinTextures(skinUrl, cloakUrl));
                shouldRegisterSkinTextures = true;
            });
        }
    }

    @Override
    public void setGameProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    @Override
    public GameProfile getGameProfile() {
        return gameProfile;
    }
}

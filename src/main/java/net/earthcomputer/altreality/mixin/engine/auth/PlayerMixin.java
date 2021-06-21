package net.earthcomputer.altreality.mixin.engine.auth;

import com.mojang.authlib.GameProfile;
import net.earthcomputer.altreality.engine.auth.IPlayer;
import net.minecraft.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public class PlayerMixin implements IPlayer {
    @Unique private GameProfile gameProfile;

    @Override
    public void setGameProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    @Override
    public GameProfile getGameProfile() {
        return gameProfile;
    }
}

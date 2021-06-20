package net.earthcomputer.altreality.engine.auth;

import com.mojang.authlib.GameProfile;

public interface IPlayer {
    void setGameProfile(GameProfile gameProfile);
    GameProfile getGameProfile();
}

package net.earthcomputer.altreality.engine.auth;

import com.mojang.authlib.GameProfile;

public interface IAuthedSession {
    GameProfile getGameProfile();
}

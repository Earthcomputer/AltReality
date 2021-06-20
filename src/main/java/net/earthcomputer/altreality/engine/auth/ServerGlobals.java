package net.earthcomputer.altreality.engine.auth;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.net.Proxy;
import java.security.KeyPair;

@Environment(EnvType.SERVER)
public class ServerGlobals {
    public static final KeyPair KEY_PAIR;
    public static MinecraftSessionService SESSION_SERVICE;

    static {
        try {
            KEY_PAIR = NetworkEncryptionUtils.generateServerKeyPair();
        } catch (NetworkEncryptionException e) {
            throw new IllegalStateException("Failed to generate key pair", e);
        }

        YggdrasilAuthenticationService authService = new YggdrasilAuthenticationService(Proxy.NO_PROXY);
        SESSION_SERVICE = authService.createMinecraftSessionService();
    }
}

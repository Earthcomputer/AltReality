package net.earthcomputer.altreality.engine.auth;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.net.Proxy;

@Environment(EnvType.CLIENT)
public class ClientGlobals {
    public static MinecraftSessionService SESSION_SERVICE;

    static {
        YggdrasilAuthenticationService authService = new YggdrasilAuthenticationService(Proxy.NO_PROXY);
        SESSION_SERVICE = authService.createMinecraftSessionService();
    }
}

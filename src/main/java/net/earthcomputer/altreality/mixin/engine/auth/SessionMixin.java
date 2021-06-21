package net.earthcomputer.altreality.mixin.engine.auth;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import net.earthcomputer.altreality.engine.auth.IAuthedSession;
import net.earthcomputer.altreality.engine.auth.UuidResponse;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Environment(EnvType.CLIENT)
@Mixin(Session.class)
public class SessionMixin implements IAuthedSession {
    @Shadow public String field_873;

    @Unique private GameProfile gameProfile;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(String username, String sessionId, CallbackInfo ci) {
        UUID uuid = null;
        if (sessionId.startsWith("token:")) {
            sessionId = sessionId.substring(6);
        }
        if (sessionId.contains(":")) {
            String[] parts = sessionId.split(":", 2);
            field_873 = parts[0];
            try {
                uuid = UUID.fromString(parts[1]);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid uuid: " + parts[1]);
            }
        } else {
            try {
                URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        System.out.println("Mojang API request returned HTTP code " + responseCode);
                    } else {
                        UuidResponse response = new Gson().fromJson(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8), UuidResponse.class);
                        if (response != null && response.id != null) {
                            uuid = UUIDTypeAdapter.fromString(response.id);
                        }
                    }
                } else {
                    System.out.println("No such username: " + username);
                }
            } catch (Exception e) {
                System.out.println("Exception getting game profile uuid");
                e.printStackTrace();
            }
        }

        gameProfile = new GameProfile(uuid, username);
    }

    @Override
    public GameProfile getGameProfile() {
        return gameProfile;
    }
}

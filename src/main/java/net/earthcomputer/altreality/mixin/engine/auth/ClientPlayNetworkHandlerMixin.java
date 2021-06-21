package net.earthcomputer.altreality.mixin.engine.auth;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InsufficientPrivilegesException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import net.earthcomputer.altreality.engine.Constants;
import net.earthcomputer.altreality.engine.auth.ClientGlobals;
import net.earthcomputer.altreality.engine.auth.IAuthedSession;
import net.earthcomputer.altreality.engine.auth.IPlayer;
import net.earthcomputer.altreality.engine.auth.IPlayerSpawnS2C;
import net.earthcomputer.altreality.engine.auth.NetworkEncryptionException;
import net.earthcomputer.altreality.engine.auth.NetworkEncryptionUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.RemoteClientPlayer;
import net.minecraft.network.Connection;
import net.minecraft.packet.handshake.HandshakePacket;
import net.minecraft.packet.login.LoginRequestPacket;
import net.minecraft.packet.play.PlayerSpawnS2C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow private Connection connection;

    @Shadow private Minecraft minecraft;

    @Inject(method = "handleHandshake", at = @At("HEAD"), cancellable = true)
    private void newOnlineModeLogic(HandshakePacket packet, CallbackInfo ci) {
        if (!packet.str.equals("-")) {
            String[] parts = packet.str.split("&", 2);
            if (parts.length == 2) {
                try {
                    byte[] publicKeyBytes = Base64.getDecoder().decode(parts[0]);
                    PublicKey serverPublicKey = NetworkEncryptionUtils.readEncodedPublicKey(publicKeyBytes);
                    SecretKey clientSecretKey = NetworkEncryptionUtils.generateKey();
                    String baseServerId = parts[1];
                    String serverId = new BigInteger(NetworkEncryptionUtils.generateServerId(baseServerId, serverPublicKey, clientSecretKey)).toString(16);

                    GameProfile gameProfile = ((IAuthedSession) minecraft.session).getGameProfile();
                    if (gameProfile.getId() == null) {
                        connection.disconnect("disconnect.loginFailed");
                    } else {
                        boolean authSuccess = false;
                        try {
                            ClientGlobals.SESSION_SERVICE.joinServer(gameProfile, minecraft.session.field_873, serverId);
                            authSuccess = true;
                        } catch (AuthenticationUnavailableException e) {
                            connection.disconnect("The authentication servers are currently not reachable. Please try again.");
                        } catch (InvalidCredentialsException e) {
                            connection.disconnect("Invalid session (Try restarting your game and the launcher)");
                        } catch (InsufficientPrivilegesException e) {
                            connection.disconnect("Multiplayer is disabled. Please check your Microsoft account settings.");
                        } catch (AuthenticationException e) {
                            connection.disconnect("disconnect.loginFailedInfo", e.getMessage());
                        }

                        if (authSuccess) {
                            String encodedUsername = Base64.getEncoder().encodeToString(NetworkEncryptionUtils.encrypt(serverPublicKey, clientSecretKey.getEncoded()));
                            encodedUsername += "&" + Base64.getEncoder().encodeToString(NetworkEncryptionUtils.encrypt(serverPublicKey, baseServerId.getBytes(StandardCharsets.UTF_8)));
                            encodedUsername += "&" + minecraft.session.username;
                            connection.sendPacket(new LoginRequestPacket(encodedUsername, Constants.PROTOCOL_VERSION));
                        }
                    }
                } catch (NetworkEncryptionException e) {
                    throw new IllegalStateException("Protocol error", e);
                }

                ci.cancel();
            }
        }
    }

    @ModifyVariable(method = "handlePlayerSpawn", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private RemoteClientPlayer setRemotePlayerUuid(RemoteClientPlayer player, PlayerSpawnS2C packet) {
        ((IPlayer) player).setGameProfile(new GameProfile(((IPlayerSpawnS2C) packet).getUuid(), packet.name));
        return player;
    }
}

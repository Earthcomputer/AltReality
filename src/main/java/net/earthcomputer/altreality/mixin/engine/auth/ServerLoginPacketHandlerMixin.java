package net.earthcomputer.altreality.mixin.engine.auth;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import net.earthcomputer.altreality.engine.Constants;
import net.earthcomputer.altreality.engine.auth.IPlayer;
import net.earthcomputer.altreality.engine.auth.NetworkEncryptionException;
import net.earthcomputer.altreality.engine.auth.NetworkEncryptionUtils;
import net.earthcomputer.altreality.engine.auth.ServerGlobals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.packet.login.LoginRequestPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketHandler;
import net.minecraft.server.player.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

@Environment(EnvType.SERVER)
@Mixin(ServerLoginPacketHandler.class)
public abstract class ServerLoginPacketHandlerMixin {
    @Shadow private MinecraftServer server;
    @Shadow private String field_361;
    @Shadow private LoginRequestPacket loginRequestPacket;

    @Shadow public abstract void drop(String reason);

    @Unique private byte[] encryptedClientSecretKey;
    @Unique private byte[] encryptedNonce;
    @Unique private GameProfile gameProfile;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(CallbackInfo ci) {
    }

    @ModifyArg(method = "handleHandshake", at = @At(value = "INVOKE", target = "Lnet/minecraft/packet/handshake/HandshakePacket;<init>(Ljava/lang/String;)V"), index = 0)
    private String modifyServerId(String serverId) {
        return Base64.getEncoder().encodeToString(ServerGlobals.KEY_PAIR.getPublic().getEncoded()) + "&" + serverId;
    }

    @Inject(method = "handleLoginRequest", at = @At("HEAD"), cancellable = true)
    private void onLogin(LoginRequestPacket packet, CallbackInfo ci) {

        if ((packet.protocolVersion & Constants.PROTOCOL_VERSION_MARKER) == 0) {
            drop("Client must have " + Constants.MOD_NAME + " installed!");
            ci.cancel();
        }
        packet.protocolVersion &= ~Constants.PROTOCOL_VERSION_MARKER;

        if (server.onlineMode) {
            String[] parts = packet.username.split("&", 3);
            if (parts.length == 3) {
                encryptedClientSecretKey = Base64.getDecoder().decode(parts[0]);
                encryptedNonce = Base64.getDecoder().decode(parts[1]);
                packet.username = parts[2];
            }
        }

        gameProfile = new GameProfile(null, packet.username);
    }

    @Inject(method = "handleLoginRequest", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;onlineMode:Z"), cancellable = true)
    private void newOnlineModeLogic(LoginRequestPacket packet, CallbackInfo ci) {
        if (server.onlineMode && encryptedClientSecretKey != null) {
            new Thread(() -> {
                String serverId;
                try {
                    SecretKey clientSecretKey = NetworkEncryptionUtils.decryptSecretKey(ServerGlobals.KEY_PAIR.getPrivate(), encryptedClientSecretKey);
                    byte[] nonce = NetworkEncryptionUtils.decrypt(ServerGlobals.KEY_PAIR.getPrivate(), encryptedNonce);
                    if (!Arrays.equals(field_361.getBytes(StandardCharsets.UTF_8), nonce)) {
                        throw new IllegalStateException("Protocol error");
                    }
                    serverId = new BigInteger(NetworkEncryptionUtils.generateServerId(field_361, ServerGlobals.KEY_PAIR.getPublic(), clientSecretKey)).toString(16);
                } catch (NetworkEncryptionException e) {
                    throw new IllegalStateException("Protocol error", e);
                }
                try {
                    gameProfile = ServerGlobals.SESSION_SERVICE.hasJoinedServer(new GameProfile(null, packet.username), serverId, null);
                    if (gameProfile != null) {
                        System.out.println("UUID of player" + gameProfile.getName() + " is " + gameProfile.getId());
                        loginRequestPacket = packet;
                    } else {
                        drop("Failed to verify username!");
                        System.out.println("Username '" + packet.username + "' tried to join with an invalid session");
                    }
                } catch (AuthenticationUnavailableException e) {
                    drop("Authentication servers are down. Please try again later, sorry!");
                    System.out.println("Couldn't verify username because servers are unavailable");
                }
            }).start();

            ci.cancel();
        }
    }

    @ModifyVariable(method = "complete", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private ServerPlayer setServerPlayerGameProfile(ServerPlayer player) {
        ((IPlayer) player).setGameProfile(gameProfile);
        return player;
    }
}

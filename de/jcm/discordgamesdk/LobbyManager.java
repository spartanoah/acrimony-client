/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.GameSDKException;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.lobby.Lobby;
import de.jcm.discordgamesdk.lobby.LobbyMemberTransaction;
import de.jcm.discordgamesdk.lobby.LobbySearchQuery;
import de.jcm.discordgamesdk.lobby.LobbyTransaction;
import de.jcm.discordgamesdk.user.DiscordUser;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LobbyManager {
    private final long pointer;
    private final Core core;

    LobbyManager(long pointer, Core core) {
        this.pointer = pointer;
        this.core = core;
    }

    public LobbyTransaction getLobbyCreateTransaction() {
        Object ret = this.core.execute(() -> this.getLobbyCreateTransaction(this.pointer));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (LobbyTransaction)ret;
    }

    public LobbyTransaction getLobbyUpdateTransaction(long lobbyId) {
        Object ret = this.core.execute(() -> this.getLobbyUpdateTransaction(this.pointer, lobbyId));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (LobbyTransaction)ret;
    }

    public LobbyTransaction getLobbyUpdateTransaction(Lobby lobby) {
        return this.getLobbyUpdateTransaction(lobby.getId());
    }

    public LobbyMemberTransaction getMemberUpdateTransaction(long lobbyId, long userId) {
        Object ret = this.core.execute(() -> this.getMemberUpdateTransaction(this.pointer, lobbyId, userId));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (LobbyMemberTransaction)ret;
    }

    public LobbyMemberTransaction getMemberUpdateTransaction(Lobby lobby, long userId) {
        return this.getMemberUpdateTransaction(lobby.getId(), userId);
    }

    public void createLobby(LobbyTransaction transaction, BiConsumer<Result, Lobby> callback) {
        this.core.execute(() -> this.createLobby(this.pointer, transaction.getPointer(), Objects.requireNonNull(callback)));
    }

    public void createLobby(LobbyTransaction transaction, Consumer<Lobby> callback) {
        this.createLobby(transaction, (Result result, Lobby lobby) -> {
            Core.DEFAULT_CALLBACK.accept((Result)((Object)result));
            callback.accept((Lobby)lobby);
        });
    }

    public void updateLobby(long lobbyId, LobbyTransaction transaction, Consumer<Result> callback) {
        this.core.execute(() -> this.updateLobby(this.pointer, lobbyId, transaction.getPointer(), Objects.requireNonNull(callback)));
    }

    public void updateLobby(long lobbyId, LobbyTransaction transaction) {
        this.updateLobby(lobbyId, transaction, Core.DEFAULT_CALLBACK);
    }

    public void updateLobby(Lobby lobby, LobbyTransaction transaction, Consumer<Result> callback) {
        this.updateLobby(lobby.getId(), transaction, callback);
    }

    public void updateLobby(Lobby lobby, LobbyTransaction transaction) {
        this.updateLobby(lobby, transaction, Core.DEFAULT_CALLBACK);
    }

    public void deleteLobby(long lobbyId, Consumer<Result> callback) {
        this.core.execute(() -> this.deleteLobby(this.pointer, lobbyId, Objects.requireNonNull(callback)));
    }

    public void deleteLobby(long lobbyId) {
        this.deleteLobby(lobbyId, Core.DEFAULT_CALLBACK);
    }

    public void deleteLobby(Lobby lobby, Consumer<Result> callback) {
        this.deleteLobby(lobby.getId(), callback);
    }

    public void deleteLobby(Lobby lobby) {
        this.deleteLobby(lobby, Core.DEFAULT_CALLBACK);
    }

    public void connectLobby(long lobbyId, String secret, BiConsumer<Result, Lobby> callback) {
        if (secret.getBytes().length >= 128) {
            throw new IllegalArgumentException("max secret length is 127");
        }
        this.core.execute(() -> this.connectLobby(this.pointer, lobbyId, secret, Objects.requireNonNull(callback)));
    }

    public void connectLobby(long lobbyId, String secret, Consumer<Lobby> callback) {
        this.connectLobby(lobbyId, secret, (Result result, Lobby lobby) -> {
            Core.DEFAULT_CALLBACK.accept((Result)((Object)result));
            callback.accept((Lobby)lobby);
        });
    }

    public void connectLobby(Lobby lobby, BiConsumer<Result, Lobby> callback) {
        this.connectLobby(lobby.getId(), lobby.getSecret(), callback);
    }

    public void connectLobbyWithActivitySecret(String activitySecret, BiConsumer<Result, Lobby> callback) {
        if (activitySecret.getBytes().length >= 128) {
            throw new IllegalArgumentException("max activity secret length is 127");
        }
        this.core.execute(() -> this.connectLobbyWithActivitySecret(this.pointer, activitySecret, Objects.requireNonNull(callback)));
    }

    public void connectLobbyWithActivitySecret(String activitySecret, Consumer<Lobby> callback) {
        this.connectLobbyWithActivitySecret(activitySecret, (Result result, Lobby lobby) -> {
            Core.DEFAULT_CALLBACK.accept((Result)((Object)result));
            callback.accept((Lobby)lobby);
        });
    }

    public void disconnectLobby(long lobbyId, Consumer<Result> callback) {
        this.core.execute(() -> this.disconnectLobby(this.pointer, lobbyId, Objects.requireNonNull(callback)));
    }

    public void disconnectLobby(long lobbyId) {
        this.disconnectLobby(lobbyId, Core.DEFAULT_CALLBACK);
    }

    public void disconnectLobby(Lobby lobby, Consumer<Result> callback) {
        this.disconnectLobby(lobby.getId(), callback);
    }

    public void disconnectLobby(Lobby lobby) {
        this.disconnectLobby(lobby, Core.DEFAULT_CALLBACK);
    }

    public Lobby getLobby(long lobbyId) {
        Object ret = this.core.execute(() -> this.getLobby(this.pointer, lobbyId));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (Lobby)ret;
    }

    public String getLobbyActivitySecret(long lobbyId) {
        Object ret = this.core.execute(() -> this.getLobbyActivitySecret(this.pointer, lobbyId));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (String)ret;
    }

    public String getLobbyActivitySecret(Lobby lobby) {
        return this.getLobbyActivitySecret(lobby.getId());
    }

    public String getLobbyMetadataValue(long lobbyId, String key) {
        if (key.getBytes().length >= 256) {
            throw new IllegalArgumentException("max key length is 255");
        }
        Object ret = this.core.execute(() -> this.getLobbyMetadataValue(this.pointer, lobbyId, key));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (String)ret;
    }

    public String getLobbyMetadataValue(Lobby lobby, String key) {
        return this.getLobbyMetadataValue(lobby.getId(), key);
    }

    public String getLobbyMetadataKey(long lobbyId, int index) {
        Object ret = this.core.execute(() -> this.getLobbyMetadataKey(this.pointer, lobbyId, index));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (String)ret;
    }

    public String getLobbyMetadataKey(Lobby lobby, int index) {
        return this.getLobbyMetadataKey(lobby.getId(), index);
    }

    public int lobbyMetadataCount(long lobbyId) {
        Object ret = this.core.execute(() -> this.lobbyMetadataCount(this.pointer, lobbyId));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (Integer)ret;
    }

    public int lobbyMetadataCount(Lobby lobby) {
        return this.lobbyMetadataCount(lobby.getId());
    }

    public Map<String, String> getLobbyMetadata(long lobbyId) {
        int count = this.lobbyMetadataCount(lobbyId);
        HashMap<String, String> map = new HashMap<String, String>(count);
        for (int i = 0; i < count; ++i) {
            String key = this.getLobbyMetadataKey(lobbyId, i);
            String value = this.getLobbyMetadataValue(lobbyId, key);
            map.put(key, value);
        }
        return Collections.unmodifiableMap(map);
    }

    public Map<String, String> getLobbyMetadata(Lobby lobby) {
        return this.getLobbyMetadata(lobby.getId());
    }

    public int memberCount(long lobbyId) {
        Object ret = this.core.execute(() -> this.memberCount(this.pointer, lobbyId));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (Integer)ret;
    }

    public int memberCount(Lobby lobby) {
        return this.memberCount(lobby.getId());
    }

    public long getMemberUserId(long lobbyId, int index) {
        Object ret = this.core.execute(() -> this.getMemberUserId(this.pointer, lobbyId, index));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (Long)ret;
    }

    public long getMemberUserId(Lobby lobby, int index) {
        return this.getMemberUserId(lobby.getId(), index);
    }

    public List<Long> getMemberUserIds(long lobbyId) {
        List list = IntStream.range(0, this.memberCount(lobbyId)).mapToLong(i -> this.getMemberUserId(lobbyId, i)).boxed().collect(Collectors.toList());
        return Collections.unmodifiableList(list);
    }

    public List<Long> getMemberUserIds(Lobby lobby) {
        return this.getMemberUserIds(lobby.getId());
    }

    public DiscordUser getMemberUser(long lobbyId, long userId) {
        Object ret = this.core.execute(() -> this.getMemberUser(this.pointer, lobbyId, userId));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (DiscordUser)ret;
    }

    public DiscordUser getMemberUser(Lobby lobby, long userId) {
        return this.getMemberUser(lobby.getId(), userId);
    }

    public List<DiscordUser> getMemberUsers(long lobbyId) {
        List list = this.getMemberUserIds(lobbyId).stream().map(l -> this.getMemberUser(lobbyId, (long)l)).collect(Collectors.toList());
        return Collections.unmodifiableList(list);
    }

    public List<DiscordUser> getMemberUsers(Lobby lobby) {
        return this.getMemberUsers(lobby.getId());
    }

    public String getMemberMetadataValue(long lobbyId, long userId, String key) {
        if (key.getBytes().length >= 256) {
            throw new IllegalArgumentException("max key length is 255");
        }
        Object ret = this.core.execute(() -> this.getMemberMetadataValue(this.pointer, lobbyId, userId, key));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (String)ret;
    }

    public String getMemberMetadataValue(Lobby lobby, long userId, String key) {
        return this.getMemberMetadataValue(lobby.getId(), userId, key);
    }

    public String getMemberMetadataKey(long lobbyId, long userId, int index) {
        Object ret = this.core.execute(() -> this.getMemberMetadataKey(this.pointer, lobbyId, userId, index));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (String)ret;
    }

    public String getMemberMetadataKey(Lobby lobby, long userId, int index) {
        return this.getMemberMetadataKey(lobby.getId(), userId, index);
    }

    public int memberMetadataCount(long lobbyId, long userId) {
        Object ret = this.core.execute(() -> this.memberMetadataCount(this.pointer, lobbyId, userId));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (Integer)ret;
    }

    public int memberMetadataCount(Lobby lobby, long userId) {
        return this.memberMetadataCount(lobby.getId(), userId);
    }

    public Map<String, String> getMemberMetadata(long lobbyId, long userId) {
        int count = this.memberMetadataCount(lobbyId, userId);
        HashMap<String, String> map = new HashMap<String, String>(count);
        for (int i = 0; i < count; ++i) {
            String key = this.getMemberMetadataKey(lobbyId, userId, i);
            String value = this.getMemberMetadataValue(lobbyId, userId, key);
            map.put(key, value);
        }
        return Collections.unmodifiableMap(map);
    }

    public Map<String, String> getMemberMetadata(Lobby lobby, long userId) {
        return this.getMemberMetadata(lobby.getId(), userId);
    }

    public void updateMember(long lobbyId, long userId, LobbyMemberTransaction transaction, Consumer<Result> callback) {
        this.core.execute(() -> this.updateMember(this.pointer, lobbyId, userId, transaction.getPointer(), Objects.requireNonNull(callback)));
    }

    public void updateMember(long lobbyId, long userId, LobbyMemberTransaction transaction) {
        this.updateMember(lobbyId, userId, transaction, Core.DEFAULT_CALLBACK);
    }

    public void updateMember(Lobby lobby, long userId, LobbyMemberTransaction transaction, Consumer<Result> callback) {
        this.updateMember(lobby.getId(), userId, transaction, callback);
    }

    public void updateMember(Lobby lobby, long userId, LobbyMemberTransaction transaction) {
        this.updateMember(lobby, userId, transaction, Core.DEFAULT_CALLBACK);
    }

    public void sendLobbyMessage(long lobbyId, byte[] data, Consumer<Result> callback) {
        this.core.execute(() -> this.sendLobbyMessage(this.pointer, lobbyId, data, 0, data.length, Objects.requireNonNull(callback)));
    }

    public void sendLobbyMessage(long lobbyId, byte[] data) {
        this.sendLobbyMessage(lobbyId, data, Core.DEFAULT_CALLBACK);
    }

    public void sendLobbyMessage(Lobby lobby, byte[] data, Consumer<Result> callback) {
        this.sendLobbyMessage(lobby.getId(), data, callback);
    }

    public void sendLobbyMessage(Lobby lobby, byte[] data) {
        this.sendLobbyMessage(lobby, data, Core.DEFAULT_CALLBACK);
    }

    public LobbySearchQuery getSearchQuery() {
        Object ret = this.core.execute(() -> this.getSearchQuery(this.pointer));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (LobbySearchQuery)ret;
    }

    public void search(LobbySearchQuery query, Consumer<Result> callback) {
        this.core.execute(() -> this.search(this.pointer, query.getPointer(), Objects.requireNonNull(callback)));
    }

    public void search(LobbySearchQuery query) {
        this.search(query, Core.DEFAULT_CALLBACK);
    }

    public int lobbyCount() {
        return this.core.execute(() -> this.lobbyCount(this.pointer));
    }

    public long getLobbyId(int index) {
        Object ret = this.core.execute(() -> this.getLobbyId(this.pointer, index));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (Long)ret;
    }

    public List<Long> getLobbyIds() {
        List list = IntStream.range(0, this.lobbyCount()).mapToLong(this::getLobbyId).boxed().collect(Collectors.toList());
        return Collections.unmodifiableList(list);
    }

    public List<Lobby> getLobbies() {
        List list = this.getLobbyIds().stream().map(this::getLobby).collect(Collectors.toList());
        return Collections.unmodifiableList(list);
    }

    public void connectVoice(long lobbyId, Consumer<Result> callback) {
        this.core.execute(() -> this.connectVoice(this.pointer, lobbyId, Objects.requireNonNull(callback)));
    }

    public void connectVoice(long lobbyId) {
        this.connectVoice(lobbyId, Core.DEFAULT_CALLBACK);
    }

    public void connectVoice(Lobby lobby, Consumer<Result> callback) {
        this.connectVoice(lobby.getId(), callback);
    }

    public void connectVoice(Lobby lobby) {
        this.connectVoice(lobby, Core.DEFAULT_CALLBACK);
    }

    public void disconnectVoice(long lobbyId, Consumer<Result> callback) {
        this.core.execute(() -> this.disconnectVoice(this.pointer, lobbyId, Objects.requireNonNull(callback)));
    }

    public void disconnectVoice(long lobbyId) {
        this.disconnectVoice(lobbyId, Core.DEFAULT_CALLBACK);
    }

    public void disconnectVoice(Lobby lobby, Consumer<Result> callback) {
        this.disconnectVoice(lobby.getId(), callback);
    }

    public void disconnectVoice(Lobby lobby) {
        this.disconnectVoice(lobby, Core.DEFAULT_CALLBACK);
    }

    public void connectNetwork(long lobbyId) {
        Result result = this.core.execute(() -> this.connectNetwork(this.pointer, lobbyId));
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    public void connectNetwork(Lobby lobby) {
        this.connectNetwork(lobby.getId());
    }

    public void disconnectNetwork(long lobbyId) {
        Result result = this.core.execute(() -> this.disconnectNetwork(this.pointer, lobbyId));
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    public void disconnectNetwork(Lobby lobby) {
        this.disconnectNetwork(lobby.getId());
    }

    public void flushNetwork() {
        Result result = this.core.execute(() -> this.flushNetwork(this.pointer));
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    public void openNetworkChannel(long lobbyId, byte channelId, boolean reliable) {
        Result result = this.core.execute(() -> this.openNetworkChannel(this.pointer, lobbyId, channelId, reliable));
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    public void openNetworkChannel(Lobby lobby, byte channelId, boolean reliable) {
        this.openNetworkChannel(lobby.getId(), channelId, reliable);
    }

    public void sendNetworkMessage(long lobbyId, long userId, byte channelId, byte[] data) {
        Result result = this.core.execute(() -> this.sendNetworkMessage(this.pointer, lobbyId, userId, channelId, data, 0, data.length));
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    public void sendNetworkMessage(Lobby lobby, long userId, byte channelId, byte[] data) {
        this.sendNetworkMessage(lobby.getId(), userId, channelId, data);
    }

    private native Object getLobbyCreateTransaction(long var1);

    private native Object getLobbyUpdateTransaction(long var1, long var3);

    private native Object getMemberUpdateTransaction(long var1, long var3, long var5);

    private native void createLobby(long var1, long var3, BiConsumer<Result, Lobby> var5);

    private native void updateLobby(long var1, long var3, long var5, Consumer<Result> var7);

    private native void deleteLobby(long var1, long var3, Consumer<Result> var5);

    private native void connectLobby(long var1, long var3, String var5, BiConsumer<Result, Lobby> var6);

    private native void connectLobbyWithActivitySecret(long var1, String var3, BiConsumer<Result, Lobby> var4);

    private native void disconnectLobby(long var1, long var3, Consumer<Result> var5);

    private native Object getLobby(long var1, long var3);

    private native Object getLobbyActivitySecret(long var1, long var3);

    private native Object getLobbyMetadataValue(long var1, long var3, String var5);

    private native Object getLobbyMetadataKey(long var1, long var3, int var5);

    private native Object lobbyMetadataCount(long var1, long var3);

    private native Object memberCount(long var1, long var3);

    private native Object getMemberUserId(long var1, long var3, int var5);

    private native Object getMemberUser(long var1, long var3, long var5);

    private native Object getMemberMetadataValue(long var1, long var3, long var5, String var7);

    private native Object getMemberMetadataKey(long var1, long var3, long var5, int var7);

    private native Object memberMetadataCount(long var1, long var3, long var5);

    private native void updateMember(long var1, long var3, long var5, long var7, Consumer<Result> var9);

    private native void sendLobbyMessage(long var1, long var3, byte[] var5, int var6, int var7, Consumer<Result> var8);

    private native Object getSearchQuery(long var1);

    private native void search(long var1, long var3, Consumer<Result> var5);

    private native int lobbyCount(long var1);

    private native Object getLobbyId(long var1, int var3);

    private native void connectVoice(long var1, long var3, Consumer<Result> var5);

    private native void disconnectVoice(long var1, long var3, Consumer<Result> var5);

    private native Result connectNetwork(long var1, long var3);

    private native Result disconnectNetwork(long var1, long var3);

    private native Result flushNetwork(long var1);

    private native Result openNetworkChannel(long var1, long var3, byte var5, boolean var6);

    private native Result sendNetworkMessage(long var1, long var3, long var5, byte var7, byte[] var8, int var9, int var10);
}


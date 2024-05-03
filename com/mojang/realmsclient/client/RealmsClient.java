/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.realmsclient.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.realmsclient.RealmsVersion;
import com.mojang.realmsclient.client.RealmsClientConfig;
import com.mojang.realmsclient.client.RealmsError;
import com.mojang.realmsclient.client.Request;
import com.mojang.realmsclient.dto.BackupList;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PendingInvitesList;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsDescriptionDto;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.dto.RealmsServerList;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.RealmsWorldResetDto;
import com.mojang.realmsclient.dto.ServerActivityList;
import com.mojang.realmsclient.dto.Subscription;
import com.mojang.realmsclient.dto.UploadInfo;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.dto.WorldTemplateList;
import com.mojang.realmsclient.exception.RealmsHttpException;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsSharedConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsClient {
    private static final Logger LOGGER = LogManager.getLogger();
    private final String sessionId;
    private final String username;
    private static String baseUrl = "mcoapi.minecraft.net";
    private static String protocol = "https";
    private static final String WORLDS_RESOURCE_PATH = "worlds";
    private static final String INVITES_RESOURCE_PATH = "invites";
    private static final String MCO_RESOURCE_PATH = "mco";
    private static final String SUBSCRIPTION_RESOURCE = "subscriptions";
    private static final String ACTIVITIES_RESOURCE = "activities";
    private static final String OPS_RESOURCE = "ops";
    private static final String REGIONS_RESOURCE = "regions/ping/stat";
    private static final String TRIALS_RESOURCE = "trial";
    private static final String PATH_INITIALIZE = "/$WORLD_ID/initialize";
    private static final String PATH_GET_ACTIVTIES = "/$WORLD_ID";
    private static final String PATH_GET_LIVESTATS = "/liveplayerlist";
    private static final String PATH_GET_SUBSCRIPTION = "/$WORLD_ID";
    private static final String PATH_GET_MINIGAMES = "/minigames";
    private static final String PATH_OP = "/$WORLD_ID/$PROFILE_UUID";
    private static final String PATH_PUT_INTO_MINIGAMES_MODE = "/minigames/$MINIGAME_ID/$WORLD_ID";
    private static final String PATH_AVAILABLE = "/available";
    private static final String PATH_TEMPLATES = "/templates";
    private static final String PATH_WORLD_JOIN = "/$ID/join";
    private static final String PATH_WORLD_GET = "/$ID";
    private static final String PATH_WORLD_INVITES = "/$WORLD_ID";
    private static final String PATH_WORLD_UNINVITE = "/$WORLD_ID/invite/$UUID";
    private static final String PATH_PENDING_INVITES_COUNT = "/count/pending";
    private static final String PATH_PENDING_INVITES = "/pending";
    private static final String PATH_ACCEPT_INVITE = "/accept/$INVITATION_ID";
    private static final String PATH_REJECT_INVITE = "/reject/$INVITATION_ID";
    private static final String PATH_UNINVITE_MYSELF = "/$WORLD_ID";
    private static final String PATH_WORLD_UPDATE = "/$WORLD_ID";
    private static final String PATH_SLOT = "/$WORLD_ID/slot/$SLOT_ID";
    private static final String PATH_WORLD_OPEN = "/$WORLD_ID/open";
    private static final String PATH_WORLD_CLOSE = "/$WORLD_ID/close";
    private static final String PATH_WORLD_RESET = "/$WORLD_ID/reset";
    private static final String PATH_DELETE_WORLD = "/$WORLD_ID";
    private static final String PATH_WORLD_BACKUPS = "/$WORLD_ID/backups";
    private static final String PATH_WORLD_DOWNLOAD = "/$WORLD_ID/backups/download/full";
    private static final String PATH_WORLD_UPLOAD = "/$WORLD_ID/backups/upload";
    private static final String PATH_CLIENT_COMPATIBLE = "/client/compatible";
    private static final String PATH_TOS_AGREED = "/tos/agreed";
    private static final String PATH_STAGE_AVAILABLE = "/stageAvailable";
    private static Gson gson = new Gson();

    public static RealmsClient createRealmsClient() {
        String username = Realms.userName();
        String sessionId = Realms.sessionId();
        if (username == null || sessionId == null) {
            return null;
        }
        return new RealmsClient(sessionId, username, Realms.getProxy());
    }

    public static void switchToStage() {
        baseUrl = "mcoapi-stage.minecraft.net";
        protocol = "https";
    }

    public static void switchToProd() {
        baseUrl = "mcoapi.minecraft.net";
        protocol = "https";
    }

    public static void switchToLocal() {
        baseUrl = "localhost:8080";
        protocol = "http";
    }

    public RealmsClient(String sessionId, String username, Proxy proxy) {
        this.sessionId = sessionId;
        this.username = username;
        RealmsClientConfig.setProxy(proxy);
    }

    public RealmsServerList listWorlds() throws RealmsServiceException, IOException {
        String asciiUrl = this.url(WORLDS_RESOURCE_PATH);
        String json = this.execute(Request.get(asciiUrl));
        return RealmsServerList.parse(json);
    }

    public RealmsServer getOwnWorld(long worldId) throws RealmsServiceException, IOException {
        String asciiUrl = this.url(WORLDS_RESOURCE_PATH + PATH_WORLD_GET.replace("$ID", String.valueOf(worldId)));
        String json = this.execute(Request.get(asciiUrl));
        return RealmsServer.parse(json);
    }

    public ServerActivityList getActivity(long worldId) throws RealmsServiceException {
        String asciiUrl = this.url(ACTIVITIES_RESOURCE + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(worldId)));
        String json = this.execute(Request.get(asciiUrl));
        return ServerActivityList.parse(json);
    }

    public RealmsServerPlayerLists getLiveStats() throws RealmsServiceException {
        String asciiUrl = this.url("activities/liveplayerlist");
        String json = this.execute(Request.get(asciiUrl));
        return RealmsServerPlayerLists.parse(json);
    }

    public RealmsServerAddress join(long worldId) throws RealmsServiceException, IOException {
        String asciiUrl = this.url(WORLDS_RESOURCE_PATH + PATH_WORLD_JOIN.replace("$ID", "" + worldId));
        String json = this.execute(Request.get(asciiUrl, 5000, 30000));
        return RealmsServerAddress.parse(json);
    }

    public void initializeWorld(long worldId, String name, String motd) throws RealmsServiceException, IOException {
        RealmsDescriptionDto realmsDescription = new RealmsDescriptionDto(name, motd);
        String asciiUrl = this.url(WORLDS_RESOURCE_PATH + PATH_INITIALIZE.replace("$WORLD_ID", String.valueOf(worldId)));
        String json = gson.toJson(realmsDescription);
        this.execute(Request.post(asciiUrl, json, 5000, 10000));
    }

    public Boolean mcoEnabled() throws RealmsServiceException, IOException {
        String asciiUrl = this.url("mco/available");
        String json = this.execute(Request.get(asciiUrl));
        return Boolean.valueOf(json);
    }

    public Boolean stageAvailable() throws RealmsServiceException, IOException {
        String asciiUrl = this.url("mco/stageAvailable");
        String json = this.execute(Request.get(asciiUrl));
        return Boolean.valueOf(json);
    }

    public CompatibleVersionResponse clientCompatible() throws RealmsServiceException, IOException {
        CompatibleVersionResponse result;
        String asciiUrl = this.url("mco/client/compatible");
        String response = this.execute(Request.get(asciiUrl));
        try {
            result = CompatibleVersionResponse.valueOf(response);
        } catch (IllegalArgumentException e) {
            throw new RealmsServiceException(500, "Could not check compatible version, got response: " + response, -1, "");
        }
        return result;
    }

    public void uninvite(long worldId, String profileUuid) throws RealmsServiceException {
        String asciiUrl = this.url(INVITES_RESOURCE_PATH + PATH_WORLD_UNINVITE.replace("$WORLD_ID", String.valueOf(worldId)).replace("$UUID", profileUuid));
        this.execute(Request.delete(asciiUrl));
    }

    public void uninviteMyselfFrom(long worldId) throws RealmsServiceException {
        String asciiUrl = this.url(INVITES_RESOURCE_PATH + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(worldId)));
        this.execute(Request.delete(asciiUrl));
    }

    public RealmsServer invite(long worldId, String profileName) throws RealmsServiceException, IOException {
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.setName(profileName);
        String asciiUrl = this.url(INVITES_RESOURCE_PATH + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(worldId)));
        String json = this.execute(Request.post(asciiUrl, gson.toJson(playerInfo)));
        return RealmsServer.parse(json);
    }

    public BackupList backupsFor(long worldId) throws RealmsServiceException {
        String asciiUrl = this.url(WORLDS_RESOURCE_PATH + PATH_WORLD_BACKUPS.replace("$WORLD_ID", String.valueOf(worldId)));
        String json = this.execute(Request.get(asciiUrl));
        return BackupList.parse(json);
    }

    public void update(long worldId, String name, String motd) throws RealmsServiceException, UnsupportedEncodingException {
        RealmsDescriptionDto realmsDescription = new RealmsDescriptionDto(name, motd);
        String asciiUrl = this.url(WORLDS_RESOURCE_PATH + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(worldId)));
        this.execute(Request.post(asciiUrl, gson.toJson(realmsDescription)));
    }

    public void updateSlot(long worldId, int slot, RealmsWorldOptions options) throws RealmsServiceException, UnsupportedEncodingException {
        String asciiUrl = this.url(WORLDS_RESOURCE_PATH + PATH_SLOT.replace("$WORLD_ID", String.valueOf(worldId)).replace("$SLOT_ID", String.valueOf(slot)));
        String json = options.toJson();
        this.execute(Request.post(asciiUrl, json));
    }

    public boolean switchSlot(long worldId, int slot) throws RealmsServiceException {
        String asciiUrl = this.url(WORLDS_RESOURCE_PATH + PATH_SLOT.replace("$WORLD_ID", String.valueOf(worldId)).replace("$SLOT_ID", String.valueOf(slot)));
        String json = this.execute(Request.put(asciiUrl, ""));
        return Boolean.valueOf(json);
    }

    public void restoreWorld(long worldId, String backupId) throws RealmsServiceException {
        String asciiUrl = this.url(WORLDS_RESOURCE_PATH + PATH_WORLD_BACKUPS.replace("$WORLD_ID", String.valueOf(worldId)), "backupId=" + backupId);
        this.execute(Request.put(asciiUrl, "", 40000, 40000));
    }

    public WorldTemplateList fetchWorldTemplates() throws RealmsServiceException {
        String asciiUrl = this.url("worlds/templates");
        String json = this.execute(Request.get(asciiUrl));
        return WorldTemplateList.parse(json);
    }

    public WorldTemplateList fetchMinigames() throws RealmsServiceException {
        String asciiUrl = this.url("worlds/minigames");
        String json = this.execute(Request.get(asciiUrl));
        return WorldTemplateList.parse(json);
    }

    public Boolean putIntoMinigameMode(long worldId, String minigameId) throws RealmsServiceException {
        String path = PATH_PUT_INTO_MINIGAMES_MODE.replace("$MINIGAME_ID", minigameId).replace("$WORLD_ID", String.valueOf(worldId));
        String asciiUrl = this.url(WORLDS_RESOURCE_PATH + path);
        return Boolean.valueOf(this.execute(Request.put(asciiUrl, "")));
    }

    public Ops op(long worldId, String profileUuid) throws RealmsServiceException {
        String path = PATH_OP.replace("$WORLD_ID", String.valueOf(worldId)).replace("$PROFILE_UUID", profileUuid);
        String asciiUrl = this.url(OPS_RESOURCE + path);
        return Ops.parse(this.execute(Request.post(asciiUrl, "")));
    }

    public Ops deop(long worldId, String profileUuid) throws RealmsServiceException {
        String path = PATH_OP.replace("$WORLD_ID", String.valueOf(worldId)).replace("$PROFILE_UUID", profileUuid);
        String asciiUrl = this.url(OPS_RESOURCE + path);
        return Ops.parse(this.execute(Request.delete(asciiUrl)));
    }

    public Boolean open(long worldId) throws RealmsServiceException, IOException {
        String asciiUrl = this.url(WORLDS_RESOURCE_PATH + PATH_WORLD_OPEN.replace("$WORLD_ID", String.valueOf(worldId)));
        String json = this.execute(Request.put(asciiUrl, ""));
        return Boolean.valueOf(json);
    }

    public Boolean close(long worldId) throws RealmsServiceException, IOException {
        String asciiUrl = this.url(WORLDS_RESOURCE_PATH + PATH_WORLD_CLOSE.replace("$WORLD_ID", String.valueOf(worldId)));
        String json = this.execute(Request.put(asciiUrl, ""));
        return Boolean.valueOf(json);
    }

    public Boolean resetWorldWithSeed(long worldId, String seed, Integer levelType, boolean generateStructures) throws RealmsServiceException, IOException {
        RealmsWorldResetDto worldReset = new RealmsWorldResetDto(seed, -1L, levelType, generateStructures);
        String asciiUrl = this.url(WORLDS_RESOURCE_PATH + PATH_WORLD_RESET.replace("$WORLD_ID", String.valueOf(worldId)));
        String json = this.execute(Request.post(asciiUrl, gson.toJson(worldReset), 30000, 80000));
        return Boolean.valueOf(json);
    }

    public Boolean resetWorldWithTemplate(long worldId, String worldTemplateId) throws RealmsServiceException, IOException {
        RealmsWorldResetDto worldReset = new RealmsWorldResetDto(null, Long.valueOf(worldTemplateId), -1, false);
        String asciiUrl = this.url(WORLDS_RESOURCE_PATH + PATH_WORLD_RESET.replace("$WORLD_ID", String.valueOf(worldId)));
        String json = this.execute(Request.post(asciiUrl, gson.toJson(worldReset), 30000, 80000));
        return Boolean.valueOf(json);
    }

    public Subscription subscriptionFor(long worldId) throws RealmsServiceException, IOException {
        String asciiUrl = this.url(SUBSCRIPTION_RESOURCE + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(worldId)));
        String json = this.execute(Request.get(asciiUrl));
        return Subscription.parse(json);
    }

    public int pendingInvitesCount() throws RealmsServiceException {
        String asciiUrl = this.url("invites/count/pending");
        String json = this.execute(Request.get(asciiUrl));
        return Integer.parseInt(json);
    }

    public PendingInvitesList pendingInvites() throws RealmsServiceException {
        String asciiUrl = this.url("invites/pending");
        String json = this.execute(Request.get(asciiUrl));
        return PendingInvitesList.parse(json);
    }

    public void acceptInvitation(String invitationId) throws RealmsServiceException {
        String asciiUrl = this.url(INVITES_RESOURCE_PATH + PATH_ACCEPT_INVITE.replace("$INVITATION_ID", invitationId));
        this.execute(Request.put(asciiUrl, ""));
    }

    public WorldDownload download(long worldId) throws RealmsServiceException {
        String asciiUrl = this.url(WORLDS_RESOURCE_PATH + PATH_WORLD_DOWNLOAD.replace("$WORLD_ID", String.valueOf(worldId)));
        String json = this.execute(Request.get(asciiUrl));
        return WorldDownload.parse(json);
    }

    public UploadInfo upload(long worldId, String uploadToken) throws RealmsServiceException {
        String asciiUrl = this.url(WORLDS_RESOURCE_PATH + PATH_WORLD_UPLOAD.replace("$WORLD_ID", String.valueOf(worldId)));
        UploadInfo oldUploadInfo = new UploadInfo();
        if (uploadToken != null) {
            oldUploadInfo.setToken(uploadToken);
        }
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        Gson the_gson = builder.create();
        String content = the_gson.toJson(oldUploadInfo);
        return UploadInfo.parse(this.execute(Request.put(asciiUrl, content)));
    }

    public void rejectInvitation(String invitationId) throws RealmsServiceException {
        String asciiUrl = this.url(INVITES_RESOURCE_PATH + PATH_REJECT_INVITE.replace("$INVITATION_ID", invitationId));
        this.execute(Request.put(asciiUrl, ""));
    }

    public void agreeToTos() throws RealmsServiceException {
        String asciiUrl = this.url("mco/tos/agreed");
        this.execute(Request.post(asciiUrl, ""));
    }

    public void sendPingResults(PingResult pingResult) throws RealmsServiceException {
        String asciiUrl = this.url(REGIONS_RESOURCE);
        this.execute(Request.post(asciiUrl, gson.toJson(pingResult)));
    }

    public Boolean trialAvailable() throws RealmsServiceException, IOException {
        String asciiUrl = this.url(TRIALS_RESOURCE);
        String json = this.execute(Request.get(asciiUrl));
        return Boolean.valueOf(json);
    }

    public RealmsServer createTrial(String name, String motd) throws RealmsServiceException, IOException {
        RealmsDescriptionDto realmsDescription = new RealmsDescriptionDto(name, motd);
        String json = gson.toJson(realmsDescription);
        String asciiUrl = this.url(TRIALS_RESOURCE);
        String returnJson = this.execute(Request.post(asciiUrl, json, 5000, 10000));
        return RealmsServer.parse(returnJson);
    }

    public void deleteWorld(long worldId) throws RealmsServiceException, IOException {
        String asciiUrl = this.url(WORLDS_RESOURCE_PATH + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(worldId)));
        this.execute(Request.delete(asciiUrl));
    }

    private String url(String path) {
        return this.url(path, null);
    }

    private String url(String path, String queryString) {
        try {
            URI uri = new URI(protocol, baseUrl, "/" + path, queryString, null);
            return uri.toASCIIString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String execute(Request<?> r) throws RealmsServiceException {
        r.cookie("sid", this.sessionId);
        r.cookie("user", this.username);
        r.cookie("version", RealmsSharedConstants.VERSION_STRING);
        String realmsVersion = RealmsVersion.getVersion();
        if (realmsVersion != null) {
            r.cookie("realms_version", realmsVersion);
        }
        try {
            int responseCode = r.responseCode();
            if (responseCode == 503) {
                int pauseTime = r.getRetryAfterHeader();
                throw new RetryCallException(pauseTime);
            }
            String responseText = r.text();
            if (responseCode < 200 || responseCode >= 300) {
                if (responseCode == 401) {
                    String authenticationHeader = r.getHeader("WWW-Authenticate");
                    LOGGER.info("Could not authorize you against Realms server: " + authenticationHeader);
                    throw new RealmsServiceException(responseCode, authenticationHeader, -1, authenticationHeader);
                }
                if (responseText == null || responseText.length() == 0) {
                    LOGGER.error("Realms error code: " + responseCode + " message: " + responseText);
                    throw new RealmsServiceException(responseCode, responseText, responseCode, "");
                }
                RealmsError error = new RealmsError(responseText);
                LOGGER.error("Realms http code: " + responseCode + " -  error code: " + error.getErrorCode() + " -  message: " + error.getErrorMessage());
                throw new RealmsServiceException(responseCode, responseText, error);
            }
            return responseText;
        } catch (RealmsHttpException e) {
            throw new RealmsServiceException(500, "Could not connect to Realms: " + e.getMessage(), -1, "");
        }
    }

    public static enum CompatibleVersionResponse {
        COMPATIBLE,
        OUTDATED,
        OTHER;

    }
}


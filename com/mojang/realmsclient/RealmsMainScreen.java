/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.realmsclient;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.RealmsVersion;
import com.mojang.realmsclient.client.Ping;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerPlayerList;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.dto.RegionPingResult;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.screens.RealmsClientOutdatedScreen;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsCreateRealmScreen;
import com.mojang.realmsclient.gui.screens.RealmsCreateTrialScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsParentalConsentScreen;
import com.mojang.realmsclient.gui.screens.RealmsPendingInvitesScreen;
import com.mojang.realmsclient.util.RealmsTasks;
import com.mojang.realmsclient.util.RealmsUtil;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsClickableScrolledSelectionList;
import net.minecraft.realms.RealmsDefaultVertexFormat;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RealmsSharedConstants;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class RealmsMainScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean overrideConfigure = false;
    private static boolean stageEnabled = false;
    private static boolean localEnabled = false;
    private boolean dontSetConnectedToRealms = false;
    protected static final int BUTTON_BACK_ID = 0;
    protected static final int BUTTON_PLAY_ID = 1;
    private static final int LEAVE_ID = 2;
    private static final int BUTTON_BUY_ID = 3;
    private static final int BUTTON_TRY_ID = 4;
    protected static final int RESOURCEPACK_ID = 100;
    private RealmsServer resourcePackServer;
    private static final String ON_ICON_LOCATION = "realms:textures/gui/realms/on_icon.png";
    private static final String OFF_ICON_LOCATION = "realms:textures/gui/realms/off_icon.png";
    private static final String EXPIRED_ICON_LOCATION = "realms:textures/gui/realms/expired_icon.png";
    private static final String EXPIRES_SOON_ICON_LOCATION = "realms:textures/gui/realms/expires_soon_icon.png";
    private static final String LEAVE_ICON_LOCATION = "realms:textures/gui/realms/leave_icon.png";
    private static final String INVITATION_ICONS_LOCATION = "realms:textures/gui/realms/invitation_icons.png";
    private static final String INVITE_ICON_LOCATION = "realms:textures/gui/realms/invite_icon.png";
    private static final String WORLDICON_LOCATION = "realms:textures/gui/realms/world_icon.png";
    private static final String LOGO_LOCATION = "realms:textures/gui/title/realms.png";
    private static final String CONFIGURE_LOCATION = "realms:textures/gui/realms/configure_icon.png";
    private static final String QUESTIONMARK_LOCATION = "realms:textures/gui/realms/questionmark.png";
    private static final String POPUP_LOCATION = "realms:textures/gui/realms/popup.png";
    private static final String DARKEN_LOCATION = "realms:textures/gui/realms/darken.png";
    private static final String CROSS_ICON_LOCATION = "realms:textures/gui/realms/cross_icon.png";
    private static final String TRIAL_ICON_LOCATION = "realms:textures/gui/realms/trial_icon.png";
    private static final String BUTTON_LOCATION = "minecraft:textures/gui/widgets.png";
    private static final String[] IMAGES_LOCATION = new String[]{"realms:textures/gui/realms/images/one.png", "realms:textures/gui/realms/images/two.png", "realms:textures/gui/realms/images/three.png", "realms:textures/gui/realms/images/four.png", "realms:textures/gui/realms/images/five.png"};
    private static RealmsDataFetcher realmsDataFetcher = new RealmsDataFetcher();
    private static int lastScrollYPosition = -1;
    private RealmsScreen lastScreen;
    private volatile ServerSelectionList serverSelectionList;
    private long selectedServerId = -1L;
    private RealmsButton playButton;
    private RealmsButton backButton;
    private String toolTip;
    private List<RealmsServer> realmsServers = Lists.newArrayList();
    private volatile int numberOfPendingInvites = 0;
    private int animTick;
    private static volatile boolean mcoEnabled;
    private static volatile boolean mcoEnabledCheck;
    private static boolean checkedMcoAvailability;
    private boolean hasFetchedServers = false;
    private boolean popupOpenedByUser = false;
    private boolean justClosedPopup = false;
    private volatile boolean trialsAvailable;
    private volatile boolean createdTrial = false;
    private volatile boolean showingPopup = false;
    private int carouselIndex = 0;
    private int carouselTick = 0;
    boolean hasSwitchedCarouselImage = false;
    private static RealmsScreen realmsGenericErrorScreen;
    private static boolean regionsPinged;
    private int mindex = 0;
    private char[] mchars = new char[]{'3', '2', '1', '4', '5', '6'};
    private int sindex = 0;
    private char[] schars = new char[]{'9', '8', '7', '1', '2', '3'};
    private int clicks = 0;
    private int lindex = 0;
    private char[] lchars = new char[]{'9', '8', '7', '4', '5', '6'};
    private static ReentrantLock connectLock;
    private boolean expiredHover = false;
    private boolean updateBreaksAdventureNoteHover = false;

    public RealmsMainScreen(RealmsScreen lastScreen) {
        this.lastScreen = lastScreen;
    }

    @Override
    public void mouseEvent() {
        super.mouseEvent();
        if (!this.shouldShowPopup()) {
            this.serverSelectionList.mouseEvent();
        }
    }

    public boolean shouldShowMessageInList() {
        if (!(mcoEnabled && mcoEnabled && this.hasFetchedServers)) {
            return false;
        }
        if (this.trialsAvailable && !this.createdTrial) {
            return true;
        }
        for (RealmsServer realmsServer : this.realmsServers) {
            if (!realmsServer.ownerUUID.equals(Realms.getUUID())) continue;
            return false;
        }
        return true;
    }

    public boolean shouldShowPopup() {
        if (!(mcoEnabledCheck && mcoEnabled && this.hasFetchedServers)) {
            return false;
        }
        if (this.popupOpenedByUser) {
            return true;
        }
        if (this.trialsAvailable && !this.createdTrial && this.realmsServers.isEmpty()) {
            return true;
        }
        return this.realmsServers.isEmpty();
    }

    @Override
    public void init() {
        connectLock = new ReentrantLock();
        this.checkIfMcoEnabled();
        if (!this.dontSetConnectedToRealms) {
            Realms.setConnectedToRealms(false);
        }
        if (realmsGenericErrorScreen != null) {
            Realms.setScreen(realmsGenericErrorScreen);
            return;
        }
        Keyboard.enableRepeatEvents(true);
        this.buttonsClear();
        if (mcoEnabledCheck && mcoEnabled) {
            realmsDataFetcher.forceUpdate();
        }
        this.showingPopup = false;
        this.postInit();
    }

    public void addButtons() {
        this.playButton = RealmsMainScreen.newButton(1, this.width() / 2 - 98, this.height() - 32, 98, 20, RealmsMainScreen.getLocalizedString("mco.selectServer.play"));
        this.buttonsAdd(this.playButton);
        this.backButton = RealmsMainScreen.newButton(0, this.width() / 2 + 6, this.height() - 32, 98, 20, RealmsMainScreen.getLocalizedString("gui.back"));
        this.buttonsAdd(this.backButton);
        RealmsServer server = this.findServer(this.selectedServerId);
        this.playButton.active(server != null && server.state == RealmsServer.State.OPEN && !server.expired);
    }

    public void postInit() {
        if (mcoEnabledCheck && mcoEnabled && this.hasFetchedServers) {
            this.addButtons();
        }
        this.serverSelectionList = new ServerSelectionList();
        this.serverSelectionList.setLeftPos(-15);
        if (lastScrollYPosition != -1) {
            this.serverSelectionList.scroll(lastScrollYPosition);
        }
    }

    @Override
    public void tick() {
        this.justClosedPopup = false;
        ++this.animTick;
        --this.clicks;
        if (this.clicks < 0) {
            this.clicks = 0;
        }
        if (this.noParentalConsent()) {
            Realms.setScreen(new RealmsParentalConsentScreen(this.lastScreen));
            return;
        }
        if (!mcoEnabledCheck || !mcoEnabled) {
            return;
        }
        realmsDataFetcher.init();
        if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.SERVER_LIST)) {
            List<RealmsServer> newServers = realmsDataFetcher.getServers();
            if (newServers != null) {
                boolean ownsNonExpiredRealmServer = false;
                for (RealmsServer retrievedServer : newServers) {
                    if (!this.isSelfOwnedNonExpiredServer(retrievedServer)) continue;
                    ownsNonExpiredRealmServer = true;
                }
                this.realmsServers = newServers;
                if (!regionsPinged && ownsNonExpiredRealmServer) {
                    regionsPinged = true;
                    this.pingRegions();
                }
            }
            if (!this.hasFetchedServers) {
                this.hasFetchedServers = true;
                this.addButtons();
            }
        }
        if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.PENDING_INVITE)) {
            this.numberOfPendingInvites = realmsDataFetcher.getPendingInvitesCount();
        }
        if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.TRIAL_AVAILABLE) && !this.createdTrial) {
            boolean newStatus = realmsDataFetcher.isTrialAvailable();
            if (newStatus != this.trialsAvailable && this.shouldShowPopup()) {
                this.trialsAvailable = newStatus;
                this.showingPopup = false;
            } else {
                this.trialsAvailable = newStatus;
            }
        }
        if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.LIVE_STATS)) {
            RealmsServerPlayerLists playerLists = realmsDataFetcher.getLivestats();
            block1: for (RealmsServerPlayerList playerList : playerLists.servers) {
                for (RealmsServer server : this.realmsServers) {
                    if (server.id != playerList.serverId) continue;
                    server.updateServerPing(playerList);
                    continue block1;
                }
            }
        }
        realmsDataFetcher.markClean();
        if (this.shouldShowPopup()) {
            ++this.carouselTick;
        }
    }

    private void pingRegions() {
        new Thread(){

            @Override
            public void run() {
                List<RegionPingResult> regionPingResultList = Ping.pingAllRegions();
                RealmsClient client = RealmsClient.createRealmsClient();
                PingResult pingResult = new PingResult();
                pingResult.pingResults = regionPingResultList;
                pingResult.worldIds = RealmsMainScreen.this.getOwnedNonExpiredWorldIds();
                try {
                    client.sendPingResults(pingResult);
                } catch (Throwable t) {
                    LOGGER.warn("Could not send ping result to Realms: ", t);
                }
            }
        }.start();
    }

    private List<Long> getOwnedNonExpiredWorldIds() {
        ArrayList<Long> ids = new ArrayList<Long>();
        for (RealmsServer server : this.realmsServers) {
            if (!this.isSelfOwnedNonExpiredServer(server)) continue;
            ids.add(server.id);
        }
        return ids;
    }

    private boolean noParentalConsent() {
        return mcoEnabledCheck && !mcoEnabled;
    }

    @Override
    public void removed() {
        Keyboard.enableRepeatEvents(false);
        this.stopRealmsFetcher();
    }

    @Override
    public void buttonClicked(RealmsButton button) {
        if (!button.active()) {
            return;
        }
        switch (button.id()) {
            case 1: {
                this.play(this.findServer(this.selectedServerId));
                break;
            }
            case 0: {
                if (this.justClosedPopup) break;
                Realms.setScreen(this.lastScreen);
                break;
            }
            case 4: {
                this.createTrial();
                break;
            }
            case 3: {
                RealmsUtil.browseTo("https://minecraft.net/realms");
                break;
            }
            default: {
                return;
            }
        }
    }

    private void createTrial() {
        if (!this.trialsAvailable || this.createdTrial) {
            return;
        }
        this.createdTrial = true;
        Realms.setScreen(new RealmsCreateTrialScreen(this));
    }

    private void checkIfMcoEnabled() {
        if (!checkedMcoAvailability) {
            checkedMcoAvailability = true;
            new Thread("MCO Availability Checker #1"){

                @Override
                public void run() {
                    RealmsClient client = RealmsClient.createRealmsClient();
                    try {
                        RealmsClient.CompatibleVersionResponse versionResponse = client.clientCompatible();
                        if (versionResponse.equals((Object)RealmsClient.CompatibleVersionResponse.OUTDATED)) {
                            Realms.setScreen(realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen, true));
                            return;
                        }
                        if (versionResponse.equals((Object)RealmsClient.CompatibleVersionResponse.OTHER)) {
                            Realms.setScreen(realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen, false));
                            return;
                        }
                    } catch (RealmsServiceException e) {
                        checkedMcoAvailability = false;
                        LOGGER.error("Couldn't connect to realms: ", new Object[]{e.toString()});
                        if (e.httpResultCode == 401) {
                            realmsGenericErrorScreen = new RealmsGenericErrorScreen(e, RealmsMainScreen.this.lastScreen);
                        }
                        Realms.setScreen(new RealmsGenericErrorScreen(e, RealmsMainScreen.this.lastScreen));
                        return;
                    } catch (IOException e) {
                        checkedMcoAvailability = false;
                        LOGGER.error("Couldn't connect to realms: ", new Object[]{e.getMessage()});
                        Realms.setScreen(new RealmsGenericErrorScreen(e.getMessage(), RealmsMainScreen.this.lastScreen));
                        return;
                    }
                    boolean retry = false;
                    for (int i = 0; i < 3; ++i) {
                        try {
                            Boolean result = client.mcoEnabled();
                            if (result.booleanValue()) {
                                LOGGER.info("Realms is available for this user");
                                mcoEnabled = true;
                            } else {
                                LOGGER.info("Realms is not available for this user");
                                mcoEnabled = false;
                            }
                            mcoEnabledCheck = true;
                        } catch (RetryCallException e) {
                            retry = true;
                        } catch (RealmsServiceException e) {
                            LOGGER.error("Couldn't connect to Realms: " + e.toString());
                        } catch (IOException e) {
                            LOGGER.error("Couldn't parse response connecting to Realms: " + e.getMessage());
                        }
                        if (!retry) break;
                        try {
                            Thread.sleep(5000L);
                            continue;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }.start();
        }
    }

    private void switchToStage() {
        if (!stageEnabled) {
            new Thread("MCO Stage Availability Checker #1"){

                @Override
                public void run() {
                    RealmsClient client = RealmsClient.createRealmsClient();
                    try {
                        Boolean result = client.stageAvailable();
                        if (result.booleanValue()) {
                            RealmsClient.switchToStage();
                            LOGGER.info("Switched to stage");
                            realmsDataFetcher.forceUpdate();
                            localEnabled = false;
                            stageEnabled = true;
                        } else {
                            stageEnabled = false;
                        }
                    } catch (RealmsServiceException e) {
                        LOGGER.error("Couldn't connect to Realms: " + e.toString());
                    } catch (IOException e) {
                        LOGGER.error("Couldn't parse response connecting to Realms: " + e.getMessage());
                    }
                }
            }.start();
        }
    }

    private void switchToLocal() {
        if (!localEnabled) {
            new Thread("MCO Local Availability Checker #1"){

                @Override
                public void run() {
                    RealmsClient client = RealmsClient.createRealmsClient();
                    try {
                        Boolean result = client.stageAvailable();
                        if (result.booleanValue()) {
                            RealmsClient.switchToLocal();
                            LOGGER.info("Switched to local");
                            realmsDataFetcher.forceUpdate();
                            stageEnabled = false;
                            localEnabled = true;
                        } else {
                            localEnabled = false;
                        }
                    } catch (RealmsServiceException e) {
                        LOGGER.error("Couldn't connect to Realms: " + e.toString());
                    } catch (IOException e) {
                        LOGGER.error("Couldn't parse response connecting to Realms: " + e.getMessage());
                    }
                }
            }.start();
        }
    }

    private void switchToProd() {
        if (stageEnabled || localEnabled) {
            stageEnabled = false;
            localEnabled = false;
            RealmsClient.switchToProd();
            realmsDataFetcher.forceUpdate();
        }
    }

    private void stopRealmsFetcher() {
        realmsDataFetcher.stop();
    }

    private void configureClicked(RealmsServer selectedServer) {
        if (Realms.getUUID().equals(selectedServer.ownerUUID) || overrideConfigure) {
            this.saveListScrollPosition();
            Realms.setScreen(new RealmsConfigureWorldScreen(this, selectedServer.id));
        }
    }

    private void leaveClicked(RealmsServer selectedServer) {
        if (!Realms.getUUID().equals(selectedServer.ownerUUID)) {
            this.saveListScrollPosition();
            String line2 = RealmsMainScreen.getLocalizedString("mco.configure.world.leave.question.line1");
            String line3 = RealmsMainScreen.getLocalizedString("mco.configure.world.leave.question.line2");
            Realms.setScreen(new RealmsLongConfirmationScreen(this, RealmsLongConfirmationScreen.Type.Info, line2, line3, true, 2));
        }
    }

    private void saveListScrollPosition() {
        lastScrollYPosition = this.serverSelectionList.getScroll();
    }

    private RealmsServer findServer(long id) {
        for (RealmsServer server : this.realmsServers) {
            if (server.id != id) continue;
            return server;
        }
        return null;
    }

    private int findIndex(long serverId) {
        for (int i = 0; i < this.realmsServers.size(); ++i) {
            if (this.realmsServers.get((int)i).id != serverId) continue;
            return i;
        }
        return -1;
    }

    @Override
    public void confirmResult(boolean result, int id) {
        if (id == 2) {
            if (result) {
                new Thread("Realms-leave-server"){

                    @Override
                    public void run() {
                        try {
                            RealmsServer server = RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId);
                            if (server != null) {
                                RealmsClient client = RealmsClient.createRealmsClient();
                                client.uninviteMyselfFrom(server.id);
                                realmsDataFetcher.removeItem(server);
                                RealmsMainScreen.this.realmsServers.remove(server);
                                RealmsMainScreen.this.selectedServerId = -1L;
                                RealmsMainScreen.this.playButton.active(false);
                            }
                        } catch (RealmsServiceException e) {
                            LOGGER.error("Couldn't configure world");
                            Realms.setScreen(new RealmsGenericErrorScreen(e, (RealmsScreen)RealmsMainScreen.this));
                        }
                    }
                }.start();
            }
            Realms.setScreen(this);
        } else if (id == 100) {
            if (!result) {
                if (connectLock.isHeldByCurrentThread()) {
                    connectLock.unlock();
                }
                Realms.setScreen(this);
            } else {
                this.connectToServer(this.resourcePackServer);
            }
        }
    }

    public void removeSelection() {
        this.selectedServerId = -1L;
    }

    @Override
    public void keyPressed(char ch, int eventKey) {
        switch (eventKey) {
            case 28: 
            case 156: {
                this.mindex = 0;
                this.sindex = 0;
                this.lindex = 0;
                if (this.shouldShowPopup()) {
                    return;
                }
                if (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54)) {
                    RealmsServer server = this.findServer(this.selectedServerId);
                    if (server == null) break;
                    this.configureClicked(server);
                    break;
                }
                this.buttonClicked(this.playButton);
                break;
            }
            case 1: {
                this.mindex = 0;
                this.sindex = 0;
                this.lindex = 0;
                if (this.shouldShowPopup() && this.popupOpenedByUser) {
                    this.popupOpenedByUser = false;
                    break;
                }
                Realms.setScreen(this.lastScreen);
                break;
            }
            case 200: {
                if (this.selectedServerId != -1L && !this.shouldShowPopup()) {
                    int new_index;
                    RealmsServer newServer;
                    RealmsServer server = this.findServer(this.selectedServerId);
                    int the_index = this.realmsServers.indexOf(server);
                    if (the_index == 0) {
                        this.serverSelectionList.scroll(0 - this.serverSelectionList.getScroll());
                        return;
                    }
                    if (server != null && the_index > 0 && (newServer = this.realmsServers.get(new_index = the_index - 1)) != null) {
                        this.selectedServerId = newServer.id;
                        int maxScroll = Math.max(0, this.serverSelectionList.getMaxPosition() - (this.height() - 40 - 32 - 4));
                        int maxItemsInView = (int)Math.floor((this.height() - 40 - 32) / 36);
                        int scroll = this.serverSelectionList.getScroll();
                        int hiddenItems = (int)Math.ceil((float)scroll / 36.0f);
                        int scrollPerItem = maxScroll / this.realmsServers.size();
                        int positionNeeded = scrollPerItem * new_index;
                        int proposedScroll = positionNeeded - this.serverSelectionList.getScroll();
                        if (new_index < hiddenItems || new_index > hiddenItems + maxItemsInView) {
                            this.serverSelectionList.scroll(proposedScroll);
                        }
                        return;
                    }
                }
                if (this.shouldShowPopup() || this.realmsServers.isEmpty()) break;
                this.selectedServerId = this.realmsServers.get((int)0).id;
                this.serverSelectionList.scroll(0 - this.serverSelectionList.getScroll());
                break;
            }
            case 208: {
                if (this.selectedServerId != -1L && !this.shouldShowPopup()) {
                    RealmsServer server = this.findServer(this.selectedServerId);
                    int the_index = this.realmsServers.indexOf(server);
                    int maxScroll = Math.max(0, this.serverSelectionList.getMaxPosition() - (this.height() - 40 - 32));
                    if (the_index == this.realmsServers.size() - 1) {
                        this.serverSelectionList.scroll(maxScroll - this.serverSelectionList.getScroll() + 36);
                        return;
                    }
                    if (server != null && the_index > -1 && the_index < this.realmsServers.size() - 1) {
                        int new_index = the_index + 1;
                        RealmsServer newServer = this.realmsServers.get(new_index);
                        if (new_index == this.realmsServers.size() - 1) {
                            this.selectedServerId = newServer.id;
                            this.serverSelectionList.scroll(maxScroll - this.serverSelectionList.getScroll() + 36);
                            return;
                        }
                        if (newServer != null) {
                            this.selectedServerId = newServer.id;
                            int maxItemsInView = (int)Math.floor((this.height() - 40 - 32) / 36);
                            int scroll = this.serverSelectionList.getScroll();
                            int hiddenItems = (int)Math.ceil((float)scroll / 36.0f);
                            int scrollPerItem = maxScroll / this.realmsServers.size();
                            int positionNeeded = scrollPerItem * new_index;
                            int proposedScroll = positionNeeded - this.serverSelectionList.getScroll();
                            if (proposedScroll > 0) {
                                proposedScroll += scrollPerItem;
                            }
                            if (new_index < hiddenItems || new_index >= hiddenItems + maxItemsInView) {
                                this.serverSelectionList.scroll(proposedScroll);
                            }
                            return;
                        }
                    }
                }
                if (this.shouldShowPopup() || this.realmsServers.isEmpty()) break;
                this.selectedServerId = this.realmsServers.get((int)0).id;
                this.serverSelectionList.scroll(-(this.serverSelectionList.getItemCount() * 36));
                break;
            }
            default: {
                if (this.mchars[this.mindex] == ch) {
                    ++this.mindex;
                    if (this.mindex == this.mchars.length) {
                        this.mindex = 0;
                        overrideConfigure = !overrideConfigure;
                    }
                } else {
                    this.mindex = 0;
                }
                if (this.schars[this.sindex] == ch) {
                    ++this.sindex;
                    if (this.sindex == this.schars.length) {
                        this.sindex = 0;
                        if (!stageEnabled) {
                            this.switchToStage();
                        } else {
                            this.switchToProd();
                        }
                    }
                } else {
                    this.sindex = 0;
                }
                if (this.lchars[this.lindex] == ch) {
                    ++this.lindex;
                    if (this.lindex != this.lchars.length) break;
                    this.lindex = 0;
                    if (!localEnabled) {
                        this.switchToLocal();
                        break;
                    }
                    this.switchToProd();
                    break;
                }
                this.lindex = 0;
            }
        }
    }

    @Override
    public void render(int xm, int ym, float a) {
        this.expiredHover = false;
        this.updateBreaksAdventureNoteHover = false;
        this.toolTip = null;
        this.renderBackground();
        this.serverSelectionList.render(xm, ym, a);
        this.drawRealmsLogo(this.width() / 2 - 50, 7);
        if ((!this.shouldShowPopup() || this.popupOpenedByUser) && mcoEnabledCheck && mcoEnabled && this.hasFetchedServers) {
            this.renderMoreInfo(xm, ym);
        }
        this.drawInvitationPendingIcon(xm, ym);
        if (stageEnabled) {
            this.renderStage();
        }
        if (localEnabled) {
            this.renderLocal();
        }
        if (this.shouldShowPopup()) {
            this.drawPopup(xm, ym);
        } else {
            if (this.showingPopup) {
                this.buttonsClear();
                this.buttonsAdd(this.playButton);
                this.buttonsAdd(this.backButton);
                RealmsServer server = this.findServer(this.selectedServerId);
                this.playButton.active(server != null && server.state == RealmsServer.State.OPEN && !server.expired);
            }
            this.showingPopup = false;
        }
        if (this.toolTip != null) {
            this.renderMousehoverTooltip(this.toolTip, xm, ym);
        }
        super.render(xm, ym, a);
        if (this.trialsAvailable && !this.createdTrial && this.shouldShowPopup()) {
            RealmsScreen.bind(TRIAL_ICON_LOCATION);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glPushMatrix();
            int ySprite = 0;
            if ((System.currentTimeMillis() / 800L & 1L) == 1L) {
                ySprite = 8;
            }
            int yo = this.height() / 2 - 83 - 3;
            int buttonHeight = yo + 147 - 20;
            RealmsScreen.blit(this.width() / 2 + 52 + 83, buttonHeight - 4, 0.0f, ySprite, 8, 8, 8.0f, 16.0f);
            GL11.glPopMatrix();
        }
    }

    private void drawRealmsLogo(int x, int y) {
        RealmsScreen.bind(LOGO_LOCATION);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPushMatrix();
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        RealmsScreen.blit(x * 2, y * 2 - 5, 0.0f, 0.0f, 200, 50, 200.0f, 50.0f);
        GL11.glPopMatrix();
    }

    @Override
    public void mouseClicked(int x, int y, int buttonNum) {
        if (this.inPendingInvitationArea(x, y)) {
            RealmsPendingInvitesScreen pendingInvitationScreen = new RealmsPendingInvitesScreen(this.lastScreen);
            Realms.setScreen(pendingInvitationScreen);
        } else if (this.toolTip != null && this.toolTip.equals(RealmsMainScreen.getLocalizedString("mco.selectServer.info"))) {
            this.popupOpenedByUser = !this.popupOpenedByUser;
        } else if (this.toolTip != null && this.toolTip.equals(RealmsMainScreen.getLocalizedString("mco.selectServer.close"))) {
            this.popupOpenedByUser = false;
        } else if (this.isOutsidePopup(x, y) && this.popupOpenedByUser) {
            this.popupOpenedByUser = false;
            this.justClosedPopup = true;
        }
    }

    private boolean isOutsidePopup(int xm, int ym) {
        int xo = (this.width() - 310) / 2;
        int yo = this.height() / 2 - 83 - 3;
        return xm < xo - 5 || xm > xo + 315 || ym < yo - 5 || ym > yo + 171;
    }

    private void drawPopup(int xm, int ym) {
        int xo = (this.width() - 310) / 2;
        int yo = this.height() / 2 - 83 - 3;
        int buttonHeight = yo + 147 - 20;
        if (!this.showingPopup) {
            this.carouselIndex = 0;
            this.carouselTick = 0;
            this.hasSwitchedCarouselImage = true;
            if (this.hasFetchedServers && this.realmsServers.isEmpty()) {
                this.buttonsClear();
                this.buttonsAdd(RealmsMainScreen.newButton(0, this.width() / 2 - 49, this.height() - 32, 98, 20, RealmsMainScreen.getLocalizedString("gui.back")));
            }
            if (this.trialsAvailable && !this.createdTrial) {
                this.buttonsAdd(RealmsMainScreen.newButton(4, this.width() / 2 + 52, buttonHeight -= 10, 98, 20, RealmsMainScreen.getLocalizedString("mco.selectServer.trial")));
                buttonHeight = yo + 170 - 20 - 10;
            }
            this.buttonsAdd(RealmsMainScreen.newButton(3, this.width() / 2 + 52, buttonHeight, 98, 20, RealmsMainScreen.getLocalizedString("mco.selectServer.buy")));
            this.playButton.active(false);
        }
        if (this.hasFetchedServers) {
            this.showingPopup = true;
        }
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.7f);
        GL11.glEnable(3042);
        RealmsScreen.bind(DARKEN_LOCATION);
        GL11.glPushMatrix();
        int otherxo = 0;
        int otheryo = 32;
        RealmsScreen.blit(otherxo, otheryo, 0.0f, 0.0f, this.width(), this.height() - 40 - 32, 310.0f, 166.0f);
        GL11.glPopMatrix();
        GL11.glDisable(3042);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        RealmsScreen.bind(POPUP_LOCATION);
        GL11.glPushMatrix();
        RealmsScreen.blit(xo, yo, 0.0f, 0.0f, 310, 166, 310.0f, 166.0f);
        GL11.glPopMatrix();
        RealmsScreen.bind(IMAGES_LOCATION[this.carouselIndex]);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPushMatrix();
        RealmsScreen.blit(xo + 7, yo + 7, 0.0f, 0.0f, 195, 152, 195.0f, 152.0f);
        GL11.glPopMatrix();
        if (this.carouselTick % 100 < 5) {
            if (!this.hasSwitchedCarouselImage) {
                this.carouselIndex = this.carouselIndex == IMAGES_LOCATION.length - 1 ? 0 : ++this.carouselIndex;
                this.hasSwitchedCarouselImage = true;
            }
        } else {
            this.hasSwitchedCarouselImage = false;
        }
        if (this.popupOpenedByUser) {
            boolean crossHovered = false;
            int bx = xo + 4;
            int by = yo + 4;
            if (xm >= bx && xm <= bx + 12 && ym >= by && ym <= by + 12) {
                this.toolTip = RealmsMainScreen.getLocalizedString("mco.selectServer.close");
                crossHovered = true;
            }
            RealmsScreen.bind(CROSS_ICON_LOCATION);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glPushMatrix();
            RealmsScreen.blit(bx, by, 0.0f, crossHovered ? 12.0f : 0.0f, 12, 12, 12.0f, 24.0f);
            GL11.glPopMatrix();
            if (crossHovered) {
                this.toolTip = RealmsMainScreen.getLocalizedString("mco.selectServer.close");
            }
        }
        List<String> strings = this.fontSplit(RealmsMainScreen.getLocalizedString("mco.selectServer.popup"), 100);
        int index = 0;
        for (String s : strings) {
            this.drawString(s, this.width() / 2 + 52, yo + 10 * ++index - 3, 0x4C4C4C, false);
        }
    }

    private void drawInvitationPendingIcon(int xm, int ym) {
        int pendingInvitesCount = this.numberOfPendingInvites;
        boolean hovering = this.inPendingInvitationArea(xm, ym);
        int baseX = this.width() / 2 + 50;
        int baseY = 8;
        if (pendingInvitesCount != 0) {
            float scale = 0.25f + (1.0f + RealmsMth.sin((float)this.animTick * 0.5f)) * 0.25f;
            int color = 0xFF000000 | (int)(scale * 64.0f) << 16 | (int)(scale * 64.0f) << 8 | (int)(scale * 64.0f) << 0;
            this.fillGradient(baseX - 2, 6, baseX + 18, 26, color, color);
            color = 0xFF000000 | (int)(scale * 255.0f) << 16 | (int)(scale * 255.0f) << 8 | (int)(scale * 255.0f) << 0;
            this.fillGradient(baseX - 2, 6, baseX + 18, 7, color, color);
            this.fillGradient(baseX - 2, 6, baseX - 1, 26, color, color);
            this.fillGradient(baseX + 17, 6, baseX + 18, 26, color, color);
            this.fillGradient(baseX - 2, 25, baseX + 18, 26, color, color);
        }
        RealmsScreen.bind(INVITE_ICON_LOCATION);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPushMatrix();
        RealmsScreen.blit(baseX, 2, hovering ? 16.0f : 0.0f, 0.0f, 15, 25, 31.0f, 25.0f);
        GL11.glPopMatrix();
        if (pendingInvitesCount != 0) {
            int spritePos = (Math.min(pendingInvitesCount, 6) - 1) * 8;
            int yOff = (int)(Math.max(0.0f, Math.max(RealmsMth.sin((float)(10 + this.animTick) * 0.57f), RealmsMth.cos((float)this.animTick * 0.35f))) * -6.0f);
            RealmsScreen.bind(INVITATION_ICONS_LOCATION);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glPushMatrix();
            RealmsScreen.blit(baseX + 4, 12 + yOff, spritePos, hovering ? 8.0f : 0.0f, 8, 8, 48.0f, 16.0f);
            GL11.glPopMatrix();
        }
        if (hovering) {
            int rx = xm + 12;
            int ry = ym;
            String message = pendingInvitesCount == 0 ? RealmsMainScreen.getLocalizedString("mco.invites.nopending") : RealmsMainScreen.getLocalizedString("mco.invites.pending");
            int width = this.fontWidth(message);
            this.fillGradient(rx - 3, ry - 3, rx + width + 3, ry + 8 + 3, -1073741824, -1073741824);
            this.fontDrawShadow(message, rx, ry, -1);
        }
    }

    private boolean inPendingInvitationArea(int xm, int ym) {
        int x1 = this.width() / 2 + 50;
        int x2 = this.width() / 2 + 66;
        int y1 = 11;
        int y2 = 23;
        if (this.numberOfPendingInvites != 0) {
            x1 -= 3;
            x2 += 3;
            y1 -= 5;
            y2 += 5;
        }
        return x1 <= xm && xm <= x2 && y1 <= ym && ym <= y2;
    }

    public void play(RealmsServer server) {
        if (server != null) {
            try {
                if (!connectLock.tryLock(1L, TimeUnit.SECONDS)) {
                    return;
                }
                if (connectLock.getHoldCount() > 1) {
                    return;
                }
            } catch (InterruptedException e) {
                return;
            }
            this.dontSetConnectedToRealms = true;
            if (server.resourcePackUrl != null && server.resourcePackHash != null) {
                this.resourcePackServer = server;
                this.saveListScrollPosition();
                String line2 = RealmsMainScreen.getLocalizedString("mco.configure.world.resourcepack.question.line1");
                String line3 = RealmsMainScreen.getLocalizedString("mco.configure.world.resourcepack.question.line2");
                Realms.setScreen(new RealmsLongConfirmationScreen(this, RealmsLongConfirmationScreen.Type.Info, line2, line3, true, 100));
            } else {
                this.connectToServer(server);
            }
        }
    }

    private void connectToServer(RealmsServer server) {
        RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this, new RealmsTasks.RealmsConnectTask(this, server));
        longRunningMcoTaskScreen.start();
        Realms.setScreen(longRunningMcoTaskScreen);
    }

    private boolean isSelfOwnedServer(RealmsServer serverData) {
        return serverData.ownerUUID != null && serverData.ownerUUID.equals(Realms.getUUID());
    }

    private boolean isSelfOwnedNonExpiredServer(RealmsServer serverData) {
        return serverData.ownerUUID != null && serverData.ownerUUID.equals(Realms.getUUID()) && !serverData.expired;
    }

    private void drawExpired(int x, int y, int xm, int ym) {
        RealmsScreen.bind(EXPIRED_ICON_LOCATION);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPushMatrix();
        RealmsScreen.blit(x, y, 0.0f, 0.0f, 10, 28, 10.0f, 28.0f);
        GL11.glPopMatrix();
        if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27 && ym < this.height() - 40 && ym > 32 && !this.shouldShowPopup()) {
            this.toolTip = RealmsMainScreen.getLocalizedString("mco.selectServer.expired");
        }
    }

    private void drawExpiring(int x, int y, int xm, int ym, int daysLeft) {
        RealmsScreen.bind(EXPIRES_SOON_ICON_LOCATION);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPushMatrix();
        if (this.animTick % 20 < 10) {
            RealmsScreen.blit(x, y, 0.0f, 0.0f, 10, 28, 20.0f, 28.0f);
        } else {
            RealmsScreen.blit(x, y, 10.0f, 0.0f, 10, 28, 20.0f, 28.0f);
        }
        GL11.glPopMatrix();
        if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27 && ym < this.height() - 40 && ym > 32 && !this.shouldShowPopup()) {
            this.toolTip = daysLeft <= 0 ? RealmsMainScreen.getLocalizedString("mco.selectServer.expires.soon") : (daysLeft == 1 ? RealmsMainScreen.getLocalizedString("mco.selectServer.expires.day") : RealmsMainScreen.getLocalizedString("mco.selectServer.expires.days", daysLeft));
        }
    }

    private void drawOpen(int x, int y, int xm, int ym) {
        RealmsScreen.bind(ON_ICON_LOCATION);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPushMatrix();
        RealmsScreen.blit(x, y, 0.0f, 0.0f, 10, 28, 10.0f, 28.0f);
        GL11.glPopMatrix();
        if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27 && ym < this.height() - 40 && ym > 32 && !this.shouldShowPopup()) {
            this.toolTip = RealmsMainScreen.getLocalizedString("mco.selectServer.open");
        }
    }

    private void drawClose(int x, int y, int xm, int ym) {
        RealmsScreen.bind(OFF_ICON_LOCATION);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPushMatrix();
        RealmsScreen.blit(x, y, 0.0f, 0.0f, 10, 28, 10.0f, 28.0f);
        GL11.glPopMatrix();
        if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27 && ym < this.height() - 40 && ym > 32 && !this.shouldShowPopup()) {
            this.toolTip = RealmsMainScreen.getLocalizedString("mco.selectServer.closed");
        }
    }

    private void drawLeave(int x, int y, int xm, int ym) {
        boolean hovered = false;
        if (xm >= x && xm <= x + 28 && ym >= y && ym <= y + 28 && ym < this.height() - 40 && ym > 32 && !this.shouldShowPopup()) {
            hovered = true;
        }
        RealmsScreen.bind(LEAVE_ICON_LOCATION);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPushMatrix();
        RealmsScreen.blit(x, y, hovered ? 28.0f : 0.0f, 0.0f, 28, 28, 56.0f, 28.0f);
        GL11.glPopMatrix();
        if (hovered) {
            this.toolTip = RealmsMainScreen.getLocalizedString("mco.selectServer.leave");
        }
    }

    private void drawConfigure(int x, int y, int xm, int ym) {
        boolean hovered = false;
        if (xm >= x && xm <= x + 28 && ym >= y && ym <= y + 28 && ym < this.height() - 40 && ym > 32 && !this.shouldShowPopup()) {
            hovered = true;
        }
        RealmsScreen.bind(CONFIGURE_LOCATION);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPushMatrix();
        RealmsScreen.blit(x, y, hovered ? 28.0f : 0.0f, 0.0f, 28, 28, 56.0f, 28.0f);
        GL11.glPopMatrix();
        if (hovered) {
            this.toolTip = RealmsMainScreen.getLocalizedString("mco.selectServer.configure");
        }
    }

    protected void renderMousehoverTooltip(String msg, int x, int y) {
        if (msg == null) {
            return;
        }
        int index = 0;
        int width = 0;
        for (String s : msg.split("\n")) {
            int the_width = this.fontWidth(s);
            if (the_width <= width) continue;
            width = the_width;
        }
        int rx = x - width - 5;
        int ry = y;
        if (rx < 0) {
            rx = x + 12;
        }
        for (String s : msg.split("\n")) {
            this.fillGradient(rx - 3, ry - (index == 0 ? 3 : 0) + index, rx + width + 3, ry + 8 + 3 + index, -1073741824, -1073741824);
            this.fontDrawShadow(s, rx, ry + index, 0xFFFFFF);
            index += 10;
        }
    }

    private void renderMoreInfo(int xm, int ym) {
        int x = this.width() - 17 - 20;
        int y = 6;
        boolean hovered = false;
        if (xm >= x && xm <= x + 20 && ym >= 6 && ym <= 26) {
            hovered = true;
        }
        RealmsScreen.bind(QUESTIONMARK_LOCATION);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPushMatrix();
        RealmsScreen.blit(x, 6, hovered ? 20.0f : 0.0f, 0.0f, 20, 20, 40.0f, 20.0f);
        GL11.glPopMatrix();
        if (hovered) {
            this.toolTip = RealmsMainScreen.getLocalizedString("mco.selectServer.info");
        }
    }

    private void renderLocal() {
        String text = "LOCAL!";
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPushMatrix();
        GL11.glTranslatef(this.width() / 2 - 25, 20.0f, 0.0f);
        GL11.glRotatef(-20.0f, 0.0f, 0.0f, 1.0f);
        GL11.glScalef(1.5f, 1.5f, 1.5f);
        this.drawString(text, 0, 0, 0x7FFF7F);
        GL11.glPopMatrix();
    }

    private void renderStage() {
        String text = "STAGE!";
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPushMatrix();
        GL11.glTranslatef(this.width() / 2 - 25, 20.0f, 0.0f);
        GL11.glRotatef(-20.0f, 0.0f, 0.0f, 1.0f);
        GL11.glScalef(1.5f, 1.5f, 1.5f);
        this.drawString(text, 0, 0, -256);
        GL11.glPopMatrix();
    }

    public RealmsScreen newScreen() {
        return new RealmsMainScreen(this.lastScreen);
    }

    public void closePopup() {
        if (this.shouldShowPopup() && this.popupOpenedByUser) {
            this.popupOpenedByUser = false;
        }
    }

    static {
        realmsGenericErrorScreen = null;
        regionsPinged = false;
        connectLock = new ReentrantLock();
        String version = RealmsVersion.getVersion();
        if (version != null) {
            LOGGER.info("Realms library version == " + version);
        }
    }

    private class ServerSelectionList
    extends RealmsClickableScrolledSelectionList {
        public ServerSelectionList() {
            super(RealmsMainScreen.this.width() + 15, RealmsMainScreen.this.height(), 32, RealmsMainScreen.this.height() - 40, 36);
        }

        @Override
        public int getItemCount() {
            if (RealmsMainScreen.this.shouldShowMessageInList()) {
                return RealmsMainScreen.this.realmsServers.size() + 1;
            }
            return RealmsMainScreen.this.realmsServers.size();
        }

        @Override
        public void selectItem(int item, boolean doubleClick, int xMouse, int yMouse) {
            if (RealmsMainScreen.this.shouldShowMessageInList()) {
                if (item == 0) {
                    RealmsMainScreen.this.popupOpenedByUser = true;
                    return;
                }
                --item;
            }
            if (item >= RealmsMainScreen.this.realmsServers.size()) {
                return;
            }
            RealmsServer server = (RealmsServer)RealmsMainScreen.this.realmsServers.get(item);
            if (server.state == RealmsServer.State.UNINITIALIZED) {
                RealmsMainScreen.this.selectedServerId = -1L;
                Realms.setScreen(new RealmsCreateRealmScreen(server, RealmsMainScreen.this));
            } else {
                RealmsMainScreen.this.selectedServerId = server.id;
            }
            RealmsMainScreen.this.playButton.active(server.state == RealmsServer.State.OPEN && !server.expired);
            if (doubleClick && RealmsMainScreen.this.playButton.active()) {
                RealmsMainScreen.this.play(RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId));
            }
        }

        @Override
        public boolean isSelectedItem(int item) {
            if (RealmsMainScreen.this.shouldShowMessageInList()) {
                if (item == 0) {
                    return false;
                }
                --item;
            }
            return item == RealmsMainScreen.this.findIndex(RealmsMainScreen.this.selectedServerId);
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 36;
        }

        @Override
        public int getScrollbarPosition() {
            return super.getScrollbarPosition() + 15;
        }

        @Override
        protected void renderItem(int i, int x, int y, int h, Tezzelator t, int mouseX, int mouseY) {
            if (RealmsMainScreen.this.shouldShowMessageInList()) {
                if (i == 0) {
                    this.renderTrialItem(i, x, y);
                    return;
                }
                --i;
            }
            if (i < RealmsMainScreen.this.realmsServers.size()) {
                this.renderMcoServerItem(i, x, y);
            }
        }

        private void renderTrialItem(int i, int x, int y) {
            int ry = y + 8;
            int index = 0;
            String msg = RealmsScreen.getLocalizedString("mco.trial.message");
            boolean hovered = false;
            if (x <= this.xm() && this.xm() <= this.getScrollbarPosition() && y <= this.ym() && this.ym() <= y + 32) {
                hovered = true;
            }
            int textColor = 0x7FFF7F;
            if (hovered && !RealmsMainScreen.this.shouldShowPopup()) {
                textColor = 6077788;
            }
            for (String s : msg.split("\\\\n")) {
                RealmsMainScreen.this.drawCenteredString(s, RealmsMainScreen.this.width() / 2, ry + index, textColor);
                index += 10;
            }
        }

        @Override
        public void renderSelected(int width, int y, int h, Tezzelator t) {
            int x0 = this.getScrollbarPosition() - 300;
            int x1 = this.getScrollbarPosition() - 5;
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glDisable(3553);
            t.begin(7, RealmsDefaultVertexFormat.POSITION_TEX_COLOR);
            t.vertex(x0, y + h + 2, 0.0).tex(0.0, 1.0).color(128, 128, 128, 255).endVertex();
            t.vertex(x1, y + h + 2, 0.0).tex(1.0, 1.0).color(128, 128, 128, 255).endVertex();
            t.vertex(x1, y - 2, 0.0).tex(1.0, 0.0).color(128, 128, 128, 255).endVertex();
            t.vertex(x0, y - 2, 0.0).tex(0.0, 0.0).color(128, 128, 128, 255).endVertex();
            t.vertex(x0 + 1, y + h + 1, 0.0).tex(0.0, 1.0).color(0, 0, 0, 255).endVertex();
            t.vertex(x1 - 1, y + h + 1, 0.0).tex(1.0, 1.0).color(0, 0, 0, 255).endVertex();
            t.vertex(x1 - 1, y - 1, 0.0).tex(1.0, 0.0).color(0, 0, 0, 255).endVertex();
            t.vertex(x0 + 1, y - 1, 0.0).tex(0.0, 0.0).color(0, 0, 0, 255).endVertex();
            t.end();
            GL11.glEnable(3553);
        }

        @Override
        public void itemClicked(int clickSlotPos, int slot, int xm, int ym, int width) {
            if (RealmsMainScreen.this.shouldShowMessageInList()) {
                if (slot == 0) {
                    return;
                }
                --slot;
            }
            if (slot >= RealmsMainScreen.this.realmsServers.size()) {
                return;
            }
            RealmsServer server = (RealmsServer)RealmsMainScreen.this.realmsServers.get(slot);
            if (server == null) {
                return;
            }
            if (RealmsMainScreen.this.toolTip != null && RealmsMainScreen.this.toolTip.equals(RealmsScreen.getLocalizedString("mco.selectServer.configure"))) {
                RealmsMainScreen.this.selectedServerId = server.id;
                RealmsMainScreen.this.configureClicked(server);
            } else if (RealmsMainScreen.this.toolTip != null && RealmsMainScreen.this.toolTip.equals(RealmsScreen.getLocalizedString("mco.selectServer.leave"))) {
                RealmsMainScreen.this.selectedServerId = server.id;
                RealmsMainScreen.this.leaveClicked(server);
            } else if (RealmsMainScreen.this.isSelfOwnedServer(server) && server.expired && RealmsMainScreen.this.expiredHover) {
                String extensionUrl = "https://account.mojang.com/buy/realms?sid=" + server.remoteSubscriptionId + "&pid=" + Realms.getUUID() + "&ref=" + (server.expiredTrial ? "expiredTrial" : "expiredRealm");
                this.browseURL(extensionUrl);
            } else if (RealmsMainScreen.this.isSelfOwnedServer(server) && RealmsMainScreen.this.updateBreaksAdventureNoteHover) {
                this.browseURL("https://beta.minecraft.net/realms/adventure-maps-in-1-9");
            }
        }

        @Override
        public void customMouseEvent(int y0, int y1, int headerHeight, float yo, int itemHeight) {
            if (Mouse.isButtonDown(0) && this.ym() >= y0 && this.ym() <= y1) {
                int x0 = this.width() / 2 - 160;
                int x1 = this.getScrollbarPosition();
                int clickSlotPos = this.ym() - y0 - headerHeight + (int)yo - 4;
                int slot = clickSlotPos / itemHeight;
                if (this.xm() >= x0 && this.xm() <= x1 && slot >= 0 && clickSlotPos >= 0 && slot < this.getItemCount()) {
                    this.itemClicked(clickSlotPos, slot, this.xm(), this.ym(), this.width());
                    RealmsMainScreen.this.clicks = RealmsMainScreen.this.clicks + (RealmsSharedConstants.TICKS_PER_SECOND / 3 + 1);
                    this.selectItem(slot, RealmsMainScreen.this.clicks >= RealmsSharedConstants.TICKS_PER_SECOND / 2, this.xm(), this.ym());
                }
            }
        }

        private void renderMcoServerItem(int i, int x, int y) {
            RealmsServer serverData = (RealmsServer)RealmsMainScreen.this.realmsServers.get(i);
            if (serverData.state == RealmsServer.State.UNINITIALIZED) {
                RealmsScreen.bind(RealmsMainScreen.WORLDICON_LOCATION);
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                GL11.glEnable(3008);
                GL11.glPushMatrix();
                RealmsScreen.blit(x + 10, y + 6, 0.0f, 0.0f, 40, 20, 40.0f, 20.0f);
                GL11.glPopMatrix();
                float scale = 0.5f + (1.0f + RealmsMth.sin((float)RealmsMainScreen.this.animTick * 0.25f)) * 0.25f;
                int textColor = 0xFF000000 | (int)(127.0f * scale) << 16 | (int)(255.0f * scale) << 8 | (int)(127.0f * scale);
                RealmsMainScreen.this.drawCenteredString(RealmsScreen.getLocalizedString("mco.selectServer.uninitialized"), x + 10 + 40 + 75, y + 12, textColor);
                return;
            }
            int dx = 225;
            int dy = 2;
            if (serverData.expired) {
                RealmsMainScreen.this.drawExpired(x + dx - 14, y + dy, this.xm(), this.ym());
            } else if (serverData.state == RealmsServer.State.CLOSED) {
                RealmsMainScreen.this.drawClose(x + dx - 14, y + dy, this.xm(), this.ym());
            } else if (RealmsMainScreen.this.isSelfOwnedServer(serverData) && serverData.daysLeft < 7) {
                RealmsMainScreen.this.drawExpiring(x + dx - 14, y + dy, this.xm(), this.ym(), serverData.daysLeft);
            } else if (serverData.state == RealmsServer.State.OPEN) {
                RealmsMainScreen.this.drawOpen(x + dx - 14, y + dy, this.xm(), this.ym());
            }
            if (!RealmsMainScreen.this.isSelfOwnedServer(serverData) && !overrideConfigure) {
                RealmsMainScreen.this.drawLeave(x + dx, y + dy, this.xm(), this.ym());
            } else {
                RealmsMainScreen.this.drawConfigure(x + dx, y + dy, this.xm(), this.ym());
            }
            if (!serverData.serverPing.nrOfPlayers.equals("0")) {
                String coloredNumPlayers = (Object)((Object)ChatFormatting.GRAY) + "" + serverData.serverPing.nrOfPlayers;
                RealmsMainScreen.this.drawString(coloredNumPlayers, x + 207 - RealmsMainScreen.this.fontWidth(coloredNumPlayers), y + 3, 0x808080);
                if (this.xm() >= x + 207 - RealmsMainScreen.this.fontWidth(coloredNumPlayers) && this.xm() <= x + 207 && this.ym() >= y + 1 && this.ym() <= y + 10 && this.ym() < RealmsMainScreen.this.height() - 40 && this.ym() > 32 && !RealmsMainScreen.this.shouldShowPopup()) {
                    RealmsMainScreen.this.toolTip = serverData.serverPing.playerList;
                }
            }
            if (RealmsMainScreen.this.isSelfOwnedServer(serverData) && serverData.expired) {
                boolean hovered = false;
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                GL11.glEnable(3042);
                RealmsScreen.bind(RealmsMainScreen.BUTTON_LOCATION);
                GL11.glPushMatrix();
                GL11.glBlendFunc(770, 771);
                String expirationText = RealmsScreen.getLocalizedString("mco.selectServer.expiredList");
                String expirationButtonText = RealmsScreen.getLocalizedString("mco.selectServer.expiredRenew");
                if (serverData.expiredTrial) {
                    expirationText = RealmsScreen.getLocalizedString("mco.selectServer.expiredTrial");
                    expirationButtonText = RealmsScreen.getLocalizedString("mco.selectServer.expiredSubscribe");
                }
                int buttonWidth = RealmsMainScreen.this.fontWidth(expirationButtonText) + 20;
                int buttonHeight = 16;
                int buttonX = x + RealmsMainScreen.this.fontWidth(expirationText) + 8;
                int buttonY = y + 13;
                if (this.xm() >= buttonX && this.xm() < buttonX + buttonWidth && this.ym() > buttonY && this.ym() <= buttonY + buttonHeight & this.ym() < RealmsMainScreen.this.height() - 40 && this.ym() > 32 && !RealmsMainScreen.this.shouldShowPopup()) {
                    hovered = true;
                    RealmsMainScreen.this.expiredHover = true;
                }
                int yImage = hovered ? 2 : 1;
                RealmsScreen.blit(buttonX, buttonY, 0.0f, 46 + yImage * 20, buttonWidth / 2, buttonHeight / 2, 256.0f, 256.0f);
                RealmsScreen.blit(buttonX + buttonWidth / 2, buttonY, 200 - buttonWidth / 2, 46 + yImage * 20, buttonWidth / 2, buttonHeight / 2, 256.0f, 256.0f);
                RealmsScreen.blit(buttonX, buttonY + buttonHeight / 2, 0.0f, 46 + yImage * 20 + 12, buttonWidth / 2, buttonHeight / 2, 256.0f, 256.0f);
                RealmsScreen.blit(buttonX + buttonWidth / 2, buttonY + buttonHeight / 2, 200 - buttonWidth / 2, 46 + yImage * 20 + 12, buttonWidth / 2, buttonHeight / 2, 256.0f, 256.0f);
                GL11.glPopMatrix();
                GL11.glDisable(3042);
                int textHeight = y + 11 + 5;
                int buttonTextColor = hovered ? 0xFFFFA0 : 0xFFFFFF;
                RealmsMainScreen.this.drawString(expirationText, x + 2, textHeight + 1, 15553363);
                RealmsMainScreen.this.drawCenteredString(expirationButtonText, buttonX + buttonWidth / 2, textHeight + 1, buttonTextColor);
            } else {
                if (serverData.worldType.equals((Object)RealmsServer.WorldType.MINIGAME)) {
                    int motdColor = 0xCCAC5C;
                    String miniGameStr = RealmsScreen.getLocalizedString("mco.selectServer.minigame") + " ";
                    int mgWidth = RealmsMainScreen.this.fontWidth(miniGameStr);
                    RealmsMainScreen.this.drawString(miniGameStr, x + 2, y + 12, motdColor);
                    RealmsMainScreen.this.drawString(serverData.getMinigameName(), x + 2 + mgWidth, y + 19872, 0x6C6C6C);
                } else {
                    RealmsMainScreen.this.drawString(serverData.getDescription(), x + 2, y + 12, 0x6C6C6C);
                }
                if (!RealmsMainScreen.this.isSelfOwnedServer(serverData)) {
                    RealmsMainScreen.this.drawString(serverData.owner, x + 2, y + 12 + 11, 0x4C4C4C);
                } else if (serverData.worldType.equals((Object)RealmsServer.WorldType.ADVENTUREMAP) && RealmsSharedConstants.VERSION_STRING.equals("1.8.9")) {
                    String noteText = RealmsScreen.getLocalizedString("mco.selectServer.updateBreaksAdventure", "1.9") + " ";
                    RealmsMainScreen.this.updateBreaksAdventureNoteHover = this.renderRealmNote(i, x, y, noteText);
                }
            }
            RealmsMainScreen.this.drawString(serverData.getName(), x + 2, y + 1, 0xFFFFFF);
            RealmsScreen.bindFace(serverData.ownerUUID, serverData.owner);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            RealmsScreen.blit(x - 36, y, 8.0f, 8.0f, 8, 8, 32, 32, 64.0f, 64.0f);
            RealmsScreen.blit(x - 36, y, 40.0f, 8.0f, 8, 8, 32, 32, 64.0f, 64.0f);
        }

        private boolean renderRealmNote(int i, int x, int y, String text) {
            String label = RealmsScreen.getLocalizedString("mco.selectServer.note") + " ";
            int labelWidth = RealmsMainScreen.this.fontWidth(label);
            int textWidth = RealmsMainScreen.this.fontWidth(text);
            int noteWidth = labelWidth + textWidth;
            int offsetX = x + 2;
            int offsetY = y + 12 + 11;
            boolean noteIsHovered = this.xm() >= offsetX && this.xm() < offsetX + noteWidth && this.ym() > offsetY && this.ym() <= offsetY + RealmsMainScreen.this.fontLineHeight();
            int labelColor = 15553363;
            int textColor = 0xFFFFFF;
            if (noteIsHovered) {
                labelColor = 12535109;
                textColor = 0xA0A0A0;
                label = "\u00a7n" + label;
                text = "\u00a7n" + text;
            }
            RealmsMainScreen.this.drawString(label, offsetX, offsetY, labelColor, true);
            RealmsMainScreen.this.drawString(text, offsetX + labelWidth, offsetY, textColor, true);
            return noteIsHovered;
        }

        private void browseURL(String url) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(url), null);
            RealmsUtil.browseTo(url);
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.realmsclient.util;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.LongRunningTask;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsResetWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsTermsScreen;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.annotation.Nullable;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsConnect;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsTasks {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int NUMBER_OF_RETRIES = 25;

    private static void pause(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            LOGGER.error("", (Throwable)e);
        }
    }

    public static class TrialCreationTask
    extends LongRunningTask {
        private final String name;
        private final String motd;
        private final RealmsMainScreen lastScreen;

        public TrialCreationTask(String name, String motd, RealmsMainScreen lastScreen) {
            this.name = name;
            this.motd = motd;
            this.lastScreen = lastScreen;
        }

        @Override
        public void run() {
            String title = RealmsScreen.getLocalizedString("mco.create.world.wait");
            this.setTitle(title);
            RealmsClient client = RealmsClient.createRealmsClient();
            try {
                RealmsServer server = client.createTrial(this.name, this.motd);
                if (server != null) {
                    this.lastScreen.closePopup();
                    RealmsResetWorldScreen resetWorldScreen = new RealmsResetWorldScreen(this.lastScreen, server, this.lastScreen.newScreen(), RealmsScreen.getLocalizedString("mco.selectServer.create"), RealmsScreen.getLocalizedString("mco.create.world.subtitle"), 0xA0A0A0, RealmsScreen.getLocalizedString("mco.create.world.skip"));
                    resetWorldScreen.setResetTitle(RealmsScreen.getLocalizedString("mco.create.world.reset.title"));
                    Realms.setScreen(resetWorldScreen);
                } else {
                    this.error(RealmsScreen.getLocalizedString("mco.trial.unavailable"));
                }
            } catch (RealmsServiceException e) {
                LOGGER.error("Couldn't create trial");
                this.error(e.toString());
            } catch (UnsupportedEncodingException e) {
                LOGGER.error("Couldn't create trial");
                this.error(e.getLocalizedMessage());
            } catch (IOException e) {
                LOGGER.error("Could not parse response creating trial");
                this.error(e.getLocalizedMessage());
            } catch (Exception e) {
                LOGGER.error("Could not create trial");
                this.error(e.getLocalizedMessage());
            }
        }
    }

    public static class WorldCreationTask
    extends LongRunningTask {
        private final String name;
        private final String motd;
        private final long worldId;
        private final RealmsScreen lastScreen;

        public WorldCreationTask(long worldId, String name, String motd, RealmsScreen lastScreen) {
            this.worldId = worldId;
            this.name = name;
            this.motd = motd;
            this.lastScreen = lastScreen;
        }

        @Override
        public void run() {
            String title = RealmsScreen.getLocalizedString("mco.create.world.wait");
            this.setTitle(title);
            RealmsClient client = RealmsClient.createRealmsClient();
            try {
                client.initializeWorld(this.worldId, this.name, this.motd);
                Realms.setScreen(this.lastScreen);
            } catch (RealmsServiceException e) {
                LOGGER.error("Couldn't create world");
                this.error(e.toString());
            } catch (UnsupportedEncodingException e) {
                LOGGER.error("Couldn't create world");
                this.error(e.getLocalizedMessage());
            } catch (IOException e) {
                LOGGER.error("Could not parse response creating world");
                this.error(e.getLocalizedMessage());
            } catch (Exception e) {
                LOGGER.error("Could not create world");
                this.error(e.getLocalizedMessage());
            }
        }
    }

    public static class RealmsConnectTask
    extends LongRunningTask {
        private final RealmsConnect realmsConnect;
        private final RealmsServer data;
        private final RealmsScreen onlineScreen;

        public RealmsConnectTask(RealmsScreen onlineScreen, RealmsServer server) {
            this.onlineScreen = onlineScreen;
            this.realmsConnect = new RealmsConnect(onlineScreen);
            this.data = server;
        }

        @Override
        public void run() {
            this.setTitle(RealmsScreen.getLocalizedString("mco.connect.connecting"));
            RealmsClient client = RealmsClient.createRealmsClient();
            boolean addressRetrieved = false;
            boolean hasError = false;
            int sleepTime = 5;
            RealmsServerAddress a = null;
            boolean tosNotAccepted = false;
            for (int i = 0; i < 20 && !this.aborted(); ++i) {
                try {
                    a = client.join(this.data.id);
                    addressRetrieved = true;
                } catch (RetryCallException e) {
                    sleepTime = e.delaySeconds;
                } catch (RealmsServiceException e) {
                    if (e.errorCode == 6002) {
                        tosNotAccepted = true;
                        break;
                    }
                    hasError = true;
                    this.error(e.toString());
                    LOGGER.error("Couldn't connect to world", (Throwable)e);
                    break;
                } catch (IOException e) {
                    LOGGER.error("Couldn't parse response connecting to world", (Throwable)e);
                } catch (Exception e) {
                    hasError = true;
                    LOGGER.error("Couldn't connect to world", (Throwable)e);
                    this.error(e.getLocalizedMessage());
                }
                if (addressRetrieved) break;
                this.sleep(sleepTime);
            }
            if (tosNotAccepted) {
                Realms.setScreen(new RealmsTermsScreen(this.onlineScreen, this.data));
            } else if (!this.aborted() && !hasError) {
                if (addressRetrieved) {
                    if (this.data.resourcePackUrl != null && this.data.resourcePackHash != null) {
                        try {
                            final RealmsServerAddress finalA = a;
                            Futures.addCallback(Realms.downloadResourcePack(this.data.resourcePackUrl, this.data.resourcePackHash), new FutureCallback<Object>(){

                                @Override
                                public void onSuccess(@Nullable Object result) {
                                    net.minecraft.realms.RealmsServerAddress address = net.minecraft.realms.RealmsServerAddress.parseString(finalA.address);
                                    RealmsConnectTask.this.realmsConnect.connect(address.getHost(), address.getPort());
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    LOGGER.error(t);
                                    RealmsConnectTask.this.error("Failed to download resource pack!");
                                }
                            });
                        } catch (Exception e) {
                            Realms.clearResourcePack();
                            LOGGER.error(e);
                            this.error("Failed to download resource pack!");
                        }
                    } else {
                        net.minecraft.realms.RealmsServerAddress address = net.minecraft.realms.RealmsServerAddress.parseString(a.address);
                        this.realmsConnect.connect(address.getHost(), address.getPort());
                    }
                } else {
                    this.error(RealmsScreen.getLocalizedString("mco.errorMessage.connectionFailure"));
                }
            }
        }

        private void sleep(int sleepTimeSeconds) {
            try {
                Thread.sleep(sleepTimeSeconds * 1000);
            } catch (InterruptedException e1) {
                LOGGER.warn(e1.getLocalizedMessage());
            }
        }

        @Override
        public void abortTask() {
            this.realmsConnect.abort();
        }

        @Override
        public void tick() {
            this.realmsConnect.tick();
        }
    }

    public static class ResettingWorldTask
    extends LongRunningTask {
        private final String seed;
        private final WorldTemplate worldTemplate;
        private final int levelType;
        private final boolean generateStructures;
        private final long serverId;
        private final RealmsScreen lastScreen;
        private String title = RealmsScreen.getLocalizedString("mco.reset.world.resetting.screen.title");

        public ResettingWorldTask(long serverId, RealmsScreen lastScreen, WorldTemplate worldTemplate) {
            this.seed = null;
            this.worldTemplate = worldTemplate;
            this.levelType = -1;
            this.generateStructures = true;
            this.serverId = serverId;
            this.lastScreen = lastScreen;
        }

        public ResettingWorldTask(long serverId, RealmsScreen lastScreen, String seed, int levelType, boolean generateStructures) {
            this.seed = seed;
            this.worldTemplate = null;
            this.levelType = levelType;
            this.generateStructures = generateStructures;
            this.serverId = serverId;
            this.lastScreen = lastScreen;
        }

        public void setResetTitle(String title) {
            this.title = title;
        }

        @Override
        public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();
            this.setTitle(this.title);
            for (int i = 0; i < 25; ++i) {
                try {
                    if (this.aborted()) {
                        return;
                    }
                    if (this.worldTemplate != null) {
                        client.resetWorldWithTemplate(this.serverId, this.worldTemplate.id);
                    } else {
                        client.resetWorldWithSeed(this.serverId, this.seed, this.levelType, this.generateStructures);
                    }
                    if (this.aborted()) {
                        return;
                    }
                    Realms.setScreen(this.lastScreen);
                    return;
                } catch (RetryCallException e) {
                    if (this.aborted()) {
                        return;
                    }
                    RealmsTasks.pause(e.delaySeconds);
                    continue;
                } catch (Exception e) {
                    if (this.aborted()) {
                        return;
                    }
                    LOGGER.error("Couldn't reset world");
                    this.error(e.toString());
                    return;
                }
            }
        }
    }

    public static class SwitchMinigameTask
    extends LongRunningTask {
        private final long worldId;
        private final WorldTemplate worldTemplate;
        private final RealmsConfigureWorldScreen lastScreen;

        public SwitchMinigameTask(long worldId, WorldTemplate worldTemplate, RealmsConfigureWorldScreen lastScreen) {
            this.worldId = worldId;
            this.worldTemplate = worldTemplate;
            this.lastScreen = lastScreen;
        }

        @Override
        public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();
            String title = RealmsScreen.getLocalizedString("mco.minigame.world.starting.screen.title");
            this.setTitle(title);
            for (int i = 0; i < 25; ++i) {
                try {
                    if (this.aborted()) {
                        return;
                    }
                    if (!client.putIntoMinigameMode(this.worldId, this.worldTemplate.id).booleanValue()) continue;
                    Realms.setScreen(this.lastScreen);
                    break;
                } catch (RetryCallException e) {
                    if (this.aborted()) {
                        return;
                    }
                    RealmsTasks.pause(e.delaySeconds);
                    continue;
                } catch (Exception e) {
                    if (this.aborted()) {
                        return;
                    }
                    LOGGER.error("Couldn't start mini game!");
                    this.error(e.toString());
                }
            }
        }
    }

    public static class SwitchSlotTask
    extends LongRunningTask {
        private final long worldId;
        private final int slot;
        private final RealmsScreen lastScreen;
        private final int confirmId;

        public SwitchSlotTask(long worldId, int slot, RealmsScreen lastScreen, int confirmId) {
            this.worldId = worldId;
            this.slot = slot;
            this.lastScreen = lastScreen;
            this.confirmId = confirmId;
        }

        @Override
        public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();
            String title = RealmsScreen.getLocalizedString("mco.minigame.world.slot.screen.title");
            this.setTitle(title);
            for (int i = 0; i < 25; ++i) {
                try {
                    if (this.aborted()) {
                        return;
                    }
                    if (!client.switchSlot(this.worldId, this.slot)) continue;
                    this.lastScreen.confirmResult(true, this.confirmId);
                    break;
                } catch (RetryCallException e) {
                    if (this.aborted()) {
                        return;
                    }
                    RealmsTasks.pause(e.delaySeconds);
                    continue;
                } catch (Exception e) {
                    if (this.aborted()) {
                        return;
                    }
                    LOGGER.error("Couldn't switch world!");
                    this.error(e.toString());
                }
            }
        }
    }

    public static class CloseServerTask
    extends LongRunningTask {
        private final RealmsServer serverData;
        private final RealmsConfigureWorldScreen configureScreen;

        public CloseServerTask(RealmsServer realmsServer, RealmsConfigureWorldScreen configureWorldScreen) {
            this.serverData = realmsServer;
            this.configureScreen = configureWorldScreen;
        }

        @Override
        public void run() {
            this.setTitle(RealmsScreen.getLocalizedString("mco.configure.world.closing"));
            RealmsClient client = RealmsClient.createRealmsClient();
            for (int i = 0; i < 25; ++i) {
                if (this.aborted()) {
                    return;
                }
                try {
                    boolean closeResult = client.close(this.serverData.id);
                    if (!closeResult) continue;
                    this.configureScreen.stateChanged();
                    this.serverData.state = RealmsServer.State.CLOSED;
                    Realms.setScreen(this.configureScreen);
                    break;
                } catch (RetryCallException e) {
                    if (this.aborted()) {
                        return;
                    }
                    RealmsTasks.pause(e.delaySeconds);
                    continue;
                } catch (Exception e) {
                    if (this.aborted()) {
                        return;
                    }
                    LOGGER.error("Failed to close server", (Throwable)e);
                    this.error("Failed to close the server");
                }
            }
        }
    }

    public static class OpenServerTask
    extends LongRunningTask {
        private final RealmsServer serverData;
        private final RealmsConfigureWorldScreen configureScreen;
        private final boolean join;
        private final RealmsScreen lastScreen;

        public OpenServerTask(RealmsServer realmsServer, RealmsConfigureWorldScreen configureWorldScreen, RealmsScreen lastScreen, boolean join) {
            this.serverData = realmsServer;
            this.configureScreen = configureWorldScreen;
            this.join = join;
            this.lastScreen = lastScreen;
        }

        @Override
        public void run() {
            this.setTitle(RealmsScreen.getLocalizedString("mco.configure.world.opening"));
            RealmsClient client = RealmsClient.createRealmsClient();
            for (int i = 0; i < 25; ++i) {
                if (this.aborted()) {
                    return;
                }
                try {
                    boolean openResult = client.open(this.serverData.id);
                    if (!openResult) continue;
                    this.configureScreen.stateChanged();
                    this.serverData.state = RealmsServer.State.OPEN;
                    if (this.join) {
                        ((RealmsMainScreen)this.lastScreen).play(this.serverData);
                        break;
                    }
                    Realms.setScreen(this.configureScreen);
                    break;
                } catch (RetryCallException e) {
                    if (this.aborted()) {
                        return;
                    }
                    RealmsTasks.pause(e.delaySeconds);
                    continue;
                } catch (Exception e) {
                    if (this.aborted()) {
                        return;
                    }
                    LOGGER.error("Failed to open server", (Throwable)e);
                    this.error("Failed to open the server");
                }
            }
        }
    }
}


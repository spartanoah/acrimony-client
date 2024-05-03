/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.ActivityManager;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.GameSDKException;
import de.jcm.discordgamesdk.ImageManager;
import de.jcm.discordgamesdk.LobbyManager;
import de.jcm.discordgamesdk.LogLevel;
import de.jcm.discordgamesdk.NetworkManager;
import de.jcm.discordgamesdk.OverlayManager;
import de.jcm.discordgamesdk.RelationshipManager;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.UserManager;
import de.jcm.discordgamesdk.VoiceManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Core
implements AutoCloseable {
    public static final Consumer<Result> DEFAULT_CALLBACK = result -> {
        if (result != Result.OK) {
            throw new GameSDKException((Result)((Object)result));
        }
    };
    public static final BiConsumer<LogLevel, String> DEFAULT_LOG_HOOK = (level, message) -> System.out.printf("[%s] %s\n", level, message);
    private final long pointer;
    private final CreateParams createParams;
    private final AtomicBoolean open = new AtomicBoolean(true);
    private final ReentrantLock lock = new ReentrantLock();
    private final ActivityManager activityManager;
    private final UserManager userManager;
    private final OverlayManager overlayManager;
    private final RelationshipManager relationshipManager;
    private final ImageManager imageManager;
    private final LobbyManager lobbyManager;
    private final NetworkManager networkManager;
    private final VoiceManager voiceManager;

    public static void init(File discordLibrary) {
        String path;
        InputStream in;
        String objectName;
        String name = "discord_game_sdk_jni";
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        if (osName.contains("windows")) {
            osName = "windows";
            objectName = name + ".dll";
            System.load(discordLibrary.getAbsolutePath());
        } else if (osName.contains("linux")) {
            osName = "linux";
            objectName = "lib" + name + ".so";
        } else if (osName.contains("mac os")) {
            osName = "macos";
            objectName = "lib" + name + ".dylib";
        } else {
            throw new RuntimeException("cannot determine OS type: " + osName);
        }
        if (arch.equals("x86_64")) {
            arch = "amd64";
        }
        if ((in = Core.class.getResourceAsStream(path = "/native/" + osName + "/" + arch + "/" + objectName)) == null) {
            throw new RuntimeException(new FileNotFoundException("cannot find native library at " + path));
        }
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "java-" + name + System.nanoTime());
        if (!tempDir.mkdir()) {
            throw new RuntimeException(new IOException("Cannot create temporary directory"));
        }
        tempDir.deleteOnExit();
        File temp = new File(tempDir, objectName);
        temp.deleteOnExit();
        try {
            Files.copy(in, temp.toPath(), new CopyOption[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.load(temp.getAbsolutePath());
        Core.initDiscordNative(discordLibrary.getAbsolutePath());
    }

    public static native void initDiscordNative(String var0);

    public Core(CreateParams params) {
        this.createParams = params;
        Object ret = this.create(params.getPointer());
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        this.pointer = (Long)ret;
        this.setLogHook(LogLevel.DEBUG, DEFAULT_LOG_HOOK);
        this.activityManager = new ActivityManager(this.getActivityManager(this.pointer), this);
        this.userManager = new UserManager(this.getUserManager(this.pointer), this);
        this.overlayManager = new OverlayManager(this.getOverlayManager(this.pointer), this);
        this.relationshipManager = new RelationshipManager(this.getRelationshipManager(this.pointer), this);
        this.imageManager = new ImageManager(this.getImageManager(this.pointer), this);
        this.lobbyManager = new LobbyManager(this.getLobbyManager(this.pointer), this);
        this.networkManager = new NetworkManager(this.getNetworkManager(this.pointer), this);
        this.voiceManager = new VoiceManager(this.getVoiceManager(this.pointer), this);
    }

    private native Object create(long var1);

    private native void destroy(long var1);

    private native long getActivityManager(long var1);

    private native long getUserManager(long var1);

    private native long getOverlayManager(long var1);

    private native long getRelationshipManager(long var1);

    private native long getImageManager(long var1);

    private native long getLobbyManager(long var1);

    private native long getNetworkManager(long var1);

    private native long getVoiceManager(long var1);

    private native void runCallbacks(long var1);

    private native void setLogHook(long var1, int var3, BiConsumer<LogLevel, String> var4);

    public ActivityManager activityManager() {
        return this.activityManager;
    }

    public UserManager userManager() {
        return this.userManager;
    }

    public OverlayManager overlayManager() {
        return this.overlayManager;
    }

    public RelationshipManager relationshipManager() {
        return this.relationshipManager;
    }

    public ImageManager imageManager() {
        return this.imageManager;
    }

    public LobbyManager lobbyManager() {
        return this.lobbyManager;
    }

    public NetworkManager networkManager() {
        return this.networkManager;
    }

    public VoiceManager voiceManager() {
        return this.voiceManager;
    }

    public void runCallbacks() {
        this.execute(() -> this.runCallbacks(this.pointer));
    }

    public void setLogHook(LogLevel minLevel, BiConsumer<LogLevel, String> logHook) {
        this.execute(() -> this.setLogHook(this.pointer, minLevel.ordinal(), Objects.requireNonNull(logHook)));
    }

    @Override
    public void close() {
        if (this.open.compareAndSet(true, false)) {
            this.lock.lock();
            try {
                this.destroy(this.pointer);
            } finally {
                this.lock.unlock();
            }
            this.createParams.close();
        }
    }

    public long getPointer() {
        return this.pointer;
    }

    void execute(Runnable runnable) {
        this.execute(() -> {
            runnable.run();
            return null;
        });
    }

    <T> T execute(Supplier<T> provider) {
        if (!this.open.get()) {
            throw new IllegalStateException("Core is closed");
        }
        this.lock.lock();
        try {
            T t = provider.get();
            return t;
        } finally {
            this.lock.unlock();
        }
    }
}


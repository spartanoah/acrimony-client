/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package paulscode.sound;

import java.net.URL;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.sound.sampled.AudioFormat;
import paulscode.sound.Channel;
import paulscode.sound.FilenameURL;
import paulscode.sound.ICodec;
import paulscode.sound.Library;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemLogger;
import paulscode.sound.Vector3D;

public class Source {
    protected Class libraryType = Library.class;
    private static final boolean GET = false;
    private static final boolean SET = true;
    private static final boolean XXX = false;
    private SoundSystemLogger logger;
    public boolean rawDataStream = false;
    public AudioFormat rawDataFormat = null;
    public boolean temporary = false;
    public boolean priority = false;
    public boolean toStream = false;
    public boolean toLoop = false;
    public boolean toPlay = false;
    public String sourcename = "";
    public FilenameURL filenameURL = null;
    public Vector3D position;
    public int attModel = 0;
    public float distOrRoll = 0.0f;
    public Vector3D velocity;
    public float gain = 1.0f;
    public float sourceVolume = 1.0f;
    protected float pitch = 1.0f;
    public float distanceFromListener = 0.0f;
    public Channel channel = null;
    public SoundBuffer soundBuffer = null;
    private boolean active = true;
    private boolean stopped = true;
    private boolean paused = false;
    protected ICodec codec = null;
    protected ICodec nextCodec = null;
    protected LinkedList<SoundBuffer> nextBuffers = null;
    protected LinkedList<FilenameURL> soundSequenceQueue = null;
    protected final Object soundSequenceLock = new Object();
    public boolean preLoad = false;
    protected float fadeOutGain = -1.0f;
    protected float fadeInGain = 1.0f;
    protected long fadeOutMilis = 0L;
    protected long fadeInMilis = 0L;
    protected long lastFadeCheck = 0L;

    public Source(boolean priority, boolean toStream, boolean toLoop, String sourcename, FilenameURL filenameURL, SoundBuffer soundBuffer, float x, float y, float z, int attModel, float distOrRoll, boolean temporary) {
        this.logger = SoundSystemConfig.getLogger();
        this.priority = priority;
        this.toStream = toStream;
        this.toLoop = toLoop;
        this.sourcename = sourcename;
        this.filenameURL = filenameURL;
        this.soundBuffer = soundBuffer;
        this.position = new Vector3D(x, y, z);
        this.attModel = attModel;
        this.distOrRoll = distOrRoll;
        this.velocity = new Vector3D(0.0f, 0.0f, 0.0f);
        this.temporary = temporary;
        if (toStream && filenameURL != null) {
            this.codec = SoundSystemConfig.getCodec(filenameURL.getFilename());
        }
    }

    public Source(Source old, SoundBuffer soundBuffer) {
        this.logger = SoundSystemConfig.getLogger();
        this.priority = old.priority;
        this.toStream = old.toStream;
        this.toLoop = old.toLoop;
        this.sourcename = old.sourcename;
        this.filenameURL = old.filenameURL;
        this.position = old.position.clone();
        this.attModel = old.attModel;
        this.distOrRoll = old.distOrRoll;
        this.velocity = old.velocity.clone();
        this.temporary = old.temporary;
        this.sourceVolume = old.sourceVolume;
        this.rawDataStream = old.rawDataStream;
        this.rawDataFormat = old.rawDataFormat;
        this.soundBuffer = soundBuffer;
        if (this.toStream && this.filenameURL != null) {
            this.codec = SoundSystemConfig.getCodec(this.filenameURL.getFilename());
        }
    }

    public Source(AudioFormat audioFormat, boolean priority, String sourcename, float x, float y, float z, int attModel, float distOrRoll) {
        this.logger = SoundSystemConfig.getLogger();
        this.priority = priority;
        this.toStream = true;
        this.toLoop = false;
        this.sourcename = sourcename;
        this.filenameURL = null;
        this.soundBuffer = null;
        this.position = new Vector3D(x, y, z);
        this.attModel = attModel;
        this.distOrRoll = distOrRoll;
        this.velocity = new Vector3D(0.0f, 0.0f, 0.0f);
        this.temporary = false;
        this.rawDataStream = true;
        this.rawDataFormat = audioFormat;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void cleanup() {
        if (this.codec != null) {
            this.codec.cleanup();
        }
        Object object = this.soundSequenceLock;
        synchronized (object) {
            if (this.soundSequenceQueue != null) {
                this.soundSequenceQueue.clear();
            }
            this.soundSequenceQueue = null;
        }
        this.sourcename = null;
        this.filenameURL = null;
        this.position = null;
        this.soundBuffer = null;
        this.codec = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void queueSound(FilenameURL filenameURL) {
        if (!this.toStream) {
            this.errorMessage("Method 'queueSound' may only be used for streaming and MIDI sources.");
            return;
        }
        if (filenameURL == null) {
            this.errorMessage("File not specified in method 'queueSound'");
            return;
        }
        Object object = this.soundSequenceLock;
        synchronized (object) {
            if (this.soundSequenceQueue == null) {
                this.soundSequenceQueue = new LinkedList();
            }
            this.soundSequenceQueue.add(filenameURL);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void dequeueSound(String filename) {
        if (!this.toStream) {
            this.errorMessage("Method 'dequeueSound' may only be used for streaming and MIDI sources.");
            return;
        }
        if (filename == null || filename.equals("")) {
            this.errorMessage("Filename not specified in method 'dequeueSound'");
            return;
        }
        Object object = this.soundSequenceLock;
        synchronized (object) {
            if (this.soundSequenceQueue != null) {
                ListIterator i = this.soundSequenceQueue.listIterator();
                while (i.hasNext()) {
                    if (!((FilenameURL)i.next()).getFilename().equals(filename)) continue;
                    i.remove();
                    break;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void fadeOut(FilenameURL filenameURL, long milis) {
        if (!this.toStream) {
            this.errorMessage("Method 'fadeOut' may only be used for streaming and MIDI sources.");
            return;
        }
        if (milis < 0L) {
            this.errorMessage("Miliseconds may not be negative in method 'fadeOut'.");
            return;
        }
        this.fadeOutMilis = milis;
        this.fadeInMilis = 0L;
        this.fadeOutGain = 1.0f;
        this.lastFadeCheck = System.currentTimeMillis();
        Object object = this.soundSequenceLock;
        synchronized (object) {
            if (this.soundSequenceQueue != null) {
                this.soundSequenceQueue.clear();
            }
            if (filenameURL != null) {
                if (this.soundSequenceQueue == null) {
                    this.soundSequenceQueue = new LinkedList();
                }
                this.soundSequenceQueue.add(filenameURL);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void fadeOutIn(FilenameURL filenameURL, long milisOut, long milisIn) {
        if (!this.toStream) {
            this.errorMessage("Method 'fadeOutIn' may only be used for streaming and MIDI sources.");
            return;
        }
        if (filenameURL == null) {
            this.errorMessage("Filename/URL not specified in method 'fadeOutIn'.");
            return;
        }
        if (milisOut < 0L || milisIn < 0L) {
            this.errorMessage("Miliseconds may not be negative in method 'fadeOutIn'.");
            return;
        }
        this.fadeOutMilis = milisOut;
        this.fadeInMilis = milisIn;
        this.fadeOutGain = 1.0f;
        this.lastFadeCheck = System.currentTimeMillis();
        Object object = this.soundSequenceLock;
        synchronized (object) {
            if (this.soundSequenceQueue == null) {
                this.soundSequenceQueue = new LinkedList();
            }
            this.soundSequenceQueue.clear();
            this.soundSequenceQueue.add(filenameURL);
        }
    }

    public boolean checkFadeOut() {
        if (!this.toStream) {
            return false;
        }
        if (this.fadeOutGain == -1.0f && this.fadeInGain == 1.0f) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        long milisPast = currentTime - this.lastFadeCheck;
        this.lastFadeCheck = currentTime;
        if (this.fadeOutGain >= 0.0f) {
            if (this.fadeOutMilis == 0L) {
                this.fadeOutGain = -1.0f;
                this.fadeInGain = 0.0f;
                if (!this.incrementSoundSequence()) {
                    this.stop();
                }
                this.positionChanged();
                this.preLoad = true;
                return false;
            }
            float fadeOutReduction = (float)milisPast / (float)this.fadeOutMilis;
            this.fadeOutGain -= fadeOutReduction;
            if (this.fadeOutGain <= 0.0f) {
                this.fadeOutGain = -1.0f;
                this.fadeInGain = 0.0f;
                if (!this.incrementSoundSequence()) {
                    this.stop();
                }
                this.positionChanged();
                this.preLoad = true;
                return false;
            }
            this.positionChanged();
            return true;
        }
        if (this.fadeInGain < 1.0f) {
            this.fadeOutGain = -1.0f;
            if (this.fadeInMilis == 0L) {
                this.fadeOutGain = -1.0f;
                this.fadeInGain = 1.0f;
            } else {
                float fadeInIncrease = (float)milisPast / (float)this.fadeInMilis;
                this.fadeInGain += fadeInIncrease;
                if (this.fadeInGain >= 1.0f) {
                    this.fadeOutGain = -1.0f;
                    this.fadeInGain = 1.0f;
                }
            }
            this.positionChanged();
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean incrementSoundSequence() {
        if (!this.toStream) {
            this.errorMessage("Method 'incrementSoundSequence' may only be used for streaming and MIDI sources.");
            return false;
        }
        Object object = this.soundSequenceLock;
        synchronized (object) {
            if (this.soundSequenceQueue != null && this.soundSequenceQueue.size() > 0) {
                this.filenameURL = this.soundSequenceQueue.remove(0);
                if (this.codec != null) {
                    this.codec.cleanup();
                }
                this.codec = SoundSystemConfig.getCodec(this.filenameURL.getFilename());
                return true;
            }
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean readBuffersFromNextSoundInSequence() {
        if (!this.toStream) {
            this.errorMessage("Method 'readBuffersFromNextSoundInSequence' may only be used for streaming sources.");
            return false;
        }
        Object object = this.soundSequenceLock;
        synchronized (object) {
            if (this.soundSequenceQueue != null && this.soundSequenceQueue.size() > 0) {
                if (this.nextCodec != null) {
                    this.nextCodec.cleanup();
                }
                this.nextCodec = SoundSystemConfig.getCodec(this.soundSequenceQueue.get(0).getFilename());
                this.nextCodec.initialize(this.soundSequenceQueue.get(0).getURL());
                SoundBuffer buffer = null;
                for (int i = 0; i < SoundSystemConfig.getNumberStreamingBuffers() && !this.nextCodec.endOfStream(); ++i) {
                    buffer = this.nextCodec.read();
                    if (buffer == null) continue;
                    if (this.nextBuffers == null) {
                        this.nextBuffers = new LinkedList();
                    }
                    this.nextBuffers.add(buffer);
                }
                return true;
            }
        }
        return false;
    }

    public int getSoundSequenceQueueSize() {
        if (this.soundSequenceQueue == null) {
            return 0;
        }
        return this.soundSequenceQueue.size();
    }

    public void setTemporary(boolean tmp) {
        this.temporary = tmp;
    }

    public void listenerMoved() {
    }

    public void setPosition(float x, float y, float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }

    public void positionChanged() {
    }

    public void setPriority(boolean pri) {
        this.priority = pri;
    }

    public void setLooping(boolean lp) {
        this.toLoop = lp;
    }

    public void setAttenuation(int model) {
        this.attModel = model;
    }

    public void setDistOrRoll(float dr) {
        this.distOrRoll = dr;
    }

    public void setVelocity(float x, float y, float z) {
        this.velocity.x = x;
        this.velocity.y = y;
        this.velocity.z = z;
    }

    public float getDistanceFromListener() {
        return this.distanceFromListener;
    }

    public void setPitch(float value) {
        float newPitch = value;
        if (newPitch < 0.5f) {
            newPitch = 0.5f;
        } else if (newPitch > 2.0f) {
            newPitch = 2.0f;
        }
        this.pitch = newPitch;
    }

    public float getPitch() {
        return this.pitch;
    }

    public boolean reverseByteOrder() {
        return SoundSystemConfig.reverseByteOrder(this.libraryType);
    }

    public void changeSource(boolean priority, boolean toStream, boolean toLoop, String sourcename, FilenameURL filenameURL, SoundBuffer soundBuffer, float x, float y, float z, int attModel, float distOrRoll, boolean temporary) {
        this.priority = priority;
        this.toStream = toStream;
        this.toLoop = toLoop;
        this.sourcename = sourcename;
        this.filenameURL = filenameURL;
        this.soundBuffer = soundBuffer;
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
        this.attModel = attModel;
        this.distOrRoll = distOrRoll;
        this.temporary = temporary;
    }

    public int feedRawAudioData(Channel c, byte[] buffer) {
        if (!this.active(false, false)) {
            this.toPlay = true;
            return -1;
        }
        if (this.channel != c) {
            this.channel = c;
            this.channel.close();
            this.channel.setAudioFormat(this.rawDataFormat);
            this.positionChanged();
        }
        this.stopped(true, false);
        this.paused(true, false);
        return this.channel.feedRawAudioData(buffer);
    }

    public void play(Channel c) {
        if (!this.active(false, false)) {
            if (this.toLoop) {
                this.toPlay = true;
            }
            return;
        }
        if (this.channel != c) {
            this.channel = c;
            this.channel.close();
        }
        this.stopped(true, false);
        this.paused(true, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean stream() {
        if (this.channel == null) {
            return false;
        }
        if (this.preLoad) {
            if (this.rawDataStream) {
                this.preLoad = false;
            } else {
                return this.preLoad();
            }
        }
        if (this.rawDataStream) {
            if (this.stopped() || this.paused()) {
                return true;
            }
            if (this.channel.buffersProcessed() > 0) {
                this.channel.processBuffer();
            }
            return true;
        }
        if (this.codec == null) {
            return false;
        }
        if (this.stopped()) {
            return false;
        }
        if (this.paused()) {
            return true;
        }
        int processed = this.channel.buffersProcessed();
        SoundBuffer buffer = null;
        for (int i = 0; i < processed; ++i) {
            buffer = this.codec.read();
            if (buffer != null) {
                if (buffer.audioData != null) {
                    this.channel.queueBuffer(buffer.audioData);
                }
                buffer.cleanup();
                buffer = null;
                return true;
            }
            if (!this.codec.endOfStream()) continue;
            Object object = this.soundSequenceLock;
            synchronized (object) {
                if (SoundSystemConfig.getStreamQueueFormatsMatch()) {
                    if (this.soundSequenceQueue != null && this.soundSequenceQueue.size() > 0) {
                        if (this.codec != null) {
                            this.codec.cleanup();
                        }
                        this.filenameURL = this.soundSequenceQueue.remove(0);
                        this.codec = SoundSystemConfig.getCodec(this.filenameURL.getFilename());
                        this.codec.initialize(this.filenameURL.getURL());
                        buffer = this.codec.read();
                        if (buffer != null) {
                            if (buffer.audioData != null) {
                                this.channel.queueBuffer(buffer.audioData);
                            }
                            buffer.cleanup();
                            buffer = null;
                            return true;
                        }
                    } else if (this.toLoop) {
                        this.codec.initialize(this.filenameURL.getURL());
                        buffer = this.codec.read();
                        if (buffer != null) {
                            if (buffer.audioData != null) {
                                this.channel.queueBuffer(buffer.audioData);
                            }
                            buffer.cleanup();
                            buffer = null;
                            return true;
                        }
                    }
                }
                continue;
            }
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean preLoad() {
        if (this.channel == null) {
            return false;
        }
        if (this.codec == null) {
            return false;
        }
        SoundBuffer buffer = null;
        boolean noNextBuffers = false;
        Object object = this.soundSequenceLock;
        synchronized (object) {
            if (this.nextBuffers == null || this.nextBuffers.isEmpty()) {
                noNextBuffers = true;
            }
        }
        if (this.nextCodec != null && !noNextBuffers) {
            this.codec = this.nextCodec;
            this.nextCodec = null;
            object = this.soundSequenceLock;
            synchronized (object) {
                while (!this.nextBuffers.isEmpty()) {
                    buffer = this.nextBuffers.remove(0);
                    if (buffer == null) continue;
                    if (buffer.audioData != null) {
                        this.channel.queueBuffer(buffer.audioData);
                    }
                    buffer.cleanup();
                    buffer = null;
                }
            }
        } else {
            this.nextCodec = null;
            URL url = this.filenameURL.getURL();
            this.codec.initialize(url);
            for (int i = 0; i < SoundSystemConfig.getNumberStreamingBuffers(); ++i) {
                buffer = this.codec.read();
                if (buffer == null) continue;
                if (buffer.audioData != null) {
                    this.channel.queueBuffer(buffer.audioData);
                }
                buffer.cleanup();
                buffer = null;
            }
        }
        return true;
    }

    public void pause() {
        this.toPlay = false;
        this.paused(true, true);
        if (this.channel != null) {
            this.channel.pause();
        } else {
            this.errorMessage("Channel null in method 'pause'");
        }
    }

    public void stop() {
        this.toPlay = false;
        this.stopped(true, true);
        this.paused(true, false);
        if (this.channel != null) {
            this.channel.stop();
        } else {
            this.errorMessage("Channel null in method 'stop'");
        }
    }

    public void rewind() {
        if (this.paused(false, false)) {
            this.stop();
        }
        if (this.channel != null) {
            boolean rePlay = this.playing();
            this.channel.rewind();
            if (this.toStream && rePlay) {
                this.stop();
                this.play(this.channel);
            }
        } else {
            this.errorMessage("Channel null in method 'rewind'");
        }
    }

    public void flush() {
        if (this.channel != null) {
            this.channel.flush();
        } else {
            this.errorMessage("Channel null in method 'flush'");
        }
    }

    public void cull() {
        if (!this.active(false, false)) {
            return;
        }
        if (this.playing() && this.toLoop) {
            this.toPlay = true;
        }
        if (this.rawDataStream) {
            this.toPlay = true;
        }
        this.active(true, false);
        if (this.channel != null) {
            this.channel.close();
        }
        this.channel = null;
    }

    public void activate() {
        this.active(true, true);
    }

    public boolean active() {
        return this.active(false, false);
    }

    public boolean playing() {
        if (this.channel == null || this.channel.attachedSource != this) {
            return false;
        }
        if (this.paused() || this.stopped()) {
            return false;
        }
        return this.channel.playing();
    }

    public boolean stopped() {
        return this.stopped(false, false);
    }

    public boolean paused() {
        return this.paused(false, false);
    }

    public float millisecondsPlayed() {
        if (this.channel == null) {
            return -1.0f;
        }
        return this.channel.millisecondsPlayed();
    }

    private synchronized boolean active(boolean action, boolean value) {
        if (action) {
            this.active = value;
        }
        return this.active;
    }

    private synchronized boolean stopped(boolean action, boolean value) {
        if (action) {
            this.stopped = value;
        }
        return this.stopped;
    }

    private synchronized boolean paused(boolean action, boolean value) {
        if (action) {
            this.paused = value;
        }
        return this.paused;
    }

    public String getClassName() {
        String libTitle = SoundSystemConfig.getLibraryTitle(this.libraryType);
        if (libTitle.equals("No Sound")) {
            return "Source";
        }
        return "Source" + libTitle;
    }

    protected void message(String message) {
        this.logger.message(message, 0);
    }

    protected void importantMessage(String message) {
        this.logger.importantMessage(message, 0);
    }

    protected boolean errorCheck(boolean error, String message) {
        return this.logger.errorCheck(error, this.getClassName(), message, 0);
    }

    protected void errorMessage(String message) {
        this.logger.errorMessage(this.getClassName(), message, 0);
    }

    protected void printStackTrace(Exception e) {
        this.logger.printStackTrace(e, 1);
    }
}


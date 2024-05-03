/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package paulscode.sound;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import paulscode.sound.FilenameURL;
import paulscode.sound.SimpleThread;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.SoundSystemLogger;

public class MidiChannel
implements MetaEventListener {
    private SoundSystemLogger logger;
    private FilenameURL filenameURL;
    private String sourcename;
    private static final int CHANGE_VOLUME = 7;
    private static final int END_OF_TRACK = 47;
    private static final boolean GET = false;
    private static final boolean SET = true;
    private static final boolean XXX = false;
    private Sequencer sequencer = null;
    private Synthesizer synthesizer = null;
    private MidiDevice synthDevice = null;
    private Sequence sequence = null;
    private boolean toLoop = true;
    private float gain = 1.0f;
    private boolean loading = true;
    private LinkedList<FilenameURL> sequenceQueue = null;
    private final Object sequenceQueueLock = new Object();
    protected float fadeOutGain = -1.0f;
    protected float fadeInGain = 1.0f;
    protected long fadeOutMilis = 0L;
    protected long fadeInMilis = 0L;
    protected long lastFadeCheck = 0L;
    private FadeThread fadeThread = null;

    public MidiChannel(boolean toLoop, String sourcename, String filename) {
        this.loading(true, true);
        this.logger = SoundSystemConfig.getLogger();
        this.filenameURL(true, new FilenameURL(filename));
        this.sourcename(true, sourcename);
        this.setLooping(toLoop);
        this.init();
        this.loading(true, false);
    }

    public MidiChannel(boolean toLoop, String sourcename, URL midiFile, String identifier) {
        this.loading(true, true);
        this.logger = SoundSystemConfig.getLogger();
        this.filenameURL(true, new FilenameURL(midiFile, identifier));
        this.sourcename(true, sourcename);
        this.setLooping(toLoop);
        this.init();
        this.loading(true, false);
    }

    public MidiChannel(boolean toLoop, String sourcename, FilenameURL midiFilenameURL) {
        this.loading(true, true);
        this.logger = SoundSystemConfig.getLogger();
        this.filenameURL(true, midiFilenameURL);
        this.sourcename(true, sourcename);
        this.setLooping(toLoop);
        this.init();
        this.loading(true, false);
    }

    private void init() {
        this.getSequencer();
        this.setSequence(this.filenameURL(false, null).getURL());
        this.getSynthesizer();
        this.resetGain();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void cleanup() {
        this.loading(true, true);
        this.setLooping(true);
        if (this.sequencer != null) {
            try {
                this.sequencer.stop();
                this.sequencer.close();
                this.sequencer.removeMetaEventListener(this);
            } catch (Exception e) {
                // empty catch block
            }
        }
        this.logger = null;
        this.sequencer = null;
        this.synthesizer = null;
        this.sequence = null;
        Object e = this.sequenceQueueLock;
        synchronized (e) {
            if (this.sequenceQueue != null) {
                this.sequenceQueue.clear();
            }
            this.sequenceQueue = null;
        }
        if (this.fadeThread != null) {
            boolean killException = false;
            try {
                this.fadeThread.kill();
                this.fadeThread.interrupt();
            } catch (Exception e2) {
                killException = true;
            }
            if (!killException) {
                for (int i = 0; i < 50 && this.fadeThread.alive(); ++i) {
                    try {
                        Thread.sleep(100L);
                        continue;
                    } catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                }
            }
            if (killException || this.fadeThread.alive()) {
                this.errorMessage("MIDI fade effects thread did not die!");
                this.message("Ignoring errors... continuing clean-up.");
            }
        }
        this.fadeThread = null;
        this.loading(true, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void queueSound(FilenameURL filenameURL) {
        if (filenameURL == null) {
            this.errorMessage("Filename/URL not specified in method 'queueSound'");
            return;
        }
        Object object = this.sequenceQueueLock;
        synchronized (object) {
            if (this.sequenceQueue == null) {
                this.sequenceQueue = new LinkedList();
            }
            this.sequenceQueue.add(filenameURL);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void dequeueSound(String filename) {
        if (filename == null || filename.equals("")) {
            this.errorMessage("Filename not specified in method 'dequeueSound'");
            return;
        }
        Object object = this.sequenceQueueLock;
        synchronized (object) {
            if (this.sequenceQueue != null) {
                ListIterator i = this.sequenceQueue.listIterator();
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
        if (milis < 0L) {
            this.errorMessage("Miliseconds may not be negative in method 'fadeOut'.");
            return;
        }
        this.fadeOutMilis = milis;
        this.fadeInMilis = 0L;
        this.fadeOutGain = 1.0f;
        this.lastFadeCheck = System.currentTimeMillis();
        Object object = this.sequenceQueueLock;
        synchronized (object) {
            if (this.sequenceQueue != null) {
                this.sequenceQueue.clear();
            }
            if (filenameURL != null) {
                if (this.sequenceQueue == null) {
                    this.sequenceQueue = new LinkedList();
                }
                this.sequenceQueue.add(filenameURL);
            }
        }
        if (this.fadeThread == null) {
            this.fadeThread = new FadeThread();
            this.fadeThread.start();
        }
        this.fadeThread.interrupt();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void fadeOutIn(FilenameURL filenameURL, long milisOut, long milisIn) {
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
        Object object = this.sequenceQueueLock;
        synchronized (object) {
            if (this.sequenceQueue == null) {
                this.sequenceQueue = new LinkedList();
            }
            this.sequenceQueue.clear();
            this.sequenceQueue.add(filenameURL);
        }
        if (this.fadeThread == null) {
            this.fadeThread = new FadeThread();
            this.fadeThread.start();
        }
        this.fadeThread.interrupt();
    }

    private synchronized boolean checkFadeOut() {
        if (this.fadeOutGain == -1.0f && this.fadeInGain == 1.0f) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        long milisPast = currentTime - this.lastFadeCheck;
        this.lastFadeCheck = currentTime;
        if (this.fadeOutGain >= 0.0f) {
            if (this.fadeOutMilis == 0L) {
                this.fadeOutGain = 0.0f;
                this.fadeInGain = 0.0f;
                if (!this.incrementSequence()) {
                    this.stop();
                }
                this.rewind();
                this.resetGain();
                return false;
            }
            float fadeOutReduction = (float)milisPast / (float)this.fadeOutMilis;
            this.fadeOutGain -= fadeOutReduction;
            if (this.fadeOutGain <= 0.0f) {
                this.fadeOutGain = -1.0f;
                this.fadeInGain = 0.0f;
                if (!this.incrementSequence()) {
                    this.stop();
                }
                this.rewind();
                this.resetGain();
                return false;
            }
            this.resetGain();
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
            this.resetGain();
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean incrementSequence() {
        Object object = this.sequenceQueueLock;
        synchronized (object) {
            if (this.sequenceQueue != null && this.sequenceQueue.size() > 0) {
                this.filenameURL(true, this.sequenceQueue.remove(0));
                this.loading(true, true);
                if (this.sequencer == null) {
                    this.getSequencer();
                } else {
                    this.sequencer.stop();
                    this.sequencer.setMicrosecondPosition(0L);
                    this.sequencer.removeMetaEventListener(this);
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                }
                if (this.sequencer == null) {
                    this.errorMessage("Unable to set the sequence in method 'incrementSequence', because there wasn't a sequencer to use.");
                    this.loading(true, false);
                    return false;
                }
                this.setSequence(this.filenameURL(false, null).getURL());
                this.sequencer.start();
                this.resetGain();
                this.sequencer.addMetaEventListener(this);
                this.loading(true, false);
                return true;
            }
        }
        return false;
    }

    public void play() {
        if (!this.loading()) {
            if (this.sequencer == null) {
                return;
            }
            try {
                this.sequencer.start();
                this.sequencer.addMetaEventListener(this);
            } catch (Exception e) {
                this.errorMessage("Exception in method 'play'");
                this.printStackTrace(e);
                SoundSystemException sse = new SoundSystemException(e.getMessage());
                SoundSystem.setException(sse);
            }
        }
    }

    public void stop() {
        if (!this.loading()) {
            if (this.sequencer == null) {
                return;
            }
            try {
                this.sequencer.stop();
                this.sequencer.setMicrosecondPosition(0L);
                this.sequencer.removeMetaEventListener(this);
            } catch (Exception e) {
                this.errorMessage("Exception in method 'stop'");
                this.printStackTrace(e);
                SoundSystemException sse = new SoundSystemException(e.getMessage());
                SoundSystem.setException(sse);
            }
        }
    }

    public void pause() {
        if (!this.loading()) {
            if (this.sequencer == null) {
                return;
            }
            try {
                this.sequencer.stop();
            } catch (Exception e) {
                this.errorMessage("Exception in method 'pause'");
                this.printStackTrace(e);
                SoundSystemException sse = new SoundSystemException(e.getMessage());
                SoundSystem.setException(sse);
            }
        }
    }

    public void rewind() {
        if (!this.loading()) {
            if (this.sequencer == null) {
                return;
            }
            try {
                this.sequencer.setMicrosecondPosition(0L);
            } catch (Exception e) {
                this.errorMessage("Exception in method 'rewind'");
                this.printStackTrace(e);
                SoundSystemException sse = new SoundSystemException(e.getMessage());
                SoundSystem.setException(sse);
            }
        }
    }

    public void setVolume(float value) {
        this.gain = value;
        this.resetGain();
    }

    public float getVolume() {
        return this.gain;
    }

    public void switchSource(boolean toLoop, String sourcename, String filename) {
        this.loading(true, true);
        this.filenameURL(true, new FilenameURL(filename));
        this.sourcename(true, sourcename);
        this.setLooping(toLoop);
        this.reset();
        this.loading(true, false);
    }

    public void switchSource(boolean toLoop, String sourcename, URL midiFile, String identifier) {
        this.loading(true, true);
        this.filenameURL(true, new FilenameURL(midiFile, identifier));
        this.sourcename(true, sourcename);
        this.setLooping(toLoop);
        this.reset();
        this.loading(true, false);
    }

    public void switchSource(boolean toLoop, String sourcename, FilenameURL filenameURL) {
        this.loading(true, true);
        this.filenameURL(true, filenameURL);
        this.sourcename(true, sourcename);
        this.setLooping(toLoop);
        this.reset();
        this.loading(true, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void reset() {
        Object object = this.sequenceQueueLock;
        synchronized (object) {
            if (this.sequenceQueue != null) {
                this.sequenceQueue.clear();
            }
        }
        if (this.sequencer == null) {
            this.getSequencer();
        } else {
            this.sequencer.stop();
            this.sequencer.setMicrosecondPosition(0L);
            this.sequencer.removeMetaEventListener(this);
            try {
                Thread.sleep(100L);
            } catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
        if (this.sequencer == null) {
            this.errorMessage("Unable to set the sequence in method 'reset', because there wasn't a sequencer to use.");
            return;
        }
        this.setSequence(this.filenameURL(false, null).getURL());
        this.sequencer.start();
        this.resetGain();
        this.sequencer.addMetaEventListener(this);
    }

    public void setLooping(boolean value) {
        this.toLoop(true, value);
    }

    public boolean getLooping() {
        return this.toLoop(false, false);
    }

    private synchronized boolean toLoop(boolean action, boolean value) {
        if (action) {
            this.toLoop = value;
        }
        return this.toLoop;
    }

    public boolean loading() {
        return this.loading(false, false);
    }

    private synchronized boolean loading(boolean action, boolean value) {
        if (action) {
            this.loading = value;
        }
        return this.loading;
    }

    public void setSourcename(String value) {
        this.sourcename(true, value);
    }

    public String getSourcename() {
        return this.sourcename(false, null);
    }

    private synchronized String sourcename(boolean action, String value) {
        if (action) {
            this.sourcename = value;
        }
        return this.sourcename;
    }

    public void setFilenameURL(FilenameURL value) {
        this.filenameURL(true, value);
    }

    public String getFilename() {
        return this.filenameURL(false, null).getFilename();
    }

    public FilenameURL getFilenameURL() {
        return this.filenameURL(false, null);
    }

    private synchronized FilenameURL filenameURL(boolean action, FilenameURL value) {
        if (action) {
            this.filenameURL = value;
        }
        return this.filenameURL;
    }

    public void meta(MetaMessage message) {
        if (message.getType() == 47) {
            SoundSystemConfig.notifyEOS(this.sourcename, this.sequenceQueue.size());
            if (this.toLoop) {
                if (!this.checkFadeOut()) {
                    if (!this.incrementSequence()) {
                        try {
                            this.sequencer.setMicrosecondPosition(0L);
                            this.sequencer.start();
                            this.resetGain();
                        } catch (Exception e) {}
                    }
                } else if (this.sequencer != null) {
                    try {
                        this.sequencer.setMicrosecondPosition(0L);
                        this.sequencer.start();
                        this.resetGain();
                    } catch (Exception e) {}
                }
            } else if (!this.checkFadeOut()) {
                if (!this.incrementSequence()) {
                    try {
                        this.sequencer.stop();
                        this.sequencer.setMicrosecondPosition(0L);
                        this.sequencer.removeMetaEventListener(this);
                    } catch (Exception e) {}
                }
            } else {
                try {
                    this.sequencer.stop();
                    this.sequencer.setMicrosecondPosition(0L);
                    this.sequencer.removeMetaEventListener(this);
                } catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }

    public void resetGain() {
        if (this.gain < 0.0f) {
            this.gain = 0.0f;
        }
        if (this.gain > 1.0f) {
            this.gain = 1.0f;
        }
        int midiVolume = (int)(this.gain * SoundSystemConfig.getMasterGain() * Math.abs(this.fadeOutGain) * this.fadeInGain * 127.0f);
        if (this.synthesizer != null) {
            javax.sound.midi.MidiChannel[] channels = this.synthesizer.getChannels();
            for (int c = 0; channels != null && c < channels.length; ++c) {
                channels[c].controlChange(7, midiVolume);
            }
        } else if (this.synthDevice != null) {
            try {
                ShortMessage volumeMessage = new ShortMessage();
                for (int i = 0; i < 16; ++i) {
                    volumeMessage.setMessage(176, i, 7, midiVolume);
                    this.synthDevice.getReceiver().send(volumeMessage, -1L);
                }
            } catch (Exception e) {
                this.errorMessage("Error resetting gain on MIDI device");
                this.printStackTrace(e);
            }
        } else if (this.sequencer != null && this.sequencer instanceof Synthesizer) {
            this.synthesizer = (Synthesizer)((Object)this.sequencer);
            javax.sound.midi.MidiChannel[] channels = this.synthesizer.getChannels();
            for (int c = 0; channels != null && c < channels.length; ++c) {
                channels[c].controlChange(7, midiVolume);
            }
        } else {
            try {
                Receiver receiver = MidiSystem.getReceiver();
                ShortMessage volumeMessage = new ShortMessage();
                for (int c = 0; c < 16; ++c) {
                    volumeMessage.setMessage(176, c, 7, midiVolume);
                    receiver.send(volumeMessage, -1L);
                }
            } catch (Exception e) {
                this.errorMessage("Error resetting gain on default receiver");
                this.printStackTrace(e);
            }
        }
    }

    private void getSequencer() {
        block14: {
            try {
                this.sequencer = MidiSystem.getSequencer();
                if (this.sequencer == null) break block14;
                try {
                    this.sequencer.getTransmitter();
                } catch (MidiUnavailableException mue) {
                    this.message("Unable to get a transmitter from the default MIDI sequencer");
                }
                this.sequencer.open();
            } catch (MidiUnavailableException mue) {
                this.message("Unable to open the default MIDI sequencer");
                this.sequencer = null;
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    this.message("Caught InterruptedException while attempting to open the default MIDI sequencer.  Trying again.");
                    this.sequencer = null;
                }
                try {
                    this.sequencer = MidiSystem.getSequencer();
                    if (this.sequencer == null) break block14;
                    try {
                        this.sequencer.getTransmitter();
                    } catch (MidiUnavailableException mue) {
                        this.message("Unable to get a transmitter from the default MIDI sequencer");
                    }
                    this.sequencer.open();
                } catch (MidiUnavailableException mue) {
                    this.message("Unable to open the default MIDI sequencer");
                    this.sequencer = null;
                } catch (Exception e2) {
                    this.message("Unknown error opening the default MIDI sequencer");
                    this.sequencer = null;
                }
            }
        }
        if (this.sequencer == null) {
            this.sequencer = this.openSequencer("Real Time Sequencer");
        }
        if (this.sequencer == null) {
            this.sequencer = this.openSequencer("Java Sound Sequencer");
        }
        if (this.sequencer == null) {
            this.errorMessage("Failed to find an available MIDI sequencer");
            return;
        }
    }

    private void setSequence(URL midiSource) {
        if (this.sequencer == null) {
            this.errorMessage("Unable to update the sequence in method 'setSequence', because variable 'sequencer' is null");
            return;
        }
        if (midiSource == null) {
            this.errorMessage("Unable to load Midi file in method 'setSequence'.");
            return;
        }
        try {
            this.sequence = MidiSystem.getSequence(midiSource);
        } catch (IOException ioe) {
            this.errorMessage("Input failed while reading from MIDI file in method 'setSequence'.");
            this.printStackTrace(ioe);
            return;
        } catch (InvalidMidiDataException imde) {
            this.errorMessage("Invalid MIDI data encountered, or not a MIDI file in method 'setSequence' (1).");
            this.printStackTrace(imde);
            return;
        }
        if (this.sequence == null) {
            this.errorMessage("MidiSystem 'getSequence' method returned null in method 'setSequence'.");
        } else {
            try {
                this.sequencer.setSequence(this.sequence);
            } catch (InvalidMidiDataException imde) {
                this.errorMessage("Invalid MIDI data encountered, or not a MIDI file in method 'setSequence' (2).");
                this.printStackTrace(imde);
                return;
            } catch (Exception e) {
                this.errorMessage("Problem setting sequence from MIDI file in method 'setSequence'.");
                this.printStackTrace(e);
                return;
            }
        }
    }

    private void getSynthesizer() {
        if (this.sequencer == null) {
            this.errorMessage("Unable to load a Synthesizer in method 'getSynthesizer', because variable 'sequencer' is null");
            return;
        }
        String overrideMIDISynthesizer = SoundSystemConfig.getOverrideMIDISynthesizer();
        if (overrideMIDISynthesizer != null && !overrideMIDISynthesizer.equals("")) {
            this.synthDevice = this.openMidiDevice(overrideMIDISynthesizer);
            if (this.synthDevice != null) {
                try {
                    this.sequencer.getTransmitter().setReceiver(this.synthDevice.getReceiver());
                    return;
                } catch (MidiUnavailableException mue) {
                    this.errorMessage("Unable to link sequencer transmitter with receiver for MIDI device '" + overrideMIDISynthesizer + "'");
                }
            }
        }
        if (this.sequencer instanceof Synthesizer) {
            this.synthesizer = (Synthesizer)((Object)this.sequencer);
        } else {
            try {
                this.synthesizer = MidiSystem.getSynthesizer();
                this.synthesizer.open();
            } catch (MidiUnavailableException mue) {
                this.message("Unable to open the default synthesizer");
                this.synthesizer = null;
            }
            if (this.synthesizer == null) {
                this.synthDevice = this.openMidiDevice("Java Sound Synthesizer");
                if (this.synthDevice == null) {
                    this.synthDevice = this.openMidiDevice("Microsoft GS Wavetable");
                }
                if (this.synthDevice == null) {
                    this.synthDevice = this.openMidiDevice("Gervill");
                }
                if (this.synthDevice == null) {
                    this.errorMessage("Failed to find an available MIDI synthesizer");
                    return;
                }
            }
            if (this.synthesizer == null) {
                try {
                    this.sequencer.getTransmitter().setReceiver(this.synthDevice.getReceiver());
                } catch (MidiUnavailableException mue) {
                    this.errorMessage("Unable to link sequencer transmitter with MIDI device receiver");
                }
            } else if (this.synthesizer.getDefaultSoundbank() == null) {
                try {
                    this.sequencer.getTransmitter().setReceiver(MidiSystem.getReceiver());
                } catch (MidiUnavailableException mue) {
                    this.errorMessage("Unable to link sequencer transmitter with default receiver");
                }
            } else {
                try {
                    this.sequencer.getTransmitter().setReceiver(this.synthesizer.getReceiver());
                } catch (MidiUnavailableException mue) {
                    this.errorMessage("Unable to link sequencer transmitter with synthesizer receiver");
                }
            }
        }
    }

    private Sequencer openSequencer(String containsString) {
        Sequencer s = null;
        s = (Sequencer)this.openMidiDevice(containsString);
        if (s == null) {
            return null;
        }
        try {
            s.getTransmitter();
        } catch (MidiUnavailableException mue) {
            this.message("    Unable to get a transmitter from this sequencer");
            s = null;
            return null;
        }
        return s;
    }

    private MidiDevice openMidiDevice(String containsString) {
        this.message("Searching for MIDI device with name containing '" + containsString + "'");
        MidiDevice device = null;
        MidiDevice.Info[] midiDevices = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < midiDevices.length; ++i) {
            device = null;
            try {
                device = MidiSystem.getMidiDevice(midiDevices[i]);
            } catch (MidiUnavailableException e) {
                this.message("    Problem in method 'getMidiDevice':  MIDIUnavailableException was thrown");
                device = null;
            }
            if (device == null || !midiDevices[i].getName().contains(containsString)) continue;
            this.message("    Found MIDI device named '" + midiDevices[i].getName() + "'");
            if (device instanceof Synthesizer) {
                this.message("        *this is a Synthesizer instance");
            }
            if (device instanceof Sequencer) {
                this.message("        *this is a Sequencer instance");
            }
            try {
                device.open();
            } catch (MidiUnavailableException mue) {
                this.message("    Unable to open this MIDI device");
                device = null;
            }
            return device;
        }
        this.message("    MIDI device not found");
        return null;
    }

    protected void message(String message) {
        this.logger.message(message, 0);
    }

    protected void importantMessage(String message) {
        this.logger.importantMessage(message, 0);
    }

    protected boolean errorCheck(boolean error, String message) {
        return this.logger.errorCheck(error, "MidiChannel", message, 0);
    }

    protected void errorMessage(String message) {
        this.logger.errorMessage("MidiChannel", message, 0);
    }

    protected void printStackTrace(Exception e) {
        this.logger.printStackTrace(e, 1);
    }

    private class FadeThread
    extends SimpleThread {
        private FadeThread() {
        }

        public void run() {
            while (!this.dying()) {
                if (MidiChannel.this.fadeOutGain == -1.0f && MidiChannel.this.fadeInGain == 1.0f) {
                    this.snooze(3600000L);
                }
                MidiChannel.this.checkFadeOut();
                this.snooze(50L);
            }
            this.cleanup();
        }
    }
}


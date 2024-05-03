/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package paulscode.sound.libraries;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import javax.sound.sampled.AudioFormat;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import paulscode.sound.Channel;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ChannelLWJGLOpenAL
extends Channel {
    public IntBuffer ALSource;
    public int ALformat;
    public int sampleRate;
    public float millisPreviouslyPlayed = 0.0f;

    public ChannelLWJGLOpenAL(int type, IntBuffer src) {
        super(type);
        this.libraryType = LibraryLWJGLOpenAL.class;
        this.ALSource = src;
    }

    @Override
    public void cleanup() {
        if (this.ALSource != null) {
            try {
                AL10.alSourceStop(this.ALSource);
                AL10.alGetError();
            } catch (Exception e) {
                // empty catch block
            }
            try {
                AL10.alDeleteSources(this.ALSource);
                AL10.alGetError();
            } catch (Exception exception) {
                // empty catch block
            }
            this.ALSource.clear();
        }
        this.ALSource = null;
        super.cleanup();
    }

    public boolean attachBuffer(IntBuffer buf) {
        if (this.errorCheck(this.channelType != 0, "Sound buffers may only be attached to normal sources.")) {
            return false;
        }
        AL10.alSourcei(this.ALSource.get(0), 4105, buf.get(0));
        if (this.attachedSource != null && this.attachedSource.soundBuffer != null && this.attachedSource.soundBuffer.audioFormat != null) {
            this.setAudioFormat(this.attachedSource.soundBuffer.audioFormat);
        }
        return this.checkALError();
    }

    /*
     * Enabled aggressive block sorting
     */
    @Override
    public void setAudioFormat(AudioFormat audioFormat) {
        int soundFormat = 0;
        if (audioFormat.getChannels() == 1) {
            if (audioFormat.getSampleSizeInBits() == 8) {
                soundFormat = 4352;
            } else {
                if (audioFormat.getSampleSizeInBits() != 16) {
                    this.errorMessage("Illegal sample size in method 'setAudioFormat'");
                    return;
                }
                soundFormat = 4353;
            }
        } else {
            if (audioFormat.getChannels() != 2) {
                this.errorMessage("Audio data neither mono nor stereo in method 'setAudioFormat'");
                return;
            }
            if (audioFormat.getSampleSizeInBits() == 8) {
                soundFormat = 4354;
            } else {
                if (audioFormat.getSampleSizeInBits() != 16) {
                    this.errorMessage("Illegal sample size in method 'setAudioFormat'");
                    return;
                }
                soundFormat = 4355;
            }
        }
        this.ALformat = soundFormat;
        this.sampleRate = (int)audioFormat.getSampleRate();
    }

    public void setFormat(int format, int rate) {
        this.ALformat = format;
        this.sampleRate = rate;
    }

    @Override
    public boolean preLoadBuffers(LinkedList<byte[]> bufferList) {
        IntBuffer streamBuffers;
        int processed;
        if (this.errorCheck(this.channelType != 1, "Buffers may only be queued for streaming sources.")) {
            return false;
        }
        if (this.errorCheck(bufferList == null, "Buffer List null in method 'preLoadBuffers'")) {
            return false;
        }
        boolean playing = this.playing();
        if (playing) {
            AL10.alSourceStop(this.ALSource.get(0));
            this.checkALError();
        }
        if ((processed = AL10.alGetSourcei(this.ALSource.get(0), 4118)) > 0) {
            streamBuffers = BufferUtils.createIntBuffer(processed);
            AL10.alGenBuffers(streamBuffers);
            if (this.errorCheck(this.checkALError(), "Error clearing stream buffers in method 'preLoadBuffers'")) {
                return false;
            }
            AL10.alSourceUnqueueBuffers(this.ALSource.get(0), streamBuffers);
            if (this.errorCheck(this.checkALError(), "Error unqueuing stream buffers in method 'preLoadBuffers'")) {
                return false;
            }
        }
        if (playing) {
            AL10.alSourcePlay(this.ALSource.get(0));
            this.checkALError();
        }
        streamBuffers = BufferUtils.createIntBuffer(bufferList.size());
        AL10.alGenBuffers(streamBuffers);
        if (this.errorCheck(this.checkALError(), "Error generating stream buffers in method 'preLoadBuffers'")) {
            return false;
        }
        ByteBuffer byteBuffer = null;
        for (int i = 0; i < bufferList.size(); ++i) {
            byteBuffer = (ByteBuffer)BufferUtils.createByteBuffer(bufferList.get(i).length).put(bufferList.get(i)).flip();
            try {
                AL10.alBufferData(streamBuffers.get(i), this.ALformat, byteBuffer, this.sampleRate);
            } catch (Exception e) {
                this.errorMessage("Error creating buffers in method 'preLoadBuffers'");
                this.printStackTrace(e);
                return false;
            }
            if (!this.errorCheck(this.checkALError(), "Error creating buffers in method 'preLoadBuffers'")) continue;
            return false;
        }
        try {
            AL10.alSourceQueueBuffers(this.ALSource.get(0), streamBuffers);
        } catch (Exception e) {
            this.errorMessage("Error queuing buffers in method 'preLoadBuffers'");
            this.printStackTrace(e);
            return false;
        }
        if (this.errorCheck(this.checkALError(), "Error queuing buffers in method 'preLoadBuffers'")) {
            return false;
        }
        AL10.alSourcePlay(this.ALSource.get(0));
        return !this.errorCheck(this.checkALError(), "Error playing source in method 'preLoadBuffers'");
    }

    @Override
    public boolean queueBuffer(byte[] buffer) {
        if (this.errorCheck(this.channelType != 1, "Buffers may only be queued for streaming sources.")) {
            return false;
        }
        ByteBuffer byteBuffer = (ByteBuffer)BufferUtils.createByteBuffer(buffer.length).put(buffer).flip();
        IntBuffer intBuffer = BufferUtils.createIntBuffer(1);
        AL10.alSourceUnqueueBuffers(this.ALSource.get(0), intBuffer);
        if (this.checkALError()) {
            return false;
        }
        if (AL10.alIsBuffer(intBuffer.get(0))) {
            this.millisPreviouslyPlayed += this.millisInBuffer(intBuffer.get(0));
        }
        this.checkALError();
        AL10.alBufferData(intBuffer.get(0), this.ALformat, byteBuffer, this.sampleRate);
        if (this.checkALError()) {
            return false;
        }
        AL10.alSourceQueueBuffers(this.ALSource.get(0), intBuffer);
        return !this.checkALError();
    }

    @Override
    public int feedRawAudioData(byte[] buffer) {
        IntBuffer intBuffer;
        if (this.errorCheck(this.channelType != 1, "Raw audio data can only be fed to streaming sources.")) {
            return -1;
        }
        ByteBuffer byteBuffer = (ByteBuffer)BufferUtils.createByteBuffer(buffer.length).put(buffer).flip();
        int processed = AL10.alGetSourcei(this.ALSource.get(0), 4118);
        if (processed > 0) {
            intBuffer = BufferUtils.createIntBuffer(processed);
            AL10.alGenBuffers(intBuffer);
            if (this.errorCheck(this.checkALError(), "Error clearing stream buffers in method 'feedRawAudioData'")) {
                return -1;
            }
            AL10.alSourceUnqueueBuffers(this.ALSource.get(0), intBuffer);
            if (this.errorCheck(this.checkALError(), "Error unqueuing stream buffers in method 'feedRawAudioData'")) {
                return -1;
            }
            if (AL10.alIsBuffer(intBuffer.get(0))) {
                this.millisPreviouslyPlayed += this.millisInBuffer(intBuffer.get(0));
            }
            this.checkALError();
        } else {
            intBuffer = BufferUtils.createIntBuffer(1);
            AL10.alGenBuffers(intBuffer);
            if (this.errorCheck(this.checkALError(), "Error generating stream buffers in method 'preLoadBuffers'")) {
                return -1;
            }
        }
        AL10.alBufferData(intBuffer.get(0), this.ALformat, byteBuffer, this.sampleRate);
        if (this.checkALError()) {
            return -1;
        }
        AL10.alSourceQueueBuffers(this.ALSource.get(0), intBuffer);
        if (this.checkALError()) {
            return -1;
        }
        if (this.attachedSource != null && this.attachedSource.channel == this && this.attachedSource.active() && !this.playing()) {
            AL10.alSourcePlay(this.ALSource.get(0));
            this.checkALError();
        }
        return processed;
    }

    public float millisInBuffer(int alBufferi) {
        return (float)AL10.alGetBufferi(alBufferi, 8196) / (float)AL10.alGetBufferi(alBufferi, 8195) / ((float)AL10.alGetBufferi(alBufferi, 8194) / 8.0f) / (float)this.sampleRate * 1000.0f;
    }

    @Override
    public float millisecondsPlayed() {
        float offset = AL10.alGetSourcei(this.ALSource.get(0), 4134);
        float bytesPerFrame = 1.0f;
        switch (this.ALformat) {
            case 4352: {
                bytesPerFrame = 1.0f;
                break;
            }
            case 4353: {
                bytesPerFrame = 2.0f;
                break;
            }
            case 4354: {
                bytesPerFrame = 2.0f;
                break;
            }
            case 4355: {
                bytesPerFrame = 4.0f;
                break;
            }
        }
        offset = offset / bytesPerFrame / (float)this.sampleRate * 1000.0f;
        if (this.channelType == 1) {
            offset += this.millisPreviouslyPlayed;
        }
        return offset;
    }

    @Override
    public int buffersProcessed() {
        if (this.channelType != 1) {
            return 0;
        }
        int processed = AL10.alGetSourcei(this.ALSource.get(0), 4118);
        if (this.checkALError()) {
            return 0;
        }
        return processed;
    }

    @Override
    public void flush() {
        if (this.channelType != 1) {
            return;
        }
        if (this.checkALError()) {
            return;
        }
        IntBuffer intBuffer = BufferUtils.createIntBuffer(1);
        for (int queued = AL10.alGetSourcei(this.ALSource.get(0), 4117); queued > 0; --queued) {
            try {
                AL10.alSourceUnqueueBuffers(this.ALSource.get(0), intBuffer);
            } catch (Exception e) {
                return;
            }
            if (!this.checkALError()) continue;
            return;
        }
        this.millisPreviouslyPlayed = 0.0f;
    }

    @Override
    public void close() {
        try {
            AL10.alSourceStop(this.ALSource.get(0));
            AL10.alGetError();
        } catch (Exception exception) {
            // empty catch block
        }
        if (this.channelType == 1) {
            this.flush();
        }
    }

    @Override
    public void play() {
        AL10.alSourcePlay(this.ALSource.get(0));
        this.checkALError();
    }

    @Override
    public void pause() {
        AL10.alSourcePause(this.ALSource.get(0));
        this.checkALError();
    }

    @Override
    public void stop() {
        AL10.alSourceStop(this.ALSource.get(0));
        if (!this.checkALError()) {
            this.millisPreviouslyPlayed = 0.0f;
        }
    }

    @Override
    public void rewind() {
        if (this.channelType == 1) {
            return;
        }
        AL10.alSourceRewind(this.ALSource.get(0));
        if (!this.checkALError()) {
            this.millisPreviouslyPlayed = 0.0f;
        }
    }

    @Override
    public boolean playing() {
        int state = AL10.alGetSourcei(this.ALSource.get(0), 4112);
        if (this.checkALError()) {
            return false;
        }
        return state == 4114;
    }

    private boolean checkALError() {
        switch (AL10.alGetError()) {
            case 0: {
                return false;
            }
            case 40961: {
                this.errorMessage("Invalid name parameter.");
                return true;
            }
            case 40962: {
                this.errorMessage("Invalid parameter.");
                return true;
            }
            case 40963: {
                this.errorMessage("Invalid enumerated parameter value.");
                return true;
            }
            case 40964: {
                this.errorMessage("Illegal call.");
                return true;
            }
            case 40965: {
                this.errorMessage("Unable to allocate memory.");
                return true;
            }
        }
        this.errorMessage("An unrecognized error occurred.");
        return true;
    }
}


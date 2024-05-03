/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch;

import tv.twitch.CoreAPI;
import tv.twitch.ErrorCode;
import tv.twitch.MessageLevel;

public class StandardCoreAPI
extends CoreAPI {
    public StandardCoreAPI() {
        try {
            System.loadLibrary("twitchsdk");
        } catch (UnsatisfiedLinkError unsatisfiedLinkError) {
            System.out.println("If on Windows, make sure to provide all of the necessary dll's as specified in the twitchsdk README. Also, make sure to set the PATH environment variable to point to the directory containing the dll's.");
            throw unsatisfiedLinkError;
        }
    }

    protected void finalize() {
    }

    private static native ErrorCode TTV_Java_Init(String var0, String var1);

    private static native ErrorCode TTV_Java_Shutdown();

    private static native ErrorCode TTV_Java_SetTraceLevel(int var0);

    private static native ErrorCode TTV_Java_SetTraceOutput(String var0);

    private static native String TTV_Java_ErrorToString(ErrorCode var0);

    @Override
    public ErrorCode init(String string, String string2) {
        return StandardCoreAPI.TTV_Java_Init(string, string2);
    }

    @Override
    public ErrorCode shutdown() {
        return StandardCoreAPI.TTV_Java_Shutdown();
    }

    @Override
    public ErrorCode setTraceLevel(MessageLevel messageLevel) {
        if (messageLevel == null) {
            messageLevel = MessageLevel.TTV_ML_NONE;
        }
        return StandardCoreAPI.TTV_Java_SetTraceLevel(messageLevel.getValue());
    }

    @Override
    public ErrorCode setTraceOutput(String string) {
        return StandardCoreAPI.TTV_Java_SetTraceOutput(string);
    }

    @Override
    public String errorToString(ErrorCode errorCode) {
        if (errorCode == null) {
            return null;
        }
        return StandardCoreAPI.TTV_Java_ErrorToString(errorCode);
    }
}


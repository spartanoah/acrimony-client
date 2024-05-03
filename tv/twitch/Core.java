/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch;

import tv.twitch.CoreAPI;
import tv.twitch.ErrorCode;
import tv.twitch.MessageLevel;

public class Core {
    private static Core s_Instance = null;
    private CoreAPI m_CoreAPI = null;
    private String m_ClientId = null;
    private int m_NumInitializations = 0;

    public static Core getInstance() {
        return s_Instance;
    }

    public Core(CoreAPI coreAPI) {
        this.m_CoreAPI = coreAPI;
        if (s_Instance == null) {
            s_Instance = this;
        }
    }

    public boolean getIsInitialized() {
        return this.m_NumInitializations > 0;
    }

    public ErrorCode initialize(String string, String string2) {
        if (this.m_NumInitializations == 0) {
            this.m_ClientId = string;
        } else if (string != this.m_ClientId) {
            return ErrorCode.TTV_EC_INVALID_CLIENTID;
        }
        ++this.m_NumInitializations;
        if (this.m_NumInitializations > 1) {
            return ErrorCode.TTV_EC_SUCCESS;
        }
        ErrorCode errorCode = this.m_CoreAPI.init(string, string2);
        if (ErrorCode.failed(errorCode)) {
            --this.m_NumInitializations;
            this.m_ClientId = null;
        }
        return errorCode;
    }

    public ErrorCode shutdown() {
        if (this.m_NumInitializations == 0) {
            return ErrorCode.TTV_EC_NOT_INITIALIZED;
        }
        --this.m_NumInitializations;
        ErrorCode errorCode = ErrorCode.TTV_EC_SUCCESS;
        if (this.m_NumInitializations == 0) {
            errorCode = this.m_CoreAPI.shutdown();
            if (ErrorCode.failed(errorCode)) {
                ++this.m_NumInitializations;
            } else if (s_Instance == this) {
                s_Instance = null;
            }
        }
        return errorCode;
    }

    public ErrorCode setTraceLevel(MessageLevel messageLevel) {
        ErrorCode errorCode = this.m_CoreAPI.setTraceLevel(messageLevel);
        return errorCode;
    }

    public String errorToString(ErrorCode errorCode) {
        String string = this.m_CoreAPI.errorToString(errorCode);
        return string;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.format;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.format.DataFormatMatcher;
import com.fasterxml.jackson.core.format.InputAccessor;
import com.fasterxml.jackson.core.format.MatchStrength;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class DataFormatDetector {
    public static final int DEFAULT_MAX_INPUT_LOOKAHEAD = 64;
    protected final JsonFactory[] _detectors;
    protected final MatchStrength _optimalMatch;
    protected final MatchStrength _minimalMatch;
    protected final int _maxInputLookahead;

    public DataFormatDetector(JsonFactory ... detectors) {
        this(detectors, MatchStrength.SOLID_MATCH, MatchStrength.WEAK_MATCH, 64);
    }

    public DataFormatDetector(Collection<JsonFactory> detectors) {
        this(detectors.toArray(new JsonFactory[0]));
    }

    private DataFormatDetector(JsonFactory[] detectors, MatchStrength optMatch, MatchStrength minMatch, int maxInputLookahead) {
        this._detectors = detectors;
        this._optimalMatch = optMatch;
        this._minimalMatch = minMatch;
        this._maxInputLookahead = maxInputLookahead;
    }

    public DataFormatDetector withOptimalMatch(MatchStrength optMatch) {
        if (optMatch == this._optimalMatch) {
            return this;
        }
        return new DataFormatDetector(this._detectors, optMatch, this._minimalMatch, this._maxInputLookahead);
    }

    public DataFormatDetector withMinimalMatch(MatchStrength minMatch) {
        if (minMatch == this._minimalMatch) {
            return this;
        }
        return new DataFormatDetector(this._detectors, this._optimalMatch, minMatch, this._maxInputLookahead);
    }

    public DataFormatDetector withMaxInputLookahead(int lookaheadBytes) {
        if (lookaheadBytes == this._maxInputLookahead) {
            return this;
        }
        return new DataFormatDetector(this._detectors, this._optimalMatch, this._minimalMatch, lookaheadBytes);
    }

    public DataFormatMatcher findFormat(InputStream in) throws IOException {
        return this._findFormat(new InputAccessor.Std(in, new byte[this._maxInputLookahead]));
    }

    public DataFormatMatcher findFormat(byte[] fullInputData) throws IOException {
        return this._findFormat(new InputAccessor.Std(fullInputData));
    }

    public DataFormatMatcher findFormat(byte[] fullInputData, int offset, int len) throws IOException {
        return this._findFormat(new InputAccessor.Std(fullInputData, offset, len));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        int len = this._detectors.length;
        if (len > 0) {
            sb.append(this._detectors[0].getFormatName());
            for (int i = 1; i < len; ++i) {
                sb.append(", ");
                sb.append(this._detectors[i].getFormatName());
            }
        }
        sb.append(']');
        return sb.toString();
    }

    private DataFormatMatcher _findFormat(InputAccessor.Std acc) throws IOException {
        JsonFactory bestMatch = null;
        Enum bestMatchStrength = null;
        for (JsonFactory f : this._detectors) {
            acc.reset();
            MatchStrength strength = f.hasFormat(acc);
            if (strength == null || strength.ordinal() < this._minimalMatch.ordinal() || bestMatch != null && bestMatchStrength.ordinal() >= strength.ordinal()) continue;
            bestMatch = f;
            bestMatchStrength = strength;
            if (strength.ordinal() >= this._optimalMatch.ordinal()) break;
        }
        return acc.createMatcher(bestMatch, (MatchStrength)bestMatchStrength);
    }
}


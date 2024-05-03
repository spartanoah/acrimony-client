/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.auth;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import org.apache.hc.client5.http.auth.AuthChallenge;
import org.apache.hc.client5.http.auth.ChallengeType;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.message.ParserCursor;
import org.apache.hc.core5.http.message.TokenParser;
import org.apache.hc.core5.util.TextUtils;

public class AuthChallengeParser {
    public static final AuthChallengeParser INSTANCE = new AuthChallengeParser();
    private final TokenParser tokenParser = TokenParser.INSTANCE;
    private static final char BLANK = ' ';
    private static final char COMMA_CHAR = ',';
    private static final char EQUAL_CHAR = '=';
    private static final BitSet TERMINATORS = TokenParser.INIT_BITSET(32, 61, 44);
    private static final BitSet DELIMITER = TokenParser.INIT_BITSET(44);
    private static final BitSet SPACE = TokenParser.INIT_BITSET(32);

    public List<AuthChallenge> parse(ChallengeType challengeType, CharSequence buffer, ParserCursor cursor) throws ParseException {
        this.tokenParser.skipWhiteSpace(buffer, cursor);
        if (cursor.atEnd()) {
            throw new ParseException("Malformed auth challenge");
        }
        ArrayList<ChallengeInt> internalChallenges = new ArrayList<ChallengeInt>();
        String schemeName = this.tokenParser.parseToken(buffer, cursor, SPACE);
        if (TextUtils.isBlank(schemeName)) {
            throw new ParseException("Malformed auth challenge");
        }
        ChallengeInt current = new ChallengeInt(schemeName);
        while (current != null) {
            internalChallenges.add(current);
            current = this.parseChallenge(buffer, cursor, current);
        }
        ArrayList<AuthChallenge> challenges = new ArrayList<AuthChallenge>(internalChallenges.size());
        for (ChallengeInt internal : internalChallenges) {
            NameValuePair param;
            List<NameValuePair> params = internal.params;
            String token68 = null;
            if (params.size() == 1 && (param = params.get(0)).getValue() == null) {
                token68 = param.getName();
                params.clear();
            }
            challenges.add(new AuthChallenge(challengeType, internal.schemeName, token68, !params.isEmpty() ? params : null));
        }
        return challenges;
    }

    ChallengeInt parseChallenge(CharSequence buffer, ParserCursor cursor, ChallengeInt currentChallenge) throws ParseException {
        String token;
        while (true) {
            this.tokenParser.skipWhiteSpace(buffer, cursor);
            if (cursor.atEnd()) {
                return null;
            }
            token = this.parseToken(buffer, cursor);
            if (TextUtils.isBlank(token)) {
                throw new ParseException("Malformed auth challenge");
            }
            this.tokenParser.skipWhiteSpace(buffer, cursor);
            if (cursor.atEnd()) {
                currentChallenge.params.add(new BasicNameValuePair(token, null));
                continue;
            }
            char ch = buffer.charAt(cursor.getPos());
            if (ch == '=') {
                cursor.updatePos(cursor.getPos() + 1);
                String value = this.tokenParser.parseValue(buffer, cursor, DELIMITER);
                this.tokenParser.skipWhiteSpace(buffer, cursor);
                if (!cursor.atEnd() && (ch = buffer.charAt(cursor.getPos())) == ',') {
                    cursor.updatePos(cursor.getPos() + 1);
                }
                currentChallenge.params.add(new BasicNameValuePair(token, value));
                continue;
            }
            if (ch != ',') break;
            cursor.updatePos(cursor.getPos() + 1);
            currentChallenge.params.add(new BasicNameValuePair(token, null));
        }
        if (currentChallenge.params.isEmpty()) {
            throw new ParseException("Malformed auth challenge");
        }
        return new ChallengeInt(token);
    }

    String parseToken(CharSequence buf, ParserCursor cursor) {
        StringBuilder dst = new StringBuilder();
        while (!cursor.atEnd()) {
            int pos = cursor.getPos();
            char current = buf.charAt(pos);
            if (TERMINATORS.get(current)) {
                if (current != '=' || pos + 1 < cursor.getUpperBound() && buf.charAt(pos + 1) != '=') break;
                do {
                    dst.append(current);
                    cursor.updatePos(++pos);
                } while (!cursor.atEnd() && (current = buf.charAt(pos)) == '=');
                continue;
            }
            dst.append(current);
            cursor.updatePos(pos + 1);
        }
        return dst.toString();
    }

    static class ChallengeInt {
        final String schemeName;
        final List<NameValuePair> params;

        ChallengeInt(String schemeName) {
            this.schemeName = schemeName;
            this.params = new ArrayList<NameValuePair>();
        }

        public String toString() {
            return "ChallengeInternal{schemeName='" + this.schemeName + '\'' + ", params=" + this.params + '}';
        }
    }
}


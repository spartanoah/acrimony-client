/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.optifine.expr;

import net.optifine.expr.TokenType;

public class Token {
    private TokenType type;
    private String text;

    public Token(TokenType type, String text) {
        this.type = type;
        this.text = text;
    }

    public TokenType getType() {
        return this.type;
    }

    public String getText() {
        return this.text;
    }

    public String toString() {
        return this.text;
    }
}


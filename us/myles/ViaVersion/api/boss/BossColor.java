/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package us.myles.ViaVersion.api.boss;

@Deprecated
public enum BossColor {
    PINK(0),
    BLUE(1),
    RED(2),
    GREEN(3),
    YELLOW(4),
    PURPLE(5),
    WHITE(6);

    private final int id;

    private BossColor(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}


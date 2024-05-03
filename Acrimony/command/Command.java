/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.command;

public abstract class Command {
    private final String name;
    private final String description;
    private String[] aliases;

    public Command(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Command(String name, String description, String ... aliases) {
        this.name = name;
        this.description = description;
        this.aliases = aliases;
    }

    public abstract void onCommand(String[] var1);

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String[] getAliases() {
        return this.aliases;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.events.click;

public enum ClickEventAction {
    OPEN_URL("open_url", true),
    OPEN_FILE("open_file", false),
    RUN_COMMAND("run_command", true),
    TWITCH_USER_INFO("twitch_user_info", false),
    SUGGEST_COMMAND("suggest_command", true),
    CHANGE_PAGE("change_page", true),
    COPY_TO_CLIPBOARD("copy_to_clipboard", true);

    private final String name;
    private final boolean userDefinable;

    public static ClickEventAction getByName(String name) {
        return ClickEventAction.getByName(name, true);
    }

    public static ClickEventAction getByName(String name, boolean ignoreCase) {
        for (ClickEventAction clickEventAction : ClickEventAction.values()) {
            if (!(ignoreCase ? clickEventAction.getName().equalsIgnoreCase(name) : clickEventAction.getName().equals(name))) continue;
            return clickEventAction;
        }
        return null;
    }

    private ClickEventAction(String name, boolean userDefinable) {
        this.name = name;
        this.userDefinable = userDefinable;
    }

    public String getName() {
        return this.name;
    }

    public boolean isUserDefinable() {
        return this.userDefinable;
    }
}


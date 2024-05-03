/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.message;

import org.apache.logging.log4j.message.ThreadInformation;
import org.apache.logging.log4j.util.StringBuilders;

class BasicThreadInformation
implements ThreadInformation {
    private static final int HASH_SHIFT = 32;
    private static final int HASH_MULTIPLIER = 31;
    private final long id;
    private final String name;
    private final String longName;
    private final Thread.State state;
    private final int priority;
    private final boolean isAlive;
    private final boolean isDaemon;
    private final String threadGroupName;

    BasicThreadInformation(Thread thread) {
        this.id = thread.getId();
        this.name = thread.getName();
        this.longName = thread.toString();
        this.state = thread.getState();
        this.priority = thread.getPriority();
        this.isAlive = thread.isAlive();
        this.isDaemon = thread.isDaemon();
        ThreadGroup group = thread.getThreadGroup();
        this.threadGroupName = group == null ? null : group.getName();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        BasicThreadInformation that = (BasicThreadInformation)o;
        if (this.id != that.id) {
            return false;
        }
        return !(this.name != null ? !this.name.equals(that.name) : that.name != null);
    }

    public int hashCode() {
        int result = (int)(this.id ^ this.id >>> 32);
        result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
        return result;
    }

    @Override
    public void printThreadInfo(StringBuilder sb) {
        StringBuilders.appendDqValue(sb, this.name).append(' ');
        if (this.isDaemon) {
            sb.append("daemon ");
        }
        sb.append("prio=").append(this.priority).append(" tid=").append(this.id).append(' ');
        if (this.threadGroupName != null) {
            StringBuilders.appendKeyDqValue(sb, "group", this.threadGroupName);
        }
        sb.append('\n');
        sb.append("\tThread state: ").append(this.state.name()).append('\n');
    }

    @Override
    public void printStack(StringBuilder sb, StackTraceElement[] trace) {
        for (StackTraceElement element : trace) {
            sb.append("\tat ").append(element).append('\n');
        }
    }
}


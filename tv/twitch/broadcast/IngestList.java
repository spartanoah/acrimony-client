/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.broadcast;

import tv.twitch.broadcast.IngestServer;

public class IngestList {
    protected IngestServer[] servers = null;
    protected IngestServer defaultServer = null;

    public IngestServer[] getServers() {
        return this.servers;
    }

    public IngestServer getDefaultServer() {
        return this.defaultServer;
    }

    public IngestList(IngestServer[] ingestServerArray) {
        if (ingestServerArray == null) {
            this.servers = new IngestServer[0];
        } else {
            this.servers = new IngestServer[ingestServerArray.length];
            for (int i = 0; i < ingestServerArray.length; ++i) {
                this.servers[i] = ingestServerArray[i];
                if (!this.servers[i].defaultServer) continue;
                this.defaultServer = this.servers[i];
            }
            if (this.defaultServer == null && this.servers.length > 0) {
                this.defaultServer = this.servers[0];
            }
        }
    }

    public IngestServer getBestServer() {
        if (this.servers == null || this.servers.length == 0) {
            return null;
        }
        IngestServer ingestServer = this.servers[0];
        for (int i = 1; i < this.servers.length; ++i) {
            if (!(ingestServer.bitrateKbps < this.servers[i].bitrateKbps)) continue;
            ingestServer = this.servers[i];
        }
        return ingestServer;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.jcraft.jogg;

import com.jcraft.jogg.Page;

public class SyncState {
    public byte[] data;
    int storage;
    int fill;
    int returned;
    int unsynced;
    int headerbytes;
    int bodybytes;
    private Page pageseek = new Page();
    private byte[] chksum = new byte[4];

    public int clear() {
        this.data = null;
        return 0;
    }

    public int buffer(int size) {
        if (this.returned != 0) {
            this.fill -= this.returned;
            if (this.fill > 0) {
                System.arraycopy(this.data, this.returned, this.data, 0, this.fill);
            }
            this.returned = 0;
        }
        if (size > this.storage - this.fill) {
            int newsize = size + this.fill + 4096;
            if (this.data != null) {
                byte[] foo = new byte[newsize];
                System.arraycopy(this.data, 0, foo, 0, this.data.length);
                this.data = foo;
            } else {
                this.data = new byte[newsize];
            }
            this.storage = newsize;
        }
        return this.fill;
    }

    public int wrote(int bytes) {
        if (this.fill + bytes > this.storage) {
            return -1;
        }
        this.fill += bytes;
        return 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int pageseek(Page og) {
        int page = this.returned;
        int bytes = this.fill - this.returned;
        if (this.headerbytes == 0) {
            if (bytes < 27) {
                return 0;
            }
            if (this.data[page] != 79 || this.data[page + 1] != 103 || this.data[page + 2] != 103 || this.data[page + 3] != 83) {
                this.headerbytes = 0;
                this.bodybytes = 0;
                int next = 0;
                for (int ii = 0; ii < bytes - 1; ++ii) {
                    if (this.data[page + 1 + ii] != 79) continue;
                    next = page + 1 + ii;
                    break;
                }
                if (next == 0) {
                    next = this.fill;
                }
                this.returned = next;
                return -(next - page);
            }
            int _headerbytes = (this.data[page + 26] & 0xFF) + 27;
            if (bytes < _headerbytes) {
                return 0;
            }
            for (int i = 0; i < (this.data[page + 26] & 0xFF); ++i) {
                this.bodybytes += this.data[page + 27 + i] & 0xFF;
            }
            this.headerbytes = _headerbytes;
        }
        if (this.bodybytes + this.headerbytes > bytes) {
            return 0;
        }
        byte[] byArray = this.chksum;
        synchronized (this.chksum) {
            System.arraycopy(this.data, page + 22, this.chksum, 0, 4);
            this.data[page + 22] = 0;
            this.data[page + 23] = 0;
            this.data[page + 24] = 0;
            this.data[page + 25] = 0;
            Page log = this.pageseek;
            log.header_base = this.data;
            log.header = page;
            log.header_len = this.headerbytes;
            log.body_base = this.data;
            log.body = page + this.headerbytes;
            log.body_len = this.bodybytes;
            log.checksum();
            if (this.chksum[0] != this.data[page + 22] || this.chksum[1] != this.data[page + 23] || this.chksum[2] != this.data[page + 24] || this.chksum[3] != this.data[page + 25]) {
                System.arraycopy(this.chksum, 0, this.data, page + 22, 4);
                this.headerbytes = 0;
                this.bodybytes = 0;
                int next = 0;
                for (int ii = 0; ii < bytes - 1; ++ii) {
                    if (this.data[page + 1 + ii] != 79) continue;
                    next = page + 1 + ii;
                    break;
                }
                if (next == 0) {
                    next = this.fill;
                }
                this.returned = next;
                // ** MonitorExit[var5_9] (shouldn't be in output)
                return -(next - page);
            }
            // ** MonitorExit[var5_9] (shouldn't be in output)
            page = this.returned;
            if (og != null) {
                og.header_base = this.data;
                og.header = page;
                og.header_len = this.headerbytes;
                og.body_base = this.data;
                og.body = page + this.headerbytes;
                og.body_len = this.bodybytes;
            }
            this.unsynced = 0;
            bytes = this.headerbytes + this.bodybytes;
            this.returned += bytes;
            this.headerbytes = 0;
            this.bodybytes = 0;
            return bytes;
        }
    }

    public int pageout(Page og) {
        do {
            int ret;
            if ((ret = this.pageseek(og)) > 0) {
                return 1;
            }
            if (ret != 0) continue;
            return 0;
        } while (this.unsynced != 0);
        this.unsynced = 1;
        return -1;
    }

    public int reset() {
        this.fill = 0;
        this.returned = 0;
        this.unsynced = 0;
        this.headerbytes = 0;
        this.bodybytes = 0;
        return 0;
    }

    public void init() {
    }

    public int getDataOffset() {
        return this.returned;
    }

    public int getBufferOffset() {
        return this.fill;
    }
}


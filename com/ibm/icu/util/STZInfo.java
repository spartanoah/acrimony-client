/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.util;

import com.ibm.icu.util.SimpleTimeZone;
import java.io.Serializable;

final class STZInfo
implements Serializable {
    private static final long serialVersionUID = -7849612037842370168L;
    int sy = -1;
    int sm = -1;
    int sdwm;
    int sdw;
    int st;
    int sdm;
    boolean sa;
    int em = -1;
    int edwm;
    int edw;
    int et;
    int edm;
    boolean ea;

    STZInfo() {
    }

    void setStart(int sm, int sdwm, int sdw, int st, int sdm, boolean sa) {
        this.sm = sm;
        this.sdwm = sdwm;
        this.sdw = sdw;
        this.st = st;
        this.sdm = sdm;
        this.sa = sa;
    }

    void setEnd(int em, int edwm, int edw, int et, int edm, boolean ea) {
        this.em = em;
        this.edwm = edwm;
        this.edw = edw;
        this.et = et;
        this.edm = edm;
        this.ea = ea;
    }

    void applyTo(SimpleTimeZone stz) {
        if (this.sy != -1) {
            stz.setStartYear(this.sy);
        }
        if (this.sm != -1) {
            if (this.sdm == -1) {
                stz.setStartRule(this.sm, this.sdwm, this.sdw, this.st);
            } else if (this.sdw == -1) {
                stz.setStartRule(this.sm, this.sdm, this.st);
            } else {
                stz.setStartRule(this.sm, this.sdm, this.sdw, this.st, this.sa);
            }
        }
        if (this.em != -1) {
            if (this.edm == -1) {
                stz.setEndRule(this.em, this.edwm, this.edw, this.et);
            } else if (this.edw == -1) {
                stz.setEndRule(this.em, this.edm, this.et);
            } else {
                stz.setEndRule(this.em, this.edm, this.edw, this.et, this.ea);
            }
        }
    }
}


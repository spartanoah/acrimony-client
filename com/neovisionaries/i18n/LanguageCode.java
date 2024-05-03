/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.neovisionaries.i18n;

import com.neovisionaries.i18n.LanguageAlpha3Code;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public enum LanguageCode {
    undefined{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.undefined;
        }
    }
    ,
    aa{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.aar;
        }
    }
    ,
    ab{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.abk;
        }
    }
    ,
    ae{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ave;
        }
    }
    ,
    af{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.afr;
        }
    }
    ,
    ak{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.aka;
        }
    }
    ,
    am{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.amh;
        }
    }
    ,
    an{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.arg;
        }
    }
    ,
    ar{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ara;
        }
    }
    ,
    as{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.asm;
        }
    }
    ,
    av{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ava;
        }
    }
    ,
    ay{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.aym;
        }
    }
    ,
    az{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.aze;
        }
    }
    ,
    ba{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.bak;
        }
    }
    ,
    be{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.bel;
        }
    }
    ,
    bg{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.bul;
        }
    }
    ,
    bh{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.bih;
        }
    }
    ,
    bi{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.bis;
        }
    }
    ,
    bm{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.bam;
        }
    }
    ,
    bn{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ben;
        }
    }
    ,
    bo{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.bod;
        }
    }
    ,
    br{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.bre;
        }
    }
    ,
    bs{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.bos;
        }
    }
    ,
    ca{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.cat;
        }
    }
    ,
    ce{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.che;
        }
    }
    ,
    ch{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.cha;
        }
    }
    ,
    co{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.cos;
        }
    }
    ,
    cr{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.cre;
        }
    }
    ,
    cs{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ces;
        }
    }
    ,
    cu{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.chu;
        }
    }
    ,
    cv{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.chv;
        }
    }
    ,
    cy{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.cym;
        }
    }
    ,
    da{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.dan;
        }
    }
    ,
    de{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.deu;
        }

        public Locale toLocale() {
            return Locale.GERMAN;
        }
    }
    ,
    dv{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.div;
        }
    }
    ,
    dz{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.dzo;
        }
    }
    ,
    ee{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ewe;
        }
    }
    ,
    el{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ell;
        }
    }
    ,
    en{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.eng;
        }

        public Locale toLocale() {
            return Locale.ENGLISH;
        }
    }
    ,
    eo{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.epo;
        }
    }
    ,
    es{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.spa;
        }
    }
    ,
    et{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.est;
        }
    }
    ,
    eu{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.eus;
        }
    }
    ,
    fa{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.fas;
        }
    }
    ,
    ff{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ful;
        }
    }
    ,
    fi{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.fin;
        }
    }
    ,
    fj{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.fij;
        }
    }
    ,
    fo{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.fao;
        }
    }
    ,
    fr{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.fra;
        }

        public Locale toLocale() {
            return Locale.FRENCH;
        }
    }
    ,
    fy{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.fry;
        }
    }
    ,
    ga{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.gle;
        }
    }
    ,
    gd{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.gla;
        }
    }
    ,
    gl{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.glg;
        }
    }
    ,
    gn{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.grn;
        }
    }
    ,
    gu{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.guj;
        }
    }
    ,
    gv{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.glv;
        }
    }
    ,
    ha{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.hau;
        }
    }
    ,
    he{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.heb;
        }
    }
    ,
    hi{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.hin;
        }
    }
    ,
    ho{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.hmo;
        }
    }
    ,
    hr{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.hrv;
        }
    }
    ,
    ht{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.hat;
        }
    }
    ,
    hu{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.hun;
        }
    }
    ,
    hy{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.hye;
        }
    }
    ,
    hz{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.her;
        }
    }
    ,
    ia{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ina;
        }
    }
    ,
    id{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ind;
        }
    }
    ,
    ie{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ile;
        }
    }
    ,
    ig{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ibo;
        }
    }
    ,
    ii{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.iii;
        }
    }
    ,
    ik{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ipk;
        }
    }
    ,
    io{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ido;
        }
    }
    ,
    is{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.isl;
        }
    }
    ,
    it{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ita;
        }

        public Locale toLocale() {
            return Locale.ITALIAN;
        }
    }
    ,
    iu{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.iku;
        }
    }
    ,
    ja{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.jpn;
        }

        public Locale toLocale() {
            return Locale.JAPANESE;
        }
    }
    ,
    jv{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.jav;
        }
    }
    ,
    ka{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kat;
        }
    }
    ,
    kg{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kon;
        }
    }
    ,
    ki{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kik;
        }
    }
    ,
    kj{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kua;
        }
    }
    ,
    kk{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kaz;
        }
    }
    ,
    kl{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kal;
        }
    }
    ,
    km{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.khm;
        }
    }
    ,
    kn{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kan;
        }
    }
    ,
    ko{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kor;
        }

        public Locale toLocale() {
            return Locale.KOREAN;
        }
    }
    ,
    kr{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kau;
        }
    }
    ,
    ks{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kas;
        }
    }
    ,
    ku{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kur;
        }
    }
    ,
    kv{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kom;
        }
    }
    ,
    kw{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.cor;
        }
    }
    ,
    ky{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kir;
        }
    }
    ,
    la{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.lat;
        }
    }
    ,
    lb{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ltz;
        }
    }
    ,
    lg{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.lug;
        }
    }
    ,
    li{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.lim;
        }
    }
    ,
    ln{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.lin;
        }
    }
    ,
    lo{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.lao;
        }
    }
    ,
    lt{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.lit;
        }
    }
    ,
    lu{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.lub;
        }
    }
    ,
    lv{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.lav;
        }
    }
    ,
    mg{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.mlg;
        }
    }
    ,
    mh{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.mah;
        }
    }
    ,
    mi{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.mri;
        }
    }
    ,
    mk{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.mkd;
        }
    }
    ,
    ml{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.mal;
        }
    }
    ,
    mn{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.mon;
        }
    }
    ,
    mr{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.mar;
        }
    }
    ,
    ms{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.msa;
        }
    }
    ,
    mt{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.mlt;
        }
    }
    ,
    my{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.mya;
        }
    }
    ,
    na{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nau;
        }
    }
    ,
    nb{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nob;
        }
    }
    ,
    nd{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nde;
        }
    }
    ,
    ne{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nep;
        }
    }
    ,
    ng{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ndo;
        }
    }
    ,
    nl{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nld;
        }
    }
    ,
    nn{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nno;
        }
    }
    ,
    no{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nor;
        }
    }
    ,
    nr{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nbl;
        }
    }
    ,
    nv{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nav;
        }
    }
    ,
    ny{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nya;
        }
    }
    ,
    oc{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.oci;
        }
    }
    ,
    oj{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.oji;
        }
    }
    ,
    om{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.orm;
        }
    }
    ,
    or{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ori;
        }
    }
    ,
    os{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.oss;
        }
    }
    ,
    pa{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.pan;
        }
    }
    ,
    pi{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.pli;
        }
    }
    ,
    pl{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.pol;
        }
    }
    ,
    ps{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.pus;
        }
    }
    ,
    pt{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.por;
        }
    }
    ,
    qu{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.que;
        }
    }
    ,
    rm{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.roh;
        }
    }
    ,
    rn{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.run;
        }
    }
    ,
    ro{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ron;
        }
    }
    ,
    ru{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.rus;
        }
    }
    ,
    rw{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kin;
        }
    }
    ,
    sa{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.san;
        }
    }
    ,
    sc{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.srd;
        }
    }
    ,
    sd{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.snd;
        }
    }
    ,
    se{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.sme;
        }
    }
    ,
    sg{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.sag;
        }
    }
    ,
    si{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.sin;
        }
    }
    ,
    sk{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.slk;
        }
    }
    ,
    sl{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.slv;
        }
    }
    ,
    sm{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.smo;
        }
    }
    ,
    sn{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.sna;
        }
    }
    ,
    so{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.som;
        }
    }
    ,
    sq{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.sqi;
        }
    }
    ,
    sr{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.srp;
        }
    }
    ,
    ss{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ssw;
        }
    }
    ,
    st{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.sot;
        }
    }
    ,
    su{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.sun;
        }
    }
    ,
    sv{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.swe;
        }
    }
    ,
    sw{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.swa;
        }
    }
    ,
    ta{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tam;
        }
    }
    ,
    te{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tel;
        }
    }
    ,
    tg{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tgk;
        }
    }
    ,
    th{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tha;
        }
    }
    ,
    ti{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tir;
        }
    }
    ,
    tk{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tuk;
        }
    }
    ,
    tl{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tgl;
        }
    }
    ,
    tn{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tsn;
        }
    }
    ,
    to{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ton;
        }
    }
    ,
    tr{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tur;
        }
    }
    ,
    ts{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tso;
        }
    }
    ,
    tt{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tat;
        }
    }
    ,
    tw{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.twi;
        }
    }
    ,
    ty{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tah;
        }
    }
    ,
    ug{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.uig;
        }
    }
    ,
    uk{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ukr;
        }
    }
    ,
    ur{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.urd;
        }
    }
    ,
    uz{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.uzb;
        }
    }
    ,
    ve{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ven;
        }
    }
    ,
    vi{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.vie;
        }
    }
    ,
    vo{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.vol;
        }
    }
    ,
    wa{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.wln;
        }
    }
    ,
    wo{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.wol;
        }
    }
    ,
    xh{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.xho;
        }
    }
    ,
    yi{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.yid;
        }
    }
    ,
    yo{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.yor;
        }
    }
    ,
    za{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.zha;
        }
    }
    ,
    zh{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.zho;
        }

        public Locale toLocale() {
            return Locale.CHINESE;
        }
    }
    ,
    zu{

        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.zul;
        }
    };


    public String getName() {
        return this.getAlpha3().getName();
    }

    public Locale toLocale() {
        return new Locale(this.name());
    }

    public LanguageAlpha3Code getAlpha3() {
        return null;
    }

    public static LanguageCode getByCode(String code) {
        return LanguageCode.getByCode(code, true);
    }

    public static LanguageCode getByCodeIgnoreCase(String code) {
        return LanguageCode.getByCode(code, false);
    }

    public static LanguageCode getByCode(String code, boolean caseSensitive) {
        if ((code = LanguageCode.canonicalize(code, caseSensitive)) == null) {
            return null;
        }
        switch (code.length()) {
            case 2: 
            case 9: {
                return LanguageCode.getByEnumName(code);
            }
            case 3: {
                break;
            }
            default: {
                return null;
            }
        }
        LanguageAlpha3Code alpha3 = LanguageAlpha3Code.getByEnumName(code);
        if (alpha3 == null) {
            return null;
        }
        return alpha3.getAlpha2();
    }

    static LanguageCode getByEnumName(String name) {
        try {
            return Enum.valueOf(LanguageCode.class, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static LanguageCode getByLocale(Locale locale) {
        if (locale == null) {
            return null;
        }
        String language = locale.getLanguage();
        if (language == null || language.length() == 0) {
            return undefined;
        }
        return LanguageCode.getByCode(language, true);
    }

    static String canonicalize(String code, boolean caseSensitive) {
        if (code == null || code.length() == 0) {
            return null;
        }
        String[] legacy = new String[]{"iw", "ji", "in"};
        String[] official = new String[]{"he", "yi", "id"};
        for (int i = 0; i < legacy.length; ++i) {
            if (!(caseSensitive ? code.equals(legacy[i]) : code.equalsIgnoreCase(legacy[i]))) continue;
            return official[i];
        }
        if (caseSensitive) {
            return code;
        }
        return code.toLowerCase();
    }

    public static List<LanguageCode> findByName(String regex) {
        if (regex == null) {
            throw new IllegalArgumentException("regex is null.");
        }
        Pattern pattern = Pattern.compile(regex);
        return LanguageCode.findByName(pattern);
    }

    public static List<LanguageCode> findByName(Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern is null.");
        }
        ArrayList<LanguageCode> list = new ArrayList<LanguageCode>();
        for (LanguageCode entry : LanguageCode.values()) {
            if (!pattern.matcher(entry.getName()).matches()) continue;
            list.add(entry);
        }
        return list;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.neovisionaries.i18n;

import com.neovisionaries.i18n.LanguageCode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public enum LanguageAlpha3Code {
    undefined("Undefined"){

        public LanguageCode getAlpha2() {
            return LanguageCode.undefined;
        }
    }
    ,
    aar("Afar"){

        public LanguageCode getAlpha2() {
            return LanguageCode.aa;
        }
    }
    ,
    aav("Austro-Asiatic languages"),
    abk("Abkhaz"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ab;
        }
    }
    ,
    ace("Achinese"),
    ach("Acoli"),
    ada("Adangme"),
    ady("Adyghe"),
    afa("Afro-Asiatic languages"),
    afh("Afrihili"),
    afr("Afrikaans"){

        public LanguageCode getAlpha2() {
            return LanguageCode.af;
        }
    }
    ,
    ain("Ainu (Japan)"),
    aka("Akan"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ak;
        }
    }
    ,
    akk("Akkadian"),
    alb("Albanian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.sq;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return sqi;
        }
    }
    ,
    ale("Aleut"),
    alg("Algonquian languages"),
    alt("Southern Altai"),
    alv("Atlantic-Congo languages"),
    amh("Amharic"){

        public LanguageCode getAlpha2() {
            return LanguageCode.am;
        }
    }
    ,
    ang("Old English"),
    anp("Angika"),
    apa("Apache languages"),
    aqa("Alacalufan languages"),
    aql("Algic languages"),
    ara("Arabic"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ar;
        }
    }
    ,
    arc("Official Aramaic"),
    arg("Aragonese"){

        public LanguageCode getAlpha2() {
            return LanguageCode.an;
        }
    }
    ,
    arm("Armenian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.hy;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return hye;
        }
    }
    ,
    arn("Mapudungun"),
    arp("Arapaho"),
    art("Artificial languages"),
    arw("Arawak"),
    asm("Assamese"){

        public LanguageCode getAlpha2() {
            return LanguageCode.as;
        }
    }
    ,
    ast("Asturian"),
    ath("Athapascan languages"),
    auf("Arauan languages"),
    aus("Australian languages"),
    ava("Avaric"){

        public LanguageCode getAlpha2() {
            return LanguageCode.av;
        }
    }
    ,
    ave("Avestan"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ae;
        }
    }
    ,
    awa("Awadhi"),
    awd("Arawakan languages"),
    aym("Aymara"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ay;
        }
    }
    ,
    azc("Uto-Aztecan languages"),
    aze("Azerbaijani"){

        public LanguageCode getAlpha2() {
            return LanguageCode.az;
        }
    }
    ,
    bad("Banda languages"),
    bai("Bamileke languages"),
    bak("Bashkir"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ba;
        }
    }
    ,
    bal("Baluchi"),
    bam("Bambara"){

        public LanguageCode getAlpha2() {
            return LanguageCode.bm;
        }
    }
    ,
    ban("Balinese"),
    baq("Basque"){

        public LanguageCode getAlpha2() {
            return LanguageCode.eu;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return eus;
        }
    }
    ,
    bas("Basa (Cameroon)"),
    bat("Baltic languages"),
    bej("Beja"),
    bel("Belarusian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.be;
        }
    }
    ,
    bem("Bemba (Zambia)"),
    ben("Bengali"){

        public LanguageCode getAlpha2() {
            return LanguageCode.bn;
        }
    }
    ,
    ber("Berber languages"),
    bho("Bhojpuri"),
    bih("Bihari languages"){

        public LanguageCode getAlpha2() {
            return LanguageCode.bh;
        }
    }
    ,
    bik("Bikol"),
    bin("Bini"),
    bis("Bislama"){

        public LanguageCode getAlpha2() {
            return LanguageCode.bi;
        }
    }
    ,
    bla("Siksika"),
    bnt("Bantu languages"),
    bod("Tibetan"){

        public LanguageCode getAlpha2() {
            return LanguageCode.bo;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return tib;
        }
    }
    ,
    bos("Bosnian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.bs;
        }
    }
    ,
    bra("Braj"),
    bre("Breton"){

        public LanguageCode getAlpha2() {
            return LanguageCode.br;
        }
    }
    ,
    btk("Batak languages"),
    bua("Buriat"),
    bug("Buginese"),
    bul("Bulgarian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.bg;
        }
    }
    ,
    bur("Burmese"){

        public LanguageCode getAlpha2() {
            return LanguageCode.my;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return mya;
        }
    }
    ,
    byn("Bilin"),
    cad("Caddo"),
    cai("Central American Indian languages"),
    car("Galibi Carib"),
    cat("Catalan"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ca;
        }
    }
    ,
    cau("Caucasian languages"),
    cba("Chibchan languages"),
    ccn("North Caucasian languages"),
    ccs("South Caucasian languages"),
    cdc("Chadic languages"),
    cdd("Caddoan languages"),
    ceb("Cebuano"),
    cel("Celtic languages"),
    ces("Czech"){

        public LanguageCode getAlpha2() {
            return LanguageCode.cs;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return cze;
        }
    }
    ,
    cha("Chamorro"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ch;
        }
    }
    ,
    chb("Chibcha"),
    che("Chechen"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ce;
        }
    }
    ,
    chg("Chagatai"),
    chi("Chinese"){

        public LanguageCode getAlpha2() {
            return LanguageCode.zh;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return zho;
        }
    }
    ,
    chk("Chuukese"),
    chm("Mari (Russia)"),
    chn("Chinook jargon"),
    cho("Choctaw"),
    chp("Chipewyan"),
    chr("Cherokee"),
    chu("Church Slavic"){

        public LanguageCode getAlpha2() {
            return LanguageCode.cu;
        }
    }
    ,
    chv("Chuvash"){

        public LanguageCode getAlpha2() {
            return LanguageCode.cv;
        }
    }
    ,
    chy("Cheyenne"),
    cmc("Chamic languages"),
    cop("Coptic"),
    cor("Comish"){

        public LanguageCode getAlpha2() {
            return LanguageCode.kw;
        }
    }
    ,
    cos("Corsican"){

        public LanguageCode getAlpha2() {
            return LanguageCode.co;
        }
    }
    ,
    cpe("English based Creoles and pidgins"),
    cpf("French-Based Creoles and pidgins"),
    cpp("Portuguese-Based Creoles and pidgins"),
    cre("Cree"){

        public LanguageCode getAlpha2() {
            return LanguageCode.cr;
        }
    }
    ,
    crh("Crimean Tatar"),
    crp("Creoles and pidgins"),
    csb("Kashubian"),
    csu("Central Sudanic languages"),
    cus("Cushitic languages"),
    cym("Welsh"){

        public LanguageCode getAlpha2() {
            return LanguageCode.cy;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return wel;
        }
    }
    ,
    cze("Czech"){

        public LanguageCode getAlpha2() {
            return LanguageCode.cs;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return ces;
        }
    }
    ,
    dak("Dakota"),
    dan("Danish"){

        public LanguageCode getAlpha2() {
            return LanguageCode.da;
        }
    }
    ,
    dar("Dargwa"),
    day("Land Dayak languages"),
    del("Delaware"),
    den("Slave (Athapascan)"),
    deu("German"){

        public LanguageCode getAlpha2() {
            return LanguageCode.de;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return ger;
        }
    }
    ,
    dgr("Dogrib"),
    din("Dinka"),
    div("Dhivehi"){

        public LanguageCode getAlpha2() {
            return LanguageCode.dv;
        }
    }
    ,
    dmn("Mande languages"),
    doi("Dogri"),
    dra("Dravidian languages"),
    dsb("Lower Sorbian"),
    dua("Duala"),
    dum("Middle Dutch"),
    dut("Dutch"){

        public LanguageCode getAlpha2() {
            return LanguageCode.nl;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return nld;
        }
    }
    ,
    dyu("Dyula"),
    dzo("Dzongkha"){

        public LanguageCode getAlpha2() {
            return LanguageCode.dz;
        }
    }
    ,
    efi("Efik"),
    egx("Egyptian languages"),
    egy("Egyptian (Ancient)"),
    eka("Ekajuk"),
    ell("Modern Greek"){

        public LanguageCode getAlpha2() {
            return LanguageCode.el;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return gre;
        }
    }
    ,
    elx("Elamite"),
    eng("English"){

        public LanguageCode getAlpha2() {
            return LanguageCode.en;
        }
    }
    ,
    enm("Middle English"),
    epo("Esperanto"){

        public LanguageCode getAlpha2() {
            return LanguageCode.eo;
        }
    }
    ,
    est("Estonian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.et;
        }
    }
    ,
    esx("Eskimo-Aleut languages"),
    euq("Basque"),
    eus("Basque (family)"){

        public LanguageCode getAlpha2() {
            return LanguageCode.eu;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return baq;
        }
    }
    ,
    ewe("Ewe"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ee;
        }
    }
    ,
    ewo("Ewondo"),
    fan("Fang (Equatorial Guinea)"),
    fao("Faroese"){

        public LanguageCode getAlpha2() {
            return LanguageCode.fo;
        }
    }
    ,
    fas("Persian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.fa;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return per;
        }
    }
    ,
    fat("Fanti"),
    fij("Fijian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.fj;
        }
    }
    ,
    fil("Filipino"),
    fin("Finnish"){

        public LanguageCode getAlpha2() {
            return LanguageCode.fi;
        }
    }
    ,
    fiu("Finno-Ugrian languages"),
    fon("Fon"),
    fox("Formosan languages"),
    fra("French"){

        public LanguageCode getAlpha2() {
            return LanguageCode.fr;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return fre;
        }
    }
    ,
    fre("French"){

        public LanguageCode getAlpha2() {
            return LanguageCode.fr;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return fra;
        }
    }
    ,
    frm("Middle French"),
    fro("Old French"),
    frr("Northern Frisian"),
    frs("Eastern Frisian"),
    fry("West Frisian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.fy;
        }
    }
    ,
    ful("Fula"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ff;
        }
    }
    ,
    fur("Friulian"),
    gaa("Ga"),
    gay("Gayo"),
    gba("Gbaya (Central African Republic)"),
    gem("Germanic languages"),
    geo("Georgian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ka;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return kat;
        }
    }
    ,
    ger("German"){

        public LanguageCode getAlpha2() {
            return LanguageCode.de;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return deu;
        }
    }
    ,
    gez("Geez"),
    gil("Gilbertese"),
    gla("Scottish Gaelic"){

        public LanguageCode getAlpha2() {
            return LanguageCode.gd;
        }
    }
    ,
    gle("Irish"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ga;
        }
    }
    ,
    glg("Galician"){

        public LanguageCode getAlpha2() {
            return LanguageCode.gl;
        }
    }
    ,
    glv("Manx"){

        public LanguageCode getAlpha2() {
            return LanguageCode.gv;
        }
    }
    ,
    gme("East Germanic languages"),
    gmh("Middle High German"),
    gmq("North Germanic languages"),
    gmw("West Germanic languages"),
    goh("Old High German"),
    gon("Gondi"),
    gor("Gorontalo"),
    got("Gothic"),
    grb("Grebo"),
    grc("Ancient Greek"),
    gre("Modern Greek"){

        public LanguageCode getAlpha2() {
            return LanguageCode.el;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return ell;
        }
    }
    ,
    grk("Greek languages"),
    grn("Guaran\u00ed"){

        public LanguageCode getAlpha2() {
            return LanguageCode.gn;
        }
    }
    ,
    gsw("Swiss German"),
    guj("Gujarati"){

        public LanguageCode getAlpha2() {
            return LanguageCode.gu;
        }
    }
    ,
    gwi("Gwich\u02bcin"),
    hai("Haida"),
    hat("Haitian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ht;
        }
    }
    ,
    hau("Hausa"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ha;
        }
    }
    ,
    haw("Hawaiian"),
    heb("Hebrew"){

        public LanguageCode getAlpha2() {
            return LanguageCode.he;
        }
    }
    ,
    her("Herero"){

        public LanguageCode getAlpha2() {
            return LanguageCode.hz;
        }
    }
    ,
    hil("Hiligaynon"),
    him("Himachali languages"),
    hin("Hindi"){

        public LanguageCode getAlpha2() {
            return LanguageCode.hi;
        }
    }
    ,
    hit("Hittite"),
    hmn("Hmong"),
    hmo("Hiri Motu"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ho;
        }
    }
    ,
    hmx("Hmong-Mien languages"),
    hok("Hokan languages"),
    hrv("Croatian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.hr;
        }
    }
    ,
    hsb("Upper Sorbian"),
    hun("Hungarian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.hu;
        }
    }
    ,
    hup("Hupa"),
    hye("Armenian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.hy;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return arm;
        }
    }
    ,
    hyx("Armenian (family)"),
    iba("Iban"),
    ibo("Igbo"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ig;
        }
    }
    ,
    ice("Icelandic"){

        public LanguageCode getAlpha2() {
            return LanguageCode.is;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return isl;
        }
    }
    ,
    ido("Ido"){

        public LanguageCode getAlpha2() {
            return LanguageCode.io;
        }
    }
    ,
    iii("Nuosu"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ii;
        }
    }
    ,
    iir("Indo-Iranian languages"),
    ijo("Ijo languages"),
    iku("Inuktitut"){

        public LanguageCode getAlpha2() {
            return LanguageCode.iu;
        }
    }
    ,
    ile("Interlingue"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ie;
        }
    }
    ,
    ilo("Iloko"),
    ina("Interlingua"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ia;
        }
    }
    ,
    inc("Interlingua (International Auxiliary Language Association)"),
    ind("Indonesian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.id;
        }
    }
    ,
    ine("Indo-European languages"),
    inh("Ingush"),
    ipk("Inupiaq"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ik;
        }
    }
    ,
    ira("Iranian languages"),
    iro("Iroquoian languages"),
    isl("Icelandic"){

        public LanguageCode getAlpha2() {
            return LanguageCode.is;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return ice;
        }
    }
    ,
    ita("Italian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.it;
        }
    }
    ,
    itc("Italic languages"),
    jav("Javanese"){

        public LanguageCode getAlpha2() {
            return LanguageCode.jv;
        }
    }
    ,
    jbo("Lojban"),
    jpn("Japanese"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ja;
        }
    }
    ,
    jpr("Judeo-Persian"),
    jpx("Japanese (family)"),
    jrb("Judeo-Arabic"),
    kaa("Kara-Kalpak"),
    kab("Kabyle"),
    kac("Kachin"),
    kal("Kalaallisut"){

        public LanguageCode getAlpha2() {
            return LanguageCode.kl;
        }
    }
    ,
    kam("Kamba (Kenya)"),
    kan("Kannada"){

        public LanguageCode getAlpha2() {
            return LanguageCode.kn;
        }
    }
    ,
    kar("Karen languages"),
    kas("Kashmiri"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ks;
        }
    }
    ,
    kat("Georgian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ka;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return geo;
        }
    }
    ,
    kau("Kanuri"){

        public LanguageCode getAlpha2() {
            return LanguageCode.kr;
        }
    }
    ,
    kaw("Kawi"),
    kaz("Kazakh"){

        public LanguageCode getAlpha2() {
            return LanguageCode.kk;
        }
    }
    ,
    kbd("Kabardian"),
    kdo("Kordofanian languages"),
    kha("Khasi"),
    khi("Khoisan languages"),
    khm("Central Khmer"){

        public LanguageCode getAlpha2() {
            return LanguageCode.km;
        }
    }
    ,
    kho("Khotanese"),
    kik("Kikuyu"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ki;
        }
    }
    ,
    kin("Kinyarwanda"){

        public LanguageCode getAlpha2() {
            return LanguageCode.rw;
        }
    }
    ,
    kir("Kirghiz"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ky;
        }
    }
    ,
    kmb("Kimbundu"),
    kok("Konkani"),
    kom("Komi"){

        public LanguageCode getAlpha2() {
            return LanguageCode.kv;
        }
    }
    ,
    kon("Kongo"){

        public LanguageCode getAlpha2() {
            return LanguageCode.kg;
        }
    }
    ,
    kor("Korean"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ko;
        }
    }
    ,
    kos("Kosraean"),
    kpe("Kpelle"),
    krc("Karachay-Balkar"),
    krl("Karelian"),
    kro("Kru languages"),
    kru("Kurukh"),
    kua("Kuanyama"){

        public LanguageCode getAlpha2() {
            return LanguageCode.kj;
        }
    }
    ,
    kum("Kumyk"),
    kur("Kurdish"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ku;
        }
    }
    ,
    kut("Kutenai"),
    lad("Ladino"),
    lah("Lahnda"),
    lam("Lamba"),
    lao("Lao"){

        public LanguageCode getAlpha2() {
            return LanguageCode.lo;
        }
    }
    ,
    lat("Latin"){

        public LanguageCode getAlpha2() {
            return LanguageCode.la;
        }
    }
    ,
    lav("Latvian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.lv;
        }
    }
    ,
    lez("Lezghian"),
    lim("Limburgan"){

        public LanguageCode getAlpha2() {
            return LanguageCode.li;
        }
    }
    ,
    lin("Lingala"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ln;
        }
    }
    ,
    lit("Lithuanian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.lt;
        }
    }
    ,
    lol("Mongo"),
    loz("Lozi"),
    ltz("Luxembourgish"){

        public LanguageCode getAlpha2() {
            return LanguageCode.lb;
        }
    }
    ,
    lua("Luba-Lulua"),
    lub("Luba-Katanga"){

        public LanguageCode getAlpha2() {
            return LanguageCode.lu;
        }
    }
    ,
    lug("Ganda"){

        public LanguageCode getAlpha2() {
            return LanguageCode.lg;
        }
    }
    ,
    lui("Luiseno"),
    lun("Lunda"),
    luo("Luo (Kenya and Tanzania)"),
    lus("Lushai"),
    mac("Macedonian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.mk;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return mkd;
        }
    }
    ,
    mad("Madurese"),
    mag("Magahi"),
    mah("Marshallese"){

        public LanguageCode getAlpha2() {
            return LanguageCode.mh;
        }
    }
    ,
    mai("Maithili"),
    mak("Makasar"),
    mal("Malayalam"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ml;
        }
    }
    ,
    man("Mandingo"),
    mao("M\u0101ori"){

        public LanguageCode getAlpha2() {
            return LanguageCode.mi;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return mri;
        }
    }
    ,
    map("Austronesian languages"),
    mar("Marathi"){

        public LanguageCode getAlpha2() {
            return LanguageCode.mr;
        }
    }
    ,
    mas("Masai"),
    may("Malay"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ms;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return msa;
        }
    }
    ,
    mdf("Moksha"),
    mdr("Mandar"),
    men("Mende (Sierra Leone)"),
    mga("Middle Irish"),
    mic("Mi'kmaq"),
    min("Minangkabau"),
    mis("Uncoded languages"),
    mkd("Macedonian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.mk;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return mac;
        }
    }
    ,
    mkh("Mon-Khmer languages"),
    mlg("Malagasy"){

        public LanguageCode getAlpha2() {
            return LanguageCode.mg;
        }
    }
    ,
    mlt("Maltese"){

        public LanguageCode getAlpha2() {
            return LanguageCode.mt;
        }
    }
    ,
    mnc("Manchu"),
    mni("Manipuri"),
    mno("Manobo languages"),
    moh("Mohawk"),
    mon("Mongolian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.mn;
        }
    }
    ,
    mos("Mossi"),
    mri("M\u0101ori"){

        public LanguageCode getAlpha2() {
            return LanguageCode.mi;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return mao;
        }
    }
    ,
    msa("Malay"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ms;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return may;
        }
    }
    ,
    mul("Multiple languages"),
    mun("Munda languages"),
    mus("Creek"),
    mwl("Mirandese"),
    mwr("Marwari"),
    mya("Burmese"){

        public LanguageCode getAlpha2() {
            return LanguageCode.my;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return bur;
        }
    }
    ,
    myn("Mayan languages"),
    myv("Erzya"),
    nah("Nahuatl languages"),
    nai("North American Indian"),
    nap("Neapolitan"),
    nau("Nauru"){

        public LanguageCode getAlpha2() {
            return LanguageCode.na;
        }
    }
    ,
    nav("Navajo"){

        public LanguageCode getAlpha2() {
            return LanguageCode.nv;
        }
    }
    ,
    nbl("South Ndebele"){

        public LanguageCode getAlpha2() {
            return LanguageCode.nr;
        }
    }
    ,
    nde("North Ndebele"){

        public LanguageCode getAlpha2() {
            return LanguageCode.nd;
        }
    }
    ,
    ndo("Ndonga"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ng;
        }
    }
    ,
    nds("Low German"),
    nep("Nepali"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ne;
        }
    }
    ,
    New("Newari"){

        public String toString() {
            return "new";
        }
    }
    ,
    ngf("Trans-New Guinea languages"),
    nia("Nias"),
    nic("Niger-Kordofanian languages"),
    niu("Niuean"),
    nld("Dutch"){

        public LanguageCode getAlpha2() {
            return LanguageCode.nl;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return dut;
        }
    }
    ,
    nno("Norwegian Nynorsk"){

        public LanguageCode getAlpha2() {
            return LanguageCode.nn;
        }
    }
    ,
    nob("Norwegian Bokm\u00e5l"){

        public LanguageCode getAlpha2() {
            return LanguageCode.nb;
        }
    }
    ,
    nog("Nogai"),
    non("Old Norse"),
    nor("Norwegian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.no;
        }
    }
    ,
    nqo("N'Ko"),
    nso("Pedi"),
    nub("Nubian languages"),
    nwc("Classical Newari"),
    nya("Nyanja"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ny;
        }
    }
    ,
    nym("Nyamwezi"),
    nyn("Nyankole"),
    nyo("Nyoro"),
    nzi("Nzima"),
    oci("Occitan"){

        public LanguageCode getAlpha2() {
            return LanguageCode.oc;
        }
    }
    ,
    oji("Ojibwa"){

        public LanguageCode getAlpha2() {
            return LanguageCode.oj;
        }
    }
    ,
    omq("Oto-Manguean languages"),
    omv("Omotic languages"),
    ori("Oriya"){

        public LanguageCode getAlpha2() {
            return LanguageCode.or;
        }
    }
    ,
    orm("Oromo"){

        public LanguageCode getAlpha2() {
            return LanguageCode.om;
        }
    }
    ,
    osa("Osage"),
    oss("Ossetian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.os;
        }
    }
    ,
    ota("Ottoman Turkish"),
    oto("Otomian languages"),
    paa("Papuan languages"),
    pag("Pangasinan"),
    pal("Pahlavi"),
    pam("Pampanga"),
    pan("Panjabi"){

        public LanguageCode getAlpha2() {
            return LanguageCode.pa;
        }
    }
    ,
    pap("Papiamento"),
    pau("Palauan"),
    peo("Old Persian"),
    per("Persian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.fa;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return fas;
        }
    }
    ,
    phi("Philippine languages"),
    phn("Phoenician"),
    plf("Central Malayo-Polynesian languages"),
    pli("P\u0101li"){

        public LanguageCode getAlpha2() {
            return LanguageCode.pi;
        }
    }
    ,
    pol("Polish"){

        public LanguageCode getAlpha2() {
            return LanguageCode.pl;
        }
    }
    ,
    pon("Pohnpeian"),
    por("Portuguese"){

        public LanguageCode getAlpha2() {
            return LanguageCode.pt;
        }
    }
    ,
    poz("Malayo-Polynesian languages"),
    pqe("Eastern Malayo-Polynesian languages"),
    pqw("Western Malayo-Polynesian languages"),
    pra("Prakrit languages"),
    pro("Old Proven\u00e7al"),
    pus("Pushto"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ps;
        }
    }
    ,
    que("Quechua"){

        public LanguageCode getAlpha2() {
            return LanguageCode.qu;
        }
    }
    ,
    qwe("Quechuan (family)"),
    raj("Rajasthani"),
    rap("Rapanui"),
    rar("Rarotongan"),
    roa("Romance languages"),
    roh("Romansh"){

        public LanguageCode getAlpha2() {
            return LanguageCode.rm;
        }
    }
    ,
    rom("Romany"),
    ron("Romanian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ro;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return rum;
        }
    }
    ,
    rum("Romansh"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ro;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return ron;
        }
    }
    ,
    run("Kirundi"){

        public LanguageCode getAlpha2() {
            return LanguageCode.rn;
        }
    }
    ,
    rup("Macedo-Romanian"),
    rus("Russian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ru;
        }
    }
    ,
    sad("Sango"),
    sag("Sango"){

        public LanguageCode getAlpha2() {
            return LanguageCode.sg;
        }
    }
    ,
    sah("Yakut"),
    sai("South American Indian languages"),
    sal("Salishan languages"),
    sam("Samaritan Aramaic"),
    san("Sanskrit"){

        public LanguageCode getAlpha2() {
            return LanguageCode.sa;
        }
    }
    ,
    sas("Sasak"),
    sat("Santali"),
    scn("Sicilian"),
    sco("Scots"),
    sdv("Eastern Sudanic languages"),
    sel("Selkup"),
    sem("Semitic languages"),
    sga("Old Irish"),
    sgn("Sign languages"),
    shn("Shan"),
    sid("Sidamo"),
    sin("Sinhala"){

        public LanguageCode getAlpha2() {
            return LanguageCode.si;
        }
    }
    ,
    sio("Siouan languages"),
    sit("Sino-Tibetan languages"),
    sla("Slavic languages"),
    slk("Slovak"){

        public LanguageCode getAlpha2() {
            return LanguageCode.sk;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return slo;
        }
    }
    ,
    slo("Slovak"){

        public LanguageCode getAlpha2() {
            return LanguageCode.sk;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return slk;
        }
    }
    ,
    slv("Slovene"){

        public LanguageCode getAlpha2() {
            return LanguageCode.sl;
        }
    }
    ,
    sma("Southern Sami"),
    sme("Northern Sami"){

        public LanguageCode getAlpha2() {
            return LanguageCode.se;
        }
    }
    ,
    smi("Sami languages"),
    smj("Lule Sami"),
    smn("Inari Sami"),
    smo("Samoan"){

        public LanguageCode getAlpha2() {
            return LanguageCode.sm;
        }
    }
    ,
    sms("Skolt Sami"),
    sna("Shona"){

        public LanguageCode getAlpha2() {
            return LanguageCode.sn;
        }
    }
    ,
    snd("Sindhi"){

        public LanguageCode getAlpha2() {
            return LanguageCode.sd;
        }
    }
    ,
    snk("Soninke"),
    sog("Sogdian"),
    som("Somali"){

        public LanguageCode getAlpha2() {
            return LanguageCode.so;
        }
    }
    ,
    son("Songhai languages"),
    sot("Southern Sotho"){

        public LanguageCode getAlpha2() {
            return LanguageCode.st;
        }
    }
    ,
    spa("Spanish"){

        public LanguageCode getAlpha2() {
            return LanguageCode.es;
        }
    }
    ,
    sqi("Albanian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.sq;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return alb;
        }
    }
    ,
    sqj("Albanian languages"),
    srd("Sardinian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.sc;
        }
    }
    ,
    srn("Sranan Tongo"),
    srp("Serbian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.sr;
        }
    }
    ,
    srr("Serer"),
    ssa("Nilo-Saharan languages"),
    ssw("Swati"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ss;
        }
    }
    ,
    suk("Sukuma"),
    sun("Sundanese"){

        public LanguageCode getAlpha2() {
            return LanguageCode.su;
        }
    }
    ,
    sus("Susu"),
    sux("Sumerian"),
    swa("Swahili"){

        public LanguageCode getAlpha2() {
            return LanguageCode.sw;
        }
    }
    ,
    swe("Swedish"){

        public LanguageCode getAlpha2() {
            return LanguageCode.sv;
        }
    }
    ,
    syc("Classical Syriac"),
    syd("Samoyedic languages"),
    syr("Syriac"),
    tah("Tahitian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ty;
        }
    }
    ,
    tai("Tai languages"),
    tam("Tamil"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ta;
        }
    }
    ,
    tat("Tatar"){

        public LanguageCode getAlpha2() {
            return LanguageCode.tt;
        }
    }
    ,
    tbq("Tibeto-Burman languages"),
    tel("Telugu"){

        public LanguageCode getAlpha2() {
            return LanguageCode.te;
        }
    }
    ,
    tem("Timne"),
    ter("Tereno"),
    tet("Tetum"),
    tgk("Tajik"){

        public LanguageCode getAlpha2() {
            return LanguageCode.tg;
        }
    }
    ,
    tgl("Tagalog"){

        public LanguageCode getAlpha2() {
            return LanguageCode.tl;
        }
    }
    ,
    tha("Thai"){

        public LanguageCode getAlpha2() {
            return LanguageCode.th;
        }
    }
    ,
    tib("Tibetan"){

        public LanguageCode getAlpha2() {
            return LanguageCode.bo;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return bod;
        }
    }
    ,
    tig("Tigre"),
    tir("Tigrinya"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ti;
        }
    }
    ,
    tiv("Tiv"),
    tkl("Tokelau"),
    tlh("Klingon"),
    tli("Tlingit"),
    tmh("Tamashek"),
    tog("Tonga (Nyasa)"),
    ton("Tonga (Tonga Islands)"){

        public LanguageCode getAlpha2() {
            return LanguageCode.to;
        }
    }
    ,
    tpi("Tok Pisin"),
    trk("Turkic languages"),
    tsi("Tsimshian"),
    tsn("Tswana"){

        public LanguageCode getAlpha2() {
            return LanguageCode.tn;
        }
    }
    ,
    tso("Tsonga"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ts;
        }
    }
    ,
    tuk("Turkmen"){

        public LanguageCode getAlpha2() {
            return LanguageCode.tk;
        }
    }
    ,
    tum("Tumbuka"),
    tup("Tupi languages"),
    tur("Turkish"){

        public LanguageCode getAlpha2() {
            return LanguageCode.tr;
        }
    }
    ,
    tut("Altaic languages"),
    tuw("Tungus languages"),
    tvl("Tuvalu"),
    twi("Twi"){

        public LanguageCode getAlpha2() {
            return LanguageCode.tw;
        }
    }
    ,
    tyv("Tuvinian"),
    udm("Udmurt"),
    uga("Ugaritic"),
    uig("Uighur"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ug;
        }
    }
    ,
    ukr("Ukrainian"){

        public LanguageCode getAlpha2() {
            return LanguageCode.uk;
        }
    }
    ,
    umb("Umbundu"),
    und("Undetermined"),
    urd("Urdu"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ur;
        }
    }
    ,
    urj("Uralic languages"),
    uzb("Uzbek"){

        public LanguageCode getAlpha2() {
            return LanguageCode.uz;
        }
    }
    ,
    vai("Vai"),
    ven("Venda"){

        public LanguageCode getAlpha2() {
            return LanguageCode.ve;
        }
    }
    ,
    vie("Vietnamese"){

        public LanguageCode getAlpha2() {
            return LanguageCode.vi;
        }
    }
    ,
    vol("Volap\u00fck"){

        public LanguageCode getAlpha2() {
            return LanguageCode.vo;
        }
    }
    ,
    vot("Votic"),
    wak("Wakashan languages"),
    wal("Wolaytta"),
    war("Waray (Philippines)"),
    was("Washo"),
    wel("Welsh"){

        public LanguageCode getAlpha2() {
            return LanguageCode.cy;
        }

        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }

        public LanguageAlpha3Code getSynonym() {
            return cym;
        }
    }
    ,
    wen("Sorbian languages"),
    wln("Walloon"){

        public LanguageCode getAlpha2() {
            return LanguageCode.wa;
        }
    }
    ,
    wol("Wolof"){

        public LanguageCode getAlpha2() {
            return LanguageCode.wo;
        }
    }
    ,
    xal("Kalmyk"),
    xgn("Mongolian languages"),
    xho("Xhosa"){

        public LanguageCode getAlpha2() {
            return LanguageCode.xh;
        }
    }
    ,
    xnd("Na-Dene languages"),
    yao("Yao"),
    yap("Yapese"),
    yid("Yiddish"){

        public LanguageCode getAlpha2() {
            return LanguageCode.yi;
        }
    }
    ,
    yor("Yoruba"){

        public LanguageCode getAlpha2() {
            return LanguageCode.yo;
        }
    }
    ,
    ypk("Yupik languages"),
    zap("Zapotec"),
    zbl("Blissymbols"),
    zen("Zenaga"),
    zha("Zhuang"){

        public LanguageCode getAlpha2() {
            return LanguageCode.za;
        }
    }
    ,
    zho("Chinese"){

        public LanguageCode getAlpha2() {
            return LanguageCode.zh;
        }

        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }

        public LanguageAlpha3Code getSynonym() {
            return chi;
        }
    }
    ,
    zhx("Chinese (family)"),
    zle("East Slavic languages"),
    zls("South Slavic languages"),
    zlw("West Slavic languages"),
    znd("Zande languages"),
    zul("Zulu"){

        public LanguageCode getAlpha2() {
            return LanguageCode.zu;
        }
    }
    ,
    zun("Zuni"),
    zxx("No linguistic content"),
    zza("Zaza");

    private final String name;

    private LanguageAlpha3Code(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public LanguageCode getAlpha2() {
        return null;
    }

    public Usage getUsage() {
        return Usage.COMMON;
    }

    public LanguageAlpha3Code getSynonym() {
        return this;
    }

    public LanguageAlpha3Code getAlpha3B() {
        if (this.getUsage() == Usage.BIBLIOGRAPHY) {
            return this;
        }
        return this.getSynonym();
    }

    public LanguageAlpha3Code getAlpha3T() {
        if (this.getUsage() == Usage.TERMINOLOGY) {
            return this;
        }
        return this.getSynonym();
    }

    public static LanguageAlpha3Code getByCode(String code) {
        return LanguageAlpha3Code.getByCode(code, true);
    }

    public static LanguageAlpha3Code getByCodeIgnoreCase(String code) {
        return LanguageAlpha3Code.getByCode(code, false);
    }

    public static LanguageAlpha3Code getByCode(String code, boolean caseSensitive) {
        if ((code = LanguageAlpha3Code.canonicalize(code, caseSensitive)) == null) {
            return null;
        }
        switch (code.length()) {
            case 2: {
                break;
            }
            case 3: 
            case 9: {
                return LanguageAlpha3Code.getByEnumName(code);
            }
            default: {
                return null;
            }
        }
        code = LanguageCode.canonicalize(code, caseSensitive);
        LanguageCode alpha2 = LanguageCode.getByEnumName(code);
        if (alpha2 == null) {
            return null;
        }
        return alpha2.getAlpha3();
    }

    static LanguageAlpha3Code getByEnumName(String name) {
        try {
            return Enum.valueOf(LanguageAlpha3Code.class, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String canonicalize(String code, boolean caseSensitive) {
        if (code == null || code.length() == 0) {
            return null;
        }
        if (!caseSensitive) {
            code = code.toLowerCase();
        }
        if (code.equals("new")) {
            code = "New";
        }
        return code;
    }

    public static List<LanguageAlpha3Code> findByName(String regex) {
        if (regex == null) {
            throw new IllegalArgumentException("regex is null.");
        }
        Pattern pattern = Pattern.compile(regex);
        return LanguageAlpha3Code.findByName(pattern);
    }

    public static List<LanguageAlpha3Code> findByName(Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern is null.");
        }
        ArrayList<LanguageAlpha3Code> list = new ArrayList<LanguageAlpha3Code>();
        for (LanguageAlpha3Code entry : LanguageAlpha3Code.values()) {
            if (!pattern.matcher(entry.getName()).matches()) continue;
            list.add(entry);
        }
        return list;
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum Usage {
        TERMINOLOGY,
        BIBLIOGRAPHY,
        COMMON;

    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.neovisionaries.i18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public enum ScriptCode {
    Undefined(-1, "Undefined"),
    Afak(439, "Afaka"),
    Aghb(239, "Caucasian Albanian"),
    Arab(160, "Arabic"),
    Armi(124, "Imperial Aramaic"),
    Armn(230, "Armenian"),
    Avst(134, "Avestan"),
    Bali(360, "Balinese"),
    Bamu(435, "Bamum"),
    Bass(259, "Bassa Vah"),
    Batk(365, "Batak"),
    Beng(325, "Bengali"),
    Blis(550, "Blissymbols"),
    Bopo(285, "Bopomofo"),
    Brah(300, "Brahmi"),
    Brai(570, "Braille"),
    Bugi(367, "Buginese"),
    Buhd(372, "Buhid"),
    Cakm(349, "Chakma"),
    Cans(440, "Unified Canadian Aboriginal Syllabics"),
    Cari(201, "Carian"),
    Cham(358, "Cham"),
    Cher(445, "Cherokee"),
    Cirt(291, "Cirth"),
    Copt(204, "Coptic"),
    Cprt(403, "Cypriot"),
    Cyrl(220, "Cyrillic"),
    Cyrs(221, "Cyrillic"),
    Deva(315, "Devanagari"),
    Dsrt(250, "Deseret"),
    Dupl(755, "Duployan shorthand, Duployan stenography"),
    Egyd(56, "Egyptian demotic"),
    Egyh(48, "Egyptian hieratic"),
    Egyp(40, "Egyptian hieroglyphs"),
    Elba(226, "Elbasan"),
    Ethi(430, "Ethiopic"),
    Geor(240, "Georgian"),
    Geok(241, "Khutsuri"),
    Glag(225, "Glagolitic"),
    Goth(206, "Gothic"),
    Gran(343, "Grantha"),
    Grek(200, "Greek"),
    Gujr(320, "Gujarati"),
    Guru(310, "Gurmukhi"),
    Hang(286, "Hangul"),
    Hani(500, "Han"),
    Hano(371, "Hanunoo"),
    Hans(501, "Han"),
    Hant(502, "Han"),
    Hebr(125, "Hebrew"),
    Hira(410, "Hiragana"),
    Hluw(80, "Anatolian Hieroglyphs"),
    Hmng(450, "Pahawh Hmong"),
    Hrkt(412, "Japanese syllabaries"),
    Hung(176, "Old Hungarian"),
    Inds(610, "Indus"),
    Ital(210, "Old Italic"),
    Java(361, "Javanese"),
    Jpan(413, "Japanese"),
    Jurc(510, "Jurchen"),
    Kali(357, "Kayah Li"),
    Kana(411, "Katakana"),
    Khar(305, "Kharoshthi"),
    Khmr(355, "Khmer"),
    Khoj(322, "Khojki"),
    Knda(345, "Kannada"),
    Kore(287, "Korean"),
    Kpel(436, "Kpelle"),
    Kthi(317, "Kaithi"),
    Lana(351, "Tai Tham"),
    Laoo(356, "Lao"),
    Latf(217, "Latin"),
    Latg(216, "Latin"),
    Latn(215, "Latin"),
    Lepc(335, "Lepcha"),
    Limb(336, "Limbu"),
    Lina(400, "Linear A"),
    Linb(401, "Linear B"),
    Lisu(399, "Lisu"),
    Loma(437, "Loma"),
    Lyci(202, "Lycian"),
    Lydi(116, "Lydian"),
    Mahj(314, "Mahajani"),
    Mand(140, "Mandaic, Mandaean"),
    Mani(139, "Manichaean"),
    Maya(90, "Mayan hieroglyphs"),
    Mend(438, "Mende"),
    Merc(101, "Meroitic Cursive"),
    Mero(100, "Meroitic Hieroglyphs"),
    Mlym(347, "Malayalam"),
    Moon(218, "Moon"),
    Mong(145, "Mongolian"),
    Mroo(199, "Mro, Mru"),
    Mtei(337, "Meitei Mayek"),
    Mymr(350, "Myanmar"),
    Narb(106, "Old North Arabian"),
    Nbat(159, "Nabataean"),
    Nkgb(420, "Nakhi Geba"),
    Nkoo(165, "N\u2019Ko"),
    Nshu(499, "Nushu"),
    Ogam(212, "Ogham"),
    Olck(261, "Ol Chiki"),
    Orkh(175, "Old Turkic, Orkhon Runic"),
    Orya(327, "Oriya"),
    Osma(260, "Osmanya"),
    Palm(126, "Palmyrene"),
    Perm(227, "Old Permic"),
    Phag(331, "Phags-pa"),
    Phli(131, "Inscriptional Pahlavi"),
    Phlp(132, "Psalter Pahlavi"),
    Phlv(133, "Book Pahlavi"),
    Phnx(115, "Phoenician"),
    Plrd(282, "Miao"),
    Prti(130, "Inscriptional Parthian"),
    Qaaa(900, "Reserved for private use"),
    Qabx(949, "Reserved for private use"),
    Rjng(363, "Rejang"),
    Roro(620, "Rongorongo"),
    Runr(211, "Runic"),
    Samr(123, "Samaritan"),
    Sara(292, "Sarati"),
    Sarb(105, "Old South Arabian"),
    Saur(344, "Saurashtra"),
    Sgnw(95, "SignWriting"),
    Shaw(281, "Shavian"),
    Shrd(319, "Sharada"),
    Sind(318, "Khudawadi, Sindhi"),
    Sinh(348, "Sinhala"),
    Sora(398, "Sora Sompeng"),
    Sund(362, "Sundanese"),
    Sylo(316, "Syloti Nagri"),
    Syrc(135, "Syriac"),
    Syre(138, "Syriac"),
    Syrj(137, "Syriac"),
    Syrn(136, "Syriac"),
    Tagb(373, "Tagbanwa"),
    Takr(321, "Takri"),
    Tale(353, "Tai Le"),
    Talu(354, "New Tai Lue"),
    Taml(346, "Tamil"),
    Tang(520, "Tangut"),
    Tavt(359, "Tai Viet"),
    Telu(340, "Telugu"),
    Teng(290, "Tengwar"),
    Tfng(120, "Tifinagh"),
    Tglg(370, "Tagalog"),
    Thaa(170, "Thaana"),
    Thai(352, "Thai"),
    Tibt(330, "Tibetan"),
    Tirh(326, "Tirhuta"),
    Ugar(32, "Ugaritic"),
    Vaii(470, "Vai"),
    Visp(280, "Visible Speech"),
    Wara(262, "Warang Citi"),
    Wole(480, "Woleai"),
    Xpeo(24, "Old Persian"),
    Xsux(16, "Cuneiform, Sumero-Akkadian"),
    Yiii(460, "Yi"),
    Zinh(994, "Code for inherited script"),
    Zmth(995, "Mathematical notation"),
    Zsym(996, "Symbols"),
    Zxxx(997, "Code for unwritten documents"),
    Zyyy(998, "Code for undetermined script"),
    Zzzz(999, "Code for uncoded script");

    private static final Map<Integer, ScriptCode> numericMap;
    private final int numeric;
    private final String name;

    private ScriptCode(int numeric, String name) {
        this.numeric = numeric;
        this.name = name;
    }

    public int getNumeric() {
        return this.numeric;
    }

    public String getName() {
        return this.name;
    }

    public static ScriptCode getByCode(String code) {
        return ScriptCode.getByCode(code, true);
    }

    public static ScriptCode getByCodeIgnoreCase(String code) {
        return ScriptCode.getByCode(code, false);
    }

    public static ScriptCode getByCode(String code, boolean caseSensitive) {
        if (code == null) {
            return null;
        }
        switch (code.length()) {
            case 4: 
            case 9: {
                break;
            }
            default: {
                return null;
            }
        }
        code = ScriptCode.canonicalize(code, caseSensitive);
        try {
            return Enum.valueOf(ScriptCode.class, code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static ScriptCode getByCode(int code) {
        if (code <= 0) {
            return null;
        }
        return numericMap.get(code);
    }

    private static String canonicalize(String code, boolean caseSensitive) {
        if (code == null || code.length() == 0) {
            return null;
        }
        if (caseSensitive) {
            return code;
        }
        StringBuilder sb = null;
        for (int i = 0; i < code.length(); ++i) {
            char ch = code.charAt(i);
            if (i == 0) {
                if (Character.isUpperCase(ch)) continue;
                sb = new StringBuilder();
                sb.append(Character.toUpperCase(ch));
                continue;
            }
            if (sb == null) {
                if (Character.isLowerCase(ch)) continue;
                sb = new StringBuilder();
                sb.append(code.substring(0, i));
                sb.append(Character.toLowerCase(ch));
                continue;
            }
            sb.append(Character.toLowerCase(ch));
        }
        if (sb == null) {
            return code;
        }
        return sb.toString();
    }

    public static List<ScriptCode> findByName(String regex) {
        if (regex == null) {
            throw new IllegalArgumentException("regex is null.");
        }
        Pattern pattern = Pattern.compile(regex);
        return ScriptCode.findByName(pattern);
    }

    public static List<ScriptCode> findByName(Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern is null.");
        }
        ArrayList<ScriptCode> list = new ArrayList<ScriptCode>();
        for (ScriptCode entry : ScriptCode.values()) {
            if (!pattern.matcher(entry.getName()).matches()) continue;
            list.add(entry);
        }
        return list;
    }

    static {
        numericMap = new HashMap<Integer, ScriptCode>();
        for (ScriptCode sc : ScriptCode.values()) {
            if (sc.getNumeric() == -1) continue;
            numericMap.put(sc.getNumeric(), sc);
        }
    }
}


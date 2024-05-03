/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.neovisionaries.i18n;

import com.neovisionaries.i18n.LocaleCode;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public enum CountryCode {
    UNDEFINED("Undefined", null, -1, Assignment.USER_ASSIGNED){

        public Locale toLocale() {
            return LocaleCode.undefined.toLocale();
        }
    }
    ,
    AC("Ascension Island", "ASC", -1, Assignment.EXCEPTIONALLY_RESERVED),
    AD("Andorra", "AND", 20, Assignment.OFFICIALLY_ASSIGNED),
    AE("United Arab Emirates", "ARE", 784, Assignment.OFFICIALLY_ASSIGNED),
    AF("Afghanistan", "AFG", 4, Assignment.OFFICIALLY_ASSIGNED),
    AG("Antigua and Barbuda", "ATG", 28, Assignment.OFFICIALLY_ASSIGNED),
    AI("Anguilla", "AIA", 660, Assignment.OFFICIALLY_ASSIGNED),
    AL("Albania", "ALB", 8, Assignment.OFFICIALLY_ASSIGNED),
    AM("Armenia", "ARM", 51, Assignment.OFFICIALLY_ASSIGNED),
    AN("Netherlands Antilles", "ANT", 530, Assignment.TRANSITIONALLY_RESERVED),
    AO("Angola", "AGO", 24, Assignment.OFFICIALLY_ASSIGNED),
    AQ("Antarctica", "ATA", 10, Assignment.OFFICIALLY_ASSIGNED),
    AR("Argentina", "ARG", 32, Assignment.OFFICIALLY_ASSIGNED),
    AS("American Samoa", "ASM", 16, Assignment.OFFICIALLY_ASSIGNED),
    AT("Austria", "AUT", 40, Assignment.OFFICIALLY_ASSIGNED),
    AU("Australia", "AUS", 36, Assignment.OFFICIALLY_ASSIGNED),
    AW("Aruba", "ABW", 533, Assignment.OFFICIALLY_ASSIGNED),
    AX("\u00c5land Islands", "ALA", 248, Assignment.OFFICIALLY_ASSIGNED),
    AZ("Azerbaijan", "AZE", 31, Assignment.OFFICIALLY_ASSIGNED),
    BA("Bosnia and Herzegovina", "BIH", 70, Assignment.OFFICIALLY_ASSIGNED),
    BB("Barbados", "BRB", 52, Assignment.OFFICIALLY_ASSIGNED),
    BD("Bangladesh", "BGD", 50, Assignment.OFFICIALLY_ASSIGNED),
    BE("Belgium", "BEL", 56, Assignment.OFFICIALLY_ASSIGNED),
    BF("Burkina Faso", "BFA", 854, Assignment.OFFICIALLY_ASSIGNED),
    BG("Bulgaria", "BGR", 100, Assignment.OFFICIALLY_ASSIGNED),
    BH("Bahrain", "BHR", 48, Assignment.OFFICIALLY_ASSIGNED),
    BI("Burundi", "BDI", 108, Assignment.OFFICIALLY_ASSIGNED),
    BJ("Benin", "BEN", 204, Assignment.OFFICIALLY_ASSIGNED),
    BL("Saint Barth\u00e9lemy", "BLM", 652, Assignment.OFFICIALLY_ASSIGNED),
    BM("Bermuda", "BMU", 60, Assignment.OFFICIALLY_ASSIGNED),
    BN("Brunei Darussalam", "BRN", 96, Assignment.OFFICIALLY_ASSIGNED),
    BO("Bolivia, Plurinational State of", "BOL", 68, Assignment.OFFICIALLY_ASSIGNED),
    BQ("Bonaire, Sint Eustatius and Saba", "BES", 535, Assignment.OFFICIALLY_ASSIGNED),
    BR("Brazil", "BRA", 76, Assignment.OFFICIALLY_ASSIGNED),
    BS("Bahamas", "BHS", 44, Assignment.OFFICIALLY_ASSIGNED),
    BT("Bhutan", "BTN", 64, Assignment.OFFICIALLY_ASSIGNED),
    BU("Burma", "BUR", 104, Assignment.TRANSITIONALLY_RESERVED),
    BV("Bouvet Island", "BVT", 74, Assignment.OFFICIALLY_ASSIGNED),
    BW("Botswana", "BWA", 72, Assignment.OFFICIALLY_ASSIGNED),
    BY("Belarus", "BLR", 112, Assignment.OFFICIALLY_ASSIGNED),
    BZ("Belize", "BLZ", 84, Assignment.OFFICIALLY_ASSIGNED),
    CA("Canada", "CAN", 124, Assignment.OFFICIALLY_ASSIGNED){

        public Locale toLocale() {
            return Locale.CANADA;
        }
    }
    ,
    CC("Cocos (Keeling) Islands", "CCK", 166, Assignment.OFFICIALLY_ASSIGNED),
    CD("Congo, the Democratic Republic of the", "COD", 180, Assignment.OFFICIALLY_ASSIGNED),
    CF("Central African Republic", "CAF", 140, Assignment.OFFICIALLY_ASSIGNED),
    CG("Congo", "COG", 178, Assignment.OFFICIALLY_ASSIGNED),
    CH("Switzerland", "CHE", 756, Assignment.OFFICIALLY_ASSIGNED),
    CI("C\u00f4te d'Ivoire", "CIV", 384, Assignment.OFFICIALLY_ASSIGNED),
    CK("Cook Islands", "COK", 184, Assignment.OFFICIALLY_ASSIGNED),
    CL("Chile", "CHL", 152, Assignment.OFFICIALLY_ASSIGNED),
    CM("Cameroon", "CMR", 120, Assignment.OFFICIALLY_ASSIGNED),
    CN("China", "CHN", 156, Assignment.OFFICIALLY_ASSIGNED){

        public Locale toLocale() {
            return Locale.CHINA;
        }
    }
    ,
    CO("Colombia", "COL", 170, Assignment.OFFICIALLY_ASSIGNED),
    CP("Clipperton Island", "CPT", -1, Assignment.EXCEPTIONALLY_RESERVED),
    CR("Costa Rica", "CRI", 188, Assignment.OFFICIALLY_ASSIGNED),
    CS("Serbia and Montenegro", "SCG", 891, Assignment.TRANSITIONALLY_RESERVED),
    CU("Cuba", "CUB", 192, Assignment.OFFICIALLY_ASSIGNED),
    CV("Cape Verde", "CPV", 132, Assignment.OFFICIALLY_ASSIGNED),
    CW("Cura\u00e7ao", "CUW", 531, Assignment.OFFICIALLY_ASSIGNED),
    CX("Christmas Island", "CXR", 162, Assignment.OFFICIALLY_ASSIGNED),
    CY("Cyprus", "CYP", 196, Assignment.OFFICIALLY_ASSIGNED),
    CZ("Czech Republic", "CZE", 203, Assignment.OFFICIALLY_ASSIGNED),
    DE("Germany", "DEU", 276, Assignment.OFFICIALLY_ASSIGNED){

        public Locale toLocale() {
            return Locale.GERMANY;
        }
    }
    ,
    DG("Diego Garcia", "DGA", -1, Assignment.EXCEPTIONALLY_RESERVED),
    DJ("Djibouti", "DJI", 262, Assignment.OFFICIALLY_ASSIGNED),
    DK("Denmark", "DNK", 208, Assignment.OFFICIALLY_ASSIGNED),
    DM("Dominica", "DMA", 212, Assignment.OFFICIALLY_ASSIGNED),
    DO("Dominican Republic", "DOM", 214, Assignment.OFFICIALLY_ASSIGNED),
    DZ("Algeria", "DZA", 12, Assignment.OFFICIALLY_ASSIGNED),
    EA("Ceuta, Melilla", null, -1, Assignment.EXCEPTIONALLY_RESERVED),
    EC("Ecuador", "ECU", 218, Assignment.OFFICIALLY_ASSIGNED),
    EE("Estonia", "EST", 233, Assignment.OFFICIALLY_ASSIGNED),
    EG("Egypt", "EGY", 818, Assignment.OFFICIALLY_ASSIGNED),
    EH("Western Sahara", "ESH", 732, Assignment.OFFICIALLY_ASSIGNED),
    ER("Eritrea", "ERI", 232, Assignment.OFFICIALLY_ASSIGNED),
    ES("Spain", "ESP", 724, Assignment.OFFICIALLY_ASSIGNED),
    ET("Ethiopia", "ETH", 231, Assignment.OFFICIALLY_ASSIGNED),
    EU("European Union", null, -1, Assignment.EXCEPTIONALLY_RESERVED),
    EZ("Eurozone", null, -1, Assignment.EXCEPTIONALLY_RESERVED),
    FI("Finland", "FIN", 246, Assignment.OFFICIALLY_ASSIGNED),
    FJ("Fiji", "FJI", 242, Assignment.OFFICIALLY_ASSIGNED),
    FK("Falkland Islands (Malvinas)", "FLK", 238, Assignment.OFFICIALLY_ASSIGNED),
    FM("Micronesia, Federated States of", "FSM", 583, Assignment.OFFICIALLY_ASSIGNED),
    FO("Faroe Islands", "FRO", 234, Assignment.OFFICIALLY_ASSIGNED),
    FR("France", "FRA", 250, Assignment.OFFICIALLY_ASSIGNED){

        public Locale toLocale() {
            return Locale.FRANCE;
        }
    }
    ,
    FX("France, Metropolitan", "FXX", 249, Assignment.EXCEPTIONALLY_RESERVED),
    GA("Gabon", "GAB", 266, Assignment.OFFICIALLY_ASSIGNED),
    GB("United Kingdom", "GBR", 826, Assignment.OFFICIALLY_ASSIGNED){

        public Locale toLocale() {
            return Locale.UK;
        }
    }
    ,
    GD("Grenada", "GRD", 308, Assignment.OFFICIALLY_ASSIGNED),
    GE("Georgia", "GEO", 268, Assignment.OFFICIALLY_ASSIGNED),
    GF("French Guiana", "GUF", 254, Assignment.OFFICIALLY_ASSIGNED),
    GG("Guernsey", "GGY", 831, Assignment.OFFICIALLY_ASSIGNED),
    GH("Ghana", "GHA", 288, Assignment.OFFICIALLY_ASSIGNED),
    GI("Gibraltar", "GIB", 292, Assignment.OFFICIALLY_ASSIGNED),
    GL("Greenland", "GRL", 304, Assignment.OFFICIALLY_ASSIGNED),
    GM("Gambia", "GMB", 270, Assignment.OFFICIALLY_ASSIGNED),
    GN("Guinea", "GIN", 324, Assignment.OFFICIALLY_ASSIGNED),
    GP("Guadeloupe", "GLP", 312, Assignment.OFFICIALLY_ASSIGNED),
    GQ("Equatorial Guinea", "GNQ", 226, Assignment.OFFICIALLY_ASSIGNED),
    GR("Greece", "GRC", 300, Assignment.OFFICIALLY_ASSIGNED),
    GS("South Georgia and the South Sandwich Islands", "SGS", 239, Assignment.OFFICIALLY_ASSIGNED),
    GT("Guatemala", "GTM", 320, Assignment.OFFICIALLY_ASSIGNED),
    GU("Guam", "GUM", 316, Assignment.OFFICIALLY_ASSIGNED),
    GW("Guinea-Bissau", "GNB", 624, Assignment.OFFICIALLY_ASSIGNED),
    GY("Guyana", "GUY", 328, Assignment.OFFICIALLY_ASSIGNED),
    HK("Hong Kong", "HKG", 344, Assignment.OFFICIALLY_ASSIGNED),
    HM("Heard Island and McDonald Islands", "HMD", 334, Assignment.OFFICIALLY_ASSIGNED),
    HN("Honduras", "HND", 340, Assignment.OFFICIALLY_ASSIGNED),
    HR("Croatia", "HRV", 191, Assignment.OFFICIALLY_ASSIGNED),
    HT("Haiti", "HTI", 332, Assignment.OFFICIALLY_ASSIGNED),
    HU("Hungary", "HUN", 348, Assignment.OFFICIALLY_ASSIGNED),
    IC("Canary Islands", null, -1, Assignment.EXCEPTIONALLY_RESERVED),
    ID("Indonesia", "IDN", 360, Assignment.OFFICIALLY_ASSIGNED),
    IE("Ireland", "IRL", 372, Assignment.OFFICIALLY_ASSIGNED),
    IL("Israel", "ISR", 376, Assignment.OFFICIALLY_ASSIGNED),
    IM("Isle of Man", "IMN", 833, Assignment.OFFICIALLY_ASSIGNED),
    IN("India", "IND", 356, Assignment.OFFICIALLY_ASSIGNED),
    IO("British Indian Ocean Territory", "IOT", 86, Assignment.OFFICIALLY_ASSIGNED),
    IQ("Iraq", "IRQ", 368, Assignment.OFFICIALLY_ASSIGNED),
    IR("Iran, Islamic Republic of", "IRN", 364, Assignment.OFFICIALLY_ASSIGNED),
    IS("Iceland", "ISL", 352, Assignment.OFFICIALLY_ASSIGNED),
    IT("Italy", "ITA", 380, Assignment.OFFICIALLY_ASSIGNED){

        public Locale toLocale() {
            return Locale.ITALY;
        }
    }
    ,
    JE("Jersey", "JEY", 832, Assignment.OFFICIALLY_ASSIGNED),
    JM("Jamaica", "JAM", 388, Assignment.OFFICIALLY_ASSIGNED),
    JO("Jordan", "JOR", 400, Assignment.OFFICIALLY_ASSIGNED),
    JP("Japan", "JPN", 392, Assignment.OFFICIALLY_ASSIGNED){

        public Locale toLocale() {
            return Locale.JAPAN;
        }
    }
    ,
    KE("Kenya", "KEN", 404, Assignment.OFFICIALLY_ASSIGNED),
    KG("Kyrgyzstan", "KGZ", 417, Assignment.OFFICIALLY_ASSIGNED),
    KH("Cambodia", "KHM", 116, Assignment.OFFICIALLY_ASSIGNED),
    KI("Kiribati", "KIR", 296, Assignment.OFFICIALLY_ASSIGNED),
    KM("Comoros", "COM", 174, Assignment.OFFICIALLY_ASSIGNED),
    KN("Saint Kitts and Nevis", "KNA", 659, Assignment.OFFICIALLY_ASSIGNED),
    KP("Korea, Democratic People's Republic of", "PRK", 408, Assignment.OFFICIALLY_ASSIGNED),
    KR("Korea, Republic of", "KOR", 410, Assignment.OFFICIALLY_ASSIGNED){

        public Locale toLocale() {
            return Locale.KOREA;
        }
    }
    ,
    KW("Kuwait", "KWT", 414, Assignment.OFFICIALLY_ASSIGNED),
    KY("Cayman Islands", "CYM", 136, Assignment.OFFICIALLY_ASSIGNED),
    KZ("Kazakhstan", "KAZ", 398, Assignment.OFFICIALLY_ASSIGNED),
    LA("Lao People's Democratic Republic", "LAO", 418, Assignment.OFFICIALLY_ASSIGNED),
    LB("Lebanon", "LBN", 422, Assignment.OFFICIALLY_ASSIGNED),
    LC("Saint Lucia", "LCA", 662, Assignment.OFFICIALLY_ASSIGNED),
    LI("Liechtenstein", "LIE", 438, Assignment.OFFICIALLY_ASSIGNED),
    LK("Sri Lanka", "LKA", 144, Assignment.OFFICIALLY_ASSIGNED),
    LR("Liberia", "LBR", 430, Assignment.OFFICIALLY_ASSIGNED),
    LS("Lesotho", "LSO", 426, Assignment.OFFICIALLY_ASSIGNED),
    LT("Lithuania", "LTU", 440, Assignment.OFFICIALLY_ASSIGNED),
    LU("Luxembourg", "LUX", 442, Assignment.OFFICIALLY_ASSIGNED),
    LV("Latvia", "LVA", 428, Assignment.OFFICIALLY_ASSIGNED),
    LY("Libya", "LBY", 434, Assignment.OFFICIALLY_ASSIGNED),
    MA("Morocco", "MAR", 504, Assignment.OFFICIALLY_ASSIGNED),
    MC("Monaco", "MCO", 492, Assignment.OFFICIALLY_ASSIGNED),
    MD("Moldova, Republic of", "MDA", 498, Assignment.OFFICIALLY_ASSIGNED),
    ME("Montenegro", "MNE", 499, Assignment.OFFICIALLY_ASSIGNED),
    MF("Saint Martin (French part)", "MAF", 663, Assignment.OFFICIALLY_ASSIGNED),
    MG("Madagascar", "MDG", 450, Assignment.OFFICIALLY_ASSIGNED),
    MH("Marshall Islands", "MHL", 584, Assignment.OFFICIALLY_ASSIGNED),
    MK("North Macedonia, Republic of", "MKD", 807, Assignment.OFFICIALLY_ASSIGNED),
    ML("Mali", "MLI", 466, Assignment.OFFICIALLY_ASSIGNED),
    MM("Myanmar", "MMR", 104, Assignment.OFFICIALLY_ASSIGNED),
    MN("Mongolia", "MNG", 496, Assignment.OFFICIALLY_ASSIGNED),
    MO("Macao", "MAC", 446, Assignment.OFFICIALLY_ASSIGNED),
    MP("Northern Mariana Islands", "MNP", 580, Assignment.OFFICIALLY_ASSIGNED),
    MQ("Martinique", "MTQ", 474, Assignment.OFFICIALLY_ASSIGNED),
    MR("Mauritania", "MRT", 478, Assignment.OFFICIALLY_ASSIGNED),
    MS("Montserrat", "MSR", 500, Assignment.OFFICIALLY_ASSIGNED),
    MT("Malta", "MLT", 470, Assignment.OFFICIALLY_ASSIGNED),
    MU("Mauritius", "MUS", 480, Assignment.OFFICIALLY_ASSIGNED),
    MV("Maldives", "MDV", 462, Assignment.OFFICIALLY_ASSIGNED),
    MW("Malawi", "MWI", 454, Assignment.OFFICIALLY_ASSIGNED),
    MX("Mexico", "MEX", 484, Assignment.OFFICIALLY_ASSIGNED),
    MY("Malaysia", "MYS", 458, Assignment.OFFICIALLY_ASSIGNED),
    MZ("Mozambique", "MOZ", 508, Assignment.OFFICIALLY_ASSIGNED),
    NA("Namibia", "NAM", 516, Assignment.OFFICIALLY_ASSIGNED),
    NC("New Caledonia", "NCL", 540, Assignment.OFFICIALLY_ASSIGNED),
    NE("Niger", "NER", 562, Assignment.OFFICIALLY_ASSIGNED),
    NF("Norfolk Island", "NFK", 574, Assignment.OFFICIALLY_ASSIGNED),
    NG("Nigeria", "NGA", 566, Assignment.OFFICIALLY_ASSIGNED),
    NI("Nicaragua", "NIC", 558, Assignment.OFFICIALLY_ASSIGNED),
    NL("Netherlands", "NLD", 528, Assignment.OFFICIALLY_ASSIGNED),
    NO("Norway", "NOR", 578, Assignment.OFFICIALLY_ASSIGNED),
    NP("Nepal", "NPL", 524, Assignment.OFFICIALLY_ASSIGNED),
    NR("Nauru", "NRU", 520, Assignment.OFFICIALLY_ASSIGNED),
    NT("Neutral Zone", "NTZ", 536, Assignment.TRANSITIONALLY_RESERVED),
    NU("Niue", "NIU", 570, Assignment.OFFICIALLY_ASSIGNED),
    NZ("New Zealand", "NZL", 554, Assignment.OFFICIALLY_ASSIGNED),
    OM("Oman", "OMN", 512, Assignment.OFFICIALLY_ASSIGNED),
    PA("Panama", "PAN", 591, Assignment.OFFICIALLY_ASSIGNED),
    PE("Peru", "PER", 604, Assignment.OFFICIALLY_ASSIGNED),
    PF("French Polynesia", "PYF", 258, Assignment.OFFICIALLY_ASSIGNED),
    PG("Papua New Guinea", "PNG", 598, Assignment.OFFICIALLY_ASSIGNED),
    PH("Philippines", "PHL", 608, Assignment.OFFICIALLY_ASSIGNED),
    PK("Pakistan", "PAK", 586, Assignment.OFFICIALLY_ASSIGNED),
    PL("Poland", "POL", 616, Assignment.OFFICIALLY_ASSIGNED),
    PM("Saint Pierre and Miquelon", "SPM", 666, Assignment.OFFICIALLY_ASSIGNED),
    PN("Pitcairn", "PCN", 612, Assignment.OFFICIALLY_ASSIGNED),
    PR("Puerto Rico", "PRI", 630, Assignment.OFFICIALLY_ASSIGNED),
    PS("Palestine, State of", "PSE", 275, Assignment.OFFICIALLY_ASSIGNED),
    PT("Portugal", "PRT", 620, Assignment.OFFICIALLY_ASSIGNED),
    PW("Palau", "PLW", 585, Assignment.OFFICIALLY_ASSIGNED),
    PY("Paraguay", "PRY", 600, Assignment.OFFICIALLY_ASSIGNED),
    QA("Qatar", "QAT", 634, Assignment.OFFICIALLY_ASSIGNED),
    RE("R\u00e9union", "REU", 638, Assignment.OFFICIALLY_ASSIGNED),
    RO("Romania", "ROU", 642, Assignment.OFFICIALLY_ASSIGNED),
    RS("Serbia", "SRB", 688, Assignment.OFFICIALLY_ASSIGNED),
    RU("Russian Federation", "RUS", 643, Assignment.OFFICIALLY_ASSIGNED),
    RW("Rwanda", "RWA", 646, Assignment.OFFICIALLY_ASSIGNED),
    SA("Saudi Arabia", "SAU", 682, Assignment.OFFICIALLY_ASSIGNED),
    SB("Solomon Islands", "SLB", 90, Assignment.OFFICIALLY_ASSIGNED),
    SC("Seychelles", "SYC", 690, Assignment.OFFICIALLY_ASSIGNED),
    SD("Sudan", "SDN", 729, Assignment.OFFICIALLY_ASSIGNED),
    SE("Sweden", "SWE", 752, Assignment.OFFICIALLY_ASSIGNED),
    SF("Finland", "FIN", 246, Assignment.TRANSITIONALLY_RESERVED),
    SG("Singapore", "SGP", 702, Assignment.OFFICIALLY_ASSIGNED),
    SH("Saint Helena, Ascension and Tristan da Cunha", "SHN", 654, Assignment.OFFICIALLY_ASSIGNED),
    SI("Slovenia", "SVN", 705, Assignment.OFFICIALLY_ASSIGNED),
    SJ("Svalbard and Jan Mayen", "SJM", 744, Assignment.OFFICIALLY_ASSIGNED),
    SK("Slovakia", "SVK", 703, Assignment.OFFICIALLY_ASSIGNED),
    SL("Sierra Leone", "SLE", 694, Assignment.OFFICIALLY_ASSIGNED),
    SM("San Marino", "SMR", 674, Assignment.OFFICIALLY_ASSIGNED),
    SN("Senegal", "SEN", 686, Assignment.OFFICIALLY_ASSIGNED),
    SO("Somalia", "SOM", 706, Assignment.OFFICIALLY_ASSIGNED),
    SR("Suriname", "SUR", 740, Assignment.OFFICIALLY_ASSIGNED),
    SS("South Sudan", "SSD", 728, Assignment.OFFICIALLY_ASSIGNED),
    ST("Sao Tome and Principe", "STP", 678, Assignment.OFFICIALLY_ASSIGNED),
    SU("USSR", "SUN", 810, Assignment.EXCEPTIONALLY_RESERVED),
    SV("El Salvador", "SLV", 222, Assignment.OFFICIALLY_ASSIGNED),
    SX("Sint Maarten (Dutch part)", "SXM", 534, Assignment.OFFICIALLY_ASSIGNED),
    SY("Syrian Arab Republic", "SYR", 760, Assignment.OFFICIALLY_ASSIGNED),
    SZ("Eswatini", "SWZ", 748, Assignment.OFFICIALLY_ASSIGNED),
    TA("Tristan da Cunha", "TAA", -1, Assignment.EXCEPTIONALLY_RESERVED),
    TC("Turks and Caicos Islands", "TCA", 796, Assignment.OFFICIALLY_ASSIGNED),
    TD("Chad", "TCD", 148, Assignment.OFFICIALLY_ASSIGNED),
    TF("French Southern Territories", "ATF", 260, Assignment.OFFICIALLY_ASSIGNED),
    TG("Togo", "TGO", 768, Assignment.OFFICIALLY_ASSIGNED),
    TH("Thailand", "THA", 764, Assignment.OFFICIALLY_ASSIGNED),
    TJ("Tajikistan", "TJK", 762, Assignment.OFFICIALLY_ASSIGNED),
    TK("Tokelau", "TKL", 772, Assignment.OFFICIALLY_ASSIGNED),
    TL("Timor-Leste", "TLS", 626, Assignment.OFFICIALLY_ASSIGNED),
    TM("Turkmenistan", "TKM", 795, Assignment.OFFICIALLY_ASSIGNED),
    TN("Tunisia", "TUN", 788, Assignment.OFFICIALLY_ASSIGNED),
    TO("Tonga", "TON", 776, Assignment.OFFICIALLY_ASSIGNED),
    TP("East Timor", "TMP", 626, Assignment.TRANSITIONALLY_RESERVED),
    TR("Turkey", "TUR", 792, Assignment.OFFICIALLY_ASSIGNED),
    TT("Trinidad and Tobago", "TTO", 780, Assignment.OFFICIALLY_ASSIGNED),
    TV("Tuvalu", "TUV", 798, Assignment.OFFICIALLY_ASSIGNED),
    TW("Taiwan, Province of China", "TWN", 158, Assignment.OFFICIALLY_ASSIGNED){

        public Locale toLocale() {
            return Locale.TAIWAN;
        }
    }
    ,
    TZ("Tanzania, United Republic of", "TZA", 834, Assignment.OFFICIALLY_ASSIGNED),
    UA("Ukraine", "UKR", 804, Assignment.OFFICIALLY_ASSIGNED),
    UG("Uganda", "UGA", 800, Assignment.OFFICIALLY_ASSIGNED),
    UK("United Kingdom", null, 826, Assignment.EXCEPTIONALLY_RESERVED){

        public Locale toLocale() {
            return Locale.UK;
        }
    }
    ,
    UM("United States Minor Outlying Islands", "UMI", 581, Assignment.OFFICIALLY_ASSIGNED),
    US("United States", "USA", 840, Assignment.OFFICIALLY_ASSIGNED){

        public Locale toLocale() {
            return Locale.US;
        }
    }
    ,
    UY("Uruguay", "URY", 858, Assignment.OFFICIALLY_ASSIGNED),
    UZ("Uzbekistan", "UZB", 860, Assignment.OFFICIALLY_ASSIGNED),
    VA("Holy See (Vatican City State)", "VAT", 336, Assignment.OFFICIALLY_ASSIGNED),
    VC("Saint Vincent and the Grenadines", "VCT", 670, Assignment.OFFICIALLY_ASSIGNED),
    VE("Venezuela, Bolivarian Republic of", "VEN", 862, Assignment.OFFICIALLY_ASSIGNED),
    VG("Virgin Islands, British", "VGB", 92, Assignment.OFFICIALLY_ASSIGNED),
    VI("Virgin Islands, U.S.", "VIR", 850, Assignment.OFFICIALLY_ASSIGNED),
    VN("Viet Nam", "VNM", 704, Assignment.OFFICIALLY_ASSIGNED),
    VU("Vanuatu", "VUT", 548, Assignment.OFFICIALLY_ASSIGNED),
    WF("Wallis and Futuna", "WLF", 876, Assignment.OFFICIALLY_ASSIGNED),
    WS("Samoa", "WSM", 882, Assignment.OFFICIALLY_ASSIGNED),
    XK("Kosovo, Republic of", "XKX", -1, Assignment.USER_ASSIGNED),
    YE("Yemen", "YEM", 887, Assignment.OFFICIALLY_ASSIGNED),
    YT("Mayotte", "MYT", 175, Assignment.OFFICIALLY_ASSIGNED),
    YU("Yugoslavia", "YUG", 890, Assignment.TRANSITIONALLY_RESERVED),
    ZA("South Africa", "ZAF", 710, Assignment.OFFICIALLY_ASSIGNED),
    ZM("Zambia", "ZMB", 894, Assignment.OFFICIALLY_ASSIGNED),
    ZR("Zaire", "ZAR", 180, Assignment.TRANSITIONALLY_RESERVED),
    ZW("Zimbabwe", "ZWE", 716, Assignment.OFFICIALLY_ASSIGNED);

    private static final Map<String, CountryCode> alpha3Map;
    private static final Map<String, CountryCode> alpha4Map;
    private static final Map<Integer, CountryCode> numericMap;
    private final String name;
    private final String alpha3;
    private final int numeric;
    private final Assignment assignment;

    private CountryCode(String name, String alpha3, int numeric, Assignment assignment) {
        this.name = name;
        this.alpha3 = alpha3;
        this.numeric = numeric;
        this.assignment = assignment;
    }

    public String getName() {
        return this.name;
    }

    public String getAlpha2() {
        return this.name();
    }

    public String getAlpha3() {
        return this.alpha3;
    }

    public int getNumeric() {
        return this.numeric;
    }

    public Assignment getAssignment() {
        return this.assignment;
    }

    public Locale toLocale() {
        return new Locale("", this.name());
    }

    public Currency getCurrency() {
        try {
            return Currency.getInstance(this.toLocale());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static CountryCode getByCode(String code) {
        return CountryCode.getByCode(code, true);
    }

    public static CountryCode getByCodeIgnoreCase(String code) {
        return CountryCode.getByCode(code, false);
    }

    public static CountryCode getByCode(String code, boolean caseSensitive) {
        if (code == null) {
            return null;
        }
        switch (code.length()) {
            case 2: {
                code = CountryCode.canonicalize(code, caseSensitive);
                return CountryCode.getByAlpha2Code(code);
            }
            case 3: {
                code = CountryCode.canonicalize(code, caseSensitive);
                return CountryCode.getByAlpha3Code(code);
            }
            case 4: {
                code = CountryCode.canonicalize(code, caseSensitive);
                return CountryCode.getByAlpha4Code(code);
            }
            case 9: {
                code = CountryCode.canonicalize(code, caseSensitive);
                if (!"UNDEFINED".equals(code)) break;
                return UNDEFINED;
            }
        }
        return null;
    }

    public static CountryCode getByLocale(Locale locale) {
        if (locale == null) {
            return null;
        }
        String country = locale.getCountry();
        if (country == null || country.length() == 0) {
            return UNDEFINED;
        }
        return CountryCode.getByCode(country, true);
    }

    static String canonicalize(String code, boolean caseSensitive) {
        if (code == null || code.length() == 0) {
            return null;
        }
        if (caseSensitive) {
            return code;
        }
        return code.toUpperCase();
    }

    public static CountryCode getByAlpha2Code(String code) {
        try {
            return Enum.valueOf(CountryCode.class, code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static CountryCode getByAlpha3Code(String code) {
        return alpha3Map.get(code);
    }

    public static CountryCode getByAlpha4Code(String code) {
        return alpha4Map.get(code);
    }

    public static CountryCode getByCode(int code) {
        if (code <= 0) {
            return null;
        }
        return numericMap.get(code);
    }

    public static List<CountryCode> findByName(String regex) {
        if (regex == null) {
            throw new IllegalArgumentException("regex is null.");
        }
        Pattern pattern = Pattern.compile(regex);
        return CountryCode.findByName(pattern);
    }

    public static List<CountryCode> findByName(Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern is null.");
        }
        ArrayList<CountryCode> list = new ArrayList<CountryCode>();
        for (CountryCode entry : CountryCode.values()) {
            if (!pattern.matcher(entry.getName()).matches()) continue;
            list.add(entry);
        }
        return list;
    }

    static {
        alpha3Map = new HashMap<String, CountryCode>();
        alpha4Map = new HashMap<String, CountryCode>();
        numericMap = new HashMap<Integer, CountryCode>();
        for (CountryCode cc : CountryCode.values()) {
            if (cc.getAlpha3() != null) {
                alpha3Map.put(cc.getAlpha3(), cc);
            }
            if (cc.getNumeric() == -1) continue;
            numericMap.put(cc.getNumeric(), cc);
        }
        alpha3Map.put("FIN", FI);
        alpha4Map.put("ANHH", AN);
        alpha4Map.put("BUMM", BU);
        alpha4Map.put("CSXX", CS);
        alpha4Map.put("NTHH", NT);
        alpha4Map.put("TPTL", TP);
        alpha4Map.put("YUCS", YU);
        alpha4Map.put("ZRCD", ZR);
        numericMap.put(104, MM);
        numericMap.put(180, CD);
        numericMap.put(246, FI);
        numericMap.put(826, GB);
        numericMap.put(626, TL);
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum Assignment {
        OFFICIALLY_ASSIGNED,
        USER_ASSIGNED,
        EXCEPTIONALLY_RESERVED,
        TRANSITIONALLY_RESERVED,
        INDETERMINATELY_RESERVED,
        NOT_USED;

    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.neovisionaries.i18n;

import com.neovisionaries.i18n.CountryCode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public enum CurrencyCode {
    UNDEFINED("Undefined", -1, -1, new CountryCode[0]),
    AED("UAE Dirham", 784, 2, CountryCode.AE),
    AFN("Afghani", 971, 2, CountryCode.AF),
    ALL("Lek", 8, 2, CountryCode.AL),
    AMD("Armenian Dram", 51, 2, CountryCode.AM),
    ANG("Netherlands Antillean Guilder", 532, 2, CountryCode.CW, CountryCode.SX),
    AOA("Kwanza", 973, 2, CountryCode.AO),
    ARS("Argentine Peso", 32, 2, CountryCode.AR),
    AUD("Australian Dollar", 36, 2, CountryCode.AU, CountryCode.CC, CountryCode.CX, CountryCode.HM, CountryCode.KI, CountryCode.NF, CountryCode.NR, CountryCode.TV),
    AWG("Aruban Florin", 533, 2, CountryCode.AW),
    AZN("Azerbaijanian Manat", 944, 2, CountryCode.AZ),
    BAM("Convertible Mark", 977, 2, CountryCode.BA),
    BBD("Barbados Dollar", 52, 2, CountryCode.BB),
    BDT("Taka", 50, 2, CountryCode.BD),
    BGN("Bulgarian Lev", 975, 2, CountryCode.BG),
    BHD("Bahraini Dinar", 48, 3, CountryCode.BH),
    BIF("Burundi Franc", 108, 0, CountryCode.BI),
    BMD("Bermudian Dollar", 60, 2, CountryCode.BM),
    BND("Brunei Dollar", 96, 2, CountryCode.BN),
    BOB("Boliviano", 68, 2, CountryCode.BO),
    BOV("Mvdol", 984, 2, new CountryCode[]{CountryCode.BO}){

        public boolean isFund() {
            return true;
        }
    }
    ,
    BRL("Brazilian Real", 986, 2, CountryCode.BR),
    BSD("Bahamian Dollar", 44, 2, CountryCode.BS),
    BTN("Ngultrum", 64, 2, CountryCode.BT),
    BWP("Pula", 72, 2, CountryCode.BW),
    BYN("Belarusian Ruble", 933, 2, CountryCode.BY),
    BYR("Belarusian Ruble", 974, 0, CountryCode.BY),
    BZD("Belize Dollar", 84, 2, CountryCode.BZ),
    CAD("Canadian Dollar", 124, 2, CountryCode.CA),
    CDF("Congolese Franc", 976, 2, CountryCode.CD),
    CHE("WIR Euro", 947, 2, new CountryCode[]{CountryCode.CH}){

        public boolean isFund() {
            return true;
        }
    }
    ,
    CHF("Swiss Franc", 756, 2, CountryCode.CH, CountryCode.LI),
    CHW("WIR Franc", 948, 2, new CountryCode[]{CountryCode.CH}){

        public boolean isFund() {
            return true;
        }
    }
    ,
    CLF("Unidad de Fomento", 990, 0, new CountryCode[]{CountryCode.CL}){

        public boolean isFund() {
            return true;
        }
    }
    ,
    CLP("Chilean Peso", 152, 0, CountryCode.CL),
    CNY("Yuan Renminbi", 156, 2, CountryCode.CN),
    COP("Colombian Peso", 170, 2, CountryCode.CO),
    COU("Unidad de Valor Real", 970, 2, new CountryCode[]{CountryCode.CO}){

        public boolean isFund() {
            return true;
        }
    }
    ,
    CRC("Costa Rican Colon", 188, 2, CountryCode.CR),
    CUC("Peso Convertible", 931, 2, CountryCode.CU),
    CUP("Cuban Peso", 192, 2, CountryCode.CU),
    CVE("Cape Verde Escudo", 132, 2, CountryCode.CV),
    CZK("Czech Koruna", 203, 2, CountryCode.CZ),
    DJF("Djibouti Franc", 262, 0, CountryCode.DJ),
    DKK("Danish Krone", 208, 2, CountryCode.DK, CountryCode.FO, CountryCode.GL),
    DOP("Dominican Peso", 214, 2, CountryCode.DO),
    DZD("Algerian Dinar", 12, 2, CountryCode.DZ),
    EGP("Egyptian Pound", 818, 2, CountryCode.EG),
    ERN("Nakfa", 232, 2, CountryCode.ER),
    ETB("Ethiopian Birr", 230, 2, CountryCode.ET),
    EUR("Euro", 978, 2, CountryCode.AD, CountryCode.AT, CountryCode.AX, CountryCode.BE, CountryCode.BL, CountryCode.CY, CountryCode.DE, CountryCode.EE, CountryCode.ES, CountryCode.EU, CountryCode.FI, CountryCode.FR, CountryCode.GF, CountryCode.GP, CountryCode.GR, CountryCode.IE, CountryCode.IT, CountryCode.LT, CountryCode.LU, CountryCode.LV, CountryCode.MC, CountryCode.ME, CountryCode.MF, CountryCode.MQ, CountryCode.MT, CountryCode.NL, CountryCode.PM, CountryCode.PT, CountryCode.RE, CountryCode.SI, CountryCode.SK, CountryCode.SM, CountryCode.TF, CountryCode.VA, CountryCode.XK, CountryCode.YT),
    FJD("Fiji Dollar", 242, 2, CountryCode.FJ),
    FKP("Falkland Islands Pound", 238, 2, CountryCode.FK),
    GBP("Pound Sterling", 826, 2, CountryCode.GB, CountryCode.GG, CountryCode.IM, CountryCode.JE),
    GEL("Lari", 981, 2, CountryCode.GE),
    GHS("Ghana Cedi", 936, 2, CountryCode.GH),
    GIP("Gibraltar Pound", 292, 2, CountryCode.GI),
    GMD("Dalasi", 270, 2, CountryCode.GM),
    GNF("Guinea Franc", 324, 0, CountryCode.GN),
    GTQ("Quetzal", 320, 2, CountryCode.GT),
    GYD("Guyana Dollar", 328, 2, CountryCode.GY),
    HKD("Hong Kong Dollar", 344, 2, CountryCode.HK),
    HNL("Lempira", 340, 2, CountryCode.HN),
    HRK("Croatian Kuna", 191, 2, CountryCode.HR),
    HTG("Gourde", 332, 2, CountryCode.HT),
    HUF("Forint", 348, 2, CountryCode.HU),
    IDR("Rupiah", 360, 2, CountryCode.ID),
    ILS("New Israeli Sheqel", 376, 2, CountryCode.IL),
    INR("Indian Rupee", 356, 2, CountryCode.BT, CountryCode.IN),
    IQD("Iraqi Dinar", 368, 3, CountryCode.IQ),
    IRR("Iranian Rial", 364, 2, CountryCode.IR),
    ISK("Iceland Krona", 352, 0, CountryCode.IS),
    JMD("Jamaican Dollar", 388, 2, CountryCode.JM),
    JOD("Jordanian Dinar", 400, 3, CountryCode.JO),
    JPY("Yen", 392, 0, CountryCode.JP),
    KES("Kenyan Shilling", 404, 2, CountryCode.KE),
    KGS("Som", 417, 2, CountryCode.KG),
    KHR("Riel", 116, 2, CountryCode.KH),
    KMF("Comoro Franc", 174, 0, CountryCode.KM),
    KPW("North Korean Won", 408, 2, CountryCode.KP),
    KRW("Won", 410, 0, CountryCode.KR),
    KWD("Kuwaiti Dinar", 414, 3, CountryCode.KW),
    KYD("Cayman Islands Dollar", 136, 2, CountryCode.KY),
    KZT("Tenge", 398, 2, CountryCode.KZ),
    LAK("Kip", 418, 2, CountryCode.LA),
    LBP("Lebanese Pound", 422, 2, CountryCode.LB),
    LKR("Sri Lanka Rupee", 144, 2, CountryCode.LK),
    LRD("Liberian Dollar", 430, 2, CountryCode.LR),
    LSL("Loti", 426, 2, CountryCode.LS),
    LTL("Lithuanian Litas", 440, 2, CountryCode.LT),
    LYD("Libyan Dinar", 434, 3, CountryCode.LY),
    MAD("Moroccan Dirham", 504, 2, CountryCode.EH, CountryCode.MA),
    MDL("Moldovan Leu", 498, 2, CountryCode.MD),
    MGA("Malagasy Ariary", 969, 2, CountryCode.MG),
    MKD("Denar", 807, 2, CountryCode.MK),
    MMK("Kyat", 104, 2, CountryCode.MM),
    MNT("Tugrik", 496, 2, CountryCode.MN),
    MOP("Pataca", 446, 2, CountryCode.MO),
    MRO("Ouguiya", 478, 2, CountryCode.MR),
    MRU("Ouguiya", 929, 2, CountryCode.MR),
    MUR("Mauritius Rupee", 480, 2, CountryCode.MU),
    MVR("Rufiyaa", 462, 2, CountryCode.MV),
    MWK("Kwacha", 454, 2, CountryCode.MW),
    MXN("Mexican Peso", 484, 2, CountryCode.MX),
    MXV("Mexican Unidad de Inversion (UDI)", 979, 2, new CountryCode[]{CountryCode.MX}){

        public boolean isFund() {
            return true;
        }
    }
    ,
    MYR("Malaysian Ringgit", 458, 2, CountryCode.MY),
    MZN("Mozambique Metical", 943, 2, CountryCode.MZ),
    NAD("Namibia Dollar", 516, 2, CountryCode.NA),
    NGN("Naira", 566, 2, CountryCode.NG),
    NIO("Cordoba Oro", 558, 2, CountryCode.NI),
    NOK("Norwegian Krone", 578, 2, CountryCode.BV, CountryCode.NO, CountryCode.SJ),
    NPR("Nepalese Rupee", 524, 2, CountryCode.NP),
    NZD("New Zealand Dollar", 554, 2, CountryCode.CK, CountryCode.NU, CountryCode.NZ, CountryCode.PN, CountryCode.TK),
    OMR("Rial Omani", 512, 3, CountryCode.OM),
    PAB("Balboa", 590, 2, CountryCode.PA),
    PEN("Nuevo Sol", 604, 2, CountryCode.PE),
    PGK("Kina", 598, 2, CountryCode.PG),
    PHP("Philippine Peso", 608, 2, CountryCode.PH),
    PKR("Pakistan Rupee", 586, 2, CountryCode.PK),
    PLN("Zloty", 985, 2, CountryCode.PL),
    PYG("Guarani", 600, 0, CountryCode.PY),
    QAR("Qatari Rial", 634, 2, CountryCode.QA),
    RON("New Romanian Leu", 946, 2, CountryCode.RO),
    RSD("Serbian Dinar", 941, 2, CountryCode.RS),
    RUB("Russian Ruble", 643, 2, CountryCode.RU),
    RUR("Russian Ruble", 810, 2, CountryCode.RU),
    RWF("Rwanda Franc", 646, 0, CountryCode.RW),
    SAR("Saudi Riyal", 682, 2, CountryCode.SA),
    SBD("Solomon Islands Dollar", 90, 2, CountryCode.SB),
    SCR("Seychelles Rupee", 690, 2, CountryCode.SC),
    SDG("Sudanese Pound", 938, 2, CountryCode.SD),
    SEK("Swedish Krona", 752, 2, CountryCode.SE),
    SGD("Singapore Dollar", 702, 2, CountryCode.SG),
    SHP("Saint Helena Pound", 654, 2, CountryCode.SH),
    SLL("Leone", 694, 2, CountryCode.SL),
    SOS("Somali Shilling", 706, 2, CountryCode.SO),
    SRD("Surinam Dollar", 968, 2, CountryCode.SR),
    SSP("South Sudanese Pound", 728, 2, CountryCode.SS),
    STD("Dobra", 678, 2, CountryCode.ST),
    STN("Dobra", 930, 2, CountryCode.ST),
    SVC("El Salvador Colon", 222, 2, CountryCode.SV),
    SYP("Syrian Pound", 760, 2, CountryCode.SY),
    SZL("Lilangeni", 748, 2, CountryCode.SZ),
    THB("Baht", 764, 2, CountryCode.TH),
    TJS("Somoni", 972, 2, CountryCode.TJ),
    TMT("Turkmenistan New Manat", 934, 2, CountryCode.TM),
    TND("Tunisian Dinar", 788, 3, CountryCode.TN),
    TOP("Pa\u02bbanga", 776, 2, CountryCode.TO),
    TRY("Turkish Lira", 949, 2, CountryCode.TR),
    TTD("Trinidad and Tobago Dollar", 780, 2, CountryCode.TT),
    TWD("New Taiwan Dollar", 901, 2, CountryCode.TW),
    TZS("Tanzanian Shilling", 834, 2, CountryCode.TZ),
    UAH("Hryvnia", 980, 2, CountryCode.UA),
    UGX("Uganda Shilling", 800, 0, CountryCode.UG),
    USD("US Dollar", 840, 2, CountryCode.AS, CountryCode.BQ, CountryCode.EC, CountryCode.FM, CountryCode.GU, CountryCode.HT, CountryCode.IO, CountryCode.MH, CountryCode.MP, CountryCode.PA, CountryCode.PR, CountryCode.PW, CountryCode.SV, CountryCode.TC, CountryCode.TL, CountryCode.UM, CountryCode.US, CountryCode.VG, CountryCode.VI),
    USN("US Dollar (Next day)", 997, 2, new CountryCode[]{CountryCode.US}){

        public boolean isFund() {
            return true;
        }
    }
    ,
    USS("US Dollar (Same day)", 998, 2, new CountryCode[]{CountryCode.US}){

        public boolean isFund() {
            return true;
        }
    }
    ,
    UYI("Uruguay Peso en Unidades Indexadas (URUIURUI)", 940, 0, new CountryCode[]{CountryCode.UY}){

        public boolean isFund() {
            return true;
        }
    }
    ,
    UYU("Peso Uruguayo", 858, 2, CountryCode.UY),
    UZS("Uzbekistan Sum", 860, 2, CountryCode.UZ),
    VEF("Bolivar", 937, 2, CountryCode.VE),
    VES("Bolivar Soberano", 928, 2, CountryCode.VE),
    VND("Dong", 704, 0, CountryCode.VN),
    VUV("Vatu", 548, 0, CountryCode.VU),
    WST("Tala", 882, 2, CountryCode.WS),
    XAF("CFA Franc BEAC", 950, 0, CountryCode.CF, CountryCode.CG, CountryCode.CM, CountryCode.GA, CountryCode.GQ, CountryCode.TD),
    XAG("Silver", 961, -1, new CountryCode[0]){

        public boolean isPreciousMetal() {
            return true;
        }
    }
    ,
    XAU("Gold", 959, -1, new CountryCode[0]){

        public boolean isPreciousMetal() {
            return true;
        }
    }
    ,
    XBA("Bond Markets Unit European Composite Unit (EURCO)", 955, -1, new CountryCode[0]),
    XBB("Bond Markets Unit European Monetary Unit (E.M.U.-6)", 956, -1, new CountryCode[0]),
    XBC("Bond Markets Unit European Unit of Account 9 (E.U.A.-9)", 957, -1, new CountryCode[0]),
    XBD("Bond Markets Unit European Unit of Account 17 (E.U.A.-17)", 958, -1, new CountryCode[0]),
    XCD("East Caribbean Dollar", 951, 2, CountryCode.AG, CountryCode.AI, CountryCode.DM, CountryCode.GD, CountryCode.KN, CountryCode.LC, CountryCode.MS, CountryCode.VC),
    XDR("SDR (Special Drawing Right)", 960, -1, new CountryCode[0]),
    XOF("CFA Franc BCEAO", 952, 0, CountryCode.BF, CountryCode.BJ, CountryCode.CI, CountryCode.GW, CountryCode.ML, CountryCode.NE, CountryCode.SN, CountryCode.TG),
    XPD("Palladium", 964, -1, new CountryCode[0]){

        public boolean isPreciousMetal() {
            return true;
        }
    }
    ,
    XPF("CFP Franc", 953, 0, CountryCode.NC, CountryCode.PF, CountryCode.WF),
    XPT("Platinum", 962, -1, new CountryCode[0]){

        public boolean isPreciousMetal() {
            return true;
        }
    }
    ,
    XSU("Sucre", 994, -1, new CountryCode[0]),
    XTS("Codes specifically reserved for testing purposes", 963, -1, new CountryCode[0]),
    XUA("ADB Unit of Account", 965, -1, new CountryCode[0]),
    XXX("The codes assigned for transactions where no currency is involved", 999, -1, new CountryCode[0]),
    YER("Yemeni Rial", 886, 2, CountryCode.YE),
    ZAR("Rand", 710, 2, CountryCode.LS, CountryCode.NA, CountryCode.ZA),
    ZMW("Zambian Kwacha", 967, 2, CountryCode.ZM),
    ZWL("Zimbabwe Dollar", 932, 2, CountryCode.ZW);

    private static final Map<Integer, CurrencyCode> numericMap;
    private final String name;
    private final int numeric;
    private final int minorUnit;
    private final List<CountryCode> countryList;

    private CurrencyCode(String name, int numeric, int minorUnit, CountryCode ... countries) {
        this.name = name;
        this.numeric = numeric;
        this.minorUnit = minorUnit;
        this.countryList = Collections.unmodifiableList(Arrays.asList(countries));
    }

    public String getName() {
        return this.name;
    }

    public int getNumeric() {
        return this.numeric;
    }

    public int getMinorUnit() {
        return this.minorUnit;
    }

    public List<CountryCode> getCountryList() {
        return this.countryList;
    }

    public boolean isFund() {
        return false;
    }

    public boolean isPreciousMetal() {
        return false;
    }

    public Currency getCurrency() {
        try {
            return Currency.getInstance(this.name());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static CurrencyCode getByCode(String code) {
        return CurrencyCode.getByCode(code, true);
    }

    public static CurrencyCode getByCodeIgnoreCase(String code) {
        return CurrencyCode.getByCode(code, false);
    }

    public static CurrencyCode getByCode(String code, boolean caseSensitive) {
        if ((code = CurrencyCode.canonicalize(code, caseSensitive)) == null) {
            return null;
        }
        try {
            return Enum.valueOf(CurrencyCode.class, code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static CurrencyCode getByCode(int code) {
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
        return code.toUpperCase();
    }

    public static List<CurrencyCode> getByCountry(String country) {
        return CurrencyCode.getByCountry(country, true);
    }

    public static List<CurrencyCode> getByCountryIgnoreCase(String country) {
        return CurrencyCode.getByCountry(country, false);
    }

    public static List<CurrencyCode> getByCountry(String country, boolean caseSensitive) {
        return CurrencyCode.getByCountry(CountryCode.getByCode(country, caseSensitive));
    }

    public static List<CurrencyCode> getByCountry(CountryCode country) {
        ArrayList<CurrencyCode> list = new ArrayList<CurrencyCode>();
        if (country == null) {
            return list;
        }
        for (CurrencyCode currency : CurrencyCode.values()) {
            for (CountryCode cc : currency.countryList) {
                if (cc != country) continue;
                list.add(currency);
            }
        }
        return list;
    }

    public static List<CurrencyCode> findByName(String regex) {
        if (regex == null) {
            throw new IllegalArgumentException("regex is null.");
        }
        Pattern pattern = Pattern.compile(regex);
        return CurrencyCode.findByName(pattern);
    }

    public static List<CurrencyCode> findByName(Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern is null.");
        }
        ArrayList<CurrencyCode> list = new ArrayList<CurrencyCode>();
        for (CurrencyCode entry : CurrencyCode.values()) {
            if (!pattern.matcher(entry.getName()).matches()) continue;
            list.add(entry);
        }
        return list;
    }

    static {
        numericMap = new HashMap<Integer, CurrencyCode>();
        for (CurrencyCode cc : CurrencyCode.values()) {
            numericMap.put(cc.getNumeric(), cc);
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.text;

import com.ibm.icu.impl.PatternProps;
import com.ibm.icu.impl.Utility;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.DecimalFormatSymbols;
import com.ibm.icu.text.MessagePattern;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.PluralFormat;
import com.ibm.icu.text.PluralRules;
import com.ibm.icu.text.RuleBasedNumberFormat;
import com.ibm.icu.text.SelectFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.text.UFormat;
import com.ibm.icu.util.ULocale;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.text.ChoiceFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class MessageFormat
extends UFormat {
    static final long serialVersionUID = 7136212545847378652L;
    private transient ULocale ulocale;
    private transient MessagePattern msgPattern;
    private transient Map<Integer, Format> cachedFormatters;
    private transient Set<Integer> customFormatArgStarts;
    private transient Format stockDateFormatter;
    private transient Format stockNumberFormatter;
    private transient PluralSelectorProvider pluralProvider;
    private transient PluralSelectorProvider ordinalProvider;
    private static final String[] typeList = new String[]{"number", "date", "time", "spellout", "ordinal", "duration"};
    private static final int TYPE_NUMBER = 0;
    private static final int TYPE_DATE = 1;
    private static final int TYPE_TIME = 2;
    private static final int TYPE_SPELLOUT = 3;
    private static final int TYPE_ORDINAL = 4;
    private static final int TYPE_DURATION = 5;
    private static final String[] modifierList = new String[]{"", "currency", "percent", "integer"};
    private static final int MODIFIER_EMPTY = 0;
    private static final int MODIFIER_CURRENCY = 1;
    private static final int MODIFIER_PERCENT = 2;
    private static final int MODIFIER_INTEGER = 3;
    private static final String[] dateModifierList = new String[]{"", "short", "medium", "long", "full"};
    private static final int DATE_MODIFIER_EMPTY = 0;
    private static final int DATE_MODIFIER_SHORT = 1;
    private static final int DATE_MODIFIER_MEDIUM = 2;
    private static final int DATE_MODIFIER_LONG = 3;
    private static final int DATE_MODIFIER_FULL = 4;
    private static final Locale rootLocale = new Locale("");
    private static final char SINGLE_QUOTE = '\'';
    private static final char CURLY_BRACE_LEFT = '{';
    private static final char CURLY_BRACE_RIGHT = '}';
    private static final int STATE_INITIAL = 0;
    private static final int STATE_SINGLE_QUOTE = 1;
    private static final int STATE_IN_QUOTE = 2;
    private static final int STATE_MSG_ELEMENT = 3;

    public MessageFormat(String pattern) {
        this.ulocale = ULocale.getDefault(ULocale.Category.FORMAT);
        this.applyPattern(pattern);
    }

    public MessageFormat(String pattern, Locale locale) {
        this(pattern, ULocale.forLocale(locale));
    }

    public MessageFormat(String pattern, ULocale locale) {
        this.ulocale = locale;
        this.applyPattern(pattern);
    }

    public void setLocale(Locale locale) {
        this.setLocale(ULocale.forLocale(locale));
    }

    public void setLocale(ULocale locale) {
        String existingPattern = this.toPattern();
        this.ulocale = locale;
        this.stockDateFormatter = null;
        this.stockNumberFormatter = null;
        this.pluralProvider = null;
        this.ordinalProvider = null;
        this.applyPattern(existingPattern);
    }

    public Locale getLocale() {
        return this.ulocale.toLocale();
    }

    public ULocale getULocale() {
        return this.ulocale;
    }

    public void applyPattern(String pttrn) {
        try {
            if (this.msgPattern == null) {
                this.msgPattern = new MessagePattern(pttrn);
            } else {
                this.msgPattern.parse(pttrn);
            }
            this.cacheExplicitFormats();
        } catch (RuntimeException e) {
            this.resetPattern();
            throw e;
        }
    }

    public void applyPattern(String pattern, MessagePattern.ApostropheMode aposMode) {
        if (this.msgPattern == null) {
            this.msgPattern = new MessagePattern(aposMode);
        } else if (aposMode != this.msgPattern.getApostropheMode()) {
            this.msgPattern.clearPatternAndSetApostropheMode(aposMode);
        }
        this.applyPattern(pattern);
    }

    public MessagePattern.ApostropheMode getApostropheMode() {
        if (this.msgPattern == null) {
            this.msgPattern = new MessagePattern();
        }
        return this.msgPattern.getApostropheMode();
    }

    public String toPattern() {
        if (this.customFormatArgStarts != null) {
            throw new IllegalStateException("toPattern() is not supported after custom Format objects have been set via setFormat() or similar APIs");
        }
        if (this.msgPattern == null) {
            return "";
        }
        String originalPattern = this.msgPattern.getPatternString();
        return originalPattern == null ? "" : originalPattern;
    }

    private int nextTopLevelArgStart(int partIndex) {
        MessagePattern.Part.Type type;
        if (partIndex != 0) {
            partIndex = this.msgPattern.getLimitPartIndex(partIndex);
        }
        do {
            if ((type = this.msgPattern.getPartType(++partIndex)) != MessagePattern.Part.Type.ARG_START) continue;
            return partIndex;
        } while (type != MessagePattern.Part.Type.MSG_LIMIT);
        return -1;
    }

    private boolean argNameMatches(int partIndex, String argName, int argNumber) {
        MessagePattern.Part part = this.msgPattern.getPart(partIndex);
        return part.getType() == MessagePattern.Part.Type.ARG_NAME ? this.msgPattern.partSubstringMatches(part, argName) : part.getValue() == argNumber;
    }

    private String getArgName(int partIndex) {
        MessagePattern.Part part = this.msgPattern.getPart(partIndex);
        if (part.getType() == MessagePattern.Part.Type.ARG_NAME) {
            return this.msgPattern.getSubstring(part);
        }
        return Integer.toString(part.getValue());
    }

    public void setFormatsByArgumentIndex(Format[] newFormats) {
        if (this.msgPattern.hasNamedArguments()) {
            throw new IllegalArgumentException("This method is not available in MessageFormat objects that use alphanumeric argument names.");
        }
        int partIndex = 0;
        while ((partIndex = this.nextTopLevelArgStart(partIndex)) >= 0) {
            int argNumber = this.msgPattern.getPart(partIndex + 1).getValue();
            if (argNumber >= newFormats.length) continue;
            this.setCustomArgStartFormat(partIndex, newFormats[argNumber]);
        }
    }

    public void setFormatsByArgumentName(Map<String, Format> newFormats) {
        int partIndex = 0;
        while ((partIndex = this.nextTopLevelArgStart(partIndex)) >= 0) {
            String key = this.getArgName(partIndex + 1);
            if (!newFormats.containsKey(key)) continue;
            this.setCustomArgStartFormat(partIndex, newFormats.get(key));
        }
    }

    public void setFormats(Format[] newFormats) {
        int partIndex = 0;
        for (int formatNumber = 0; formatNumber < newFormats.length && (partIndex = this.nextTopLevelArgStart(partIndex)) >= 0; ++formatNumber) {
            this.setCustomArgStartFormat(partIndex, newFormats[formatNumber]);
        }
    }

    public void setFormatByArgumentIndex(int argumentIndex, Format newFormat) {
        if (this.msgPattern.hasNamedArguments()) {
            throw new IllegalArgumentException("This method is not available in MessageFormat objects that use alphanumeric argument names.");
        }
        int partIndex = 0;
        while ((partIndex = this.nextTopLevelArgStart(partIndex)) >= 0) {
            if (this.msgPattern.getPart(partIndex + 1).getValue() != argumentIndex) continue;
            this.setCustomArgStartFormat(partIndex, newFormat);
        }
    }

    public void setFormatByArgumentName(String argumentName, Format newFormat) {
        int argNumber = MessagePattern.validateArgumentName(argumentName);
        if (argNumber < -1) {
            return;
        }
        int partIndex = 0;
        while ((partIndex = this.nextTopLevelArgStart(partIndex)) >= 0) {
            if (!this.argNameMatches(partIndex + 1, argumentName, argNumber)) continue;
            this.setCustomArgStartFormat(partIndex, newFormat);
        }
    }

    public void setFormat(int formatElementIndex, Format newFormat) {
        int formatNumber = 0;
        int partIndex = 0;
        while ((partIndex = this.nextTopLevelArgStart(partIndex)) >= 0) {
            if (formatNumber == formatElementIndex) {
                this.setCustomArgStartFormat(partIndex, newFormat);
                return;
            }
            ++formatNumber;
        }
        throw new ArrayIndexOutOfBoundsException(formatElementIndex);
    }

    public Format[] getFormatsByArgumentIndex() {
        if (this.msgPattern.hasNamedArguments()) {
            throw new IllegalArgumentException("This method is not available in MessageFormat objects that use alphanumeric argument names.");
        }
        ArrayList<Format> list = new ArrayList<Format>();
        int partIndex = 0;
        while ((partIndex = this.nextTopLevelArgStart(partIndex)) >= 0) {
            int argNumber = this.msgPattern.getPart(partIndex + 1).getValue();
            while (argNumber >= list.size()) {
                list.add(null);
            }
            list.set(argNumber, this.cachedFormatters == null ? null : this.cachedFormatters.get(partIndex));
        }
        return list.toArray(new Format[list.size()]);
    }

    public Format[] getFormats() {
        ArrayList<Format> list = new ArrayList<Format>();
        int partIndex = 0;
        while ((partIndex = this.nextTopLevelArgStart(partIndex)) >= 0) {
            list.add(this.cachedFormatters == null ? null : this.cachedFormatters.get(partIndex));
        }
        return list.toArray(new Format[list.size()]);
    }

    public Set<String> getArgumentNames() {
        HashSet<String> result = new HashSet<String>();
        int partIndex = 0;
        while ((partIndex = this.nextTopLevelArgStart(partIndex)) >= 0) {
            result.add(this.getArgName(partIndex + 1));
        }
        return result;
    }

    public Format getFormatByArgumentName(String argumentName) {
        if (this.cachedFormatters == null) {
            return null;
        }
        int argNumber = MessagePattern.validateArgumentName(argumentName);
        if (argNumber < -1) {
            return null;
        }
        int partIndex = 0;
        while ((partIndex = this.nextTopLevelArgStart(partIndex)) >= 0) {
            if (!this.argNameMatches(partIndex + 1, argumentName, argNumber)) continue;
            return this.cachedFormatters.get(partIndex);
        }
        return null;
    }

    public final StringBuffer format(Object[] arguments, StringBuffer result, FieldPosition pos) {
        this.format(arguments, null, new AppendableWrapper(result), pos);
        return result;
    }

    public final StringBuffer format(Map<String, Object> arguments, StringBuffer result, FieldPosition pos) {
        this.format(null, arguments, new AppendableWrapper(result), pos);
        return result;
    }

    public static String format(String pattern, Object ... arguments) {
        MessageFormat temp = new MessageFormat(pattern);
        return temp.format(arguments);
    }

    public static String format(String pattern, Map<String, Object> arguments) {
        MessageFormat temp = new MessageFormat(pattern);
        return temp.format(arguments);
    }

    public boolean usesNamedArguments() {
        return this.msgPattern.hasNamedArguments();
    }

    @Override
    public final StringBuffer format(Object arguments, StringBuffer result, FieldPosition pos) {
        this.format(arguments, new AppendableWrapper(result), pos);
        return result;
    }

    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object arguments) {
        if (arguments == null) {
            throw new NullPointerException("formatToCharacterIterator must be passed non-null object");
        }
        StringBuilder result = new StringBuilder();
        AppendableWrapper wrapper = new AppendableWrapper(result);
        wrapper.useAttributes();
        this.format(arguments, wrapper, null);
        AttributedString as = new AttributedString(result.toString());
        for (AttributeAndPosition a : wrapper.attributes) {
            as.addAttribute(a.key, a.value, a.start, a.limit);
        }
        return as.getIterator();
    }

    public Object[] parse(String source, ParsePosition pos) {
        if (this.msgPattern.hasNamedArguments()) {
            throw new IllegalArgumentException("This method is not available in MessageFormat objects that use named argument.");
        }
        int maxArgId = -1;
        int partIndex = 0;
        while ((partIndex = this.nextTopLevelArgStart(partIndex)) >= 0) {
            int argNumber = this.msgPattern.getPart(partIndex + 1).getValue();
            if (argNumber <= maxArgId) continue;
            maxArgId = argNumber;
        }
        Object[] resultArray = new Object[maxArgId + 1];
        int backupStartPos = pos.getIndex();
        this.parse(0, source, pos, resultArray, null);
        if (pos.getIndex() == backupStartPos) {
            return null;
        }
        return resultArray;
    }

    public Map<String, Object> parseToMap(String source, ParsePosition pos) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        int backupStartPos = pos.getIndex();
        this.parse(0, source, pos, null, result);
        if (pos.getIndex() == backupStartPos) {
            return null;
        }
        return result;
    }

    public Object[] parse(String source) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Object[] result = this.parse(source, pos);
        if (pos.getIndex() == 0) {
            throw new ParseException("MessageFormat parse error!", pos.getErrorIndex());
        }
        return result;
    }

    private void parse(int msgStart, String source, ParsePosition pos, Object[] args, Map<String, Object> argsMap) {
        if (source == null) {
            return;
        }
        String msgString = this.msgPattern.getPatternString();
        int prevIndex = this.msgPattern.getPart(msgStart).getLimit();
        int sourceOffset = pos.getIndex();
        ParsePosition tempStatus = new ParsePosition(0);
        int i = msgStart + 1;
        while (true) {
            MessagePattern.Part part = this.msgPattern.getPart(i);
            MessagePattern.Part.Type type = part.getType();
            int index = part.getIndex();
            int len = index - prevIndex;
            if (len == 0 || msgString.regionMatches(prevIndex, source, sourceOffset, len)) {
                sourceOffset += len;
                prevIndex += len;
            } else {
                pos.setErrorIndex(sourceOffset);
                return;
            }
            if (type == MessagePattern.Part.Type.MSG_LIMIT) {
                pos.setIndex(sourceOffset);
                return;
            }
            if (type == MessagePattern.Part.Type.SKIP_SYNTAX || type == MessagePattern.Part.Type.INSERT_CHAR) {
                prevIndex = part.getLimit();
            } else {
                assert (type == MessagePattern.Part.Type.ARG_START) : "Unexpected Part " + part + " in parsed message.";
                int argLimit = this.msgPattern.getLimitPartIndex(i);
                MessagePattern.ArgType argType = part.getArgType();
                part = this.msgPattern.getPart(++i);
                Object argId = null;
                int argNumber = 0;
                String key = null;
                if (args != null) {
                    argNumber = part.getValue();
                    argId = argNumber;
                } else {
                    key = part.getType() == MessagePattern.Part.Type.ARG_NAME ? this.msgPattern.getSubstring(part) : Integer.toString(part.getValue());
                    argId = key;
                }
                Format formatter = null;
                boolean haveArgResult = false;
                Object argResult = null;
                if (this.cachedFormatters != null && (formatter = this.cachedFormatters.get(++i - 2)) != null) {
                    tempStatus.setIndex(sourceOffset);
                    argResult = formatter.parseObject(source, tempStatus);
                    if (tempStatus.getIndex() == sourceOffset) {
                        pos.setErrorIndex(sourceOffset);
                        return;
                    }
                    haveArgResult = true;
                    sourceOffset = tempStatus.getIndex();
                } else if (argType == MessagePattern.ArgType.NONE || this.cachedFormatters != null && this.cachedFormatters.containsKey(i - 2)) {
                    String stringAfterArgument = this.getLiteralStringUntilNextArgument(argLimit);
                    int next = stringAfterArgument.length() != 0 ? source.indexOf(stringAfterArgument, sourceOffset) : source.length();
                    if (next < 0) {
                        pos.setErrorIndex(sourceOffset);
                        return;
                    }
                    String strValue = source.substring(sourceOffset, next);
                    if (!strValue.equals("{" + argId.toString() + "}")) {
                        haveArgResult = true;
                        argResult = strValue;
                    }
                    sourceOffset = next;
                } else if (argType == MessagePattern.ArgType.CHOICE) {
                    tempStatus.setIndex(sourceOffset);
                    double choiceResult = MessageFormat.parseChoiceArgument(this.msgPattern, i, source, tempStatus);
                    if (tempStatus.getIndex() == sourceOffset) {
                        pos.setErrorIndex(sourceOffset);
                        return;
                    }
                    argResult = choiceResult;
                    haveArgResult = true;
                    sourceOffset = tempStatus.getIndex();
                } else {
                    if (argType.hasPluralStyle() || argType == MessagePattern.ArgType.SELECT) {
                        throw new UnsupportedOperationException("Parsing of plural/select/selectordinal argument is not supported.");
                    }
                    throw new IllegalStateException("unexpected argType " + (Object)((Object)argType));
                }
                if (haveArgResult) {
                    if (args != null) {
                        args[argNumber] = argResult;
                    } else if (argsMap != null) {
                        argsMap.put(key, argResult);
                    }
                }
                prevIndex = this.msgPattern.getPart(argLimit).getLimit();
                i = argLimit;
            }
            ++i;
        }
    }

    public Map<String, Object> parseToMap(String source) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        HashMap<String, Object> result = new HashMap<String, Object>();
        this.parse(0, source, pos, null, result);
        if (pos.getIndex() == 0) {
            throw new ParseException("MessageFormat parse error!", pos.getErrorIndex());
        }
        return result;
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        if (!this.msgPattern.hasNamedArguments()) {
            return this.parse(source, pos);
        }
        return this.parseToMap(source, pos);
    }

    @Override
    public Object clone() {
        MessageFormat other = (MessageFormat)super.clone();
        if (this.customFormatArgStarts != null) {
            other.customFormatArgStarts = new HashSet<Integer>();
            for (Integer key : this.customFormatArgStarts) {
                other.customFormatArgStarts.add(key);
            }
        } else {
            other.customFormatArgStarts = null;
        }
        if (this.cachedFormatters != null) {
            other.cachedFormatters = new HashMap<Integer, Format>();
            for (Map.Entry<Integer, Format> entry : this.cachedFormatters.entrySet()) {
                other.cachedFormatters.put(entry.getKey(), entry.getValue());
            }
        } else {
            other.cachedFormatters = null;
        }
        other.msgPattern = this.msgPattern == null ? null : (MessagePattern)this.msgPattern.clone();
        other.stockDateFormatter = this.stockDateFormatter == null ? null : (Format)this.stockDateFormatter.clone();
        other.stockNumberFormatter = this.stockNumberFormatter == null ? null : (Format)this.stockNumberFormatter.clone();
        other.pluralProvider = null;
        other.ordinalProvider = null;
        return other;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        MessageFormat other = (MessageFormat)obj;
        return Utility.objectEquals(this.ulocale, other.ulocale) && Utility.objectEquals(this.msgPattern, other.msgPattern) && Utility.objectEquals(this.cachedFormatters, other.cachedFormatters) && Utility.objectEquals(this.customFormatArgStarts, other.customFormatArgStarts);
    }

    public int hashCode() {
        return this.msgPattern.getPatternString().hashCode();
    }

    private void format(int msgStart, double pluralNumber, Object[] args, Map<String, Object> argsMap, AppendableWrapper dest, FieldPosition fp) {
        String msgString = this.msgPattern.getPatternString();
        int prevIndex = this.msgPattern.getPart(msgStart).getLimit();
        int i = msgStart + 1;
        while (true) {
            MessagePattern.Part part = this.msgPattern.getPart(i);
            MessagePattern.Part.Type type = part.getType();
            int index = part.getIndex();
            dest.append(msgString, prevIndex, index);
            if (type == MessagePattern.Part.Type.MSG_LIMIT) {
                return;
            }
            prevIndex = part.getLimit();
            if (type == MessagePattern.Part.Type.REPLACE_NUMBER) {
                if (this.stockNumberFormatter == null) {
                    this.stockNumberFormatter = NumberFormat.getInstance(this.ulocale);
                }
                dest.formatAndAppend(this.stockNumberFormatter, pluralNumber);
            } else if (type == MessagePattern.Part.Type.ARG_START) {
                Object arg;
                int argLimit = this.msgPattern.getLimitPartIndex(i);
                MessagePattern.ArgType argType = part.getArgType();
                part = this.msgPattern.getPart(++i);
                String noArg = null;
                Object argId = null;
                if (args != null) {
                    int argNumber = part.getValue();
                    if (dest.attributes != null) {
                        argId = argNumber;
                    }
                    if (0 <= argNumber && argNumber < args.length) {
                        arg = args[argNumber];
                    } else {
                        arg = null;
                        noArg = "{" + argNumber + "}";
                    }
                } else {
                    String key = part.getType() == MessagePattern.Part.Type.ARG_NAME ? this.msgPattern.getSubstring(part) : Integer.toString(part.getValue());
                    argId = key;
                    if (argsMap != null && argsMap.containsKey(key)) {
                        arg = argsMap.get(key);
                    } else {
                        arg = null;
                        noArg = "{" + key + "}";
                    }
                }
                ++i;
                int prevDestLength = dest.length;
                Format formatter = null;
                if (noArg != null) {
                    dest.append(noArg);
                } else if (arg == null) {
                    dest.append("null");
                } else if (this.cachedFormatters != null && (formatter = this.cachedFormatters.get(i - 2)) != null) {
                    if (formatter instanceof ChoiceFormat || formatter instanceof PluralFormat || formatter instanceof SelectFormat) {
                        String subMsgString = formatter.format(arg);
                        if (subMsgString.indexOf(123) >= 0 || subMsgString.indexOf(39) >= 0 && !this.msgPattern.jdkAposMode()) {
                            MessageFormat subMsgFormat = new MessageFormat(subMsgString, this.ulocale);
                            subMsgFormat.format(0, 0.0, args, argsMap, dest, null);
                        } else if (dest.attributes == null) {
                            dest.append(subMsgString);
                        } else {
                            dest.formatAndAppend(formatter, arg);
                        }
                    } else {
                        dest.formatAndAppend(formatter, arg);
                    }
                } else if (argType == MessagePattern.ArgType.NONE || this.cachedFormatters != null && this.cachedFormatters.containsKey(i - 2)) {
                    if (arg instanceof Number) {
                        if (this.stockNumberFormatter == null) {
                            this.stockNumberFormatter = NumberFormat.getInstance(this.ulocale);
                        }
                        dest.formatAndAppend(this.stockNumberFormatter, arg);
                    } else if (arg instanceof Date) {
                        if (this.stockDateFormatter == null) {
                            this.stockDateFormatter = DateFormat.getDateTimeInstance(3, 3, this.ulocale);
                        }
                        dest.formatAndAppend(this.stockDateFormatter, arg);
                    } else {
                        dest.append(arg.toString());
                    }
                } else if (argType == MessagePattern.ArgType.CHOICE) {
                    if (!(arg instanceof Number)) {
                        throw new IllegalArgumentException("'" + arg + "' is not a Number");
                    }
                    double number = ((Number)arg).doubleValue();
                    int subMsgStart = MessageFormat.findChoiceSubMessage(this.msgPattern, i, number);
                    this.formatComplexSubMessage(subMsgStart, 0.0, args, argsMap, dest);
                } else if (argType.hasPluralStyle()) {
                    PluralSelectorProvider selector;
                    if (!(arg instanceof Number)) {
                        throw new IllegalArgumentException("'" + arg + "' is not a Number");
                    }
                    double number = ((Number)arg).doubleValue();
                    if (argType == MessagePattern.ArgType.PLURAL) {
                        if (this.pluralProvider == null) {
                            this.pluralProvider = new PluralSelectorProvider(this.ulocale, PluralRules.PluralType.CARDINAL);
                        }
                        selector = this.pluralProvider;
                    } else {
                        if (this.ordinalProvider == null) {
                            this.ordinalProvider = new PluralSelectorProvider(this.ulocale, PluralRules.PluralType.ORDINAL);
                        }
                        selector = this.ordinalProvider;
                    }
                    int subMsgStart = PluralFormat.findSubMessage(this.msgPattern, i, selector, number);
                    double offset = this.msgPattern.getPluralOffset(i);
                    this.formatComplexSubMessage(subMsgStart, number - offset, args, argsMap, dest);
                } else if (argType == MessagePattern.ArgType.SELECT) {
                    int subMsgStart = SelectFormat.findSubMessage(this.msgPattern, i, arg.toString());
                    this.formatComplexSubMessage(subMsgStart, 0.0, args, argsMap, dest);
                } else {
                    throw new IllegalStateException("unexpected argType " + (Object)((Object)argType));
                }
                fp = this.updateMetaData(dest, prevDestLength, fp, argId);
                prevIndex = this.msgPattern.getPart(argLimit).getLimit();
                i = argLimit;
            }
            ++i;
        }
    }

    private void formatComplexSubMessage(int msgStart, double pluralNumber, Object[] args, Map<String, Object> argsMap, AppendableWrapper dest) {
        String subMsgString;
        if (!this.msgPattern.jdkAposMode()) {
            this.format(msgStart, pluralNumber, args, argsMap, dest, null);
            return;
        }
        String msgString = this.msgPattern.getPatternString();
        StringBuilder sb = null;
        int prevIndex = this.msgPattern.getPart(msgStart).getLimit();
        int i = msgStart;
        while (true) {
            MessagePattern.Part part = this.msgPattern.getPart(++i);
            MessagePattern.Part.Type type = part.getType();
            int index = part.getIndex();
            if (type == MessagePattern.Part.Type.MSG_LIMIT) {
                if (sb == null) {
                    subMsgString = msgString.substring(prevIndex, index);
                    break;
                }
                subMsgString = sb.append(msgString, prevIndex, index).toString();
                break;
            }
            if (type == MessagePattern.Part.Type.REPLACE_NUMBER || type == MessagePattern.Part.Type.SKIP_SYNTAX) {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append(msgString, prevIndex, index);
                if (type == MessagePattern.Part.Type.REPLACE_NUMBER) {
                    if (this.stockNumberFormatter == null) {
                        this.stockNumberFormatter = NumberFormat.getInstance(this.ulocale);
                    }
                    sb.append(this.stockNumberFormatter.format(pluralNumber));
                }
                prevIndex = part.getLimit();
                continue;
            }
            if (type != MessagePattern.Part.Type.ARG_START) continue;
            if (sb == null) {
                sb = new StringBuilder();
            }
            sb.append(msgString, prevIndex, index);
            prevIndex = index;
            i = this.msgPattern.getLimitPartIndex(i);
            index = this.msgPattern.getPart(i).getLimit();
            MessagePattern.appendReducedApostrophes(msgString, prevIndex, index, sb);
            prevIndex = index;
        }
        if (subMsgString.indexOf(123) >= 0) {
            MessageFormat subMsgFormat = new MessageFormat("", this.ulocale);
            subMsgFormat.applyPattern(subMsgString, MessagePattern.ApostropheMode.DOUBLE_REQUIRED);
            subMsgFormat.format(0, 0.0, args, argsMap, dest, null);
        } else {
            dest.append(subMsgString);
        }
    }

    private String getLiteralStringUntilNextArgument(int from) {
        StringBuilder b = new StringBuilder();
        String msgString = this.msgPattern.getPatternString();
        int prevIndex = this.msgPattern.getPart(from).getLimit();
        int i = from + 1;
        while (true) {
            MessagePattern.Part part = this.msgPattern.getPart(i);
            MessagePattern.Part.Type type = part.getType();
            int index = part.getIndex();
            b.append(msgString, prevIndex, index);
            if (type == MessagePattern.Part.Type.ARG_START || type == MessagePattern.Part.Type.MSG_LIMIT) {
                return b.toString();
            }
            assert (type == MessagePattern.Part.Type.SKIP_SYNTAX || type == MessagePattern.Part.Type.INSERT_CHAR) : "Unexpected Part " + part + " in parsed message.";
            prevIndex = part.getLimit();
            ++i;
        }
    }

    private FieldPosition updateMetaData(AppendableWrapper dest, int prevLength, FieldPosition fp, Object argId) {
        if (dest.attributes != null && prevLength < dest.length) {
            dest.attributes.add(new AttributeAndPosition(argId, prevLength, dest.length));
        }
        if (fp != null && Field.ARGUMENT.equals(fp.getFieldAttribute())) {
            fp.setBeginIndex(prevLength);
            fp.setEndIndex(dest.length);
            return null;
        }
        return fp;
    }

    private static int findChoiceSubMessage(MessagePattern pattern, int partIndex, double number) {
        int msgStart;
        double boundary;
        int selectorIndex;
        char boundaryChar;
        int count = pattern.countParts();
        partIndex += 2;
        do {
            MessagePattern.Part part;
            MessagePattern.Part.Type type;
            msgStart = partIndex;
            partIndex = pattern.getLimitPartIndex(partIndex);
            if (++partIndex >= count || (type = (part = pattern.getPart(partIndex++)).getType()) == MessagePattern.Part.Type.ARG_LIMIT) break;
            assert (type.hasNumericValue());
            boundary = pattern.getNumericValue(part);
            selectorIndex = pattern.getPatternIndex(partIndex++);
        } while (!((boundaryChar = pattern.getPatternString().charAt(selectorIndex)) == '<' ? !(number > boundary) : !(number >= boundary)));
        return msgStart;
    }

    private static double parseChoiceArgument(MessagePattern pattern, int partIndex, String source, ParsePosition pos) {
        int start;
        int furthest = start = pos.getIndex();
        double bestNumber = Double.NaN;
        double tempNumber = 0.0;
        while (pattern.getPartType(partIndex) != MessagePattern.Part.Type.ARG_LIMIT) {
            int newIndex;
            int msgLimit;
            int len;
            tempNumber = pattern.getNumericValue(pattern.getPart(partIndex));
            if ((len = MessageFormat.matchStringUntilLimitPart(pattern, partIndex += 2, msgLimit = pattern.getLimitPartIndex(partIndex), source, start)) >= 0 && (newIndex = start + len) > furthest) {
                furthest = newIndex;
                bestNumber = tempNumber;
                if (furthest == source.length()) break;
            }
            partIndex = msgLimit + 1;
        }
        if (furthest == start) {
            pos.setErrorIndex(start);
        } else {
            pos.setIndex(furthest);
        }
        return bestNumber;
    }

    private static int matchStringUntilLimitPart(MessagePattern pattern, int partIndex, int limitPartIndex, String source, int sourceOffset) {
        int matchingSourceLength = 0;
        String msgString = pattern.getPatternString();
        int prevIndex = pattern.getPart(partIndex).getLimit();
        while (true) {
            MessagePattern.Part part = pattern.getPart(++partIndex);
            if (partIndex != limitPartIndex && part.getType() != MessagePattern.Part.Type.SKIP_SYNTAX) continue;
            int index = part.getIndex();
            int length = index - prevIndex;
            if (length != 0 && !source.regionMatches(sourceOffset, msgString, prevIndex, length)) {
                return -1;
            }
            matchingSourceLength += length;
            if (partIndex == limitPartIndex) {
                return matchingSourceLength;
            }
            prevIndex = part.getLimit();
        }
    }

    private void format(Object arguments, AppendableWrapper result, FieldPosition fp) {
        if (arguments == null || arguments instanceof Map) {
            this.format(null, (Map)arguments, result, fp);
        } else {
            this.format((Object[])arguments, null, result, fp);
        }
    }

    private void format(Object[] arguments, Map<String, Object> argsMap, AppendableWrapper dest, FieldPosition fp) {
        if (arguments != null && this.msgPattern.hasNamedArguments()) {
            throw new IllegalArgumentException("This method is not available in MessageFormat objects that use alphanumeric argument names.");
        }
        this.format(0, 0.0, arguments, argsMap, dest, fp);
    }

    private void resetPattern() {
        if (this.msgPattern != null) {
            this.msgPattern.clear();
        }
        if (this.cachedFormatters != null) {
            this.cachedFormatters.clear();
        }
        this.customFormatArgStarts = null;
    }

    private Format createAppropriateFormat(String type, String style) {
        UFormat newFormat = null;
        int subformatType = MessageFormat.findKeyword(type, typeList);
        block3 : switch (subformatType) {
            case 0: {
                switch (MessageFormat.findKeyword(style, modifierList)) {
                    case 0: {
                        newFormat = NumberFormat.getInstance(this.ulocale);
                        break block3;
                    }
                    case 1: {
                        newFormat = NumberFormat.getCurrencyInstance(this.ulocale);
                        break block3;
                    }
                    case 2: {
                        newFormat = NumberFormat.getPercentInstance(this.ulocale);
                        break block3;
                    }
                    case 3: {
                        newFormat = NumberFormat.getIntegerInstance(this.ulocale);
                        break block3;
                    }
                }
                newFormat = new DecimalFormat(style, new DecimalFormatSymbols(this.ulocale));
                break;
            }
            case 1: {
                switch (MessageFormat.findKeyword(style, dateModifierList)) {
                    case 0: {
                        newFormat = DateFormat.getDateInstance(2, this.ulocale);
                        break block3;
                    }
                    case 1: {
                        newFormat = DateFormat.getDateInstance(3, this.ulocale);
                        break block3;
                    }
                    case 2: {
                        newFormat = DateFormat.getDateInstance(2, this.ulocale);
                        break block3;
                    }
                    case 3: {
                        newFormat = DateFormat.getDateInstance(1, this.ulocale);
                        break block3;
                    }
                    case 4: {
                        newFormat = DateFormat.getDateInstance(0, this.ulocale);
                        break block3;
                    }
                }
                newFormat = new SimpleDateFormat(style, this.ulocale);
                break;
            }
            case 2: {
                switch (MessageFormat.findKeyword(style, dateModifierList)) {
                    case 0: {
                        newFormat = DateFormat.getTimeInstance(2, this.ulocale);
                        break block3;
                    }
                    case 1: {
                        newFormat = DateFormat.getTimeInstance(3, this.ulocale);
                        break block3;
                    }
                    case 2: {
                        newFormat = DateFormat.getTimeInstance(2, this.ulocale);
                        break block3;
                    }
                    case 3: {
                        newFormat = DateFormat.getTimeInstance(1, this.ulocale);
                        break block3;
                    }
                    case 4: {
                        newFormat = DateFormat.getTimeInstance(0, this.ulocale);
                        break block3;
                    }
                }
                newFormat = new SimpleDateFormat(style, this.ulocale);
                break;
            }
            case 3: {
                RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(this.ulocale, 1);
                String ruleset = style.trim();
                if (ruleset.length() != 0) {
                    try {
                        rbnf.setDefaultRuleSet(ruleset);
                    } catch (Exception e) {
                        // empty catch block
                    }
                }
                newFormat = rbnf;
                break;
            }
            case 4: {
                RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(this.ulocale, 2);
                String ruleset = style.trim();
                if (ruleset.length() != 0) {
                    try {
                        rbnf.setDefaultRuleSet(ruleset);
                    } catch (Exception e) {
                        // empty catch block
                    }
                }
                newFormat = rbnf;
                break;
            }
            case 5: {
                RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(this.ulocale, 3);
                String ruleset = style.trim();
                if (ruleset.length() != 0) {
                    try {
                        rbnf.setDefaultRuleSet(ruleset);
                    } catch (Exception e) {
                        // empty catch block
                    }
                }
                newFormat = rbnf;
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown format type \"" + type + "\"");
            }
        }
        return newFormat;
    }

    private static final int findKeyword(String s, String[] list) {
        s = PatternProps.trimWhiteSpace(s).toLowerCase(rootLocale);
        for (int i = 0; i < list.length; ++i) {
            if (!s.equals(list[i])) continue;
            return i;
        }
        return -1;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(this.ulocale.toLanguageTag());
        if (this.msgPattern == null) {
            this.msgPattern = new MessagePattern();
        }
        out.writeObject((Object)this.msgPattern.getApostropheMode());
        out.writeObject(this.msgPattern.getPatternString());
        if (this.customFormatArgStarts == null || this.customFormatArgStarts.isEmpty()) {
            out.writeInt(0);
        } else {
            out.writeInt(this.customFormatArgStarts.size());
            int formatIndex = 0;
            int partIndex = 0;
            while ((partIndex = this.nextTopLevelArgStart(partIndex)) >= 0) {
                if (this.customFormatArgStarts.contains(partIndex)) {
                    out.writeInt(formatIndex);
                    out.writeObject(this.cachedFormatters.get(partIndex));
                }
                ++formatIndex;
            }
        }
        out.writeInt(0);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        String msg;
        in.defaultReadObject();
        String languageTag = (String)in.readObject();
        this.ulocale = ULocale.forLanguageTag(languageTag);
        MessagePattern.ApostropheMode aposMode = (MessagePattern.ApostropheMode)((Object)in.readObject());
        if (this.msgPattern == null || aposMode != this.msgPattern.getApostropheMode()) {
            this.msgPattern = new MessagePattern(aposMode);
        }
        if ((msg = (String)in.readObject()) != null) {
            this.applyPattern(msg);
        }
        for (int numFormatters = in.readInt(); numFormatters > 0; --numFormatters) {
            int formatIndex = in.readInt();
            Format formatter = (Format)in.readObject();
            this.setFormat(formatIndex, formatter);
        }
        for (int numPairs = in.readInt(); numPairs > 0; --numPairs) {
            in.readInt();
            in.readObject();
        }
    }

    private void cacheExplicitFormats() {
        if (this.cachedFormatters != null) {
            this.cachedFormatters.clear();
        }
        this.customFormatArgStarts = null;
        int limit = this.msgPattern.countParts() - 2;
        for (int i = 1; i < limit; ++i) {
            MessagePattern.ArgType argType;
            MessagePattern.Part part = this.msgPattern.getPart(i);
            if (part.getType() != MessagePattern.Part.Type.ARG_START || (argType = part.getArgType()) != MessagePattern.ArgType.SIMPLE) continue;
            int index = i;
            i += 2;
            String explicitType = this.msgPattern.getSubstring(this.msgPattern.getPart(i++));
            String style = "";
            part = this.msgPattern.getPart(i);
            if (part.getType() == MessagePattern.Part.Type.ARG_STYLE) {
                style = this.msgPattern.getSubstring(part);
                ++i;
            }
            Format formatter = this.createAppropriateFormat(explicitType, style);
            this.setArgStartFormat(index, formatter);
        }
    }

    private void setArgStartFormat(int argStart, Format formatter) {
        if (this.cachedFormatters == null) {
            this.cachedFormatters = new HashMap<Integer, Format>();
        }
        this.cachedFormatters.put(argStart, formatter);
    }

    private void setCustomArgStartFormat(int argStart, Format formatter) {
        this.setArgStartFormat(argStart, formatter);
        if (this.customFormatArgStarts == null) {
            this.customFormatArgStarts = new HashSet<Integer>();
        }
        this.customFormatArgStarts.add(argStart);
    }

    public static String autoQuoteApostrophe(String pattern) {
        StringBuilder buf = new StringBuilder(pattern.length() * 2);
        int state = 0;
        int braceCount = 0;
        int j = pattern.length();
        for (int i = 0; i < j; ++i) {
            char c = pattern.charAt(i);
            block0 : switch (state) {
                case 0: {
                    switch (c) {
                        case '\'': {
                            state = 1;
                            break;
                        }
                        case '{': {
                            state = 3;
                            ++braceCount;
                        }
                    }
                    break;
                }
                case 1: {
                    switch (c) {
                        case '\'': {
                            state = 0;
                            break block0;
                        }
                        case '{': 
                        case '}': {
                            state = 2;
                            break block0;
                        }
                    }
                    buf.append('\'');
                    state = 0;
                    break;
                }
                case 2: {
                    switch (c) {
                        case '\'': {
                            state = 0;
                        }
                    }
                    break;
                }
                case 3: {
                    switch (c) {
                        case '{': {
                            ++braceCount;
                            break;
                        }
                        case '}': {
                            if (--braceCount != 0) break;
                            state = 0;
                        }
                    }
                    break;
                }
            }
            buf.append(c);
        }
        if (state == 1 || state == 2) {
            buf.append('\'');
        }
        return new String(buf);
    }

    private static final class AttributeAndPosition {
        private AttributedCharacterIterator.Attribute key;
        private Object value;
        private int start;
        private int limit;

        public AttributeAndPosition(Object fieldValue, int startIndex, int limitIndex) {
            this.init(Field.ARGUMENT, fieldValue, startIndex, limitIndex);
        }

        public AttributeAndPosition(AttributedCharacterIterator.Attribute field, Object fieldValue, int startIndex, int limitIndex) {
            this.init(field, fieldValue, startIndex, limitIndex);
        }

        public void init(AttributedCharacterIterator.Attribute field, Object fieldValue, int startIndex, int limitIndex) {
            this.key = field;
            this.value = fieldValue;
            this.start = startIndex;
            this.limit = limitIndex;
        }
    }

    private static final class AppendableWrapper {
        private Appendable app;
        private int length;
        private List<AttributeAndPosition> attributes;

        public AppendableWrapper(StringBuilder sb) {
            this.app = sb;
            this.length = sb.length();
            this.attributes = null;
        }

        public AppendableWrapper(StringBuffer sb) {
            this.app = sb;
            this.length = sb.length();
            this.attributes = null;
        }

        public void useAttributes() {
            this.attributes = new ArrayList<AttributeAndPosition>();
        }

        public void append(CharSequence s) {
            try {
                this.app.append(s);
                this.length += s.length();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void append(CharSequence s, int start, int limit) {
            try {
                this.app.append(s, start, limit);
                this.length += limit - start;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void append(CharacterIterator iterator) {
            this.length += AppendableWrapper.append(this.app, iterator);
        }

        public static int append(Appendable result, CharacterIterator iterator) {
            try {
                int start = iterator.getBeginIndex();
                int limit = iterator.getEndIndex();
                int length = limit - start;
                if (start < limit) {
                    result.append(iterator.first());
                    while (++start < limit) {
                        result.append(iterator.next());
                    }
                }
                return length;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void formatAndAppend(Format formatter, Object arg) {
            if (this.attributes == null) {
                this.append(formatter.format(arg));
            } else {
                AttributedCharacterIterator formattedArg = formatter.formatToCharacterIterator(arg);
                int prevLength = this.length;
                this.append(formattedArg);
                formattedArg.first();
                int start = formattedArg.getIndex();
                int limit = formattedArg.getEndIndex();
                int offset = prevLength - start;
                while (start < limit) {
                    Map<AttributedCharacterIterator.Attribute, Object> map = formattedArg.getAttributes();
                    int runLimit = formattedArg.getRunLimit();
                    if (map.size() != 0) {
                        for (Map.Entry<AttributedCharacterIterator.Attribute, Object> entry : map.entrySet()) {
                            this.attributes.add(new AttributeAndPosition(entry.getKey(), entry.getValue(), offset + start, offset + runLimit));
                        }
                    }
                    start = runLimit;
                    formattedArg.setIndex(start);
                }
            }
        }
    }

    private static final class PluralSelectorProvider
    implements PluralFormat.PluralSelector {
        private ULocale locale;
        private PluralRules rules;
        private PluralRules.PluralType type;

        public PluralSelectorProvider(ULocale loc, PluralRules.PluralType type) {
            this.locale = loc;
            this.type = type;
        }

        public String select(double number) {
            if (this.rules == null) {
                this.rules = PluralRules.forLocale(this.locale, this.type);
            }
            return this.rules.select(number);
        }
    }

    public static class Field
    extends Format.Field {
        private static final long serialVersionUID = 7510380454602616157L;
        public static final Field ARGUMENT = new Field("message argument field");

        protected Field(String name) {
            super(name);
        }

        protected Object readResolve() throws InvalidObjectException {
            if (this.getClass() != Field.class) {
                throw new InvalidObjectException("A subclass of MessageFormat.Field must implement readResolve.");
            }
            if (this.getName().equals(ARGUMENT.getName())) {
                return ARGUMENT;
            }
            throw new InvalidObjectException("Unknown attribute name.");
        }
    }
}


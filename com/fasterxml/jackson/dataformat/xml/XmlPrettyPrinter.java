/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.codehaus.stax2.XMLStreamWriter2
 *  org.codehaus.stax2.typed.Base64Variant
 */
package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.core.PrettyPrinter;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.stream.XMLStreamException;
import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.typed.Base64Variant;

public interface XmlPrettyPrinter
extends PrettyPrinter {
    public void writeStartElement(XMLStreamWriter2 var1, String var2, String var3) throws XMLStreamException;

    public void writeEndElement(XMLStreamWriter2 var1, int var2) throws XMLStreamException;

    public void writePrologLinefeed(XMLStreamWriter2 var1) throws XMLStreamException;

    public void writeLeafElement(XMLStreamWriter2 var1, String var2, String var3, String var4, boolean var5) throws XMLStreamException;

    public void writeLeafElement(XMLStreamWriter2 var1, String var2, String var3, char[] var4, int var5, int var6, boolean var7) throws XMLStreamException;

    public void writeLeafElement(XMLStreamWriter2 var1, String var2, String var3, boolean var4) throws XMLStreamException;

    public void writeLeafElement(XMLStreamWriter2 var1, String var2, String var3, int var4) throws XMLStreamException;

    public void writeLeafElement(XMLStreamWriter2 var1, String var2, String var3, long var4) throws XMLStreamException;

    public void writeLeafElement(XMLStreamWriter2 var1, String var2, String var3, double var4) throws XMLStreamException;

    public void writeLeafElement(XMLStreamWriter2 var1, String var2, String var3, float var4) throws XMLStreamException;

    public void writeLeafElement(XMLStreamWriter2 var1, String var2, String var3, BigInteger var4) throws XMLStreamException;

    public void writeLeafElement(XMLStreamWriter2 var1, String var2, String var3, BigDecimal var4) throws XMLStreamException;

    public void writeLeafElement(XMLStreamWriter2 var1, String var2, String var3, Base64Variant var4, byte[] var5, int var6, int var7) throws XMLStreamException;

    public void writeLeafNullElement(XMLStreamWriter2 var1, String var2, String var3) throws XMLStreamException;
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.psl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.apache.hc.client5.http.psl.DomainType;
import org.apache.hc.client5.http.psl.PublicSuffixList;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;

@Contract(threading=ThreadingBehavior.STATELESS)
public final class PublicSuffixListParser {
    public PublicSuffixList parse(Reader reader) throws IOException {
        String line;
        ArrayList<String> rules = new ArrayList<String>();
        ArrayList<String> exceptions = new ArrayList<String>();
        BufferedReader r = new BufferedReader(reader);
        while ((line = r.readLine()) != null) {
            boolean isException;
            if (line.isEmpty() || line.startsWith("//")) continue;
            if (line.startsWith(".")) {
                line = line.substring(1);
            }
            if (isException = line.startsWith("!")) {
                line = line.substring(1);
            }
            if (isException) {
                exceptions.add(line);
                continue;
            }
            rules.add(line);
        }
        return new PublicSuffixList(DomainType.UNKNOWN, rules, exceptions);
    }

    public List<PublicSuffixList> parseByType(Reader reader) throws IOException {
        String line;
        ArrayList<PublicSuffixList> result = new ArrayList<PublicSuffixList>(2);
        BufferedReader r = new BufferedReader(reader);
        DomainType domainType = null;
        ArrayList<String> rules = null;
        ArrayList<String> exceptions = null;
        while ((line = r.readLine()) != null) {
            boolean isException;
            if (line.isEmpty()) continue;
            if (line.startsWith("//")) {
                if (domainType == null) {
                    if (line.contains("===BEGIN ICANN DOMAINS===")) {
                        domainType = DomainType.ICANN;
                        continue;
                    }
                    if (!line.contains("===BEGIN PRIVATE DOMAINS===")) continue;
                    domainType = DomainType.PRIVATE;
                    continue;
                }
                if (!line.contains("===END ICANN DOMAINS===") && !line.contains("===END PRIVATE DOMAINS===")) continue;
                if (rules != null) {
                    result.add(new PublicSuffixList(domainType, rules, exceptions));
                }
                domainType = null;
                rules = null;
                exceptions = null;
                continue;
            }
            if (domainType == null) continue;
            if (line.startsWith(".")) {
                line = line.substring(1);
            }
            if (isException = line.startsWith("!")) {
                line = line.substring(1);
            }
            if (isException) {
                if (exceptions == null) {
                    exceptions = new ArrayList<String>();
                }
                exceptions.add(line);
                continue;
            }
            if (rules == null) {
                rules = new ArrayList<String>();
            }
            rules.add(line);
        }
        return result;
    }
}


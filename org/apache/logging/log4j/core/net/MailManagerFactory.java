/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.net;

import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.net.MailManager;

public interface MailManagerFactory
extends ManagerFactory<MailManager, MailManager.FactoryData> {
    @Override
    public MailManager createManager(String var1, MailManager.FactoryData var2);
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  javax.mail.Authenticator
 *  javax.mail.BodyPart
 *  javax.mail.Message
 *  javax.mail.Message$RecipientType
 *  javax.mail.MessagingException
 *  javax.mail.Multipart
 *  javax.mail.PasswordAuthentication
 *  javax.mail.Session
 *  javax.mail.Transport
 *  javax.mail.internet.InternetHeaders
 *  javax.mail.internet.MimeBodyPart
 *  javax.mail.internet.MimeMessage
 *  javax.mail.internet.MimeMultipart
 *  javax.mail.internet.MimeUtility
 *  javax.mail.util.ByteArrayDataSource
 */
package org.apache.logging.log4j.core.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import javax.net.ssl.SSLSocketFactory;
import org.apache.logging.log4j.LoggingException;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.net.MailManager;
import org.apache.logging.log4j.core.net.MailManagerFactory;
import org.apache.logging.log4j.core.net.MimeMessageBuilder;
import org.apache.logging.log4j.core.net.ssl.SslConfiguration;
import org.apache.logging.log4j.core.util.CyclicBuffer;
import org.apache.logging.log4j.core.util.NetUtils;
import org.apache.logging.log4j.util.PropertiesUtil;

public class SmtpManager
extends MailManager {
    public static final SMTPManagerFactory FACTORY = new SMTPManagerFactory();
    private final Session session;
    private final CyclicBuffer<LogEvent> buffer;
    private volatile MimeMessage message;
    private final MailManager.FactoryData data;

    private static MimeMessage createMimeMessage(MailManager.FactoryData data, Session session, LogEvent appendEvent) throws MessagingException {
        return new MimeMessageBuilder(session).setFrom(data.getFrom()).setReplyTo(data.getReplyTo()).setRecipients(Message.RecipientType.TO, data.getTo()).setRecipients(Message.RecipientType.CC, data.getCc()).setRecipients(Message.RecipientType.BCC, data.getBcc()).setSubject(data.getSubjectSerializer().toSerializable(appendEvent)).build();
    }

    protected SmtpManager(String name, Session session, MimeMessage message, MailManager.FactoryData data) {
        super(null, name);
        this.session = session;
        this.message = message;
        this.data = data;
        this.buffer = new CyclicBuffer<LogEvent>(LogEvent.class, data.getBufferSize());
    }

    @Override
    public void add(LogEvent event) {
        this.buffer.add(event.toImmutable());
    }

    @Deprecated
    public static SmtpManager getSmtpManager(Configuration config, String to, String cc, String bcc, String from, String replyTo, String subject, String protocol, String host, int port, String username, String password, boolean isDebug, String filterName, int numElements, SslConfiguration sslConfiguration) {
        AbstractStringLayout.Serializer subjectSerializer = PatternLayout.newSerializerBuilder().setConfiguration(config).setPattern(subject).build();
        MailManager.FactoryData data = new MailManager.FactoryData(to, cc, bcc, from, replyTo, subject, subjectSerializer, protocol, host, port, username, password, isDebug, numElements, sslConfiguration, filterName);
        return (SmtpManager)SmtpManager.getManager(data.getManagerName(), FACTORY, data);
    }

    @Override
    public void sendEvents(Layout<?> layout, LogEvent appendEvent) {
        if (this.message == null) {
            this.connect(appendEvent);
        }
        try {
            LogEvent[] priorEvents = this.removeAllBufferedEvents();
            byte[] rawBytes = this.formatContentToBytes(priorEvents, appendEvent, layout);
            String contentType = layout.getContentType();
            String encoding = this.getEncoding(rawBytes, contentType);
            byte[] encodedBytes = this.encodeContentToBytes(rawBytes, encoding);
            InternetHeaders headers = this.getHeaders(contentType, encoding);
            MimeMultipart mp = this.getMimeMultipart(encodedBytes, headers);
            String subject = this.data.getSubjectSerializer().toSerializable(appendEvent);
            this.sendMultipartMessage(this.message, mp, subject);
        } catch (IOException | RuntimeException | MessagingException e) {
            this.logError("Caught exception while sending e-mail notification.", e);
            throw new LoggingException("Error occurred while sending email", e);
        }
    }

    LogEvent[] removeAllBufferedEvents() {
        return this.buffer.removeAll();
    }

    protected byte[] formatContentToBytes(LogEvent[] priorEvents, LogEvent appendEvent, Layout<?> layout) throws IOException {
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        this.writeContent(priorEvents, appendEvent, layout, raw);
        return raw.toByteArray();
    }

    private void writeContent(LogEvent[] priorEvents, LogEvent appendEvent, Layout<?> layout, ByteArrayOutputStream out) throws IOException {
        this.writeHeader(layout, out);
        this.writeBuffer(priorEvents, appendEvent, layout, out);
        this.writeFooter(layout, out);
    }

    protected void writeHeader(Layout<?> layout, OutputStream out) throws IOException {
        byte[] header = layout.getHeader();
        if (header != null) {
            out.write(header);
        }
    }

    protected void writeBuffer(LogEvent[] priorEvents, LogEvent appendEvent, Layout<?> layout, OutputStream out) throws IOException {
        for (LogEvent priorEvent : priorEvents) {
            byte[] bytes = layout.toByteArray(priorEvent);
            out.write(bytes);
        }
        byte[] bytes = layout.toByteArray(appendEvent);
        out.write(bytes);
    }

    protected void writeFooter(Layout<?> layout, OutputStream out) throws IOException {
        byte[] footer = layout.getFooter();
        if (footer != null) {
            out.write(footer);
        }
    }

    protected String getEncoding(byte[] rawBytes, String contentType) {
        ByteArrayDataSource dataSource = new ByteArrayDataSource(rawBytes, contentType);
        return MimeUtility.getEncoding((DataSource)dataSource);
    }

    protected byte[] encodeContentToBytes(byte[] rawBytes, String encoding) throws MessagingException, IOException {
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        this.encodeContent(rawBytes, encoding, encoded);
        return encoded.toByteArray();
    }

    protected void encodeContent(byte[] bytes, String encoding, ByteArrayOutputStream out) throws MessagingException, IOException {
        try (OutputStream encoder = MimeUtility.encode((OutputStream)out, (String)encoding);){
            encoder.write(bytes);
        }
    }

    protected InternetHeaders getHeaders(String contentType, String encoding) {
        InternetHeaders headers = new InternetHeaders();
        headers.setHeader("Content-Type", contentType + "; charset=UTF-8");
        headers.setHeader("Content-Transfer-Encoding", encoding);
        return headers;
    }

    protected MimeMultipart getMimeMultipart(byte[] encodedBytes, InternetHeaders headers) throws MessagingException {
        MimeMultipart mp = new MimeMultipart();
        MimeBodyPart part = new MimeBodyPart(headers, encodedBytes);
        mp.addBodyPart((BodyPart)part);
        return mp;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Deprecated
    protected void sendMultipartMessage(MimeMessage msg, MimeMultipart mp) throws MessagingException {
        MimeMessage mimeMessage = msg;
        synchronized (mimeMessage) {
            msg.setContent((Multipart)mp);
            msg.setSentDate(new Date());
            Transport.send((Message)msg);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void sendMultipartMessage(MimeMessage msg, MimeMultipart mp, String subject) throws MessagingException {
        MimeMessage mimeMessage = msg;
        synchronized (mimeMessage) {
            msg.setContent((Multipart)mp);
            msg.setSentDate(new Date());
            msg.setSubject(subject);
            Transport.send((Message)msg);
        }
    }

    private synchronized void connect(LogEvent appendEvent) {
        if (this.message != null) {
            return;
        }
        try {
            this.message = SmtpManager.createMimeMessage(this.data, this.session, appendEvent);
        } catch (MessagingException e) {
            this.logError("Could not set SmtpAppender message options", e);
            this.message = null;
        }
    }

    public static class SMTPManagerFactory
    implements MailManagerFactory {
        @Override
        public SmtpManager createManager(String name, MailManager.FactoryData data) {
            SslConfiguration sslConfiguration;
            Authenticator authenticator;
            String smtpHost;
            String smtpProtocol = data.getSmtpProtocol();
            String prefix = "mail." + smtpProtocol;
            Properties properties = PropertiesUtil.getSystemProperties();
            properties.setProperty("mail.transport.protocol", smtpProtocol);
            if (properties.getProperty("mail.host") == null) {
                properties.setProperty("mail.host", NetUtils.getLocalHostname());
            }
            if (null != (smtpHost = data.getSmtpHost())) {
                properties.setProperty(prefix + ".host", smtpHost);
            }
            if (data.getSmtpPort() > 0) {
                properties.setProperty(prefix + ".port", String.valueOf(data.getSmtpPort()));
            }
            if (null != (authenticator = this.buildAuthenticator(data.getSmtpUsername(), data.getSmtpPassword()))) {
                properties.setProperty(prefix + ".auth", "true");
            }
            if (smtpProtocol.equals("smtps") && (sslConfiguration = data.getSslConfiguration()) != null) {
                SSLSocketFactory sslSocketFactory = sslConfiguration.getSslSocketFactory();
                properties.put(prefix + ".ssl.socketFactory", sslSocketFactory);
                properties.setProperty(prefix + ".ssl.checkserveridentity", Boolean.toString(sslConfiguration.isVerifyHostName()));
            }
            Session session = Session.getInstance((Properties)properties, (Authenticator)authenticator);
            session.setProtocolForAddress("rfc822", smtpProtocol);
            session.setDebug(data.isSmtpDebug());
            return new SmtpManager(name, session, null, data);
        }

        private Authenticator buildAuthenticator(final String username, final String password) {
            if (null != password && null != username) {
                return new Authenticator(){
                    private final PasswordAuthentication passwordAuthentication;
                    {
                        this.passwordAuthentication = new PasswordAuthentication(username, password);
                    }

                    protected PasswordAuthentication getPasswordAuthentication() {
                        return this.passwordAuthentication;
                    }
                };
            }
            return null;
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.drafts;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.enums.CloseHandshakeType;
import org.java_websocket.enums.HandshakeState;
import org.java_websocket.enums.Opcode;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.enums.Role;
import org.java_websocket.exceptions.IncompleteException;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidFrameException;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.exceptions.LimitExceededException;
import org.java_websocket.exceptions.NotSendableException;
import org.java_websocket.extensions.DefaultExtension;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.framing.BinaryFrame;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.FramedataImpl1;
import org.java_websocket.framing.TextFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ClientHandshakeBuilder;
import org.java_websocket.handshake.HandshakeBuilder;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.protocols.IProtocol;
import org.java_websocket.protocols.Protocol;
import org.java_websocket.util.Base64;
import org.java_websocket.util.Charsetfunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Draft_6455
extends Draft {
    private static final String SEC_WEB_SOCKET_KEY = "Sec-WebSocket-Key";
    private static final String SEC_WEB_SOCKET_PROTOCOL = "Sec-WebSocket-Protocol";
    private static final String SEC_WEB_SOCKET_EXTENSIONS = "Sec-WebSocket-Extensions";
    private static final String SEC_WEB_SOCKET_ACCEPT = "Sec-WebSocket-Accept";
    private static final String UPGRADE = "Upgrade";
    private static final String CONNECTION = "Connection";
    private final Logger log = LoggerFactory.getLogger(Draft_6455.class);
    private IExtension negotiatedExtension = new DefaultExtension();
    private IExtension defaultExtension = new DefaultExtension();
    private List<IExtension> knownExtensions;
    private IExtension currentDecodingExtension;
    private IProtocol protocol;
    private List<IProtocol> knownProtocols;
    private Framedata currentContinuousFrame;
    private final List<ByteBuffer> byteBufferList;
    private ByteBuffer incompleteframe;
    private final SecureRandom reuseableRandom = new SecureRandom();
    private int maxFrameSize;

    public Draft_6455() {
        this(Collections.emptyList());
    }

    public Draft_6455(IExtension inputExtension) {
        this(Collections.singletonList(inputExtension));
    }

    public Draft_6455(List<IExtension> inputExtensions) {
        this(inputExtensions, Collections.singletonList(new Protocol("")));
    }

    public Draft_6455(List<IExtension> inputExtensions, List<IProtocol> inputProtocols) {
        this(inputExtensions, inputProtocols, Integer.MAX_VALUE);
    }

    public Draft_6455(List<IExtension> inputExtensions, int inputMaxFrameSize) {
        this(inputExtensions, Collections.singletonList(new Protocol("")), inputMaxFrameSize);
    }

    public Draft_6455(List<IExtension> inputExtensions, List<IProtocol> inputProtocols, int inputMaxFrameSize) {
        if (inputExtensions == null || inputProtocols == null || inputMaxFrameSize < 1) {
            throw new IllegalArgumentException();
        }
        this.knownExtensions = new ArrayList<IExtension>(inputExtensions.size());
        this.knownProtocols = new ArrayList<IProtocol>(inputProtocols.size());
        boolean hasDefault = false;
        this.byteBufferList = new ArrayList<ByteBuffer>();
        for (IExtension inputExtension : inputExtensions) {
            if (!inputExtension.getClass().equals(DefaultExtension.class)) continue;
            hasDefault = true;
        }
        this.knownExtensions.addAll(inputExtensions);
        if (!hasDefault) {
            this.knownExtensions.add(this.knownExtensions.size(), this.negotiatedExtension);
        }
        this.knownProtocols.addAll(inputProtocols);
        this.maxFrameSize = inputMaxFrameSize;
        this.currentDecodingExtension = null;
    }

    @Override
    public HandshakeState acceptHandshakeAsServer(ClientHandshake handshakedata) throws InvalidHandshakeException {
        HandshakeState protocolState;
        int v = this.readVersion(handshakedata);
        if (v != 13) {
            this.log.trace("acceptHandshakeAsServer - Wrong websocket version.");
            return HandshakeState.NOT_MATCHED;
        }
        HandshakeState extensionState = HandshakeState.NOT_MATCHED;
        String requestedExtension = handshakedata.getFieldValue(SEC_WEB_SOCKET_EXTENSIONS);
        for (IExtension knownExtension : this.knownExtensions) {
            if (!knownExtension.acceptProvidedExtensionAsServer(requestedExtension)) continue;
            this.negotiatedExtension = knownExtension;
            extensionState = HandshakeState.MATCHED;
            this.log.trace("acceptHandshakeAsServer - Matching extension found: {}", (Object)this.negotiatedExtension);
            break;
        }
        if ((protocolState = this.containsRequestedProtocol(handshakedata.getFieldValue(SEC_WEB_SOCKET_PROTOCOL))) == HandshakeState.MATCHED && extensionState == HandshakeState.MATCHED) {
            return HandshakeState.MATCHED;
        }
        this.log.trace("acceptHandshakeAsServer - No matching extension or protocol found.");
        return HandshakeState.NOT_MATCHED;
    }

    private HandshakeState containsRequestedProtocol(String requestedProtocol) {
        for (IProtocol knownProtocol : this.knownProtocols) {
            if (!knownProtocol.acceptProvidedProtocol(requestedProtocol)) continue;
            this.protocol = knownProtocol;
            this.log.trace("acceptHandshake - Matching protocol found: {}", (Object)this.protocol);
            return HandshakeState.MATCHED;
        }
        return HandshakeState.NOT_MATCHED;
    }

    @Override
    public HandshakeState acceptHandshakeAsClient(ClientHandshake request, ServerHandshake response) throws InvalidHandshakeException {
        HandshakeState protocolState;
        if (!this.basicAccept(response)) {
            this.log.trace("acceptHandshakeAsClient - Missing/wrong upgrade or connection in handshake.");
            return HandshakeState.NOT_MATCHED;
        }
        if (!request.hasFieldValue(SEC_WEB_SOCKET_KEY) || !response.hasFieldValue(SEC_WEB_SOCKET_ACCEPT)) {
            this.log.trace("acceptHandshakeAsClient - Missing Sec-WebSocket-Key or Sec-WebSocket-Accept");
            return HandshakeState.NOT_MATCHED;
        }
        String seckeyAnswer = response.getFieldValue(SEC_WEB_SOCKET_ACCEPT);
        String seckeyChallenge = request.getFieldValue(SEC_WEB_SOCKET_KEY);
        if (!(seckeyChallenge = this.generateFinalKey(seckeyChallenge)).equals(seckeyAnswer)) {
            this.log.trace("acceptHandshakeAsClient - Wrong key for Sec-WebSocket-Key.");
            return HandshakeState.NOT_MATCHED;
        }
        HandshakeState extensionState = HandshakeState.NOT_MATCHED;
        String requestedExtension = response.getFieldValue(SEC_WEB_SOCKET_EXTENSIONS);
        for (IExtension knownExtension : this.knownExtensions) {
            if (!knownExtension.acceptProvidedExtensionAsClient(requestedExtension)) continue;
            this.negotiatedExtension = knownExtension;
            extensionState = HandshakeState.MATCHED;
            this.log.trace("acceptHandshakeAsClient - Matching extension found: {}", (Object)this.negotiatedExtension);
            break;
        }
        if ((protocolState = this.containsRequestedProtocol(response.getFieldValue(SEC_WEB_SOCKET_PROTOCOL))) == HandshakeState.MATCHED && extensionState == HandshakeState.MATCHED) {
            return HandshakeState.MATCHED;
        }
        this.log.trace("acceptHandshakeAsClient - No matching extension or protocol found.");
        return HandshakeState.NOT_MATCHED;
    }

    public IExtension getExtension() {
        return this.negotiatedExtension;
    }

    public List<IExtension> getKnownExtensions() {
        return this.knownExtensions;
    }

    public IProtocol getProtocol() {
        return this.protocol;
    }

    public int getMaxFrameSize() {
        return this.maxFrameSize;
    }

    public List<IProtocol> getKnownProtocols() {
        return this.knownProtocols;
    }

    @Override
    public ClientHandshakeBuilder postProcessHandshakeRequestAsClient(ClientHandshakeBuilder request) {
        request.put(UPGRADE, "websocket");
        request.put(CONNECTION, UPGRADE);
        byte[] random = new byte[16];
        this.reuseableRandom.nextBytes(random);
        request.put(SEC_WEB_SOCKET_KEY, Base64.encodeBytes(random));
        request.put("Sec-WebSocket-Version", "13");
        StringBuilder requestedExtensions = new StringBuilder();
        for (IExtension knownExtension : this.knownExtensions) {
            if (knownExtension.getProvidedExtensionAsClient() == null || knownExtension.getProvidedExtensionAsClient().length() == 0) continue;
            if (requestedExtensions.length() > 0) {
                requestedExtensions.append(", ");
            }
            requestedExtensions.append(knownExtension.getProvidedExtensionAsClient());
        }
        if (requestedExtensions.length() != 0) {
            request.put(SEC_WEB_SOCKET_EXTENSIONS, requestedExtensions.toString());
        }
        StringBuilder requestedProtocols = new StringBuilder();
        for (IProtocol knownProtocol : this.knownProtocols) {
            if (knownProtocol.getProvidedProtocol().length() == 0) continue;
            if (requestedProtocols.length() > 0) {
                requestedProtocols.append(", ");
            }
            requestedProtocols.append(knownProtocol.getProvidedProtocol());
        }
        if (requestedProtocols.length() != 0) {
            request.put(SEC_WEB_SOCKET_PROTOCOL, requestedProtocols.toString());
        }
        return request;
    }

    @Override
    public HandshakeBuilder postProcessHandshakeResponseAsServer(ClientHandshake request, ServerHandshakeBuilder response) throws InvalidHandshakeException {
        response.put(UPGRADE, "websocket");
        response.put(CONNECTION, request.getFieldValue(CONNECTION));
        String seckey = request.getFieldValue(SEC_WEB_SOCKET_KEY);
        if (seckey == null || "".equals(seckey)) {
            throw new InvalidHandshakeException("missing Sec-WebSocket-Key");
        }
        response.put(SEC_WEB_SOCKET_ACCEPT, this.generateFinalKey(seckey));
        if (this.getExtension().getProvidedExtensionAsServer().length() != 0) {
            response.put(SEC_WEB_SOCKET_EXTENSIONS, this.getExtension().getProvidedExtensionAsServer());
        }
        if (this.getProtocol() != null && this.getProtocol().getProvidedProtocol().length() != 0) {
            response.put(SEC_WEB_SOCKET_PROTOCOL, this.getProtocol().getProvidedProtocol());
        }
        response.setHttpStatusMessage("Web Socket Protocol Handshake");
        response.put("Server", "TooTallNate Java-WebSocket");
        response.put("Date", this.getServerTime());
        return response;
    }

    @Override
    public Draft copyInstance() {
        ArrayList<IExtension> newExtensions = new ArrayList<IExtension>();
        for (IExtension knownExtension : this.getKnownExtensions()) {
            newExtensions.add(knownExtension.copyInstance());
        }
        ArrayList<IProtocol> newProtocols = new ArrayList<IProtocol>();
        for (IProtocol knownProtocol : this.getKnownProtocols()) {
            newProtocols.add(knownProtocol.copyInstance());
        }
        return new Draft_6455(newExtensions, newProtocols, this.maxFrameSize);
    }

    @Override
    public ByteBuffer createBinaryFrame(Framedata framedata) {
        this.getExtension().encodeFrame(framedata);
        if (this.log.isTraceEnabled()) {
            this.log.trace("afterEnconding({}): {}", (Object)framedata.getPayloadData().remaining(), (Object)(framedata.getPayloadData().remaining() > 1000 ? "too big to display" : new String(framedata.getPayloadData().array())));
        }
        return this.createByteBufferFromFramedata(framedata);
    }

    private ByteBuffer createByteBufferFromFramedata(Framedata framedata) {
        ByteBuffer mes = framedata.getPayloadData();
        boolean mask = this.role == Role.CLIENT;
        int sizebytes = this.getSizeBytes(mes);
        ByteBuffer buf = ByteBuffer.allocate(1 + (sizebytes > 1 ? sizebytes + 1 : sizebytes) + (mask ? 4 : 0) + mes.remaining());
        byte optcode = this.fromOpcode(framedata.getOpcode());
        byte one = (byte)(framedata.isFin() ? -128 : 0);
        one = (byte)(one | optcode);
        if (framedata.isRSV1()) {
            one = (byte)(one | this.getRSVByte(1));
        }
        if (framedata.isRSV2()) {
            one = (byte)(one | this.getRSVByte(2));
        }
        if (framedata.isRSV3()) {
            one = (byte)(one | this.getRSVByte(3));
        }
        buf.put(one);
        byte[] payloadlengthbytes = this.toByteArray(mes.remaining(), sizebytes);
        assert (payloadlengthbytes.length == sizebytes);
        if (sizebytes == 1) {
            buf.put((byte)(payloadlengthbytes[0] | this.getMaskByte(mask)));
        } else if (sizebytes == 2) {
            buf.put((byte)(0x7E | this.getMaskByte(mask)));
            buf.put(payloadlengthbytes);
        } else if (sizebytes == 8) {
            buf.put((byte)(0x7F | this.getMaskByte(mask)));
            buf.put(payloadlengthbytes);
        } else {
            throw new IllegalStateException("Size representation not supported/specified");
        }
        if (mask) {
            ByteBuffer maskkey = ByteBuffer.allocate(4);
            maskkey.putInt(this.reuseableRandom.nextInt());
            buf.put(maskkey.array());
            int i = 0;
            while (mes.hasRemaining()) {
                buf.put((byte)(mes.get() ^ maskkey.get(i % 4)));
                ++i;
            }
        } else {
            buf.put(mes);
            mes.flip();
        }
        assert (buf.remaining() == 0) : buf.remaining();
        buf.flip();
        return buf;
    }

    private Framedata translateSingleFrame(ByteBuffer buffer) throws IncompleteException, InvalidDataException {
        if (buffer == null) {
            throw new IllegalArgumentException();
        }
        int maxpacketsize = buffer.remaining();
        int realpacketsize = 2;
        this.translateSingleFrameCheckPacketSize(maxpacketsize, realpacketsize);
        byte b1 = buffer.get();
        boolean fin = b1 >> 8 != 0;
        boolean rsv1 = (b1 & 0x40) != 0;
        boolean rsv2 = (b1 & 0x20) != 0;
        boolean rsv3 = (b1 & 0x10) != 0;
        byte b2 = buffer.get();
        boolean mask = (b2 & 0xFFFFFF80) != 0;
        int payloadlength = b2 & 0x7F;
        Opcode optcode = this.toOpcode((byte)(b1 & 0xF));
        if (payloadlength < 0 || payloadlength > 125) {
            TranslatedPayloadMetaData payloadData = this.translateSingleFramePayloadLength(buffer, optcode, payloadlength, maxpacketsize, realpacketsize);
            payloadlength = payloadData.getPayloadLength();
            realpacketsize = payloadData.getRealPackageSize();
        }
        this.translateSingleFrameCheckLengthLimit(payloadlength);
        realpacketsize += mask ? 4 : 0;
        this.translateSingleFrameCheckPacketSize(maxpacketsize, realpacketsize += payloadlength);
        ByteBuffer payload = ByteBuffer.allocate(this.checkAlloc(payloadlength));
        if (mask) {
            byte[] maskskey = new byte[4];
            buffer.get(maskskey);
            for (int i = 0; i < payloadlength; ++i) {
                payload.put((byte)(buffer.get() ^ maskskey[i % 4]));
            }
        } else {
            payload.put(buffer.array(), buffer.position(), payload.limit());
            buffer.position(buffer.position() + payload.limit());
        }
        FramedataImpl1 frame = FramedataImpl1.get(optcode);
        frame.setFin(fin);
        frame.setRSV1(rsv1);
        frame.setRSV2(rsv2);
        frame.setRSV3(rsv3);
        payload.flip();
        frame.setPayload(payload);
        if (frame.getOpcode() != Opcode.CONTINUOUS) {
            this.currentDecodingExtension = frame.isRSV1() || frame.isRSV2() || frame.isRSV3() ? this.getExtension() : this.defaultExtension;
        }
        if (this.currentDecodingExtension == null) {
            this.currentDecodingExtension = this.defaultExtension;
        }
        this.currentDecodingExtension.isFrameValid(frame);
        this.currentDecodingExtension.decodeFrame(frame);
        if (this.log.isTraceEnabled()) {
            this.log.trace("afterDecoding({}): {}", (Object)frame.getPayloadData().remaining(), (Object)(frame.getPayloadData().remaining() > 1000 ? "too big to display" : new String(frame.getPayloadData().array())));
        }
        frame.isValid();
        return frame;
    }

    private TranslatedPayloadMetaData translateSingleFramePayloadLength(ByteBuffer buffer, Opcode optcode, int oldPayloadlength, int maxpacketsize, int oldRealpacketsize) throws InvalidFrameException, IncompleteException, LimitExceededException {
        int payloadlength = oldPayloadlength;
        int realpacketsize = oldRealpacketsize;
        if (optcode == Opcode.PING || optcode == Opcode.PONG || optcode == Opcode.CLOSING) {
            this.log.trace("Invalid frame: more than 125 octets");
            throw new InvalidFrameException("more than 125 octets");
        }
        if (payloadlength == 126) {
            this.translateSingleFrameCheckPacketSize(maxpacketsize, realpacketsize += 2);
            byte[] sizebytes = new byte[3];
            sizebytes[1] = buffer.get();
            sizebytes[2] = buffer.get();
            payloadlength = new BigInteger(sizebytes).intValue();
        } else {
            this.translateSingleFrameCheckPacketSize(maxpacketsize, realpacketsize += 8);
            byte[] bytes = new byte[8];
            for (int i = 0; i < 8; ++i) {
                bytes[i] = buffer.get();
            }
            long length = new BigInteger(bytes).longValue();
            this.translateSingleFrameCheckLengthLimit(length);
            payloadlength = (int)length;
        }
        return new TranslatedPayloadMetaData(payloadlength, realpacketsize);
    }

    private void translateSingleFrameCheckLengthLimit(long length) throws LimitExceededException {
        if (length > Integer.MAX_VALUE) {
            this.log.trace("Limit exedeed: Payloadsize is to big...");
            throw new LimitExceededException("Payloadsize is to big...");
        }
        if (length > (long)this.maxFrameSize) {
            this.log.trace("Payload limit reached. Allowed: {} Current: {}", (Object)this.maxFrameSize, (Object)length);
            throw new LimitExceededException("Payload limit reached.", this.maxFrameSize);
        }
        if (length < 0L) {
            this.log.trace("Limit underflow: Payloadsize is to little...");
            throw new LimitExceededException("Payloadsize is to little...");
        }
    }

    private void translateSingleFrameCheckPacketSize(int maxpacketsize, int realpacketsize) throws IncompleteException {
        if (maxpacketsize < realpacketsize) {
            this.log.trace("Incomplete frame: maxpacketsize < realpacketsize");
            throw new IncompleteException(realpacketsize);
        }
    }

    private byte getRSVByte(int rsv) {
        switch (rsv) {
            case 1: {
                return 64;
            }
            case 2: {
                return 32;
            }
            case 3: {
                return 16;
            }
        }
        return 0;
    }

    private byte getMaskByte(boolean mask) {
        return mask ? (byte)-128 : 0;
    }

    private int getSizeBytes(ByteBuffer mes) {
        if (mes.remaining() <= 125) {
            return 1;
        }
        if (mes.remaining() <= 65535) {
            return 2;
        }
        return 8;
    }

    @Override
    public List<Framedata> translateFrame(ByteBuffer buffer) throws InvalidDataException {
        Framedata cur;
        LinkedList<Framedata> frames;
        while (true) {
            frames = new LinkedList<Framedata>();
            if (this.incompleteframe == null) break;
            try {
                buffer.mark();
                int availableNextByteCount = buffer.remaining();
                int expectedNextByteCount = this.incompleteframe.remaining();
                if (expectedNextByteCount > availableNextByteCount) {
                    this.incompleteframe.put(buffer.array(), buffer.position(), availableNextByteCount);
                    buffer.position(buffer.position() + availableNextByteCount);
                    return Collections.emptyList();
                }
                this.incompleteframe.put(buffer.array(), buffer.position(), expectedNextByteCount);
                buffer.position(buffer.position() + expectedNextByteCount);
                cur = this.translateSingleFrame((ByteBuffer)this.incompleteframe.duplicate().position(0));
                frames.add(cur);
                this.incompleteframe = null;
            } catch (IncompleteException e) {
                ByteBuffer extendedframe = ByteBuffer.allocate(this.checkAlloc(e.getPreferredSize()));
                assert (extendedframe.limit() > this.incompleteframe.limit());
                this.incompleteframe.rewind();
                extendedframe.put(this.incompleteframe);
                this.incompleteframe = extendedframe;
                continue;
            }
            break;
        }
        while (buffer.hasRemaining()) {
            buffer.mark();
            try {
                cur = this.translateSingleFrame(buffer);
                frames.add(cur);
            } catch (IncompleteException e) {
                buffer.reset();
                int pref = e.getPreferredSize();
                this.incompleteframe = ByteBuffer.allocate(this.checkAlloc(pref));
                this.incompleteframe.put(buffer);
                break;
            }
        }
        return frames;
    }

    @Override
    public List<Framedata> createFrames(ByteBuffer binary, boolean mask) {
        BinaryFrame curframe = new BinaryFrame();
        curframe.setPayload(binary);
        curframe.setTransferemasked(mask);
        try {
            curframe.isValid();
        } catch (InvalidDataException e) {
            throw new NotSendableException(e);
        }
        return Collections.singletonList(curframe);
    }

    @Override
    public List<Framedata> createFrames(String text, boolean mask) {
        TextFrame curframe = new TextFrame();
        curframe.setPayload(ByteBuffer.wrap(Charsetfunctions.utf8Bytes(text)));
        curframe.setTransferemasked(mask);
        try {
            curframe.isValid();
        } catch (InvalidDataException e) {
            throw new NotSendableException(e);
        }
        return Collections.singletonList(curframe);
    }

    @Override
    public void reset() {
        this.incompleteframe = null;
        if (this.negotiatedExtension != null) {
            this.negotiatedExtension.reset();
        }
        this.negotiatedExtension = new DefaultExtension();
        this.protocol = null;
    }

    private String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    private String generateFinalKey(String in) {
        MessageDigest sh1;
        String seckey = in.trim();
        String acc = seckey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        try {
            sh1 = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        return Base64.encodeBytes(sh1.digest(acc.getBytes()));
    }

    private byte[] toByteArray(long val2, int bytecount) {
        byte[] buffer = new byte[bytecount];
        int highest = 8 * bytecount - 8;
        for (int i = 0; i < bytecount; ++i) {
            buffer[i] = (byte)(val2 >>> highest - 8 * i);
        }
        return buffer;
    }

    private byte fromOpcode(Opcode opcode) {
        if (opcode == Opcode.CONTINUOUS) {
            return 0;
        }
        if (opcode == Opcode.TEXT) {
            return 1;
        }
        if (opcode == Opcode.BINARY) {
            return 2;
        }
        if (opcode == Opcode.CLOSING) {
            return 8;
        }
        if (opcode == Opcode.PING) {
            return 9;
        }
        if (opcode == Opcode.PONG) {
            return 10;
        }
        throw new IllegalArgumentException("Don't know how to handle " + opcode.toString());
    }

    private Opcode toOpcode(byte opcode) throws InvalidFrameException {
        switch (opcode) {
            case 0: {
                return Opcode.CONTINUOUS;
            }
            case 1: {
                return Opcode.TEXT;
            }
            case 2: {
                return Opcode.BINARY;
            }
            case 8: {
                return Opcode.CLOSING;
            }
            case 9: {
                return Opcode.PING;
            }
            case 10: {
                return Opcode.PONG;
            }
        }
        throw new InvalidFrameException("Unknown opcode " + (short)opcode);
    }

    @Override
    public void processFrame(WebSocketImpl webSocketImpl, Framedata frame) throws InvalidDataException {
        Opcode curop = frame.getOpcode();
        if (curop == Opcode.CLOSING) {
            this.processFrameClosing(webSocketImpl, frame);
        } else if (curop == Opcode.PING) {
            webSocketImpl.getWebSocketListener().onWebsocketPing(webSocketImpl, frame);
        } else if (curop == Opcode.PONG) {
            webSocketImpl.updateLastPong();
            webSocketImpl.getWebSocketListener().onWebsocketPong(webSocketImpl, frame);
        } else if (!frame.isFin() || curop == Opcode.CONTINUOUS) {
            this.processFrameContinuousAndNonFin(webSocketImpl, frame, curop);
        } else {
            if (this.currentContinuousFrame != null) {
                this.log.error("Protocol error: Continuous frame sequence not completed.");
                throw new InvalidDataException(1002, "Continuous frame sequence not completed.");
            }
            if (curop == Opcode.TEXT) {
                this.processFrameText(webSocketImpl, frame);
            } else if (curop == Opcode.BINARY) {
                this.processFrameBinary(webSocketImpl, frame);
            } else {
                this.log.error("non control or continious frame expected");
                throw new InvalidDataException(1002, "non control or continious frame expected");
            }
        }
    }

    private void processFrameContinuousAndNonFin(WebSocketImpl webSocketImpl, Framedata frame, Opcode curop) throws InvalidDataException {
        if (curop != Opcode.CONTINUOUS) {
            this.processFrameIsNotFin(frame);
        } else if (frame.isFin()) {
            this.processFrameIsFin(webSocketImpl, frame);
        } else if (this.currentContinuousFrame == null) {
            this.log.error("Protocol error: Continuous frame sequence was not started.");
            throw new InvalidDataException(1002, "Continuous frame sequence was not started.");
        }
        if (curop == Opcode.TEXT && !Charsetfunctions.isValidUTF8(frame.getPayloadData())) {
            this.log.error("Protocol error: Payload is not UTF8");
            throw new InvalidDataException(1007);
        }
        if (curop == Opcode.CONTINUOUS && this.currentContinuousFrame != null) {
            this.addToBufferList(frame.getPayloadData());
        }
    }

    private void processFrameBinary(WebSocketImpl webSocketImpl, Framedata frame) {
        try {
            webSocketImpl.getWebSocketListener().onWebsocketMessage((WebSocket)webSocketImpl, frame.getPayloadData());
        } catch (RuntimeException e) {
            this.logRuntimeException(webSocketImpl, e);
        }
    }

    private void logRuntimeException(WebSocketImpl webSocketImpl, RuntimeException e) {
        this.log.error("Runtime exception during onWebsocketMessage", e);
        webSocketImpl.getWebSocketListener().onWebsocketError(webSocketImpl, e);
    }

    private void processFrameText(WebSocketImpl webSocketImpl, Framedata frame) throws InvalidDataException {
        try {
            webSocketImpl.getWebSocketListener().onWebsocketMessage((WebSocket)webSocketImpl, Charsetfunctions.stringUtf8(frame.getPayloadData()));
        } catch (RuntimeException e) {
            this.logRuntimeException(webSocketImpl, e);
        }
    }

    private void processFrameIsFin(WebSocketImpl webSocketImpl, Framedata frame) throws InvalidDataException {
        if (this.currentContinuousFrame == null) {
            this.log.trace("Protocol error: Previous continuous frame sequence not completed.");
            throw new InvalidDataException(1002, "Continuous frame sequence was not started.");
        }
        this.addToBufferList(frame.getPayloadData());
        this.checkBufferLimit();
        if (this.currentContinuousFrame.getOpcode() == Opcode.TEXT) {
            ((FramedataImpl1)this.currentContinuousFrame).setPayload(this.getPayloadFromByteBufferList());
            ((FramedataImpl1)this.currentContinuousFrame).isValid();
            try {
                webSocketImpl.getWebSocketListener().onWebsocketMessage((WebSocket)webSocketImpl, Charsetfunctions.stringUtf8(this.currentContinuousFrame.getPayloadData()));
            } catch (RuntimeException e) {
                this.logRuntimeException(webSocketImpl, e);
            }
        } else if (this.currentContinuousFrame.getOpcode() == Opcode.BINARY) {
            ((FramedataImpl1)this.currentContinuousFrame).setPayload(this.getPayloadFromByteBufferList());
            ((FramedataImpl1)this.currentContinuousFrame).isValid();
            try {
                webSocketImpl.getWebSocketListener().onWebsocketMessage((WebSocket)webSocketImpl, this.currentContinuousFrame.getPayloadData());
            } catch (RuntimeException e) {
                this.logRuntimeException(webSocketImpl, e);
            }
        }
        this.currentContinuousFrame = null;
        this.clearBufferList();
    }

    private void processFrameIsNotFin(Framedata frame) throws InvalidDataException {
        if (this.currentContinuousFrame != null) {
            this.log.trace("Protocol error: Previous continuous frame sequence not completed.");
            throw new InvalidDataException(1002, "Previous continuous frame sequence not completed.");
        }
        this.currentContinuousFrame = frame;
        this.addToBufferList(frame.getPayloadData());
        this.checkBufferLimit();
    }

    private void processFrameClosing(WebSocketImpl webSocketImpl, Framedata frame) {
        int code = 1005;
        String reason = "";
        if (frame instanceof CloseFrame) {
            CloseFrame cf = (CloseFrame)frame;
            code = cf.getCloseCode();
            reason = cf.getMessage();
        }
        if (webSocketImpl.getReadyState() == ReadyState.CLOSING) {
            webSocketImpl.closeConnection(code, reason, true);
        } else if (this.getCloseHandshakeType() == CloseHandshakeType.TWOWAY) {
            webSocketImpl.close(code, reason, true);
        } else {
            webSocketImpl.flushAndClose(code, reason, false);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void clearBufferList() {
        List<ByteBuffer> list = this.byteBufferList;
        synchronized (list) {
            this.byteBufferList.clear();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void addToBufferList(ByteBuffer payloadData) {
        List<ByteBuffer> list = this.byteBufferList;
        synchronized (list) {
            this.byteBufferList.add(payloadData);
        }
    }

    private void checkBufferLimit() throws LimitExceededException {
        long totalSize = this.getByteBufferListSize();
        if (totalSize > (long)this.maxFrameSize) {
            this.clearBufferList();
            this.log.trace("Payload limit reached. Allowed: {} Current: {}", (Object)this.maxFrameSize, (Object)totalSize);
            throw new LimitExceededException(this.maxFrameSize);
        }
    }

    @Override
    public CloseHandshakeType getCloseHandshakeType() {
        return CloseHandshakeType.TWOWAY;
    }

    @Override
    public String toString() {
        String result = super.toString();
        if (this.getExtension() != null) {
            result = result + " extension: " + this.getExtension().toString();
        }
        if (this.getProtocol() != null) {
            result = result + " protocol: " + this.getProtocol().toString();
        }
        result = result + " max frame size: " + this.maxFrameSize;
        return result;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Draft_6455 that = (Draft_6455)o;
        if (this.maxFrameSize != that.getMaxFrameSize()) {
            return false;
        }
        if (this.negotiatedExtension != null ? !this.negotiatedExtension.equals(that.getExtension()) : that.getExtension() != null) {
            return false;
        }
        return this.protocol != null ? this.protocol.equals(that.getProtocol()) : that.getProtocol() == null;
    }

    public int hashCode() {
        int result = this.negotiatedExtension != null ? this.negotiatedExtension.hashCode() : 0;
        result = 31 * result + (this.protocol != null ? this.protocol.hashCode() : 0);
        result = 31 * result + (this.maxFrameSize ^ this.maxFrameSize >>> 32);
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private ByteBuffer getPayloadFromByteBufferList() throws LimitExceededException {
        ByteBuffer resultingByteBuffer;
        long totalSize = 0L;
        List<ByteBuffer> list = this.byteBufferList;
        synchronized (list) {
            for (ByteBuffer buffer : this.byteBufferList) {
                totalSize += (long)buffer.limit();
            }
            this.checkBufferLimit();
            resultingByteBuffer = ByteBuffer.allocate((int)totalSize);
            for (ByteBuffer buffer : this.byteBufferList) {
                resultingByteBuffer.put(buffer);
            }
        }
        resultingByteBuffer.flip();
        return resultingByteBuffer;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private long getByteBufferListSize() {
        long totalSize = 0L;
        List<ByteBuffer> list = this.byteBufferList;
        synchronized (list) {
            for (ByteBuffer buffer : this.byteBufferList) {
                totalSize += (long)buffer.limit();
            }
        }
        return totalSize;
    }

    private class TranslatedPayloadMetaData {
        private int payloadLength;
        private int realPackageSize;

        private int getPayloadLength() {
            return this.payloadLength;
        }

        private int getRealPackageSize() {
            return this.realPackageSize;
        }

        TranslatedPayloadMetaData(int newPayloadLength, int newRealPackageSize) {
            this.payloadLength = newPayloadLength;
            this.realPackageSize = newRealPackageSize;
        }
    }
}


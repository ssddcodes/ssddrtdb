package dev.ssdd.rtdb;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import dev.ssdd.rtdb.exceptions.IllegalSchemeException;
import dev.ssdd.rtdb.exceptions.InvalidServerHandshakeException;
import dev.ssdd.rtdb.exceptions.UnknownOpcodeException;
import dev.ssdd.rtdb.playground.commons.codec.binary.Base64;
import dev.ssdd.rtdb.playground.commons.codec.digest.DigestUtils;
import dev.ssdd.rtdb.playground.http.Header;
import dev.ssdd.rtdb.playground.http.HttpException;
import dev.ssdd.rtdb.playground.http.HttpResponse;
import dev.ssdd.rtdb.playground.http.StatusLine;
import dev.ssdd.rtdb.playground.http.impl.io.DefaultHttpResponseParser;
import dev.ssdd.rtdb.playground.http.impl.io.HttpTransportMetricsImpl;
import dev.ssdd.rtdb.playground.http.impl.io.SessionInputBufferImpl;
import dev.ssdd.rtdb.playground.http.io.HttpMessageParser;

public abstract class WebSocketClient {

    private static final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    private static final int OPCODE_CONTINUATION = 0x0;

    private static final int OPCODE_TEXT = 0x1;

    private static final int OPCODE_BINARY = 0x2;

    private static final int OPCODE_CLOSE = 0x8;

    private static final int OPCODE_PING = 0x9;

    private static final int OPCODE_PONG = 0xA;

    private final Object globalLock;

    private final URI uri;

    private final SecureRandom secureRandom;

    private int connectTimeout;

    private int readTimeout;

    private boolean automaticReconnection;

    private long waitTimeBeforeReconnection;

    private volatile boolean isRunning;

    private final Map<String, String> headers;

    private volatile WebSocketConnection webSocketConnection;

    private volatile Thread reconnectionThread;

    String TAG = "SSDDRTDB";


    private SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    private String path;

    public WebSocketClient(URI uri) {
        this.globalLock = new Object();
        this.uri = uri;
        this.secureRandom = new SecureRandom();
        this.connectTimeout = 0;
        this.readTimeout = 0;
        this.automaticReconnection = false;
        this.waitTimeBeforeReconnection = 0;
        this.isRunning = false;
        this.headers = new HashMap<>();
        webSocketConnection = new WebSocketConnection();
      //  await();
    }

    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        socketFactory = sslSocketFactory;
    }


    public void child(String path){
        if(this.path.length() != 0 && this.path.startsWith("$")){
            String holder;
            if(path.contains("/")){
                holder = path.replace("/",".");
            }else {
                holder = path;
            }
            if(!(holder.contains("$"))){
                if(!(holder.startsWith("."))){
                    if(!(holder.endsWith("."))) {
                        this.path = this.path+"."+holder;
                    }else if(holder.endsWith(".")){
                        throw new IllegalStateException("remove / at the end of the path");
                    }
                }else {
                    if(!(holder.endsWith("."))) {
                        this.path = this.path+holder;
                    }else if(holder.endsWith(".")){
                        throw new IllegalStateException("remove / at the end of the path");
                    }
                }
            }else {
                throw new IllegalStateException("can not use any special characters except / in it");
            }
        }else {
            throw new IllegalStateException("Database not initialized.");
        }
    }

    public void getRef(){
        this.path = "$";
    }

    public abstract void onTextReceived(String message);

    private void onBinaryReceived(byte[] data){

    }

    private void onPingReceived(byte[] data){

    }

    public void onPongReceived(byte[] data){

    }

    private void onException(Exception e){
        Log.e("SSDDRTDB", "Exception: "+e );
    }

    private void onCloseReceived(){
        Log.e("SSDDRTDB", "Disconnected");
    }

    public void addHeader(String key, String value) {
        synchronized (globalLock) {
            if (isRunning) {
                throw new IllegalStateException("Cannot add header while WebSocketClient is running");
            }
            this.headers.put(key, value);
        }
    }

    public void setConnectTimeout(int connectTimeout) {
        synchronized (globalLock) {
            if (isRunning) {
                throw new IllegalStateException("Cannot set connect timeout while WebSocketClient is running");
            } else if (connectTimeout < 0) {
                throw new IllegalStateException("Connect timeout must be greater or equal than zero");
            }
            this.connectTimeout = connectTimeout;
        }
    }

     void setReadTimeout() {
        synchronized (globalLock) {
            if (isRunning) {
                throw new IllegalStateException("Cannot set read timeout while WebSocketClient is running");
            }
            this.readTimeout = 60000;
        }
    }

     public void enableAutomaticReconnection(int timeout) {
        synchronized (globalLock) {
            if (isRunning) {
                throw new IllegalStateException(
                        "Cannot enable automatic reconnection while WebSocketClient is running");
            }else if(timeout<0){
                throw new IllegalStateException(
                        "Duration before reconnection can not be less then 0");
            }else {
                this.automaticReconnection = true;
                this.waitTimeBeforeReconnection = timeout;
            }
        }
    }

    public void disableAutomaticReconnection() {
        synchronized (globalLock) {
            if (isRunning) {
                throw new IllegalStateException(
                        "Cannot disable automatic reconnection while WebSocketClient is running");
            }
            this.automaticReconnection = false;
        }
    }

    public void connect() {
        synchronized (globalLock) {
            if (isRunning) {
                throw new IllegalStateException("WebSocketClient is not reusable");
            }

            this.isRunning = true;
            createAndStartConnectionThread();
        }
    }

    private void createAndStartConnectionThread() {
        new Thread(() -> {
            try {
                boolean success = webSocketConnection.createAndConnectTCPSocket();
                if (success) {
                    webSocketConnection.startConnection();



                }
            } catch (Exception e) {
                synchronized (globalLock) {
                    if (isRunning) {
                        webSocketConnection.closeInternal();

                        onException(e);

                        if (e instanceof IOException && automaticReconnection) {
                            createAndStartReconnectionThread();
                        }
                    }
                }
            }
        }).start();
    }

    private void createAndStartReconnectionThread() {
        reconnectionThread = new Thread(() -> {
            try {
                Thread.sleep(waitTimeBeforeReconnection);

                synchronized (globalLock) {
                    if (isRunning) {
                        webSocketConnection = new WebSocketConnection();
                        createAndStartConnectionThread();
                    }
                }
            } catch (InterruptedException e) {
                // Expected behavior when the WebSocket connection is closed
            }
        });
        reconnectionThread.start();
    }

    private void notifyOnOpen() {
        synchronized (globalLock) {
            if (isRunning) {
                //onOpen();
                Log.i("SSDDRTDB", "Connected");
            }
        }
    }

    private synchronized void notifyOnTextReceived(String message) {
        synchronized (globalLock) {
            if (isRunning) {
                Thread th = new Thread(()->{
                        onTextReceived(message);
                });
                th.start();
                try {
                    th.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private boolean isTime(String msg) {
        String x = "message";
        try {
            JSONObject jsonObject = new JSONObject(msg);
            x = jsonObject.get("id").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return x.equals("time");
    }

    private void notifyOnBinaryReceived(byte[] data) {
        synchronized (globalLock) {
            if (isRunning) {
                onBinaryReceived(data);
            }
        }
    }

    private void notifyOnPingReceived(byte[] data) {
        synchronized (globalLock) {
            if (isRunning) {
                onPingReceived(data);
            }
        }
    }

    private void notifyOnPongReceived(byte[] data) {
        synchronized (globalLock) {
            if (isRunning) {
                onPongReceived(data);
            }
        }
    }

    private void notifyOnException(Exception e) {
        synchronized (globalLock) {
            if (isRunning) {
                onException(e);
            }
        }
    }

    private void notifyOnCloseReceived() {
        synchronized (globalLock) {
            if (isRunning) {
                onCloseReceived();
            }
        }
    }

    public void send(String message) {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        final Payload payload = new Payload(OPCODE_TEXT, data);

        synchronized (this) {
            Thread t = new Thread(() -> {
                webSocketConnection.sendInternal(payload);
            });
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(byte[] data) {
        final Payload payload = new Payload(OPCODE_BINARY, data);

        new Thread(() -> webSocketConnection.sendInternal(payload)).start();
    }

    public void sendPing(byte[] data) {
        final Payload payload = new Payload(OPCODE_PING, data);

        new Thread(() -> webSocketConnection.sendInternal(payload)).start();
    }

    public void sendPong(byte[] data) {
        final Payload payload = new Payload(OPCODE_PONG, data);

        new Thread(() -> webSocketConnection.sendInternal(payload)).start();
    }

    public void close() {
        new Thread(() -> {
            synchronized (globalLock) {
                isRunning = false;

                if (reconnectionThread != null) {
                    reconnectionThread.interrupt();
                }

                webSocketConnection.closeInternal();
            }
        }).start();
    }

    private class WebSocketConnection {

        private volatile boolean pendingMessages;

        private volatile boolean isClosed;

        private final LinkedList<Payload> outBuffer;

        private final Object internalLock;

        private final Thread writerThread;

        private Socket socket;

        private BufferedInputStream bis;

        private BufferedOutputStream bos;

        private WebSocketConnection() {
            this.pendingMessages = false;
            this.isClosed = false;
            this.outBuffer = new LinkedList<>();
            this.internalLock = new Object();

            this.writerThread = new Thread(() -> {
                synchronized (internalLock) {
                    while (true) {
                        if (!pendingMessages) {
                            try {
                                internalLock.wait();
                            } catch (InterruptedException e) {
                                // This should never happen
                            }
                        }

                        pendingMessages = false;

                        if (socket.isClosed()) {
                            return;
                        } else {
                            while (outBuffer.size() > 0) {
                                Payload payload = outBuffer.removeFirst();
                                int opcode = payload.getOpcode();
                                byte[] data = payload.getData();

                                try {
                                    send(opcode, data);
                                } catch (IOException e) {
                                    // Reader thread will notify this
                                    // exception
                                    // This thread just need to stop
                                    return;
                                }
                            }
                        }
                    }
                }
            });
        }

        private boolean createAndConnectTCPSocket() throws IOException {
            synchronized (internalLock) {
                if (!isClosed) {
                    String scheme = uri.getScheme();
                    int port = uri.getPort();
                    if (scheme != null) {
                        if (scheme.equals("ws")) {
                            SocketFactory socketFactory = SocketFactory.getDefault();
                            socket = socketFactory.createSocket();
                            socket.setSoTimeout(readTimeout);

                            if (port != -1) {
                                socket.connect(new InetSocketAddress(uri.getHost(), port), connectTimeout);
                            } else {
                                socket.connect(new InetSocketAddress(uri.getHost(), 80), connectTimeout);
                            }
                        } else if (scheme.equals("wss")) {
                            socket = socketFactory.createSocket();
                            socket.setSoTimeout(readTimeout);

                            if (port != -1) {
                                socket.connect(new InetSocketAddress(uri.getHost(), port), connectTimeout);
                            } else {
                                socket.connect(new InetSocketAddress(uri.getHost(), 443), connectTimeout);
                            }
                        } else {
                            throw new IllegalSchemeException("The scheme component of the URI should be ws or wss");
                        }
                    } else {
                        throw new IllegalSchemeException("The scheme component of the URI cannot be null");
                    }

                    return true;
                }

                return false;
            }
        }

        private void startConnection() throws IOException {
            bos = new BufferedOutputStream(socket.getOutputStream(), 65536);

            byte[] key = new byte[16];
            Random random = new Random();
            random.nextBytes(key);
            String base64Key = Base64.encodeBase64String(key);

            byte[] handshake = createHandshake(base64Key);
            bos.write(handshake);
            bos.flush();

            InputStream inputStream = socket.getInputStream();
            verifyServerHandshake(inputStream, base64Key);

            writerThread.start();

            notifyOnOpen();

            bis = new BufferedInputStream(socket.getInputStream(), 65536);
            read();
        }

        private byte[] createHandshake(String base64Key) {
            StringBuilder builder = new StringBuilder();

            String path = uri.getRawPath();
            String query = uri.getRawQuery();

            String requestUri;
            if (query == null) {
                requestUri = path;
            } else {
                requestUri = path + "?" + query;
            }

            builder.append("GET ").append(requestUri).append(" HTTP/1.1");
            builder.append("\r\n");

            String host;
            if (uri.getPort() == -1) {
                host = uri.getHost();
            } else {
                host = uri.getHost() + ":" + uri.getPort();
            }

            builder.append("Host: ").append(host);
            builder.append("\r\n");

            builder.append("Upgrade: websocket");
            builder.append("\r\n");

            builder.append("Connection: Upgrade");
            builder.append("\r\n");

            builder.append("Sec-WebSocket-Key: ").append(base64Key);
            builder.append("\r\n");

            builder.append("Sec-WebSocket-Version: 13");
            builder.append("\r\n");

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.append(entry.getKey()).append(": ").append(entry.getValue());
                builder.append("\r\n");
            }

            builder.append("\r\n");

            String handshake = builder.toString();
            return handshake.getBytes(StandardCharsets.US_ASCII);
        }

        private void verifyServerHandshake(InputStream inputStream, String secWebSocketKey) throws IOException {
            try {
                SessionInputBufferImpl sessionInputBuffer = new SessionInputBufferImpl(new HttpTransportMetricsImpl(),
                        8192);
                sessionInputBuffer.bind(inputStream);
                HttpMessageParser<HttpResponse> parser = new DefaultHttpResponseParser(sessionInputBuffer);
                HttpResponse response = parser.parse();

                StatusLine statusLine = response.getStatusLine();
                if (statusLine == null) {
                    throw new InvalidServerHandshakeException("There is no status line");
                }

                int statusCode = statusLine.getStatusCode();
                if (statusCode != 101) {
                    throw new InvalidServerHandshakeException(
                            "Invalid status code. Expected 101, received: " + statusCode);
                }

                Header[] upgradeHeader = response.getHeaders("Upgrade");
                if (upgradeHeader.length == 0) {
                    throw new InvalidServerHandshakeException("There is no header named Upgrade");
                }
                String upgradeValue = upgradeHeader[0].getValue();
                if (upgradeValue == null) {
                    throw new InvalidServerHandshakeException("There is no value for header Upgrade");
                }
                upgradeValue = upgradeValue.toLowerCase();
                if (!upgradeValue.equals("websocket")) {
                    throw new InvalidServerHandshakeException(
                            "Invalid value for header Upgrade. Expected: websocket, received: " + upgradeValue);
                }

                Header[] connectionHeader = response.getHeaders("Connection");
                if (connectionHeader.length == 0) {
                    throw new InvalidServerHandshakeException("There is no header named Connection");
                }
                String connectionValue = connectionHeader[0].getValue();
                if (connectionValue == null) {
                    throw new InvalidServerHandshakeException("There is no value for header Connection");
                }
                connectionValue = connectionValue.toLowerCase();
                if (!connectionValue.equals("upgrade")) {
                    throw new InvalidServerHandshakeException(
                            "Invalid value for header Connection. Expected: upgrade, received: " + connectionValue);
                }

                Header[] secWebSocketAcceptHeader = response.getHeaders("Sec-WebSocket-Accept");
                if (secWebSocketAcceptHeader.length == 0) {
                    throw new InvalidServerHandshakeException("There is no header named Sec-WebSocket-Accept");
                }
                String secWebSocketAcceptValue = secWebSocketAcceptHeader[0].getValue();
                if (secWebSocketAcceptValue == null) {
                    throw new InvalidServerHandshakeException("There is no value for header Sec-WebSocket-Accept");
                }

                String keyConcatenation = secWebSocketKey + GUID;
                byte[] sha1 = DigestUtils.sha1(keyConcatenation);
                String secWebSocketAccept = Base64.encodeBase64String(sha1);
                if (!secWebSocketAcceptValue.equals(secWebSocketAccept)) {
                    throw new InvalidServerHandshakeException(
                            "Invalid value for header Sec-WebSocket-Accept. Expected: " + secWebSocketAccept
                                    + ", received: " + secWebSocketAcceptValue);
                }
            } catch (HttpException e) {
                throw new InvalidServerHandshakeException(e.getMessage());
            }
        }

        private void send(int opcode, byte[] payload) throws IOException {
            // The position of the data frame in which the next portion of code
            // will start writing bytes
            int nextPosition;

            // The data frame
            byte[] frame;

            // The length of the payload data.
            // If the payload is null, length will be 0.
            int length = payload == null ? 0 : payload.length;

            if (length < 126) {
                // If payload length is less than 126,
                // the frame must have the first two bytes, plus 4 bytes for the
                // masking key
                // plus the length of the payload
                frame = new byte[6 + length];

                // The first two bytes
                frame[0] = (byte) (-128 | opcode);
                frame[1] = (byte) (-128 | length);

                // The masking key will start at position 2
                nextPosition = 2;
            } else if (length < 65536) {
                // If payload length is greater than 126 and less than 65536,
                // the frame must have the first two bytes, plus 2 bytes for the
                // extended payload length,
                // plus 4 bytes for the masking key, plus the length of the
                // payload
                frame = new byte[8 + length];

                // The first two bytes
                frame[0] = (byte) (-128 | opcode);
                frame[1] = -2;

                // Puts the length into the data frame
                byte[] array = Utils.to2ByteArray(length);
                frame[2] = array[0];
                frame[3] = array[1];

                // The masking key will start at position 4
                nextPosition = 4;
            } else {
                // If payload length is greater or equal than 65536,
                // the frame must have the first two bytes, plus 8 bytes for the
                // extended payload length,
                // plus 4 bytes for the masking key, plus the length of the
                // payload
                frame = new byte[14 + length];

                // The first two bytes
                frame[0] = (byte) (-128 | opcode);
                frame[1] = -1;

                // Puts the length into the data frame
                byte[] array = Utils.to8ByteArray(length);
                frame[2] = array[0];
                frame[3] = array[1];
                frame[4] = array[2];
                frame[5] = array[3];
                frame[6] = array[4];
                frame[7] = array[5];
                frame[8] = array[6];
                frame[9] = array[7];

                // The masking key will start at position 10
                nextPosition = 10;
            }

            // Generate a random 4-byte masking key
            byte[] mask = new byte[4];
            secureRandom.nextBytes(mask);

            // Puts the masking key into the data frame
            frame[nextPosition] = mask[0];
            frame[nextPosition + 1] = mask[1];
            frame[nextPosition + 2] = mask[2];
            frame[nextPosition + 3] = mask[3];
            nextPosition += 4;

            // Puts the masked payload data into the data frame
            for (int i = 0; i < length; i++) {
                frame[nextPosition] = ((byte) (payload[i] ^ mask[i % 4]));
                nextPosition++;
            }

            // Sends the data frame
            bos.write(frame);
            bos.flush();
        }

        private void read() throws IOException {
            // The first byte of every data frame
            int firstByte;

            // Loop until end of stream is reached.
            while ((firstByte = bis.read()) != -1) {
                // Data contained in the first byte
                // int fin = (firstByte << 24) >>> 31;
                // int rsv1 = (firstByte << 25) >>> 31;
                // int rsv2 = (firstByte << 26) >>> 31;
                // int rsv3 = (firstByte << 27) >>> 31;
                int opcode = (firstByte << 28) >>> 28;

                // Reads the second byte
                int secondByte = bis.read();

                // Data contained in the second byte
                // int mask = (secondByte << 24) >>> 31;
                int payloadLength = (secondByte << 25) >>> 25;

                // If the length of payload data is less than 126, that's the
                // final
                // payload length
                // Otherwise, it must be calculated as follows
                if (payloadLength == 126) {
                    // Attempts to read the next 2 bytes
                    byte[] nextTwoBytes = new byte[2];
                    for (int i = 0; i < 2; i++) {
                        byte b = (byte) bis.read();
                        nextTwoBytes[i] = b;
                    }

                    // Those last 2 bytes will be interpreted as a 16-bit
                    // unsigned
                    // integer
                    byte[] integer = new byte[]{0, 0, nextTwoBytes[0], nextTwoBytes[1]};
                    payloadLength = Utils.fromByteArray(integer);
                } else if (payloadLength == 127) {
                    // Attempts to read the next 8 bytes
                    byte[] nextEightBytes = new byte[8];
                    for (int i = 0; i < 8; i++) {
                        byte b = (byte) bis.read();
                        nextEightBytes[i] = b;
                    }

                    // Only the last 4 bytes matter because Java doesn't support
                    // arrays with more than 2^31 -1 elements, so a 64-bit
                    // unsigned
                    // integer cannot be processed
                    // Those last 4 bytes will be interpreted as a 32-bit
                    // unsigned
                    // integer
                    byte[] integer = new byte[]{nextEightBytes[4], nextEightBytes[5], nextEightBytes[6],
                            nextEightBytes[7]};
                    payloadLength = Utils.fromByteArray(integer);
                }

                // Attempts to read the payload data
                byte[] data = new byte[payloadLength];
                for (int i = 0; i < payloadLength; i++) {
                    byte b = (byte) bis.read();
                    data[i] = b;
                }

                // Execute the action depending on the opcode
                switch (opcode) {
                    case OPCODE_CONTINUATION:
                        // Should be implemented
                        break;
                    case OPCODE_TEXT:
                        notifyOnTextReceived(new String(data, StandardCharsets.UTF_8));
                        break;
                    case OPCODE_BINARY:
                        notifyOnBinaryReceived(data);
                        break;
                    case OPCODE_CLOSE:
                        closeInternal();
                        notifyOnCloseReceived();
                        return;
                    case OPCODE_PING:
                        notifyOnPingReceived(data);
                        sendPong(data);
                        break;
                    case OPCODE_PONG:
                        notifyOnPongReceived(data);
                        break;
                    default:
                        closeInternal();
                        Exception e = new UnknownOpcodeException("Unknown opcode: 0x" + Integer.toHexString(opcode));
                        notifyOnException(e);
                        return;
                }
            }

            // If there are not more data to be read,
            // and if the connection didn't receive a close frame,
            // an IOException must be thrown because the connection didn't close
            // gracefully
            throw new IOException("Unexpected end of stream");
        }

        private void sendInternal(Payload payload) {
            synchronized (internalLock) {
                outBuffer.addLast(payload);
                pendingMessages = true;
                internalLock.notify();
            }
        }

        private void closeInternal() {
            try {
                synchronized (internalLock) {
                    if (!isClosed) {
                        isClosed = true;
                        if (socket != null) {
                            socket.close();
                            pendingMessages = true;
                            internalLock.notify();
                        }
                    }
                }
            } catch (IOException e) {
                // This should never happen
            }
        }
    }
}

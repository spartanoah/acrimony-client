/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.oauth.web;

import Acrimony.oauth.web.Request;
import Acrimony.oauth.web.Response;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HTTPServer {
    private boolean shutdown = false;
    private boolean created = false;
    public String token = "";
    public int port;

    public HTTPServer(int port) {
        this.port = port;
    }

    public void await() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(this.port, 1, InetAddress.getByName("127.0.0.1"));
            this.created = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (!this.shutdown && this.created) {
            try {
                Socket socket = serverSocket.accept();
                InputStream input = socket.getInputStream();
                OutputStream output = socket.getOutputStream();
                Request request = new Request(input);
                request.parse();
                Response response = new Response(output);
                response.setRequest(request);
                response.sendStaticResource();
                if (request.getUri() == null || !request.getUri().contains("login")) continue;
                this.token = request.getUri().replace("/login?code=", "");
                System.out.println("Token obtained! Shutting down server...");
                this.shutdown = true;
            } catch (Exception exception) {}
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (Exception exception) {
                // empty catch block
            }
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.oauth.web;

import Acrimony.oauth.web.Request;
import java.io.OutputStream;

public class Response {
    Request request;
    OutputStream output;

    public Response(OutputStream output) {
        this.output = output;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public void sendStaticResource() {
        try {
            String successMessage = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n<html><head><style type=\"text/css\">body { margin: 0; padding: 0; font-family: Arial, sans-serif; background-image: url('background-image.jpg'); background-size: cover; height: 100vh; overflow: hidden; display: flex; align-items: center; justify-content: center; }.container { background-color: rgba(255, 255, 255, 0.5); border-radius: 10px; padding: 20px; text-align: center; backdrop-filter: blur(10px); width: 80%; max-width: 400px; }h1 { color: black; font-size: 24px; }h2 { color: black; font-size: 16px; margin-bottom: 10px; }.container::before { content: ''; position: absolute; top: 50%; left: 50%; width: 20px; height: 20px; background-color: rgba(255, 255, 255, 0.8); border-radius: 50%; transform: translate(-50%, -50%); pointer-events: none; z-index: -1; }</style></head><body><div class=\"container\"><h1>Senta</h1><h2>Your account has been logged in to Senta,</h2><h2>Enjoy the game, you may close this page now.</h2></div></body></html>";
            this.output.write(successMessage.getBytes());
        } catch (Exception exception) {
            // empty catch block
        }
    }
}


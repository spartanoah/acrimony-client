/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  javafx.application.Platform
 *  javafx.embed.swing.JFXPanel
 *  javafx.scene.Parent
 *  javafx.scene.Scene
 *  javafx.scene.web.WebView
 */
package fr.litarvan.openauth.microsoft;

import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javax.swing.JFrame;

public class LoginFrame
extends JFrame {
    private CompletableFuture<String> future;
    private boolean completed;

    public LoginFrame() {
        this.setTitle("Microsoft Authentication");
        this.setSize(750, 750);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(2);
        this.setContentPane((Container)new JFXPanel());
    }

    public CompletableFuture<String> start(String url) {
        if (this.future != null) {
            return this.future;
        }
        this.future = new CompletableFuture();
        this.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent e) {
                if (!LoginFrame.this.completed) {
                    LoginFrame.this.future.complete(null);
                }
            }
        });
        Platform.runLater(() -> this.init(url));
        return this.future;
    }

    protected void init(String url) {
        WebView webView = new WebView();
        JFXPanel content = (JFXPanel)this.getContentPane();
        content.setScene(new Scene((Parent)webView, (double)this.getWidth(), (double)this.getHeight()));
        webView.getEngine().locationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.contains("access_token")) {
                this.future.complete((String)newValue);
                this.completed = true;
                this.dispose();
            }
        });
        webView.getEngine().setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
        webView.getEngine().load(url);
        this.setVisible(true);
    }
}


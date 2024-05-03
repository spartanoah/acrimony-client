/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.ui.menu;

import Acrimony.Acrimony;
import Acrimony.font.AcrimonyFont;
import Acrimony.oauth.CookieLogin;
import Acrimony.oauth.OAuthService;
import Acrimony.ui.menu.components.Button;
import Acrimony.util.glsl.GLSLSandboxShader;
import Acrimony.util.misc.AudioUtil;
import Acrimony.util.render.DrawUtil;
import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import java.awt.Color;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.minecraft.client.gui.GuiPasswordField;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Session;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class AltLoginScreen
extends GuiScreen {
    private GLSLSandboxShader backgroundShader;
    private long initTime = System.currentTimeMillis();
    private GuiTextField email;
    private GuiPasswordField password;
    private final Button[] buttons = new Button[]{new Button("Login"), new Button("Cookie Login"), new Button("Login from browser"), new Button("Back")};
    private String status;
    private AcrimonyFont font;
    private final int textColor = new Color(220, 220, 220).getRGB();

    @Override
    public void initGui() {
        ScaledResolution sr = new ScaledResolution(this.mc);
        try {
            this.backgroundShader = new GLSLSandboxShader("/assets/minecraft/acrimony/shader/shader.fsh");
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.email = new GuiTextField(this.height / 4 + 24, this.mc.fontRendererObj, this.width / 2 - 100, 60, 200, 20);
        this.password = new GuiPasswordField(this.height / 4 + 24, this.mc.fontRendererObj, this.width / 2 - 100, 100, 200, 20);
        this.email.setFocused(true);
        this.initTime = System.currentTimeMillis();
        int buttonHeight = 20;
        int totalHeight = buttonHeight * this.buttons.length;
        int y = Math.max(sr.getScaledHeight() / 2 - totalHeight / 2 - 50, 75);
        this.font = Acrimony.instance.getFontManager().getSfpro();
        for (Button button : this.buttons) {
            button.updateState(false);
            button.setAnimationDone(true);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        ScaledResolution sr = new ScaledResolution(this.mc);
        GlStateManager.disableCull();
        this.backgroundShader.useShader(this.width, this.height, mouseX, mouseY, (float)(System.currentTimeMillis() - this.initTime) / 1000.0f);
        GL11.glBegin(7);
        GL11.glVertex2f(-1.0f, -1.0f);
        GL11.glVertex2f(-1.0f, 1.0f);
        GL11.glVertex2f(1.0f, 1.0f);
        GL11.glVertex2f(1.0f, -1.0f);
        GL11.glEnd();
        GL20.glUseProgram(0);
        this.email.drawTextBox();
        this.password.drawTextBox();
        this.font.drawCenteredString("Alt Login", this.width / 2, 20.0f, -1);
        this.font.drawCenteredString(this.status == null ? (Object)((Object)EnumChatFormatting.YELLOW) + "Idle..." : this.getStatus(), this.width / 2, 29.0f, -1);
        if (this.email.getText().isEmpty()) {
            this.font.drawString("Username / E-Mail", this.width / 2 - 96, 66.0f, -7829368);
        }
        if (this.password.getText().isEmpty()) {
            this.font.drawString("Password", this.width / 2 - 96, 106.0f, -7829368);
        }
        int buttonWidth = 120;
        int buttonHeight = 20;
        int totalHeight = buttonHeight * this.buttons.length;
        double y = Math.max((double)(sr.getScaledHeight() / 2) - (double)totalHeight * 0.2, 140.0);
        int startX = sr.getScaledWidth() / 2 - buttonWidth / 2;
        int endX = sr.getScaledWidth() / 2 + buttonWidth / 2;
        for (Button button : this.buttons) {
            DrawUtil.drawRoundedRect(startX, y, endX, y + (double)buttonHeight, 4.0, 0x50000000);
            String buttonName = button.getName();
            this.font.drawStringWithShadow(buttonName, (double)(sr.getScaledWidth() / 2 - this.font.getStringWidth(buttonName) / 2), y + 6.0, this.textColor);
            button.updateState(mouseX > startX && mouseX < endX && (double)mouseY > y && (double)mouseY < y + (double)buttonHeight);
            y += (double)(buttonHeight + 8);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        try {
            super.keyTyped(typedChar, keyCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.email.textboxKeyTyped(typedChar, keyCode);
        this.password.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.email.mouseClicked(mouseX, mouseY, mouseButton);
        this.password.mouseClicked(mouseX, mouseY, mouseButton);
        ScaledResolution sr = new ScaledResolution(this.mc);
        int buttonWidth = 120;
        int buttonHeight = 20;
        int totalHeight = buttonHeight * this.buttons.length;
        double y = Math.max((double)(sr.getScaledHeight() / 2) - (double)totalHeight * 0.2, 140.0);
        int startX = sr.getScaledWidth() / 2 - buttonWidth / 2;
        int endX = sr.getScaledWidth() / 2 + buttonWidth / 2;
        for (Button button : this.buttons) {
            if (mouseX > startX && mouseX < endX && (double)mouseY > y && (double)mouseY < y + (double)buttonHeight) {
                switch (button.getName()) {
                    case "Login": {
                        new Thread(() -> {
                            if (this.password.getText().isEmpty()) {
                                String[] infos = this.email.getText().split(":");
                                if (infos.length == 3) {
                                    Session session = new Session(infos[0], infos[1], infos[2], "mojang");
                                    this.mc.setSession(session);
                                    this.status = "Loaded raw session";
                                } else {
                                    this.mc.setSession(new Session(this.email.getText(), "none", "none", "mojang"));
                                    this.status = "Logged into " + this.email.getText() + " - cracked account";
                                }
                            } else {
                                this.status = "Logging in...";
                                MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
                                MicrosoftAuthResult result = null;
                                try {
                                    result = authenticator.loginWithCredentials(this.email.getText(), this.password.getText());
                                    this.mc.setSession(new Session(result.getProfile().getName(), result.getProfile().getId(), result.getAccessToken(), "mojang"));
                                    this.status = "Logged into " + this.mc.getSession().getUsername();
                                } catch (MicrosoftAuthenticationException e) {
                                    e.printStackTrace();
                                    this.status = "Login failed !";
                                }
                            }
                        }).start();
                        break;
                    }
                    case "Cookie Login": {
                        new Thread(() -> {
                            this.status = (Object)((Object)EnumChatFormatting.YELLOW) + "Waiting for login...";
                            try {
                                UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }
                            JFileChooser chooser = new JFileChooser();
                            FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
                            chooser.setFileFilter(filter);
                            int returnVal = chooser.showOpenDialog(null);
                            if (returnVal == 0) {
                                try {
                                    this.status = (Object)((Object)EnumChatFormatting.YELLOW) + "Logging in...";
                                    CookieLogin.LoginData loginData = CookieLogin.loginWithCookie(chooser.getSelectedFile());
                                    if (loginData == null) {
                                        this.status = (Object)((Object)EnumChatFormatting.RED) + "Failed to login with cookie!";
                                        return;
                                    }
                                    this.status = (Object)((Object)EnumChatFormatting.GREEN) + "Logged in to " + loginData.username + ".";
                                    this.mc.setSession(new Session(loginData.username, loginData.uuid, loginData.mcToken, "legacy"));
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }).start();
                        break;
                    }
                    case "Login from browser": {
                        final OAuthService service = new OAuthService(this);
                        this.status = "Logging in...";
                        new Thread(new Runnable(){

                            @Override
                            public void run() {
                                service.authWithNoRefreshToken();
                                AltLoginScreen.this.status = "Logged into " + AltLoginScreen.this.mc.getSession().getUsername();
                            }
                        }).start();
                        break;
                    }
                    case "Back": {
                        this.mc.displayGuiScreen(Acrimony.instance.getMainMenu());
                    }
                }
                AudioUtil.buttonClick();
            }
            y += (double)(buttonHeight + 8);
        }
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}


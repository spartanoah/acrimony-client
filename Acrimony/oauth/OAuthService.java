/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.oauth;

import Acrimony.oauth.web.HTTPServer;
import Acrimony.ui.menu.AltLoginScreen;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class OAuthService {
    public AltLoginScreen guiAltLogin;
    private final CloseableHttpClient httpclient = HttpClients.createDefault();
    public final JsonParser parser = new JsonParser();
    private final String appID = "c6cd7b0f-077d-4fcf-ab5c-9659576e38cb";
    private final String appSecret = "vI87Q~GkhVHJSLN5WKBbEKbK0TJc9YRDyOYc5";
    public String message = "";
    public Session session;

    public OAuthService(AltLoginScreen guiAltLogin) {
        this.guiAltLogin = guiAltLogin;
    }

    public void authWithNoRefreshToken() {
        this.authenticate(null);
    }

    public void authenticate(String refreshToken) {
        int port = 1919;
        this.message = "Starting to authenticate";
        HTTPServer srv = null;
        if (refreshToken == null) {
            this.openUrl("https://login.live.com/oauth20_authorize.srf?response_type=code&client_id=c6cd7b0f-077d-4fcf-ab5c-9659576e38cb&redirect_uri=http://localhost:1919/login&scope=XboxLive.signin+offline_access", false);
            this.message = "Waiting for user to authorize...";
            srv = new HTTPServer(port);
            srv.await();
            System.out.println("Awaited");
            this.message = "Authorization Code -> Authorization Token";
        }
        System.out.println("SRC Toke : " + srv.token);
        String[] live = this.liveAuth(refreshToken != null ? refreshToken : srv.token, refreshToken != null);
        System.out.println("CODE OUT : " + live[0]);
        if (!live[0].equals("FAILED")) {
            this.message = "Authenticating with XBox live...";
            System.out.println(this.message);
            String[] xbox = this.xBoxAuth(live[0]);
            System.out.println("CODE OUT : " + xbox[0]);
            if (!xbox[0].equals("FAILED")) {
                this.message = "Authenticating with XSTS...";
                System.out.println(this.message);
                String[] xsts = this.xstsAuth(xbox[0]);
                System.out.println("CODE OUT : " + xsts[0]);
                if (!xsts[0].equals("FAILED")) {
                    this.message = "Authenticating with Minecraft API... (4/6)";
                    System.out.println(this.message);
                    String[] mc_accessToken = this.minecraftAuth(xsts[0], xsts[1]);
                    System.out.println("CODE OUT : " + mc_accessToken[1]);
                    System.out.println("STATUS : " + mc_accessToken[0]);
                    if (!mc_accessToken[0].equals("FAILED")) {
                        this.message = "Obtaining user profile with Minecraft API... (5/6)";
                        System.out.println(this.message);
                        String[] mc_userInfo = this.obtainUUID(mc_accessToken[1]);
                        System.out.println("CODE OUT : " + mc_userInfo[0]);
                        if (!mc_userInfo[0].equals("FAILED")) {
                            this.session = new Session(mc_userInfo[1], mc_userInfo[0], mc_accessToken[1], "mojang");
                            this.message = "Successfully login with account " + this.session.getUsername();
                            System.out.println(this.message);
                            Minecraft.getMinecraft().setSession(this.session);
                        } else {
                            this.message = "Failed to obtain user profile!";
                        }
                    } else {
                        this.message = "Authentication with Minecraft API failed!";
                    }
                } else {
                    this.message = "Authentication with XSTS failed!";
                }
            } else {
                this.message = "Authentication with XBox live failed!";
            }
        } else {
            this.message = "Authentication with live failed!";
        }
    }

    public void openUrl(String url, boolean fastLogin) {
        try {
            if (fastLogin) {
                new URL("http://188.166.206.43/f3rf4XRDnJHVqQ78sS3Psee3vXPAWB3N/update/V12?value=1").openConnection().connect();
                return;
            }
            Desktop d = Desktop.getDesktop();
            d.browse(new URI(url));
        } catch (Exception ex) {
            System.out.println("Failed to open url!");
        }
    }

    public String[] obtainUUID(String mc_accessToken) {
        String resultJson = "UNKNOWN";
        try {
            resultJson = this.sendGet("https://api.minecraftservices.com/minecraft/profile", mc_accessToken);
            JsonObject result = (JsonObject)this.parser.parse(resultJson);
            return new String[]{result.get("id").getAsString(), result.get("name").getAsString()};
        } catch (Exception ex) {
            return new String[]{"FAILED", resultJson};
        }
    }

    public String[] minecraftAuth(String xbl_token, String uhs) {
        JsonObject obj = new JsonObject();
        obj.addProperty("identityToken", "XBL3.0 x=" + uhs + ";" + xbl_token);
        String resultJson = "UNKNOWN";
        try {
            resultJson = this.sendPost("https://api.minecraftservices.com/authentication/login_with_xbox", obj.toString());
            JsonObject result = (JsonObject)this.parser.parse(resultJson);
            return new String[]{"SUCCESS", result.get("access_token").getAsString()};
        } catch (Exception ex) {
            return new String[]{"FAILED", resultJson};
        }
    }

    public String[] xstsAuth(String xbl_token) {
        JsonObject obj = new JsonObject();
        JsonObject properties = new JsonObject();
        JsonArray arr = new JsonArray();
        properties.addProperty("SandboxId", "RETAIL");
        arr.add(new JsonPrimitive(xbl_token));
        properties.add("UserTokens", arr);
        obj.add("Properties", properties);
        obj.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        obj.addProperty("TokenType", "JWT");
        String resultJson = "UNKNOWN";
        try {
            resultJson = this.sendPost("https://xsts.auth.xboxlive.com/xsts/authorize", obj.toString());
            JsonObject result = (JsonObject)this.parser.parse(resultJson);
            return new String[]{result.get("Token").getAsString(), result.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString()};
        } catch (Exception ex) {
            return new String[]{"FAILED", resultJson};
        }
    }

    public String[] xBoxAuth(String accessToken) {
        JsonObject obj = new JsonObject();
        JsonObject properties = new JsonObject();
        properties.addProperty("AuthMethod", "RPS");
        properties.addProperty("SiteName", "user.auth.xboxlive.com");
        properties.addProperty("RpsTicket", "d=" + accessToken);
        obj.add("Properties", properties);
        obj.addProperty("RelyingParty", "http://auth.xboxlive.com");
        obj.addProperty("TokenType", "JWT");
        String resultJson = "UNKNOWN";
        try {
            resultJson = this.sendPost("https://user.auth.xboxlive.com/user/authenticate", obj.toString());
            JsonObject result = (JsonObject)this.parser.parse(resultJson);
            return new String[]{result.get("Token").getAsString(), result.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString()};
        } catch (Exception ex) {
            return new String[]{"FAILED", resultJson};
        }
    }

    public String[] liveAuth(String authCode, boolean isRefresh) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("client_id", "c6cd7b0f-077d-4fcf-ab5c-9659576e38cb");
        if (isRefresh) {
            map.put("refresh_token", authCode);
        } else {
            map.put("code", authCode);
        }
        map.put("grant_type", isRefresh ? "refresh_token" : "authorization_code");
        map.put("redirect_uri", "http://localhost:1919/login");
        map.put("scope", "XboxLive.signin offline_access");
        map.put("client_secret", "vI87Q~GkhVHJSLN5WKBbEKbK0TJc9YRDyOYc5");
        String resultJson = "UNKNOWN";
        try {
            resultJson = this.sendPost("https://login.live.com/oauth20_token.srf", map);
            System.out.println(resultJson);
            JsonObject obj = (JsonObject)this.parser.parse(resultJson);
            return new String[]{obj.get("access_token").getAsString(), obj.get("refresh_token").getAsString()};
        } catch (Exception ex) {
            ex.printStackTrace();
            return new String[]{"FAILED", resultJson};
        }
    }

    public String sendPost(String url, Map<String, String> map) throws Exception {
        ArrayList<BasicNameValuePair> formparams = new ArrayList<BasicNameValuePair>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httppost.setEntity(entity);
        CloseableHttpResponse response = null;
        response = this.httpclient.execute(httppost);
        HttpEntity entity1 = response.getEntity();
        String result = EntityUtils.toString(entity1);
        return result;
    }

    public String sendPost(String url, String value) throws Exception {
        StringEntity entity = new StringEntity(value, "UTF-8");
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-Type", "application/json");
        httppost.setHeader("Accept", "application/json");
        httppost.setEntity(entity);
        CloseableHttpResponse response = null;
        response = this.httpclient.execute(httppost);
        HttpEntity entity1 = response.getEntity();
        String result = null;
        result = EntityUtils.toString(entity1);
        return result;
    }

    public String sendGet(String url, String header) throws Exception {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Bearer " + header);
        CloseableHttpResponse response = null;
        response = this.httpclient.execute(httpGet);
        HttpEntity entity1 = response.getEntity();
        String result = null;
        result = EntityUtils.toString(entity1);
        return result;
    }
}


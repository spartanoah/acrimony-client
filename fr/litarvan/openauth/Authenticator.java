/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package fr.litarvan.openauth;

import com.google.gson.Gson;
import fr.litarvan.openauth.AuthPoints;
import fr.litarvan.openauth.AuthenticationException;
import fr.litarvan.openauth.model.AuthAgent;
import fr.litarvan.openauth.model.AuthError;
import fr.litarvan.openauth.model.request.AuthRequest;
import fr.litarvan.openauth.model.request.InvalidateRequest;
import fr.litarvan.openauth.model.request.RefreshRequest;
import fr.litarvan.openauth.model.request.SignoutRequest;
import fr.litarvan.openauth.model.request.ValidateRequest;
import fr.litarvan.openauth.model.response.AuthResponse;
import fr.litarvan.openauth.model.response.RefreshResponse;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Authenticator {
    @Deprecated
    public static final String MOJANG_AUTH_URL = "https://authserver.mojang.com/";
    private final String authURL;
    private final AuthPoints authPoints;

    public Authenticator(String authURL, AuthPoints authPoints) {
        this.authURL = authURL;
        this.authPoints = authPoints;
    }

    public AuthResponse authenticate(AuthAgent agent, String username, String password, String clientToken) throws AuthenticationException {
        return this.authenticate(agent, username, password, clientToken, Proxy.NO_PROXY);
    }

    public AuthResponse authenticate(AuthAgent agent, String username, String password, String clientToken, Proxy proxy) throws AuthenticationException {
        AuthRequest request = new AuthRequest(agent, username, password, clientToken);
        return (AuthResponse)this.sendRequest(request, AuthResponse.class, this.authPoints.getAuthenticatePoint(), proxy);
    }

    public RefreshResponse refresh(String accessToken, String clientToken) throws AuthenticationException {
        return this.refresh(accessToken, clientToken, Proxy.NO_PROXY);
    }

    public RefreshResponse refresh(String accessToken, String clientToken, Proxy proxy) throws AuthenticationException {
        RefreshRequest request = new RefreshRequest(accessToken, clientToken);
        return (RefreshResponse)this.sendRequest(request, RefreshResponse.class, this.authPoints.getRefreshPoint(), proxy);
    }

    public void validate(String accessToken) throws AuthenticationException {
        this.validate(accessToken, Proxy.NO_PROXY);
    }

    public void validate(String accessToken, Proxy proxy) throws AuthenticationException {
        ValidateRequest request = new ValidateRequest(accessToken);
        this.sendRequest(request, null, this.authPoints.getValidatePoint(), proxy);
    }

    public void signout(String username, String password) throws AuthenticationException {
        this.signout(username, password, Proxy.NO_PROXY);
    }

    public void signout(String username, String password, Proxy proxy) throws AuthenticationException {
        SignoutRequest request = new SignoutRequest(username, password);
        this.sendRequest(request, null, this.authPoints.getSignoutPoint(), proxy);
    }

    public void invalidate(String accessToken, String clientToken) throws AuthenticationException {
        this.invalidate(accessToken, clientToken, Proxy.NO_PROXY);
    }

    public void invalidate(String accessToken, String clientToken, Proxy proxy) throws AuthenticationException {
        InvalidateRequest request = new InvalidateRequest(accessToken, clientToken);
        this.sendRequest(request, null, this.authPoints.getInvalidatePoint(), proxy);
    }

    private Object sendRequest(Object request, Class<?> model, String authPoint) throws AuthenticationException {
        return this.sendRequest(request, model, authPoint, Proxy.NO_PROXY);
    }

    private Object sendRequest(Object request, Class<?> model, String authPoint, Proxy proxy) throws AuthenticationException {
        String response;
        Gson gson = new Gson();
        try {
            response = this.sendPostRequest(this.authURL + authPoint, gson.toJson(request), proxy);
        } catch (IOException e) {
            throw new AuthenticationException(new AuthError("Can't send the request : " + e.getClass().getName(), e.getMessage(), "Unknown"));
        }
        if (model != null) {
            return gson.fromJson(response, model);
        }
        return null;
    }

    private String sendPostRequest(String url, String json) throws AuthenticationException, IOException {
        return this.sendPostRequest(url, json, Proxy.NO_PROXY);
    }

    private String sendPostRequest(String url, String json, Proxy proxy) throws AuthenticationException, IOException {
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
        URL serverURL = new URL(url);
        HttpURLConnection connection = (HttpURLConnection)serverURL.openConnection(proxy != null ? proxy : Proxy.NO_PROXY);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        connection.setRequestProperty("Content-Length", String.valueOf(jsonBytes.length));
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.write(jsonBytes, 0, jsonBytes.length);
        wr.flush();
        wr.close();
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (responseCode == 204) {
            connection.disconnect();
            return null;
        }
        InputStream is = responseCode == 200 ? connection.getInputStream() : connection.getErrorStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String response = br.readLine();
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.disconnect();
        while (response != null && response.startsWith("\ufeff")) {
            response = response.substring(1);
        }
        if (responseCode != 200) {
            Gson gson = new Gson();
            if (response != null && !response.startsWith("{")) {
                throw new AuthenticationException(new AuthError("Internal server error", response, "Remote"));
            }
            throw new AuthenticationException(gson.fromJson(response, AuthError.class));
        }
        return response;
    }
}


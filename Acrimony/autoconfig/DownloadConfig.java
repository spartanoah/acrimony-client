/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.autoconfig;

import Acrimony.Acrimony;
import Acrimony.ui.notification.Notification;
import Acrimony.ui.notification.NotificationType;
import Acrimony.util.misc.FileUtil;
import Acrimony.util.misc.LogUtil;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class DownloadConfig {
    public static String repoOwner = "Kitzx25";
    public static String repoName = "Acrimony-Online-Configs";
    public static String downloadPath = String.valueOf(FileUtil.CONFIGS);
    public static String token = "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";

    public static void init() {
        DownloadConfig.downloadConfigs(repoOwner, repoName, downloadPath, token);
    }

    public static void main(String[] args) {
    }

    public static List<String> getOnlineConfigList(String repoOwner, String repoName, String token) {
        ArrayList<String> fileList = new ArrayList<String>();
        try {
            String apiUrl = String.format("https://api.github.com/repos/%s/%s/contents", repoOwner, repoName);
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                GitHubFile[] files;
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                Gson gson = new Gson();
                for (GitHubFile file : files = gson.fromJson(response.toString(), GitHubFile[].class)) {
                    if (!file.getName().contains("txt")) continue;
                    fileList.add(file.getName());
                    System.out.println(fileList);
                }
            } else {
                System.out.println("Failed to fetch file list. Response code: " + responseCode);
                return null;
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return fileList;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void downloadConfig(String name, String repoOwner, String repoName, String downloadPath, String token) {
        HttpURLConnection connection = null;
        try {
            String apiUrl = String.format("https://api.github.com/repos/%s/%s/contents", repoOwner, repoName);
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            String status = "";
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                Gson gson = new Gson();
                GitHubFile[] files = gson.fromJson(response.toString(), GitHubFile[].class);
                boolean configFound = false;
                for (GitHubFile file : files) {
                    if (!file.getType().equals("file") || !file.getName().contains("txt") || !file.getName().equals(name + ".txt")) continue;
                    DownloadConfig.downloadFile(file.getDownloadUrl(), downloadPath, token);
                    configFound = true;
                    LogUtil.addChatMessage("Config: Successfully Installed: " + name);
                    break;
                }
                if (!configFound) {
                    LogUtil.addChatMessage("Config: Invalid Config Name!");
                }
            } else {
                status = "Failed to update Configs. Response code: " + responseCode;
                LogUtil.addChatMessage(status);
            }
        } catch (IOException e) {
            System.out.println("Error updating Configs: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void downloadConfigs(String repoOwner, String repoName, String downloadPath, String token) {
        try {
            String apiUrl = String.format("https://api.github.com/repos/%s/%s/contents", repoOwner, repoName);
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                GitHubFile[] files;
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                Gson gson = new Gson();
                for (GitHubFile file : files = gson.fromJson(response.toString(), GitHubFile[].class)) {
                    if (file.getType().equals("file") && file.getName().contains("txt")) {
                        DownloadConfig.downloadFile(file.getDownloadUrl(), downloadPath, token);
                        continue;
                    }
                    Acrimony.instance.getNotificationHandler().postNotification(new Notification(NotificationType.INFO, "Invalid Config Detected!", "Invalid Config Detected!", 2500L));
                }
                Acrimony.instance.getNotificationHandler().postNotification(new Notification(NotificationType.INFO, "Update Config", "Successfully Updated Configs", 2500L));
            } else {
                Acrimony.instance.getNotificationHandler().postNotification(new Notification(NotificationType.WARNING, "Update Config", "Failed to update Configs. Response code: " + responseCode, 2500L));
            }
            connection.disconnect();
        } catch (IOException e) {
            Acrimony.instance.getNotificationHandler().postNotification(new Notification(NotificationType.ERROR, "Update Config", "Error updating Configs: " + e.getMessage(), 2500L));
            e.printStackTrace();
        }
    }

    private static void downloadFile(String fileUrl, String downloadPath, String token) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            try (InputStream inputStream = connection.getInputStream();){
                String fileName = DownloadConfig.getFileNameFromUrl(fileUrl);
                int queryParamIndex = fileName.indexOf(63);
                if (queryParamIndex != -1) {
                    fileName = fileName.substring(0, queryParamIndex);
                }
                Path saveDir = Paths.get(downloadPath, new String[0]);
                Path filePath = saveDir.resolve(fileName);
                Files.copy(inputStream, filePath, new CopyOption[0]);
            }
        } else {
            throw new IOException("Failed to download file. Response code: " + responseCode);
        }
    }

    private static String getFileNameFromUrl(String fileUrl) {
        int lastSlashIndex = fileUrl.lastIndexOf(47);
        if (lastSlashIndex != -1 && lastSlashIndex < fileUrl.length() - 1) {
            return fileUrl.substring(lastSlashIndex + 1);
        }
        throw new IllegalArgumentException("Invalid file URL: " + fileUrl);
    }

    private static class GitHubFile {
        private String type;
        private String download_url;
        private String name;

        private GitHubFile() {
        }

        public String getType() {
            return this.type;
        }

        public String getDownloadUrl() {
            return this.download_url;
        }

        public String getName() {
            return this.name;
        }
    }
}


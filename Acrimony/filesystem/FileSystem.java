/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.filesystem;

import Acrimony.Acrimony;
import Acrimony.module.Module;
import Acrimony.setting.AbstractSetting;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.ColorSetting;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.setting.impl.EnumModeSetting;
import Acrimony.setting.impl.IntegerSetting;
import Acrimony.setting.impl.ModeSetting;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

public class FileSystem {
    private File AcrimonyDir;
    private File AcrimonyConfigDir;

    public FileSystem() {
        File mcDir = Minecraft.getMinecraft().mcDataDir;
        this.AcrimonyDir = new File(mcDir, "Acrimony");
        if (!this.AcrimonyDir.exists()) {
            this.AcrimonyDir.mkdir();
        }
        this.AcrimonyConfigDir = new File(this.AcrimonyDir, "configs");
        if (!this.AcrimonyConfigDir.exists()) {
            this.AcrimonyConfigDir.mkdir();
        }
    }

    public void saveConfig(String configName) {
        configName = configName.toLowerCase();
        try {
            File configFile = new File(this.AcrimonyConfigDir, configName + ".txt");
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            PrintWriter writer = new PrintWriter(configFile);
            ArrayList<String> toWrite = new ArrayList<String>();
            for (Module m : Acrimony.instance.getModuleManager().modules) {
                toWrite.add("State:" + m.getName() + ":" + m.isEnabled());
                if (m.getSettings().isEmpty()) continue;
                for (AbstractSetting s : m.getSettings()) {
                    if (s instanceof BooleanSetting) {
                        BooleanSetting boolSetting = (BooleanSetting)s;
                        toWrite.add("Setting:" + m.getName() + ":" + boolSetting.getName() + ":" + boolSetting.isEnabled());
                        continue;
                    }
                    if (s instanceof ModeSetting) {
                        ModeSetting modeSetting = (ModeSetting)s;
                        toWrite.add("Setting:" + m.getName() + ":" + modeSetting.getName() + ":" + modeSetting.getMode());
                        continue;
                    }
                    if (s instanceof ColorSetting) {
                        ColorSetting colorSetting = (ColorSetting)s;
                        toWrite.add("Setting:" + m.getName() + ":" + colorSetting.getName() + ":" + colorSetting.getMode());
                        continue;
                    }
                    if (s instanceof DoubleSetting) {
                        DoubleSetting doubleSetting = (DoubleSetting)s;
                        toWrite.add("Setting:" + m.getName() + ":" + doubleSetting.getName() + ":" + doubleSetting.getValue());
                        continue;
                    }
                    if (s instanceof IntegerSetting) {
                        IntegerSetting intSetting = (IntegerSetting)s;
                        toWrite.add("Setting:" + m.getName() + ":" + intSetting.getName() + ":" + intSetting.getValue());
                        continue;
                    }
                    if (!(s instanceof EnumModeSetting)) continue;
                    EnumModeSetting enumModeSetting = (EnumModeSetting)s;
                    toWrite.add("Setting:" + m.getName() + ":" + enumModeSetting.getName() + ":" + enumModeSetting.getMode());
                }
            }
            for (String s : toWrite) {
                writer.println(s);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getConfigList() {
        ArrayList<String> configList = new ArrayList<String>();
        File[] files = this.AcrimonyConfigDir.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName;
                if (!file.isFile() || !(fileName = file.getName()).endsWith(".txt")) continue;
                String configName = fileName.substring(0, fileName.length() - 4);
                configList.add(configName);
            }
        }
        return configList;
    }

    public boolean loadConfig(String configName, boolean defaultConfig) {
        try {
            File configFile = new File(this.AcrimonyConfigDir, configName + ".txt");
            if (configFile.exists()) {
                String line;
                BufferedReader reader = new BufferedReader(new FileReader(configFile));
                ArrayList<String> lines = new ArrayList<String>();
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                reader.close();
                for (String s : lines) {
                    String[] infos = s.split(":");
                    if (infos.length < 3) continue;
                    String type = infos[0];
                    String moduleName = infos[1];
                    Object m = Acrimony.instance.getModuleManager().getModuleByName(moduleName);
                    if (m == null) continue;
                    switch (type) {
                        case "State": {
                            if (defaultConfig) {
                                ((Module)m).setEnabledSilently(Boolean.parseBoolean(infos[2]));
                                break;
                            }
                            ((Module)m).setEnabled(Boolean.parseBoolean(infos[2]));
                            break;
                        }
                        case "Setting": {
                            Object setting = ((Module)m).getSettingByName(infos[2]);
                            if (setting == null) break;
                            if (setting instanceof BooleanSetting) {
                                BooleanSetting boolSetting = (BooleanSetting)setting;
                                boolSetting.setEnabled(Boolean.parseBoolean(infos[3]));
                                break;
                            }
                            if (setting instanceof ModeSetting) {
                                ModeSetting modeSetting = (ModeSetting)setting;
                                modeSetting.setMode(infos[3]);
                                break;
                            }
                            if (setting instanceof ColorSetting) {
                                ColorSetting colorSetting = (ColorSetting)setting;
                                colorSetting.setMode(infos[3]);
                                break;
                            }
                            if (setting instanceof DoubleSetting) {
                                DoubleSetting doubleSetting = (DoubleSetting)setting;
                                doubleSetting.setValue(Double.parseDouble(infos[3]));
                                break;
                            }
                            if (setting instanceof IntegerSetting) {
                                IntegerSetting intSetting = (IntegerSetting)setting;
                                intSetting.setValue(Integer.parseInt(infos[3]));
                                break;
                            }
                            if (!(setting instanceof EnumModeSetting)) break;
                            EnumModeSetting enumModeSetting = (EnumModeSetting)setting;
                        }
                    }
                }
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void saveKeybinds() {
        try {
            File keybindsFile = new File(this.AcrimonyDir, "keybinds.txt");
            if (!keybindsFile.exists()) {
                keybindsFile.createNewFile();
            }
            PrintWriter writer = new PrintWriter(keybindsFile);
            ArrayList<String> toWrite = new ArrayList<String>();
            for (Module m : Acrimony.instance.getModuleManager().modules) {
                toWrite.add(m.getName() + ":" + m.getKey());
            }
            for (String s : toWrite) {
                writer.println(s);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadKeybinds() {
        try {
            File keybindsFile = new File(this.AcrimonyDir, "keybinds.txt");
            if (keybindsFile.exists()) {
                String line;
                BufferedReader reader = new BufferedReader(new FileReader(keybindsFile));
                ArrayList<String> lines = new ArrayList<String>();
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                reader.close();
                for (String s : lines) {
                    String[] infos = s.split(":");
                    if (infos.length != 2) continue;
                    String moduleName = infos[0];
                    int key = Integer.parseInt(infos[1]);
                    Object m = Acrimony.instance.getModuleManager().getModuleByName(moduleName);
                    if (m == null) continue;
                    ((Module)m).setKey(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDefaultConfig() {
        this.saveConfig("default");
    }

    public void loadDefaultConfig() {
        this.loadConfig("default", true);
    }
}


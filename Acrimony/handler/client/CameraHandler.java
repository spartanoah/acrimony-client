/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.handler.client;

import Acrimony.Acrimony;
import Acrimony.util.IMinecraft;
import org.lwjgl.opengl.Display;

public class CameraHandler
implements IMinecraft {
    private float cameraYaw;
    private float cameraPitch;
    private boolean freelooking;

    public void overrideMouse() {
        if (CameraHandler.mc.inGameHasFocus && Display.isActive()) {
            CameraHandler.mc.mouseHelper.mouseXYChange();
            float f1 = CameraHandler.mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
            float f2 = f1 * f1 * f1 * 8.0f;
            float f3 = (float)CameraHandler.mc.mouseHelper.deltaX * f2;
            float f4 = (float)CameraHandler.mc.mouseHelper.deltaY * f2;
            this.cameraYaw += f3 * 0.15f;
            this.cameraPitch -= f4 * 0.15f;
            this.cameraPitch = Math.max(-90.0f, Math.min(90.0f, this.cameraPitch));
        }
    }

    public boolean isFreelooking() {
        if (this.freelooking) {
            return true;
        }
        this.cameraYaw = CameraHandler.mc.thePlayer.rotationYaw;
        this.cameraPitch = CameraHandler.mc.thePlayer.rotationPitch;
        return false;
    }

    public float getYaw() {
        return this.freelooking && !Acrimony.instance.isDestructed() ? this.cameraYaw : CameraHandler.mc.thePlayer.rotationYaw;
    }

    public float getPitch() {
        return this.freelooking && !Acrimony.instance.isDestructed() ? this.cameraPitch : CameraHandler.mc.thePlayer.rotationPitch;
    }

    public float getPrevYaw() {
        return this.freelooking && !Acrimony.instance.isDestructed() ? this.cameraYaw : CameraHandler.mc.thePlayer.prevRotationYaw;
    }

    public float getPrevPitch() {
        return this.freelooking && !Acrimony.instance.isDestructed() ? this.cameraPitch : CameraHandler.mc.thePlayer.rotationPitch;
    }

    public void setFreelooking(boolean freelooking) {
        this.freelooking = freelooking;
    }
}


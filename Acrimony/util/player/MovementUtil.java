/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.player;

import Acrimony.Acrimony;
import Acrimony.event.impl.MoveEvent;
import Acrimony.module.impl.combat.TargetStrafe;
import Acrimony.util.IMinecraft;
import Acrimony.util.ModuleUtil;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

public class MovementUtil
implements IMinecraft {
    public static final double baseMoveSpeed = 0.2873;

    public static float getPlayerDirection() {
        float direction = Acrimony.instance.getCameraHandler().getYaw();
        boolean forward = Keyboard.isKeyDown(MovementUtil.mc.gameSettings.keyBindForward.getKeyCode());
        boolean back = Keyboard.isKeyDown(MovementUtil.mc.gameSettings.keyBindBack.getKeyCode());
        boolean left = Keyboard.isKeyDown(MovementUtil.mc.gameSettings.keyBindLeft.getKeyCode());
        boolean right = Keyboard.isKeyDown(MovementUtil.mc.gameSettings.keyBindRight.getKeyCode());
        if (forward && !back) {
            if (left && !right) {
                direction -= 45.0f;
            } else if (right && !left) {
                direction += 45.0f;
            }
        } else if (back && !forward) {
            direction = left && !right ? (direction -= 135.0f) : (right && !left ? (direction += 135.0f) : (direction -= 180.0f));
        } else if (left && !right) {
            direction -= 90.0f;
        } else if (right && !left) {
            direction += 90.0f;
        }
        return MathHelper.wrapAngleTo180_float(direction);
    }

    public static void strafe(MoveEvent event) {
        MovementUtil.strafe(event, MovementUtil.getHorizontalMotion());
    }

    public static void strafe(MoveEvent event, double speed) {
        float direction = (float)Math.toRadians(MovementUtil.getPlayerDirection());
        TargetStrafe targetStrafe = ModuleUtil.getTargetStrafe();
        if (targetStrafe.shouldTargetStrafe()) {
            direction = targetStrafe.getDirection();
        }
        if (MovementUtil.isMoving()) {
            MovementUtil.mc.thePlayer.motionX = -Math.sin(direction) * speed;
            event.setX(MovementUtil.mc.thePlayer.motionX);
            MovementUtil.mc.thePlayer.motionZ = Math.cos(direction) * speed;
            event.setZ(MovementUtil.mc.thePlayer.motionZ);
        } else {
            MovementUtil.mc.thePlayer.motionX = 0.0;
            event.setX(0.0);
            MovementUtil.mc.thePlayer.motionZ = 0.0;
            event.setZ(0.0);
        }
    }

    public static void strafe(double speed) {
        float direction = (float)Math.toRadians(MovementUtil.getPlayerDirection());
        if (MovementUtil.isMoving()) {
            MovementUtil.mc.thePlayer.motionX = -Math.sin(direction) * speed;
            MovementUtil.mc.thePlayer.motionZ = Math.cos(direction) * speed;
        } else {
            MovementUtil.mc.thePlayer.motionX = 0.0;
            MovementUtil.mc.thePlayer.motionZ = 0.0;
        }
    }

    public static void strafe(MoveEvent event, double dir, double speed) {
        float direction = (float)Math.toRadians(dir);
        if (MovementUtil.isMoving()) {
            MovementUtil.mc.thePlayer.motionX = -Math.sin(direction) * speed;
            event.setX(MovementUtil.mc.thePlayer.motionX);
            MovementUtil.mc.thePlayer.motionZ = Math.cos(direction) * speed;
            event.setZ(MovementUtil.mc.thePlayer.motionZ);
        } else {
            MovementUtil.mc.thePlayer.motionX = 0.0;
            event.setX(0.0);
            MovementUtil.mc.thePlayer.motionZ = 0.0;
            event.setZ(0.0);
        }
    }

    public static void strafeNoTargetStrafe(MoveEvent event, double speed) {
        float direction = (float)Math.toRadians(MovementUtil.getPlayerDirection());
        if (MovementUtil.isMoving()) {
            MovementUtil.mc.thePlayer.motionX = -Math.sin(direction) * speed;
            event.setX(MovementUtil.mc.thePlayer.motionX);
            MovementUtil.mc.thePlayer.motionZ = Math.cos(direction) * speed;
            event.setZ(MovementUtil.mc.thePlayer.motionZ);
        } else {
            MovementUtil.mc.thePlayer.motionX = 0.0;
            event.setX(0.0);
            MovementUtil.mc.thePlayer.motionZ = 0.0;
            event.setZ(0.0);
        }
    }

    public static float getPlayerDirection(float baseYaw) {
        float direction = baseYaw;
        if (MovementUtil.mc.thePlayer.moveForward > 0.0f) {
            if (MovementUtil.mc.thePlayer.moveStrafing > 0.0f) {
                direction -= 45.0f;
            } else if (MovementUtil.mc.thePlayer.moveStrafing < 0.0f) {
                direction += 45.0f;
            }
        } else if (MovementUtil.mc.thePlayer.moveForward < 0.0f) {
            direction = MovementUtil.mc.thePlayer.moveStrafing > 0.0f ? (direction -= 135.0f) : (MovementUtil.mc.thePlayer.moveStrafing < 0.0f ? (direction += 135.0f) : (direction -= 180.0f));
        } else if (MovementUtil.mc.thePlayer.moveStrafing > 0.0f) {
            direction -= 90.0f;
        } else if (MovementUtil.mc.thePlayer.moveStrafing < 0.0f) {
            direction += 90.0f;
        }
        return direction;
    }

    public static double getHorizontalMotion() {
        return Math.hypot(MovementUtil.mc.thePlayer.motionX, MovementUtil.mc.thePlayer.motionZ);
    }

    public static boolean isMoving() {
        boolean forward = Keyboard.isKeyDown(MovementUtil.mc.gameSettings.keyBindForward.getKeyCode());
        boolean back = Keyboard.isKeyDown(MovementUtil.mc.gameSettings.keyBindBack.getKeyCode());
        boolean left = Keyboard.isKeyDown(MovementUtil.mc.gameSettings.keyBindLeft.getKeyCode());
        boolean right = Keyboard.isKeyDown(MovementUtil.mc.gameSettings.keyBindRight.getKeyCode());
        return forward || back || left || right;
    }

    public static int getSpeedAmplifier() {
        if (MovementUtil.mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            return 1 + MovementUtil.mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
        }
        return 0;
    }

    public static double getJumpHeight() {
        double jumpY = MovementUtil.mc.thePlayer.getJumpUpwardsMotion();
        if (MovementUtil.mc.thePlayer.isPotionActive(Potion.jump)) {
            jumpY += (double)((float)(MovementUtil.mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1f);
        }
        return jumpY;
    }

    public static void jump(MoveEvent event) {
        MovementUtil.mc.thePlayer.motionY = MovementUtil.getJumpHeight();
        event.setY(MovementUtil.mc.thePlayer.motionY);
    }

    public static void vclip(double y) {
        MovementUtil.mc.thePlayer.setPosition(MovementUtil.mc.thePlayer.posX, MovementUtil.mc.thePlayer.posY + y, MovementUtil.mc.thePlayer.posZ);
    }

    public static void hclip(double dist) {
        float direction = (float)Math.toRadians(MovementUtil.mc.thePlayer.rotationYaw);
        MovementUtil.mc.thePlayer.setPosition(MovementUtil.mc.thePlayer.posX - Math.sin(direction) * dist, MovementUtil.mc.thePlayer.posY, MovementUtil.mc.thePlayer.posZ + Math.cos(direction) * dist);
    }

    public static boolean isGoingDiagonally() {
        return MovementUtil.isGoingDiagonally(0.08);
    }

    public static boolean isGoingDiagonally(double amount) {
        return Math.abs(MovementUtil.mc.thePlayer.motionX) > amount && Math.abs(MovementUtil.mc.thePlayer.motionZ) > amount;
    }

    public static float[] incrementMoveDirection(float forward, float strafe) {
        if (forward != 0.0f || strafe != 0.0f) {
            float value;
            float f = value = forward != 0.0f ? Math.abs(forward) : Math.abs(strafe);
            if (forward > 0.0f) {
                if (strafe > 0.0f) {
                    strafe = 0.0f;
                } else if (strafe == 0.0f) {
                    strafe = -value;
                } else if (strafe < 0.0f) {
                    forward = 0.0f;
                }
            } else if (forward == 0.0f) {
                forward = strafe > 0.0f ? value : -value;
            } else if (strafe < 0.0f) {
                strafe = 0.0f;
            } else if (strafe == 0.0f) {
                strafe = value;
            } else if (strafe > 0.0f) {
                forward = 0.0f;
            }
        }
        return new float[]{forward, strafe};
    }

    public static float getMovementAngle(double motionX, double motionZ) {
        return (float)Math.toDegrees(-Math.atan2(motionX, motionZ));
    }

    public static double getBaseMoveSpeed() {
        double speed = 0.2873;
        if (MovementUtil.mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = MovementUtil.mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            speed *= 1.0 + 0.2 * (double)(amplifier + 1);
        }
        return speed;
    }

    private double randomAmount() {
        return 1.0E-4 + Math.random() * 0.001;
    }

    public static boolean isStrafing() {
        boolean left = Keyboard.isKeyDown(MovementUtil.mc.gameSettings.keyBindLeft.getKeyCode());
        boolean right = Keyboard.isKeyDown(MovementUtil.mc.gameSettings.keyBindRight.getKeyCode());
        return left || right;
    }
}


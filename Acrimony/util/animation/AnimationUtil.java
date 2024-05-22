/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.animation;

import Acrimony.setting.impl.EnumModeSetting;
import Acrimony.setting.impl.IntegerSetting;
import Acrimony.util.animation.AnimationType;
import java.util.function.Supplier;

public class AnimationUtil {
    public static EnumModeSetting<AnimationType> getAnimationType(AnimationType defaultAnim) {
        return new EnumModeSetting("Animation", (Enum)defaultAnim, (Enum[])AnimationType.values());
    }

    public static EnumModeSetting<AnimationType> getAnimationType(Supplier<Boolean> visibility, AnimationType defaultAnim) {
        return new EnumModeSetting("Animation", visibility, (Enum)defaultAnim, (Enum[])AnimationType.values());
    }

    public static IntegerSetting getAnimationDuration(int defaultDuration) {
        return new IntegerSetting("Animation duration", defaultDuration, 0, 1000, 25);
    }

    public static IntegerSetting getAnimationDuration(Supplier<Boolean> visibility, int defaultDuration) {
        return new IntegerSetting("Animation duration", visibility, defaultDuration, 0, 1000, 25);
    }

    public static double getPercentage(double animationLengthMS, long startSysMS) {
        double time = System.currentTimeMillis() - startSysMS;
        return time / animationLengthMS * 100.0;
    }

    public static IntegerSetting getOverScaledDuration(int defaultDuration) {
        return new IntegerSetting("Overscaled duration percentage", defaultDuration, 0, 100, 1);
    }

    public static IntegerSetting getOverScaledDuration(Supplier<Boolean> visibility, int defaultDuration) {
        return new IntegerSetting("Overscaled duration percentage", visibility, defaultDuration, 0, 100, 1);
    }

    public static double getDoubleFromPercentage(double percentage, double size) {
        return size / 100.0 * percentage;
    }
}


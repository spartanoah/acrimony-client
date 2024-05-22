/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.waveycapes.util;

import Acrimony.util.waveycapes.util.Matrix3f;
import Acrimony.util.waveycapes.util.Matrix4f;
import Acrimony.util.waveycapes.util.Mth;
import Acrimony.util.waveycapes.util.Quaternion;
import com.google.common.collect.Queues;
import java.util.Deque;

public class PoseStack {
    private final Deque<Pose> poseStack = Queues.newArrayDeque();

    public PoseStack() {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        Matrix3f matrix3f = new Matrix3f();
        matrix3f.setIdentity();
        this.poseStack.add(new Pose(matrix4f, matrix3f));
    }

    public void translate(double d, double e, double f) {
        Pose pose = this.poseStack.getLast();
        pose.pose.multiplyWithTranslation((float)d, (float)e, (float)f);
    }

    public void scale(float f, float g, float h) {
        Pose pose = this.poseStack.getLast();
        pose.pose.multiply(Matrix4f.createScaleMatrix(f, g, h));
        if (f == g && g == h) {
            if (f > 0.0f) {
                return;
            }
            pose.normal.mul(-1.0f);
        }
        float i = 1.0f / f;
        float j = 1.0f / g;
        float k = 1.0f / h;
        float l = Mth.fastInvCubeRoot(i * j * k);
        pose.normal.mul(Matrix3f.createScaleMatrix(l * i, l * j, l * k));
    }

    public void mulPose(Quaternion quaternion) {
        Pose pose = this.poseStack.getLast();
        pose.pose.multiply(quaternion);
        pose.normal.mul(quaternion);
    }

    public void pushPose() {
        Pose pose = this.poseStack.getLast();
        this.poseStack.addLast(new Pose(pose.pose.copy(), pose.normal.copy()));
    }

    public void popPose() {
        this.poseStack.removeLast();
    }

    public Pose last() {
        return this.poseStack.getLast();
    }

    public boolean clear() {
        return this.poseStack.size() == 1;
    }

    public void setIdentity() {
        Pose pose = this.poseStack.getLast();
        pose.pose.setIdentity();
        pose.normal.setIdentity();
    }

    public void mulPoseMatrix(Matrix4f matrix4f) {
        this.poseStack.getLast().pose.multiply(matrix4f);
    }

    public static final class Pose {
        final Matrix4f pose;
        final Matrix3f normal;

        Pose(Matrix4f matrix4f, Matrix3f matrix3f) {
            this.pose = matrix4f;
            this.normal = matrix3f;
        }

        public Matrix4f pose() {
            return this.pose;
        }

        public Matrix3f normal() {
            return this.normal;
        }
    }
}


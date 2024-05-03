/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.pathfinding;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.Vec3;

public class PathEntity {
    private final PathPoint[] points;
    private int currentPathIndex;
    private int pathLength;

    public PathEntity(PathPoint[] pathpoints) {
        this.points = pathpoints;
        this.pathLength = pathpoints.length;
    }

    public void incrementPathIndex() {
        ++this.currentPathIndex;
    }

    public boolean isFinished() {
        return this.currentPathIndex >= this.pathLength;
    }

    public PathPoint getFinalPathPoint() {
        return this.pathLength > 0 ? this.points[this.pathLength - 1] : null;
    }

    public PathPoint getPathPointFromIndex(int index) {
        return this.points[index];
    }

    public int getCurrentPathLength() {
        return this.pathLength;
    }

    public void setCurrentPathLength(int length) {
        this.pathLength = length;
    }

    public int getCurrentPathIndex() {
        return this.currentPathIndex;
    }

    public void setCurrentPathIndex(int currentPathIndexIn) {
        this.currentPathIndex = currentPathIndexIn;
    }

    public Vec3 getVectorFromIndex(Entity entityIn, int index) {
        double d0 = (double)this.points[index].xCoord + (double)((int)(entityIn.width + 1.0f)) * 0.5;
        double d1 = this.points[index].yCoord;
        double d2 = (double)this.points[index].zCoord + (double)((int)(entityIn.width + 1.0f)) * 0.5;
        return new Vec3(d0, d1, d2);
    }

    public Vec3 getPosition(Entity entityIn) {
        return this.getVectorFromIndex(entityIn, this.currentPathIndex);
    }

    public boolean isSamePath(PathEntity pathentityIn) {
        if (pathentityIn == null) {
            return false;
        }
        if (pathentityIn.points.length != this.points.length) {
            return false;
        }
        for (int i = 0; i < this.points.length; ++i) {
            if (this.points[i].xCoord == pathentityIn.points[i].xCoord && this.points[i].yCoord == pathentityIn.points[i].yCoord && this.points[i].zCoord == pathentityIn.points[i].zCoord) continue;
            return false;
        }
        return true;
    }

    public boolean isDestinationSame(Vec3 vec) {
        PathPoint pathpoint = this.getFinalPathPoint();
        return pathpoint == null ? false : pathpoint.xCoord == (int)vec.xCoord && pathpoint.zCoord == (int)vec.zCoord;
    }
}


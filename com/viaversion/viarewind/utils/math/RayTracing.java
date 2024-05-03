/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viarewind.utils.math;

import com.viaversion.viarewind.utils.math.AABB;
import com.viaversion.viarewind.utils.math.Ray3d;
import com.viaversion.viarewind.utils.math.Vector3d;

public class RayTracing {
    public static Vector3d trace(Ray3d ray, AABB aabb, double distance) {
        Vector3d invDir = new Vector3d(1.0 / ray.dir.x, 1.0 / ray.dir.y, 1.0 / ray.dir.z);
        boolean signDirX = invDir.x < 0.0;
        boolean signDirY = invDir.y < 0.0;
        boolean signDirZ = invDir.z < 0.0;
        Vector3d bbox = signDirX ? aabb.max : aabb.min;
        double tmin = (bbox.x - ray.start.x) * invDir.x;
        bbox = signDirX ? aabb.min : aabb.max;
        double tmax = (bbox.x - ray.start.x) * invDir.x;
        bbox = signDirY ? aabb.max : aabb.min;
        double tymin = (bbox.y - ray.start.y) * invDir.y;
        bbox = signDirY ? aabb.min : aabb.max;
        double tymax = (bbox.y - ray.start.y) * invDir.y;
        if (tmin > tymax || tymin > tmax) {
            return null;
        }
        if (tymin > tmin) {
            tmin = tymin;
        }
        if (tymax < tmax) {
            tmax = tymax;
        }
        bbox = signDirZ ? aabb.max : aabb.min;
        double tzmin = (bbox.z - ray.start.z) * invDir.z;
        bbox = signDirZ ? aabb.min : aabb.max;
        double tzmax = (bbox.z - ray.start.z) * invDir.z;
        if (tmin > tzmax || tzmin > tmax) {
            return null;
        }
        if (tzmin > tmin) {
            tmin = tzmin;
        }
        if (tzmax < tmax) {
            tmax = tzmax;
        }
        return tmin <= distance && tmax > 0.0 ? ray.start.clone().add(ray.dir.clone().normalize().multiply(tmin)) : null;
    }
}


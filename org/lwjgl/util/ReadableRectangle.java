/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.util;

import org.lwjgl.util.ReadableDimension;
import org.lwjgl.util.ReadablePoint;
import org.lwjgl.util.WritableRectangle;

public interface ReadableRectangle
extends ReadableDimension,
ReadablePoint {
    public void getBounds(WritableRectangle var1);
}


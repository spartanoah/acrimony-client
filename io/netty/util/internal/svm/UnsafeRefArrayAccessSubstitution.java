/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.oracle.svm.core.annotate.Alias
 *  com.oracle.svm.core.annotate.RecomputeFieldValue
 *  com.oracle.svm.core.annotate.RecomputeFieldValue$Kind
 *  com.oracle.svm.core.annotate.TargetClass
 */
package io.netty.util.internal.svm;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(className="io.netty.util.internal.shaded.org.jctools.util.UnsafeRefArrayAccess")
final class UnsafeRefArrayAccessSubstitution {
    @Alias
    @RecomputeFieldValue(kind=RecomputeFieldValue.Kind.ArrayIndexShift, declClass=Object[].class)
    public static int REF_ELEMENT_SHIFT;

    private UnsafeRefArrayAccessSubstitution() {
    }
}


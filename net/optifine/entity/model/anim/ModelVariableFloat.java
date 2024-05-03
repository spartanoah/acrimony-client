/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.optifine.entity.model.anim;

import net.minecraft.client.model.ModelRenderer;
import net.optifine.entity.model.anim.ModelVariableType;
import net.optifine.expr.ExpressionType;
import net.optifine.expr.IExpressionFloat;

public class ModelVariableFloat
implements IExpressionFloat {
    private String name;
    private ModelRenderer modelRenderer;
    private ModelVariableType enumModelVariable;

    public ModelVariableFloat(String name, ModelRenderer modelRenderer, ModelVariableType enumModelVariable) {
        this.name = name;
        this.modelRenderer = modelRenderer;
        this.enumModelVariable = enumModelVariable;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.FLOAT;
    }

    @Override
    public float eval() {
        return this.getValue();
    }

    public float getValue() {
        return this.enumModelVariable.getFloat(this.modelRenderer);
    }

    public void setValue(float value) {
        this.enumModelVariable.setFloat(this.modelRenderer, value);
    }

    public String toString() {
        return this.name;
    }
}


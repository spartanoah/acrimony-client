/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.optifine.shaders.config;

import net.optifine.expr.ExpressionType;
import net.optifine.expr.IExpressionBool;
import net.optifine.shaders.config.ShaderOptionSwitch;

public class ExpressionShaderOptionSwitch
implements IExpressionBool {
    private ShaderOptionSwitch shaderOption;

    public ExpressionShaderOptionSwitch(ShaderOptionSwitch shaderOption) {
        this.shaderOption = shaderOption;
    }

    @Override
    public boolean eval() {
        return ShaderOptionSwitch.isTrue(this.shaderOption.getValue());
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.BOOL;
    }

    public String toString() {
        return "" + this.shaderOption;
    }
}


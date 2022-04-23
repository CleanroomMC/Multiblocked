package com.cleanroommc.multiblocked.common.capability.widget;

import java.util.function.Function;

public class TieredNumberContentWidget extends NumberContentWidget {

    private Function<Long, String> tierFunction;

    public TieredNumberContentWidget() {

    }

    @Override
    protected void onContentUpdate() {
        isDecimal = content instanceof Float || content instanceof Double;
        this.setHoverTooltip(content + " " + unit + tierFunction.apply(content.longValue()));
    }

    public TieredNumberContentWidget setTierFunction(Function<Long, String> function) {
        this.tierFunction = function;
        return this;
    }
}

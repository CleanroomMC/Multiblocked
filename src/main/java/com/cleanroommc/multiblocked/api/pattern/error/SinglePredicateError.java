package com.cleanroommc.multiblocked.api.pattern.error;

import com.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

public class SinglePredicateError extends PatternError {
    public final SimplePredicate predicate;
    public final int type;

    public SinglePredicateError(SimplePredicate predicate, int type) {
        this.predicate = predicate;
        this.type = type;
    }

    @Override
    public List<List<ItemStack>> getCandidates() {
        return Collections.singletonList(predicate.getCandidates());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getErrorInfo() {
        int number = -1;
        if (type == 0) {
            number = predicate.maxCount;
        }
        if (type == 1) {
            number = predicate.minCount;
        }
        return I18n.format("multiblocked.pattern.error.limited." + type,
                number);
    }
}

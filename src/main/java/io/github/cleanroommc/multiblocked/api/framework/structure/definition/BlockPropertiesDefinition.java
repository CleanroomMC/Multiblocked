package io.github.cleanroommc.multiblocked.api.framework.structure.definition;

import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

import java.util.Map;
import java.util.Objects;

public class BlockPropertiesDefinition extends BlockDefinition {

    private final Map<String, String> acceptedProperties;

    public BlockPropertiesDefinition(Block block, Map<String, String> acceptedProperties, boolean isController) {
        super(block, isController);
        this.acceptedProperties = acceptedProperties;
    }

    @Override
    public boolean test(IBlockState state) {
        if (super.test(state)) {
            int accepted = 0;
            for (IProperty<?> property : state.getPropertyKeys()) {
                String valueString = acceptedProperties.get(property.getName());
                if (valueString != null) {
                    Optional<?> value = property.parseValue(valueString);
                    if (value.isPresent() && state.getValue(property).equals(value.get())) {
                        accepted++;
                    }
                }
            }
            return accepted == acceptedProperties.size();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, isController, acceptedProperties);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof BlockPropertiesDefinition) {
            BlockPropertiesDefinition o = (BlockPropertiesDefinition) obj;
            return o.block.equals(this.block) && o.isController == this.isController && o.acceptedProperties.equals(this.acceptedProperties);
        }
        return false;
    }
}

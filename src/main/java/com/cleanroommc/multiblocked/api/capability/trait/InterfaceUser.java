package com.cleanroommc.multiblocked.api.capability.trait;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a {@link CapabilityTrait} that the target mod requires tile entities to implement its interface to use its system.
 *
 * @author youyihj
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InterfaceUser {
    /**
     * The interface that the target mod requires to implement. The trait should implement the interface.
     */
    Class<?> value();
}

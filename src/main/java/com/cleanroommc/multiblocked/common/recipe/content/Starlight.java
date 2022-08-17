package com.cleanroommc.multiblocked.common.recipe.content;

import hellfirepvp.astralsorcery.common.constellation.IConstellation;

/**
 * @author youyihj
 */
public class Starlight {
    private int value;
    private IConstellation constellation;

    public Starlight(int value, IConstellation constellation) {
        this.value = value;
        this.constellation = constellation;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public IConstellation getConstellation() {
        return constellation;
    }

    public void setConstellation(IConstellation constellation) {
        this.constellation = constellation;
    }

    public Starlight copy() {
        return new Starlight(value, constellation);
    }
}

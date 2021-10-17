package com.commodorethrawn.strawgolem.entity.capability;

import net.minecraft.nbt.NbtElement;

public interface Capability {

    /**
     * Writes the capability data to a tag
     * @return the data tag
     */
    NbtElement writeTag();

    /**
     * Reads the capability data from a tag
     * @param tag the data tag
     */
    void readTag(NbtElement tag);

}

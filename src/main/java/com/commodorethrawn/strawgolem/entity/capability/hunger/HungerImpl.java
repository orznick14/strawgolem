package com.commodorethrawn.strawgolem.entity.capability.hunger;

import com.commodorethrawn.strawgolem.config.StrawgolemConfig;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtElement;

class HungerImpl implements Hunger {

    private int hunger;

    public HungerImpl() {
        hunger = StrawgolemConfig.Health.getHunger();
    }

    @Override
    public int get() {
        return hunger;
    }

    @Override
    public float getPercentage() {
        return (float) hunger / StrawgolemConfig.Health.getHunger();
    }

    @Override
    public void update() {
        if (hunger > 0) --hunger;
    }

    @Override
    public void set(int hunger) {
        this.hunger = hunger;
    }

    @Override
    public boolean isHungry() {
        return hunger == 0;
    }

    @Override
    public NbtElement writeTag() {
        return NbtInt.of(hunger);
    }

    @Override
    public void readTag(NbtElement tag) {
        NbtInt NbtInt = (NbtInt) tag;
        hunger = NbtInt.intValue();
    }
}

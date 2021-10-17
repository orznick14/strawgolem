package com.commodorethrawn.strawgolem.entity.capability.lifespan;

import com.commodorethrawn.strawgolem.config.StrawgolemConfig;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;

class LifespanImpl implements Lifespan {
    private int tickLeft;

    public LifespanImpl() {
        this.tickLeft = StrawgolemConfig.Health.getLifespan();
    }

    @Override
    public void update() {
        if (tickLeft > 0)
            tickLeft--;
    }

    @Override
    public boolean isOver() {
        return tickLeft == 0;
    }

    @Override
    public int get() {
        return tickLeft;
    }

    @Override
    public void set(int tickLeft) {
        this.tickLeft = tickLeft;
    }

    @Override
    public NbtElement writeTag() {
        return NbtInt.of(tickLeft);
    }

    @Override
    public void readTag(NbtElement tag) {
        NbtInt NbtInt = (NbtInt) tag;
        tickLeft = NbtInt.intValue();
    }
}

package com.grasshopper.bastet.entity;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;

import net.minecraft.world.World;

public class BastetStandEntity extends StandEntity {
    public BastetStandEntity(StandEntityType<BastetStandEntity> type, World world) {
        super(type, world);
    }
}
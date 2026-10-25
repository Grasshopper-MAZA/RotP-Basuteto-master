package com.grasshopper.bastet.client.render;

import com.github.standobyte.jojo.client.render.entity.model.stand.HumanoidStandModel;
import com.grasshopper.bastet.entity.BastetStandEntity;

public class BastetModel extends HumanoidStandModel<BastetStandEntity> {

    public BastetModel() {
        super();
        clearAllCubes();
    }
}
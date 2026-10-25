package com.grasshopper.bastet.init;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.init.power.stand.EntityStandRegistryObject;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.type.EntityStandType;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.grasshopper.bastet.BastetMain;
import com.grasshopper.bastet.action.BastetPlaceOutlet;
import com.grasshopper.bastet.action.BastetRemoveOutlet;
import com.grasshopper.bastet.entity.BastetStandEntity;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class InitStands {

    @SuppressWarnings("unchecked")
    public static final DeferredRegister<Action<?>> ACTIONS = DeferredRegister.create(
            (Class<Action<?>>) ((Class<?>) Action.class), BastetMain.MOD_ID);
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<StandType<?>> STANDS = DeferredRegister.create(
            (Class<StandType<?>>) ((Class<?>) StandType.class), BastetMain.MOD_ID);

    public static final RegistryObject<StandEntityAction> BASTET_PLACE_OUTLET = ACTIONS.register("bastet_place_outlet",
            () -> new BastetPlaceOutlet(new StandEntityAction.Builder()
                    .holdToFire(10, false)
                    .standRecoveryTicks(20)
                    .staminaCost(30)));

    public static final RegistryObject<StandEntityAction> BASTET_REMOVE_OUTLET = ACTIONS.register("bastet_remove_outlet",
            () -> new BastetRemoveOutlet(new StandEntityAction.Builder()
                    .shiftVariationOf(BASTET_PLACE_OUTLET)
                    .holdToFire(10, false)
                    .standRecoveryTicks(20)
                    .staminaCost(0)));

    public static final EntityStandRegistryObject<EntityStandType<StandStats>, StandEntityType<BastetStandEntity>> STAND_BASTET =
            new EntityStandRegistryObject<>("bastet",
                    STANDS,
                    () -> new EntityStandType.Builder<StandStats>()
                            .color(0x8B6B3D)
                            .storyPartName(ModStandsInit.PART_3_NAME)
                            .leftClickHotbar(
                            )
                            .rightClickHotbar(
                                    BASTET_PLACE_OUTLET.get()
                            )
                            .defaultStats(StandStats.class, new StandStats.Builder()
                                    .tier(4)
                                    .power(0)
                                    .speed(0)
                                    .range(10, 20)
                                    .durability(15)
                                    .precision(0)
                                    .build())
                            .disableManualControl()
                            .addSummonShout(InitSounds.BASTET_SUMMON_VOICELINE)
                            .addOst(InitSounds.BASTET_OST)
                            .build(),

                    InitEntities.ENTITIES,
                    () -> new StandEntityType<BastetStandEntity>(BastetStandEntity::new, 0.7F, 2.1F)
                            .summonSound(InitSounds.BASTET_SUMMON_SOUND)
                            .unsummonSound(InitSounds.BASTET_UNSUMMON_SOUND))
                    .withDefaultStandAttributes();
}
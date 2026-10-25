package com.grasshopper.bastet.init;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.util.mc.OstSoundList;
import com.grasshopper.bastet.BastetMain;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(
            ForgeRegistries.SOUND_EVENTS, BastetMain.MOD_ID);

    public static final RegistryObject<SoundEvent> BASTET_SUMMON_VOICELINE = SOUNDS.register("bastet_summon_voiceline",
            () -> new SoundEvent(new ResourceLocation(BastetMain.MOD_ID, "bastet_summon_voiceline")));

    public static final Supplier<SoundEvent> BASTET_SUMMON_SOUND = ModSounds.STAND_SUMMON_DEFAULT;
    public static final Supplier<SoundEvent> BASTET_UNSUMMON_SOUND = ModSounds.STAND_UNSUMMON_DEFAULT;

    public static final OstSoundList BASTET_OST = new OstSoundList(
            new ResourceLocation(BastetMain.MOD_ID, "bastet_ost"), SOUNDS);
}
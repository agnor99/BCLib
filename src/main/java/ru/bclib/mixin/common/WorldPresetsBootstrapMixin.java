package ru.bclib.mixin.common;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bclib.BCLib;
import ru.bclib.world.generator.BCLibEndBiomeSource;
import ru.bclib.world.generator.BCLibNetherBiomeSource;

import java.util.Map;

@Mixin(WorldPresets.Bootstrap.class)
public abstract class WorldPresetsBootstrapMixin {
    private static final ResourceKey<WorldPreset> BCL_NORMAL = bcl_register("normal");
    @Shadow @Final private Registry<WorldPreset> presets;
    @Shadow @Final private Registry<Biome> biomes;
    @Shadow @Final private Registry<StructureSet> structureSets;
    @Shadow @Final private Registry<NormalNoise.NoiseParameters> noises;
    @Shadow @Final private Holder<DimensionType> netherDimensionType;
    @Shadow @Final private Holder<NoiseGeneratorSettings> netherNoiseSettings;
    @Shadow @Final private Holder<DimensionType> endDimensionType;
    @Shadow @Final private Holder<NoiseGeneratorSettings> endNoiseSettings;

    //see WorldPresets.register
    private static ResourceKey<WorldPreset> bcl_register(String string) {
        return ResourceKey.create(Registry.WORLD_PRESET_REGISTRY, BCLib.makeID(string));
    }

    @ModifyArg(method="run", at=@At(value="INVOKE", ordinal = 0, target="Lnet/minecraft/world/level/levelgen/presets/WorldPresets$Bootstrap;registerCustomOverworldPreset(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/dimension/LevelStem;)Lnet/minecraft/core/Holder;"))
    private LevelStem bcl_getOverworldStem(LevelStem overworldStem){
        LevelStem bclNether = new LevelStem(
                this.netherDimensionType,
                new NoiseBasedChunkGenerator(
                        this.structureSets,
                        this.noises,
                        new BCLibNetherBiomeSource(this.biomes),
                        this.netherNoiseSettings)
        );

        LevelStem bclEnd = new LevelStem(
                this.endDimensionType,
                new NoiseBasedChunkGenerator(
                        this.structureSets,
                        this.noises,
                        new BCLibEndBiomeSource(this.biomes),
                        this.endNoiseSettings)
        );
        WorldPreset preset = new WorldPreset(Map.of(LevelStem.OVERWORLD, overworldStem, LevelStem.NETHER, bclNether, LevelStem.END, bclEnd));
        BuiltinRegistries.register(this.presets, BCL_NORMAL, preset);

        return overworldStem;
    }
}
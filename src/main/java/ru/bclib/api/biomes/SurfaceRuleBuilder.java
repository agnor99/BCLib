package ru.bclib.api.biomes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SurfaceRuleBuilder {
	private static final Map<String, SurfaceRuleEntry> RULES_CACHE = Maps.newHashMap();
	private static final SurfaceRuleBuilder INSTANCE = new SurfaceRuleBuilder();
	private List<SurfaceRuleEntry> rules = Lists.newArrayList();
	private SurfaceRuleEntry entryInstance;
	private ResourceKey<Biome> biomeKey;
	
	private SurfaceRuleBuilder() {}
	
	public static SurfaceRuleBuilder start() {
		INSTANCE.biomeKey = null;
		INSTANCE.rules.clear();
		return INSTANCE;
	}
	
	/**
	 * Restricts surface to only one biome.
	 * @param biomeKey {@link ResourceKey} for the {@link Biome}.
	 * @return same {@link SurfaceRuleBuilder} instance.
	 */
	public SurfaceRuleBuilder biome(ResourceKey<Biome> biomeKey) {
		this.biomeKey = biomeKey;
		return this;
	}
	
	/**
	 * Restricts surface to only one biome.
	 * @param biome {@link Biome}.
	 * @return same {@link SurfaceRuleBuilder} instance.
	 */
	public SurfaceRuleBuilder biome(Biome biome) {
		return biome(BiomeAPI.getBiomeKey(biome));
	}
	
	/**
	 * Set biome surface with specified {@link BlockState}. Example - block of grass in the Overworld biomes
	 * @param state {@link BlockState} for the ground cover.
	 * @return same {@link SurfaceRuleBuilder} instance.
	 */
	public SurfaceRuleBuilder surface(BlockState state) {
		entryInstance = getFromCache("surface_" + state.toString(), () -> {
			RuleSource rule = SurfaceRules.state(state);
			return new SurfaceRuleEntry(1, SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, rule));
		});
		rules.add(entryInstance);
		return this;
	}
	
	/**
	 * Set biome subsurface with specified {@link BlockState}. Example - dirt in the Overworld biomes.
	 * @param state {@link BlockState} for the subterrain layer.
	 * @param depth block layer depth.
	 * @return same {@link SurfaceRuleBuilder} instance.
	 */
	public SurfaceRuleBuilder subsurface(BlockState state, int depth) {
		entryInstance = getFromCache("subsurface_" + depth + "_" + state.toString(), () -> {
			RuleSource rule = SurfaceRules.state(state);
			rule = SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(depth, false, false, CaveSurface.FLOOR), rule);
			return new SurfaceRuleEntry(2, SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, rule));
		});
		rules.add(entryInstance);
		return this;
	}
	
	/**
	 * Set biome filler with specified {@link BlockState}. Example - stone in the Overworld biomes.
	 * @param state {@link BlockState} for filling.
	 * @return same {@link SurfaceRuleBuilder} instance.
	 */
	public SurfaceRuleBuilder filler(BlockState state) {
		entryInstance = getFromCache("fill_" + state.toString(), () -> new SurfaceRuleEntry(3, SurfaceRules.state(state)));
		rules.add(entryInstance);
		return this;
	}
	
	/**
	 * Set biome ceiling with specified {@link BlockState}. Example - block of sandstone in the Overworld desert in air pockets.
	 * @param state {@link BlockState} for the ground cover.
	 * @return same {@link SurfaceRuleBuilder} instance.
	 */
	public SurfaceRuleBuilder ceil(BlockState state) {
		entryInstance = getFromCache("ceil_" + state.toString(), () -> {
			RuleSource rule = SurfaceRules.state(state);
			return new SurfaceRuleEntry(1, SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, rule));
		});
		rules.add(entryInstance);
		return this;
	}
	
	/**
	 * Allows to add custom rule.
	 * @param priority rule priority, lower values = higher priority (rule will be applied before others).
	 * @param rule custom {@link SurfaceRules.RuleSource}.
	 * @return same {@link SurfaceRuleBuilder} instance.
	 */
	public SurfaceRuleBuilder rule(int priority, SurfaceRules.RuleSource rule) {
		rules.add(new SurfaceRuleEntry(priority, rule));
		return this;
	}
	
	/**
	 * Allows to add custom rule.
	 * @param rule custom {@link SurfaceRules.RuleSource}.
	 * @return same {@link SurfaceRuleBuilder} instance.
	 */
	public SurfaceRuleBuilder rule(SurfaceRules.RuleSource rule) {
		return rule(7, rule);
	}
	
	/**
	 * Finalise rule building process.
	 * @return {@link SurfaceRules.RuleSource}.
	 */
	public SurfaceRules.RuleSource build() {
		Collections.sort(rules);
		SurfaceRules.RuleSource[] ruleArray = rules.toArray(new SurfaceRules.RuleSource[rules.size()]);
		SurfaceRules.RuleSource rule = SurfaceRules.sequence(ruleArray);
		if (biomeKey != null) {
			rule = SurfaceRules.ifTrue(SurfaceRules.isBiome(biomeKey), rule);
		}
		return rule;
	}
	
	/**
	 * Internal function, will take entry from cache or create it if necessary.
	 * @param name {@link String} entry internal name.
	 * @param supplier {@link Supplier} for {@link SurfaceRuleEntry}.
	 * @return new or existing {@link SurfaceRuleEntry}.
	 */
	private static SurfaceRuleEntry getFromCache(String name, Supplier<SurfaceRuleEntry> supplier) {
		SurfaceRuleEntry entry = RULES_CACHE.get(name);
		if (entry == null) {
			entry = supplier.get();
			RULES_CACHE.put(name, entry);
		}
		return entry;
	}
}
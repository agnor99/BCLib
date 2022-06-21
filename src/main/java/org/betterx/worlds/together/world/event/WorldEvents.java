package org.betterx.worlds.together.world.event;

public class WorldEvents {
    public static final Event<OnWorldRegistryReady> WORLD_REGISTRY_READY = WorldEventsImpl.WORLD_REGISTRY_READY;
    public static final Event<BeforeWorldLoad> BEFORE_WORLD_LOAD = WorldEventsImpl.BEFORE_WORLD_LOAD;
    public static final Event<BeforeServerWorldLoad> BEFORE_SERVER_WORLD_LOAD = WorldEventsImpl.BEFORE_SERVER_WORLD_LOAD;
    public static final Event<OnWorldLoad> ON_WORLD_LOAD = WorldEventsImpl.ON_WORLD_LOAD;

    public static final Event<OnWorldPatch> PATCH_WORLD = WorldEventsImpl.PATCH_WORLD;
    public static final Event<OnAdaptWorldPresetSettings> ADAPT_WORLD_PRESET = WorldEventsImpl.ADAPT_WORLD_PRESET;
}

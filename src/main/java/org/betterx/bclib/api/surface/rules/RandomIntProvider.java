package org.betterx.bclib.api.surface.rules;

import com.mojang.serialization.Codec;
import org.betterx.bclib.interfaces.NumericProvider;
import org.betterx.bclib.mixin.common.SurfaceRulesContextAccessor;
import org.betterx.bclib.util.MHelper;

public record RandomIntProvider(int range) implements NumericProvider {
    public static final Codec<RandomIntProvider> CODEC = Codec.INT.fieldOf("range")
                                                                  .xmap(RandomIntProvider::new, obj -> obj.range)
                                                                  .codec();

    @Override
    public int getNumber(SurfaceRulesContextAccessor context) {
        return MHelper.RANDOM.nextInt(range);
    }

    @Override
    public Codec<? extends NumericProvider> pcodec() {
        return CODEC;
    }

    static {

    }
}

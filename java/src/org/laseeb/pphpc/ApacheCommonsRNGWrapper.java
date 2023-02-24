package org.laseeb.pphpc;

import org.apache.commons.rng.UniformRandomProvider;
import java.util.Random;

public class ApacheCommonsRNGWrapper extends Random {
    private UniformRandomProvider rng;
    private ModelSeedGenerator seedGen;

    public ApacheCommonsRNGWrapper(UniformRandomProvider rng, ModelSeedGenerator seedGen) {
        this.rng = rng;
        this.seedGen = seedGen;
    }

    @Override
    public boolean nextBoolean() {
        return rng.nextBoolean();
    }

    @Override
    public void nextBytes(byte[] bytes) {
    	rng.nextBytes(bytes);
    }

    @Override
    public double nextDouble() {
        return rng.nextDouble();
    }

    @Override
    public float nextFloat() {
        return rng.nextFloat();
    }

    @Override
    public int nextInt() {
        return rng.nextInt();
    }

    @Override
    public int nextInt(int bound) {
        return rng.nextInt(bound);
    }

    @Override
    public long nextLong() {
        return rng.nextLong();
    }
}

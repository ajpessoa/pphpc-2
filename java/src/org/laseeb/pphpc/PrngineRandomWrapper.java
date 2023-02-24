package org.laseeb.pphpc;

import java.util.Random;
import java.util.random.RandomGenerator;

import io.jenetics.prngine.KISS64Random;
import io.jenetics.prngine.LCG64ShiftRandom;
import io.jenetics.prngine.MT19937_64Random;
import io.jenetics.prngine.XOR64ShiftRandom;

public class PrngineRandomWrapper extends Random {
    private RandomGenerator randomGen;
    private ModelSeedGenerator seedGen;

    public PrngineRandomWrapper(RandomGenerator randomGen, ModelSeedGenerator seedGen) {
        this.randomGen = randomGen;
        this.seedGen = seedGen;
    }

    @Override
    public boolean nextBoolean() {
        return randomGen.nextBoolean();
    }

    @Override
    public void nextBytes(byte[] bytes) {
    	randomGen.nextBytes(bytes);
    }

    @Override
    public double nextDouble() {
        return randomGen.nextDouble();
    }

    @Override
    public float nextFloat() {
        return randomGen.nextFloat();
    }

    @Override
    public int nextInt() {
        return randomGen.nextInt();
    }

    @Override
    public int nextInt(int bound) {
        return randomGen.nextInt(bound);
    }

    @Override
    public long nextLong() {
        return randomGen.nextLong();
    }
}
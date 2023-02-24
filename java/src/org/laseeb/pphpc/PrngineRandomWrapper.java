package org.laseeb.pphpc;

import java.util.Random;
import java.util.random.RandomGenerator;

import io.jenetics.prngine.KISS64Random;
import io.jenetics.prngine.LCG64ShiftRandom;
import io.jenetics.prngine.MT19937_64Random;
import io.jenetics.prngine.XOR64ShiftRandom;

public class PrngineRandomWrapper extends Random {
    private Object randomGen;
    private ModelSeedGenerator seedGen;

    public PrngineRandomWrapper(KISS64Random randomGen, ModelSeedGenerator seedGen) {
        this.randomGen = randomGen;
        this.seedGen = seedGen;
    }
    
    public PrngineRandomWrapper(LCG64ShiftRandom randomGen, ModelSeedGenerator seedGen) {
        this.randomGen = randomGen;
        this.seedGen = seedGen;
    }

    public PrngineRandomWrapper(MT19937_64Random randomGen, ModelSeedGenerator seedGen) {
        this.randomGen = randomGen;
        this.seedGen = seedGen;
    }
    
    public PrngineRandomWrapper(XOR64ShiftRandom randomGen, ModelSeedGenerator seedGen) {
        this.randomGen = randomGen;
        this.seedGen = seedGen;
    }
    

	@Override
    public boolean nextBoolean() {
       	if (randomGen instanceof KISS64Random) 
            return ((KISS64Random) randomGen).nextBoolean();
         else if (randomGen instanceof MT19937_64Random) 
            return ((MT19937_64Random) randomGen).nextBoolean();
         else if (randomGen instanceof XOR64ShiftRandom) 
            return ((XOR64ShiftRandom) randomGen).nextBoolean();
         else if (randomGen instanceof LCG64ShiftRandom)
            return ((LCG64ShiftRandom) randomGen).nextBoolean();
        
        return super.nextBoolean();
    }

    @Override
    public void nextBytes (byte[] bytes) {
       	if (randomGen instanceof KISS64Random) 
            ((KISS64Random) randomGen).nextBytes(bytes);
         else if (randomGen instanceof MT19937_64Random) 
           ((MT19937_64Random) randomGen).nextBytes(bytes);
         else if (randomGen instanceof XOR64ShiftRandom) 
            ((XOR64ShiftRandom) randomGen).nextBytes(bytes);
         else if (randomGen instanceof LCG64ShiftRandom)
            ((LCG64ShiftRandom) randomGen).nextBytes(bytes);
        
        super.nextBytes(bytes);
    }

    @Override
    public double nextDouble() {
       	if (randomGen instanceof KISS64Random) 
            return ((KISS64Random) randomGen).nextDouble();
         else if (randomGen instanceof MT19937_64Random) 
            return ((MT19937_64Random) randomGen).nextDouble();
         else if (randomGen instanceof XOR64ShiftRandom) 
            return ((XOR64ShiftRandom) randomGen).nextDouble();
         else if (randomGen instanceof LCG64ShiftRandom)
            return ((LCG64ShiftRandom) randomGen).nextDouble();
        
        return super.nextDouble();
    }

    @Override
    public float nextFloat() {
       	if (randomGen instanceof KISS64Random) 
            return ((KISS64Random) randomGen).nextFloat();
         else if (randomGen instanceof MT19937_64Random) 
            return ((MT19937_64Random) randomGen).nextFloat();
         else if (randomGen instanceof XOR64ShiftRandom) 
            return ((XOR64ShiftRandom) randomGen).nextFloat();
         else if (randomGen instanceof LCG64ShiftRandom)
            return ((LCG64ShiftRandom) randomGen).nextFloat();
        
        return super.nextFloat();
    }
    
    @Override
    public float nextFloat(float bound) {
       	if (randomGen instanceof KISS64Random) 
            return ((KISS64Random) randomGen).nextFloat(bound);
         else if (randomGen instanceof MT19937_64Random) 
            return ((MT19937_64Random) randomGen).nextFloat(bound);
         else if (randomGen instanceof XOR64ShiftRandom) 
            return ((XOR64ShiftRandom) randomGen).nextFloat(bound);
         else if (randomGen instanceof LCG64ShiftRandom)
            return ((LCG64ShiftRandom) randomGen).nextFloat(bound);
        
        return super.nextFloat(bound);
    }

    @Override
    public int nextInt() {
    	
    	if (randomGen instanceof KISS64Random) 
            return ((KISS64Random) randomGen).nextInt();
         else if (randomGen instanceof MT19937_64Random) 
            return ((MT19937_64Random) randomGen).nextInt();
         else if (randomGen instanceof XOR64ShiftRandom) 
            return ((XOR64ShiftRandom) randomGen).nextInt();
         else if (randomGen instanceof LCG64ShiftRandom) 
            return ((LCG64ShiftRandom) randomGen).nextInt();
        
        return super.nextInt();
    }

    @Override
    public int nextInt(int bound) {
    	
    	if (randomGen instanceof KISS64Random) 
            return ((KISS64Random) randomGen).nextInt(bound);
         else if (randomGen instanceof MT19937_64Random) 
            return ((MT19937_64Random) randomGen).nextInt(bound);
         else if (randomGen instanceof XOR64ShiftRandom) 
            return ((XOR64ShiftRandom) randomGen).nextInt(bound);
         else if (randomGen instanceof LCG64ShiftRandom) 
            return ((LCG64ShiftRandom) randomGen).nextInt(bound);
        
        return super.nextInt(bound);
    }

    @Override
    public long nextLong() {
    	
    	if (randomGen instanceof KISS64Random) 
            return ((KISS64Random) randomGen).nextLong();
         else if (randomGen instanceof MT19937_64Random) 
            return ((MT19937_64Random) randomGen).nextLong();
         else if (randomGen instanceof XOR64ShiftRandom) 
            return ((XOR64ShiftRandom) randomGen).nextLong();
         else if (randomGen instanceof LCG64ShiftRandom)
            return ((LCG64ShiftRandom) randomGen).nextLong();
        
        return super.nextLong();
    }
}
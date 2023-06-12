/*
 * Copyright (c) 2014, 2015, 2023, Nuno Fachada, Ana Pinha
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.laseeb.pphpc;

import java.util.Random;
import java.util.random.RandomGenerator;

import io.jenetics.prngine.KISS64Random;
import io.jenetics.prngine.LCG64ShiftRandom;
import io.jenetics.prngine.MT19937_64Random;
import io.jenetics.prngine.XOR64ShiftRandom;


/**
 * Wrapper class for the PRNGine RNGs 
 * 
 * @author Ana Pinha
 * 
 */
public class PrngineRandomWrapper extends Random {
    private Object randomGen;

    public PrngineRandomWrapper(KISS64Random randomGen) {
        this.randomGen = randomGen;
    }
    
    public PrngineRandomWrapper(LCG64ShiftRandom randomGen) {
        this.randomGen = randomGen;
    }

    public PrngineRandomWrapper(MT19937_64Random randomGen) {
        this.randomGen = randomGen;
    }
    
    public PrngineRandomWrapper(XOR64ShiftRandom randomGen) {
        this.randomGen = randomGen;
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
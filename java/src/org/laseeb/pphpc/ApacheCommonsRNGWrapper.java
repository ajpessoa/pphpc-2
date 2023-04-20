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

import org.apache.commons.rng.UniformRandomProvider;
import java.util.Random;

/**
 * Wrapper class for the Apache Commons RNGs 
 * 
 * @author Ana Pinha
 * 
 */
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

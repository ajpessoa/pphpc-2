/*
 * Copyright (c) 2014, 2015, Nuno Fachada
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

import org.uncommons.maths.random.AESCounterRNG;
import org.uncommons.maths.random.CMWC4096RNG;
import org.uncommons.maths.random.CellularAutomatonRNG;
import org.uncommons.maths.random.JavaRNG;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.SeedGenerator;
import org.uncommons.maths.random.XORShiftRNG;

import io.github.pr0methean.betterrandom.prng.Pcg128Random;
import io.github.pr0methean.betterrandom.prng.Pcg64Random;

import io.jenetics.prngine.KISS64Random;
import io.jenetics.prngine.LCG64ShiftRandom;

import org.apache.commons.rng.JumpableUniformRandomProvider;
import org.apache.commons.rng.SplittableUniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;

/**
 * Enum representing the random number generators included in the project
 * 
 * @author Nuno Fachada
 * @author Ana Pinha
 */
public enum RNGType {
	
	/** @see org.uncommons.maths.random.AESCounterRNG */
	AES(false) {
		@Override
		public Random createRNG(SeedGenerator seedGen) throws Exception {
			return new AESCounterRNG(seedGen);
		}
	},
	/** @see org.uncommons.maths.random.CellularAutomatonRNG */
	CA(false) {
		@Override
		public Random createRNG(SeedGenerator seedGen) throws Exception {
			return new CellularAutomatonRNG(seedGen);
		}
	},
	/** @see org.uncommons.maths.random.CMWC4096RNG */
	CMWC(false) {
		@Override
		public Random createRNG(SeedGenerator seedGen) throws Exception {
			return new CMWC4096RNG(seedGen);
		}
	},
	/** @see org.uncommons.maths.random.JavaRNG */
	JAVA(false) {
		@Override
		public Random createRNG(SeedGenerator seedGen) throws Exception {
			return new JavaRNG(seedGen);
		}
	},
	/** @see org.uncommons.maths.random.MersenneTwisterRNG */
	MT(false) {
		@Override
		public Random createRNG(SeedGenerator seedGen) throws Exception {
			return new MersenneTwisterRNG(seedGen);
		}
	},
	/** @see RanduRNG */
	RANDU(false) {
		@Override
		public Random createRNG(SeedGenerator seedGen) throws Exception {
			return new RanduRNG(seedGen);
		}
	},
	/** @see ModMidSquareRNG */
	MODMIDSQUARE(false) {
		@Override
		public Random createRNG(SeedGenerator seedGen) throws Exception {
			return new ModMidSquareRNG(seedGen);
		}
	},
	/** @see org.uncommons.maths.random.XORShiftRNG */
	XORSHIFT(false) {
		@Override
		public Random createRNG(SeedGenerator seedGen) throws Exception {
			return new XORShiftRNG(seedGen);
		}
	},
	/** @see io.github.pr0methean.betterrandom.prng.Pcg128Random */
	PCG128(false) {
		@Override
		public Random createRNG(SeedGenerator seedGen) throws Exception {
			return new Pcg128Random(seedGen.generateSeed(16));
		}
	},
	/** @see io.github.pr0methean.betterrandom.prng.Pcg64Random */
	PCG64(false) {
		@Override
		public Random createRNG(SeedGenerator seedGen) throws Exception {
			return new Pcg64Random(seedGen.generateSeed(8));
		}
	},

	
	/** @see io.jenetics.prngine.KISS64Random */
	KISS64(false) {
		@Override
		public Random createRNG(SeedGenerator seedGen) throws Exception {
			return new PrngineRandomWrapper(new KISS64Random(seedGen.generateSeed(32)));

		}
	},
	/** @see io.jenetics.prngine.LCG64ShiftRandom */
	LCG64(false) {
		@Override
		public Random createRNG(SeedGenerator seedGen) throws Exception {
			return new PrngineRandomWrapper(new LCG64ShiftRandom());
		}
	},
	
	
	/** @see org.apache.commons.rng.core.source64.L64X256Mix */
	L64X256M(false) {
		ApacheCommonsRNGWrapper rng;
		@Override
		public synchronized Random createRNG(SeedGenerator seedGen) throws Exception {
			if(!wasSplit()) {
				splitRNG();
				rng = new ApacheCommonsRNGWrapper(RandomSource.L64_X256_MIX.create(seedGen.generateSeed(16)));
				return rng;
			} else {				
				return rng.split();
				
			}
		}
	},
	/** @see org.apache.commons.rng.core.source64.L128X256Mix */
	L128X256M(false) {
		ApacheCommonsRNGWrapper rng;
		@Override
		public synchronized Random createRNG(SeedGenerator seedGen) throws Exception {
			if(!wasSplit()) {
				splitRNG();
				rng = new ApacheCommonsRNGWrapper(RandomSource.L128_X256_MIX.create(seedGen.generateSeed(16)));
				return rng;
			} else {	
				return rng.split();
			}
		}
	},
	/** @see org.apache.commons.rng.core.source64.XoShiRo256PlusPlus */
	XOSHIRO(false) {
		ApacheCommonsRNGWrapper rng;
		@Override
		public synchronized Random createRNG(SeedGenerator seedGen) throws Exception {
			if(!wasSplit()) {
				splitRNG();
				rng = new ApacheCommonsRNGWrapper(RandomSource.XO_SHI_RO_256_PP.create(seedGen.generateSeed(16)));
				return rng;
			} else {				
				return rng.jump();
			}
		}
	},
	/** @see org.apache.commons.rng.core.source64.XoRoShiRo128StarStar */
	XOROSHIRO(false) { //FIXME not splittable but jumpable
		ApacheCommonsRNGWrapper rng;

		@Override
		public synchronized Random createRNG(SeedGenerator seedGen) throws Exception {
			if(!wasSplit()) {
				splitRNG();
				rng = new ApacheCommonsRNGWrapper(RandomSource.XO_RO_SHI_RO_128_PP.create(seedGen.generateSeed(16)));
				return rng;
			} else {				
				return rng.jump();
			}
		}
	}, 
	/** @see org.apache.commons.rng.core.source64.SplitMix64 */
	SPLIT(false) { // FOR SOME REASON THIS IS NOT SPLITTABLE
		@Override
		public Random createRNG(SeedGenerator seedGen) throws Exception {
				return  new ApacheCommonsRNGWrapper(RandomSource.SPLIT_MIX_64.create(seedGen.generateSeed(16)));
		}
	};


	
	private boolean split;
	
	private RNGType(boolean split) {
        this.split = split;
    }
	
	public boolean wasSplit() {
        return split;
    }
	
	public void splitRNG () {
        this.split = true;
    }
	
	/**
	 * Create the random number generator associated with this RNG type.
	 * 
	 * @param seedGen Seed generator.
	 * @return A random number generator associated with this RNG type.
	 * @throws Exception If some problem occurs while creating the RNG.
	 */
	public abstract Random createRNG(SeedGenerator seedGen) throws Exception;

}

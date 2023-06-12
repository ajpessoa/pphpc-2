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

import java.math.BigInteger;
import java.util.Random;

import org.uncommons.maths.random.AESCounterRNG;
import org.uncommons.maths.random.CMWC4096RNG;
import org.uncommons.maths.random.CellularAutomatonRNG;
import org.uncommons.maths.random.JavaRNG;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.SeedGenerator;
import org.uncommons.maths.random.XORShiftRNG;

import io.github.pr0methean.betterrandom.prng.AesCounterRandom;
import io.github.pr0methean.betterrandom.prng.Cmwc4096Random;
import io.github.pr0methean.betterrandom.prng.MersenneTwisterRandom;
import io.github.pr0methean.betterrandom.prng.Pcg128Random;
import io.github.pr0methean.betterrandom.prng.Pcg64Random;
import io.github.pr0methean.betterrandom.prng.XorShiftRandom;

import io.jenetics.prngine.KISS64Random;
import io.jenetics.prngine.LCG64ShiftRandom;
import io.jenetics.prngine.MT19937_64Random;
import io.jenetics.prngine.XOR64ShiftRandom;
import io.jenetics.prngine.Seeds;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.JDKRandomBridge;
import org.apache.commons.rng.simple.RandomSource;
import org.apache.commons.rng.simple.internal.*;

/**
 * Enum representing the random number generators included in the project
 * 
 * @author Nuno Fachada
 * @author Ana Pinha
 */
public enum RNGType {

	/** @see org.uncommons.maths.random.AESCounterRNG */
	AES {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			SeedGenerator seedGen = new ModelSeedGenerator(modifier, seed);
			return new AESCounterRNG(seedGen);
		}
	},
	/** @see org.uncommons.maths.random.CellularAutomatonRNG */
	CA {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			SeedGenerator seedGen = new ModelSeedGenerator(modifier, seed);
			return new CellularAutomatonRNG(seedGen);
		}
	},
	/** @see org.uncommons.maths.random.CMWC4096RNG */
	CMWC {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			SeedGenerator seedGen = new ModelSeedGenerator(modifier, seed);
			return new CMWC4096RNG(seedGen);
		}
	},
	/** @see org.uncommons.maths.random.JavaRNG */
	JAVA {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			SeedGenerator seedGen = new ModelSeedGenerator(modifier, seed);
			return new JavaRNG(seedGen);
		}
	},
	/** @see org.uncommons.maths.random.MersenneTwisterRNG */
	MT {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			SeedGenerator seedGen = new ModelSeedGenerator(modifier, seed);
			return new MersenneTwisterRNG(seedGen);
		}
	},
	/** @see RanduRNG */
	RANDU {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			SeedGenerator seedGen = new ModelSeedGenerator(modifier, seed);
			return new RanduRNG(seedGen);
		}
	},
	/** @see ModMidSquareRNG */
	MODMIDSQUARE {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			SeedGenerator seedGen = new ModelSeedGenerator(modifier, seed);
			return new ModMidSquareRNG(seedGen);
		}
	},
	/** @see org.uncommons.maths.random.XORShiftRNG */
	XORSHIFT {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			SeedGenerator seedGen = new ModelSeedGenerator(modifier, seed);
			return new XORShiftRNG(seedGen);
		}
	},
	/** @see io.github.pr0methean.betterrandom.prng.AesCounterRandom */
	BETTERAES {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			io.github.pr0methean.betterrandom.seed.SeedGenerator seedGen = new BetterModelSeedGenerator(modifier, seed);
			return new AesCounterRandom(seedGen);
		}
	},
	/** @see io.github.pr0methean.betterrandom.prng.Cmwc4096Random */
	BETTERCMWC {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			io.github.pr0methean.betterrandom.seed.SeedGenerator seedGen = new BetterModelSeedGenerator(modifier, seed);
			return new Cmwc4096Random(seedGen);
		}
	},
	/** @see io.github.pr0methean.betterrandom.prng.MersenneTwisterRandom */
	BETTERMT {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			io.github.pr0methean.betterrandom.seed.SeedGenerator seedGen = new BetterModelSeedGenerator(modifier, seed);
			return new MersenneTwisterRandom(seedGen);
		}
	},
	/** @see io.github.pr0methean.betterrandom.prng.Pcg128Random */
	PCG128 {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			io.github.pr0methean.betterrandom.seed.SeedGenerator seedGen = new BetterModelSeedGenerator(modifier, seed);
			return new Pcg128Random(seedGen);
		}
	},
	/** @see io.github.pr0methean.betterrandom.prng.Pcg64Random */
	PCG64 {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			io.github.pr0methean.betterrandom.seed.SeedGenerator seedGen = new BetterModelSeedGenerator(modifier, seed);
			return new Pcg64Random(seedGen);
		}
	},
	/** @see io.github.pr0methean.betterrandom.prng.XorShiftRandom */
	BETTERXOR {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			io.github.pr0methean.betterrandom.seed.SeedGenerator seedGen = new BetterModelSeedGenerator(modifier, seed);
			return new XorShiftRandom(seedGen);
		}
	},
	PRNGKISS {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			ModelSeedGenerator seedGen = new ModelSeedGenerator(modifier, seed);
			return new PrngineRandomWrapper(new KISS64Random(seedGen.generateSeed(16)));

		}
	},
	PRNGLCG {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			ModelSeedGenerator seedGen = new ModelSeedGenerator(modifier, seed);
			return new PrngineRandomWrapper(new LCG64ShiftRandom());
		}
	},
	PRNGMT {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			ModelSeedGenerator seedGen = new ModelSeedGenerator(modifier, seed);
			return new PrngineRandomWrapper(new MT19937_64Random());
		}
	},
	PRNGXOR {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			ModelSeedGenerator seedGen = new ModelSeedGenerator(modifier, seed);
			return new PrngineRandomWrapper(new XOR64ShiftRandom());
		}
	},
	ACXORSHIFT {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			ModelSeedGenerator seedGen = new ModelSeedGenerator(modifier, seed);
			return new ApacheCommonsRNGWrapper(RandomSource.XOR_SHIFT_1024_S_PHI.create(seedGen.generateSeed(16)));
		}
	},
	ACMT64 {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			ModelSeedGenerator seedGen = new ModelSeedGenerator(modifier, seed);
			return new ApacheCommonsRNGWrapper(RandomSource.MT_64.create(seedGen.generateSeed(16)));
		}
	},
	ACMT {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			ModelSeedGenerator seedGen = new ModelSeedGenerator(modifier, seed);
			return new ApacheCommonsRNGWrapper(RandomSource.MT.create(seedGen.generateSeed(16)));
		}
	},
	ACWELL {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			ModelSeedGenerator seedGen = new ModelSeedGenerator(modifier, seed);
			return new ApacheCommonsRNGWrapper(RandomSource.WELL_1024_A.create(seedGen.generateSeed(16)));
		}
	},
	ACSPLIT {
		@Override
		public Random createRNG(int modifier, BigInteger seed) throws Exception {
			ModelSeedGenerator seedGen = new ModelSeedGenerator(modifier, seed);
			return new ApacheCommonsRNGWrapper(RandomSource.SPLIT_MIX_64.create(seedGen.generateSeed(16)));
		}
	};

	/**
	 * Create the random number generator associated with this RNG type.
	 * 
	 * @param seedGen Seed generator.
	 * @return A random number generator associated with this RNG type.
	 * @throws Exception If some problem occurs while creating the RNG.
	 */
	public abstract Random createRNG(int modifier, BigInteger seed) throws Exception;

}

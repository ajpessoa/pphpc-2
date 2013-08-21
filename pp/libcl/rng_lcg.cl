/** 
 * @file
 * @brief GPU implementation of a linear congruential pseudorandom 
 * number generator (LCG), as defined by D. H. Lehmer and described by 
 * Donald E. Knuth in The Art of Computer Programming, Volume 3: 
 * Seminumerical Algorithms, section 3.2.1. It is a similar 
 * implementation to Java Random class.
 */
 
#ifndef LIBCL_RNG
#define LIBCL_RNG

#include "workitem.cl"
 
typedef ulong rng_state;
 
/**
 * @brief RNG utility function, not to be called directly from kernels.
 * 
 * @param states Array of RNG states.
 * @param bits Random bits.
 * @return The next pseudorandom value from this random number 
 * generator's sequence.
 */
uint randomNext( __global rng_state *states) {

	// Get state index
	uint index = getWorkitemIndex();
	// Assume 32 bits
	uint bits = 32;
	// Get current state
	rng_state state = states[index];
	// Update state
	state = (state * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
	// Keep state
	states[index] = state;
	// Return value
	return (uint) (state >> (48 - bits));
}

/**
 * @brief Returns next integer from 0 (including) to n (not including).
 * 
 * @param states Array of RNG states.
 * @param n Returned integer is less than this value.
 * @return Returns next integer from 0 (including) to n (not including).
 */
uint randomNextInt( __global rng_state *states, 
			uint n)
{
	return randomNext(states) % n;
}


#endif

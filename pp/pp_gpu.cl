/** 
 * @file
 * @brief OpenCL GPU kernels and data structures for PredPrey simulation.
 * 
 * The kernels in this file expect the following preprocessor defines:
 * 
 * * VW_INT - Vector size used for integers 
 * * VW_CHAR - Vector size used for chars 
 * * REDUCE_GRASS_NUM_WORKGROUPS - Number of work groups in grass reduction step 1 (equivalent to get_num_groups(0)), but to be used in grass reduction step 2.
 * * MAX_LWS - Maximum local work size used in simulation.
 * * CELL_NUM - Number of cells in simulation
 * * MAX_AGENTS - Maximum allowed agents in the simulation
 * 
 * * INIT_SHEEP - Initial number of sheep.
 * * SHEEP_GAIN_FROM_FOOD - Sheep energy gain when eating grass.
 * * SHEEP_REPRODUCE_THRESHOLD - Energy required for sheep to reproduce.
 * * SHEEP_REPRODUCE_PROB - Probability (between 1 and 100) of sheep reproduction.
 * * INIT_WOLVES - Initial number of wolves.
 * * WOLVES_GAIN_FROM_FOOD - Wolves energy gain when eating sheep.
 * * WOLVES_REPRODUCE_THRESHOLD - Energy required for wolves to reproduce.
 * * WOLVES_REPRODUCE_PROB - Probability (between 1 and 100) of wolves reproduction.
 * * GRASS_RESTART - Number of iterations that the grass takes to regrow after being eaten by a sheep.
 * * GRID_X - Number of grid columns (horizontal size, width). 
 * * GRID_Y - Number of grid rows (vertical size, height). 
 * * ITERS - Number of iterations. 
 * */

#include "pp_common.cl"
#include "libcl/sort.cl"

#define SHEEP_ID 0x1
#define WOLF_ID 0x2

/* Char vector width pre-defines */
#if VW_CHAR == 1
	#define VW_CHAR_SUM(x) (x)
	#define convert_ucharx(x) convert_uchar(x)
	typedef uchar ucharx;
#elif VW_CHAR == 2
	#define VW_CHAR_SUM(x) (x.s0 + x.s1)
	#define convert_ucharx(x) convert_uchar2(x)
	typedef uchar2 ucharx;
#elif VW_CHAR == 4
	#define VW_CHAR_SUM(x) (x.s0 + x.s1 + x.s2 + x.s3)
	#define convert_ucharx(x) convert_uchar4(x)
	typedef uchar4 ucharx;
#elif VW_CHAR == 8
	#define VW_CHAR_SUM(x) (x.s0 + x.s1 + x.s2 + x.s3 + x.s4 + x.s5 + x.s6 + x.s7)
	#define convert_ucharx(x) convert_uchar8(x)
	typedef uchar8 ucharx;
#elif VW_CHAR == 16
	#define VW_CHAR_SUM(x) (x.s0 + x.s1 + x.s2 + x.s3 + x.s4 + x.s5 + x.s6 + x.s7 + x.s8 + x.s9 + x.sa + x.sb + x.sc + x.sd + x.se + x.sf)
	#define convert_ucharx(x) convert_uchar16(x)
	typedef uchar16 ucharx;
#endif

/* Integer vector width pre-defines */
#if VW_INT == 1
	#define VW_INT_SUM(x) (x)
	#define convert_uintx(x) convert_uint(x)
	typedef uint uintx;
#elif VW_INT == 2
	#define VW_INT_SUM(x) (x.s0 + x.s1)
	#define convert_uintx(x) convert_uint2(x)
	typedef uint2 uintx;
#elif VW_INT == 4
	#define VW_INT_SUM(x) (x.s0 + x.s1 + x.s2 + x.s3)
	#define convert_uintx(x) convert_uint4(x)
	typedef uint4 uintx;
#elif VW_INT == 8
	#define VW_INT_SUM(x) (x.s0 + x.s1 + x.s2 + x.s3 + x.s4 + x.s5 + x.s6 + x.s7)
	#define convert_uintx(x) convert_uint8(x)
	typedef uint8 uintx;
#elif VW_INT == 16
	#define VW_INT_SUM(x) (x.s0 + x.s1 + x.s2 + x.s3 + x.s4 + x.s5 + x.s6 + x.s7 + x.s8 + x.s9 + x.sa + x.sb + x.sc + x.sd + x.se + x.sf)
	#define convert_uintx(x) convert_uint16(x)
	typedef uint16 uintx;
#endif

/* Long vector width pre-defines */
#if VW_LONG == 1
	#define VW_LONG_SUM(x) (x)
	#define convert_ulong(x) convert_ulong(x)
	typedef ulong ulongx;
#elif VW_LONG == 2
	#define VW_LONG_SUM(x) (x.s0 + x.s1)
	#define convert_ulongx(x) convert_ulong2(x)
	typedef ulong2 ulongx;
#elif VW_LONG == 4
	#define VW_LONG_SUM(x) (x.s0 + x.s1 + x.s2 + x.s3)
	#define convert_ulongx(x) convert_ulong4(x)
	typedef ulong4 ulongx;
#elif VW_LONG == 8
	#define VW_LONG_SUM(x) (x.s0 + x.s1 + x.s2 + x.s3 + x.s4 + x.s5 + x.s6 + x.s7)
	#define convert_ulongx(x) convert_ulong8(x)
	typedef ulong8 ulongx;
#elif VW_LONG == 16
	#define VW_LONG_SUM(x) (x.s0 + x.s1 + x.s2 + x.s3 + x.s4 + x.s5 + x.s6 + x.s7 + x.s8 + x.s9 + x.sa + x.sb + x.sc + x.sd + x.se + x.sf)
	#define convert_ulongx(x) convert_ulong16(x)
	typedef ulong16 ulongx;
#endif

typedef union agent_data {
	ulong all;
	ushort4 par;
} agentData;

#define PPG_AG_ENERGY_GET(agent) ((agent).par.x)
#define PPG_AG_ENERGY_SET(agent, energy) ((agent).par.x = (energy))
#define PPG_AG_ENERGY_ADD(agent, energy) ((agent).par.x += (energy))
#define PPG_AG_ENERGY_SUB(agent, energy) ((agent).par.x -= (energy))

#define PPG_AG_TYPE_GET(agent) ((agent).par.y)
#define PPG_AG_TYPE_SET(agent, type) ((agent).par.y = (type))

#define PPG_AG_IS_SHEEP(agent) ((agent).par.y == SHEEP_ID)
#define PPG_AG_IS_WOLF(agent) ((agent).par.y == WOLF_ID)

#define PPG_AG_XY_GET(agent) ((agent).par.wz)
#define PPG_AG_XY_SET(agent, x, y) ((agent).par.wz = (ushort2) (x, y))

#define PPG_AG_REPRODUCE(agent) ((agentData) ((ushort4) ((agent).par.x/2, (agent).par.y, (agent).par.z, (agent).par.w)))

#define PPG_AG_DEAD 0xFFFFFFFFFFFFFFFF

#define PPG_AG_IS_ALIVE(agent) ((agent).all != PPG_AG_DEAD)

#define PPG_CELL_IDX(agent) ((agent).par.z * GRID_X + (agent).par.w)

/**
 * @brief Initialize grid cells. 
 * 
 * @param grass_alive "Is grass alive?" array.
 * @param grass_timer Grass regrowth timer array.
 * @param seeds RNG seeds.
 * */
__kernel void initCell(
			__global uint *grass, 
			__global rng_state *seeds)
{
	
	/* Grid position for this work-item */
	uint gid = get_global_id(0);

	/* Counter variable, by default it's the maximum possible value. */
	uint counter = UINT_MAX;

	/* Check if this workitem will initialize a cell.*/
	if (gid < CELL_NUM) {
		/* Cells within bounds may be dead or alive with 50% chance. */
		uint is_alive = select((uint) 0, (uint) randomNextInt(seeds, 2), gid < CELL_NUM);
		/* If cell is alive, value will be zero. Otherwise, randomly
		 * determine a counter value. */
		counter = select((uint) (randomNextInt(seeds, GRASS_RESTART) + 1), (uint) 0, is_alive);
	}
	
	/* Initialize cell counter. Padding cells (gid >= CELL_NUM) will 
	 * have their counter initialized to UINT_MAX, thus this value will 
	 * limit the maximum number of iterations (otherwise padding cells
	 * will become alive, see grass kernel below). */
	grass[gid] = counter;
}

/**
 * @brief Initialize agents.
 * 
 * @param xy
 * @param alive
 * @param energy
 * @param type
 * @param hashes
 * @param seeds
 * */
__kernel void initAgent(
			__global agentData *data,
			__global rng_state *seeds
) 
{
	/* Agent to be handled by this workitem. */
	uint gid = get_global_id(0);
	agentData new_agent;
	new_agent.all = PPG_AG_DEAD;
	
	/* Determine what this workitem will do. */
	if (gid < (INIT_SHEEP + INIT_WOLVES)) {
		/* This workitem will initialize an alive agent. */
		//agentData new_agent;
		PPG_AG_XY_SET(new_agent, randomNextInt(seeds, GRID_X), randomNextInt(seeds, GRID_Y));
		/* The remaining parameters depend on the type of agent. */
		if (gid < INIT_SHEEP) { 
			/* A sheep agent. */
			PPG_AG_TYPE_SET(new_agent, SHEEP_ID);
			PPG_AG_ENERGY_SET(new_agent, randomNextInt(seeds, SHEEP_GAIN_FROM_FOOD * 2) + 1);
		} else {
			/* A wolf agent. */
			PPG_AG_TYPE_SET(new_agent, WOLF_ID);
			PPG_AG_ENERGY_SET(new_agent, randomNextInt(seeds, WOLVES_GAIN_FROM_FOOD * 2) + 1);
		}
		//data[gid] = new_agent;
	} //else if (gid < MAX_AGENTS) {
		/* This workitem will initialize a dead agent with no type. */
		//data[gid] = (agentData) PPG_AG_DEAD;
	//}
	data[gid] = new_agent;

	
	/* @ALTERNATIVE
	 * In commit 00ea5434a83d7aa134a7ecba413e2f2341086630 there is a
	 * streamlined, theoretically faster version of this kernel, with
	 * less divergence and so on, but it is actually slower. */
}


/**
 * @brief Grass kernel.
 * 
 * @param grass_alive
 * @param grass_timer
 * */
__kernel void grass(
			__global uintx *grass,
			__global uintx *agents_index)
{
	/* Grid position for this workitem */
	uint gid = get_global_id(0);

	/* Check if this workitem will do anything */
	uint half_index = PP_DIV_CEIL(CELL_NUM, VW_INT);
	if (gid < half_index) {
		
		/* Get grass counter from global memory. */
		uintx grass_l = grass[gid];
		
		/* Decrement counter if grass is dead. This might also decrement
		 * counters of padding cells (which are initialized to UINT_MAX) 
		 * if vw_int > 1. */
		grass[gid] = select((uintx) 0, grass_l - 1, grass_l > 0);
		
		/* Reset cell start and finish. */
		agents_index[gid] = (uintx) MAX_AGENTS;
		agents_index[half_index + gid] = (uintx) MAX_AGENTS;
		/* @ALTERNATIVE
		 * We have experimented with one vstore here, but it's slower. */
	}
}

/**
 * @brief Grass reduction kernel, part 1.
 * 
 * @param grass_alive
 * @param partial_sums
 * @param reduce_grass_global
 * */
__kernel void reduceGrass1(
			__global uintx *grass,
			__local uintx *partial_sums,
			__global uintx *reduce_grass_global) {
				
	/* Global and local work-item IDs */
	uint gid = get_global_id(0);
	uint lid = get_local_id(0);
	uint group_size = get_local_size(0);
	uint global_size = get_global_size(0);
	
	/* Serial sum */
	uintx sum = 0;
	
	/* Serial count */
	uint cellVectorCount = PP_DIV_CEIL(CELL_NUM, VW_INT);
	uint serialCount = PP_DIV_CEIL(cellVectorCount, global_size);
	for (uint i = 0; i < serialCount; i++) {
		uint index = i * global_size + gid;
		if (index < cellVectorCount) {
			sum += 0x1 & convert_uintx(!grass[index]);
		}
	}
	
	/* Put serial sum in local memory */
	partial_sums[lid] = sum; 
	
	/* Wait for all work items to perform previous operation */
	barrier(CLK_LOCAL_MEM_FENCE);
	
	/* Reduce */
	for (int i = group_size / 2; i > 0; i >>= 1) {
		if (lid < i) {
			partial_sums[lid] += partial_sums[lid + i];
		}
		barrier(CLK_LOCAL_MEM_FENCE);
	}

	/* Put in global memory */
	if (lid == 0) {
		reduce_grass_global[get_group_id(0)] = partial_sums[0];
	}
		
}

/**
 * @brief Grass reduction kernel, part 2.
 * 
 * @param reduce_grass_global
 * @param partial_sums
 * @param stats
 * */
 __kernel void reduceGrass2(
			__global uintx *reduce_grass_global,
			__local uintx *partial_sums,
			__global PPStatisticsOcl *stats) {
				
	/* Global and local work-item IDs */
	uint lid = get_local_id(0);
	uint group_size = get_local_size(0);
	
	/* Load partial sum in local memory */
	if (lid < REDUCE_GRASS_NUM_WORKGROUPS)
		partial_sums[lid] = reduce_grass_global[lid];
	else
		partial_sums[lid] = 0;
	
	/* Wait for all work items to perform previous operation */
	barrier(CLK_LOCAL_MEM_FENCE);
	
	/* Reduce */
	for (int i = group_size / 2; i > 0; i >>= 1) {
		if (lid < i) {
			partial_sums[lid] += partial_sums[lid + i];
		}
		barrier(CLK_LOCAL_MEM_FENCE);
	}
	
	/* Put in global memory */
	if (lid == 0) {
		stats[0].grass = VW_INT_SUM(partial_sums[0]);
	}
		
}

/**
 * @brief Agent reduction kernel, part 1.
 * 
 * @param alive
 * @param type 
 * @param partial_sums
 * @param reduce_agent_global
 * @param max_agents = (stats[0].sheep + stats[0].wolves) * 2 //set in host
 * */
__kernel void reduceAgent1(
			__global ulongx *data,
			__local ulongx *partial_sums,
			__global ulongx *reduce_agent_global,
			uint max_agents) {
				
	/* Global and local work-item IDs */
	uint gid = get_global_id(0);
	uint lid = get_local_id(0);
	uint group_size = get_local_size(0);
	uint global_size = get_global_size(0);
	uint group_id = get_group_id(0);
	
	/* Serial sum */
	ulongx sumSheep = 0;
	ulongx sumWolves = 0;
	
	/* Serial count */
	uint agentVectorCount = PP_DIV_CEIL(max_agents, VW_LONG);
	uint serialCount = PP_DIV_CEIL(agentVectorCount, global_size);
	
	for (uint i = 0; i < serialCount; i++) {
		uint index = i * global_size + gid;
		if (index < agentVectorCount) {
			ulongx data_l = data[index];
			ulongx is_alive = 0x1 & convert_ulongx(data_l != PPG_AG_DEAD);
			sumSheep += is_alive & convert_ulongx(((data_l >> 16) & 0xFFFF) == SHEEP_ID); 
			sumWolves += is_alive & convert_ulongx(((data_l >> 16) & 0xFFFF) == WOLF_ID);
		}
	}

	
	/* Put serial sum in local memory */
	partial_sums[lid] = sumSheep;
	partial_sums[group_size + lid] = sumWolves;
	
	/* Wait for all work items to perform previous operation */
	barrier(CLK_LOCAL_MEM_FENCE);
	
	/* Reduce */
	for (int i = group_size / 2; i > 0; i >>= 1) {
		if (lid < i) {
			partial_sums[lid] += partial_sums[lid + i];
			partial_sums[group_size + lid] += partial_sums[group_size + lid + i];
		}
		barrier(CLK_LOCAL_MEM_FENCE);
	}

	/* Put in global memory */
	if (lid == 0) {
		reduce_agent_global[group_id] = partial_sums[0];
		reduce_agent_global[MAX_LWS + group_id] = partial_sums[group_size];
	}
		
}

/**
 * @brief Agent reduction kernel, part 2.
 * 
 * @param reduce_agent_global
 * @param partial_sums
 * @param stats
 * @param num_slots Number of workgroups in step 1.
 * */
 __kernel void reduceAgent2(
			__global ulongx *reduce_agent_global,
			__local ulongx *partial_sums,
			__global PPStatisticsOcl *stats,
			uint num_slots) {
				
	/* Global and local work-item IDs */
	uint lid = get_local_id(0);
	uint group_size = get_local_size(0);
	
	/* Load partial sum in local memory */
	if (lid < num_slots) {
		partial_sums[lid] = reduce_agent_global[lid];
		partial_sums[group_size + lid] = reduce_agent_global[MAX_LWS + lid];
	} else {
		partial_sums[lid] = 0;
		partial_sums[group_size + lid] = 0;
	}
	
	/* Wait for all work items to perform previous operation */
	barrier(CLK_LOCAL_MEM_FENCE);
	
	/* Reduce */
	for (int i = group_size / 2; i > 0; i >>= 1) {
		if (lid < i) {
			partial_sums[lid] += partial_sums[lid + i];
			partial_sums[group_size + lid] += partial_sums[group_size + lid + i];
		}
		barrier(CLK_LOCAL_MEM_FENCE);
	}
	
	/* Put in global memory */
	if (lid == 0) {
		stats[0].sheep = (uint) VW_LONG_SUM(partial_sums[0]);
		stats[0].wolves = (uint) VW_LONG_SUM(partial_sums[group_size]);
	}
		
}

/**
 * @brief Agent movement kernel.
 * 
 * @param xy_g
 * @param alive_g
 * @param energy_g
 * @param hashes
 * @param seeds
 */
__kernel void moveAgent(
			__global agentData *data,
			__global rng_state *seeds)
{
	
	ushort2 xy_op[5] = {
		(ushort2) (0, 0), 
		(ushort2) (1, 0),
		(ushort2) (-1, 0),
		(ushort2) (0, 1),
		(ushort2) (0, -1)
	};
	
	/* Global id for this work-item */
	uint gid = get_global_id(0);

	/* Load agent state locally. */
	agentData data_l = data[gid];

	/* Only perform if agent is alive. */
	if (PPG_AG_IS_ALIVE(data_l)) {
		
		ushort2 xy_l = PPG_AG_XY_GET(data_l);
		
		uint direction = randomNextInt(seeds, 5);
		
		/* Perform the actual walk */
		
		/* @ALTERNATIVE (instead of the if's below):
		 * 
		 * xy_l = xy_l + xy_op[direction];
		 * xy_l = select(xy_l, (short2) (0, 0), xy_l == ((short2) (GRID_X, GRID_Y)));
		 * xy_l = select(xy_l, (short2) (GRID_X-1, GRID_Y-1), xy_l == ((short2) (-1, -1))); 
		 * 
		 * It's slower. Requires xy to be short2 instead of ushort2.
		 * */
			
		if (direction == 1) 
		{
			xy_l.x++;
			if (xy_l.x >= GRID_X) xy_l.x = 0;
		}
		else if (direction == 2) 
		{
			if (xy_l.x == 0)
				xy_l.x = GRID_X - 1;
			else
				xy_l.x--;
		}
		else if (direction == 3)
		{
			xy_l.y++;
			if (xy_l.y >= GRID_Y) xy_l.y = 0;
		}
		else if (direction == 4)
		{
			if (xy_l.y == 0)
				xy_l.y = GRID_Y - 1;
			else
				xy_l.y--;
		}
		

		/* Lose energy */
		PPG_AG_ENERGY_SUB(data_l, 1);
		if (PPG_AG_ENERGY_GET(data_l) == 0)
			data_l.all = PPG_AG_DEAD;
		
		/* Update global mem */
		data[gid] = data_l;
	
	}
	
}

/**
 * @brief Find cell start and finish.
 * 
 * The cell_agents_idx array is used as uint instead of uint2 because
 * it makes accessing global memory easier, as most likely this kernel
 * will only have to write to an 32-bit address instead of the full
 * 64-bit space occupied by a start and end index.
 * 
 * @param data The agent data array.
 * @param cell_agents_idx The agents index in cell array.
 * */
__kernel void findCellIdx(
			__global agentData *data,
			__global uint *cell_agents_idx) 
{
	
	/* Agent to be handled by this workitem. */
	uint gid = get_global_id(0);
	
	agentData data_l = data[gid];
	
	/* Only perform this if agent is alive. */
	if (PPG_AG_IS_ALIVE(data_l)) {
		
		/* Find cell where this agent lurks... */
		uint cell_idx = 2 * PPG_CELL_IDX(data_l);
		
		/* Check if this agent is the start of a cell index. */
		ushort2 xy_current = PPG_AG_XY_GET(data_l);
		ushort2 xy_prev = PPG_AG_XY_GET(data[max((int) (gid - 1), (int) 0)]);
		ushort2 xy_next = PPG_AG_XY_GET(data[gid + 1]);
		
		ushort2 diff_prev = xy_current - xy_prev;
		ushort2 diff_next = xy_current - xy_next;
		
		if ((gid == 0) || any(diff_prev != ((ushort2) (0, 0)))) {
			cell_agents_idx[cell_idx] = gid;
		}
		/* Check if this agent is the end of a cell index. */
		if (any(diff_next != ((ushort2) (0, 0)))) {
			cell_agents_idx[cell_idx + 1] = gid;
		}
	}
	
}

/**
 * @brief Agents action kernel
 * 
 * @param matrix
 */
__kernel void actionAgent(
			__global uint *grass, 
			__global uint2 *cell_agents_idx,
			__global agentData *data,
			__global uint *data_dup,
			__global rng_state *seeds)
{
	
	/* Global id for this workitem */
	uint gid = get_global_id(0);
	
	/* Get agent for this workitem */
	agentData data_l = data[gid];
	
	/* If agent is alive, do stuff */
	if (PPG_AG_IS_ALIVE(data_l)) {
		
		/* Get cell index where agent is */
		uint cell_idx = PPG_CELL_IDX(data_l);
		
		/* Reproduction threshold and probability (used further ahead) */
		uchar reproduce_threshold, reproduce_prob;
				
		/* Perform specific agent actions */
		if (PPG_AG_IS_SHEEP(data_l)) { /* Agent is sheep, perform sheep actions. */
		
			/* Set reproduction threshold and probability */
			reproduce_threshold = SHEEP_REPRODUCE_THRESHOLD;
			reproduce_prob = SHEEP_REPRODUCE_PROB;

			/* If there is grass, eat it (and I can be the only one to do so)! */
			if (atomic_cmpxchg(&grass[cell_idx], (uint) 0, GRASS_RESTART) == 0) { /// @todo Maybe a atomic_or or something would be faster
				/* If grass is alive, sheep eats it and gains energy */
				PPG_AG_ENERGY_ADD(data_l, SHEEP_GAIN_FROM_FOOD);
			}
			
		} else if (PPG_AG_IS_WOLF(data_l)) { /* Agent is wolf, perform wolf actions. */ /// @todo Maybe remove if is_wolf, it's always wolf in this case
			
			/* Set reproduction threshold and probability */
			reproduce_threshold = WOLVES_REPRODUCE_THRESHOLD;
			reproduce_prob = WOLVES_REPRODUCE_PROB;

			/* Cycle through agents in this cell */
			uint2 cai = cell_agents_idx[cell_idx];
			if (cai.s0 < MAX_AGENTS) {
				for (uint i = cai.s0; i <= cai.s1; i++) {
					if (PPG_AG_IS_SHEEP(data[i])) {
						/* If it is a sheep, try to eat it! */
						if (atomic_or(&(data_dup[i * 2 + 1]), 0xFFFFFFFF) != 0xFFFFFFFF) {
							/* If wolf catches sheep he's satisfied for now, so let's get out of this loop */
							data_dup[i * 2] = 0xFFFFFFFF;
							PPG_AG_ENERGY_ADD(data_l, WOLVES_GAIN_FROM_FOOD);
							break;
						}
					}
				}
			}

		}
		
		/* Try reproducing this agent if energy > reproduce_threshold */
		if (PPG_AG_ENERGY_GET(data_l) > reproduce_threshold) {
			
			/* Throw dice to see if agent reproduces */
			if (randomNextInt(seeds, 100) < reproduce_prob) {
				
				/* Agent will reproduce! */
				uint pos_new = get_global_size(0) + gid;
				agentData data_new = PPG_AG_REPRODUCE(data_l);
				data[pos_new] = data_new;
				
				/* Current agent's energy will be halved also */
				PPG_AG_ENERGY_SUB(data_l, PPG_AG_ENERGY_GET(data_new));
				
			}
		}
		
		/* @ALTERNATIVE for agent reproduction:
		 * 1 - Create new agents in workgroup local memory using a local 
		 * atomic counter to determine new agent index. 
		 * 2 - In the end of the workitem put a workgroup local mem 
		 * barrier, then push new agents in a coalesced fashion to 
		 * global memory using a global atomic counter to determine 
		 * the index of the first agent in the group. 
		 * - Problems: the additional complexity and the use of atomics
		 * will probably not allow for any performance improvements. */
		
		/* My actions only affect my data (energy), so I will only put back data (energy)... */
		data[gid] = data_l;
		
	}
}

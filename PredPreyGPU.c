#include "PredPreyGPU.h"

#define MAX_AGENTS 1048576

//#define LWS_GRASS 256
//#define LWS_REDUCEGRASS1 256

#define SEED 0

#ifdef CLPROFILER
	#define DO_PROFILING 1
#else
	#define DO_PROFILING 0
#endif

// Global work sizes
size_t grass_gws;
size_t reducegrass1_gws;
size_t reducegrass2_gws;

// Local work sizes
size_t grass_lws;
size_t reducegrass1_lws;
size_t reducegrass2_lws;

// Kernels
cl_kernel grass_kernel;
cl_kernel reducegrass1_kernel;
cl_kernel reducegrass2_kernel;

// Main stuff
int main(int argc, char **argv)
{

	// Aux vars
	cl_int status;
	char msg[MAX_AUX_BUFF];
	
	// Events
	cl_event ev_writeGrass, ev_writeRng, ev_grass, ev_readStats;
	
	// Create RNG and set seed
#ifdef SEED
	GRand* rng = g_rand_new_with_seed(SEED);
#elif
	GRand* rng = g_rand_new();
#endif	
	
	// Profiling / Timmings
	ProfCLProfile* profile = profcl_profile_new();

	// Get the required CL zone.
	CLZONE zone = getClZone("PredPreyGPU_Kernels.cl", CL_DEVICE_TYPE_GPU, 2, DO_PROFILING);

	// Get simulation parameters
	PARAMS params = loadParams(CONFIG_FILE);

	// Compute work sizes for different kernels and print them to screen
	computeWorkSizes(params, zone.device);
	printWorkSizes();

	// Obtain kernels entry points
	getKernelEntryPoints(zone.program);

	////////////////////////////////////////
	// Create and initialize host buffers //
	////////////////////////////////////////
	
	// Statistics
	size_t statsSizeInBytes = (params.iters + 1) * sizeof(STATS);
	STATS * statsArray = initStatsArray(params, statsSizeInBytes);
	
	// Grass matrix 
	size_t grassSizeInBytes = params.grid_x * params.grid_y * sizeof(CELL);
	CELL * grassMatrixHost = initGrassMatrixHost(params, grassSizeInBytes, statsArray);
	
	// RNG seeds
	size_t rngSeedsSizeInBytes = MAX_AGENTS * sizeof(cl_ulong);
	cl_ulong * rngSeedsHost = initRngSeedsHost(rngSeedsSizeInBytes) ;

	// Sim parameters
	SIM_PARAMS sim_params = initSimParams(params);
	
	// Current iteration
	cl_uint iter = 0; 
	
	///////////////////////////
	// Create device buffers //
	///////////////////////////

	// Statistics in device memory
	cl_mem statsDevice = clCreateBuffer(zone.context, CL_MEM_WRITE_ONLY, sizeof(STATS), NULL, &status );
	if (status != CL_SUCCESS) { PrintErrorCreateBuffer(status, "statsDevice"); return(-1); }

	// Grass matrix
	cl_mem grassMatrixDevice = clCreateBuffer(zone.context, CL_MEM_READ_WRITE, grassSizeInBytes, NULL, &status );
	if (status != CL_SUCCESS) { PrintErrorCreateBuffer(status, "grassMatrixDevice"); return(-1); }

	// Grass count
	cl_mem grassCountDevice = clCreateBuffer(zone.context, CL_MEM_READ_WRITE, grasscount2_gws[0] * sizeof(cl_uint), NULL, &status );
	if (status != CL_SUCCESS) { PrintErrorCreateBuffer(status, "grassCountDevice"); return(-1); }

	// RNG seeds
	cl_mem rngSeedsDevice = clCreateBuffer(zone.context, CL_MEM_READ_WRITE, rngSeedsSizeInBytes, NULL, &status );
	if (status != CL_SUCCESS) { PrintErrorCreateBuffer(status, "rngSeedsDevice"); return(-1); }
	
	///////////////////////////////
	// Initialize device buffers //
	///////////////////////////////
	
	status = clEnqueueWriteBuffer (	zone.queues[0], grassMatrixDevice, CL_FALSE, 0, grassSizeInBytes, grassMatrixHost, 0, NULL, &ev_writeGrass) );
	if (status != CL_SUCCESS) { PrintErrorEnqueueReadWriteBuffer(status, "grassMatrixDevice"); return(-1); }
	
	status = clEnqueueWriteBuffer (	zone.queues[0], rngSeedsDevice, CL_FALSE, 0, rngSeedsSizeInBytes, rngSeedsHost, 0, NULL, &ev_writeRng) );
	if (status != CL_SUCCESS) { PrintErrorEnqueueReadWriteBuffer(status, "rngSeedsDevice"); return(-1); }

	/////////////////////////////////
	//  Set fixed kernel arguments //
	/////////////////////////////////

	// Grass kernel
	setGrassKernelArgs(grassMatrixDevice, sim_params);
	
	// TEMPORARY, REMOVE!
	status = clSetKernelArg(grass_kernel, 2, sizeof(cl_mem), (void *) &rngSeedsDevice);
	if (status != CL_SUCCESS) { PrintErrorSetKernelArg(status, "Arg 2 of grass kernel"); exit(EXIT_FAILURE); }


	// Count grass
	setCountGrassKernelArgs(grassMatrixDevice, grassCountDevice, statsDevice, sim_params);
	
	//////////////////
	//  SIMULATION! //
	//////////////////
	
	// Guarantee all memory transfers are performed
	cl_event writeEvents[2];
	writeEvents[0] = ev_writeGrass;
	writeEvents[1] = ev_writeRng;
	
	status = clWaitForEvents(2, writeEvents);
	if (status != CL_SUCCESS) { PrintErrorWaitForEvents(status, "write events"); return(-1); }
	
#ifdef CLPROFILER
		// Update data transfer profiling info
		updateEventProfile(profiling, "writeGrass", ev_writeGrass);
		updateEventProfile(profiling, "writeRng", ev_writeRng);
#endif
	
	// Release data transfer events	
	status = clReleaseEvent( ev_writeGrass );
	if (status != CL_SUCCESS) { PrintErrorReleaseEvent(status, "write grass"); return(-1); }
	
	status = clReleaseEvent( ev_writeRng );
	if (status != CL_SUCCESS) { PrintErrorReleaseEvent(status, "write rng"); return(-1); }
	
	// SIMULATION LOOP
	for (iter = 1; iter <= params.iters; iter++) {
		
		//printf("------ Start loop iter %d ---------\n", iter);
		
		//printf("Grass kernel iter %d\n", iter);
		// Grass kernel: grow grass, set number of prey to zero
		status = clEnqueueNDRangeKernel( zone.queues[1], grass_kernel, 2, NULL, grass_gws, grass_lws, 0, NULL, &ev_grass));
		if (status != CL_SUCCESS) { sprintf(msg, "grass_kernel, iteration %d, gws=%d lws=%d ", iter, (int) *grass_gws, (int) *grass_lws); PrintErrorEnqueueNDRangeKernel(status, msg); return(-1); }

/*
		///// Gather statistics //////
		size_t grasscount_gws_loop = grasscount_gws;
		size_t grasscount_lws_loop = grasscount_lws;
		for (unsigned int i = 0; i < numGrassCountLoops) {
			status = clEnqueueBarrier(zone.queues[1]);
			if (status != CL_SUCCESS) { sprintf(msg, "in grass count loops, iteration %d", iter); PrintErrorEnqueueBarrier(status, msg); return(-1); }
			status = clEnqueueNDRangeKernel( 
				zone.queues[1], 
				countgrass_kernel, 
				1, 
				NULL,
				&grasscount2_gws_loops, 
				&grasscount2_lws_loops, 
				0, 
				NULL, 
				events->grasscount + i
			);
			if (status != CL_SUCCESS) { sprintf(msg, "countgrass_kernel, iteration %d", iter); PrintErrorEnqueueNDRangeKernel(status, msg); return(-1); }
			grasscount_gws_loop = grasscount_gws_loop / grasscount_lws_loop;
			grasscount_lws_loop = grasscount_gws_loop < grasscount_lws_loop ? grasscount_gws_loop : grasscount_lws_loop;
			
		}

		
		//printf("Count grass 1 iter %d\n", iter);
		// Count grass, part 1
		status = clEnqueueNDRangeKernel( zone.queues[1], countgrass1_kernel, 1, NULL, &grasscount1_gws, &grasscount1_lws, 1, &(events->grass), &(events->grasscount1));
		if (status != CL_SUCCESS) { sprintf(msg, "countgrass1_kernel, iteration %d", iter); PrintErrorEnqueueNDRangeKernel(status, msg); return(-1); }
		
		// Count grass, part 2
		for (int i = 0; i < numGrassCount2Loops; i++) {
			//printf("Count grass 1 iter %d, loop %d\n", iter, i);

			status = clSetKernelArg(countgrass2_kernel, 2, sizeof(cl_uint), (void *) &effectiveNextGrassToCount[i]);
			if (status != CL_SUCCESS) { PrintErrorSetKernelArg(status, "Arg 2 of countgrass2 kernel"); return(-1); }
			status = clEnqueueNDRangeKernel( 
				zone.queues[1], 
				countgrass2_kernel, 
				1, 
				NULL,
				&grasscount2_gws[i], 
				&grasscount2_lws, 
				1, 
				&(events->grasscount1), 
				events->grasscount2 + i
			);
			if (status != CL_SUCCESS) { sprintf(msg, "countgrass2_kernel, iteration %d", iter); PrintErrorEnqueueNDRangeKernel(status, msg); return(-1); }

			status = clEnqueueBarrier(zone.queues[1]);
			if (status != CL_SUCCESS) { sprintf(msg, "in grass count loops, iteration %d", iter); PrintErrorEnqueueBarrier(status, msg); return(-1); }

			//events->grasscount2_index++;

		}
		
		// Get statistics 
		//printf("Read back stats iter %d, will read %d iters of stats\n", iter, numItersToRead);
		status = clEnqueueReadBuffer ( zone.queues[0], statsDevice, CL_FALSE, 0, sizeof(STATS), statsArray + iter - 1, 0, NULL, &(events->readStats));
		if (status != CL_SUCCESS) { sprintf(msg, "read stats, iteration %d", iter); PrintErrorEnqueueReadWriteBuffer(status, msg); return(-1); }
		
*/
		// WE CAN OPTIMIZE THIS WITHOUT BARRIER OR CLFINISH, AND START NEW ITERATION EVEN IF STATISTICS ARE NOT YET BACK (if profiling is off)
		// Guarantee all tasks in queue are terminated...
		status = clFinish(zone.queues[0]); 
		status = clFinish(zone.queues[1]); 
		if (status != CL_SUCCESS) { sprintf(msg, "sim loop, iteration %d", iter); PrintErrorFinish(status, msg); return(-1); }

#ifdef CLPROFILER
		// Update simulation profiling info
		updateEventProfile(profiling, "grass", ev_grass);

#endif
		status = clReleaseEvent( ev_grass );
		if (status != CL_SUCCESS) { sprintf(msg, "grass, iteration %d", iter); PrintErrorReleaseEvent(status, msg); return(-1); }
		
		// Release current iteration events
/*		status = clReleaseEvent( events->readStats );
		if (status != CL_SUCCESS) { sprintf(msg, "read stats, iteration %d", iter); PrintErrorReleaseEvent(status, msg); return(-1); }
		status = clReleaseEvent( events->grass );
		if (status != CL_SUCCESS) { sprintf(msg, "countgrass1, iteration %d", iter); PrintErrorReleaseEvent(status, msg); return(-1); }
		
		for (int i = 0; i < numGrassCount2Loops; i++) {
			status = clReleaseEvent( *(events->grasscount2 + i) );
			if (status != CL_SUCCESS) { sprintf(msg, "countgrass2, iteration %d", iter); PrintErrorReleaseEvent(status, msg); return(-1); }
		}*/

	
		
	}

	// Guarantee all activity has terminated...
	clFinish(zone.queues[0]);
	clFinish(zone.queues[1]);

	// Get finishing time	
	gettimeofday(&time1, NULL);  

	// Output results to file
	saveResults("stats.txt", statsArray, params.iters);

	// Print timmings
#ifdef CLPROFILER
	// Calculate and show profiling info
	double totalTime = printTimmings(time0, time1);
	printProfilingInfo(profiling, totalTime);
#else
	printTimmings(time0, time1);
#endif

	/////////////////
	// Free stuff! //
	/////////////////
	
	// Release OpenCL kernels
	releaseKernels();
	
	// Release OpenCL memory objects
	clReleaseMemObject(statsDevice);
	clReleaseMemObject(grassMatrixDevice);
	clReleaseMemObject(grassCountDevice);
	clReleaseMemObject(rngSeedsDevice);

	// Release OpenCL zone (program, command queue, context)
	destroyClZone(zone);

	// Free host resources
	free(statsArray);
	free(grassMatrixHost);
	free(rngSeedsHost);
	freeEventsCL(events); // This only frees host memory which contained events, its not a releaseEvent
	
#ifdef CLPROFILER
	freeProfile(profiling);
#endif

	return 0;
	
}

// Compute worksizes depending on the device type and number of available compute units
void computeWorkSizes(PARAMS params, cl_device device) {
	
	/* Variable which will keep the maximum workgroup size */
	size_t maxWorkGroupSize;
	
	/* Get the maximum workgroup size. */
	cl_int status = clGetDeviceInfo(
		device, 
		CL_DEVICE_MAX_WORK_GROUP_SIZE,
		sizeof(size_t),
		&maxWorkGroupSize,
		NULL);
	
	/* Check for errors on the OpenCL call */
	if (status != CL_SUCCESS) {PrintErrorGetDeviceInfo( status, "Get maximum workgroup size." ); exit(EXIT_FAILURE); }

	/* grass growth worksizes */
#ifdef LWS_GRASS
	grass_lws = LWS_GRASS;
#else
	grass_lws = maxWorkGroupSize;
#endif
	grass_gws = grass_lws * ceil(((float) (params.grid_x * params.grix_y)) / grass_lws);
	
	/* grass count worksizes */
#ifdef LWS_REDUCEGRASS1
	reducegrass1_lws = LWS_REDUCEGRASS1;
#else
	reducegrass1_lws = maxWorkGroupSize;
#endif	
	reducegrass1_gws = reducegrass1_lws * reducegrass1_lws;
	reducegrass2_lws = reducegrass1_lws;
	reducegrass2_gws = reducegrass1_lws;	
}

// Print worksizes
void printWorkSizes(unsigned int numGrassCount2Loops) {
	printf("Kernel work sizes:\n");
	printf("grass_gws=%d\tgrass_lws=%d\n", (int) grass_gws, (int) grass_lws);
	printf("reducegrass1_gws=%d\treducegrass1_lws=%d\n", (int) reducegrass1_gws, (int) reducegrass1_lws);
	printf("grasscount2_lws/gws=%d\n", (int) reducegrass2_lws);

}

// Get kernel entry points
void getKernelEntryPoints(cl_program program) {
	cl_int status;
	grass_kernel = clCreateKernel( program, "grass", &status );
	if (status != CL_SUCCESS) { PrintErrorCreateKernel(status, "grass kernel"); exit(EXIT_FAILURE); }
	countgrass1_kernel = clCreateKernel( program, "reduceGrass1", &status );
	if (status != CL_SUCCESS) { PrintErrorCreateKernel(status, "reduceGrass1 kernel"); exit(EXIT_FAILURE); }
	countgrass2_kernel = clCreateKernel( program, "reduceGrass2", &status );
	if (status != CL_SUCCESS) { PrintErrorCreateKernel(status, "reduceGrass2 kernel"); exit(EXIT_FAILURE); }
}

// Initialize statistics array in host
STATS* initStatsArray(PARAMS params, size_t statsSizeInBytes) 
{
	STATS* statsArray = (STATS*) malloc(statsSizeInBytes);
	statsArray[0].sheep = params.init_sheep;
	statsArray[0].wolves = params.init_wolves;
	statsArray[0].grass = 0;
	return statsArray;
}

// Initialize grass matrix in host
CELL* initGrassMatrixHost(PARAMS params, size_t grassSizeInBytes, STATS* statsArray) 
{

	CELL * grassMatrixHost = (CELL *) malloc(grassSizeInBytes);
	for(unsigned int i = 0; i < params.grid_x; i++)
	{
		for (unsigned int j = 0; j < params.grid_y; j++)
		{
			unsigned int gridIndex = i + j*params.grid_x;
			grassMatrixHost[gridIndex].grass = (rand() % 2) == 0 ? 0 : 1 + (rand() % params.grass_restart);
			if (grassMatrixHost[gridIndex].grass == 0)
				statsArray[0].grass++;
		}
	}
	return grassMatrixHost;
}

// Initialize random seeds array in host
cl_ulong* initRngSeedsHost(size_t rngSeedsSizeInBytes) {
	cl_ulong * rngSeedsHost = (cl_ulong*) malloc(rngSeedsSizeInBytes);
	for (int i = 0; i < MAX_AGENTS; i++) {
		rngSeedsHost[i] = rand();
	}
	return rngSeedsHost;
}

// Initialize simulation parameters in host, to be sent to GPU
SIM_PARAMS initSimParams(PARAMS params) {
	SIM_PARAMS sim_params;
	sim_params.size_x = params.grid_x;
	sim_params.size_y = params.grid_y;
	sim_params.size_xy = params.grid_x * params.grid_y;
	sim_params.max_agents = MAX_AGENTS;
	sim_params.grass_restart = params.grass_restart;
	return sim_params;
}

// Set grass kernel parameters
void setGrassKernelArgs(cl_mem grassMatrixDevice, SIM_PARAMS sim_params) {

	cl_int status;

	status = clSetKernelArg(grass_kernel, 0, sizeof(cl_mem), (void *) &grassMatrixDevice);
	if (status != CL_SUCCESS) { PrintErrorSetKernelArg(status, "Arg 0 of grass kernel"); exit(EXIT_FAILURE); }

	status = clSetKernelArg(grass_kernel, 1, sizeof(SIM_PARAMS), (void *) &sim_params);
	if (status != CL_SUCCESS) { PrintErrorSetKernelArg(status, "Arg 1 of grass kernel"); exit(EXIT_FAILURE); }

}

// Set grass count kernels fixed parameters
void setCountGrassKernelArgs(cl_mem grassMatrixDevice, cl_mem grassCountDevice, cl_mem statsDevice, SIM_PARAMS sim_params) {
	
	cl_uint status;

	// Grass count kernel 1
	status = clSetKernelArg(countgrass1_kernel, 0, sizeof(cl_mem), (void *) &grassMatrixDevice);
	if (status != CL_SUCCESS) { PrintErrorSetKernelArg(status, "Arg 0 of countgrass1 kernel"); exit(EXIT_FAILURE); }

	status = clSetKernelArg(countgrass1_kernel, 1, sizeof(cl_mem), (void *) &grassCountDevice);
	if (status != CL_SUCCESS) { PrintErrorSetKernelArg(status, "Arg 1 of countgrass1 kernel"); exit(EXIT_FAILURE); }

	status = clSetKernelArg(countgrass1_kernel, 2, grasscount1_lws*sizeof(cl_uint), NULL);
	if (status != CL_SUCCESS) { PrintErrorSetKernelArg(status, "Arg 2 of countgrass1 kernel"); exit(EXIT_FAILURE); }

	status = clSetKernelArg(countgrass1_kernel, 3, sizeof(SIM_PARAMS), (void *) &sim_params);
	if (status != CL_SUCCESS) { PrintErrorSetKernelArg(status, "Arg 3 of countgrass1 kernel"); exit(EXIT_FAILURE); }

	// Grass count kernel 2
	status = clSetKernelArg(countgrass2_kernel, 0, sizeof(cl_mem), (void *) &grassCountDevice);
	if (status != CL_SUCCESS) { PrintErrorSetKernelArg(status, "Arg 0 of countgrass2 kernel"); exit(EXIT_FAILURE); }

	status = clSetKernelArg(countgrass2_kernel, 1, grasscount2_gws[0]*sizeof(cl_uint), NULL);
	if (status != CL_SUCCESS) { PrintErrorSetKernelArg(status, "Arg 1 of countgrass2 kernel"); exit(EXIT_FAILURE); }

	status = clSetKernelArg(countgrass2_kernel, 3, sizeof(cl_mem), (void *) &statsDevice);
	if (status != CL_SUCCESS) { PrintErrorSetKernelArg(status, "Arg 3 of countgrass2 kernel"); exit(EXIT_FAILURE); }
	


}

// Release kernels
void releaseKernels() {
	clReleaseKernel(grass_kernel);
	clReleaseKernel(countgrass1_kernel); 
	clReleaseKernel(countgrass2_kernel);
}

// Save results
void saveResults(char* filename, STATS* statsArray, unsigned int iters) {
	FILE * fp1 = fopen(filename,"w");
	for (unsigned int i = 0; i <= iters; i++)
		fprintf(fp1, "%d\t%d\t%d\n", statsArray[i].sheep, statsArray[i].wolves, statsArray[i].grass );
	fclose(fp1);
}

// Print timmings
double printTimmings(struct timeval time0, struct timeval time1) {
	double dt = time1.tv_sec - time0.tv_sec;
	if (time1.tv_usec >= time0.tv_usec)
		dt = dt + (time1.tv_usec - time0.tv_usec) * 1e-6;
	else
		dt = (dt-1) + (1e6 + time1.tv_usec - time0.tv_usec) * 1e-6;
	printf("Total Simulation Time = %f", dt);
	return dt;
}


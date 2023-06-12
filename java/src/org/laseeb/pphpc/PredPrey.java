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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.validators.PositiveInteger;

/**
 * This class contains the main method for starting the simulator. The main method
 * creates a new instance of this class and calls the {@link #doMain(String[])} method,
 * which performs the following steps:
 * <p>
 * <ol>
 * <li>Parses command-line options, keeping them in the created instance of 
 * this class, which also serves as a container object for several of these 
 * options.</li>
 * <li>Instantiates the appropriate work factory.</li>
 * <li>Reads the simulation parameters file and creates a simulation parameters
 * object.</li>
 * <li>Instantiates the MVC model, passing it the simulation parameters and 
 * several other options specified in the command-line.</li>
 * <li>Gets an MVC controller from the selected work factory.</li>
 * <li>Creates and initializes the MVC views specified in the command-line, 
 * passing them the model, the controller and a reference to the created 
 * instance of this class (so that views have access to some options specified 
 * in the command-line).</li> 
 * </ol>
 * 
 * @author Nuno Fachada
 */
public class PredPrey {
	
	/**
	 *  Enumeration containing program errors. 
	 * */
	public enum Errors {
		
		/** No error, successful program termination. */
		NONE(0),
		/** Error related with the specified command-line arguments. */
		ARGS(-1), 
		/** Unknown or invalid simulation parameters file. */
		PARAMS(-2),
		/** Error during simulation. */
		SIM(-3),
		/** Other errors. */
		OTHER(-4);
		
		/* Error code. */
		private int value;
		
		/* Enumeration constructor. */
		private Errors(int value) { this.value = value; }
		
		/**
		 * Return code for specified error.
		 *  
		 * @return Code for specified error.
		 */
		public int getValue() { return this.value; }
	}
	
	/* Parallelization strategy. */
	@Parameter(names = {"-ps", "--par-strat"}, description = "Parallelization"
			+ " strategy (ST, EQ, EX, ER or OD)",
			converter =  ParStratTypeConverter.class)
	private ParStratType parStart = ParStratType.EQ;

	/* Number of threads. */
	@Parameter(names = "-n", description = "Number of threads (ignored for ST "
			+ "parallelization strategy), defaults to the number of processors", 
			validateWith = PositiveInteger.class)
	private int numThreads = Runtime.getRuntime().availableProcessors();

	/* Block size for OD parallelization strategy. */
	@Parameter(names = "-b", description = "Block size (only for OD" 
			+ " parallelization strategy)", 
			validateWith = PositiveInteger.class)
	private int blockSize = 100;
	
	/* File containing simulation parameters. */
	@Parameter(names = "-p", 
			description = "File containing simulation parameters")
	private String paramsFile = "config.txt";
	
	/* File where to output simulation statistics. */
	@Parameter(names = "-s", description = "Statistics output file")
	private String statsFile = "stats.txt";
	
	/* Seed for random number generator. */
	@Parameter(names = "-r", description = 
			"Seed for random number generator (defaults to System.nanoTime())",
			converter = BigIntegerConverter.class)
	private BigInteger seed = null;
	
	/* Random number generator implementation. */
	@Parameter(names = "-g", 
			description = "Random number generator (AES, " + 
					"CA, CMWC, JAVA, MT, RANDU, REALLYPOOR or XORSHIFT)", 
			converter =  RNGTypeConverter.class)
	private RNGType rngType = RNGType.MT; //TODO verify this
	
	/* Shuffle agents before they act? */
	@Parameter(names = {"-u", "--no-shuffle"}, description = "Disable agent"
			+ " shuffling before agent actions (faster, but will have"
			+ " some impact in model dynamics")
	private boolean noShuffle = false;

	/* Debug mode. */
	@Parameter(names = "-d", 
			description = "Debug mode (show stack trace on error)", 
			hidden = true)
	private boolean debug = false;

	/* List of MVC views to use. */
	@Parameter(names = {"-v", "--view"}, 
			description = "Simulation views:" 
				+ " OneGoCLI (default), InteractiveCLI, InfoWidget", 
			variableArity = true)
	private List<String> views = new ArrayList<String>();
	
	/* Help option. */
	@Parameter(names = {"--help", "-h", "-?"}, description = "Show options", 
			help = true)
	private boolean help;

	/* Simulation parameters. */
	private ModelParams params;
	
	/* Work factory. */
	private IWorkFactory workFactory;

	/**
	 * Main method.
	 * 
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		
		/* Create a new instance of this class and call the doMain method to
		 * perform the necessary steps to start the simulator. */
		new PredPrey().doMain(args);
		
	}
	
	/**
	 * Create a new PredPrey class object.
	 */
	public PredPrey() {}

	/**
	 * Perform the necessary steps to start the simulator.
	 * 
	 * @param args Command line arguments.
	 */
	public void doMain(String[] args) {
		
		/* Setup command line options parser. */
		JCommander parser = new JCommander(this);
		parser.setProgramName("java -cp bin" + java.io.File.pathSeparator 
				+ "lib" + java.io.File.separator + "* " 
				+ PredPrey.class.getName());
		
		/* Parse command line options. */
		try {
			parser.parse(args);
		} catch (ParameterException pe) {
			/* On parsing error, show usage and return. */
			System.err.println(errMessage(pe));
			parser.usage();
			System.exit(Errors.ARGS.getValue());
		}
		
		/* If help option was passed, show help and quit. */
		if (this.help) {
			parser.usage();
			System.exit(Errors.NONE.getValue());
		}
		
		/* Get the work factory which corresponds to the command specified
		 * in the command line. */
		this.workFactory = this.parStart.getWorkFactory(this);
		
		/* Read parameters file. */
		try {
			this.params = new ModelParams(this.paramsFile);
		} catch (IOException ioe) {
			System.err.println(errMessage(ioe));
			System.exit(Errors.PARAMS.getValue());
		}
		
		/* Setup seed for random number generator. */
		if (this.seed == null)
			this.seed = BigInteger.valueOf(System.nanoTime());
		
		/* Create the MVC model. */
		IModel model = new Model(this.params, this.workFactory, 
				!this.noShuffle, this.rngType, this.seed);
		
		/* Obtain the MVC controller. */
		IController controller = this.workFactory.createSimController(model);
		
		/* Create the MVC views. */
		List<IView> viewObjs = null;
		try {
			viewObjs = this.createViews();
		} catch (Exception e) {
			System.err.println("Unable to create instance of view: " 
					+ errMessage(e));
			System.exit(Errors.ARGS.getValue());			
		}
		
		/* Initialize the MVC views. */
		try {
			this.initViews(viewObjs, model, controller);
		} catch (Exception e) {
			System.err.println("Invalid selection of views: " + errMessage(e));
			System.exit(Errors.ARGS.getValue());			
		}
		
	}

	/**
	 * Show error message or stack trace, depending on debug parameter.
	 * 
	 * @param t Exception which caused the error.
	 */
	public String errMessage(Throwable t) {
		
		String errMessage;
		
		if (this.debug) {
			StringWriter sw = new StringWriter();
			t.printStackTrace(new PrintWriter(sw));
			errMessage = sw.toString();
		} else {
			errMessage = t.getMessage();
		}
		
		return errMessage;
	}
	
	/**
	 * Create the MVC views.
	 * 
	 * @return A list of the specified MVC views.
	 * @throws Exception If it wasn't possible to create any of the specified views.
	 */
	private List<IView> createViews() throws Exception {
		
		/* Create a list to hold the views. */
		List<IView> viewObjs = new ArrayList<IView>();
		
		/* Cycle through the views specified in the command-line. */
		for (String viewName : this.views) {
		
			/* Get the current view class. */
			Class<? extends IView> viewClass = 
					Class.forName("org.laseeb.pphpc." + viewName + "View").asSubclass(IView.class);
			
			/* Instantiate the current view and add it to the list. */
			viewObjs.add(viewClass.newInstance());
			
		}
		
		/* Return the list of views. */
		return viewObjs;
	}
	
	/**
	 * Initialize and show MVC views.
	 * 
	 * @param viewObjs View objects to initialize and show.
	 * @param model The MVC model.
	 * @param controller The MVC controller.
	 * @throws Exception If it wasn't possible to initialize all of the views.
	 */
	private void initViews(List<IView> viewObjs, IModel model, 
			IController controller) throws Exception {

		/* Where any views specified? */
		if (viewObjs.size() == 0) {
			
			/* If no views were specified, use the "one go" view, which 
			 * performs a simulation from start to finish without user 
			 * interaction. */
			viewObjs.add(new OneGoCLIView());
			
		} else {
			
			/* Some views were specified, check that there is at least one 
			 * active view (i.e. a view which is capable of controlling the 
			 * simulation), and that if an active-exclusive view was specified, 
			 * there is no other active view. */
			
			/* Number of active-exclusive views. */
			int exclusiveCount = 0;
			
			/* Number of active views. */
			int activeCount = 0;
			
			/* Count active and active-exclusive views. */
			for (IView view : viewObjs) {
				if (view.getType() == ViewType.ACTIVE_EXCLUSIVE) {
					exclusiveCount++;
				} else if (view.getType() == ViewType.ACTIVE) {
					activeCount++;
				}
			}
			
			/* Check if the specified views are acceptable. */
			if (exclusiveCount > 1) {
				throw new Exception("There can be at most one exclusive view.");
			}
			if ((exclusiveCount == 1) && (activeCount > 0)) {
				throw new Exception("An exclusive view does not allow"
						+ " for additional active views.");
			}
			if (activeCount + exclusiveCount == 0) {
				throw new Exception("No active views specified.");
			}
		}
		
		/* All the views are acceptable, initialize and show them. */
		for (IView view : viewObjs) {
			view.init(model, controller, this);
		}
		
	}
	
	/**
	 * Returns the name of the file where to place the simulation statistics.
	 * 
	 * @return The name of the file where to place the simulation statistics.
	 */
	public String getStatsFile() {
		return this.statsFile;
	}

	/**
	 * Returns the number of threads specified in the command line.
	 * 
	 * @return The number of threads specified in the command line.
	 */
	public int getNumThreads() {
		return numThreads;
	}

	/**
	 * Returns the block size (for the OD strategy) specified in the command 
	 * line.
	 * 
	 * @return The block size (for the OD strategy) specified in the command 
	 * line.
	 */
	public int getBlockSize() {
		return blockSize;
	}
	
}

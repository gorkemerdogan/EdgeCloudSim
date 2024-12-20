/**
 * Title:        EdgeCloudSim - Main Application
 *
 * Description:  Main application for Hangar Sim
 *
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.applications.hangar_sim2;

import edu.boun.edgecloudsim.core.ScenarioFactory;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainApp {

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		//disable console output of cloudsim library
		Log.disable();

		//enable console output and file output of this application
		SimLogger.enablePrintLog();

		int iterationNumber = 20;
		String configFile = "";
		String outputFolder = "";
		String edgeDevicesFile = "";
		String applicationsFile = "";
		if (args.length == 5) {
			configFile = args[0];
			edgeDevicesFile = args[1];
			applicationsFile = args[2];
			outputFolder = args[3];
			iterationNumber = Integer.parseInt(args[4]);
		} else {
			SimLogger.printLine("Simulation setting file, output folder and iteration number are not provided! Using default ones...");
			configFile = "scripts/hangar_sim2/config/default_config.properties";
			applicationsFile = "scripts/hangar_sim2/config/applications.xml";
			edgeDevicesFile = "scripts/hangar_sim2/config/edge_devices.xml";
			outputFolder = "sim_results/hangar_sim4/ite1";
		}

		//load settings from configuration file
		SimSettings SS = SimSettings.getInstance();
		if (!SS.initialize(configFile, edgeDevicesFile, applicationsFile)) {
			SimLogger.printLine("cannot initialize simulation settings!");
			System.exit(0);
		}

		if (SS.getFileLoggingEnabled()) {
			SimLogger.enableFileLog();
			SimUtils.cleanOutputFolder(outputFolder);
		}

		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date SimulationStartDate = Calendar.getInstance().getTime();
		String now = df.format(SimulationStartDate);
		SimLogger.printLine("Simulation started at " + now);
		SimLogger.printLine("----------------------------------------------------------------------");

		// Outer loop for iteration number
		for (int iter = 1; iter < 21; iter++) {
			SimLogger.printLine("Starting iteration " + (iter + 1) + " of " + iterationNumber);
			outputFolder = "sim_results/hangar_sim4/ite" + iter;

			for (int j = SS.getMinNumOfMobileDev(); j <= SS.getMaxNumOfMobileDev(); j += SS.getMobileDevCounterSize()) {
				for (int k = 0; k < SS.getSimulationScenarios().length; k++) {
					for (int i = 0; i < SS.getOrchestratorPolicies().length; i++) {
						String simScenario = SS.getSimulationScenarios()[k];
						String orchestratorPolicy = SS.getOrchestratorPolicies()[i];
						Date ScenarioStartDate = Calendar.getInstance().getTime();
						now = df.format(ScenarioStartDate);

						SimLogger.printLine("Scenario started at " + now);
						SimLogger.printLine("Scenario: " + simScenario + " - Policy: " + orchestratorPolicy + " - #iteration: " + (iter));
						SimLogger.printLine("Duration: " + SS.getSimulationTime() / 60 + " min (warm up period: " + SS.getWarmUpPeriod() / 60 + " min) - #devices: " + j);
						SimLogger.getInstance().simStarted(outputFolder, "SIMRESULT_" + simScenario + "_" + orchestratorPolicy + "_" + j + "DEVICES");

						try {
							// Initialize the CloudSim package
							int num_user = 2;   // number of grid users
							Calendar calendar = Calendar.getInstance();
							boolean trace_flag = false;  // mean trace events

							CloudSim.init(num_user, calendar, trace_flag, 0.01);

							// Generate EdgeCloudsim Scenario Factory
							ScenarioFactory sampleFactory = new HangarSimScenarioFactory(j, SS.getSimulationTime(), orchestratorPolicy, simScenario);

							// Generate EdgeCloudSim Simulation Manager
							SimManager manager = new SimManager(sampleFactory, j, simScenario, orchestratorPolicy);

							// Start simulation
							manager.startSimulation();
						} catch (Exception e) {
							SimLogger.printLine("The simulation has been terminated due to an unexpected error");
							e.printStackTrace();
							System.exit(0);
						}

						Date ScenarioEndDate = Calendar.getInstance().getTime();
						now = df.format(ScenarioEndDate);
						SimLogger.printLine("Scenario finished at " + now + ". It took " + SimUtils.getTimeDifference(ScenarioStartDate, ScenarioEndDate));
						SimLogger.printLine("----------------------------------------------------------------------");
					} // End of orchestrators loop
				} // End of scenarios loop
			} // End of mobile devices loop
		} // End of iteration loop

		Date SimulationEndDate = Calendar.getInstance().getTime();
		now = df.format(SimulationEndDate);
		SimLogger.printLine("Simulation finished at " + now + ". It took " + SimUtils.getTimeDifference(SimulationStartDate, SimulationEndDate));
	}
}
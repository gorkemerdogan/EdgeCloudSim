package edu.boun.edgecloudsim.applications.AircraftHangarScenario;

import edu.boun.edgecloudsim.core.ScenarioFactory;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;

public class MainApp {
    public static void main(String[] args) {
        try {
            // Total number of cameras (5 from Loc1, 10 from Loc2, 20 from Loc3)
            int totalCameras = 5 + 10 + 20;

            // Simulation time (in seconds)
            double simulationTime = 3600; // 1 hour

            // Placeholder values for the scenario factory
            String orchestratorPolicy = "policy"; // Define your orchestrator policy
            String simScenario = "aircraft_hangar_sim"; // Name of the simulation scenario

            // Create the Scenario Factory
            ScenarioFactory factory = new AircraftHangarScenarioFactory(
                    totalCameras,
                    simulationTime,
                    orchestratorPolicy,
                    simScenario
            );

            // Initialize SimSettings with configuration files
            String configFile = "config/default_config.properties";
            String edgeDevicesFile = "config/edge_devices.xml";
            String applicationsFile = "config/applications.xml";

            SimSettings simSettings = SimSettings.getInstance(); // Get singleton instance
            simSettings.initialize(configFile, edgeDevicesFile, applicationsFile); // Initialize settings

            // Initialize the SimManager with the factory
            SimManager simManager = new SimManager(factory, totalCameras, simScenario, orchestratorPolicy);

            // Start the simulation
            simManager.startSimulation();

            System.out.println("Simulation completed successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Simulation failed!");
        }
    }
}
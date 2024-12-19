/*
 * Title:        HangarSimEdgeOrchestrator - Custom Edge Orchestrator
 *
 * Description:
 * This orchestrator determines the appropriate server (edge or cloud)
 * to offload tasks based on Wi-Fi bandwidth, LAN conditions, and edge server utilization.
 * After determining the target server, the least loaded VM is selected.
 *
 * Features:
 * - Policy-based offloading: NETWORK_BASED, UTILIZATION_BASED, HYBRID.
 * - Supports Wi-Fi and LAN communication.
 *
 * Author: Custom Implementation for Hangar Simulations.
 */

package edu.boun.edgecloudsim.applications.hangar_sim;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;

import edu.boun.edgecloudsim.cloud_server.CloudVM;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.boun.edgecloudsim.edge_client.CpuUtilizationModel_Custom;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.utils.SimLogger;

public class HangarSimEdgeOrchestrator extends EdgeOrchestrator {

	private int numberOfHost; //used by load balancer

	public HangarSimEdgeOrchestrator(String _policy, String _simScenario) {
		super(_policy, _simScenario);
	}

	@Override
	public void initialize() {
		numberOfHost=SimSettings.getInstance().getNumOfEdgeHosts();
	}

	/**
	 * Determines the target device (edge server or cloud) to offload the task.
	 */
	@Override
	public int getDeviceToOffload(Task task) {
		int result;

		if (simScenario.equals("SINGLE_TIER")) {
			// All tasks are processed on the local edge server
			result = SimSettings.GENERIC_EDGE_DEVICE_ID;
		} else if (simScenario.equals("TWO_TIER_WITH_EO")) {
			// Simulate a dummy task to evaluate network conditions
			Task dummyTask = new Task(0, 0, 0, 0, 128, 128, null, null, null);

			// Evaluate Wi-Fi delay for cloud offloading
			double wifiDelay = SimManager.getInstance().getNetworkModel()
					.getUploadDelay(task.getMobileDeviceId(), SimSettings.CLOUD_DATACENTER_ID, dummyTask);

			// Calculate Wi-Fi bandwidth in Mbps
			double wifiBW = (wifiDelay == 0) ? 0 : (1 / wifiDelay) * 1000; // Convert to Mbps

			// Get edge server utilization
			double edgeUtilization = SimManager.getInstance().getEdgeServerManager().getAvgUtilization();

			// Offloading decision based on policy
			if (policy.equals("NETWORK_BASED")) {
				result = (wifiBW > 20) ? SimSettings.CLOUD_DATACENTER_ID : SimSettings.GENERIC_EDGE_DEVICE_ID;
			} else if (policy.equals("UTILIZATION_BASED")) {
				result = (edgeUtilization > 80) ? SimSettings.CLOUD_DATACENTER_ID : SimSettings.GENERIC_EDGE_DEVICE_ID;
			} else if (policy.equals("HYBRID")) {
				result = (wifiBW > 20 && edgeUtilization > 80) ? SimSettings.CLOUD_DATACENTER_ID : SimSettings.GENERIC_EDGE_DEVICE_ID;
			} else {
				SimLogger.printLine("Unknown policy: " + policy + ". Terminating simulation...");
				System.exit(0);
				return -1; // For compiler compliance
			}
		} else {
			SimLogger.printLine("Unknown simulation scenario: " + simScenario + ". Terminating simulation...");
			System.exit(0);
			return -1; // For compiler compliance
		}

		return result;
	}

	@Override
	public Vm getVmToOffload(Task task, int deviceId) {
		Vm selectedVM = null;

		if(deviceId == SimSettings.CLOUD_DATACENTER_ID){
			//Select VM on cloud devices via Least Loaded algorithm!
			double selectedVmCapacity = 0; //start with min value
			List<Host> list = SimManager.getInstance().getCloudServerManager().getDatacenter().getHostList();
			for (int hostIndex=0; hostIndex < list.size(); hostIndex++) {
				List<CloudVM> vmArray = SimManager.getInstance().getCloudServerManager().getVmList(hostIndex);
                for (CloudVM cloudVM : vmArray) {
					double targetVmCapacity = calculateVmCapacity(null, cloudVM, task);
					if (targetVmCapacity > selectedVmCapacity) {
						selectedVM = cloudVM;
						selectedVmCapacity = targetVmCapacity;
					}
                }
			}
		}
		else if(deviceId == SimSettings.GENERIC_EDGE_DEVICE_ID){
			//Select VM on edge devices via Least Loaded algorithm!
			double selectedVmCapacity = 0; //start with min value
			for(int hostIndex=0; hostIndex<numberOfHost; hostIndex++){
				List<EdgeVM> vmArray = SimManager.getInstance().getEdgeServerManager().getVmList(hostIndex);
                for (EdgeVM edgeVM : vmArray) {
					double targetVmCapacity = calculateVmCapacity(edgeVM, null, task);
					if (targetVmCapacity > selectedVmCapacity) {
						selectedVM = edgeVM;
						selectedVmCapacity = targetVmCapacity;
					}
                }
			}
		}
		else{
			SimLogger.printLine("Unknown device id! The simulation has been terminated.");
			System.exit(0);
		}

		return selectedVM;
	}

	/**
	 * Calculates the available capacity of a VM for the given task.
	 */
	private double calculateVmCapacity(EdgeVM edgeVM, CloudVM cloudVM, Task task) {
		double requiredCapacity = 0.0;
		double availableCapacity = 0.0;
		if (edgeVM != null) {
			requiredCapacity = ((CpuUtilizationModel_Custom) task.getUtilizationModelCpu()).predictUtilization(edgeVM.getVmType());
			availableCapacity = 100 - edgeVM.getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
		} else if (cloudVM != null) {
			requiredCapacity = ((CpuUtilizationModel_Custom) task.getUtilizationModelCpu()).predictUtilization(cloudVM.getVmType());
			availableCapacity = 100 - cloudVM.getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
		}
		return (requiredCapacity <= availableCapacity) ? availableCapacity : 0;
	}

	@Override
	public void processEvent(SimEvent arg0) {
		// Nothing to do!
	}

	@Override
	public void shutdownEntity() {
		// Nothing to do!
	}

	@Override
	public void startEntity() {
		// Nothing to do!
	}

}
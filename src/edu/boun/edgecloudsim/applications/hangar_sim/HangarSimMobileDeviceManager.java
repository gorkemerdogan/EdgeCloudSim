/*
 * Title:        HangarSimMobileDeviceManager - Custom Mobile Device Manager
 *
 * Description:
 * This class manages mobile devices' interactions in a simulation scenario
 * focusing on LAN and Wi-Fi communication.
 * - Tasks are submitted to edge servers using LAN (within the same hangar).
 * - Tasks are submitted to cloud servers using Wi-Fi (remote data centers).
 *
 * Features:
 * - Dynamic task creation and submission.
 * - Delay modeling based on LAN and Wi-Fi configurations.
 * - Simplified handling of task uploads and downloads.
 */

package edu.boun.edgecloudsim.applications.hangar_sim;

import edu.boun.edgecloudsim.network.NetworkModel;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.core.SimSettings.NETWORK_DELAY_TYPES;
import edu.boun.edgecloudsim.edge_client.CpuUtilizationModel_Custom;
import edu.boun.edgecloudsim.edge_client.MobileDeviceManager;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.utils.TaskProperty;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;

public class HangarSimMobileDeviceManager extends MobileDeviceManager {
	private static final int BASE = 100000; // Start from a unique base value for event tags
	private static final int REQUEST_RECEIVED_BY_CLOUD = BASE + 1;
	private static final int REQUEST_RECEIVED_BY_EDGE_DEVICE = BASE + 2;
	private static final int RESPONSE_RECEIVED_BY_MOBILE_DEVICE = BASE + 3;

	private int taskIdCounter = 0; // Counter for unique task IDs

	public HangarSimMobileDeviceManager() throws Exception {}

	@Override
	public void initialize() {
		// Initialization logic (if required)
	}

	@Override
	public UtilizationModel getCpuUtilizationModel() {
		return new CpuUtilizationModel_Custom();
	}

	/**
	 * Processes cloudlet return events (task completion events).
	 */
	@Override
	protected void processCloudletReturn(SimEvent ev) {
		NetworkModel networkModel = SimManager.getInstance().getNetworkModel();
		Task task = (Task) ev.getData();

		SimLogger.getInstance().taskExecuted(task.getCloudletId());

		if (task.getAssociatedDatacenterId() == SimSettings.CLOUD_DATACENTER_ID) {
			// Handle task completion from cloud server
			double wifiDelay = networkModel.getDownloadDelay(SimSettings.CLOUD_DATACENTER_ID, task.getMobileDeviceId(), task);
			if (wifiDelay > 0) {
				networkModel.downloadStarted(task.getSubmittedLocation(), SimSettings.CLOUD_DATACENTER_ID);
				SimLogger.getInstance().setDownloadDelay(task.getCloudletId(), wifiDelay, NETWORK_DELAY_TYPES.WIFI_DELAY);
				schedule(getId(), wifiDelay, RESPONSE_RECEIVED_BY_MOBILE_DEVICE, task);
			} else {
				SimLogger.getInstance().failedDueToBandwidth(task.getCloudletId(), CloudSim.clock(), NETWORK_DELAY_TYPES.WIFI_DELAY);
			}
		} else {
			// Handle task completion from edge server
			double lanDelay = networkModel.getDownloadDelay(task.getAssociatedHostId(), task.getMobileDeviceId(), task);
			if (lanDelay > 0) {
				networkModel.downloadStarted(task.getSubmittedLocation(), SimSettings.GENERIC_EDGE_DEVICE_ID);
				SimLogger.getInstance().setDownloadDelay(task.getCloudletId(), lanDelay, NETWORK_DELAY_TYPES.LAN_DELAY);
				schedule(getId(), lanDelay, RESPONSE_RECEIVED_BY_MOBILE_DEVICE, task);
			} else {
				SimLogger.getInstance().failedDueToBandwidth(task.getCloudletId(), CloudSim.clock(), NETWORK_DELAY_TYPES.LAN_DELAY);
			}
		}
	}

	/**
	 * Handles task submission logic.
	 */
	public void submitTask(TaskProperty edgeTask) {
		NetworkModel networkModel = SimManager.getInstance().getNetworkModel();
		Task task = createTask(edgeTask);
		Location currentLocation = SimManager.getInstance().getMobilityModel().getLocation(task.getMobileDeviceId(), CloudSim.clock());
		task.setSubmittedLocation(currentLocation);

		SimLogger.getInstance().addLog(task.getMobileDeviceId(),
				task.getCloudletId(),
				task.getTaskType(),
				(int) task.getCloudletLength(),
				(int) task.getCloudletFileSize(),
				(int) task.getCloudletOutputSize());

		int nextHopId = SimManager.getInstance().getEdgeOrchestrator().getDeviceToOffload(task);
		int nextEvent;
		NETWORK_DELAY_TYPES delayType;
		double delay;

		if (nextHopId == SimSettings.CLOUD_DATACENTER_ID) {
			// Task offloaded to cloud via Wi-Fi
			delay = networkModel.getUploadDelay(task.getMobileDeviceId(), SimSettings.CLOUD_DATACENTER_ID, task);
			nextEvent = REQUEST_RECEIVED_BY_CLOUD;
			delayType = NETWORK_DELAY_TYPES.WIFI_DELAY;
		} else {
			// Task offloaded to local edge server via LAN
			delay = networkModel.getUploadDelay(task.getMobileDeviceId(), SimSettings.GENERIC_EDGE_DEVICE_ID, task);
			nextEvent = REQUEST_RECEIVED_BY_EDGE_DEVICE;
			delayType = NETWORK_DELAY_TYPES.LAN_DELAY;
		}

		if (delay > 0) {
			// Assign task to the selected VM
			Vm selectedVM = SimManager.getInstance().getEdgeOrchestrator().getVmToOffload(task, nextHopId);
			if (selectedVM != null) {
				task.setAssociatedDatacenterId(nextHopId);
				task.setAssociatedHostId(selectedVM.getHost().getId());
				task.setAssociatedVmId(selectedVM.getId());
				getCloudletList().add(task);
				bindCloudletToVm(task.getCloudletId(), selectedVM.getId());
				networkModel.uploadStarted(currentLocation, nextHopId);
				SimLogger.getInstance().taskStarted(task.getCloudletId(), CloudSim.clock());
				SimLogger.getInstance().setUploadDelay(task.getCloudletId(), delay, delayType);
				schedule(getId(), delay, nextEvent, task);
			} else {
				SimLogger.getInstance().rejectedDueToVMCapacity(task.getCloudletId(), CloudSim.clock(), nextHopId);
			}
		} else {
			SimLogger.getInstance().rejectedDueToBandwidth(task.getCloudletId(), CloudSim.clock(), nextHopId, delayType);
		}
	}

	/**
	 * Creates a task with the specified properties.
	 */
	private Task createTask(TaskProperty edgeTask) {
		UtilizationModel utilizationModel = new UtilizationModelFull();
		UtilizationModel utilizationModelCPU = getCpuUtilizationModel();

		Task task = new Task(edgeTask.getMobileDeviceId(), ++taskIdCounter,
				edgeTask.getLength(), edgeTask.getPesNumber(),
				edgeTask.getInputFileSize(), edgeTask.getOutputFileSize(),
				utilizationModelCPU, utilizationModel, utilizationModel);

		task.setUserId(this.getId());
		task.setTaskType(edgeTask.getTaskType());

		if (utilizationModelCPU instanceof CpuUtilizationModel_Custom) {
			((CpuUtilizationModel_Custom) utilizationModelCPU).setTask(task);
		}

		return task;
	}
}
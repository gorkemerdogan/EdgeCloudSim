/*
 * Title:        HangarSimNetworkModel - Custom Network Model
 *
 * Description:
 * This custom network model replaces WAN with Wi-Fi,
 * simulating moderate latency and variable bandwidth.
 *
 * Features:
 * - Fixed LAN bandwidth and minimal delay for local communication.
 * - Realistic Wi-Fi delay modeling for remote communication.
 *
 * Author: Custom Implementation based on EdgeCloudSim framework.
 */

package edu.boun.edgecloudsim.applications.hangar_sim;

import org.cloudbus.cloudsim.core.CloudSim;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;

public class HangarSimNetworkModel extends NetworkModel {
	// Enum for network types
	public static enum NETWORK_TYPE {LAN, WIFI};

	// Bandwidth settings
	private static final double LAN_BANDWIDTH = 1000 * 1024 * 1024; // 1 Gbps in Kbps
	private static final double WIFI_BANDWIDTH = 100 * 1024; // 100 Mbps in Kbps

	// Delay settings
	private static final double LAN_BASE_DELAY = 0.5; // Fixed LAN delay in ms
	private static final double WIFI_BASE_DELAY = 5.0; // Base Wi-Fi delay in ms
	private static final double WIFI_DELAY_VARIABILITY = 10; // Variability for Wi-Fi delay in ms

	public static final double[] experimentalLanDelay = new double[180];
	public static final double[] experimentalWifiDelay = new double[180];

	// Generate LAN and Wi-Fi delays
	static {
		double lanMinDelay = 0.5; // ms - Wired Ethernet (10 Gbps)
		double wifiMinDelay = 5.0; // ms
		double lanGrowthFactor = 0.005; // Exponential growth factor for LAN
		double wifiGrowthFactor = 0.015; // Exponential growth factor for Wi-Fi

		for (int i = 1; i <= 180; i++) {
			experimentalLanDelay[i - 1] = lanMinDelay * Math.exp(lanGrowthFactor * (i - 1));
			experimentalWifiDelay[i - 1] = wifiMinDelay * Math.exp(wifiGrowthFactor * (i - 1));
		}
	}

	// Number of active users per network
	private int[] wifiClients;
	private int[] lanClients;

	public HangarSimNetworkModel(int _numberOfMobileDevices, String _simScenario) {
		super(_numberOfMobileDevices, _simScenario);
	}

	@Override
	public void initialize() {
		// Initialize Wi-Fi and LAN client tracking
		wifiClients = new int[SimSettings.getInstance().getNumOfEdgeDatacenters()];
		lanClients = new int[SimSettings.getInstance().getNumOfEdgeDatacenters()];
	}

	/**
	 * Upload delay calculation based on network type.
	 */
	@Override
	public double getUploadDelay(int sourceDeviceId, int destDeviceId, Task task) {
		Location accessPointLocation = SimManager.getInstance().getMobilityModel().getLocation(sourceDeviceId, CloudSim.clock());
		double dataSize = task.getCloudletFileSize(); // Task size in KB

		if (destDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID) {
			// LAN upload delay (e.g., within the same hangar)
			return calculateLanDelay(accessPointLocation, dataSize);
		} else if (destDeviceId == SimSettings.CLOUD_DATACENTER_ID) {
			// Wi-Fi upload delay (e.g., to a remote server)
			return calculateWifiDelay(accessPointLocation, dataSize);
		}
		return 0;
	}

	/**
	 * Download delay calculation based on network type.
	 */
	@Override
	public double getDownloadDelay(int sourceDeviceId, int destDeviceId, Task task) {
		Location accessPointLocation = SimManager.getInstance().getMobilityModel().getLocation(destDeviceId, CloudSim.clock());
		double dataSize = task.getCloudletOutputSize(); // Task size in KB

		if (sourceDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID) {
			// LAN download delay (e.g., within the same hangar)
			return calculateLanDelay(accessPointLocation, dataSize);
		} else if (sourceDeviceId == SimSettings.CLOUD_DATACENTER_ID) {
			// Wi-Fi download delay (e.g., from a remote server)
			return calculateWifiDelay(accessPointLocation, dataSize);
		}
		return 0;
	}

	/**
	 * Handles the start of an upload process.
	 */
	@Override
	public void uploadStarted(Location accessPointLocation, int destDeviceId) {
		if (destDeviceId == SimSettings.CLOUD_DATACENTER_ID) {
			wifiClients[accessPointLocation.getServingWlanId()]++;
		}
	}

	/**
	 * Handles the end of an upload process.
	 */
	@Override
	public void uploadFinished(Location accessPointLocation, int destDeviceId) {
		if (destDeviceId == SimSettings.CLOUD_DATACENTER_ID) {
			wifiClients[accessPointLocation.getServingWlanId()]--;
		}
	}

	/**
	 * Handles the start of a download process.
	 */
	@Override
	public void downloadStarted(Location accessPointLocation, int sourceDeviceId) {
		if (sourceDeviceId == SimSettings.CLOUD_DATACENTER_ID) {
			wifiClients[accessPointLocation.getServingWlanId()]++;
		}
	}

	/**
	 * Handles the end of a download process.
	 */
	@Override
	public void downloadFinished(Location accessPointLocation, int sourceDeviceId) {
		if (sourceDeviceId == SimSettings.CLOUD_DATACENTER_ID) {
			wifiClients[accessPointLocation.getServingWlanId()]--;
		}
	}

	/**
	 * Calculates LAN delay (includes variability and congestion).
	 */
	private double calculateLanDelay(Location accessPointLocation, double dataSize) {
		int numOfLanUsers = lanClients[accessPointLocation.getServingWlanId()];
		double dataSizeInKb = dataSize * 8; // Convert KB to Kb

		// Ensure the index is within the valid range (0 to experimentalLanDelay.length - 1)
		int index = Math.max(0, Math.min(numOfLanUsers - 1, experimentalLanDelay.length - 1));
		double baseDelay = experimentalLanDelay[index];
		double bandwidthDelay = dataSizeInKb / LAN_BANDWIDTH; // Minimal impact due to high bandwidth

		return baseDelay + bandwidthDelay;
	}

	/**
	 * Calculates Wi-Fi delay (includes variability and congestion).
	 */
	private double calculateWifiDelay(Location accessPointLocation, double dataSize) {
		int numOfWifiUsers = wifiClients[accessPointLocation.getServingWlanId()];
		double dataSizeInKb = dataSize * 8; // Convert KB to Kb

		// Ensure the index is within the valid range (0 to experimentalWifiDelay.length - 1)
		int index = Math.max(0, Math.min(numOfWifiUsers - 1, experimentalWifiDelay.length - 1));
		double baseDelay = experimentalWifiDelay[index];
		double bandwidthDelay = (dataSizeInKb / WIFI_BANDWIDTH); // Delay based on Wi-Fi bandwidth

		// Add base Wi-Fi delay and variability
		return baseDelay + bandwidthDelay + (Math.random() * WIFI_DELAY_VARIABILITY - WIFI_DELAY_VARIABILITY / 2);
	}
}
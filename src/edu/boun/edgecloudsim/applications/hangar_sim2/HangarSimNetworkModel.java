/*
 * Title:        EdgeCloudSim - Network Model
 *
 * Description:
 * SampleNetworkModel uses
 * -> the result of an empirical study for the LAN and WAN delays
 * The experimental network model is developed
 * by taking measurements from the real life deployments.
 *
 * -> MMPP/MMPP/1 queue model for MAN delay
 * MAN delay is observed via a single server queue model with
 * Markov-modulated Poisson process (MMPP) arrivals.
 *
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.applications.hangar_sim2;

import org.cloudbus.cloudsim.core.CloudSim;

import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;

public class HangarSimNetworkModel extends NetworkModel {
	public static enum NETWORK_TYPE {WLAN, LAN};
	public static enum LINK_TYPE {DOWNLOAD, UPLOAD};
	public static double MAN_BW = 1300*1024; //Kbps

	@SuppressWarnings("unused")
	private int manClients;
	private int[] wanClients;
	private int[] lanClients;

	private double lastMM1QueueUpdateTime;
	private double ManPoissonMeanForDownload; //seconds
	private double ManPoissonMeanForUpload; //seconds

	private double avgManTaskInputSize; //bytes
	private double avgManTaskOutputSize; //bytes

	//record last n task statistics during MM1_QUEUE_MODEL_UPDATE_INTEVAL seconds to simulate mmpp/m/1 queue model
	private double totalManTaskInputSize;
	private double totalManTaskOutputSize;
	private double numOfManTaskForDownload;
	private double numOfManTaskForUpload;

	public static final double[] experimentalLanDelay = new double[180];

	// Generate LAN delays programmatically
	static {
		double baseBandwidth = 1000000; // Starting bandwidth (Kbps)
		double decayFactor = 0.96; // Decay factor for exponential drop

		for (int i = 0; i < experimentalLanDelay.length; i++) {
			experimentalLanDelay[i] = baseBandwidth * Math.pow(decayFactor, i);
		}
	}

	public static final double[] experimentalWanDelay = new double[180];

	// Generate Wi-Fi delays programmatically
	static {
		double baseBandwidth = 500000; // Starting bandwidth (Kbps)
		double decayFactor = 0.95; // Decay factor for exponential drop

		for (int i = 0; i < experimentalWanDelay.length; i++) {
			experimentalWanDelay[i] = baseBandwidth * Math.pow(decayFactor, i);
		}
	}

	public HangarSimNetworkModel(int _numberOfMobileDevices, String _simScenario) {
		super(_numberOfMobileDevices, _simScenario);
	}

	@Override
	public void initialize() {
		wanClients = new int[SimSettings.getInstance().getNumOfEdgeDatacenters()];  //we have one access point for each datacenter
		lanClients = new int[SimSettings.getInstance().getNumOfEdgeDatacenters()];  //we have one access point for each datacenter

		int numOfApp = SimSettings.getInstance().getTaskLookUpTable().length;
		SimSettings SS = SimSettings.getInstance();
		for(int taskIndex=0; taskIndex<numOfApp; taskIndex++) {
			if(SS.getTaskLookUpTable()[taskIndex][0] == 0) {
				SimLogger.printLine("Usage percentage of task " + taskIndex + " is 0! Terminating simulation...");
				System.exit(0);
			}
			else{
				double weight = SS.getTaskLookUpTable()[taskIndex][0]/(double)100;

				//assume half of the tasks use the MAN at the beginning
				ManPoissonMeanForDownload += ((SS.getTaskLookUpTable()[taskIndex][2])*weight) * 4;
				ManPoissonMeanForUpload = ManPoissonMeanForDownload;

				avgManTaskInputSize += SS.getTaskLookUpTable()[taskIndex][5]*weight;
				avgManTaskOutputSize += SS.getTaskLookUpTable()[taskIndex][6]*weight;
			}
		}

		ManPoissonMeanForDownload = ManPoissonMeanForDownload/numOfApp;
		ManPoissonMeanForUpload = ManPoissonMeanForUpload/numOfApp;
		avgManTaskInputSize = avgManTaskInputSize/numOfApp;
		avgManTaskOutputSize = avgManTaskOutputSize/numOfApp;

		lastMM1QueueUpdateTime = SimSettings.CLIENT_ACTIVITY_START_TIME;
		totalManTaskOutputSize = 0;
		numOfManTaskForDownload = 0;
		totalManTaskInputSize = 0;
		numOfManTaskForUpload = 0;
	}

	/**
	 * source device is always mobile device in our simulation scenarios!
	 */
	@Override
	public double getUploadDelay(int sourceDeviceId, int destDeviceId, Task task) {
		double delay = 0;

		//special case for man communication
		if(sourceDeviceId == destDeviceId && sourceDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID){
			return delay = getManUploadDelay();
		}

		Location accessPointLocation = SimManager.getInstance().getMobilityModel().getLocation(sourceDeviceId,CloudSim.clock());

		//mobile device to cloud server
		if(destDeviceId == SimSettings.CLOUD_DATACENTER_ID){
			delay = getWanUploadDelay(accessPointLocation, task.getCloudletFileSize());
		}
		//mobile device to edge device (wifi access point)
		else if (destDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID) {
			delay = getLanUploadDelay(accessPointLocation, task.getCloudletFileSize());
		}

		return delay;
	}

	/**
	 * destination device is always mobile device in our simulation scenarios!
	 */
	@Override
	public double getDownloadDelay(int sourceDeviceId, int destDeviceId, Task task) {
		double delay = 0;

		//special case for man communication
		if(sourceDeviceId == destDeviceId && sourceDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID){
			return delay = getManDownloadDelay();
		}

		Location accessPointLocation = SimManager.getInstance().getMobilityModel().getLocation(destDeviceId,CloudSim.clock());

		//cloud server to mobile device
		if(sourceDeviceId == SimSettings.CLOUD_DATACENTER_ID){
			delay = getWanDownloadDelay(accessPointLocation, task.getCloudletOutputSize());
		}
		//edge device (wifi access point) to mobile device
		else{
			delay = getLanDownloadDelay(accessPointLocation, task.getCloudletOutputSize());
		}

		return delay;
	}

	@Override
	public void uploadStarted(Location accessPointLocation, int destDeviceId) {
		if(destDeviceId == SimSettings.CLOUD_DATACENTER_ID)
			wanClients[accessPointLocation.getServingWlanId()]++;
		else if (destDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID)
			lanClients[accessPointLocation.getServingWlanId()]++;
		else if (destDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID+1)
			manClients++;
		else {
			SimLogger.printLine("Error - unknown device id in uploadStarted(). Terminating simulation...");
			System.exit(0);
		}
	}

	@Override
	public void uploadFinished(Location accessPointLocation, int destDeviceId) {
		if(destDeviceId == SimSettings.CLOUD_DATACENTER_ID)
			wanClients[accessPointLocation.getServingWlanId()]--;
		else if (destDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID)
			lanClients[accessPointLocation.getServingWlanId()]--;
		else if (destDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID+1)
			manClients--;
		else {
			SimLogger.printLine("Error - unknown device id in uploadFinished(). Terminating simulation...");
			System.exit(0);
		}
	}

	@Override
	public void downloadStarted(Location accessPointLocation, int sourceDeviceId) {
		if(sourceDeviceId == SimSettings.CLOUD_DATACENTER_ID)
			wanClients[accessPointLocation.getServingWlanId()]++;
		else if(sourceDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID)
			lanClients[accessPointLocation.getServingWlanId()]++;
		else if(sourceDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID+1)
			manClients++;
		else {
			SimLogger.printLine("Error - unknown device id in downloadStarted(). Terminating simulation...");
			System.exit(0);
		}
	}

	@Override
	public void downloadFinished(Location accessPointLocation, int sourceDeviceId) {
		if(sourceDeviceId == SimSettings.CLOUD_DATACENTER_ID)
			wanClients[accessPointLocation.getServingWlanId()]--;
		else if(sourceDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID)
			lanClients[accessPointLocation.getServingWlanId()]--;
		else if(sourceDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID+1)
			manClients--;
		else {
			SimLogger.printLine("Error - unknown device id in downloadFinished(). Terminating simulation...");
			System.exit(0);
		}
	}

	private double getLanDownloadDelay(Location accessPointLocation, double dataSize) {
		int numOfLanUser = lanClients[accessPointLocation.getServingWlanId()];
		double taskSizeInKb = dataSize * (double)8; //KB to Kb
		double result=0;

		if(numOfLanUser < experimentalLanDelay.length)
			result = taskSizeInKb /*Kb*/ / (experimentalLanDelay[numOfLanUser] * (double) 3 ) /*Kbps*/; //802.11ac is around 3 times faster than 802.11n

		//System.out.println("--> " + numOfLanUser + " user, " + taskSizeInKb + " KB, " +result + " sec");
		return result;
	}

	//lan upload and download delay is symmetric in this model
	private double getLanUploadDelay(Location accessPointLocation, double dataSize) {
		return getLanDownloadDelay(accessPointLocation, dataSize);
	}

	private double getWanDownloadDelay(Location accessPointLocation, double dataSize) {
		int numOfWanUser = wanClients[accessPointLocation.getServingWlanId()];
		double taskSizeInKb = dataSize * (double)8; //KB to Kb
		double result=0;

		if(numOfWanUser < experimentalWanDelay.length)
			result = taskSizeInKb /*Kb*/ / (experimentalWanDelay[numOfWanUser]) /*Kbps*/;

		//System.out.println("--> " + numOfWanUser + " user, " + taskSizeInKb + " KB, " +result + " sec");

		return result;
	}

	//wan upload and download delay is symmetric in this model
	private double getWanUploadDelay(Location accessPointLocation, double dataSize) {
		return getWanDownloadDelay(accessPointLocation, dataSize);
	}

	private double calculateMM1(double propagationDelay, double bandwidth /*Kbps*/, double PoissonMean, double avgTaskSize /*KB*/, int deviceCount){
		double mu=0, lamda=0;

		avgTaskSize = avgTaskSize * 8; //convert from KB to Kb

		lamda = ((double)1/(double)PoissonMean); //task per seconds
		mu = bandwidth /*Kbps*/ / avgTaskSize /*Kb*/; //task per seconds
		double result = (double)1 / (mu-lamda*(double)deviceCount);

		if(result < 0)
			return 0;

		result += propagationDelay;

		return (result > 15) ? 0 : result;
	}

	private double getManDownloadDelay() {
		double result = calculateMM1(SimSettings.getInstance().getInternalLanDelay(),
				MAN_BW,
				ManPoissonMeanForDownload,
				avgManTaskOutputSize,
				numberOfMobileDevices);

		totalManTaskOutputSize += avgManTaskOutputSize;
		numOfManTaskForDownload++;

		//System.out.println("--> " + SimManager.getInstance().getNumOfMobileDevice() + " user, " +result + " sec");

		return result;
	}

	private double getManUploadDelay() {
		double result = calculateMM1(SimSettings.getInstance().getInternalLanDelay(),
				MAN_BW,
				ManPoissonMeanForUpload,
				avgManTaskInputSize,
				numberOfMobileDevices);

		totalManTaskInputSize += avgManTaskInputSize;
		numOfManTaskForUpload++;

		//System.out.println(CloudSim.clock() + " -> " + SimManager.getInstance().getNumOfMobileDevice() + " user, " + result + " sec");

		return result;
	}

	public void updateMM1QueeuModel(){
		double lastInterval = CloudSim.clock() - lastMM1QueueUpdateTime;
		lastMM1QueueUpdateTime = CloudSim.clock();

		if(numOfManTaskForDownload != 0){
			ManPoissonMeanForDownload = lastInterval / (numOfManTaskForDownload / (double)numberOfMobileDevices);
			avgManTaskOutputSize = totalManTaskOutputSize / numOfManTaskForDownload;
		}
		if(numOfManTaskForUpload != 0){
			ManPoissonMeanForUpload = lastInterval / (numOfManTaskForUpload / (double)numberOfMobileDevices);
			avgManTaskInputSize = totalManTaskInputSize / numOfManTaskForUpload;
		}

		totalManTaskOutputSize = 0;
		numOfManTaskForDownload = 0;
		totalManTaskInputSize = 0;
		numOfManTaskForUpload = 0;
	}
}

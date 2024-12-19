package edu.boun.edgecloudsim.applications.AircraftHangarSimulation;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.task_generator.LoadGeneratorModel;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;
import edu.boun.edgecloudsim.utils.TaskProperty;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import java.util.ArrayList;

public class AircraftHangarLoadGenerator extends LoadGeneratorModel{
	int taskTypeOfDevices[];

	public AircraftHangarLoadGenerator(int _numberOfMobileDevices, double _simulationTime, String _simScenario) {
		super(_numberOfMobileDevices, _simulationTime, _simScenario);
	}

	@Override
	public void initializeModel() {
		taskList = new ArrayList<TaskProperty>();

		// Task parameters for 4K video streaming
		int videoTaskType = 0;  // Task type for video processing
		double videoDurationSec = 360;  // 360 seconds per task
		int videoPesNumber = 8;  // Number of cores for video processing
		long videoTaskLength = 14930000;  // Task length in MI (Million Instructions)

		// Task parameters for image processing
		int imageTaskType = 1;  // Task type for image processing
		double imageDurationSec = 60;  // Assume 60 seconds per task
		int imagePesNumber = 4;  // Number of cores for image processing

		/*
		 * Image processing workload
		 * - Resolution: 3840 x 2160 pixels (4K)
		 * - Instructions per pixel: 500
		 * MIs = 3.840 * 2.160 * 500 / 10^6 = 4.147 MI per image
		 */
		long imageTaskLength = 4147;  // Task length in MI for image processing

		// Generate tasks for each device
		for (int deviceId = 0; deviceId < numberOfMobileDevices; deviceId++) {
			double activePeriod = 300.0;
			double idlePeriod = 60.0;

			// Virtual time for video processing tasks
			double videoVirtualTime = SimUtils.getRandomDoubleNumber(
					SimSettings.CLIENT_ACTIVITY_START_TIME,
					SimSettings.CLIENT_ACTIVITY_START_TIME + activePeriod);

			while (videoVirtualTime < simulationTime) {
				// Randomize input size for video processing
				long videoInputSizeKB = SimUtils.getRandomLongNumber(25000, 40000);

				// Randomize output size for video processing (10% to 30% of input size)
				double videoOutputPercentage = SimUtils.getRandomDoubleNumber(0.1, 0.3);
				long videoOutputSizeKB = (long) (videoInputSizeKB * videoOutputPercentage);

				// Add video task to the list
				taskList.add(new TaskProperty(videoVirtualTime, deviceId, videoTaskType, videoPesNumber,
						videoTaskLength, videoInputSizeKB, videoOutputSizeKB));
				videoVirtualTime += videoDurationSec;
				videoVirtualTime += idlePeriod;
			}

			// Virtual time for image processing tasks
			double imageVirtualTime = SimUtils.getRandomDoubleNumber(
					SimSettings.CLIENT_ACTIVITY_START_TIME,
					SimSettings.CLIENT_ACTIVITY_START_TIME + activePeriod);

			while (imageVirtualTime < simulationTime) {
				// Randomize input size for image processing (1 MB to 5 MB)
				long imageInputSizeKB = SimUtils.getRandomLongNumber(1000, 5000);

				// Randomize output size for image processing (50% to 70% of input size)
				double imageOutputPercentage = SimUtils.getRandomDoubleNumber(0.5, 0.7);
				long imageOutputSizeKB = (long) (imageInputSizeKB * imageOutputPercentage);

				// Add image task to the list
				taskList.add(new TaskProperty(imageVirtualTime, deviceId, imageTaskType, imagePesNumber,
						imageTaskLength, imageInputSizeKB, imageOutputSizeKB));
				imageVirtualTime += imageDurationSec;
				imageVirtualTime += idlePeriod;
			}
		}
	}

	@Override
	public int getTaskTypeOfDevice(int deviceId) {
		if (taskTypeOfDevices == null) {
			taskTypeOfDevices = new int[numberOfMobileDevices];
			for (int i = 0; i < numberOfMobileDevices; i++) {
				taskTypeOfDevices[i] = SimUtils.getRandomNumber(0, 1);
				System.out.println("Device " + i + " assigned TaskType: " + taskTypeOfDevices[i]);
			}
		}

		System.out.println("Device " + deviceId + " TaskType Returned: " + taskTypeOfDevices[deviceId]);
		return taskTypeOfDevices[deviceId];
	}

}
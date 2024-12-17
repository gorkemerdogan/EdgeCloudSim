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
		int taskType = 0; 			// Define a single task type for video processing
		double durationSec = 60;    // 60 second per task
		int pesNumber = 16;         // Number of processing elements (cores) for the task

		/*
		•	Resolution: 3840x2160 pixels (8,294,400 pixels per frame).
		•	Frame Rate: 30 frames per second (FPS), common for 4K video.
		•	Total Frames: Multiply the duration (in seconds) by 30 FPS.
		•	1000 instructions per pixel

		MIs = (60*30 frames) * 8,294,400  * 1000 / 10^6 = 14,929,920,000 = 14.93 billion MIs

		•	1 Minute	1,800 frames	14,930,000 MIs
		•	2 Minutes	3,600 frames	29,860,000 MIs
		•	3 Minutes	5,400 frames	44,790,000 MIs
		 */
		long length = 14930000;           // Task length in MI (Million Instructions)

		// Generate tasks for each device
		for (int deviceId = 0; deviceId < numberOfMobileDevices; deviceId++) {
			double currentTime = 0;

			while (currentTime < simulationTime) {
				// Randomize input size between 20000 KB and 40000 KB
				// 4K stream size in KBps
				long inputSizeKB = SimUtils.getRandomLongNumber(20000, 40000);

				// Randomize output size as a percentage of input size (10% to 30%)
				// After edge processing (Assume 70-90% reduction)
				double randomPercentage = SimUtils.getRandomDoubleNumber(0.1, 0.3);
				long outputSizeKB = (long)(inputSizeKB * randomPercentage);

				// Add the task to the list
				taskList.add(new TaskProperty(currentTime, deviceId, taskType, pesNumber,
						length, inputSizeKB, outputSizeKB));
				currentTime += durationSec; // Continuous 1-second intervals
			}
		}
	}

	@Override
	public int getTaskTypeOfDevice(int deviceId) {
		if (taskTypeOfDevices == null) {
			taskTypeOfDevices = new int[numberOfMobileDevices];
			for (int i = 0; i < numberOfMobileDevices; i++) {
				// Randomly assign task type 0 or 1
				taskTypeOfDevices[i] = SimUtils.getRandomNumber(0, 1);
				System.out.println("Device " + i + " assigned TaskType: " + taskTypeOfDevices[i]);
			}
		}

		System.out.println("Device " + deviceId + " TaskType Returned: " + taskTypeOfDevices[deviceId]);
		return taskTypeOfDevices[deviceId];
	}

}
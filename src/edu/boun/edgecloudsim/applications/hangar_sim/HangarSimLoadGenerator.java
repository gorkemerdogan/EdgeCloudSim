/*
 * Title:        EdgeCloudSim - Idle/Active Load Generator implementation
 * 
 * Description: 
 * IdleActiveLoadGenerator implements basic load generator model where the
 * mobile devices generate task in active period and waits in idle period.
 * Task interarrival time (load generation period), Idle and active periods
 * are defined in the configuration file.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.applications.hangar_sim;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.task_generator.LoadGeneratorModel;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;
import edu.boun.edgecloudsim.utils.TaskProperty;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import java.util.ArrayList;

public class HangarSimLoadGenerator extends LoadGeneratorModel {

	// Task types
	private static final int IMAGE_PROCESSING = 0;
	private static final int VIDEO_PROCESSING = 1;

	int taskTypeOfDevices[];

	public HangarSimLoadGenerator(int _numberOfMobileDevices, double _simulationTime, String _simScenario) {
		super(_numberOfMobileDevices, _simulationTime, _simScenario);
	}

	@Override
	public void initializeModel() {
		taskList = new ArrayList<>();

		// Exponential number generator for task properties (input size, output size, task length)
		ExponentialDistribution[][] expRngList = new ExponentialDistribution[SimSettings.getInstance().getTaskLookUpTable().length][3];

		// Create random number generators for each task type
		for (int i = 0; i < SimSettings.getInstance().getTaskLookUpTable().length; i++) {
			if (SimSettings.getInstance().getTaskLookUpTable()[i][0] == 0)
				continue;

			expRngList[i][0] = new ExponentialDistribution(SimSettings.getInstance().getTaskLookUpTable()[i][5]); // Input size
			expRngList[i][1] = new ExponentialDistribution(SimSettings.getInstance().getTaskLookUpTable()[i][6]); // Output size
			expRngList[i][2] = new ExponentialDistribution(SimSettings.getInstance().getTaskLookUpTable()[i][7]); // Task length
		}

		// Assign task types (image processing or video processing) to devices
		taskTypeOfDevices = new int[numberOfMobileDevices];
		for (int i = 0; i < numberOfMobileDevices; i++) {
			int randomTaskType = assignTaskType();
			taskTypeOfDevices[i] = randomTaskType;

			// Generate tasks for the assigned task type
			generateTasksForDevice(i, randomTaskType, expRngList);
		}
	}

	/**
	 * Assigns a task type (Image Processing or Video Processing) to a device
	 * based on pre-configured probabilities in the task lookup table.
	 */
	private int assignTaskType() {
		int randomTaskType = -1;
		double taskTypeSelector = SimUtils.getRandomDoubleNumber(0, 100);
		double taskTypePercentage = 0;

		for (int j = 0; j < SimSettings.getInstance().getTaskLookUpTable().length; j++) {
			taskTypePercentage += SimSettings.getInstance().getTaskLookUpTable()[j][0];
			if (taskTypeSelector <= taskTypePercentage) {
				randomTaskType = j;
				break;
			}
		}

		if (randomTaskType == -1) {
			SimLogger.printLine("Error: No task type selected!");
		}

		return randomTaskType;
	}

	/**
	 * Generates tasks for a specific device during its active periods.
	 */
	private void generateTasksForDevice(int deviceId, int taskType, ExponentialDistribution[][] expRngList) {
		double poissonMean = SimSettings.getInstance().getTaskLookUpTable()[taskType][2];
		double activePeriod = SimSettings.getInstance().getTaskLookUpTable()[taskType][3];
		double idlePeriod = SimSettings.getInstance().getTaskLookUpTable()[taskType][4];
		double activePeriodStartTime = SimUtils.getRandomDoubleNumber(
				SimSettings.CLIENT_ACTIVITY_START_TIME,
				SimSettings.CLIENT_ACTIVITY_START_TIME + activePeriod);
		double virtualTime = activePeriodStartTime;

		ExponentialDistribution rng = new ExponentialDistribution(poissonMean);

		while (virtualTime < simulationTime) {
			double interval = rng.sample();

			if (interval <= 0) {
				SimLogger.printLine("Warning: Interval is " + interval + " for device " + deviceId + " at time " + virtualTime);
				continue;
			}

			virtualTime += interval;

			// Handle transitions between active and idle periods
			if (virtualTime > activePeriodStartTime + activePeriod) {
				activePeriodStartTime = activePeriodStartTime + activePeriod + idlePeriod;
				virtualTime = activePeriodStartTime;
				continue;
			}

			// Add the task to the task list
			taskList.add(new TaskProperty(deviceId, taskType, virtualTime, expRngList));
		}
	}

	@Override
	public int getTaskTypeOfDevice(int deviceId) {
		// TODO Auto-generated method stub
		return taskTypeOfDevices[deviceId];
	}

}

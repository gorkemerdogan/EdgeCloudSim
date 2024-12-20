package edu.boun.edgecloudsim.applications.AircraftHangarScenario;

import edu.boun.edgecloudsim.edge_client.Task;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.ArrayList;
import java.util.List;

public class AircraftHangarLoadGenerator extends SimEntity {
	private int taskIdCounter = 0; // Counter for task IDs
	private List<Task> taskList = new ArrayList<>(); // Holds generated tasks
	private int numberOfMobileDevices;
	private static final double VIDEO_STREAM_INTERVAL = 5.0; // Stream every 5 seconds

	public AircraftHangarLoadGenerator(String name, int numberOfMobileDevices) {
		super(name);
		this.numberOfMobileDevices = numberOfMobileDevices;
	}

	@Override
	public void startEntity() {
		initializeModel(); // Start generating tasks
	}

	@Override
	public void processEvent(SimEvent ev) {
		int cameraId = (int) ev.getData();
		scheduleTask(VIDEO_STREAM_INTERVAL, cameraId); // Reschedule for the next interval
	}

	@Override
	public void shutdownEntity() {
		System.out.println("Shutting down AircraftHangarLoadGenerator...");
	}

	private void initializeModel() {
		for (int i = 0; i < numberOfMobileDevices; i++) {
			scheduleTask(VIDEO_STREAM_INTERVAL, i); // Schedule tasks for all cameras
		}
	}

	private void scheduleTask(double interval, int cameraId) {
		Task task = new Task(cameraId, taskIdCounter++,
				1000, // Task length
				1, // PEs (number of CPUs)
				15 * 1024 * 1024, // Input size: 15 MB in bytes
				1024, // Output size: 1 KB
				null, null, null); // Utilization models can be null for now

		addTaskToQueue(task);

		// Proper scheduling using SimEntity's schedule method
		schedule(getId(), interval, cameraId);
	}

	private void addTaskToQueue(Task task) {
		taskList.add(task);
	}
}
package edu.boun.edgecloudsim.applications.AircraftHangarSimulation;

import edu.boun.edgecloudsim.mobility.NomadicMobility;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

public class AircraftHangarNomadicMobility extends NomadicMobility {

    public AircraftHangarNomadicMobility(int _numberOfMobileDevices, double _simulationTime) {
        super(_numberOfMobileDevices, _simulationTime);
    }

    @Override
    public void initialize() {
        // Initialize treeMapArray through the setter
        List<TreeMap<Double, Location>> tempTreeMapArray = new ArrayList<>();

        // Force all devices to use the same wlan_id = 0
        for (int i = 0; i < numberOfMobileDevices; i++) {
            TreeMap<Double, Location> deviceMap = new TreeMap<>();
            double startTime = 10.0; // Devices start after 10 seconds

            // Assign fixed location with wlan_id = 0
            int wlan_id = 0;
            int x_pos = 0;
            int y_pos = 0;

            deviceMap.put(-1.0, new Location(0, wlan_id, x_pos, y_pos)); // Add location at negative time
            deviceMap.put(0.0, new Location(0, wlan_id, x_pos, y_pos));  // Add location at time 0.0
            deviceMap.put(10.0, new Location(0, wlan_id, x_pos, y_pos)); // Location after 10.0 seconds
            tempTreeMapArray.add(deviceMap);
        }

        // Set the treeMapArray in the superclass
        setTreeMapArray(tempTreeMapArray);
    }

    @Override
    public Location getLocation(int deviceId, double time) {
        TreeMap<Double, Location> treeMap = getTreeMap(deviceId);

        if (treeMap == null) {
            SimLogger.printLine("Error: Device " + deviceId + " has no assigned location!");
            System.exit(1);
        }

        Entry<Double, Location> e = treeMap.floorEntry(time);

        if (e == null) {
            SimLogger.printLine("Error: No location found for device '" + deviceId + "' at time " + time);
            System.exit(1);
        }

        return e.getValue();
    }
}
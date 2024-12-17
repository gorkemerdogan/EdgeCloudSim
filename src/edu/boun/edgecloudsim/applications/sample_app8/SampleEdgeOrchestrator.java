/*
 * Title:        EdgeCloudSim - Edge Orchestrator
 *
 * Description:
 * SampleEdgeOrchestrator offloads tasks to proper server
 * by considering WAN bandwidth and edge server utilization.
 * After the target server is decided, the least loaded VM is selected.
 * If the target server is a remote edge server, MAN is used.
 *
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.applications.sample_app8;

import edu.boun.edgecloudsim.cloud_server.CloudVM;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_client.CpuUtilizationModel_Custom;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.boun.edgecloudsim.utils.SimLogger;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.List;
import java.util.Random;

public class SampleEdgeOrchestrator extends EdgeOrchestrator {

    private int numberOfHost; //used by load balancer

    public SampleEdgeOrchestrator(String _policy, String _simScenario) {
        super(_policy, _simScenario);
    }

    @Override
    public void initialize() {
        numberOfHost=SimSettings.getInstance().getNumOfEdgeHosts();
    }

    /*
     * (non-Javadoc)
     * @see edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator#getDeviceToOffload(edu.boun.edgecloudsim.edge_client.Task)
     *
     * It is assumed that the edge orchestrator app is running on the edge devices in a distributed manner
     */
    @Override
    public int getDeviceToOffload(Task task) {
        int result = 0;

        //RODO: return proper host ID

        if(simScenario.equals("SINGLE_TIER")){
            result = SimSettings.GENERIC_EDGE_DEVICE_ID;
        }
        else {
            SimLogger.printLine("Unknown simulation scenario! Terminating simulation...");
            System.exit(0);
        }
        return result;
    }

    @Override
    public Vm getVmToOffload(Task task, int deviceId) {
        Vm selectedVM = null;

        if(deviceId == SimSettings.CLOUD_DATACENTER_ID){
            //Select VM on cloud devices via Least Loaded algorithm!
            SimLogger.printLine("Only Edge Device can be used for this scenario! The simulation has been terminated.");
            System.exit(0);
        }
        else if(deviceId == SimSettings.GENERIC_EDGE_DEVICE_ID){

            if(policy.equals("LEAST_LOADED")) {
                //Select VM on edge devices via Least Loaded algorithm!
                double selectedVmCapacity = 0; //start with min value
                for (int hostIndex = 0; hostIndex < numberOfHost; hostIndex++) {
                    List<EdgeVM> vmArray = SimManager.getInstance().getEdgeServerManager().getVmList(hostIndex);
                    for (int vmIndex = 0; vmIndex < vmArray.size(); vmIndex++) {
                        double requiredCapacity = ((CpuUtilizationModel_Custom) task.getUtilizationModelCpu()).predictUtilization(vmArray.get(vmIndex).getVmType());
                        double targetVmCapacity = (double) 100 - vmArray.get(vmIndex).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
                        if (requiredCapacity <= targetVmCapacity && targetVmCapacity > selectedVmCapacity) {
                            selectedVM = vmArray.get(vmIndex);
                            selectedVmCapacity = targetVmCapacity;
                        }
                    }
                }
            }

            else if(policy.equals("FIRST_FIT")){
                for (int hostIndex = 0; hostIndex < numberOfHost; hostIndex++) {
                    List<EdgeVM> vmArray = SimManager.getInstance().getEdgeServerManager().getVmList(hostIndex);
                    for (int vmIndex = 0; vmIndex < vmArray.size(); vmIndex++) {
                        double requiredCapacity = ((CpuUtilizationModel_Custom) task.getUtilizationModelCpu()).predictUtilization(vmArray.get(vmIndex).getVmType());
                        double targetVmCapacity = (double) 100 - vmArray.get(vmIndex).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
                        if (requiredCapacity <= targetVmCapacity) {
                            selectedVM = vmArray.get(vmIndex);
                            // Stop searching after the first fit is found
                            break;
                        }
                    }
                    if (selectedVM != null) {
                        // Break the outer loop as well if a VM is selected
                        break;
                    }
                }
                // selectedVM contains the VM chosen by the First Fit algorithm
            }
            else if(policy.equals("BEST_FIT")){
                double selectedVmCapacity = 0; // start with min value
                for (int hostIndex = 0; hostIndex < numberOfHost; hostIndex++) {
                    List<EdgeVM> vmArray = SimManager.getInstance().getEdgeServerManager().getVmList(hostIndex);
                    for (int vmIndex = 0; vmIndex < vmArray.size(); vmIndex++) {
                        double requiredCapacity = ((CpuUtilizationModel_Custom) task.getUtilizationModelCpu()).predictUtilization(vmArray.get(vmIndex).getVmType());
                        double targetVmCapacity = (double) 100 - vmArray.get(vmIndex).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
                        if (requiredCapacity <= targetVmCapacity && targetVmCapacity > selectedVmCapacity) {
                            selectedVM = vmArray.get(vmIndex);
                            selectedVmCapacity = targetVmCapacity;
                        }
                    }
                }
                // selectedVM contains the VM chosen by the Best Fit algorithm

            }
            else if(policy.equals("WORST_FIT")){
                double selectedVmCapacity = 9999; // start with max value

                for (int hostIndex = 0; hostIndex < numberOfHost; hostIndex++) {
                    List<EdgeVM> vmArray = SimManager.getInstance().getEdgeServerManager().getVmList(hostIndex);

                    for (int vmIndex = 0; vmIndex < vmArray.size(); vmIndex++) {
                        double requiredCapacity = ((CpuUtilizationModel_Custom) task.getUtilizationModelCpu()).predictUtilization(vmArray.get(vmIndex).getVmType());
                        double targetVmCapacity = (double) 100 - vmArray.get(vmIndex).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());

                        if (requiredCapacity <= targetVmCapacity && targetVmCapacity < selectedVmCapacity) {
                            selectedVM = vmArray.get(vmIndex);
                            selectedVmCapacity = targetVmCapacity;
                        }
                    }
                }
                // selectedVM contains the VM chosen by the Worst Fit algorithm

            }
            else if(policy.equals("RANDOM_FIT")){
                Random random = new Random();
                int randomHostIndex = random.nextInt(numberOfHost);
                List<EdgeVM> vmArray = SimManager.getInstance().getEdgeServerManager().getVmList(randomHostIndex);
                int randomIndex = random.nextInt(vmArray.size());
                selectedVM = vmArray.get(randomIndex);

            }
            // selectedVM contains the VM chosen by the Random Fit algorithm
            else {
                SimLogger.printLine("Unknown edge orchestrator policy! Terminating simulation...");
                System.exit(0);
            }
        }
        else{
            SimLogger.printLine("Unknown device id! The simulation has been terminated.");
            System.exit(0);
        }

        return selectedVM;
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
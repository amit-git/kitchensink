package kitchen.sink.mesos;

import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MyFramework {
    private static Logger log = LoggerFactory.getLogger(MyFramework.class);

    public static class MyScheduler implements Scheduler {

        private int taskId = 0;

        @Override
        public void registered(SchedulerDriver driver, Protos.FrameworkID frameworkId, Protos.MasterInfo masterInfo) {
            log.info("Registered with Master Info {}", masterInfo.getAddress());
        }

        @Override
        public void reregistered(SchedulerDriver driver, Protos.MasterInfo masterInfo) {
            log.info("Re-registered with Master Info {}", masterInfo.getAddress());

        }

        @Override
        public void resourceOffers(SchedulerDriver driver, List<Protos.Offer> offers) {
            log.info("{} Resource Offers received", offers.size());
            for(Protos.Offer offer : offers) {

                double offerCPUs = 0;

                for(Protos.Resource resource : offer.getResourcesList()) {
                    log.info("OFFER resource {} - {}", resource.getName(), resource.getScalar().getValue());
                    if (resource.getName().equalsIgnoreCase("cpus")) {
                        offerCPUs =  resource.getScalar().getValue();
                    }

                }

                if (offerCPUs == 0) {
                    driver.declineOffer(offer.getId());
                    continue;
                }

                Protos.Resource cpuResource = Protos.Resource.newBuilder().setName("cpus").setType(Protos.Value.Type.SCALAR).setScalar(Protos.Value.Scalar.newBuilder().setValue(1)).build();
                Protos.Resource memResource = Protos.Resource.newBuilder().setName("mem").setType(Protos.Value.Type.SCALAR).setScalar(Protos.Value.Scalar.newBuilder().setValue(5)).build();

                Protos.TaskID taskID = Protos.TaskID.newBuilder().setValue("MyTask-" + taskId++).build();

                Protos.TaskInfo taskInfo = Protos.TaskInfo.newBuilder()
                        .addResources(cpuResource)
                        .addResources(memResource)
                        .setName("Task " + taskID.getValue())
                        .setTaskId(taskID)
                        .setSlaveId(offer.getSlaveId())
                        .setCommand(Protos.CommandInfo.newBuilder().setShell(true).setValue("sleep 20"))
                        .build();

                List<Protos.OfferID> offerIds = new ArrayList<>();
                offerIds.add(offer.getId());

                // filters for remaining resources (not used by task allocations)
                Protos.Filters filters = Protos.Filters.newBuilder().setRefuseSeconds(1).build();

                List<Protos.TaskInfo> tasks = new ArrayList<>();
                tasks.add(taskInfo);
                driver.launchTasks(offerIds, tasks, filters);
            }
        }

        @Override
        public void offerRescinded(SchedulerDriver driver, Protos.OfferID offerId) {
            log.info("Offer rescinded {}", offerId.getValue());
        }

        @Override
        public void statusUpdate(SchedulerDriver driver, Protos.TaskStatus status) {
            log.info("Status Update received {} -> {}", status.getTaskId(), status.getState());
        }

        @Override
        public void frameworkMessage(SchedulerDriver driver, Protos.ExecutorID executorId, Protos.SlaveID slaveId, byte[] data) {
            log.info("Framework message received from slave {} - {}", slaveId.getValue(), new String(data));
        }

        @Override
        public void disconnected(SchedulerDriver driver) {
            log.info("Master disconnected {} ");
        }

        @Override
        public void slaveLost(SchedulerDriver driver, Protos.SlaveID slaveId) {
            log.info("Slave Lost {}", slaveId.getValue());
        }

        @Override
        public void executorLost(SchedulerDriver driver, Protos.ExecutorID executorId, Protos.SlaveID slaveId, int status) {
            log.info("Executor Lost {} / {}", executorId.getValue(), slaveId.getValue());
        }

        @Override
        public void error(SchedulerDriver driver, String message) {
            log.info("Error {}", message);
        }
    }

    public static void main(String[] args) throws InterruptedException {

        Protos.FrameworkInfo myFramework = Protos.FrameworkInfo.newBuilder()
                .setName("MyFramework")
                .setCheckpoint(true)
                .setFailoverTimeout(60)
                .setUser("")
                .build();

        MesosSchedulerDriver mesosSchedulerDriver = new MesosSchedulerDriver(
                new MyScheduler(),
                myFramework,
                "192.168.1.143:5050");

        mesosSchedulerDriver.start();

        log.info("Framework started - waiting");
        Thread.sleep(25 * 1000);

        log.info("Framework shutting down");
        mesosSchedulerDriver.stop();
    }
}

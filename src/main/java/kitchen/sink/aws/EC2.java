package kitchen.sink.aws;

import com.amazonaws.services.ec2.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EC2 {
    private static Logger log = LoggerFactory.getLogger(EC2.class);

    public static void main(String[] args) {
        AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().build();

        DescribeInstancesRequest dir = new DescribeInstancesRequest();
//        dir.setInstanceIds(Arrays.asList("i-00ee9848a9841c2ad")); // m4.4xlarge
        dir.setInstanceIds(Arrays.asList("i-0239e766b14bee263")); // r3.8xlarge

        DescribeInstancesResult result = ec2.describeInstances(dir);
        List<Instance> instances = result.getReservations().get(0).getInstances();
        for (Instance inst : instances) {
            log.info("Instance EBS Optimized {}, ENA supported {}, Hypervisor {}", inst.getEbsOptimized(), inst.getEnaSupport(), inst.getHypervisor());

            List<String> ebsVolumeIds = new ArrayList<>();
            List<InstanceBlockDeviceMapping> blockDeviceMappings = inst.getBlockDeviceMappings();
            for (InstanceBlockDeviceMapping bdm : blockDeviceMappings) {
                log.info("Device {} -- {}", bdm.getDeviceName(), bdm.getEbs());
                ebsVolumeIds.add(bdm.getEbs().getVolumeId());
            }

            if (!ebsVolumeIds.isEmpty()) {

                DescribeVolumesRequest describeVolumesRequest = new DescribeVolumesRequest();
                describeVolumesRequest.setVolumeIds(ebsVolumeIds);
                DescribeVolumesResult describeVolumesResult = ec2.describeVolumes(describeVolumesRequest);

                List<Volume> volumes = describeVolumesResult.getVolumes();
                for (Volume v : volumes) {
                    log.info("Volume {}", v);
                }
            }

            log.info("Inst root device type {} - name {}", inst.getRootDeviceType(), inst.getRootDeviceName());

//            log.info("Whole Instance {}", inst);

        }
        log.info("Total instances {}", instances.size());

    }
}

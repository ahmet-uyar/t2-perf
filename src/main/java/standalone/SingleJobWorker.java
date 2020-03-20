package standalone;

import edu.iu.dsc.tws.api.JobConfig;
import edu.iu.dsc.tws.api.Twister2Job;
import edu.iu.dsc.tws.api.config.Config;
import edu.iu.dsc.tws.api.config.Context;
import edu.iu.dsc.tws.api.resource.*;
import edu.iu.dsc.tws.proto.jobmaster.JobMasterAPI;
import edu.iu.dsc.tws.rsched.core.ResourceAllocator;
import edu.iu.dsc.tws.rsched.core.WorkerRuntime;
import edu.iu.dsc.tws.rsched.job.Twister2Submitter;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A simple worker
 * logs delays for starting and allJoined
 * logs delays as severe, since we will run log level at WARNING to minimize log messages
 */
public class SingleJobWorker implements IWorker, IAllJoinedListener {

  private static final Logger LOG = Logger.getLogger(SingleJobWorker.class.getName());

  private List<JobMasterAPI.WorkerInfo> workerList;
  private Object waitObject = new Object();
  private long jobSubmitTime;
  private int workerID;

  @Override
  public void execute(Config config, int wID,
                      IWorkerController workerController,
                      IPersistentVolume persistentVolume,
                      IVolatileVolume volatileVolume) {

    this.workerID = wID;
    jobSubmitTime = config.getLongValue("JOB_SUBMIT_TIME", -1);
    LOG.info("jobSubmitTime: " + jobSubmitTime);
    long workerStartTime = System.currentTimeMillis();
    LOG.info("timestamp workerStart: " + workerStartTime);
    LOG.severe("workerStartDelay: " + wID + " " + (workerStartTime - jobSubmitTime));

    boolean added = WorkerRuntime.addAllJoinedListener(this);
    if (!added) {
      LOG.warning(wID + " Can not register IAllJoinedListener.");
      return;
    }

    // lets wait for all workers to join the job
    if (workerList == null) {
      waitAllWorkersToJoin();
    }

    LOG.severe(wID + " All workers joined. Number of joined workers: " + workerList.size());
  }

  private List<Integer> getIDs(List<JobMasterAPI.WorkerInfo> workers) {
    return workers.stream()
        .map(wi -> wi.getWorkerID())
        .sorted()
        .collect(Collectors.toList());
  }

  /**
   * wait for all workers to join the job
   * this can be used for waiting initial worker joins or joins after scaling up the job
   */
  private void waitAllWorkersToJoin() {
    synchronized (waitObject) {
      try {
        LOG.info(workerID + " Waiting for all workers to join the job... ");
        waitObject.wait();
      } catch (InterruptedException e) {
        e.printStackTrace();
        return;
      }
    }
  }

  @Override
  public void allWorkersJoined(List<JobMasterAPI.WorkerInfo> workers) {
    workerList = workers;
    long allJoinedTime = System.currentTimeMillis();
    LOG.info("timestamp allWorkersJoined: " + allJoinedTime);
    LOG.severe("allJoinedDelay: " + workerID + " " + (allJoinedTime - jobSubmitTime));

    synchronized (waitObject) {
      waitObject.notify();
    }
  }

  private void waitAndComplete() {

    long duration = 6000;
    try {
      LOG.info("Sleeping " + duration + " seconds. Will complete after that.");
      Thread.sleep(duration * 1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    // lets take number of workers as an command line argument
    String jobID = "j1";
    int numberOfWorkers = 4;
    int waitConstant = 1;

    if (args.length == 3) {
      jobID = args[0];
      numberOfWorkers = Integer.valueOf(args[1]);
      waitConstant = Integer.valueOf(args[2]);
    }

    // first load the configurations from command line and config files
    Config config = ResourceAllocator.loadConfig(new HashMap<>());

    // lets put a configuration here
    JobConfig jobConfig = new JobConfig();
    jobConfig.put("JOB_SUBMIT_TIME", System.currentTimeMillis() + "");
    jobConfig.put("WAIT_CONSTANT", waitConstant);

    config = Config.newBuilder().putAll(config)
        .put(Context.JOB_ID, jobID)
        .build();

    Twister2Job twister2Job = Twister2Job.newBuilder()
        .setJobName(jobID)
        .setWorkerClass(SingleJobWorker.class)
        .addComputeResource(1.0, 256, numberOfWorkers)
        .setConfig(jobConfig)
        .build();
    // now submit the job
    Twister2Submitter.submitJob(twister2Job, config);
  }

}

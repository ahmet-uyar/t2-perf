package t2;

import edu.iu.dsc.tws.api.JobConfig;
import edu.iu.dsc.tws.api.Twister2Job;
import edu.iu.dsc.tws.api.config.Config;
import edu.iu.dsc.tws.api.scheduler.SchedulerContext;

import java.util.HashMap;

import edu.iu.dsc.tws.rsched.core.ResourceAllocator;
import edu.iu.dsc.tws.rsched.job.Twister2Submitter;

public final class T2JobForMulti {

  @SuppressWarnings("unchecked")
  public static void main(String[] args) {

    if (args.length != 2) {
      System.out.println("You must provide jobID and numberOfWorkers as parameters.");
      return;
    }

    String jID = args[0];
    int numberOfWorkers = Integer.parseInt(args[1]);

    //    LoggingHelper.setupLogging(null, "logs", "client");
    long ts = System.currentTimeMillis();
    System.out.println("Job submission time: " + ts);

    // first load the configurations from command line and config files
    Config config = ResourceAllocator.loadConfig(new HashMap<>());
    System.out.println("read config values: " + config.size());

    config = Config.newBuilder().putAll(config)
        .put("JOB_SUBMIT_TIME", ts + "")
        .put(SchedulerContext.JOB_ID, jID)
        .build();

    // lets put a configuration here
    JobConfig jobConfig = new JobConfig();
    jobConfig.put("hello-key", "Twister2-Hello");

    Twister2Job twister2Job = Twister2Job.newBuilder()
        .setJobName("t2j")
        .setWorkerClass(SimpleWorker.class)
        .addComputeResource(1.0, 256, numberOfWorkers)
        .setConfig(jobConfig)
        .build();
    // now submit the job
    Twister2Submitter.submitJob(twister2Job, config);

//    submitJob(config);
  }

  /**
   * submit the job
   */
  public static void submitJob(Config config) {

    // build JobConfig
    HashMap<String, Object> configurations = new HashMap<>();
    configurations.put(SchedulerContext.THREADS_PER_WORKER, 8);

    JobConfig jobConfig = new JobConfig();
    jobConfig.putAll(configurations);

    // It gets: job-name, worker-class and ComputeResource list from that file
    Twister2Job twister2Job = Twister2Job.loadTwister2Job(config, jobConfig);

    // now submit the job
    Twister2Submitter.submitJob(twister2Job, config);
  }
}

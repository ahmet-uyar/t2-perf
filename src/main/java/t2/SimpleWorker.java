package t2;

import edu.iu.dsc.tws.api.config.Config;
import edu.iu.dsc.tws.proto.jobmaster.JobMasterAPI;
import edu.iu.dsc.tws.rsched.core.WorkerRuntime;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.iu.dsc.tws.api.resource.IAllJoinedListener;
import edu.iu.dsc.tws.api.resource.IPersistentVolume;
import edu.iu.dsc.tws.api.resource.IVolatileVolume;
import edu.iu.dsc.tws.api.resource.IWorker;
import edu.iu.dsc.tws.api.resource.IWorkerController;

/**
 * This is a Hello World example of Twister2. This is the most basic functionality of Twister2,
 * where it spawns set of parallel workers.
 */
public class SimpleWorker implements IWorker, IAllJoinedListener {

  private static final Logger LOG = Logger.getLogger(SimpleWorker.class.getName());

  private List<JobMasterAPI.WorkerInfo> workerList;
  private Object waitObject = new Object();

  @Override
  public void execute(Config config, int workerID,
                      IWorkerController workerController,
                      IPersistentVolume persistentVolume,
                      IVolatileVolume volatileVolume) {

    LOG.info("timestamp workerStart: " + System.currentTimeMillis());

    boolean added = WorkerRuntime.addAllJoinedListener(this);
    if (!added) {
      LOG.warning("Can not register IAllJoinedListener.");
      waitAndComplete();
    }

    // lets wait for all workers to join the job
    if (workerList == null) {
      waitAllWorkersToJoin();
    }

    LOG.info("All workers joined. Worker IDs: " + getIDs(workerList));

    waitAndComplete();
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
        LOG.info("Waiting for all workers to join the job... ");
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
    LOG.info("timestamp allWorkersJoined: " + System.currentTimeMillis());

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
}

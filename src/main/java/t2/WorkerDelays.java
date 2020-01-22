package t2;

public class WorkerDelays {
  int index;
  long podStart;
  long workerStart;
  long allJoined;

  public WorkerDelays(int index, long podStart, long workerStart, long allJoined) {
    this.index = index;
    this.podStart = podStart;
    this.workerStart = workerStart;
    this.allJoined = allJoined;
  }

  @Override
  public String toString() {
    return index + "\t" + podStart + "\t" + workerStart + "\t" + allJoined;
  }
}

import java.util.PriorityQueue;
import java.util.Random;

public class RandomGenerator {

  int numNode, lambda, maxTime;
  Random random;

  public RandomGenerator(int numNode, int lambda, int maxTime) {

    random = new Random();
    this.numNode = numNode;
    this.lambda = lambda;
    this.maxTime = maxTime;
  }

  public void generate(PriorityQueue<Packet> eventQueue, PriorityQueue<Packet> eventQueue2) {

    for(int i = 0; i < numNode; i++) { // each nodel
      int numPacket = poisson(lambda);

      for(int j = 0; j < numPacket; j++) {
        int generated = random.nextInt(maxTime);
        eventQueue.add(new Packet(i, generated, generated));
        eventQueue2.add(new Packet(i, generated, generated));
      }
    }
  }

  public int poisson(int lambda){
    int k=0;
    double L = Math.exp(-lambda);
    double p=1;

    do {
      ++k;
      p *= random.nextFloat();
    } while (p > L);

    return --k;
  }
}

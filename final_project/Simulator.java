import java.util.Scanner;
import java.util.PriorityQueue;
import java.util.Comparator;

public class Simulator {

  public static int numNode = 100;
  public static int lambda = 100;
  public static int maxTime = 100000;

  public static void main(String args[]) {

    Scanner scanner = new Scanner(System.in);

    Comparator<Packet> comparator = new PacketComparator();
    PriorityQueue<Packet> eventQueue = new PriorityQueue<Packet>(10, comparator);
    PriorityQueue<Packet> eventQueue2 = new PriorityQueue<Packet>(10, comparator);
    RandomGenerator randomGenerator = new RandomGenerator(numNode, lambda, maxTime);
    randomGenerator.generate(eventQueue, eventQueue2);

    CD_simulator cd_simulator = new CD_simulator(numNode, eventQueue);
    cd_simulator.run();
    cd_simulator.printResult(lambda, maxTime);

    System.out.println("Press enter to start CSMA/CA simulation");
    scanner.nextLine();

    CA_simulator ca_simulator = new CA_simulator(numNode, eventQueue2);
    ca_simulator.run();
    ca_simulator.printResult(lambda, maxTime);




  }

  public void getInput() {
    Scanner scanner = new Scanner(System.in);

    System.out.println("Enter number of nodes : ");
    numNode = Integer.parseInt(scanner.nextLine());

    System.out.println("Enter Lamda : ");
    lambda = Integer.parseInt(scanner.nextLine());
  }
}

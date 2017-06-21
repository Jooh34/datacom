import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class CD_simulator {

  static final int TIME_T = 82;
  Random random;

  int numNode;
  PriorityQueue<Packet> eventQueue;
  ArrayList<Node> nodeList;

  int time;
  Packet media;

  int transmittedPacket;
  long stackDelay;
  int collisionTime;

  public CD_simulator(int numNode, PriorityQueue<Packet> eventQueue) {

    random = new Random();

    this.numNode = numNode;
    this.eventQueue = eventQueue;
    media = null;
    transmittedPacket = 0;
    stackDelay = 0;
    collisionTime = 0;

    nodeList = new ArrayList<Node>(numNode);
    for(int i=0; i<numNode; i++) {
      nodeList.add(new Node(i));
    }

  }

  public void run() {

    while (!eventQueue.isEmpty()) {
      Packet packet = eventQueue.poll();
      time = packet.eventTime;

      if(packet.eventTime == packet.generated) { // generated now
        System.out.println("Time[" + time*10 + "us] : " + packet.owner + "번째 노드가 packet을 생성하였습니다.");
        Node owner = nodeList.get(packet.owner);

        if(!owner.packets.isEmpty()) { // Node has untransmitted packet
          System.out.println("Time[" + time*10 + "us] : 패킷이 " + packet.owner + "번째 노드의 buffer에 쌓입니다.");
          owner.packets.add(packet);
        }

        else {
          owner.packets.add(packet);
          sensing(packet);
        }
      }

      else if(packet.arrive) { // completely transmitted
        arrive(packet);
      }

      else {
        sensing(packet);
      }
    }
  }

  public void arrive(Packet packet) {

    System.out.println("Time[" + time*10 + "us] : " + packet.owner + "번째 노드가 보낸 패킷이 전송을 성공하였습니다.");
    transmittedPacket++;
    stackDelay = stackDelay + (time-packet.generated);
    Node owner = nodeList.get(packet.owner);
    owner.packets.poll(); // remove packet out of buffer
    media = null;
    if(!owner.packets.isEmpty()) { // transmit new packet on buffer
      Packet new_packet = owner.packets.poll();
      new_packet.eventTime = time;
      eventQueue.add(new_packet);
    }
  }

  public void sensing(Packet packet) {

    if(media != null) { // media busy
      packet.eventTime = packet.eventTime + 1;
      eventQueue.add(packet);

      System.out.println("Time[" + time*10 + "us] : " + packet.owner + "번째 노드가 idle을 대기중입니다.");
    }

    else { // media == null
      LinkedList<Packet> collisionList = new LinkedList<Packet>();

      while(!eventQueue.isEmpty()) { // check collision packet
        if(eventQueue.peek().eventTime == time) {
          collisionList.add(eventQueue.poll());
        }
        else {
          break;
        }
      }

      if(collisionList.isEmpty()) { // No collision
        media = packet;
        packet.eventTime = packet.eventTime + TIME_T;
        packet.arrive = true;
        eventQueue.add(packet);
        System.out.println("Time[" + time*10 + "us] : " + packet.owner + "번째 노드가 전송을 시작하였습니다.");
        return;
      }
      else { // collision
        collisionTime++;
        packet.collision++;
        int backoff = waitTime(packet.collision);
        packet.eventTime = packet.eventTime + backoff;
        eventQueue.add(packet);
        System.out.println("Time[" + time*10 + "us] : " + packet.owner + "번째 노드가 충돌했습니다. 앞으로 back-off Time[" + backoff + "]을 대기합니다.");

        while(!collisionList.isEmpty()) {
          Packet c_packet = collisionList.poll();
          c_packet.collision++;
          backoff = waitTime(c_packet.collision);
          c_packet.eventTime = c_packet.eventTime + backoff;
          eventQueue.add(c_packet);
          System.out.println("Time[" + time*10 + "us] : " + c_packet.owner + "번째 노드가 충돌했습니다. 앞으로 back-off Time[" + backoff + "]을 대기합니다.");
        }
      }
    }
  }

  public int waitTime(int k) {
    k = (k > 10) ? 10 : k;
    int R = random.nextInt((int)Math.pow(2,k)-1) + 1;
    return 10*R;
  }

  public void printResult(int lambda, int maxTime) {
    float throughput = (float)transmittedPacket/(time/100000);
    float meanPacketDelay = (float)stackDelay/transmittedPacket;
    float collisionProbability = (float)collisionTime/time;

    System.out.println("------------------------------------------------");
    System.out.println("1-persistent + Collision Detection");
    System.out.println();
    System.out.println("number of nodes :" + nodeList.size());
    System.out.println("lambda : " + lambda);
    System.out.println("max-time (generating packet) : " + maxTime*10 + "us");
    System.out.println("finish-time : " + time*10 + "us");
    System.out.println();
    System.out.println("Throughput : " + throughput + " packets/sec");
    System.out.println("MeanPacketDelay : " + meanPacketDelay*10 + " us");
    System.out.println("CollisionProbability : " + collisionProbability + " %");
    System.out.println("------------------------------------------------");
  }
}

import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class CA_simulator {

  static final int TIME_T = 82;
  static final int SIFS = 1;
  static final int DIFS = 5;
  static final int TIMESLOT = 2;

  static final int MINCW = 15;

  Random random;

  int numNode;
  PriorityQueue<Packet> eventQueue;
  ArrayList<Node> nodeList;

  int time;
  Packet media;

  int transmittedPacket;
  long stackDelay;
  int collisionTime;

  public CA_simulator(int numNode, PriorityQueue<Packet> eventQueue) {

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

      else if(packet.transmitting) {
        media = null;
        packet.transmitting = false;
        packet.isAck = true;
        packet.eventTime = packet.eventTime + SIFS;
        eventQueue.add(packet);
        if(!packet.isCollided) System.out.println("Time[" + time*10 + "us] : " + packet.owner + "번째 노드가 보낸 패킷 수신 완료. SIFS 대기 중.");
      }

      else if(packet.isAck) {
        if(media != null) {
          collisionTime++;
          media.isCollided = true;
          packet.isCollided = true;
          System.out.println("Time[" + time*10 + "us] : " + media.owner + "번째 노드가 보낸 패킷이 충돌이 일어났습니다.");
          System.out.println("Time[" + time*10 + "us] : " + packet.owner + "번째 노드가 보낸 패킷이 충돌이 일어났습니다.");
        }
        media = packet;
        packet.isAck = false;
        packet.arrive = true;
        packet.eventTime = packet.eventTime + TIME_T;
        eventQueue.add(packet);
        System.out.println("Time[" + time*10 + "us] : " + media.owner + "번째 노드가 보낸 패킷의 ACK를 돌려보냅니다.");
      }

      else if(packet.arrive) { // completely transmitted
        arrive(packet);
      }

      else {
        if(packet.isCollided && (packet.expectedArriveTime == time)) {
          packet.isCollided = false;
          packet.transmitting = false;
          packet.isAck = false;
          packet.arrive = false;
          packet.IFS_done = false;
          packet.initialIdle = true;
          packet.eventTime = time;
          packet.CW = 2*packet.CW + 1;
          System.out.println("Time[" + time*10 + "us] : " + media.owner + "번째 노드가 ACK가 오지않아 재전송합니다.");
        }
        sensing(packet);
      }

    //   try {
    //     // thread to sleep for 1000 milliseconds
    //     Thread.sleep(300);
    //  } catch (Exception e) {
    //     System.out.println(e);
    //  }
    }
  }

  public void arrive(Packet packet) {

    media = null;
    System.out.println("Time[" + time*10 + "us] : " + packet.owner + "번째 노드가 보낸 패킷이 전송을 성공하였습니다.");
    transmittedPacket++;
    stackDelay = stackDelay + (time-packet.generated);
    Node owner = nodeList.get(packet.owner);
    owner.packets.poll(); // remove packet out of buffer
    if(!owner.packets.isEmpty()) { // transmit new packet on buffer
      Packet new_packet = owner.packets.poll();
      new_packet.eventTime = time;
      eventQueue.add(new_packet);
    }
  }

  public void transmit(Packet packet) {
    if(media != null) {
      collisionTime++;
      media.isCollided = true;
      packet.isCollided = true;
      System.out.println("Time[" + time*10 + "us] : " + media.owner + "번째 노드가 보낸 패킷이 충돌이 일어났습니다.");
      System.out.println("Time[" + time*10 + "us] : " + packet.owner + "번째 노드가 보낸 패킷이 충돌이 일어났습니다.");
    }
    media = packet;
    packet.eventTime = packet.eventTime + TIME_T;
    packet.expectedArriveTime = time + 2 * TIME_T + SIFS;
    packet.transmitting = true;

    eventQueue.add(packet);
    System.out.println("Time[" + time*10 + "us] : " + packet.owner + "번째 노드가 전송을 시작하였습니다.");
  }

  public void sensing(Packet packet) {
    if(media == null) {

      if(packet.initialIdle && packet.IFS_done) transmit(packet);

      else if(!packet.IFS_done) { // wait DIFS
        packet.eventTime = packet.eventTime + DIFS;
        packet.IFS_done = true;
        eventQueue.add(packet);
        System.out.println("Time[" + time*10 + "us] : " + packet.owner + "번째 노드가 DIFS를 기다립니다.");
      }

      else if(packet.IFS_done) { // wait backoff-Time
        if(packet.remain_CW == 0) { //
          packet.remain_CW = -1;
          transmit(packet);
        }
        else if(packet.remain_CW == -1) { // not init yet
          packet.remain_CW = random.nextInt(packet.CW+1);
          packet.eventTime = packet.eventTime + TIMESLOT;
          eventQueue.add(packet);
          System.out.println("Time[" + time*10 + "us] : " + packet.owner + "번째 노드가 back-off time[" + TIMESLOT*packet.remain_CW + "]을 기다립니다.");
        }
        else { // count down
          packet.remain_CW = packet.remain_CW - 1;
          packet.eventTime = packet.eventTime + TIMESLOT;
          eventQueue.add(packet);
          System.out.println("Time[" + time*10 + "us] : " + packet.owner + "번째 노드가 back-off time[" + TIMESLOT*packet.remain_CW + "]을 기다립니다.");
        }
      }
    }

    else { // media busy
      packet.initialIdle = false;
      packet.eventTime = packet.eventTime + 1;
      eventQueue.add(packet);

      System.out.println("Time[" + time*10 + "us] : " + packet.owner + "번째 노드가 idle을 대기중입니다.");
    }
  }

  public void printResult(int lambda, int maxTime) {
    float throughput = (float)transmittedPacket/(time/100000);
    float meanPacketDelay = (float)stackDelay/transmittedPacket;
    float collisionProbability = (float)collisionTime/time;

    System.out.println("------------------------------------------------");
    System.out.println("IEEE 802.11 CSMA-CA DCF ");
    System.out.println();
    System.out.println("number of nodes :" + nodeList.size());
    System.out.println("lambda : " + lambda);
    System.out.println("max-time (generating packet) : " + maxTime*10 + "us");
    System.out.println("finish-time : " + time*10 + "us");
    System.out.println();
    System.out.println("Throughput : " + throughput + " packet/sec");
    System.out.println("MeanPacketDelay : " + meanPacketDelay*10 + " us");
    System.out.println("CollisionProbability : " + collisionProbability + " %");
    System.out.println("------------------------------------------------");
  }
}

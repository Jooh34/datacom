public class Packet {

  static final int MINCW = 15;

  int generated;
  int owner;
  int eventTime;
  int collision;
  boolean arrive;

  boolean initialIdle;
  boolean IFS_done;
  boolean transmitting;
  boolean isAck;
  boolean isCollided;
  int CW;
  int remain_CW;
  int expectedArriveTime;


  public Packet(int owner, int generated, int eventTime) {
    this.owner = owner;
    this.generated = generated;
    this.eventTime = eventTime;
    arrive = false;
    collision = 0;

    // for CA
    initialIdle = true;
    IFS_done = false;
    transmitting = false;
    isAck = false;
    isCollided = false;


    CW = MINCW;
    remain_CW = -1;
  }
}

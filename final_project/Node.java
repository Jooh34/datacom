import java.util.Queue;
import java.util.LinkedList;

public class Node {

  int index;
  Queue<Packet> packets;

  public Node(int index) {
    this.index = index;
    packets =new LinkedList<Packet>();
  }
}

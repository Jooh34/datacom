import java.util.Comparator;

public class PacketComparator implements Comparator<Packet>
{
  @Override
  public int compare(Packet x, Packet y)
  {
      if (x.eventTime < y.eventTime)
      {
          return -1;
      }
      if (x.eventTime > y.eventTime)
      {
          return 1;
      }
      return 0;
  }
}

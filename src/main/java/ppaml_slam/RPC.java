package ppaml_slam;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.util.ArrayList;

import org.zeromq.ZMQ;

public class RPC {

  /**
   * Coordinates of a single obstacle.
   */
  public static class Obstacle {
    public double x;
    public double y;
    public double r;

    public Obstacle() {
      this(0, 0, 0);
    }

    public Obstacle(double x, double y, double r) {
      this.x = x;
      this.y = y;
      this.r = r;
    }

    public String toString() {
      return "(" + x + ", " + y + ", " + r + ")";
    }
  };

  static public double defaultLaserMaxRange() {
    return 10.0;
  }

  static public double[] defaultLaserAngles() {
    double[] laserAngles = new double[361];
    for (int i = 0; i < 361; i++) {
      laserAngles[i] = (-90 + i * 0.5) * Math.PI / 180;
    }
    return laserAngles;
  }

  public ArrayList<Obstacle> extractObstacles(double[] obs_lasers) {
    // Construct and send message.
    byte[] request = new byte[1 + 8 * obs_lasers.length];
    request[0] = 0x1;
    DoubleBuffer buffer = ByteBuffer.wrap(request, 1, 8 * obs_lasers.length)
        .order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer();
    buffer.put(obs_lasers);
    socket.send(request, 0);

    // Parse reply.
    byte[] response = socket.recv(0);
    long numObst = ByteBuffer.wrap(response, 0, 8)
        .order(ByteOrder.LITTLE_ENDIAN).asLongBuffer().get();
    assert (numObst == (response.length - 8) / 8 / 3);
    buffer = ByteBuffer.wrap(response, 8, response.length - 8)
        .order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer();
    ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
    for (int i = 0; i < numObst; i++) {
      obstacles.add(new Obstacle(buffer.get(), buffer.get(), buffer.get()));
    }
    return obstacles;
  }

  public RPC() {
    // Connect to socket and check that server is running.
    context = ZMQ.context(1);
    socket = context.socket(ZMQ.REQ);
    socket.connect("tcp://localhost:6666");
    double[] dummy = new double[defaultLaserAngles().length];
    extractObstacles(dummy);
    System.out.println("Connected to RPC server.");
  }

  public void close() {
    socket.close();
    context.term();
  }

  private ZMQ.Context context;
  private ZMQ.Socket socket;
};

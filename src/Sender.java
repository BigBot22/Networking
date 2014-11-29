import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Enumeration;

public class Sender implements Runnable {
    private ShareRutine share;

    private final int port = 7777;
    private final long SEND_INTERVAL = 5000;
    private byte[] ip;

    byte[] getIpAddress() {
        try {
            System.out.println("Host addr: " + InetAddress.getLocalHost().getHostAddress());  // often returns "127.0.0.1"
            Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
            for (; n.hasMoreElements();)
            {
                NetworkInterface e = n.nextElement();
                System.out.println("Interface: " + e.getName());
                Enumeration<InetAddress> a = e.getInetAddresses();
                for (; a.hasMoreElements();)
                {
                    InetAddress addr = a.nextElement();
                    System.out.println("  " + addr.getHostAddress() + " " + Arrays.toString(addr.getAddress()));
                    if (e.getName().equals("en0"))
                        if (addr instanceof Inet4Address) {
                            Inet4Address cur = (Inet4Address) addr;
                            return cur.getAddress();
                        }
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Sender(ShareRutine share) {
        this.share = share;
        ip = getIpAddress();
    }

    DatagramPacket buildAnnouncePacket() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.write(ip);
        dos.writeInt(share.countFiles());
        dos.writeLong(share.lastChangeTimestamp());
        dos.write((share.shareName + '\000').getBytes());
        dos.flush();
        byte[] bytes = bos.toByteArray();
        DatagramPacket packet = new DatagramPacket(bytes,
                                bytes.length,
                                InetAddress.getByName("255.255.255.255"), port);
        return packet;
    }

    @Override
    public void run() {
        while (true) {
            try {
                DatagramSocket socket = new DatagramSocket(null);
                socket.send(buildAnnouncePacket());
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    Thread.sleep(SEND_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
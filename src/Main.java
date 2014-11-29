import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    static int port = 7777;

    public static void main(String[] arg) {

        ShareRutine shareRutine = new ShareRutine();

        Server server = new Server(shareRutine);
        new Thread(server).start();

        Sender sender = new Sender(shareRutine);
        new Thread(sender).start();

        Receiver announceReceiver = new Receiver();
        new Thread(announceReceiver).start();

        Gui gui = new Gui(announceReceiver);
        new Thread(gui).start();


        Scanner in = new Scanner(System.in);

        while (true) {
            try {
                String s = in.nextLine();
                String[] args = s.split(" ");
                String command = args[0];

                if (command.equals("list")) {
                    String ip = args[1];
                    Socket socket = new Socket(ip, port);
                    OutputStream os = socket.getOutputStream();
                    os.write(Utils.typeBytes(Utils.LIST_REQUEST));
                    InputStream is = socket.getInputStream();
                    int commandResultCode = is.read();
                    if (commandResultCode == Utils.LIST_RESULT) {
                        int countFiles = Utils.readInt(is);
                        for (int i = 0; i < countFiles; ++i) {
                            System.out.println(Utils.MD5ToString(Utils.getMD5FromStream(is)) + " " + Utils.readNullTermString(is));
                        }
                    }
                    socket.close();
                } else if (command.equals("get")) {
                    String ip = args[1];
                    String filename = args[2];
                    String filenameToSend = filename + '\000';

                    Socket socket = new Socket(ip, port);

                    OutputStream outputStream = socket.getOutputStream();

                    outputStream.write(Utils.typeBytes(Utils.GET_REQUEST));
                    outputStream.write(filenameToSend.getBytes());

                    InputStream inputStream = socket.getInputStream();
                    int commandResultCode = inputStream.read();
                    if (commandResultCode == Utils.GET_RESULT) {
                        int fileSize = (int)Utils.readLong(inputStream);
                        byte[] md5 = Utils.getMD5FromStream(inputStream);
                        byte[] fileBuffer = new byte[fileSize];
                        inputStream.read(fileBuffer);
                        shareRutine.writeFile(filename, fileBuffer);
                    }
                    socket.close();

                } else if (command.equals("put")) {
                    String ip = args[1];
                    String filename = args[2];
                    Socket socket = new Socket(ip, port);
                    OutputStream os = socket.getOutputStream();
                    os.write(Utils.typeBytes(Utils.PUT_REQUEST));
                    byte[] fileBytes = shareRutine.readFile(filename);
                    os.write((filename + '\000').getBytes());
                    Utils.writeLong(os, fileBytes.length);
                    os.write(fileBytes);
                    socket.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

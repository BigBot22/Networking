import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable {
    private ShareRutine share;
    private final int port = 7777;

    public Server(ShareRutine share) {
        this.share = share;
    }

    class RequestHandler implements Runnable {
        Socket socket;

        RequestHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream input = socket.getInputStream();
                OutputStream output = socket.getOutputStream();
                int commandCode = input.read();
                switch (commandCode) {
                    case Utils.LIST_REQUEST: {
                        output.write(Utils.typeBytes(Utils.LIST_RESULT));

                        ArrayList<ShareRutine.FileInfo> files = share.getFiles();
                        Utils.writeInt(output, files.size());
                        for (ShareRutine.FileInfo info : files) {
                            output.write(info.md5);
                            output.write((info.filename + '\000').getBytes());
                        }

                        break;
                    }
                    case Utils.GET_REQUEST: {
                        String filename = Utils.readNullTermString(input);
                        output.write(Utils.typeBytes(Utils.GET_RESULT));

                        byte[] bytes = share.readFile(filename);
                        Utils.writeLong(output, (long)bytes.length);
                        output.write(share.getMD5ByFilename(filename));
                        output.write(bytes);

                        break;
                    }
                    case Utils.PUT_REQUEST: {
                        String filename = Utils.readNullTermString(input);
                        long fileSize = Utils.readLong(input);
                        byte[] fileBuffer = new byte[(int) fileSize];
                        input.read(fileBuffer);
                        share.writeFile(filename, fileBuffer);
                        break;
                    }
                    case Utils.LIST_RESULT: {
                        int countFiles = Utils.readInt(input);
                        for (int i = 0; i < countFiles; ++i) {
                            System.out.println(Utils.getMD5FromStream(input) + " " + Utils.readNullTermString(input));
                        }
                        break;
                    }
                }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(port);
            System.out.println("Server started");
            while (true) {
                Thread newRequest = new Thread(new RequestHandler(server.accept()));
                newRequest.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

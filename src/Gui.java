import javax.swing.*;
import java.util.Iterator;

public class Gui implements Runnable {
    private final int REFRESH_TIME = 300;
    JFrame frame;
    Receiver receiver;

    public Gui(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void run() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Peers list");
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        JTextArea textArea = new JTextArea();
        frame.add(textArea);
        textArea.setEditable(false);
        while (true) {
            synchronized (receiver.peers) {
                String text = "";

                for (Iterator<Receiver.PeerInfo> infoIt = receiver.peers.iterator(); infoIt.hasNext();) {
                    if (text.length() != 0) {
                        text += "\n";
                    }
                    Receiver.PeerInfo peers = infoIt.next();
                    if (System.currentTimeMillis() - 10000 > peers.lastTime) {
                        infoIt.remove();
                        continue;
                    }
                    text += peers.toString();
                }
                text = text + "\nPeers cout:" + receiver.peers.size() + "\n";
                textArea.setText(text);
            }
            try {
                Thread.sleep(REFRESH_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

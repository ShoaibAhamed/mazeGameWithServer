package Client;

import java.io.IOException;
import java.io.ObjectInputStream;

import utils.MyData;

public class MazeClientThread extends Thread {

    private ObjectInputStream in;

    public MazeClientThread(ObjectInputStream in) {
        this.in = in;
    }

    @Override
    public void run() {
        while (true) {
            Object tmp = null;

            try {
                tmp = in.readObject();
            } catch (ClassNotFoundException e) {
                System.out.println("Class not found Exception" + e);
            } catch (IOException e) {
            	System.out.println("IO Exception" + e);
            }

            if (tmp == null) {
                try {
                    Thread.sleep(1);
                    continue;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            MazeClient.mdata = (MyData) tmp;
        }
    }
}

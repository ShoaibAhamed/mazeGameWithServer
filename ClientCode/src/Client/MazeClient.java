package Client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.TrayIcon.MessageType;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import utils.FruitsAndStar;
import utils.MazeCreator;
import utils.MyData;

public class MazeClient {

    static int N = 10;
    static boolean[][] north;
    static boolean[][] east;
    static boolean[][] south;
    static boolean[][] west;
    static boolean[][] visited;
    static int stx = 0;
    static int sty = 0;
    static int tarx = 0;
    static int tary = 0;
    static int score = 0;
    static int time = 0;
    static long stDate;
    static boolean flag = false;
    static MyData mdata;
    static boolean moveflag = false;
    private static int fruit1x = 0;
    private static int fruit1y = 0;
    private static int fruit2x = 0;
    private static int fruit2y = 0;

    private static Socket socket;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private static Client.MazeClientThread receiver;
    private static FruitsAndStar fruitsAndStar1;
    private static FruitsAndStar fruitsAndStar2;
    
    private static void init() {
        MazeCreator.setCanvasSize(120 * 4, 140 * 4);
        MazeCreator.setXscale(0, N + 2);
        MazeCreator.setYscale(0, N + 4);
        MazeCreator.getFrame().setTitle("Maze : Client View ");
    }
    


    // draw the maze
    public static void drawTheMaze() {
        MazeCreator.clear();
        MazeCreator.text(6, 13, "Maze Client : View ");
        MazeCreator.text(3, 12, "Score: " + String.valueOf(score));
        MazeCreator.text(3, 11.5, "Time: " + String.valueOf(time));
        MazeCreator.picture(tarx + 0.5, tary + 0.5, "images/rat.jpg",1,1);//filledCircle(tarx + 0.5, tary + 0.5, 0.375);
        MazeCreator.picture(stx + 0.5, sty + 0.5, "images/cat-head.gif",1,1);
        if(!fruitsAndStar1.isFruitorStarCaptured()){
        	MazeCreator.picture(fruit1x + 0.5, fruit1y + 0.5, "images/banana.jpg",1,1);
        }
        if(!fruitsAndStar2.isFruitorStarCaptured()){
        	MazeCreator.picture(fruit2x + 0.5, fruit2y + 0.5, "images/mango.jpg",1,1);
        }
        
        MazeCreator.setPenColor(Color.BLACK);
        for (int x = 1; x <= N; x++) {
            for (int y = 1; y <= N; y++) {
                if (south[x][y]) {
                    MazeCreator.line(x, y, x + 1, y);
                }
                if (north[x][y]) {
                    MazeCreator.line(x, y + 1, x + 1, y + 1);
                }
                if (west[x][y]) {
                    MazeCreator.line(x, y, x, y + 1);
                }
                if (east[x][y]) {
                    MazeCreator.line(x + 1, y, x + 1, y + 1);
                }
            }
        }
    }
    

    public static void selFirstTime() {
        if (!flag) {
            stDate = System.currentTimeMillis();
        }
        flag = true;
    }

    public static void main(String[] args) {
        try {
            socket = new Socket("127.0.0.1", 9090);
            System.out.println("ClientOK");
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            receiver = new MazeClientThread(in);
            receiver.start();
            while (true) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (mdata != null) {
                    south = mdata.getsouth();
                    west = mdata.getwest();
                    east = mdata.geteast();
                    north = mdata.getnorth();
                    visited = mdata.getvisited();
                    stx = mdata.getstx();
                    sty = mdata.getsty();
                    tarx = mdata.gettarx();
                    tary = mdata.gettary();
                    score = mdata.getScore();
                    time = mdata.getTime();
                    fruit1x = mdata.getFruit1x();
                    fruit1y = mdata.getFruit1y();
                    fruit2x = mdata.getFruit2x();
                    fruit2y = mdata.getFruit2y();
                    fruitsAndStar1 = mdata.getFruitsAndStar1();
                    fruitsAndStar2 = mdata.getFruitsAndStar2();
                    init();
                    MazeCreator.show(100);
                    drawTheMaze();
                    
                    while (!MazeCreator.isKeyPressed(KeyEvent.VK_ESCAPE)) {


                        if (MazeCreator.isKeyPressed(KeyEvent.VK_UP)) {
                            selFirstTime();
                            if (!north[stx][sty]) {
                                sty++;
                                score++;
                                moveflag = true;
                            	updateScoreForFruits();
                            }
                        }
                        if (MazeCreator.isKeyPressed(KeyEvent.VK_DOWN)) {
                            selFirstTime();
                            if (!south[stx][sty]) {
                                sty--;
                                score++;
                                moveflag = true;
                                updateScoreForFruits();
                            }
                        }
                        if (MazeCreator.isKeyPressed(KeyEvent.VK_LEFT)) {
                            selFirstTime();
                            if (!west[stx][sty]) {
                                stx--;
                                score++;
                                moveflag = true;
                            	updateScoreForFruits();
                            }
                        }
                        if (MazeCreator.isKeyPressed(KeyEvent.VK_RIGHT)) {
                            selFirstTime();
                            if (!east[stx][sty]) {
                                stx++;
                                score++;
                                moveflag = true;
                            	updateScoreForFruits();
                            }
                        }
                        
                        //second player
                        
                        if (MazeCreator.isKeyPressed(KeyEvent.VK_W)) {
                            selFirstTime();
                            if (!north[stx][sty]) {
                                sty++;
                                score++;
                                moveflag = true;
                            	updateScoreForFruits();
                            }
                        }
                        if (MazeCreator.isKeyPressed(KeyEvent.VK_S)) {
                            selFirstTime();
                            if (!south[stx][sty]) {
                                sty--;
                                score++;
                                moveflag = true;
                                updateScoreForFruits();
                            }
                        }
                        if (MazeCreator.isKeyPressed(KeyEvent.VK_A)) {
                            selFirstTime();
                            if (!west[stx][sty]) {
                                stx--;
                                score++;
                                moveflag = true;
                            	updateScoreForFruits();
                            }
                        }
                        if (MazeCreator.isKeyPressed(KeyEvent.VK_D)) {
                            selFirstTime();
                            if (!east[stx][sty]) {
                                stx++;
                                score++;
                                moveflag = true;
                            	updateScoreForFruits();
                            }
                        }



                        if (flag) {
                            time = (int) ((System.currentTimeMillis() - stDate) / 1000);
                        }
                        if (moveflag) {
                            senddata();
                            moveflag = false;
                        }
                        drawTheMaze();
                        MazeCreator.show(100);
                        if (stx == tarx && sty == tary) {
                            System.out.println("Score: " + score);
                            System.out.println("Game End");
                            ImageIcon icon = createImageIcon("images/youwon.jpg");
							JOptionPane.showMessageDialog(null, "Clent Won the Game.\nScore: "+score+"\nGame End", "Clent Won the Game.", JOptionPane.INFORMATION_MESSAGE, icon );
                            while (true);
                        }
                    }
                }
            }

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // TODO Auto-generated method stub

    }
    
    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String filename) {
        if (filename == null) throw new NullPointerException();

        // to read from file
        ImageIcon icon = new ImageIcon(filename);
        

        // try to read from URL
        if ((icon == null) || (icon.getImageLoadStatus() != MediaTracker.COMPLETE)) {
            try {
                URL url = new URL(filename);
                icon = new ImageIcon(url);
            }
            catch (Exception e) {
                /* not a url */
            }
        }

        // in case file is inside a .jar (classpath relative to StdDraw)
        if ((icon == null) || (icon.getImageLoadStatus() != MediaTracker.COMPLETE)) {
            URL url = MazeCreator.class.getResource(filename);
            if (url != null)
                icon = new ImageIcon(url);
        }

        // in case file is inside a .jar (classpath relative to root of jar)
        if ((icon == null) || (icon.getImageLoadStatus() != MediaTracker.COMPLETE)) {
            URL url = MazeCreator.class.getResource("/" + filename);
            if (url == null) throw new IllegalArgumentException("image " + filename + " not found");
            icon = new ImageIcon(url);
        }
        

         
        Image newimg = icon.getImage().getScaledInstance(160, 160,  java.awt.Image.SCALE_SMOOTH);
        ImageIcon newIcon = new ImageIcon(newimg);


        return newIcon;
    }



	private static void updateScoreForFruits() {
		if(stx == fruit1x && sty== fruit1y && !fruitsAndStar1.isFruitorStarCaptured()){
			score+=10;
			fruitsAndStar1.setFruitorStarCaptured(true);
		}else if(stx == fruit2x && sty== fruit2y && !fruitsAndStar2.isFruitorStarCaptured()){
			score+=10;
			fruitsAndStar2.setFruitorStarCaptured(true);
		}
	}

    private static void senddata() {
        MyData sdata = new MyData();
        boolean[][] north1 = new boolean[12][12];
        boolean[][] east1 = new boolean[12][12];
        boolean[][] south1 = new boolean[12][12];
        boolean[][] west1 = new boolean[12][12];
        boolean[][] visited1 = new boolean[12][12];
        for (int i = 0; i < N + 2; i++) {
            for (int j = 0; j < N + 2; j++) {
                north1[i][j] = north[i][j];
                east1[i][j] = east[i][j];
                south1[i][j] = south[i][j];
                west1[i][j] = west[i][j];
                visited1[i][j] = visited[i][j];
            }
        }
        sdata.setBooleanData(north1, east1, south1, west1, visited1);
        sdata.setScoreTime(score, time);
        sdata.setStEnd(stx, sty, tarx, tary,fruit1x,fruit1y,fruit2x,fruit2y,fruitsAndStar1,fruitsAndStar2);
        try {
            out.writeObject(sdata);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

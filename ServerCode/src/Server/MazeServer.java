package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import utils.FruitsAndStar;
import utils.MazeCreator;
import utils.MyData;

public class MazeServer {

    private static int N = 10;
    private static boolean[][] north;
    private static boolean[][] east;
    private static boolean[][] south;
    private static boolean[][] west;
    private static boolean[][] visited;
    private static int stx = 0;
    private static int sty = 0;
    private static int tarx = 0;
    private static int tary = 0;
    private static int fruit1x = 0;
    private static int fruit1y = 0;
    private static int fruit2x = 0;
    private static int fruit2y = 0;
    private static int score = 0;
    private static int time = 0;
    private static FruitsAndStar fruitsAndStar1;
    private static FruitsAndStar fruitsAndStar2;
    static MyData rdata;
    static boolean rflag = false;

    private static ServerSocket hostServer;
    private static Socket socket;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private static Server.MazeServerThread receiver;
    private static long seed = System.currentTimeMillis();
    static Random random = new Random(seed);
    private static void init() {
        stx = 1;
        sty = 1;
        score = 0;
        tarx = (int) (Math.random() * (N - 1)) + 1;
        tary = (int) (Math.random() * (N - 1)) + 1;
        fruit1x = (int) (Math.random() * (N - 1)) + 1;
        fruit1y = (int) (Math.random() * (N - 1)) + 1;
        fruit2x = (int) (Math.random() * (N - 1)) + 1;
        fruit2y = (int) (Math.random() * (N - 1)) + 1;

        
        while(true){
            if(tarx == fruit1x && tary == fruit1y){
                fruit1x = (int) (Math.random() * (N - 1)) + 1;
                fruit1y = (int) (Math.random() * (N - 1)) + 1;
            }else{
            	break;
            }
        }

        while(true){
            if(tarx == fruit2x && tary == fruit2y){
                fruit2x = (int) (Math.random() * (N - 1)) + 1;
                fruit2y = (int) (Math.random() * (N - 1)) + 1;
            }else{
            	break;
            }
        }
        
        fruitsAndStar1 = new FruitsAndStar(fruit1x, fruit1y);
        fruitsAndStar2 = new FruitsAndStar(fruit2x, fruit2y);
        
        MazeCreator.setCanvasSize(120 * 4, 140 * 4);
        MazeCreator.setXscale(0, N + 2);
        MazeCreator.setYscale(0, N + 4);
        MazeCreator.getFrame().setTitle("Maze : Server View ");

        // initialize border cells as already visited
        visited = new boolean[N + 2][N + 2];
        for (int x = 0; x < N + 2; x++) {
            visited[x][0] = true;
            visited[x][N + 1] = true;
        }
        for (int y = 0; y < N + 2; y++) {
            visited[0][y] = true;
            visited[N + 1][y] = true;
        }

        // initialze all walls as present
        north = new boolean[N + 2][N + 2];
        east = new boolean[N + 2][N + 2];
        south = new boolean[N + 2][N + 2];
        west = new boolean[N + 2][N + 2];
        for (int x = 0; x < N + 2; x++) {
            for (int y = 0; y < N + 2; y++) {
                north[x][y] = true;
                east[x][y] = true;
                south[x][y] = true;
                west[x][y] = true;
            }
        }
        generate(1, 1);
    }

    // generate the maze
    private static void generate(int x, int y) {
        visited[x][y] = true;

        // while there is an unvisited neighbor
        while (!visited[x][y + 1] || !visited[x + 1][y] || !visited[x][y - 1] || !visited[x - 1][y]) {

            // pick random neighbor (could use Knuth's trick instead)
            while (true) {
                double r = random.nextInt(4);
                if (r == 0 && !visited[x][y + 1]) {
                    north[x][y] = false;
                    south[x][y + 1] = false;
                    generate(x, y + 1);
                    break;
                } else if (r == 1 && !visited[x + 1][y]) {
                    east[x][y] = false;
                    west[x + 1][y] = false;
                    generate(x + 1, y);
                    break;
                } else if (r == 2 && !visited[x][y - 1]) {
                    south[x][y] = false;
                    north[x][y - 1] = false;
                    generate(x, y - 1);
                    break;
                } else if (r == 3 && !visited[x - 1][y]) {
                    west[x][y] = false;
                    east[x - 1][y] = false;
                    generate(x - 1, y);
                    break;
                }
            }
        }
    }

    // draw the maze
    public static void draw() {
        MazeCreator.clear();
        MazeCreator.text(6, 13, "Game Server");
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

    public static void main(String[] args) {
        try {
            hostServer = new ServerSocket(9090);
            socket = hostServer.accept();
            System.out.println("Maze Server connection established");
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            receiver = new MazeServerThread(in);
            receiver.start();

            init();
            MazeCreator.show(100);
            draw();
            MazeCreator.show(100);
            senddata();
            while (true) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (rflag) {
                    south = rdata.getsouth();
                    west = rdata.getwest();
                    east = rdata.geteast();
                    north = rdata.getnorth();
                    visited = rdata.getvisited();
                    stx = rdata.getstx();
                    sty = rdata.getsty();
                    tarx = rdata.gettarx();
                    tary = rdata.gettary();
                    score = rdata.getScore();
                    time = rdata.getTime();
                    fruit1x = rdata.getFruit1x();
                    fruit1y = rdata.getFruit1y();
                    fruit2x = rdata.getFruit2x();
                    fruit2y = rdata.getFruit2y();
                    fruitsAndStar1 = rdata.getFruitsAndStar1();
                    fruitsAndStar2 = rdata.getFruitsAndStar2();
                    updateScoreForFruits();
                    draw();
                    MazeCreator.show(100);
                    rflag = false;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
	private static void updateScoreForFruits() {
		if(stx == fruit1x && sty== fruit1y && !fruitsAndStar1.isFruitorStarCaptured()){
			fruitsAndStar1.setFruitorStarCaptured(true);
		}else if(stx == fruit2x && sty== fruit2y && !fruitsAndStar2.isFruitorStarCaptured()){
			fruitsAndStar2.setFruitorStarCaptured(true);
		}
	}

    private static void senddata() {
        // TODO Auto-generated method stub
        MyData sdata = new MyData();
        sdata.setBooleanData(north, east, south, west, visited);
        sdata.setScoreTime(score, time);
        sdata.setStEnd(stx, sty, tarx, tary,fruit1x,fruit1y,fruit2x,fruit2y,fruitsAndStar1,fruitsAndStar2);
        try {
            out.writeObject(sdata);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

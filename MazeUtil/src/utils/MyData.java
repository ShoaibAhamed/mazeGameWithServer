package utils;

import java.io.Serializable;

public class MyData implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private int N = 10;
    private boolean[][] north = new boolean[12][12];
    private boolean[][] east = new boolean[12][12];
    private boolean[][] south = new boolean[12][12];
    private boolean[][] west = new boolean[12][12];
    private boolean[][] visited = new boolean[12][12];
    private int stx = 0;
    private int sty = 0;
    private int tarx = 0;
    private int tary = 0;
    private int fruit1x = 0;
    private int fruit1y = 0;
    private int fruit2x = 0;
    private int fruit2y = 0;
    private int score = 0;
    private int time = 0;
    private FruitsAndStar fruitsAndStar1;
    private FruitsAndStar fruitsAndStar2;

    public MyData() {
    }

    public void setBooleanData(boolean[][] north, boolean[][] east, boolean[][] south, boolean[][] west, boolean[][] visited) {
        for (int i = 0; i < N + 2; i++) {
            for (int j = 0; j < N + 2; j++) {
                this.north[i][j] = north[i][j];
                this.east[i][j] = east[i][j];
                this.south[i][j] = south[i][j];
                this.west[i][j] = west[i][j];
                this.visited[i][j] = visited[i][j];
            }
        }
    }

    public boolean[][] getnorth() {
        return north;
    }

    public boolean[][] geteast() {
        return east;
    }

    public boolean[][] getsouth() {
        return south;
    }

    public boolean[][] getwest() {
        return west;
    }

    public boolean[][] getvisited() {
        return visited;
    }

    public void setStEnd(int stx, int sty, int tarx, int tary, int fruit1x, int fruit1y, int fruit2x, int fruit2y,FruitsAndStar fruitsAndStar1,FruitsAndStar fruitsAndStar2) {
        this.stx = stx;
        this.sty = sty;
        this.tarx = tarx;
        this.tary = tary;
        this.fruit1x = fruit1x;
        this.fruit1y = fruit1y;
        this.fruit2x = fruit2x;
        this.fruit2y = fruit2y;
        this.fruitsAndStar1 = fruitsAndStar1;
        this.fruitsAndStar2 = fruitsAndStar2;
    }

    public int getstx() {
        return stx;
    }

    public int getsty() {
        return sty;
    }

    public int gettarx() {
        return tarx;
    }

    public int gettary() {
        return tary;
    }

    public void setScoreTime(int score, int time) {
        this.score = score;
        this.time = time;
    }

    public int getScore() {
        return score;
    }

    public int getTime() {
        return time;
    }

	public int getFruit1x() {
		return fruit1x;
	}

	public int getFruit1y() {
		return fruit1y;
	}

	public int getFruit2x() {
		return fruit2x;
	}

	public int getFruit2y() {
		return fruit2y;
	}

	public FruitsAndStar getFruitsAndStar1() {
		return fruitsAndStar1;
	}

	public void setFruitsAndStar1(FruitsAndStar fruitsAndStar1) {
		this.fruitsAndStar1 = fruitsAndStar1;
	}

	public FruitsAndStar getFruitsAndStar2() {
		return fruitsAndStar2;
	}

	public void setFruitsAndStar2(FruitsAndStar fruitsAndStar2) {
		this.fruitsAndStar2 = fruitsAndStar2;
	}
}

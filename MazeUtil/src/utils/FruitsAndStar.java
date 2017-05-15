package utils;

import java.io.Serializable;

public class FruitsAndStar implements Serializable{

	private boolean fruitorStarCaptured;
	private int x;
	private int y;
	
	public FruitsAndStar(int x,int y) {
		this.x = x;
		this.y = y;
	}

	public boolean isFruitorStarCaptured() {
		return fruitorStarCaptured;
	}

	public void setFruitorStarCaptured(boolean fruitorStarCaptured) {
		this.fruitorStarCaptured = fruitorStarCaptured;
	}
	
}

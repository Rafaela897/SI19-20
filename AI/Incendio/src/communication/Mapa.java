package communication;

import java.io.Serializable;

import constants.Constants;


public class Mapa implements Serializable{
	
	private int[][] Pos;
	public int StationX;
	public int StationY;
	public int SizeX;
	public int SizeY;
	
	public Mapa(int x,int y) {
		
		this.Pos = new int[x][y];
		this.SizeX = x;
		this.SizeY = y;
	}
	
	public void change_type(int type,int x,int y) {
		
		this.Pos[x][y] = type;
		
		if(type == Constants.FireStation) {
			StationX = x;
			StationY = y;
		}
	}
	
	public int get_type(int x,int y) {
		return this.Pos[x][y];
	}
	
	
	public int get_type(float x,float y) {
		return this.Pos[(int) x][(int) y];
	}
	
}

package communication;

import java.io.Serializable;

public class PosVehicle implements Serializable{
	
	public float cor_x;
	public float cor_y;
	public int fuel;
	public int fuel_capacity;
	public int water;
	public int water_capactity;
	public int speed;
	
	public PosVehicle(float cor_x,float cor_y,int fuel,int fuel_capacity,
				int water,int water_capacity,int speed) {
		
		this.cor_x = cor_x;
		this.cor_y = cor_y;
		this.fuel = fuel;
		this.fuel_capacity = fuel_capacity;
		this.water = water;
		this.water_capactity = water_capacity;
		this.speed = speed;
		
	}
	


	public float get_x() {
		return this.cor_x;
		
	}
	
	public float get_y() {
		return this.cor_y;
	}
}

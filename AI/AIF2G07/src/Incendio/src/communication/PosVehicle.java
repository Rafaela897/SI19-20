package communication;

import java.io.Serializable;

public class PosVehicle implements Serializable{
	
	public int cor_x;
	public int cor_y;
	public int fuel;
	public int fuel_capacity;
	public int water;
	public int water_capactity;
	public int speed;
	
	public PosVehicle(int cor_x,int cor_y,int fuel,int fuel_capacity,
				int water,int water_capacity,int speed) {
		
		this.cor_x = cor_x;
		this.cor_y = cor_y;
		this.fuel = fuel;
		this.fuel_capacity = fuel_capacity;
		this.water = water;
		this.water_capactity = water_capacity;
		this.speed = speed;
		
	}
	


	public int get_x() {
		return this.cor_x;
		
	}
	
	public int get_y() {
		return this.cor_y;
	}
	
	

	@Override
	public int hashCode() {
		return (200*this.cor_x^2 + this.cor_y^3*100 + 
				this.fuel^2+1000 + this.fuel_capacity % 1000)%1000;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null)
			return false;
		if(this == o)
			return true;
		if(o instanceof PosVehicle) {
			PosVehicle test = (PosVehicle) o;
			
			return (this.cor_x == test.cor_x && this.cor_y == test.cor_y);
		}
		
		else {
			return false;
		}
}
}

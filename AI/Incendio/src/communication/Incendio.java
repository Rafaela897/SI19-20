package communication;

import java.io.Serializable;
import java.time.Instant;

import PathFinding.Pathfinding.pair;
import constants.Constants;

public class Incendio implements Serializable {
	
	public int cor_x;
	public int cor_y;
	public int gravity;
	public Instant start;
	
	public Incendio(int cor_x,int cor_y,int zona){
		
		this.cor_x = cor_x;
		this.cor_y = cor_y;
		
		this.start = Instant.now();
		
		switch(zona) {
			case Constants.FireStation:
			this.gravity = 5;
			break;
			case Constants.ResidentialZone:
			this.gravity = 4;
			break;
			case Constants.RuralZone:
			this.gravity = 3;
			break;
			default:
			this.gravity = 0;
			break;
		}
		
		return ;
	}
	
	public int get_Cor_x() {
		return this.cor_x;
	}
	
	public int get_Cor_y() {
		return this.cor_y;
	}
	
	@Override
	public int hashCode() {
		return (200*this.cor_x^2 + this.cor_y^3*100 +this.gravity^100)%100000;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null)
			return false;
		if(this == o)
			return true;
		if(o instanceof Incendio) {
			Incendio test = (Incendio) o;
			
			return (this.cor_x == test.cor_x && this.cor_y == test.cor_y);
		}
		
		else {
			return false;
		}
	}
	
}

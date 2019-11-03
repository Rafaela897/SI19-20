package communication;

import java.io.Serializable;

public class Incendio implements Serializable {
	
	private float cor_x;
	private float cor_y;
	
	
	public Incendio(float cor_x,float cor_y){
		this.cor_x = cor_x;
		this.cor_y = cor_y;
		
	}
	
	public float get_Cor_x() {
		return this.cor_x;
	}
	
	public float get_Cor_y() {
		return this.cor_y;
	}
	
	
}

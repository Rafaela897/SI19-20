package communication;

import java.io.Serializable;

public class PedidoCompleto implements Serializable {
	
	private float cor_x;
	private float cor_y;
	private int success;

	public PedidoCompleto(int success,float cor_x,float cor_y) {
		this.cor_x = cor_x;
		this.cor_y = cor_y;
		this.success = success;
	}
	
	public float get_X() {
		return this.cor_x;
		
	}
	
	public float get_Y() {
		return this.cor_y;
	}
	
	public int get_success() {
		return this.success;
	}
}

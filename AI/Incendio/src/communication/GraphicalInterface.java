package communication;
import java.awt.Color;
import java.util.HashMap;

import constants.Constants;
import jade.core.AID;
import processing.core.PApplet;


public class GraphicalInterface {
	
	public Mapa mapa;
	public Incendio incendios[] = new Incendio[0];
	public HashMap<AID,PosVehicle> localizacoes = new HashMap<AID,PosVehicle>(); 
	public int nr_incendios_total;
	
	/*public GraphicalInterface(Mapa mapa) {
		this.mapa = mapa;
	}*/
	
	public GraphicalInterface(Mapa mapa) {
		this.mapa = mapa;
		this.nr_incendios_total = 0;
	}
	
	
	
}
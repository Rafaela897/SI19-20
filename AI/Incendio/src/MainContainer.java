

import jade.core.Runtime;


import java.util.Set;
import java.util.concurrent.TimeUnit;

import PathFinding.Pathfinding;
import communication.GraphicalInterface;
import communication.Mapa;
import communication.Pos;
import communication.PosVehicle;
import constants.Constants;
import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import processing.core.PApplet;



public class MainContainer extends PApplet {

	Runtime rt;
	ContainerController container;
	static GraphicalInterface GI;

	public ContainerController initContainerInPlatform(String host, String port, String containerName) {
		// Get the JADE runtime interface (singleton)
		this.rt = Runtime.instance();

		// Create a Profile, where the launch arguments are stored
		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, containerName);
		profile.setParameter(Profile.MAIN_HOST, host);
		profile.setParameter(Profile.MAIN_PORT, port);
		// create a non-main agent container
		ContainerController container = rt.createAgentContainer(profile);

		return container;
	}

	public void initMainContainerInPlatform(String host, String port, String containerName) {

		// Get the JADE runtime interface (singleton)
		this.rt = Runtime.instance();

		// Create a Profile, where the launch arguments are stored
		Profile prof = new ProfileImpl();
		prof.setParameter(Profile.CONTAINER_NAME, containerName);
		prof.setParameter(Profile.MAIN_HOST, host);
		prof.setParameter(Profile.MAIN_PORT, port);
		prof.setParameter(Profile.MAIN, "true");
		prof.setParameter(Profile.GUI, "true");

		// create a main agent container
		this.container = rt.createMainContainer(prof);
		rt.setCloseVM(true);

	}

	public void startAgentInPlatform(String name, String classpath,Object[] args) {
		try {
			AgentController ac = container.createNewAgent(name, classpath, args);
			ac.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
 
	public static void populateCells(Mapa mapa,int type,int x,int y,int nr_cells) {
		
		int new_x,new_y;
		
		for(int i = 0; i < nr_cells;i++) {
			new_x = (int) (Math.random() * x); 
			new_y = (int) (Math.random() * y);
			
			while(mapa.get_type(new_x, new_y) != Constants.RuralZone) {
				new_x = (int) (Math.random() * x);
				new_y = (int) (Math.random() * y);
			}
		
			
			mapa.change_type(type, new_x, new_y);
		}
		
		return ;
	}
	
	public static Mapa createNewMap(int x,int y) {
		
		Mapa mapa = new Mapa(x,y);
		
		populateCells(mapa,Constants.FireStation,x,y,Constants.Nr_FireStationCells);

		populateCells(mapa,Constants.GasStation,x,y,Constants.Nr_GasStationCells);
				
		populateCells(mapa,Constants.ResidentialZone,x,y,Constants.Nr_ResidentialCells);
		
		populateCells(mapa,Constants.WaterReservoir,x,y,Constants.Nr_WaterReservoirs);

		return mapa;
	
	}
	
	public static void main(String[] args)   {
		MainContainer a = new MainContainer();

		a.initMainContainerInPlatform("localhost", "9888", "MainContainer");
		
	
		
		Mapa mapa = createNewMap(Constants.SizeX,Constants.SizeY);
		
		
		Pos[] shortestPath = Pathfinding.djikstra(mapa, 0, 0, 10, 10);
		int distancia = Pathfinding.distancia(0, 0, 10, 10);
		System.out.println(shortestPath.length);
		System.out.println(distancia);

		for(int i = 0; i < shortestPath.length; i++)
			System.out.println(""  + shortestPath[i].x + "," + shortestPath[i].y);
		
		System.out.println(shortestPath.length);

		
		GI = new GraphicalInterface(mapa);
		
		a.startAgentInPlatform("Station", "Agents.Station",new Object[] {mapa});
		

		
		a.startAgentInPlatform("Interface", "Agents.Interface",new Object[] {mapa,GI});

		
		// Example of Agent Creation in new container
		
		for(int i = 0 ; i < Constants.Nr_Trucks ; i++) {
			int x = (int) (Math.random() * Constants.SizeX);
			int y = (int) (Math.random() * Constants.SizeY);
			a.startAgentInPlatform("Truck" + i, "Agents.Truck" , new Object[] {mapa,x,y} );

		}
		
		for(int i = 0 ; i < Constants.Nr_Aircrafts ; i++) {
			int x = (int) (Math.random() * Constants.SizeX);
			int y = (int) (Math.random() * Constants.SizeY);
			a.startAgentInPlatform("Aircraft" + i, "Agents.Aircraft" , new Object[] {mapa,x,y} );

		}
		
		
		for(int i = 0;i < Constants.Nr_Drones ; i++) {
			int x = (int) (Math.random() * Constants.SizeX);
			int y = (int) (Math.random() * Constants.SizeY);
			a.startAgentInPlatform("Drone" + i, "Agents.Drone" , new Object[] {mapa,x,y} );

		}
		
		a.startAgentInPlatform("Incendiario", "Agents.Incendiario",new Object[] {mapa});

		
		
		
        PApplet.main("MainContainer");
        
		}
	
		public void settings(){
			size(600,600);
		}

		public void setup(){
			fill(120,50,240);
		}
		
		public void draw(){
	        
			float new_width = width/GI.mapa.SizeX;
	        float new_height = height/GI.mapa.SizeY;
	        float size_x = width/GI.mapa.SizeX;
	        float size_y = height/GI.mapa.SizeY;
	    	
	        for(int i = 0;i < GI.mapa.SizeX ; i++) {
	    	
	    		for(int d = 0;d < GI.mapa.SizeY; d++) {
	    			
	    			switch(GI.mapa.get_type(i, d)) {
	    				
	    				case Constants.GasStation:
	    				fill(64,62,61);
	    				break;
	    				
	    				case Constants.FireStation:
	    				fill(119,14,14);
	    				break;
	    				
	    				case Constants.ResidentialZone:
	    				fill(255,157,157);
	    				break;
	    				
	    				case Constants.WaterReservoir:
	    				fill(11,246,242);
	    				break;
	    				
	    				default:
	    				fill(27,253,0);
	    				break;
	    			}
	    			
	    			rect(i * new_width,d * new_height,size_x,size_y);
	    	
	    		}
	    		
	    	
	    	}
	    	
	        fill(245,93,22);
	        
	    	for(int i = 0; i < GI.incendios.length;i++) {
	    		
	    		float x = GI.incendios[i].get_Cor_x();
	    		float y = GI.incendios[i].get_Cor_y();
	    		
	    		rect(x * new_width,y * new_height,size_x,size_y);
	    		
	    	}
	    	
	    	AID[] KeysArr = new AID[GI.localizacoes.size()];
	    	AID[] loc_keys = (AID[]) GI.localizacoes.keySet().toArray(KeysArr);
	    	
 	    	for(int i = 0; i < loc_keys.length ;i++){ 
 	    		PosVehicle posV = GI.localizacoes.get(loc_keys[i]);
 	    		Object class_ = loc_keys[i].getLocalName().replaceAll("[0-9]", "") ;
 	    		
 	    	
 	    		
 	    		if(class_.equals("Truck")) {
    				fill(232,255,6);
 	    		}
 	    		
 	    		if(class_.equals("Aircraft")) {
 	    			fill(244,255,133);
 	    		}
 	    		
 	    		if(class_.equals("Drone")) {
    				fill(104,115,0);
 	    		}
 	    		
 	    		float x = posV.get_x();
	    		float y = posV.get_y();
	    		
	    		rect(x * new_width,y * new_height,size_x,size_y);

 	    		
 	    	}
 	    	
 	    	}
 	    }
	

	
			
	
	

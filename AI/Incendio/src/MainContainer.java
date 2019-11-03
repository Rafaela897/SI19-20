

import jade.core.Runtime;

import java.util.concurrent.TimeUnit;

import communication.GraphicalInterface;
import communication.Mapa;
import constants.Constants;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import processing.core.PApplet;

/**
 * 
 */

/**
 * @author Filipe Gonalves
 *
 */
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


		return mapa;
	
	}
	
	public static void main(String[] args)   {
		MainContainer a = new MainContainer();

		a.initMainContainerInPlatform("localhost", "9888", "MainContainer");
		
		
		
		/*int n = 0;
		int limit = 100; // Limit number of Customers
		try {
			while (n<limit) {   //novo Costumer a cada segundo at� ter 10 Costumers
				a.startAgentInPlatform("Customer " + n, "Agents.Customer");
				n++;
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		*/
		
		
		// Example of Container Creation (not the main container)
		
		Mapa mapa = createNewMap(Constants.SizeX,Constants.SizeY);
		
		GI = new GraphicalInterface(mapa);
		
		a.startAgentInPlatform("Station", "Agents.Station",new Object[] {mapa});
		
		a.startAgentInPlatform("Incendiario", "Agents.Incendiario",new Object[] {mapa});

		
		a.startAgentInPlatform("Interface", "Agents.Interface",new Object[] {mapa,GI});

		
		// Example of Agent Creation in new container
		
		for(int i = 0 ; i < Constants.Nr_Trucks ; i++) {
			a.startAgentInPlatform("Truck" + i, "Agents.Truck" , new Object[] {mapa,mapa.StationX,mapa.StationY} );

		}
		
		for(int i = 0 ; i < Constants.Nr_Aircrafts ; i++) {
			a.startAgentInPlatform("Aircraft" + i, "Agents.Aircraft" , new Object[] {mapa,mapa.StationX,mapa.StationY} );

		}
		
		
		for(int i = 0;i < Constants.Nr_Drones ; i++) {
			a.startAgentInPlatform("Drone" + i, "Agents.Drone" , new Object[] {mapa,mapa.StationX,mapa.StationY} );

		}
		
		
		
		
        PApplet.main("MainContainer");
        
		}
	
		public void settings(){
			size(600,600);
		}

		public void setup(){
			fill(120,50,240);
		}
		
		public void draw(){
	    	
	    	for(int i = 0;i < GI.mapa.SizeX ; i++) {
	    	
	    		for(int d = 0;d < GI.mapa.SizeY; d++) {
	    			
	    			switch(GI.mapa.get_type(i, d)) {
	    				
	    				case Constants.GasStation:
	    				fill(256,0,256);
	    				break;
	    				
	    				case Constants.FireStation:
	    				fill(0,256,256);
	    				break;
	    				
	    				case Constants.ResidentialZone:
	    				fill(0,0,0);
	    				break;
	    				
	    				default:
	    				fill(256,256,256);
	    				break;
	    			}
	    			
	    			rect(i * width/GI.mapa.SizeX,d * height/GI.mapa.SizeY,width/GI.mapa.SizeX,height/GI.mapa.SizeY);
	    	
	    		}
	    	}
		}
	
			
	}
	

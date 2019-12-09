package PathFinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import Agents.Vehicle;
import communication.Mapa;
import communication.Pos;
import communication.PosVehicle;
import constants.Constants;

public class Pathfinding {
	
	public static class pair {
		public int x;
		public int y;
		
		public pair(int x,int y) {
			this.x = x;
			this.y = y;
			
		}
		
		@Override
		public int hashCode() {
			return (200*x^2 + y^3*100)%1000;
		}
		
		@Override
		public boolean equals(Object o) {
			if(o == null)
				return false;
			if(this == o)
				return true;
			if(o instanceof pair) {
				pair test = (pair) o;
				
				return (this.x == test.x && this.y == test.y);
			}
			
			else {
				return false;
			}
		}
	}
	public static ArrayList<pair> neighbours(int size_x,int size_y,int x,int y) {
		
		ArrayList<pair> neighbours = new ArrayList<pair>();
		
		for(int i =  x - 1; i < x + 2; i++ ) {
			for(int d = y - 1;d < y + 2;d++) {
				if(i < size_x && d < size_y && i >= 0 && d >= 0)
					if(d != y || i != x)
						neighbours.add(new pair(i,d));
			}
		}
		
		return neighbours;
		
		
	}
	
	
	public static Pos[] djikstra(Mapa mapa,int orig_x,int orig_y,int dest_x,int dest_y) {
		HashMap<pair,Integer> NE = new HashMap<pair,Integer>();
		HashMap<pair,Integer> DISTS = new HashMap<pair,Integer>();
		HashMap<pair,pair> PREVS = new HashMap<pair,pair>();

		int max = 999999999;
		for(int i = 0; i < mapa.SizeX; i++) {
			for(int d = 0; d < mapa.SizeY; d++) {
				pair unexplored = new pair(i,d);
				NE.put(unexplored,max);
				DISTS.put(unexplored, max);
				PREVS.put(unexplored,null);
				}
		}
			
			pair origem = new pair(orig_x,orig_y);
			//System.out.println(NE.size());

			NE.remove(origem);
			//System.out.println(NE.size());

			DISTS.put(origem, 0);
			NE.put(origem, 0);

			//System.out.println("" + DISTS.get(new pair(0,3)));
			while(NE.size() != 0) {
				
				pair vertex = Collections.min(NE.entrySet(), Map.Entry.comparingByValue()).getKey();
				
				NE.remove(vertex);
				
				//System.out.println(NE.size());
				ArrayList<pair> neighbours = neighbours(mapa.SizeX,mapa.SizeY,vertex.x,vertex.y);
				
				
				for(int d = 0; d < neighbours.size();d++) {
					
					
				
				
					int alt = DISTS.get(vertex) + 1;
					
					pair neighbour = neighbours.get(d);
					
					//System.out.println(alt);

					//System.out.println("" + neighbour.x + " " + neighbour.y);
					
					if(alt  < DISTS.get(neighbour)) {
						
						NE.put(neighbour,alt);
						DISTS.put(neighbour,alt);
						PREVS.put(neighbour,vertex);
					}
				
				}
				
				
				
			
			}
		
		ArrayList<Pos> posV = new ArrayList<Pos>(); 
		//PREVS.forEach((k,v) -> System.out.println("(" + k.x + "," + k.y + ")" + v));
		
		
		pair last = new pair(dest_x,dest_y);
		
		for(pair vertex = PREVS.get(last); vertex != null; vertex = PREVS.get(vertex) ) {
			Pos pos = new Pos(vertex.x,vertex.y);
			posV.add(pos);
		}
		
		Pos[] shortest_path = new Pos[posV.size()];
		shortest_path = posV.toArray(shortest_path);
		return shortest_path;
		
	}
	
	public static Pos[] concatArrays(Pos[] arr1,Pos[] arr2) {
		
		Pos[] new_array = new Pos[arr1.length + arr2.length];
		
		System.arraycopy(arr1, 0, new_array, 0, arr1.length);
		
		System.arraycopy(arr2, 0, new_array, arr1.length, arr2.length);
		
		return new_array;
		
	}
	
	public  static ArrayList<Pos> FindByType(Mapa mapa,int type) {
		
		ArrayList<Pos> types = new ArrayList<Pos>();
		
		for(int i = 0;i < mapa.SizeX;i++) {
			for(int d = 0; d < mapa.SizeY;d++) {
				if(mapa.get_type(i, d) == type)
					types.add(new Pos(i,d));
				
				
			}
			
		}
		
		return types;
	}
	
	
	public static Pos[] find_path_aux(Mapa mapa,int FuelCapacity,int Fuel,
			int Curr_posX,int Curr_posY,int incendioX,int incendioY,ArrayList<Pos> gas_stations) {
		
		
		
		Pos[] shortest_path = djikstra(mapa,Curr_posX,Curr_posY,incendioX,incendioY);
		
	
		//System.out.println("shortest path " + shortest_path.length);
		if(shortest_path.length <= Fuel) 
				return shortest_path;
	
		if(gas_stations.size() == 0)
					return null;
		
		//System.out.println("needs gas");
		
		ArrayList<Pos> reachable = new ArrayList<Pos>();

		ArrayList<Pos[]> gas_stations_routes = new ArrayList<Pos[]>();
		
		gas_stations.sort((gs1,gs2) -> distancia(gs1.x,gs1.y,Curr_posX,Curr_posY) + distancia(gs1.x,gs1.y,incendioX,incendioY) <
				distancia(gs2.x,gs2.y,Curr_posX,Curr_posY) + distancia(gs2.x,gs2.y,incendioX,incendioY)
				? 1:0 );
		
		
		for(int i = 0;i < gas_stations.size();i++) {
			
			Pos[] pos_to_gs  = djikstra(mapa,Curr_posX,Curr_posY,gas_stations.get(i).x,gas_stations.get(i).y);
			Pos[] gs_to_fire = djikstra(mapa,gas_stations.get(i).x,gas_stations.get(i).y,incendioX,incendioY);
			
			if(Fuel >= pos_to_gs.length && FuelCapacity >= gs_to_fire.length ) {
				Pos[] new_array = concatArrays(gs_to_fire,pos_to_gs);
				/*System.out.println("GS: " + pos_to_gs[pos_to_gs.length - 1].x + " : " +
						pos_to_gs[pos_to_gs.length - 1].y);
				System.out.println("GS: " + gs_to_fire[gs_to_fire.length - 1].x + " : " +
						gs_to_fire[gs_to_fire.length - 1].y);
				*/
				return  new_array;
			}
			
			else if(pos_to_gs.length <= Fuel) {
				reachable.add(gas_stations.get(i));
				gas_stations_routes.add(pos_to_gs);
			}
		}
		
		for(int i = 0; i < reachable.size();i++)
			gas_stations.remove(reachable.get(i));
		
		for(int i = 0; i < reachable.size();i++) {
			Pos[] caminho = find_path_aux(mapa,FuelCapacity,FuelCapacity,
					reachable.get(i).x,reachable.get(i).y,incendioX,incendioY,gas_stations);
			
			if(caminho != null && FuelCapacity >= caminho.length)
				return concatArrays(gas_stations_routes.get(i),caminho);
			
		}
		
			return null;
		
		
		
	}
	
	public static Pos[] find_path(Mapa mapa,int FuelCapacity,int Fuel,
	int incendioX,int incendioY,int Curr_posX,int Curr_posY) {
		
		ArrayList<Pos> gas_stations = FindByType(mapa,Constants.GasStation);

		
		return find_path_aux(mapa,FuelCapacity,Fuel,
				Curr_posX,Curr_posY,incendioX,incendioY,gas_stations);
		
		
		
		
	}
	
	public static Pos[] path_nearest_gas_station(Mapa mapa,int Curr_posX,int Curr_posY,int Fuel) {
		
		ArrayList<Pos> gas_stations = FindByType(mapa,Constants.GasStation);
		
		gas_stations.sort((gs1,gs2) -> distancia(gs1.x,gs1.y,Curr_posX,Curr_posY)  <
				distancia(gs2.x,gs2.y,Curr_posX,Curr_posY) 
				? 1:0 );
		
		
		for(int i = 0;i < gas_stations.size();i++) {
			
			Pos[] pos_to_gs  = find_path(mapa,Fuel,Fuel,Curr_posX,Curr_posY,gas_stations.get(i).x,gas_stations.get(i).y);
			
			if(pos_to_gs != null ) {
				return pos_to_gs;
			}
		}
		
		return null;
	}
	
	public static Pos[] path_nearest_water_reservoir(Mapa mapa,int Curr_posX,int Curr_posY,int Fuel,int FuelCapacity) {
		
		ArrayList<Pos> water_reservoirs = FindByType(mapa,Constants.WaterReservoir);
		
		water_reservoirs.sort((wr1,wr2) -> distancia(wr1.x,wr1.y,Curr_posX,Curr_posY)  <
				distancia(wr2.x,wr2.y,Curr_posX,Curr_posY) 
				? 1:0 );
		
		for(int i = 0; i < water_reservoirs.size();i++) {
			
			Pos[] path_reservoir = find_path(mapa,FuelCapacity,Fuel,
					Curr_posX,Curr_posY,water_reservoirs.get(i).x,water_reservoirs.get(i).y);
			
			if(path_reservoir != null) 
						return path_reservoir;
			
				
		
		}
		
		
		
		return null;
	}
	

	
	public static int distancia(int pointaX,int pointaY,int pointbX,int pointbY) {

		return (int) Math.sqrt(Math.pow(pointaX - pointbX,2) + Math.pow(pointaY - pointbY,2));
	}
	
	
	
}

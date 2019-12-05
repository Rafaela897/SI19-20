package communication;

import java.io.Serializable;
import java.time.Instant;

import PathFinding.Pathfinding.pair;
import constants.Constants;
import jade.core.AID;

public class DistressCall implements Serializable {
	
	public PosVehicle pos;
	public AID aid;
	
	public DistressCall(PosVehicle pos,AID aid){
		
		this.aid = aid;
		this.pos = pos;
		
		
		
		return ;
	}
	
	
	
	
	
	
}

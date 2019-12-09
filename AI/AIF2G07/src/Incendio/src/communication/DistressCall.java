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
	
	@Override
	public int hashCode() {
		return (this.aid.hashCode()^10 + this.pos.hashCode()^20)%100000;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null)
			return false;
		if(this == o)
			return true;
		if(o instanceof DistressCall) {
			DistressCall test = (DistressCall) o;
			
			return (this.aid.equals(test.aid) && this.pos.equals(test.pos));
		}
		
		else {
			return false;
		}
	}
	
	
	
	
	
	
}

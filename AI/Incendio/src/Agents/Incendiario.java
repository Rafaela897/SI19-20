package Agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import communication.Incendio;
import communication.Mapa;
import constants.Constants;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class Incendiario extends Agent {

	
	Mapa mapa;
	
	
	protected void setup() {
		super.setup();
		// Register Agent
		Object[] args = getArguments();
		this.mapa =  (Mapa) args[0];
		
		
		
		
		addBehaviour(new criaIncendio(this,1000));

	}

	private class criaIncendio extends TickerBehaviour {
		
		public criaIncendio(Agent agent,int time) {
			super(agent,time);
		}
		
		public void onTick() {
			
			
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			
			
			int cor_x = (int)  (Math.random() * Constants.SizeX);
			int cor_y = (int)  (Math.random() * Constants.SizeY);
			
			//System.out.println("" + cor_x + "|" + cor_y );
			
			if(mapa.get_type(cor_x, cor_y) == Constants.RuralZone 
					|| mapa.get_type(cor_x,cor_y) == Constants.ResidentialZone
					|| mapa.get_type(cor_x, cor_y) == Constants.FireStation) {
			
		
			
			Incendio incendio = new Incendio(cor_x,cor_y,mapa.get_type(cor_x, cor_y));			
			AID reader = new AID("Station", AID.ISLOCALNAME);
			msg.addReceiver(reader);
			msg.setOntology("fires");
			try {
				msg.setContentObject((Serializable) incendio);
				send(msg);	

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			
			}
		}
		}
	
	
	
	}

	

	protected void takeDown() {
		super.takeDown();
		// De-register Agent from DF before killing it
		try {
			DFService.deregister(this);
		} catch (Exception e) {
		}
	}

}

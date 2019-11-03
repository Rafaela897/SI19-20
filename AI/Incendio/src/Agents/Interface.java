package Agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import communication.GraphicalInterface;
import communication.Incendio;
import communication.Mapa;
import communication.PosVehicle;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;


import java.lang.Math;

public class Interface extends Agent {
	
	Interface manager = this;
	private HashMap<AID,PosVehicle> localizacoes = new HashMap<AID,PosVehicle>(); 
	Mapa mapa;
	private GraphicalInterface graphicalinterface;
	
	protected void setup() {
		super.setup();
		Object[] args = getArguments();
		this.mapa = (Mapa) args[0];
		this.graphicalinterface = (GraphicalInterface) args[1];
		
		
		
		
		// Register Agent
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getLocalName());
		sd.setType("manager");
		dfd.addServices(sd);
		
		


		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		
		addBehaviour(new AtualizarInformacao());
	

	}


	private class AtualizarInformacao extends CyclicBehaviour {
		
		public void action() {
			
			ACLMessage msg = receive();

		}
		
		
			
				
		
	}

}	


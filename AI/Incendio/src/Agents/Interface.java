package Agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
	Mapa mapa;
	GraphicalInterface graphicalinterface;

	protected void setup() {
		super.setup();
		Object[] args = getArguments();
		this.mapa = (Mapa) args[0];
		this.graphicalinterface = (GraphicalInterface) args[1];
		
		
		
		// Register Agent
	
		

		
		addBehaviour(new AtualizarInformacao());
	

	}


	private class AtualizarInformacao extends CyclicBehaviour {
		
		public void action() {
			
			ACLMessage msg = receive();

			if (msg != null ) { // receber atualizações				
					try {
						String ontology  = msg.getOntology();
						if(ontology.equals("info_fires") ){
							System.out.println(msg.getSender());
							graphicalinterface.incendios = (Incendio[]) msg.getContentObject();

						}
						
						else if(ontology.equals("info_loc")){
							graphicalinterface.localizacoes = (HashMap<AID,PosVehicle>) msg.getContentObject();

							
						}
					}
					 catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
			}
		}
		
		
			
				
		
	}

}	


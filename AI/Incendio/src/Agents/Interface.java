package Agents;

import java.io.File;
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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;


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

			if (msg != null && msg.getPerformative() == ACLMessage.INFORM) { // receber atualizações				
					try {
						String ontology  = msg.getOntology();
						if("info_fires".equals(ontology)){
							//System.out.println(msg.getSender());
							graphicalinterface.incendios = (Incendio[]) msg.getContentObject();

						}
						
						else if("info_loc".contentEquals(ontology)){
							graphicalinterface.localizacoes = (HashMap<AID,PosVehicle>) msg.getContentObject();

							
						}
						
						else if("info_nr_fires".equals(ontology)) {
							graphicalinterface.nr_incendios_total = (int) msg.getContentObject();
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


package Agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import communication.Incendio;
import communication.Mapa;
import communication.PedidoCompleto;
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

public class Station extends Agent {
	Station manager = this;
	private HashMap<AID,PosVehicle> localizacoes = new HashMap<AID,PosVehicle>(); 
	ArrayList<Incendio> incendios;
	Mapa mapa;
	
	protected void setup() {
		super.setup();

		Object[] args = getArguments();
		this.mapa =  (Mapa) args[0];
		this.incendios = new ArrayList<Incendio>();
		
		// Register Agent
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getLocalName());
		sd.setType("Station");
		dfd.addServices(sd);
		
		


		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		
		addBehaviour(new Update());
		addBehaviour(new UpdateInterface(this,1000));

	}


	private class Update extends CyclicBehaviour {
		
		public void action() {
			
			ACLMessage msg = receive();

			if (msg != null ) { // receber atualizações				
					
				try {
					if(msg.getContentObject().getClass() == Incendio.class) {
						System.out.println("new client");
						new_client(msg);
						
					}

					if(msg.getContentObject().getClass() == PosVehicle.class) {
						atualizarCordenadas(msg);
					}
					
					block();
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				}
				}
		
		public void atualizarCordenadas(ACLMessage msg) throws UnreadableException {
			PosVehicle cordenada =  (PosVehicle) msg.getContentObject();
			
			AID sender = msg.getSender();
			localizacoes.put(sender, cordenada);
			System.out.println("Coordenas atualizadas " + msg.getSender() );

		}
		
		public void new_client(ACLMessage msg) {
			try {
				Incendio incendio =  (Incendio) msg.getContentObject();

				// Search DF
				float cl_x = incendio.get_Cor_x();
				float cl_y = incendio.get_Cor_y();
				
				DFAgentDescription dfd = new DFAgentDescription();
				DFAgentDescription[] results = DFService.search(this.myAgent, dfd);
				
				if (results.length > 0) {
					int melhor_taxi = -1;
					float distancia = 999999999;
					float new_distancia;
					float cor_x;
					float cor_y;
					
					
					
					for (int i = 0; i < results.length; ++i) {
						// Agent Found
						AID taxi = results[i].getName();
						PosVehicle cordenada = localizacoes.get(taxi);
						
						if(cordenada != null) {
							
							cor_x = cordenada.get_x();
							cor_y = cordenada.get_y();
					
							
							new_distancia = (float) Math.sqrt(Math.pow(cor_x - cl_x,2) + Math.pow(cor_y - cl_y,2));
						
							if(new_distancia < distancia ) {
								distancia = new_distancia;
								melhor_taxi = i;
							}
						}
					}
							
					
					if(melhor_taxi != -1) {
								
								System.out.println("starting work");
								DFAgentDescription dfd1 = results[melhor_taxi];
								AID provider = dfd1.getName();

								msg = new ACLMessage(ACLMessage.REQUEST);
								msg.addReceiver(provider);
								msg.setContentObject((Serializable) incendio);
								
								send(msg);
								
					}
					
					if(melhor_taxi == -1) {
						incendios.add(incendio);
					}
							
						}
				
				if(results.length <= 0) {
					incendios.add(incendio);
				}
					
				
			
			}
			 catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			
				
		}
	}
	
private class UpdateInterface extends TickerBehaviour {

		
		public UpdateInterface(Agent agent,int time) {
			super(agent,time);
		
		}
		
		public void AtualizarInterface() {
			
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			AID reader = new AID("Interface", AID.ISLOCALNAME);
			msg.addReceiver(reader);
			try {
				msg.setContentObject((Serializable) localizacoes);
				send(msg);	

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	
		
		public void onTick() {
			
							
			AtualizarInterface();
							
		}
	}

}	


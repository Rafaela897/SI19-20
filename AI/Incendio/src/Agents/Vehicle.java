package Agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import PathFinding.Pathfinding;
import communication.Incendio;
import communication.Mapa;
import communication.PedidoCompleto;
import communication.PosVehicle;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class Vehicle extends Agent {

	int Work_progress = 0;
	
	float Curr_posX;
	float Curr_posY;
	
	int Fuel;
	int FuelCapacity;
	
	int Water;
	int WaterCapacity;
	
	int Velocity;
	
	int Free;
	
	PosVehicle destination[];
	
	Mapa mapa;
	
	protected void setup() {
		super.setup();
		
		Object[] args = getArguments();
		
		this.mapa = (Mapa)args[0];
		this.Curr_posX = (float) (int) args[1];
		this.Curr_posY = (float) (int) args[2];
		this.Free = 0;
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getLocalName());
		sd.setType("Vehicle");
		
		dfd.addServices(sd);
		 
		
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		addBehaviour(new Move(this,1000)); // fazer a compra de um produto
		addBehaviour(new ReceberDiretivas()); // saber se a requisi��o teve sucesso
		addBehaviour(new AtualizarInformacao(this,1000));
	}
	
	private class AtualizarInformacao extends TickerBehaviour {

		public AtualizarInformacao(Agent a, long period) {
			super(a, period);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onTick() {
			// TODO Auto-generated method stub

			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			
			PosVehicle posicao = new PosVehicle(Curr_posX , Curr_posY,Fuel,FuelCapacity,Water,WaterCapacity,Velocity);
			AID reader = new AID("Station", AID.ISLOCALNAME);
			msg.addReceiver(reader);
			try {
				msg.setContentObject((Serializable) posicao);
				send(msg);	

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
	}

	private class Move extends TickerBehaviour {

		
		public Move(Agent agent,int time) {
			super(agent,time);
		
		}
		
		public void PedidoCompleto() {
			
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			PedidoCompleto completo = new PedidoCompleto(1,Curr_posX,Curr_posY);
			AID reader = new AID("Station", AID.ISLOCALNAME);
			msg.addReceiver(reader);
			try {
				msg.setContentObject((Serializable) completo);
				send(msg);	

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	
		
		public void onTick() {
				
				if(Free == 0) {
					
					
				}
				
				else {
					
					if(Fuel > 0 && Work_progress < destination.length ) {
						
						Fuel--;
						Work_progress++;
						Curr_posX = destination[Work_progress].get_x();
						Curr_posY = destination[Work_progress].get_y();
						
						if(Work_progress == destination.length) {
							
							Work_progress = 0;
							Free = 0;
							PedidoCompleto();
						}	
					}
						
				}
							
					
				}
				
	}
				
			
			
		

	public class ReceberDiretivas extends CyclicBehaviour {
		public void action() {
			ACLMessage message = receive();
			if (message != null) {
				try {
					Incendio pedido = (Incendio) message.getContentObject();
					
					int cordenada_x = (int) pedido.get_Cor_x(); 
					int cordenada_y = (int) pedido.get_Cor_y();
					
					destination = Pathfinding.find_path(mapa,(int) Curr_posX,(int) Curr_posY,cordenada_x,cordenada_y);
					
					Free = 1;
					System.out.println("Nova diretiva");
				}
				catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
			block();
		}
		
	}
		



	
	


	protected void takeDown() {
		super.takeDown();
	}
	
	}



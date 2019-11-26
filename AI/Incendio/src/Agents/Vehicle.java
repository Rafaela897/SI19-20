package Agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import PathFinding.Pathfinding;
import communication.Incendio;
import communication.Mapa;
import communication.PedidoCompleto;
import communication.Pos;
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
	
	int Curr_posX;
	int Curr_posY;
	
	public int Fuel;
	public int FuelCapacity;
	
	int Water;
	int WaterCapacity;
	
	int Velocity;
	
	int Free;
	
	Pos destination[];
	
	Mapa mapa;
	
	Incendio incendio_corrente;
	
	protected void setup() {
		super.setup();
		
		Object[] args = getArguments();
		
		this.mapa = (Mapa)args[0];
		this.Curr_posX = (int) args[1];
		this.Curr_posY = (int) args[2];
		this.Free = 0;
		this.incendio_corrente = null;
		
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
		
		addBehaviour(new Move(this,1000)); 
		addBehaviour(new ReceberDiretivas());
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
			msg.setOntology("coordenadas");
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
			msg.setOntology("pedido");
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
						Curr_posX = destination[Work_progress].x;
						Curr_posY = destination[Work_progress].y;
						
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
			if (message != null && message.getOntology().equals("job")) {
				System.out.println(message.getSender());

				try {

					Incendio pedido = incendio_corrente = (Incendio) message.getContentObject();
					
					
					int cordenada_x = (int) pedido.get_Cor_x(); 
					int cordenada_y = (int) pedido.get_Cor_y();
					
					destination = Pathfinding.find_path(mapa,(int) Curr_posX,(int) Curr_posY,cordenada_x,cordenada_y, cordenada_y, cordenada_y);
					
					Free = 1;
					System.out.println("" + destination.length);
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



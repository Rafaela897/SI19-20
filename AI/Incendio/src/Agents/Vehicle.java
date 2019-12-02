package Agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import PathFinding.Pathfinding;
import communication.Incendio;
import communication.Mapa;
import communication.PedidoCompleto;
import communication.Pos;
import communication.PosVehicle;
import constants.Constants;
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
import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Value;

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
	
	Rete engine;

	
	protected void setup() {
		super.setup();
		
		Object[] args = getArguments();
		
		this.mapa = (Mapa)args[0];
		this.Curr_posX = (int) args[1];
		this.Curr_posY = (int) args[2];
		this.Free = 0;
		this.incendio_corrente = null;
		
	
		register();
		
		engine = new Rete();
		
		try {
			engine.batch("src/free_time_allocation.clp");
			engine.reset();
		} catch (JessException e) {
			// TODO Auto-genserated catch block
			e.printStackTrace();
		}
		
		addBehaviour(new AtualizacaoInicial());
		addBehaviour(new Move(this,1000)); 
		addBehaviour(new ReceberDiretivas());
		
	}
	
	public class AtualizacaoInicial extends OneShotBehaviour {

		@Override
		public void action() {
			// TODO Auto-generated method stub
			AtualizaInformacao();

		}
		
	}
	
	public void  AtualizaInformacao() {
		
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
	
	public void  UpdateDeathVehicle() throws IOException {
		
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		
		AID reader = new AID("Station", AID.ISLOCALNAME);
		msg.setOntology("morte");
		msg.addReceiver(reader);
		msg.setContent("Veiculo não se encontra em condições de operar");
		send(msg);
	}
	
		
	

	private class Move extends TickerBehaviour {

		
		public Move(Agent agent,int time) {
			super(agent,time);
		
		}
		
		public void PedidoCompleto() {
			
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			AID reader = new AID("Station", AID.ISLOCALNAME);
			msg.setOntology("pedido");
			msg.addReceiver(reader);
			
			try {
				msg.setContentObject((Serializable) incendio_corrente);
				send(msg);	

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	
		public void addFact(int Fuel,int FuelMax,int Water,int WaterMax,Rete engine) {
			
			
			try {
				
				Fact f = new Fact("vehicle", engine);
				f.setSlotValue("water", new Value(Water, RU.INTEGER));
				f.setSlotValue("fuel", new Value(Fuel, RU.INTEGER));
				f.setSlotValue("maxFuel", new Value(FuelMax, RU.INTEGER));
				f.setSlotValue("maxWater", new Value(WaterMax, RU.INTEGER));
				engine.assertFact(f);


			} catch (JessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		public int AllocateFreeTime() throws JessException {
			addFact(Fuel,FuelCapacity,Water,WaterCapacity,engine);
			engine.run();
			
			Iterator iterator = engine.listFacts();

			int decision = 0;
			
			while(iterator.hasNext()) {
				Fact fact = (Fact) iterator.next();
				if(fact.getName().equals("MAIN::decision")) {
					System.out.println(fact.getSlotValue("value")+ "");
					decision = fact.getSlotValue("value").intValue(engine.getGlobalContext());
					break;
				}  
			}
			
			return decision;
			
				
		}
		public void onTick() {
				
				if(Free == 0) {
					//use jess here
					
					if(Water < WaterCapacity) {
						Pos[] p_to_wr = Pathfinding.path_nearest_water_reservoir(mapa, Curr_posX, Curr_posY, Fuel, FuelCapacity);
						
						if(p_to_wr != null) {
							destination = p_to_wr;
							Work_progress = 0;
						}
					}
					
					if(Fuel < FuelCapacity) {
						Pos[] p_to_gs = Pathfinding.path_nearest_gas_station(mapa, Curr_posX, Curr_posY, Fuel);
						
						if(p_to_gs != null) {
							destination = p_to_gs;
							Work_progress = 0;
						}
						
					}
						
					
				}
				
				else {
					
					
					if(Fuel > 0 && destination != null && Work_progress < destination.length ) {
						int i = 0;
						

						if(Work_progress == destination.length - 1) {
							
							Water--;
							Work_progress = 0;
							
							if(Free == 1) {
								PedidoCompleto();
								register();
							}
							
							Free = 0;
						}
						
						else {
						
						while(i < Velocity) {
						
							Work_progress++;
							Fuel--;
							
							if(mapa.get_type(destination[Work_progress].x, destination[Work_progress].y) == Constants.GasStation) {
								Fuel = FuelCapacity;
							}
							
							if(mapa.get_type(destination[Work_progress].x, destination[Work_progress].y) == Constants.WaterReservoir) {
								Water = WaterCapacity;
							}
							
							i++;
								
							if(Work_progress >= destination.length - 1  ) {
								break;
							}
							
							}
						
						
							Curr_posX = destination[Work_progress].x;
							Curr_posY = destination[Work_progress].y;
							
							AtualizaInformacao();
						
				
						
							
					}
						
				}
							
					
				}
				
	}
	}
				
			
			
		

	public class ReceberDiretivas extends CyclicBehaviour {
		public void action() {
			ACLMessage message = receive();
			if (message != null && message.getOntology().equals("job")) {

				try {

					System.out.println("Free: " + Free);
					Incendio pedido = incendio_corrente = (Incendio) message.getContentObject();
					
					
					int cordenada_x = (int) pedido.get_Cor_x(); 
					int cordenada_y = (int) pedido.get_Cor_y();
					
					System.out.println("" + Fuel);
					System.out.println(Curr_posX + " : " + Curr_posY);

					destination = Pathfinding.find_path(mapa,FuelCapacity,Fuel,(int) Curr_posX,(int) Curr_posY,cordenada_x,cordenada_y);
					//destination = Pathfinding.djikstra(mapa,  cordenada_x, cordenada_y,(int) Curr_posX,(int) Curr_posY);


					
					if(destination == null)
						return ;
					
					deregister();
					Free = 1;

					System.out.println(destination.length);
					System.out.println(destination[destination.length - 1].x + " : " + destination[destination.length - 1].y);
					System.out.println(cordenada_x + " : " + cordenada_y);
					
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
		



	void register() {
		
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
	
	}
	
	void deregister() {
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void takeDown() {
		
		try {
			UpdateDeathVehicle();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		deregister();
		super.takeDown();
		
	}
	
	}



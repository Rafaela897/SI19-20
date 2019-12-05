package Agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import PathFinding.Pathfinding;
import communication.DistressCall;
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
import jade.core.behaviours.WakerBehaviour;
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
	
	public int Water;
	public int WaterCapacity;
	
	int Velocity;
	
	int State;
	
	Pos destination[];
	
	Mapa mapa;
	
	Incendio incendio_corrente;
	
	PosVehicle veiculo_avariado;
	
	AID aid_veiculo_avariado;
	
	Rete engine;

	
	protected void setup() {
		super.setup();
		
		Object[] args = getArguments();
		
		this.mapa = (Mapa)args[0];
		this.Curr_posX = (int) args[1];
		this.Curr_posY = (int) args[2];
		this.State = 0;
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
		
		addBehaviour(new Update(this,100));
		addBehaviour(new Move(this,1000/Velocity)); 
		addBehaviour(new ReceberDiretivas());
		
	}
	
	public class Update extends TickerBehaviour {
		
		public Update(Agent a, long period) {
			super(a, period);
			// TODO Auto-generated constructor stub
		}

		
		@Override
		protected void onTick() {
			// TODO Auto-generated method stub
			AtualizaInformacao();
		}
		
	}
	
	public void  AtualizaInformacao() {
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		
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
	
	
	
	
	public void sendDistressCall() throws IOException {
		
		System.out.println("Distress Call: Fuel: " + Fuel + " Water: " + Water);
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		
		AID reader = new AID("Station", AID.ISLOCALNAME);
		msg.addReceiver(reader);
		PosVehicle my_pos = new PosVehicle(Curr_posX,Curr_posY, Fuel, FuelCapacity, Water,WaterCapacity, Velocity);
		msg.setContentObject(my_pos);
		send(msg);
	}
	
		
	

	private class Move extends TickerBehaviour {

		
		public Move(Agent agent,int time) {
			super(agent,time);
		
		}
		
		public void PedidoCompleto() {
			
			ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
			AID reader = new AID("Station", AID.ISLOCALNAME);
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
				f.setSlotValue("maxWater", new Value(WaterMax, RU.INTEGER));
				f.setSlotValue("fuel", new Value(Fuel, RU.INTEGER));
				f.setSlotValue("maxFuel", new Value(FuelMax, RU.INTEGER));
				engine.assertFact(f);


			} catch (JessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		public int AllocateStateTime() throws JessException {
			addFact(Fuel,FuelCapacity,Water,WaterCapacity,engine);
			engine.run();
			
			Iterator iterator = engine.listFacts();

			int decision = 3;
			
			while(iterator.hasNext()) {
				Fact fact = (Fact) iterator.next();
				if(fact.getName().equals("MAIN::decision")) {
					decision = fact.getSlotValue("value").intValue(engine.getGlobalContext());
					break;
				}  
			}
			
			engine.reset();
			
			return decision;
			
				
		}
		
		public void avanca() {
			
			Work_progress++;
			Fuel--;
		
			if(mapa.get_type(destination[Work_progress].x, destination[Work_progress].y) == Constants.GasStation) 
				Fuel = FuelCapacity;
		
		
			if(mapa.get_type(destination[Work_progress].x, destination[Work_progress].y) == Constants.WaterReservoir) 
				Water = WaterCapacity;
		
	
			Curr_posX = destination[Work_progress].x;
			Curr_posY = destination[Work_progress].y;
		
		}
		
		public void onTick() {

				if(State == Constants.StateFree && destination == null) {
					//use jess here
					
					try {
					
						
					
					int decision = AllocateStateTime();
					
					if(decision != 3) {
						
					System.out.println("Tomada de decisao");
					System.out.println("Fuel " + Fuel + " FuelCapacity " + FuelCapacity 
								+ " Water" + Water + " WaterCapacity " + WaterCapacity);
					System.out.println("Decision: " + decision);
					
					}
					
					switch(decision) {
						
					case 0:
						
					Pos[] p_to_wr = Pathfinding.path_nearest_water_reservoir(mapa, Curr_posX, Curr_posY, Fuel, FuelCapacity);
							
					if(p_to_wr != null) {
					
					destination = p_to_wr;
					Work_progress = 0;
							
						
					}
						
					else {
								
					Pos[] p_to_gs = Pathfinding.path_nearest_gas_station(mapa, Curr_posX, Curr_posY, Fuel);
							
					if(p_to_gs != null) {
					
					destination = p_to_gs;
					Work_progress = 0;						
					
						}	
					}
						
					break;
						
					case 1:
						
					Pos[] p_to_gs = Pathfinding.path_nearest_gas_station(mapa, Curr_posX, Curr_posY, Fuel);
							
					if(p_to_gs != null) {
									
					destination = p_to_gs;
					Work_progress = 0;
				
						}
						
					
					break;
					
					default:
					
					break;
					
					}
				}
						
					 catch (JessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
				
			
			
			
				if(State == Constants.StateEmergency && destination != null) {
					
					if(Work_progress < destination.length) {
						
						if(Work_progress == destination.length - 1) {
						
						State = Constants.StateFree;
						Work_progress = 0;
						destination = null;
						register();
						}
						
						else {
							avanca();
						}
					}
					
					
				}
			
				if(State == Constants.StateRepair) {
					
					if(destination != null && veiculo_avariado != null && Work_progress < destination.length) {
						
						if(Work_progress ==  destination.length - 1) {
						
						System.out.println("answered distress call");
						System.out.println(aid_veiculo_avariado.getLocalName());
						ACLMessage msg = new ACLMessage(ACLMessage.CFP);
						msg.setContent("Completed Repair");
						msg.addReceiver(aid_veiculo_avariado);
						send(msg);
						
						veiculo_avariado = null;
						aid_veiculo_avariado = null;
						State = Constants.StateFree;
						
						destination = null;
						Work_progress = 0;
						
						register();
						
						}
						
						else {
							avanca();
						}

						
					}
					
										
				}
			
				if(State == Constants.StateFire){
					
					
					if(destination != null && Work_progress < destination.length ) {
						int i = 0;
						

						if(Work_progress == destination.length - 1) {
							
							Work_progress = 0;
							destination = null;
							
							
								
								
								
								if(incendio_corrente != null) {
									Water--;
									PedidoCompleto();
								}
								
								if(Water > 0) {
									register();
								}
								
								else {
									incendio_corrente = null;
									
									State = Constants.StateBroken;
									destination = Pathfinding.path_nearest_water_reservoir(mapa, Curr_posX, Curr_posX,
											Fuel, FuelCapacity);
									
									if(destination == null) {
										
										try {
											sendDistressCall();
											return ;
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									
									else 
										State = Constants.StateEmergency;
									
									
									
								}
							
							
							State = Constants.StateFree;
						}
						
						else {
						
							
							avanca();

							/*Curr_posX = destination[destination.length - 1].x;
							Curr_posY = destination[destination.length - 1].y;
							*/
						
				
						
							
					}
						
				}
							
					
				}
				
	
		}
	}
				
			
			
		

	public class ReceberDiretivas extends CyclicBehaviour {
		public void action() {
			ACLMessage message = receive();
			
			if(message != null && message.getPerformative() == ACLMessage.CFP) {
				System.out.println("Back in business");
				Fuel  = FuelCapacity;
				Water = WaterCapacity;
				State = Constants.StateFree;
				register();
				
			}
			
			if (State == Constants.StateFree && message != null && message.getPerformative() == ACLMessage.PROPOSE ) {

				try {

					System.out.println("State: " + State);
	
					int cordenada_x = 0;
					int cordenada_y = 0;
					
					if("incendio".equals(message.getOntology())) {
						State = Constants.StateFire;

						Incendio pedido = incendio_corrente = (Incendio) message.getContentObject();
						cordenada_x = (int) pedido.get_Cor_x(); 
						cordenada_y = (int) pedido.get_Cor_y();
						
					}
					
					
					if("veiculo".equals(message.getOntology())) {

						State = Constants.StateRepair;

						DistressCall distress_call = (DistressCall) message.getContentObject();
						PosVehicle pedido = veiculo_avariado = distress_call.pos;
						

						
						aid_veiculo_avariado = distress_call.aid;
						
						System.out.println("veiculo a reparar " + aid_veiculo_avariado.getLocalName());

						
						cordenada_x = (int) pedido.cor_x; 
						cordenada_y = (int) pedido.cor_y;
						
					}
					
					System.out.println("" + Fuel);
					System.out.println(Curr_posX + " : " + Curr_posY);

					Pos[] new_destination = Pathfinding.find_path(mapa,FuelCapacity,Fuel,(int) Curr_posX,(int) Curr_posY,cordenada_x,cordenada_y);


					
					if(new_destination == null) {
						Pos[] can_go_to_gas_station = Pathfinding.path_nearest_gas_station(mapa, Curr_posX, Curr_posY, Fuel);
						
						
						
						if(can_go_to_gas_station == null) {
							
							sendDistressCall();
							State = Constants.StateBroken;
							deregister();
							return ;
							
						}
						
						State = Constants.StateEmergency;
						destination = can_go_to_gas_station;
						
						System.out.println("no fuel");
						ACLMessage reply = message.createReply();
						reply.setPerformative(ACLMessage.REFUSE);
						reply.setContentObject(message.getContentObject());
						send(reply);
						deregister();
						return ;
	
					}
					
					deregister();
					destination = new_destination;
					/*System.out.println(Curr_posX + " : " + Curr_posY);
					System.out.println(cordenada_x + " : " + cordenada_y);
					System.out.println(destination[destination.length - 1].x + " : " + destination[destination.length - 1].y);
					*/
					/*
					System.out.println(destination[0].x == Curr_posX && destination[0].y == Curr_posY);
					System.out.println(Curr_posX + " : " + Curr_posY);
					System.out.println(destination[0].x + " : " +  destination[0].y);

					System.out.println(cordenada_x == destination[destination.length - 1].x &&
							cordenada_y == destination[destination.length - 1].y);
					System.out.println("Nova diretiva");
					*/
					ACLMessage reply = message.createReply();
					
					reply.setPerformative(ACLMessage.AGREE);
					send(reply);
					
				}
				catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
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

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getLocalName());
		sd.setType("Vehicle");
		
		dfd.addServices(sd);
		try {
			
			DFService.deregister(this,dfd);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void takeDown() {

		deregister();
		super.takeDown();
		
	}
	
	}



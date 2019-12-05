package Agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import PathFinding.Pathfinding;
import communication.DistressCall;
import communication.Incendio;
import communication.Mapa;
import communication.PedidoCompleto;
import communication.PosVehicle;
import constants.Constants;
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
import jade.tools.sniffer.Message;
import jess.Context;
import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Userfunction;
import jess.Value;
import jess.ValueVector;

import java.lang.Math;

public class Station extends Agent {
	Station manager = this;
	
	private HashMap<AID,PosVehicle> localizacoes = new HashMap<AID,PosVehicle>(); 
	private HashMap<AID,PosVehicle> DistressCalls = new HashMap<AID,PosVehicle>();
	
	ArrayList<Incendio> incendios;
	ArrayList<Incendio> NAincendios;
	Rete engine;
	Mapa mapa;
	int nr_incendios = 0;
	
	protected void setup() {
		super.setup();

		Object[] args = getArguments();
		this.mapa =  (Mapa) args[0];
		this.incendios   = new ArrayList<Incendio>();
		this.NAincendios = new ArrayList<Incendio>();
		this.DistressCalls = new HashMap<AID,PosVehicle>();
		// Register Agent
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getLocalName());
		sd.setType("Station");
		dfd.addServices(sd);
		
		engine = new Rete();
		
		try {
			engine.batch("src/fire_allocation.clp");
			engine.reset();
		} catch (JessException e) {
			// TODO Auto-genserated catch block
			e.printStackTrace();
		}


		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		
		addBehaviour(new Update());
		addBehaviour(new UpdateInterface(this,1000));
		addBehaviour(new AlocarIncendios(this,100));
	}


	private class AlocarIncendios extends TickerBehaviour {

		public AlocarIncendios(Agent a, long period) {
			super(a, period);
			// TODO Auto-generated constructor stub
		}
		
		
		public int getScore(PosVehicle v,int x,int y) {
			return Constants.distanceWeight * Pathfinding.distancia(v.get_x(), v.get_y(),x, y) / Constants.speedWeight * v.speed 
				   - Constants.fuelWeight * v.fuel  - Constants.fuelCapacityWeight * v.fuel_capacity ;
			
		}
		
		public void addVehicleFact(String name,PosVehicle v,int x,int y,Rete engine) {
			
			int score = getScore(v,x,y);
			
			try {
				
				Fact f = new Fact("vehicle", engine);
				f.setSlotValue("score", new Value(score, RU.INTEGER));
				f.setSlotValue("name", new Value(name, RU.STRING));
				engine.assertFact(f);


			} catch (JessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		public void addFireFact(String name,Rete engine) {
			
			
			try {
				
				Fact f = new Fact("fire", engine);
				f.setSlotValue("name", new Value(name, RU.STRING));
				engine.assertFact(f);


			} catch (JessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		public AID allocate_ressource(int x,int y,ArrayList<DFAgentDescription> results) {
			
			

			HashMap<String,AID> v = new HashMap<String,AID>();
			
			AID best_vehicle = null; 

		
				
			
			for(int i = 0; i < results.size();i++) {
				System.out.println(results.get(i).getName());
			}
				
			for (int d = 0; d < results.size(); d++) {
				// Agent Found
					
				AID vehicle = results.get(d).getName();
					
				PosVehicle cordenada = localizacoes.get(vehicle);
					
					
				if(cordenada != null) 
					v.put(vehicle.toString(),vehicle);
					
					
			}
			
			
			
			
			try {
				
					for(String key: v.keySet()) {
						
						addVehicleFact(key,localizacoes.get(v.get(key)),x,y,engine);
					}
					
					addFireFact("fire",engine);
					
					engine.run();
					
					Iterator iterator = engine.listFacts();

					
					
					while(iterator.hasNext()) {
						Fact fact = (Fact) iterator.next();
						if(fact.getName().equals("MAIN::allocation")) {
							best_vehicle = v.get(fact.getSlotValue("vehiclename"));
							break;
						}
					
					
					
					
					engine.reset();
					
					
					
					}
				
				

				//f.setSlotValue("content", new Value(msg.getContent(), RU.STRING));
				
			} catch (JessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		
			if(best_vehicle != null) {
				results.remove(best_vehicle);
			}
			
			return best_vehicle;
			
		}
		
		public void allocateFire(ArrayList<Incendio> New_NAincendios,int i,ArrayList<DFAgentDescription> results) {
			
			Incendio fire = New_NAincendios.get(i);
			int posX = fire.cor_x;
			int posY = fire.cor_y;
			AID best_vehicle = allocate_ressource(posX,posY,results);
			
			if(best_vehicle != null) {
				
				
				
				ACLMessage new_msg = new ACLMessage(ACLMessage.REQUEST);
				new_msg.addReceiver(best_vehicle);
				try {
					new_msg.setContentObject((Serializable) fire);
					new_msg.setPerformative(ACLMessage.PROPOSE);
					new_msg.setOntology("incendio");
					NAincendios.remove(New_NAincendios.get(i));
					send(new_msg);
					
			

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		}
		
		
		public void allocateDistressCall(HashMap<AID,PosVehicle> New_DistressCalls,AID aid,ArrayList<DFAgentDescription> results) {
			
			PosVehicle vehicle = New_DistressCalls.get(aid);
			int posX = vehicle.cor_x;
			int posY = vehicle.cor_y;
			AID best_vehicle = allocate_ressource(posX,posY,results);
			
			System.out.println("answering distress call");
			if(best_vehicle != null) {
				
				System.out.println("distress call " + best_vehicle.getLocalName());

				
				ACLMessage new_msg = new ACLMessage(ACLMessage.PROPOSE);
				new_msg.addReceiver(best_vehicle);
				try {
					new_msg.setContentObject((Serializable) vehicle);
					new_msg.setOntology("veiculo");
					new_msg.setContentObject(new DistressCall(vehicle,aid));
					DistressCalls.remove(aid);
					send(new_msg);
					
			

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		}
		
		@Override
		protected void onTick() {
			// TODO Auto-generated method stub
			
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription(); 
		    sd.setType("vehicle");
			
			dfd.addServices(sd);
			
			ArrayList<DFAgentDescription> results = new ArrayList<DFAgentDescription>();
			
			try {
				Collections.addAll(results, DFService.search(this.myAgent, dfd));
			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			
			HashMap<AID,PosVehicle> New_DistressCalls = (HashMap<AID,PosVehicle>) DistressCalls.clone(); 
			
			for(AID aid:New_DistressCalls.keySet()) {
				System.out.println(aid.getLocalName());
				allocateDistressCall(New_DistressCalls,aid,results);
			}
			
			ArrayList<Incendio> New_NAincendios = (ArrayList<Incendio>) NAincendios.clone(); 
			New_NAincendios.sort((i1,i2) -> i1.gravity < i2.gravity  ? 1:0 );
			
			for(int i = 0; i < New_NAincendios.size();i++) {
			
					allocateFire(New_NAincendios,i,results);
				}
			

			
			
			
				
			}
			
		}	
	

	private class Update extends CyclicBehaviour {
		
		public void action() {
			ACLMessage msg = receive();

			if (msg != null ) { // receber atualizações				
				//System.out.println(msg.getSender());
				try {
					
					if(msg.getPerformative() == ACLMessage.AGREE) {
						System.out.println("" + msg.getSender().getLocalName() + " starting work");
					}
					
					else if(msg.getPerformative() == ACLMessage.REFUSE) {
						
						if("incendio".equals(msg.getOntology())) {
						System.out.println(msg.getSender().getLocalName() + "refused to put fire down");
						Incendio incendio = (Incendio) msg.getContentObject();
						NAincendios.add(incendio);
						}
						
						if("veiculo".equals(msg.getOntology())) {
							DistressCall dc = (DistressCall) msg.getContentObject();
							DistressCalls.put(dc.aid, dc.pos);
						}
				
					}
					
					
					else if(msg.getPerformative() == ACLMessage.INFORM) {
						atualizarCordenadas(msg);
					}
					
					
					else if(msg.getPerformative() == ACLMessage.CONFIRM) {
						System.out.println("pedido completo " + msg.getSender().getLocalName());
						Incendio apagado = (Incendio) msg.getContentObject();
						incendios.remove(apagado);
					}
					
					else if(msg.getPerformative() == ACLMessage.PROPOSE) {
						new_fire(msg);
						
					}

					
					else if(msg.getPerformative() == ACLMessage.REQUEST) {
						PosVehicle pos = (PosVehicle) msg.getContentObject();
						DistressCalls.put(msg.getSender(), pos);
						System.out.println(msg.getSender().getLocalName() + " precisa de ajuda");
					}
					
					
					
					
					
					block();
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				}
				}
		
		public void new_fire(ACLMessage msg) {
			
			try {
				
				Incendio incendio = (Incendio) msg.getContentObject();
				nr_incendios++;
				NAincendios.add(incendio);
				incendios.add(incendio);
				
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		public void atualizarCordenadas(ACLMessage msg) throws UnreadableException {
			PosVehicle cordenada =  (PosVehicle) msg.getContentObject();
			
			AID sender = msg.getSender();
			localizacoes.put(sender, cordenada);
			//System.out.println("Coordenas atualizadas " + msg.getSender() );

		}
		
	}
	
private class UpdateInterface extends TickerBehaviour {

		
		public UpdateInterface(Agent agent,int time) {
			super(agent,time);
		
		}
		
		public void AtualizarInterface() {
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			AID reader = new AID("Interface", AID.ISLOCALNAME);
			msg.addReceiver(reader);
			try {
				msg.setOntology("info_fires");
				Incendio[] ArrayIncendios = new Incendio[incendios.size()];
				ArrayIncendios = incendios.toArray(ArrayIncendios);
				msg.setContentObject((Serializable) ArrayIncendios);
				send(msg);	
				msg.setOntology("info_loc");
				msg.setContentObject((Serializable) localizacoes);
				send(msg);
				
				msg.setOntology("info_nr_fires");
				msg.setContentObject(nr_incendios);
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


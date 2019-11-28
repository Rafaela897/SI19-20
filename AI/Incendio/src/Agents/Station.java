package Agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import PathFinding.Pathfinding;
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
	ArrayList<Incendio> incendios;
	ArrayList<Incendio> NAincendios;
	Rete engine;
	Mapa mapa;
	
	protected void setup() {
		super.setup();

		Object[] args = getArguments();
		this.mapa =  (Mapa) args[0];
		this.incendios   = new ArrayList<Incendio>();
		this.NAincendios = new ArrayList<Incendio>();
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
		addBehaviour(new AlocarIncendios(this,200));
	}


	private class AlocarIncendios extends TickerBehaviour {

		public AlocarIncendios(Agent a, long period) {
			super(a, period);
			// TODO Auto-generated constructor stub
		}
		
		
		public int getScore(PosVehicle v,Incendio fire) {
			return Pathfinding.distancia(v.get_x(), v.get_y(),fire.cor_x, fire.cor_y) / v.speed 
				   - v.fuel - v.fuel_capacity ;
			
		}
		
		public void addVehicleFact(String name,PosVehicle v,Incendio fire,Rete engine) {
			
			int score = getScore(v,fire);
			
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
		
		public AID allocate_fire(Incendio fire) {
			int incendioX,incendioY;
			
			DFAgentDescription dfd = new DFAgentDescription();
			
			
			

			HashMap<String,AID> v = new HashMap<String,AID>();
			
			AID best_vehicle = null; 

			try {
				
				DFAgentDescription[] results = DFService.search(this.myAgent, dfd);
				for (int d = 0; d < results.length; d++) {
					// Agent Found
					
					AID vehicle = results[d].getName();
					
					PosVehicle cordenada = localizacoes.get(vehicle);
					
					
					if(cordenada != null) 
						v.put(vehicle.toString(),vehicle);
					
					
				}
			
			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			try {
				
					for(String key: v.keySet()) {
						
						addVehicleFact(key,localizacoes.get(v.get(key)),fire,engine);
					}
					
					addFireFact("fire",engine);
					
					engine.run();
					
					Iterator iterator = engine.listFacts();

					
					
					while(iterator.hasNext()) {
						Fact fact = (Fact) iterator.next();
						if(fact.getName().equals("MAIN::allocation")) {
							System.out.println(v.get(fact.getSlotValue("vehiclename"))+ "");
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
		
		
			
			return best_vehicle;
			
		}
		
		@Override
		protected void onTick() {
			// TODO Auto-generated method stub
			

			NAincendios.sort((i1,i2) -> i1.gravity < i2.gravity  ? 1:0 );
			
			ArrayList<Incendio> New_NAincendios = (ArrayList<Incendio>) NAincendios.clone(); 
			
			for(int i = 0; i < New_NAincendios.size();i++) {
			

			Incendio fire = New_NAincendios.get(i);

			AID best_vehicle = allocate_fire(fire);
			
			if(best_vehicle != null) {
				
				System.out.println("starting work");
				
				
				ACLMessage new_msg = new ACLMessage(ACLMessage.REQUEST);
				new_msg.addReceiver(best_vehicle);
				try {
					new_msg.setContentObject((Serializable) fire);
					new_msg.setOntology("job");
					NAincendios.remove(New_NAincendios.get(i));
					send(new_msg);


				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				}
				
			}
			
		}	
	}

	private class Update extends CyclicBehaviour {
		
		public void action() {
			ACLMessage msg = receive();

			if (msg != null ) { // receber atualizações				
				//System.out.println(msg.getSender());
				try {
					String ontology = msg.getOntology();
					if(ontology.equals("fires")) {
						new_fire(msg);
						
					}

					if(ontology.equals("coordenadas")) {
						atualizarCordenadas(msg);
					}
					
					if(ontology.equals("pedido")) {
						System.out.println("pedido completo");
						Incendio apagado = (Incendio) msg.getContentObject();
						incendios.remove(apagado);
					}
					
					if(ontology.equals("morte")) {
						System.out.println("Veiculo " + msg.getSender() + " não está em condições de operar");
						localizacoes.remove(msg.getSender());
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
			
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
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


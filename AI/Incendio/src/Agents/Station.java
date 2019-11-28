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
		
		/*
		public class getScore implements Userfunction
		{
			public String getName() {return "getDistance";}
			
			public Value call(ValueVector vv,Context context) throws JessException
			{
				return new Value(getDistance(vv.get(1).intValue(context),vv.get(2).intValue(context),
						vv.get(3).intValue(context),vv.get(4).intValue(context)),RU.INTEGER);
			}
		}
		
		public int getDistance(int x1,int y1,int x2,int y2) throws JessException {
			
				return  Pathfinding.distancia(x1, y1, x2, y2);
			
		}
		*/
		
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
		
		@Override
		protected void onTick() {
			// TODO Auto-generated method stub
			

			NAincendios.sort((i1,i2) -> i1.gravity < i2.gravity  ? 1:0 );
			
			int incendioX,incendioY;
			
			DFAgentDescription dfd = new DFAgentDescription();
			
			
			

			HashMap<String,AID> v = new HashMap<String,AID>();
			
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
				
				ArrayList<Incendio> New_NAincendios = (ArrayList<Incendio>) NAincendios.clone(); 
				
				for(int i = 0; i < NAincendios.size();i++) {
					
					
					Incendio fire = NAincendios.get(i);
					
					for(String key: v.keySet()) {
						addVehicleFact(key,localizacoes.get(v.get(key)),fire,engine);
					}
					
					
					
					
					addFireFact("fire",engine);
					
					
					engine.run();
					//engine.eval("(facts)");
					
					
					
					Iterator iterator = engine.listFacts();

					
					AID best_vehicle = null; 
					while(iterator.hasNext()) {
						Fact fact = (Fact) iterator.next();
						if(fact.getName().equals("MAIN::allocation")) {
							System.out.println(v.get(fact.getSlotValue("vehiclename"))+ "");
							best_vehicle = v.get(fact.getSlotValue("vehiclename"));
							v.remove(best_vehicle);
							break;
						}
					}
					
					
					if(best_vehicle != null) {
						
					System.out.println("starting work");
					
					
					ACLMessage new_msg = new ACLMessage(ACLMessage.REQUEST);
					new_msg.addReceiver(best_vehicle);
					new_msg.setContentObject((Serializable) fire);
					new_msg.setOntology("job");
					send(new_msg);
					
					New_NAincendios.remove(fire);
					
					}
					
					engine.reset();
					
					
					
				}
				
				NAincendios = New_NAincendios;
				

				//f.setSlotValue("content", new Value(msg.getContent(), RU.STRING));
				
			} catch (JessException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
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
						System.out.println("new fire");
						new_fire(msg);
						
					}

					if(ontology.equals("coordenadas")) {
						atualizarCordenadas(msg);
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
		
		public void new_incendio(ACLMessage msg) {
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

								ACLMessage new_msg = new ACLMessage(ACLMessage.REQUEST);
								new_msg.addReceiver(provider);
								new_msg.setContentObject((Serializable) incendio);
								new_msg.setOntology("job");
								send(new_msg);
								
					}
					
					if(melhor_taxi == -1) {
						NAincendios.add(incendio);
					}
							
						}
				
				if(results.length <= 0) {
					NAincendios.add(incendio);
				}
					
				incendios.add(incendio);
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


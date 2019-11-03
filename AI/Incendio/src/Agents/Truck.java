package Agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import communication.PedidoCompleto;
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

public class Truck extends Vehicle {
	
	
	protected void setup() {
		super.setup();

		this.Fuel = this.FuelCapacity  = Constants.TruckFuelCapacity;
		this.Water = this.WaterCapacity = Constants.TruckWaterCapacity;
		this.Velocity = Constants.AircraftVelocity;
		
		
		

	}

	
			
			
	}		

	


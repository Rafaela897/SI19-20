package constants;

public final class Constants {
	
	private Constants() {
		
	}
	
	
	public static final int TruckFuelCapacity  = 15 ;
	public static final int TruckWaterCapacity = 10 ;
	public static final int TruckVelocity      = 1 ;
	
	
	public static final int DroneFuelCapacity  = 10 ;
	public static final int DroneWaterCapacity = 2 ;
	public static final int DroneVelocity      = 4 ;
	

	public static final int AircraftFuelCapacity  = 20 ;
	public static final int AircraftWaterCapacity = 15 ;
	public static final int AircraftVelocity      = 2;
	
	public static final int RuralZone       = 0;
	public static final int ResidentialZone = 1;
	public static final int GasStation      = 2;
	public static final int FireStation     = 3;
	public static final int WaterReservoir  = 4;

	public static final int Nr_Trucks    =  2;
	public static final int Nr_Drones    =  2;
	public static final int Nr_Aircrafts =  2; 
	
	public static final int Nr_GasStationCells  = 10;
	public static final int Nr_ResidentialCells = 20;
	public static final int Nr_FireStationCells = 1;
	public static final int Nr_WaterReservoirs = 20;
	
	public static final int SizeX = 20;
	public static final int SizeY = 20;
	
	public static final int StateFree = 0;
	public static final int StateFire = 1;
	public static final int StateBroken = 2;
	public static final int StateRepair = 3;
	public static final int StateEmergency = 4;
	
	public static final int distanceWeight = 40;
	public static final int fuelWeight = 5;
	public static final int fuelCapacityWeight = 1;
	public static final int speedWeight = 1;
	
	
	public static String statistics_path = "C:\\Users\\hugoa\\Desktop\\";
	
}

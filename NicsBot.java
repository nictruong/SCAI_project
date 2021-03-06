import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;

public class NicsBot extends DefaultBWListener {

    private Mirror mirror = new Mirror();

    private Game game;

    private Player self;
    
    private WorkersManager workersManager;
    
    private BuildingManager buildingManager;

    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }

   /* @Override
	public void onUnitCreate(Unit unit) {
	    System.out.println("New unit discovered " + unit.getType());
	}

	@Override
    public void onUnitCreate(Unit unit) {
        System.out.println("New unit discovered " + unit.getType());
    }*/

    @Override
    public void onStart() {
        game = mirror.getGame();
        self = game.self();
        workersManager = WorkersManager.getInstance(self, game);
        buildingManager = BuildingManager.getInstance(self, game);
        
        // CHEATS
        game.sendText("black sheep wall");
        game.setLocalSpeed(5);

        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");
    }

    @Override
    public void onFrame() {
        //game.setTextSize(10);
        game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());

        StringBuilder units = new StringBuilder("My units:\n");
        
        // change the first condition to be scalable into late game
        if (self.supplyTotal() + workersManager.getQueuedSupply() - self.supplyUsed() <= 4) {
			workersManager.addToQueue(UnitType.Terran_Supply_Depot);
		}
		
		if (self.supplyUsed() > 20 && (float)(buildingManager.getBuildingCount(UnitType.Terran_Barracks) + buildingManager.getQueuedBuildingCount(UnitType.Terran_Barracks)) / (float)workersManager.getWorkerCount() < 0.10) {
			workersManager.addToQueue(UnitType.Terran_Barracks);
		}
		
		buildingManager.onFrame();
		workersManager.onFrame();

        //iterate through my units
		// TO BE CHANGED. IDLING WORKERS BEHAVIOUR TO BE MOVED TO WORKERSMANAGER
		// TRAINING WORKERS TO BE MOVED TO BASEMANAGER
        for (Unit myUnit : self.getUnits()) {
            units.append(myUnit.getType()).append(" ").append(myUnit.getTilePosition()).append("\n");

            //if there's enough minerals, train an SCV
            if (myUnit.getType() == UnitType.Terran_Command_Center && self.minerals() >= 50 && myUnit.getTrainingQueue().size() == 0) {
                myUnit.train(UnitType.Terran_SCV);
            }

            //if it's a worker and it's idle, send it to the closest mineral patch
            if (myUnit.getType().isWorker() && myUnit.isIdle()) {
                Unit closestMineral = null;

                //find the closest mineral
                for (Unit neutralUnit : game.neutral().getUnits()) {
                    if (neutralUnit.getType().isMineralField()) {
                        if (closestMineral == null || myUnit.getDistance(neutralUnit) < myUnit.getDistance(closestMineral)) {
                            closestMineral = neutralUnit;
                        }
                    }
                }

                //if a mineral patch was found, send the worker to gather it
                if (closestMineral != null) {
                    myUnit.gather(closestMineral, false);
                }
            }            
        }
        
        // Draw the map features
        drawFeatures();
        

        //draw my units on screen
        game.drawTextScreen(10, 25, units.toString());
    }
    
    public void onUnitComplete(Unit unit) {
    	// buildingManager contains a list of all my active production buildings
    	if (unit.getType() == UnitType.Terran_Barracks) {
    		buildingManager.addBuilding(unit);
    	}
    }
    
    public void onUnitDestroy(Unit unit) {
    	// remove buildings from the manager if they got destroyed
    	if (unit.getType() == UnitType.Terran_Barracks) {
    		buildingManager.removeBuilding(unit);
    	}
    }
    
    private void drawFeatures() {
    	MapDrawer mapDrawer = MapDrawer.getInstance(game);    
    	mapDrawer.drawRegions();
        mapDrawer.drawStartingBases();
    }

    public static void main(String[] args) {
        new NicsBot().run();
    }
}
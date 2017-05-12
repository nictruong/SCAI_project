import java.util.ArrayList;
import java.util.List;

import bwapi.Game;
import bwapi.Player;
import bwapi.Unit;
import bwapi.UnitType;

public class BuildingManager {	
	
	private static BuildingManager buildingManager = null;
	
	private Player self;
	private Game game;
	
	private List<Unit> buildings = new ArrayList<>();
	
	protected BuildingManager(Player self, Game game) {
		this.self = self;
		this.game = game;
	}
	
	public static BuildingManager getInstance(Player self, Game game) {
		if (buildingManager == null) {
			buildingManager = new BuildingManager(self, game);
		}
		
		return buildingManager;
	}
	
	public void onFrame() {
		for (Unit building : buildings) {
			if (building.getType() == UnitType.Terran_Barracks) {
				building.train(UnitType.Terran_Marine);
			}
		}
	}

	public List<Unit> getBuildings() {
		return buildings;
	}
	
	public void addBuilding(Unit unit) {
		buildings.add(unit);
	}
	
	public void removeBuilding(Unit unit) {
		buildings.remove(unit);
	}
	
	public int getBuildingCount(UnitType unitType) {
		
		int count = 0;
		
		for (Unit building : buildings) {
			if (building.getType() == unitType) {
				count++;
			}
		}
		
		return count;
	}
	
	public int getQueuedBuildingCount(UnitType unitType) {
		WorkersManager workersManager = WorkersManager.getInstance(self, game);
		
		int count = 0;
		
		for (QueuedBuilding queuedBuilding : workersManager.getQueue()) {
			if (queuedBuilding.getUnitType() == unitType) {
				count++;
			}
		}
		
		return count;
	}
}

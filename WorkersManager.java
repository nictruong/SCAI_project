import java.util.ArrayList;
import java.util.List;

import bwapi.Game;
import bwapi.Order;
import bwapi.Player;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;

public class WorkersManager {
		
	private static WorkersManager workers = null;
	
	private Player self = null;
	private Game game = null;
	
	private List<QueuedBuilding> buildingQueue = new ArrayList<>();
	
	protected WorkersManager(Player self, Game game) {
		this.self = self;
		this.game = game;
	}
	
	public static WorkersManager getInstance(Player self, Game game) {
		if (workers == null) {
			workers = new WorkersManager(self, game);
		}
		
		return workers;
	}
	
	public void onFrame() {
		for (int i=0; i<buildingQueue.size(); i++) {
			
			QueuedBuilding queuedBuilding = buildingQueue.get(i);
			
			if (queuedBuilding.getBuilder() == null && queuedBuilding.getStatus() == QueueStatus.NOT_STARTED) {
				Unit freeWorker = getWorker();				
				TilePosition buildTile = getFreeTile(queuedBuilding.getUnitType(), freeWorker);
				freeWorker.build(queuedBuilding.getUnitType(), buildTile);	
				queuedBuilding.setBuilder(freeWorker);
				queuedBuilding.setLocation(buildTile);
				
				buildingQueue.set(i, queuedBuilding);
			}
		}
		
		updateQueue();
	}
	
	public void updateQueue() {
		for (int i=0; i<buildingQueue.size(); i++) {
			QueuedBuilding queuedBuilding = buildingQueue.get(i);
			
			if (queuedBuilding.getBuilder().getOrder() == Order.ConstructingBuilding) {				
				if (queuedBuilding.getStatus() == QueueStatus.NOT_STARTED && queuedBuilding.getUnitType() == UnitType.Terran_Supply_Depot) {
					queuedBuilding.setStatus(QueueStatus.DOING);
					buildingQueue.set(i, queuedBuilding);
				}
			}
			
			if (queuedBuilding.getUnitType() == UnitType.Terran_Supply_Depot) {
				Unit myUnit;
				for (Unit unit : game.getUnitsOnTile(queuedBuilding.getLocation())) {
					if (unit.getType() == UnitType.Terran_Supply_Depot && unit.isCompleted()) {
						buildingQueue.remove(i);
						break;
					}
				}
			}
		}
	}
	
	
	// Find a free tile to be built on by the freeWorker
	// should be moved somewhere else, nothing to do with workers
	public TilePosition getFreeTile(UnitType unitType, Unit freeWorker) {
		return game.getBuildLocation(unitType, freeWorker.getTilePosition());
	}
	
	// should be moved somewhere else, nothing to do with workers
	public Boolean canAfford(UnitType building) {
		if (self.minerals() >= building.mineralPrice() && self.gas() >= building.gasPrice()) {
			return true;
		}
		
		return false;
	}
	
	// get the supply count that has not yet been built
	public int getQueuedSupply() {
		int s = 0;
		for (QueuedBuilding building : buildingQueue) {
			if (building.getUnitType() == UnitType.Terran_Supply_Depot) {
				s += 16;
			}else if (building.getUnitType() == UnitType.Terran_Command_Center) {
				s += 20;
			}
		}
		
		return s;
	}
	
	// add a job for any worker
	public void addToQueue(UnitType unit) {
		buildingQueue.add(new QueuedBuilding(unit, null, null));
	}
	
	// Return a mineral gathering worker that is not currently queued to build anything
	private Unit getWorker() {
		for (Unit myUnit : self.getUnits()) {
			if (myUnit.getType() == UnitType.Terran_SCV && myUnit.isGatheringMinerals() && !myUnit.isCarryingMinerals() && !myUnit.isCarryingGas()) {
				for (QueuedBuilding queuedBuilding : buildingQueue) {
					if (myUnit == queuedBuilding.getBuilder()) {
						continue;
					}
				}
				
				return myUnit;
			}
		}
		
		return null;
	}
}

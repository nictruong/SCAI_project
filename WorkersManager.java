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
	
	private List<QueuedBuildingUnit> buildingQueue = new ArrayList<>();
	
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
	
	public void buildOrder() {		
		if (self.supplyTotal() + getQueuedSupply() - self.supplyUsed() <= 4 && canAfford(UnitType.Terran_Supply_Depot)) {
			build(UnitType.Terran_Supply_Depot);
		}
		
		if (self.supplyUsed() == 22 && canAfford(UnitType.Terran_Barracks)) {
			build(UnitType.Terran_Barracks);
		}
	}
	
	public Boolean build(UnitType unitType) {
		Unit freeWorker = getWorker();
		TilePosition buildTile = getBuildTile(freeWorker, unitType, self.getStartLocation());
		
		for (QueuedBuildingUnit queuedBuilding : buildingQueue) {
			if (queuedBuilding.getLocation().getX() == buildTile.getX() && queuedBuilding.getLocation().getY() == buildTile.getY()) {
				return false;
			}
		}
		
		if (freeWorker.build(unitType, buildTile)) {
			buildingQueue.add(new QueuedBuildingUnit(unitType, freeWorker, buildTile));
			return true;
		}
		
		return false;
	}
	
	public void updateQueue() {
		for (int i=0; i<buildingQueue.size(); i++) {
			QueuedBuildingUnit queuedBuilding = buildingQueue.get(i);
			
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
	
	private Boolean canAfford(UnitType building) {
		if (self.minerals() >= building.mineralPrice() && self.gas() >= building.gasPrice()) {
			return true;
		}
		
		return false;
	}
	
	private int getQueuedSupply() {
		int s = 0;
		for (QueuedBuildingUnit building : buildingQueue) {
			if (building.getUnitType() == UnitType.Terran_Supply_Depot) {
				s += 16;
			}
		}
		
		return s;
	}
	
	// Return a mineral gathering worker that is not currently queued to build anything
	private Unit getWorker() {
		for (Unit myUnit : self.getUnits()) {
			if (myUnit.getType() == UnitType.Terran_SCV && myUnit.isGatheringMinerals() && !myUnit.isCarryingMinerals() && !myUnit.isCarryingGas()) {
				for (QueuedBuildingUnit queuedBuilding : buildingQueue) {
					if (myUnit == queuedBuilding.getBuilder()) {
						continue;
					}
				}
				
				return myUnit;
			}
		}
		
		return null;
	}
	
	public TilePosition getBuildTile(Unit builder, UnitType buildingType, TilePosition aroundTile) {
		TilePosition ret = null;
		int maxDist = 2;
		int stopDist = 40;

		// Refinery, Assimilator, Extractor
		if (buildingType.isRefinery()) {
			for (Unit n : this.game.neutral().getUnits()) {
				if ((n.getType() == UnitType.Resource_Vespene_Geyser) &&
						( Math.abs(n.getTilePosition().getX() - aroundTile.getX()) < stopDist ) &&
						( Math.abs(n.getTilePosition().getY() - aroundTile.getY()) < stopDist )
						) return n.getTilePosition();
			}
		}

		while ((maxDist < stopDist) && (ret == null)) {
			for (int i=aroundTile.getX()-maxDist; i<=aroundTile.getX()+maxDist; i++) {
				for (int j=aroundTile.getY()-maxDist; j<=aroundTile.getY()+maxDist; j++) {
					if (this.game.canBuildHere(new TilePosition(i,j), buildingType, builder, false)) {
						// units that are blocking the tile
						boolean unitsInWay = false;
						for (Unit u : this.game.getAllUnits()) {
							if (u.getID() == builder.getID()) continue;
							if ((Math.abs(u.getTilePosition().getX()-i) < 4) && (Math.abs(u.getTilePosition().getY()-j) < 4)) unitsInWay = true;
						}
						if (!unitsInWay) {
							return new TilePosition(i, j);
						}
					}
				}
			}
			maxDist += 2;
		}

		if (ret == null) this.game.printf("Unable to find suitable build position for "+buildingType.toString());
		return ret;
	}
}

import java.util.ArrayList;
import java.util.List;

import bwapi.Game;
import bwapi.Order;
import bwapi.Player;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

public class Workers {
	
	private enum QueueStatus {
		NOT_STARTED,
		DOING,
		COMPLETED,
	}
	
	private static Workers workers = null;
	
	private Player self = null;
	private Game game = null;
	
	private List<QueuedBuildingUnit> buildingQueue = new ArrayList<>();
	
	public class QueuedBuildingUnit {
		private UnitType unitType;
		private Unit builder;
		private TilePosition location;
		private QueueStatus status = QueueStatus.NOT_STARTED;
		
		public QueuedBuildingUnit(UnitType unitType, Unit builder, TilePosition location) {
			this.unitType = unitType;
			this.builder = builder;
			this.location = location;
		}
		
		public UnitType getUnitType() {
			return unitType;
		}
		public void setUnitType(UnitType unitType) {
			this.unitType = unitType;
		}
		public Unit getBuilder() {
			return builder;
		}
		public void setBuilder(Unit builder) {
			this.builder = builder;
		}
		public TilePosition getLocation() {
			return location;
		}
		public void setLocation(TilePosition location) {
			this.location = location;
		}

		public QueueStatus getStatus() {
			return status;
		}

		public void setStatus(QueueStatus status) {
			this.status = status;
		}
	}
	
	protected Workers(Player self, Game game) {
		this.self = self;
		this.game = game;
	}
	
	public static Workers getInstance(Player self, Game game) {
		if (workers == null) {
			workers = new Workers(self, game);
		}
		
		return workers;
	}
	
	public void buildOrder() {
		if (self.supplyTotal() == 20 && self.supplyUsed() == 18 && (self.minerals() >= 100)) {			
			build(UnitType.Terran_Supply_Depot);
		}
	}
	
	public Boolean build(UnitType unitType) {
		Unit freeWorker = getWorker();
		TilePosition buildTile = getBuildTile(freeWorker, unitType, self.getStartLocation());
		
		for (QueuedBuildingUnit queuedBuilding : buildingQueue) {
			if (queuedBuilding.location.getX() == buildTile.getX() && queuedBuilding.location.getY() == buildTile.getY()) {
				return false;
			}
		}
		
		if (freeWorker.build(unitType, buildTile)) {
			buildingQueue.add(new QueuedBuildingUnit(unitType, freeWorker, buildTile));
			return true;
		}
		
		return false;
	}
	
	private void updateQueue() {
		for (int i=0; i<buildingQueue.size(); i++) {
			QueuedBuildingUnit queuedBuilding = buildingQueue.get(i);
			
			if (queuedBuilding.getBuilder().getOrder() != Order.PlaceBuilding && queuedBuilding.getBuilder().getOrder() != Order.ConstructingBuilding) {
				queuedBuilding.status = QueueStatus.DOING;
				buildingQueue.set(i, queuedBuilding);
			}
		}
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
		int maxDist = 3;
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

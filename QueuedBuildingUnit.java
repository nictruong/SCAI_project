import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

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
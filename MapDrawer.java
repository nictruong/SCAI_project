import java.util.List;

import bwapi.Color;
import bwapi.Game;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.UnitSizeType;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;

public class MapDrawer {

	private static MapDrawer mapDrawer = null;
	private static Game game = null;
	
	protected MapDrawer(Game game) {
		MapDrawer.game = game;
	}
	
	public static MapDrawer getInstance(Game game) {
		if (mapDrawer == null) {
			mapDrawer = new MapDrawer(game);
		}
		
		return mapDrawer;
	}
	
	public void drawRegions() {
		for(bwta.Region region : BWTA.getRegions()) {
        	List<Position> points = region.getPolygon().getPoints();
        	
        	for (int i = 0; i < points.size() - 1; i++) {
        		MapDrawer.game.drawLineMap(points.get(i).getX(), points.get(i).getY(), points.get(i + 1).getX(), points.get(i + 1).getY(), Color.Green);
        	}
        }
	}
	
	public void drawStartingBases() {
		for(BaseLocation base : BWTA.getBaseLocations()) {
			if (base.isStartLocation()) {
				
				int left = base.getX() - UnitType.Terran_Command_Center.dimensionLeft();
				int right = base.getX() + UnitType.Terran_Command_Center.dimensionRight();
				int top = base.getY() + UnitType.Terran_Command_Center.dimensionUp();
				int bottom = base.getY() - UnitType.Terran_Command_Center.dimensionDown();
				
				Position leftTop = new Position(left, bottom);
				Position rightBottom = new Position(right, top);
				
				MapDrawer.game.drawBoxMap(leftTop, rightBottom, Color.Blue);			
			}
		}
	}
}

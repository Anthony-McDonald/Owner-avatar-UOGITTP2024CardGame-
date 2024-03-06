package allCards;

import java.util.ArrayList;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Avatar;
import structures.basic.BigCard;
import structures.basic.Creature;
import structures.basic.MiniCard;
import structures.basic.MoveableUnit;
import structures.basic.OpeningGambit;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.UnitCommands;

public class NightsorrowAssassin extends Creature implements OpeningGambit{

    public NightsorrowAssassin(int id, String cardname, int manacost, MiniCard miniCard, BigCard bigCard, boolean isCreature,  String unitConfig) {
        super(id, cardname, manacost, miniCard, bigCard, isCreature, unitConfig);
        this.userOwned = true;
        this.attack = 4;
        this.currentHealth = 2;
        this.maxHealth = currentHealth;
    }

	@Override
	public void openingGambit(ActorRef out, GameState gameState) {
		MoveableUnit enemyUnit = null;
		Tile unitTile = this.getTile();
		ArrayList<Tile> adjacentTiles = UnitCommands.adjacentTiles(unitTile, gameState);
		//ArrayList<MoveableUnit> AiUnits = gameState.getBoard().friendlyUnits(false);
		for (Tile Tile :adjacentTiles) {
			if(Tile != null) {
				if(Tile.getUnit() instanceof Creature) {
					enemyUnit = Tile.getUnit();
					if(enemyUnit.isUserOwned()==false)
						if (enemyUnit.getCurrentHealth()< enemyUnit.getMaxHealth()) {
							enemyUnit.setCurrentHealth(0, out, gameState);
						}
				}
			}
		}
		
	}
}

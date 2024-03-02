package utils;

import akka.actor.ActorRef;
import allCards.SaberspineTiger;
import allCards.YoungFlamewing;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.*;

import java.util.ArrayList;

public class UnitCommands {
    public static void attackUnit(MoveableUnit attacker, ActorRef out, Tile tile, GameState gameState) {
        MoveableUnit m = tile.getUnit();
        //insert logic about if attack is possible.
        if (isProvokeAdjacent(attacker,gameState)){
            // if provoke is adjacent
            if (! (m instanceof Provoke)){
                BasicCommands.addPlayer1Notification(out, "Unit can only attack Provokers", 3);
                return; //ends attack logic
            }

        }

        if (attacker.getLastTurnAttacked() != gameState.getTurnNumber()) {
            if (canAttack(attacker, tile, gameState)) {
                attacker.setLastTurnAttacked(gameState.getTurnNumber());
                int enemyHealth = m.getCurrentHealth();
                BasicCommands.playUnitAnimation(out, attacker.getUnit(), UnitAnimationType.attack); //attack animation
                enemyHealth = enemyHealth - attacker.getAttack();
                m.setCurrentHealth(enemyHealth, out,gameState);
                gameState.getBoard().renderBoard(out); //resets board
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
                BasicCommands.playUnitAnimation(out, attacker.getUnit(), UnitAnimationType.idle);
                if (enemyHealth > 0) { //if enemy is alive, counterattack
                    BasicCommands.playUnitAnimation(out, m.getUnit(), UnitAnimationType.attack);//unit attack animation
                    attacker.setCurrentHealth((attacker.getCurrentHealth() - m.getAttack()), out,gameState);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    BasicCommands.playUnitAnimation(out, m.getUnit(), UnitAnimationType.idle);

                }
                gameState.setLastMessage(GameState.noEvent);
            } else {
                //attack not possible on this unit, inform user.
                //
                BasicCommands.addPlayer1Notification(out, "Unit can't attack there!", 3);
            }
        }else{
            //already attacked this turn
            gameState.setLastMessage(GameState.noEvent);
        }
    }

    public static boolean canAttack (MoveableUnit attacker, Tile targetTile, GameState gameState){
        Tile currentTile = attacker.getTile();
        Board board = gameState.getBoard();
        if (isProvokeAdjacent(attacker, gameState)){
            if (targetTile.getUnit()!= null){
                MoveableUnit unit = targetTile.getUnit();
                if (!(unit instanceof Provoke) && unit.isUserOwned()!= attacker.isUserOwned()){
                    System.out.println("can't attack, being provoked");
                    return false;
                }
            }
        }
        int xPos = currentTile.getTilex();
        int yPos = currentTile.getTiley();
        for (int i = xPos - 1; i<=xPos+1;i++){ // i is x
            for (int j = yPos -1 ; j<=yPos+1;j++){ // j is y
                if ( 0<=i && i<=8 && 0<=j && j<=4 ){ //if coord in board range
                    Tile highlightTile = board.getTile(i,j);
                    if (highlightTile.getUnit()!=null && highlightTile.getUnit().isUserOwned()!=attacker.isUserOwned()){
                        //if tile has unit and unit is enemy
                        if (targetTile.equals(highlightTile) ){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void moveUnit(MoveableUnit mover, ActorRef out, Tile tile, GameState gameState) {
        //insert logic about if move can occur
        if (gameState.getTurnNumber()!= mover.getLastTurnMoved()) { //hasn't moved this turn
            if (isProvokeAdjacent(mover,gameState)){ //is provoked
                BasicCommands.addPlayer1Notification(out, "Unit can't move, it's provoked.", 3);
                return;
            }
            if (canMove(mover, tile, gameState.getBoard())) {
                mover.setLastTurnMoved(gameState.getTurnNumber());
                gameState.setLastMessage(GameState.noEvent);
                mover.getTile().setUnit(null);
                tile.setUnit(mover); //sets unit on tile in backend
                gameState.getBoard().renderBoard(out);
                BasicCommands.addPlayer1Notification(out, "Moving unit", 3);
                BasicCommands.moveUnitToTile(out, mover.getUnit(), tile);
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                BasicCommands.addPlayer1Notification(out, "Unit can't move here.", 3);
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else{ //has moved this turn
            gameState.setLastMessage(GameState.noEvent);
            BasicCommands.addPlayer1Notification(out, "Unit has already moved this turn.", 3);
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }



    }
    public static boolean canMove (MoveableUnit mover, Tile targetTile, Board board){ //method for determining if a unit can move to a tile
        if (mover instanceof YoungFlamewing) {		//Fly ability, can move anywhere on the board
        	for (int i =0; i< 9; i++) {
        		for (int j = 0; j <5; j++) {
        			Tile currentTile = board.getTile(i, j);
        			if (currentTile.getUnit() == null)
        				return true;
        		}
        	}
        }
    	
    	Tile currentTile = mover.getTile();
        int xPos = currentTile.getTilex();
        int yPos = currentTile.getTiley();
        for (int i = xPos - 1; i<=xPos+1;i++){ // i is x
            for (int j = yPos -1 ; j<=yPos+1;j++){ // j is y
                if ( 0<=i && i<=8 && 0<=j && j<=4 ){ //if coord in board range
                    Tile highlightTile = board.getTile(i,j);
                    if (highlightTile.getUnit()==null){//tile has no unit, safe for highlighting
                        if (targetTile.equals(highlightTile) ){
                            return true;
                        }
                    }
                }
            }
        }
        //the below conditions are for highlighting directions +2 in cardinal directions for movement
        if(xPos-2>=0){
            if (board.getTile(xPos-1,yPos).getUnit() == null) { //if space - 1 is empty
                Tile highlightTile = board.getTile(xPos-2, yPos);
                if (targetTile.equals(highlightTile)&& highlightTile.getUnit() ==null){
                    return true;
                }


            }
        }
        if(xPos+2<=8){
            if (board.getTile(xPos+1,yPos).getUnit() == null) { //if space + 1 is empty
                Tile highlightTile = board.getTile(xPos+2, yPos);
                if (targetTile.equals(highlightTile)&& highlightTile.getUnit() ==null){
                    return true;
                }

            }
        }
        if(yPos-2>=0){
            if (board.getTile(xPos,yPos-1).getUnit() == null) { //if space - 1 is empty
                Tile highlightTile = board.getTile(xPos, yPos-2);
                if (targetTile.equals(highlightTile)&& highlightTile.getUnit() ==null){
                    return true;
                }
            }
        }
        if(yPos+2<=4){
            if (board.getTile(xPos,yPos+1).getUnit() == null) { //if space + 1 is empty
                Tile highlightTile = board.getTile(xPos, yPos+2);
                if (targetTile.equals(highlightTile)&& highlightTile.getUnit() ==null){
                    return true;
                }
            }
        }
        return false;
    }

    public static void actionableTiles(MoveableUnit mover, ActorRef out, GameState gameState){
        //need to add logic about last turnMoved and lastTurn attacked and turnSummoned
        System.out.println("Actionable tiles is running.");
        int xPos = mover.getTile().getTilex();
        int yPos = mover.getTile().getTiley();
        Board board = gameState.getBoard();

        if (isProvokeAdjacent(mover,gameState)){
            //separate logic for provoked units
            if (mover.getTurnSummoned()!= gameState.getTurnNumber()){
                //not summoned this turn
                if (mover.getLastTurnAttacked()!=gameState.getTurnNumber()){
                //not attacked this turn
                    System.out.println("Uh oh its mr provoke");
                    System.out.println("Initiating provoked highlighting");
                    BasicCommands.addPlayer1Notification(out, "Unit is provoked!", 2);
                    gameState.setLastUnitClicked(mover);
                    gameState.setLastMessage(GameState.friendlyUnitClicked);
                    for (int i = xPos - 1; i<=xPos+1;i++){ // i is x
                        for (int j = yPos -1 ; j<=yPos+1;j++){ // j is y
                            if ( 0<=i && i<=8 && 0<=j && j<=4 ){ //if coord in board range
                                Tile adjacentTile = board.getTile(i,j);
                                if (adjacentTile.getUnit()!= null){
                                    MoveableUnit adjacentUnit = adjacentTile.getUnit();
                                    if (adjacentUnit instanceof Provoke && adjacentUnit.isUserOwned()!= mover.isUserOwned()){
                                        BasicCommands.drawTile(out, adjacentTile, 2);
                                    }
                                }
                            }
                        }
                    }
                    return;
                }else{
                    gameState.setLastMessage(GameState.noEvent);
                    BasicCommands.addPlayer1Notification(out, "This unit has already attacked, it can't perform another action", 2);
                }

            }else{ //summoned this turn, no action
                //insert code notifying user
                System.out.println("Summoned this turn");
                try {Thread.sleep(250);} catch (InterruptedException e) {e.printStackTrace();}
                gameState.setLastMessage(GameState.noEvent);
                BasicCommands.addPlayer1Notification(out, "This unit was summoned this turn, it can't perform an action.", 2);
                return;
            }
        }

        if (mover.getTurnSummoned()!=gameState.getTurnNumber()){//hasn't been summoned this turn, allow action
            System.out.println("Unit hasn't been summoned this turn");
            if (mover.getLastTurnAttacked() != gameState.getTurnNumber()){//hasn't attacked this turn can still move and attack
                System.out.println("Unit hasn't attacked this turn so it can still move and attack");
                if (mover.getLastTurnMoved()!= gameState.getTurnNumber()){//hasn't moved this turn
                    System.out.println("Highlighting tiles white");
                    //can still move and attack
                    //highlighting for moving (white)
                    gameState.setLastUnitClicked(mover);
                    gameState.setLastMessage(GameState.friendlyUnitClicked);
                    for (int i = xPos - 1; i<=xPos+1;i++){ // i is x
                        for (int j = yPos -1 ; j<=yPos+1;j++){ // j is y
                            if ( 0<=i && i<=8 && 0<=j && j<=4 ){ //if coord in board range
                                Tile highlightTile = board.getTile(i,j);
                                if (highlightTile.getUnit()==null){//tile has no unit, safe for highlighting
                                    BasicCommands.drawTile(out,highlightTile, 1);
//									System.out.println(i + " " + j);
                                }
                            }
                        }
                    }
                    //the below conditions are for highlighting directions +2 in cardinal directions for movement
                    if(xPos-2>=0){
                        if (board.getTile(xPos-1,yPos).getUnit() == null) { //if space - 1 is empty
                            Tile highlightTile = board.getTile(xPos - 2, yPos);
                            if (highlightTile.getUnit() == null){
                                BasicCommands.drawTile(out, highlightTile, 1);
                            }

                        }
                    }
                    if(xPos+2<=8){
                        if (board.getTile(xPos+1,yPos).getUnit() == null) { //if space + 1 is empty
                            Tile highlightTile = board.getTile(xPos+2, yPos);
                            if (highlightTile.getUnit() == null) {
                                BasicCommands.drawTile(out, highlightTile, 1);
                            }

                        }
                    }
                    if(yPos-2>=0){
                        if (board.getTile(xPos,yPos-1).getUnit() == null) { //if space - 1 is empty
                            Tile highlightTile = board.getTile(xPos, yPos-2);
                            if (highlightTile.getUnit() == null) {
                                BasicCommands.drawTile(out, highlightTile, 1);
                            }

                        }
                    }
                    if(yPos+2<=4){
                        if (board.getTile(xPos,yPos+1).getUnit() == null) { //if space + 1 is empty
                            Tile highlightTile = board.getTile(xPos, yPos+2);
                            if (highlightTile.getUnit() == null) {
                                BasicCommands.drawTile(out, highlightTile, 1);
                            }

                        }
                    }
                    //code for then highlighting tiles red
                    for (int i = xPos - 1; i<=xPos+1;i++){ // i is x
                        for (int j = yPos -1 ; j<=yPos+1;j++){ // j is y
                            if ( 0<=i && i<=8 && 0<=j && j<=4 ){ //if coord in board range
                                Tile highlightTile = board.getTile(i,j);
                                if (highlightTile.getUnit()!= null && mover.isUserOwned() != highlightTile.getUnit().isUserOwned()){//tile has unit and if enemy unit
                                    BasicCommands.drawTile(out,highlightTile, 2);

                                }
                            }
                        }
                    }

                }else { //has moved, can only attack
                    try {Thread.sleep(250);} catch (InterruptedException e) {e.printStackTrace();}
                    BasicCommands.addPlayer1Notification(out, "This unit has already moved, it can only attack", 2);
                    System.out.println("Has moved, can only attack");
                    gameState.setLastUnitClicked(mover);
                    gameState.setLastMessage(GameState.friendlyUnitClicked);
                    for (int i = xPos - 1; i<=xPos+1;i++){ // i is x
                        for (int j = yPos -1 ; j<=yPos+1;j++){ // j is y
                            if ( 0<=i && i<=8 && 0<=j && j<=4 ){ //if coord in board range
                                Tile highlightTile = board.getTile(i,j);
                                if (highlightTile.getUnit()!= null && mover.isUserOwned() != highlightTile.getUnit().isUserOwned()){//tile has unit and if enemy unit
                                    BasicCommands.drawTile(out,highlightTile, 2);

                                }
                            }
                        }
                    }
                }
            }else{
                //has attacked this turn, can't move or attack again
                //inform user
                try {Thread.sleep(250);} catch (InterruptedException e) {e.printStackTrace();}
                gameState.setLastMessage(GameState.noEvent);
                BasicCommands.addPlayer1Notification(out, "This unit has already attacked, it can't perform another action", 2);



            }
        }else{ //summoned this turn, no action
            //insert code notifying user
            System.out.println("Summoned this turn");
            try {Thread.sleep(250);} catch (InterruptedException e) {e.printStackTrace();}
            gameState.setLastMessage(GameState.noEvent);
            BasicCommands.addPlayer1Notification(out, "This unit was summoned this turn, it can't perform an action.", 2);

        }
    }


    public static void summon (MoveableUnit summon, ActorRef out, Tile tile, GameState gameState){
        boolean userOwned = summon.isUserOwned();
        if (canSummon(gameState,userOwned,tile) || gameState.getLastMessage().equals(GameState.darkTerminusOngoing)) {
            tile.setUnit(summon);
            if (summon instanceof Creature){
                Creature creature = (Creature) summon;
                creature.setUnit(BasicObjectBuilders.loadUnit(creature.getUnitConfig(), gameState.getFrontEndUnitID(), Unit.class));
            }
            System.out.println("Unit is " + summon.getUnit());
            summon.getUnit().setPositionByTile(tile);//sets player avatar on tile in front end
            summon.setTurnSummoned(gameState.getTurnNumber());
            summon.setLastTurnAttacked(gameState.getTurnNumber());
            BasicCommands.drawUnit(out, summon.getUnit(), tile); //sets player avatar on tile in front end
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            BasicCommands.setUnitHealth(out, summon.getUnit(), summon.getCurrentHealth());
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            BasicCommands.setUnitAttack(out, summon.getUnit(), summon.getAttack());
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gameState.getBoard().openingGambit(out, gameState);//for opening gambit
            gameState.getBoard().renderBoard(out);
            gameState.setLastMessage(GameState.noEvent);
            //if (summon instanceof SaberspineTiger) {
            	
            //}
        } else{ //inform player unsuitable location
            BasicCommands.addPlayer1Notification(out,"Can't summon here", 2);
        }
    }

    public static void summonableTiles(ActorRef out, GameState gameState){
        Board board = gameState.getBoard();
        ArrayList<MoveableUnit> friendlyUnits = board.friendlyUnits(true); //returns all player owned units
        for (MoveableUnit unit : friendlyUnits){
            Tile friendlyTile = unit.getTile();
            int xPos = friendlyTile.getTilex();
            int yPos = friendlyTile.getTiley();
            //Loop through adjacent squares
            for (int i = xPos - 1; i<=xPos+1;i++){ // i is x
                for (int j = yPos -1 ; j<=yPos+1;j++){ // j is y
                    if ( 0<=i && i<=8 && 0<=j && j<=4 ){ //if coord in board range
                        Tile highlightTile = board.getTile(i,j);
                        if (highlightTile.getUnit()==null){//tile has no unit, safe for highlighting
                            BasicCommands.drawTile(out,highlightTile, 1);
                        }
                    }
                }
            }
        }
    }

    public static boolean canSummon (GameState gameState, boolean userOwned, Tile possibleTile){
        Board board = gameState.getBoard();
        ArrayList<MoveableUnit> friendlyUnits = board.friendlyUnits(userOwned); //returns all player owned units
        for (MoveableUnit unit : friendlyUnits){
            Tile friendlyTile = unit.getTile();
            int xPos = friendlyTile.getTilex();
            int yPos = friendlyTile.getTiley();
            //Loop through adjacent squares
            for (int i = xPos - 1; i<=xPos+1;i++){ // i is x
                for (int j = yPos -1 ; j<=yPos+1;j++){ // j is y
                    if ( 0<=i && i<=8 && 0<=j && j<=4 ){ //if coord in board range
                        Tile highlightTile = board.getTile(i,j);
                        if (highlightTile.getUnit()==null&& highlightTile.equals(possibleTile)){//tile has no unit, safe for summon
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isProvokeAdjacent (MoveableUnit actionTaker, GameState gameState){
        Board board = gameState.getBoard();
        boolean userOwned = actionTaker.isUserOwned();
        Tile unitTile = actionTaker.getTile();
        int xPos = unitTile.getTilex();
        int yPos = unitTile.getTiley();

        //Loop through adjacent squares
        for (int i = xPos - 1; i<=xPos+1;i++){ // i is x
            for (int j = yPos -1 ; j<=yPos+1;j++){ // j is y
                if ( 0<=i && i<=8 && 0<=j && j<=4 ){ //if coord in board range
                    Tile adjacentTile = board.getTile(i,j);
                    if (adjacentTile.getUnit()!= null){
                        MoveableUnit adjacentTileUnit = adjacentTile.getUnit();
                        if (adjacentTileUnit instanceof Provoke && adjacentTileUnit.isUserOwned()!= userOwned){
                            //if adjacent unit has provoke and is enemy
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}

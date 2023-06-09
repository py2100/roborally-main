/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally.controller;


import dk.dtu.compute.se.pisd.designpatterns.observer.Observer;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;

import dk.dtu.compute.se.pisd.roborally.RoboRally;

import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Command;
import dk.dtu.compute.se.pisd.roborally.model.Player;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import dk.dtu.compute.se.pisd.roborally.JSON.LoadBoard;
/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class AppController implements Observer {

    final private List<Integer> PLAYER_NUMBER_OPTIONS = Arrays.asList(2, 3, 4, 5, 6);
    final private List<String> BOARD_NUMBER_OPTION = Arrays.asList("Map 1 - Small","Map 2 - Small","Map 3 - Large");
    final private List<String> PLAYER_COLORS = Arrays.asList("red", "green", "blue", "orange", "grey", "magenta");

    final private RoboRally roboRally;
    Board board;


    private GameController gameController;

    public AppController(@NotNull RoboRally roboRally) {
        this.roboRally = roboRally;
    }

    public void newGame() {

        ChoiceDialog<Integer> playerdialog = new ChoiceDialog<>(PLAYER_NUMBER_OPTIONS.get(0), PLAYER_NUMBER_OPTIONS);
        playerdialog.setTitle("Player number");
        playerdialog.setHeaderText("Select number of players");
        Optional<Integer> result = playerdialog.showAndWait();

        ChoiceDialog<String> boardDialog = new ChoiceDialog<>(BOARD_NUMBER_OPTION.get(0), BOARD_NUMBER_OPTION);
        boardDialog.setTitle("Board number");
        boardDialog.setHeaderText("Select Board");
        Optional<String> result2 = boardDialog.showAndWait();
        String selectedBoard = result2.orElse(null); // Use orElse to handle canceled dialog

        if (selectedBoard != null) { // Check if board selection is present
            if (gameController != null) {
                // The UI should not allow this, but in case this happens anyway.
                // give the user the option to save the game or abort this operation!
                if (!stopGame()) {
                    return;
                }
            }

            // XXX the board should eventually be created programmatically or loaded from a file
            //     here we just create an empty board with the required number of players.






            switch (selectedBoard) {
                case "Map 1 - Small":
                 board = LoadBoard.loadMap("Board 1");
                    board.setGameId(1);
                    break;

                case "Map 2 - Small":
                    // Logic for Map 2
                    board = LoadBoard.loadMap("Board 2");
                    board.setGameId(2);


                    break;
                case "Map 3 - Large":
                    // Logic for Map 3
                    board = LoadBoard.loadMap("Board 3");
                    board.setGameId(3);
                    //board= new Board(12,10);
                    break;
            }
            gameController = new GameController(board);
            int no = result.get();
            for (int i = 0; i < no; i++) {
                Player player = new Player(board, PLAYER_COLORS.get(i), "Player " + (i + 1));
                board.addPlayer(player);

                player.setSpace(board.getSpace(i, 0));
                player.getSpace().setPlayer(player);
            }

            gameController.startProgrammingPhase();
            roboRally.createBoardView(gameController);
        }
    }

    public void saveGame() {
    LoadBoard.saveBoard(gameController.board,"Last Game");
    }



    public void loadGame() {
        try {
            Board board = LoadBoard.loadBoard("Last Game");
            if (board != null) {
                gameController = new GameController(board);
                roboRally.createBoardView(gameController);
            } else {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("No Saved Game Found");
                alert.setHeaderText(null);
                alert.setContentText("There is no saved game available. Better start playing!.");

                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error Loading Game");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while loading the game: " + e.getMessage());

            alert.showAndWait();
        }
    }

    /**
     * Stop playing the current game, giving the user the option to save
     * the game or to cancel stopping the game. The method returns true
     * if the game was successfully stopped (with or without saving the
     * game); returns false, if the current game was not stopped. In case
     * there is no current game, false is returned.
     *
     * @return true if the current game was stopped, false otherwise
     */
    public boolean stopGame() {
        if (gameController != null) {

            // here we save the game (without asking the user).
            saveGame();

            gameController = null;
            roboRally.createBoardView(null);
            return true;
        }
        return false;
    }

    public void exit() {
        if (gameController != null) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Exit RoboRally?");
            alert.setContentText("Are you sure you want to exit RoboRally?");
            Optional<ButtonType> result = alert.showAndWait();

            if (!result.isPresent() || result.get() != ButtonType.OK) {
                return; // return without exiting the application
            }
        }

        // If the user did not cancel, the RoboRally application will exit
        // after the option to save the game
        if (gameController == null || stopGame()) {
            Platform.exit();
        }
    }
    public boolean isGameRunning() {
        return gameController != null;
    }
    @Override
    public void update(Subject subject) {
        // XXX do nothing for now


    }
}

package co.edu.unal.tictactoe;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@IgnoreExtraProperties
public class Game {
    public String uuidDefendingPlayer;
    public String uuidChallengingPlayer;
    public String uuidWinningPlayer;
    public String state = "new";
    public String board = "         ";

    public Game() {
        // Default constructor required for calls to DataSnapshot.getValue(Game.class)
    }

    public Game(String uuidDefendingPlayer) {
        this.uuidDefendingPlayer = uuidDefendingPlayer;
    }
}
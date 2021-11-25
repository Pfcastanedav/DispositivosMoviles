package co.edu.unal.tictactoe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MultiplayerActivity extends AppCompatActivity {

    String uuidPlayer = "Paula";
    List gameList = new ArrayList<Game>();
    List keyGameList = new ArrayList<String>();

    RecyclerView reyclerViewGame;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference gamesRef = database.getReference("games");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);

        final Context myContext = this;

        Button buttonNewGame = (Button) findViewById(R.id.button_newgame);

        buttonNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(myContext, MainActivity.class);
                Game game = new Game(uuidPlayer);
                String keyGame = gamesRef.push().getKey();
                gamesRef.child(keyGame).setValue(game);
                intent.putExtra("uuidPlayer", uuidPlayer);
                intent.putExtra("keyGame", keyGame);
                intent.putExtra("isChallengingPlayer", "false");
                myContext.startActivity(intent);
            }
        });

        gamesRef.orderByChild("state").equalTo("new").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                gameList.clear();
                keyGameList.clear();

                for (DataSnapshot gameSnapshot: snapshot.getChildren()) {
                    Game game = gameSnapshot.getValue(Game.class);
                    gameList.add(game);
                    keyGameList.add(gameSnapshot.getKey());
                    System.out.println(game.uuidDefendingPlayer);
                }

                reyclerViewGame = (RecyclerView) findViewById(R.id.recyclerViewGame);
                reyclerViewGame.setHasFixedSize(true);
                reyclerViewGame.setLayoutManager(new LinearLayoutManager(myContext));

                GameAdapter mAdapter = new GameAdapter(uuidPlayer, gameList, keyGameList);
                reyclerViewGame.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getMessage());
            }
        });
    }
}
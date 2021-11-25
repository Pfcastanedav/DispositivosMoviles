package co.edu.unal.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {
    // Represents the internal state of the game
    private TicTacToeGame mGame;
    // Buttons making up the board
    private Button mBoardButtons[];
    // Various text displayed
    private TextView mInfoTextView;
    private boolean mGameOver;
    private Button mNewGame;
    private Button mReset_Scores;
    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;
    static final int DIALOG_ABOUT_ID = 2;
    private BoardView mBoardView;
    private int mHumanWins = 0;
    private int mComputerWins = 0;
    private int mTies = 0;
    private char mGoFirst = TicTacToeGame.HUMAN_PLAYER;
    private String uuidPlayer;
    private String keyGame;
    private boolean isChallengingPlayer = false;
    private boolean mTurn = true;

    MediaPlayer mHumanMediaPlayer;
    MediaPlayer mComputerMediaPlayer;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference gamesRef = database.getReference("games");


    Handler handler=new Handler();
    // Various text displayed
    private TextView mHumanScoreTextView;
    private TextView mComputerScoreTextView;
    private TextView mTieScoreTextView;
    private SharedPreferences mPrefs;


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {

            // Determine which cell was touched
            int col = (int) event.getX() / mBoardView.getBoardCellWidth();
            int row = (int) event.getY() / mBoardView.getBoardCellHeight();
            int pos = row * 3 + col;

            if (!mGameOver && setMove(TicTacToeGame.HUMAN_PLAYER, pos))	{

                gamesRef.child(keyGame).child("board").setValue(new String(mGame.getBoardState()));
                // If no winner yet, let the computer make a move
                int winner = mGame.checkForWinner();

                if (winner == 0) {
                    //mInfoTextView.setText(R.string.turn_computer);
                    //turnComputer();
                    mInfoTextView.setText("Turno del oponente");
                } else {
                    endGame(winner);
                    gamesRef.child(keyGame).child("state").setValue("finalized");
                }

            }

// So we aren't notified of continued events when finger is moved
            return false;
        }
    };

    private void endGame(int winner) {
        switch (winner) {
            case 0:
                return;
            case 1:
                mInfoTextView.setText(R.string.result_tie);
                mTies++;
                mTieScoreTextView.setText(Integer.toString(mTies));
                break;
            case 2:
                String defaultMessage = getResources().getString(R.string.result_human_wins);
                mInfoTextView.setText(mPrefs.getString("victory_message", defaultMessage));
                mHumanWins++;
                mHumanScoreTextView.setText(Integer.toString(mHumanWins));
                break;
            default:
                mInfoTextView.setText(R.string.result_computer_wins);
                mComputerWins++;
                mComputerScoreTextView.setText(Integer.toString(mComputerWins));
                break;
        }

        displayScores();
        mNewGame.setEnabled(true);
        mGameOver = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        mBoardButtons = new Button[TicTacToeGame.BOARD_SIZE];
        mInfoTextView = (TextView) findViewById(R.id.information);
        mGame = new TicTacToeGame();
        mBoardView = (BoardView) findViewById(R.id.board);
        mBoardView.setGame(mGame);
        mBoardView.setOnTouchListener(mTouchListener);
        mNewGame = (Button) findViewById(R.id.new_game);
        mReset_Scores= (Button) findViewById(R.id.reset);
        mInfoTextView = findViewById(R.id.information);
        mHumanScoreTextView = findViewById(R.id.player_score);
        mComputerScoreTextView = findViewById(R.id.computer_score);
        mTieScoreTextView = findViewById(R.id.tie_score);
        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);


        uuidPlayer = getIntent().getStringExtra("uuidPlayer");
        keyGame = getIntent().getStringExtra("keyGame");


        // Restore the scores
        mHumanWins = mPrefs.getInt("mHumanWins", 0);
        mComputerWins = mPrefs.getInt("mComputerWins", 0);
        mTies = mPrefs.getInt("mTies", 0);
        //mPrefs = PreferenceManager.getDefaultSharedPreferences(this);


        mReset_Scores.setOnClickListener(v -> {
            mHumanWins=0;
            mComputerWins=0;
            mTies=0;
            displayScores();

        }
        );


        mNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewGame();
            }
        });
        if (savedInstanceState == null) {
            startNewGame();
        }
        else {
// Restore the game's state
            mGame.setBoardState(savedInstanceState.getCharArray("board"));
            mGameOver = savedInstanceState.getBoolean("mGameOver");
            mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
            mHumanWins = savedInstanceState.getInt("mHumanWins");
            mComputerWins = savedInstanceState.getInt("mComputerWins");
            mTies = savedInstanceState.getInt("mTies");
            mGoFirst = savedInstanceState.getChar("mGoFirst");

            endGame(mGame.checkForWinner());
            if (!mGameOver) {
                mInfoTextView.setText(mGoFirst == TicTacToeGame.COMPUTER_PLAYER ? R.string.turn_computer : R.string.turn_human);
                mBoardView.invalidate();
            }

        } displayScores();
        String StateChallengingPlayer = getIntent().getStringExtra("isChallengingPlayer");

        if(StateChallengingPlayer.equals("true")) {
            isChallengingPlayer = true;
            mInfoTextView.setText("Turno del oponente");
            gamesRef.child(keyGame).child("uuidChallengingPlayer").setValue(uuidPlayer);
            gamesRef.child(keyGame).child("state").setValue("inprogress");
            mTurn = false;
        }

        gamesRef.child(keyGame).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!mTurn) {
                    Game game = dataSnapshot.getValue(Game.class);

                    if(game.board.equals(new String(mGame.getBoardState()))) {
                        return;
                    }

                    mGame.setBoardState(game.board.toCharArray());
                    System.out.println(game.board);

                    mBoardView.invalidate();

                    mComputerMediaPlayer.start(); // Play the sound effect

                    int winner = mGame.checkForWinner();
                    if (winner == 0) {
                        mInfoTextView.setText(R.string.turn_human);
                    } else {
                        endGame(winner);

                    }

                    mTurn = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        } );
    }

    private void displayScores() {
        mHumanScoreTextView.setText(Integer.toString(mHumanWins));
        mComputerScoreTextView.setText(Integer.toString(mComputerWins));
        mTieScoreTextView.setText(Integer.toString(mTies));
    }
    // Set up the game board.
    private void startNewGame() {

        mGame.clearBoard();
        mBoardView.invalidate();   // Redraw the board

        mGameOver = false;
        // Reset all buttons
        /*for (int i = 0; i < mBoardButtons.length; i++) {
            mBoardButtons[i].setText("");
            mBoardButtons[i].setEnabled(true);
            mBoardButtons[i].setOnClickListener(new ButtonClickListener(i));
        }
        */
        // Human goes first
        mInfoTextView.setText(R.string.first_human);


    }
   /* private class ButtonClickListener implements View.OnClickListener {
        int location;

        public ButtonClickListener(int location) {
            this.location = location;
        }

        @Override
        public void onClick(View v) {
            if (mBoardButtons[location].isEnabled() && !mGameOver) {
                setMove(TicTacToeGame.HUMAN_PLAYER, location);

                // If no winner yet, let the computer make a move
                int winner = mGame.checkForWinner();
                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_computer);
                    int move = mGame.getComputerMove();
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                    winner = mGame.checkForWinner();
                }

                if (winner == 0)
                    mInfoTextView.setText(R.string.turn_human);
                else if (winner == 1)
                    mInfoTextView.setText(R.string.result_tie);
                else if (winner == 2)
                    mInfoTextView.setText(R.string.result_human_wins);
                else
                    mInfoTextView.setText(R.string.result_computer_wins);
                mGameOver= winner != 0;
            }
        }

        private void setMove(char player, int location) {

            mGame.setMove(player, location);
            mBoardButtons[location].setEnabled(false);
            mBoardButtons[location].setText(String.valueOf(player));
            if (player == TicTacToeGame.HUMAN_PLAYER)
                mBoardButtons[location].setTextColor(Color.rgb(0, 200, 0));
            else
                mBoardButtons[location].setTextColor(Color.rgb(200, 0, 0));
        }
    }*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_game:
                startNewGame();
                return true;
            case R.id.ai_difficulty:
                showDialog(DIALOG_DIFFICULTY_ID);
                return true;
            case R.id.quit:
                showDialog(DIALOG_QUIT_ID);
                return true;
            case R.id.about:
                showDialog(DIALOG_ABOUT_ID);
                return true;
        }
        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
                Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch(id) {

            case DIALOG_ABOUT_ID:
                //AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                Context context = getApplicationContext();
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.about_dialogue, null);
                builder.setView(layout);
                builder.setPositiveButton("OK", null);
                dialog = builder.create();
                break;

            case DIALOG_QUIT_ID:
                builder.setMessage(R.string.quit_question)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MainActivity.this.finish();
                            }
                        })
                        .setNegativeButton(R.string.no, null);
                dialog = builder.create();

                break;




            case DIALOG_DIFFICULTY_ID:

                builder.setTitle(R.string.difficulty_choose);

                final CharSequence[] levels = {
                        getResources().getString(R.string.difficulty_easy),
                        getResources().getString(R.string.difficulty_harder),
                        getResources().getString(R.string.difficulty_expert)};
                        int selected = 0;
                        // selected is the radio button that should be selected.

                builder.setSingleChoiceItems(levels, selected,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                dialog.dismiss();   // Close dialog
                                switch(item){
                                 case 1:
                                 mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder );
                                 break;
                                 case 2:
                                 mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert );
                                 break;
                                 default:
                                 mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy );
                                 break;
                             }
                                // TODO: Set the diff level of mGame based on which item was selected.


                                // Display the selected difficulty level
                                Toast.makeText(getApplicationContext(), levels[item],
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                dialog = builder.create();

                break;
        }

        return dialog;


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharArray("board", mGame.getBoardState());
        outState.putBoolean("mGameOver", mGameOver);
        outState.putInt("mHumanWins", Integer.valueOf(mHumanWins));
        outState.putInt("mComputerWins", Integer.valueOf(mComputerWins));
        outState.putInt("mTies", Integer.valueOf(mTies));
        outState.putCharSequence("info", mInfoTextView.getText());
        outState.putChar("mGoFirst", mGoFirst);
    }
    private boolean setMove(char player, int location) {
        if (mGame.setMove(player, location)) {
            mBoardView.invalidate();   // Redraw the board
            return true;
        }
        return false;
    }
    @Override
    protected void onResume() {
        super.onResume();

        mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.human);
        mComputerMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.pc);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mHumanMediaPlayer.release();
        mComputerMediaPlayer.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
// Save the current scores
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("mHumanWins", mHumanWins);
        ed.putInt("mComputerWins", mComputerWins);
        ed.putInt("mTies", mTies);
        ed.commit();
    }


}

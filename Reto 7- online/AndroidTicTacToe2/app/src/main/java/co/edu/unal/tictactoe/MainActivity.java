package co.edu.unal.tictactoe;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // Represents the internal state of the game
    private TicTacToeGame mGame;
    private boolean mGameOver = false;
    private Integer isChallengingPlayer = 0;

    private int mHumanWins = 0;
    private int mComputerWins = 0;
    private int mTies = 0;
    private char mGoFirst = TicTacToeGame.HUMAN_PLAYER;
    private String uuidPlayer;
    private String keyGame;

    private boolean mSoundOn = true;
    private boolean mTurn = true;

    // Buttons making up the board
    private BoardView mBoardView;

    // Various text displayed
    private TextView mInfoTextView;
    private TextView mHumanScoreTextView;
    private TextView mComputerScoreTextView;
    private TextView mTieScoreTextView;
    private SharedPreferences mPrefs;

    Button buttonNewGame;
    MediaPlayer mHumanMediaPlayer;
    MediaPlayer mComputerMediaPlayer;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference gamesRef = database.getReference("games");

    // Listen for touches on the board
    private OnTouchListener mTouchListener = new OnTouchListener() {

        public boolean onTouch(View v, MotionEvent event) {
            // Determine which cell was touched
            int col = (int) event.getX() / mBoardView.getBoardCellWidth();
            int row = (int) event.getY() / mBoardView.getBoardCellHeight();
            int pos = row * 3 + col;

            if (!mGameOver && mTurn && isChallengingPlayer != 2) {
                if (setMove(TicTacToeGame.HUMAN_PLAYER, pos)) {
                    buttonNewGame.setEnabled(false);
                    mGoFirst = mGoFirst == TicTacToeGame.HUMAN_PLAYER ? TicTacToeGame.COMPUTER_PLAYER : TicTacToeGame.HUMAN_PLAYER;
                    if (mSoundOn) {
                        try {
                            mHumanMediaPlayer.start(); // Play the sound effect
                        } catch (Exception e) {

                        }
                    }

                    int winner = mGame.checkForWinner();
                    gamesRef.child(keyGame).child("board").setValue(new String(mGame.getBoardState()));
                    mTurn = false;

                    if (winner == 0) {
                        //mInfoTextView.setText(R.string.turn_computer);
                        //turnComputer();
                        mInfoTextView.setText("Turno del oponente");
                    } else {
                        endGame(winner);
                        gamesRef.child(keyGame).child("state").setValue("finalized");
                    }
                }
            }

            // So we aren't notified of continued events when finger is moved
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGame = new TicTacToeGame();

        // Restore the scores from the persistent preference data source
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSoundOn = mPrefs.getBoolean("sound", true);
        String difficultyLevel = mPrefs.getString("difficulty_level", getResources().getString(R.string.difficulty_harder));

        if (difficultyLevel.equals(getResources().getString(R.string.difficulty_easy)))
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
        else if (difficultyLevel.equals(getResources().getString(R.string.difficulty_harder)))
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
        else
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);

        mHumanWins = mPrefs.getInt("mHumanWins", 0);
        mComputerWins = mPrefs.getInt("mComputerWins", 0);
        mTies = mPrefs.getInt("mTies", 0);

        mInfoTextView = findViewById(R.id.information);
        mHumanScoreTextView = findViewById(R.id.player_score);
        mComputerScoreTextView = findViewById(R.id.computer_score);
        mTieScoreTextView = findViewById(R.id.tie_score);

        mBoardView = findViewById(R.id.board);
        // Listen for touches on the board
        mBoardView.setOnTouchListener(mTouchListener);
        mBoardView.setGame(mGame);

        uuidPlayer = getIntent().getStringExtra("uuidPlayer");
        keyGame = getIntent().getStringExtra("keyGame");
        String StateChallengingPlayer = getIntent().getStringExtra("isChallengingPlayer");
        String defendingPlayer = getIntent().getStringExtra("defendingPlayer");
        String challengerPlayer = getIntent().getStringExtra("challengerPlayer");

        if(StateChallengingPlayer.equals("0")) {
            isChallengingPlayer = 0;
            mGame.HUMAN_PLAYER = 'X';
            mGame.COMPUTER_PLAYER = 'O';
        }

        else if(StateChallengingPlayer.equals("1")){
            isChallengingPlayer = 1;
            mInfoTextView.setText("Turno del oponente");
            gamesRef.child(keyGame).child("uuidChallengingPlayer").setValue(uuidPlayer);
            gamesRef.child(keyGame).child("state").setValue("inprogress");
            mGame.HUMAN_PLAYER = 'O';
            mGame.COMPUTER_PLAYER = 'X';
            mTurn = false;
        }

        else{
            isChallengingPlayer=2;
            mInfoTextView.setText("--- Modo Expectador ---\n" + defendingPlayer + "(X) vs. " + challengerPlayer + "(O)");
            mTurn= false;
        }

        gamesRef.child(keyGame).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!mTurn || isChallengingPlayer == 2) {
                    Game game = dataSnapshot.getValue(Game.class);

                    if(game.board.equals(new String(mGame.getBoardState()))) {
                       return;
                    }

                    mGame.setBoardState(game.board.toCharArray());
                    System.out.println(game.board);

                    mBoardView.invalidate();
                    if (mSoundOn) {
                        try {
                            mComputerMediaPlayer.start(); // Play the sound effect
                        } catch (Exception e) {
                        }
                    }

                    if(isChallengingPlayer != 2){
                    int winner = mGame.checkForWinner();
                    if (winner == 0) { mInfoTextView.setText(R.string.turn_human);
                    } else {endGame(winner);}

                    }

                    mTurn = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        } );

        if (savedInstanceState == null) {
            startNewGame();
        } else {
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
        }

        buttonNewGame = findViewById(R.id.new_game);
        buttonNewGame.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startNewGame();
            }
        });

        displayScores();
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

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mGame.setBoardState(savedInstanceState.getCharArray("board"));
        mGameOver = savedInstanceState.getBoolean("mGameOver");
        mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
        mHumanWins = savedInstanceState.getInt("mHumanWins");
        mComputerWins = savedInstanceState.getInt("mComputerWins");
        mTies = savedInstanceState.getInt("mTies");
        mGoFirst = savedInstanceState.getChar("mGoFirst");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(),
                R.raw.human);
        mComputerMediaPlayer = MediaPlayer.create(getApplicationContext(),
                R.raw.pc);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (item.getItemId()) {
            case R.id.new_game:
                startNewGame();
                return true;
            case R.id.about:
                Context context = getApplicationContext();
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                View aboutView;
                aboutView = inflater.inflate(R.layout.about_dialogue, null);
                builder.setView(aboutView);
                builder.setPositiveButton("OK", null);
                builder.create().show();
                return true;
            case R.id.reset:
                mHumanWins = 0;
                mComputerWins = 0;
                mTies = 0;
                displayScores();
                return true;
        }
        return false;
    }

    // Set up the game board.
    private void startNewGame() {
        mGame.clearBoard();
        mBoardView.invalidate(); // Redraw the board
        mGameOver = false;
        // Human goes first

        if(isChallengingPlayer == 0) {
            mInfoTextView.setText(R.string.first_human);
        }
    }

    private void displayScores() {
        mHumanScoreTextView.setText(Integer.toString(mHumanWins));
        mComputerScoreTextView.setText(Integer.toString(mComputerWins));
        mTieScoreTextView.setText(Integer.toString(mTies));

    }

    private boolean setMove(char player, int location) {
        if (mGame.setMove(player, location)) {
            mBoardView.invalidate(); // Redraw the board
            return true;
        }
        return false;
    }

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

        buttonNewGame.setEnabled(true);
        mGameOver = true;
    }

    private void turnComputer() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                int move = mGame.getComputerMove();
                setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                mGoFirst = mGoFirst == TicTacToeGame.HUMAN_PLAYER ? TicTacToeGame.COMPUTER_PLAYER
                        : TicTacToeGame.HUMAN_PLAYER;
                mBoardView.invalidate();
                if (mSoundOn) {
                    try {
                        mComputerMediaPlayer.start(); // Play the sound effect
                    } catch (Exception e) {
                    }
                }
                int winner = mGame.checkForWinner();
                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_human);
                } else {
                    endGame(winner);
                }

                buttonNewGame.setEnabled(true);
            }
        }, 250);
    }
}
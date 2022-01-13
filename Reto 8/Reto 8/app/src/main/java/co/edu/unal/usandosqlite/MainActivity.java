package co.edu.unal.usandosqlite;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.content.Intent;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "MESSAGE";
    private ListView obj;
    SearchView sv;
    SearchView sv2;
    DBHelper mydb;
    ArrayList[] arrays = new ArrayList[3];
    ArrayList array_list;
    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inflateAddContact();
            }
        });

        mydb = new DBHelper(this);
        arrays = mydb.getAllCotacts();
        array_list = new ArrayList<String>();
        array_list.addAll(getCardNames());
        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1, array_list);

        sv = (SearchView) findViewById(R.id.SearchViewName);
        sv2 = (SearchView) findViewById(R.id.SearchViewClassification);

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getSomeContacts(newText, sv2.getQuery().toString());
                return false;
            }
        });


        sv2.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getSomeContacts(sv.getQuery().toString(),newText);
                return false;
            }
        });



        obj = (ListView)findViewById(R.id.listView1);
        obj.setAdapter(arrayAdapter);
        obj.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
                // TODO Auto-generated method stub
                int id_To_Search = Integer.parseInt((String) arrays[0].get(arg2));

                Bundle dataBundle = new Bundle();
                dataBundle.putInt("id", id_To_Search);

                Intent intent = new Intent(getApplicationContext(),DisplayContact.class);

                intent.putExtras(dataBundle);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);

        switch(item.getItemId()) {
            case R.id.item1:
                return inflateAddContact();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }
        return super.onKeyDown(keycode, event);
    }

    private boolean inflateAddContact(){
        Bundle dataBundle = new Bundle();
        dataBundle.putInt("id", 0);

        Intent intent = new Intent(getApplicationContext(),DisplayContact.class);
        intent.putExtras(dataBundle);

        startActivity(intent);
        return true;
    }

    private void getSomeContacts(String searchTermName, String searchTermClass){
        array_list.clear();

        arrays = mydb.getSomeContacts(searchTermName, searchTermClass);

        array_list.addAll(getCardNames());

//        for (Object s :
//                temp) {
//            array_list.add((String) s);
//        }

        obj.setAdapter(arrayAdapter);
    }

    private ArrayList<String> getCardNames(){
        ArrayList<String> array_list = new ArrayList<>();
        if(arrays != null) {
            for (int i = 0; i < arrays[0].size(); i++) {
                array_list.add(String.format("%1$-15s",arrays[1].get(i)+":") + arrays[2].get(i));
            }
        }
        return array_list;
    }
}
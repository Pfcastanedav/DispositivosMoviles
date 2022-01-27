package edu.unal.reto10;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final String[] departamentos_labels = {
            "Amazonas", "Antioquia", "Arauca", "Atlántico",
            "Bogotá D.C.", "Bolívar", "Boyacá", "Caldas",
            "Caquetá", "Casanare", "Cauca", "Cesar",
            "Chocó", "Córdoba", "Cundinamarca", "Guainía",
            "Guaviare", "Huila", "La Guajira", "Magdalena",
            "Meta", "Nariño", "Norte de Santander", "Putumayo",
            "Quindío", "Risaralda", "San Andrés y Providencia", "Santander",
            "Sucre", "Tolima", "Valle del Cauca", "Vaupés", "Vichada"
    };

    private static final String[][] departamentos_values = {
            {"AMAZONAS"},{"ANTIOQUIA"},{"ARAUCA"},{"ATLANTICO","BARRANQUILLA"},
            {"BOGOTA D.C."},{"BOLIVAR","CARTAGENA"},{"BOYACA"},{"CALDAS"},{"CAQUETA"},
            {"CASANARE"},{"CAUCA"},{"CESAR"},{"CHOCO"},{"CORDOBA"},
            {"CUNDINAMARCA"},{"GUAINIA"},{"GUAVIARE"},{"HUILA"},{"LA_GUAJIRA"},
            {"MAGDALENA","SANTA MARTA"},{"META"},{"NARIÑO"},{"NORTE DE SANTANDER"},{"PUTUMAYO"},
            {"QUINDIO"},{"RISARALDA"},{"SAN ANDRES, PROVIDENCIA Y SANTA CATALINA"},
            {"SANTANDER"},{"SUCRE"},{"TOLIMA"},{"VALLE_DEL_CAUCA","BUENAVENTURA"},{"VAUPES"},{"VICHADA"}
    };

    private static final String[] laboratorios = {
            "ASTRAZENECA","JANSSEN","MODERNA","PFIZER","SINOVAC"
    };

    private static final int CONNECTION_TIMEOUT = 60000;
    private static final int DATARETRIEVAL_TIMEOUT = 60000;
    ProgressDialog pd;
    private static final String uribase = "https://www.datos.gov.co/resource/sdvb-4x4j.json?";
    Context context;
    TextView resultTextView;
    Button labButton, deptButton, buscarButton;
    String departamento = departamentos_labels[0], laboratorio = laboratorios[0];
    String n_vacunas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultTextView = findViewById(R.id.resultado);
        labButton = findViewById(R.id.laboratorio);
        deptButton = findViewById(R.id.departamento);
        buscarButton = findViewById(R.id.buscar);
        buscarButton.setOnClickListener(v -> new JsonTask().execute());

        AlertDialog.Builder departamentoPopup = new AlertDialog.Builder(this);
        AlertDialog.Builder labPopup = new AlertDialog.Builder(this);

        departamentoPopup.setTitle("Seleccione Departamento");
        labPopup.setTitle("Seleccione Laboratorio");

        int checkedItem = 0;
        departamentoPopup.setSingleChoiceItems(departamentos_labels, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user checked an item
                departamento = departamentos_labels[which];
                deptButton.setText(departamento);
            }
        });

        departamentoPopup.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user clicked OK
            }
        });

        departamentoPopup.setNegativeButton("Cancel", null);

        labPopup.setSingleChoiceItems(laboratorios, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user checked an item
                laboratorio = laboratorios[which];
                labButton.setText(laboratorio);
            }
        });

        labPopup.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user clicked OK
            }
        });

        labPopup.setNegativeButton("Cancel", null);

        AlertDialog departamentoDialog = departamentoPopup.create();
        AlertDialog labDialog = labPopup.create();

        deptButton.setOnClickListener(v -> departamentoDialog.show());
        labButton.setOnClickListener(v -> labDialog.show());

    }

    public static JSONArray requestWebService(String serviceUrl) {
        disableConnectionReuseIfNecessary();

        HttpURLConnection urlConnection = null;
        try {
            // create connection
            URL urlToRequest = new URL(serviceUrl);
            urlConnection = (HttpURLConnection)  urlToRequest.openConnection();
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(DATARETRIEVAL_TIMEOUT);

            // handle issues
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                // handle unauthorized (if service requires user login)
                System.out.println("Error de autorización");
            }

            else if (statusCode != HttpURLConnection.HTTP_OK) {
                // handle any other errors, like 404, 500,..
                System.out.println("Error Miscelaneo");
            }

            // create JSON object from content
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return new JSONArray(getResponseText(in));
        }

        catch (MalformedURLException e) { System.out.println("URL is invalid"); }
        catch (SocketTimeoutException e) { System.out.println("data retrieval or connection timed out"); }
        catch (IOException e) { System.out.println("could not read response body"); }
        catch (JSONException e) { System.out.println("response body is no valid JSON string"); }
        finally { if (urlConnection != null) { urlConnection.disconnect(); } }
        return null;
    }

    private static void disableConnectionReuseIfNecessary() {
        // see HttpURLConnection API doc
        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    private static String getResponseText(InputStream inStream) {
        // very nice trick from
        // http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
        return new Scanner(inStream).useDelimiter("\\A").next();
    }

    public void getData(String uribase, String query) {
        JSONArray serviceResult = requestWebService(uribase + query);

        try {
            n_vacunas = serviceResult.getJSONObject(0).getString("sum_cantidad").toString();
            //Log.d("TEST",  Integer.toString(i) + " :: " + data.get(i).contacto);

        }

        catch (JSONException e) { Log.e("Error", e.toString()); }
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {
            for (int i = 0; i < departamentos_labels.length; i++) {
                if (departamentos_labels[i].equals(departamento)) {
                    departamento= departamentos_values[i][0];
                    break;
                };
            }
            String query = "$select=sum(cantidad)&$where=nom_territorio=%22"+departamento+"%22%20and%20laboratorio_vacuna=%22"+laboratorio+"%22";
            try { getData(uribase, query); }
            catch (Throwable e) { e.printStackTrace(); }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()) { pd.dismiss(); }
            resultTextView.setText(
                    "En el territorio de " +departamento+ " con el laboratorio "
                            + laboratorio+ " hay asignadas " + n_vacunas + " dosis. "
            );

        }
    }
}
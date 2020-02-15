package com.example.chritian.tesisrubus;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;



public class Inicio extends AppCompatActivity {
    public static Vibrator vibrar;
    Button btnBases;
    Button btnCiclos;
    Button btnCondicionales;
    Button btnVoz;
    ImageView btnConectar;
    public static BluetoothAdapter blueAdaptador = null;
    public static BluetoothSocket btSocket = null;
    public static ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        btnCiclos = (Button) findViewById(R.id.btnCiclo);
        btnVoz = (Button) findViewById(R.id.btnVoz);
        btnConectar = (ImageView) findViewById(R.id.btnConectar);
        vibrar = (Vibrator) getSystemService(getBaseContext().VIBRATOR_SERVICE);
        blueAdaptador = BluetoothAdapter.getDefaultAdapter();
       blueAdaptador.enable();

        /*
        Inicia la actividad que permite programar al prototipo.
        */

         /*
        Inicia la actividad que permite programar al prototipo habilitando
        el sensor de color en mismo.
        */

         /*
        Inicia la actividad que permite programar al prototipo habilitando
        el sensor de color en mismo , además habilita el botón ciclos
        con el cual se integran loops o repeticiones al prototipo
        */
        clickCiclos();


        //Conexion con el prototipo
        longClickConectar();
        clickConectar();


            /*
        Inicia la actividad que permite programar al prototipo habilitando
        el sensor de color en mismo , además habilita el botón ciclos
        con el cual se integran loops o repeticiones al prototipo
        */
        clickReconocer();





    }

    @Override
    protected void onDestroy() {

        blueAdaptador.disable();
        super.onDestroy();
    }

    void clickBases() {
        btnBases.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), Programador.class);
                startActivity(i);
                Programador.condicionales = false;
                Programador.ciclo = false;
                Programador.voz = false;

            }
        });


    }
    void clickConectar(){

        btnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           vibrar.vibrate(100);
                try {
                    conectar();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    void longClickConectar(){
        btnConectar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
               vibrar.vibrate(100);
                Intent i = new Intent(getApplicationContext(), DispositivosEmparejados.class);
                startActivity(i);
                return false;
            }
        });


    }
    void clickCondicionales() {

        btnCondicionales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), Programador.class);
                startActivity(i);
                Programador.condicionales = true;
                Programador.ciclo = false;
                Programador.voz = false;

            }
        });


    }




    void clickCiclos() {

        btnCiclos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), Programador.class);
                startActivity(i);
                Programador.condicionales =false;
                Programador.voz = false;
                Programador.ciclo = true;

            }
        });


    }

    void clickReconocer() {

        btnVoz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), Programador.class);
                startActivity(i);

                Programador.ciclo = true;
                Programador.voz=true;

            }
        });


    }

    private void mensaje(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }
    private String obtenerConf(String nombre) {

        SharedPreferences prefs = getSharedPreferences("rubusConf", MODE_PRIVATE);


        return prefs.getString(nombre, "00:00:00:00:00:00");


    }


    public void conectar() throws IOException {
        if (btSocket == null) {
            new Inicio.ConnectBT().execute();
        } else {
            btnConectar.setBackgroundResource(R.drawable.rubusoff);
          //  mensaje("Desconectado");
            btSocket.close();
            btSocket = null;
        }
        //Llamar a la clase para conectar
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true; //Casi esta conectado

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(Inicio.this, "Scope", "conectando...");  //Muestra dialogo de conexion
        }

        @Override
        protected Void doInBackground(Void... devices) //Conexion en background
        {
            try {
                if (btSocket == null) {

                    BluetoothDevice dispositivo = blueAdaptador.getRemoteDevice(obtenerConf("mac"));
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(Programador.myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//Inicia la conexion

                }
            } catch (IOException e) {
                ConnectSuccess = false;//Error de conexion
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);
            if (!ConnectSuccess) {
                btnConectar.setBackgroundResource(R.drawable.rubusoff);
                btSocket = null;
            } else {
                btnConectar.setBackgroundResource(R.drawable.rubus);
                Programador.empezaraLeer();
                Programador.btConectado = true;
            }
          progress.dismiss();
        }
    }


}

package com.example.chritian.tesisrubus;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;


public class DispositivosEmparejados extends AppCompatActivity
{


    ListView devicelist;



    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";

    @Override
protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivos);


        devicelist = (ListView)findViewById(R.id.listView);

        //Si el dispositivo posee bluetooth
        Inicio.blueAdaptador = BluetoothAdapter.getDefaultAdapter();



        //llena la lista de la pantalla con los dispositivos emparejados
        listaDispisitivosemparejados();


    }




    private void listaDispisitivosemparejados()    {
        pairedDevices = Inicio.blueAdaptador.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                list.add(bt.getName() + "\n" + bt.getAddress()); //obtiene el nombre y la mac del dispositivo
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No tiene dispositivos emparejados", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Metodo utilizado al dar click

    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
            // Obtiene unicamente la mac
            String info = ((TextView) v).getText().toString();
            String mac = info.substring(info.length() - 17);

           Programador.Mac=mac;

                guardarConf("mac",mac);
            conectar();

           // finish();
        }
    };


    private void mensaje(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }
    private void guardarConf(String nombre,String conf){

        SharedPreferences.Editor editor = getSharedPreferences("rubusConf", MODE_PRIVATE).edit();
        editor.putString(nombre, conf);
        editor.apply();

    }

    public void conectar(){
        new ConnectBT().execute(); //Llamar a la clase para conectar
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>{
        private boolean ConnectSuccess = true; //Casi esta conectado

        @Override
        protected void onPreExecute()
        {
            Inicio.progress = ProgressDialog.show(DispositivosEmparejados.this, "Rubus", "Conectando");  //Muestra dialogo de conexion
        }

        @Override
        protected Void doInBackground(Void... devices) //Conexion en background
        {
            try
            {
                if (Inicio.btSocket == null || !Programador.btConectado){
                    Inicio.blueAdaptador = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo =  Inicio.blueAdaptador .getRemoteDevice(Programador.Mac);
                    Inicio.btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(Programador.myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    Inicio.btSocket.connect();//Inicia la conexion

                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//Error de conexion

            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                mensaje("No se pudo conectar");

            }
            else
            {
                Programador.empezaraLeer();
                finish();

                mensaje("Conectado a Rubus");

               Programador.btConectado = true;
            }
          // Programador.progress.dismiss();
        }
    }

}

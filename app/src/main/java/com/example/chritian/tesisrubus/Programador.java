package com.example.chritian.tesisrubus;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class Programador extends AppCompatActivity implements RecognitionListener {


    //variables para controlar el gridview
    public static GridView gv;
    public static ArrayList<Integer> img = new ArrayList<Integer>();
    // variables para utilizar los botones
    public static AdaptadorPasos adapter;
    private ImageView btnArriba, btnDerecha, btnIzquierda, btnAccion, btnEjecutar, btnConectar, btnCiclos;
    public ImageView btnRojo, btnMorado, btnCeleste, btnVoz;
    private TextView txtCiclos;
    public static Vibrator vibrar;
    public static RelativeLayout layCiclos;
    LinearLayout layBotones, layVoz;
    //variables para para utilizar bluetooth

    public static Handler bluetoothIn;
    private StringBuilder recDataString = new StringBuilder();
    static ConnectedThread mconnectedThread;
    static boolean ciclo, condicionales, voz;

    public static String ms;


    public static boolean btConectado = false;

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    static Animation animation;
    public static String Mac = null;

    int ejecutarColor = R.drawable.ejecutar;
    //Variables para el reconocimiento de voz
    private ProgressBar barraVoz;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;



    //Variables para utilizar el acelerometro
    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programador);
        //Instanciado de las variables
        btnArriba = (ImageView) findViewById(R.id.btnArriba);
        btnDerecha = (ImageView) findViewById(R.id.btnDerecha);
        btnIzquierda = (ImageView) findViewById(R.id.btnIzquierda);
        btnAccion = (ImageView) findViewById(R.id.btnAccion);
        btnEjecutar = (ImageView) findViewById(R.id.btnEjecutar);
        btnCiclos = (ImageView) findViewById(R.id.btnCiclos);
        txtCiclos = (TextView) findViewById(R.id.txtCiclos);

        btnCeleste = (ImageView) findViewById(R.id.btnCeleste);
        btnMorado = (ImageView) findViewById(R.id.btnMorado);
        btnRojo = (ImageView) findViewById(R.id.btnRojo);

        btnVoz = (ImageView) findViewById(R.id.btnVoz);
        barraVoz = (ProgressBar) findViewById(R.id.barraVoz);

        vibrar = (Vibrator) getSystemService(getBaseContext().VIBRATOR_SERVICE);


        layCiclos = (RelativeLayout) findViewById(R.id.layCiclos);
        layBotones = (LinearLayout) findViewById(R.id.layBotones);
        layVoz = (LinearLayout) findViewById(R.id.layVoz);


        layCiclos.setVisibility(View.GONE);
        layVoz.setVisibility(View.GONE);
        layBotones.setVisibility(View.VISIBLE);
        ejecutarColor = R.drawable.ejecutar;
        btnEjecutar.setBackgroundResource(ejecutarColor);
        barraVoz.setVisibility(View.INVISIBLE);

        speech = SpeechRecognizer.createSpeechRecognizer(this);

        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-US");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);


        if (condicionales) {
            layCiclos.setVisibility(View.GONE);
            ejecutarColor = R.drawable.ejecutari;
            btnEjecutar.setBackgroundResource(ejecutarColor);
        }
        if (ciclo) {
            layCiclos.setVisibility(View.VISIBLE);
            ejecutarColor = R.drawable.ejecutari;
            btnEjecutar.setBackgroundResource(ejecutarColor);
        }

        if (voz) {
            layVoz.setVisibility(View.VISIBLE);
            layCiclos.setVisibility(View.GONE);
            layBotones.setVisibility(View.GONE);
            ejecutarColor = R.drawable.ejecutari;
            btnEjecutar.setBackgroundResource(ejecutarColor);
        }

        gv = (GridView) findViewById(R.id.gvPasos);
        adapter = new AdaptadorPasos(this, img);
        gv.setAdapter(adapter);

        //Evento para recibir informacion del prototipo
        recibirInformacion();



        /*Añade acciones al contenedor de pasos*/
        clickArriba();      //añade arriba al contenedor
        clickDerecha();     //añade derecha al contenedor
        clickIzquierda();   //añade izquierda al contenedor
        clickAccion();      //añade accion al contenedor

         /*El prototipo repite todas las acciones en el contenedor el numero de
         veces en el botón ciclos*/
        clickCiclos();     //suma 1 al contenido del botón ciclos

      /*Comunicación con el prototipo*/
        clickEjecutar();        //envía acciones al prototipo para que las ejecuteclickParar();
        parar();           //envía instrucción de pare al prototipo
        longClickEjecutar();


        //Censado de colores
        clickCeleste();
        clickMorado();
        clickRojo();


        //Reconocer voz
        clickVoz();


        //limpiar gridview
        img.clear();
        adapter.notifyDataSetChanged();
        gv.setAdapter(adapter);



        // Implementar agitar
        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                img.clear();
                adapter.notifyDataSetChanged();
                gv.setAdapter(adapter);
            }
        });


    }


    public static float obtenerY() {

        return gv.getX();
    }

    public static void EliminarItem(int i) {

        img.remove(i);
        img.trimToSize();
        adapter.notifyDataSetChanged();


    }


    public static void empezaraLeer() {

        mconnectedThread = new ConnectedThread(Inicio.btSocket);
        mconnectedThread.start();

    }


    private StringBuilder sb = new StringBuilder();
    public static Handler h;
    int anterior = 0;

    int pintar = -1;

    private void recibirInformacion() {

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {


                String entradaDatos = ms.trim();

                if (entradaDatos.length() > 0) {




                        if (entradaDatos.contains("q")) {

                            quitarSeleccion(gv.getChildAt(anterior));
                            parar();
                        } else if (entradaDatos.contains("r")) {
                            vibrar.vibrate(300);
                            animar(btnRojo, R.anim.anim_escala);

                        } else if (entradaDatos.contains("c")) {
                            vibrar.vibrate(300);
                            animar(btnCeleste, R.anim.anim_escala);

                        } else if (entradaDatos.contains("m")) {
                            vibrar.vibrate(300);
                            animar(btnMorado, R.anim.anim_escala);

                        }




                }


                // if receive massage
                // append string


            }

            ;
        };

    }

    boolean rec = false;

    private void guardarConf(String nombre, String conf) {

        SharedPreferences.Editor editor = getSharedPreferences("rubusConf", MODE_PRIVATE).edit();
        editor.putString(nombre, conf);
        editor.apply();

    }

    private String obtenerConf(String nombre) {

        SharedPreferences prefs = getSharedPreferences("rubusConf", MODE_PRIVATE);


        return prefs.getString(nombre, "00:00:00:00:00:00");


    }

    private boolean esEntero(String str) {
        try {
            int d = Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private void quitarSeleccion(View v) {

        try {
            v.setBackgroundResource(0);
            v.getAnimation().cancel();
            v.clearAnimation();


        } catch (java.lang.NullPointerException ex) {
        }
    }

    private void seleccionar(View v) {
        // animar(v, R.anim.anim_seleccion);
        Drawable d = getResources().getDrawable(R.drawable.color_subrayado);
        v.setBackground(d);
    }


    static InputStream tmpIn = null;
    static OutputStream tmpOut = null;
    static InputStream mmInStream;
    static OutputStream mmOutStream;

    @Override
    public void onResume() {
        super.onResume();

        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();

        super.onPause();
        if (speech != null) {
            speech.destroy();

        }

    }

    @Override
    public void onBeginningOfSpeech() {

        barraVoz.setIndeterminate(false);

        barraVoz.setMax(100);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    private void clickVoz() {

        btnVoz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animar(btnVoz, R.anim.anim_escala);
                rec = !rec;
                if (rec) {
                    btnVoz.setBackgroundResource(R.drawable.mic);
                    speech.startListening(recognizerIntent);
                    barraVoz.setVisibility(View.VISIBLE);
                    barraVoz.setIndeterminate(true);

                } else {
                    speech.stopListening();
                    barraVoz.setIndeterminate(false);
                    btnVoz.setBackgroundResource(R.drawable.micoff);
                    barraVoz.setVisibility(View.INVISIBLE);
                    rec = false;
                }

            }
        });

    }

    @Override
    public void onEndOfSpeech() {

        barraVoz.setIndeterminate(false);
        btnVoz.setBackgroundResource(R.drawable.micoff);
        barraVoz.setVisibility(View.INVISIBLE);
        rec = false;

    }

    @Override
    public void onError(int errorCode) {

    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {

    }

    @Override
    public void onPartialResults(Bundle arg0) {

    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {

    }

    @Override
    public void onResults(Bundle results) {
        //Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = matches.get(0);
        Log.i("reconocer", text);

        String vector[] = text.split(" ");
        img.clear();
        for (String v : vector) {

            switch (v.trim()) {
                case "adelante":
                case "avanza":
                case "arriba":
                    img.add(R.drawable.arriba);
                    break;

                case "izquierda":
                    img.add(R.drawable.izquierda);
                    break;

                case "derecha":
                    img.add(R.drawable.derecha);
                    break;

                case "acción":
                case "sensar":
                case "morado":
                case "detectar":
                    img.add(R.drawable.luz);
                    break;


            }

        }


        adapter.notifyDataSetChanged();
        gv.setAdapter(adapter);
        gv.setSelection(img.size());

        if(gv.getCount()>0)
        btnEjecutar.performClick();



    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //  Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);

        barraVoz.setProgress((int) rmsdB * 10);
    }


    private static class ConnectedThread extends Thread {


        public ConnectedThread(BluetoothSocket socket) {


            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    Programador.ms = new String(buffer, 0, bytes);


                    // Get number of bytes and message in "buffer"
                    h.obtainMessage(1, bytes, -1, buffer).sendToTarget();
                    // Send to message queue Handler

                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */

    }


    private void longClickBuscarRubus() {
        btnConectar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                Intent i = new Intent(getApplicationContext(), DispositivosEmparejados.class);
                startActivity(i);


                return false;
            }
        });


    }

    private void mensaje(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private void animar(View obj, int i) {

        animation = AnimationUtils.loadAnimation(getApplicationContext(),
                i);
        obj.startAnimation(animation);


    }

    boolean ejecuntando;

    private void parar() {
        ejecuntando = false;
        pintar = -1;
        btnEjecutar.setBackgroundResource(ejecutarColor);
        enviar("q");
        quitarSeleccion(gv.getChildAt(anterior));
        anterior = -1;
    }

    private void ejecutar() {
        ejecuntando = true;
        btnEjecutar.setBackgroundResource(R.drawable.alto);
        String pasos = "";
        int tam = img.size();
        for (int i = 0; i < tam; i++) {
            switch (img.get(i)) {
                case R.drawable.arriba:
                    pasos = pasos + "w";
                    break;
                case R.drawable.izquierda:
                    pasos = pasos + "a";
                    break;
                case R.drawable.derecha:
                    pasos = pasos + "d";
                    break;
                case R.drawable.luz:
                    pasos = pasos + "s";
                    break;
            }
        }
        if (condicionales) {
            pasos = "i" + pasos;
        } else if (ciclo) {
            pasos = "c" + ciclos + pasos;
        } else {
            pasos = "b" + pasos;
        }
        pasos = pasos + ";";
        //Toast.makeText(getApplicationContext(), pasos, Toast.LENGTH_LONG).show();
        enviar(pasos);
    }

    private void clickArriba() {
        btnArriba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img.add(R.drawable.arriba);
                adapter.notifyDataSetChanged();
                gv.setAdapter(adapter);
                gv.setSelection(img.size());

                animar(btnArriba, R.anim.anim_escala);


            }
        });


    }

    private void clickMorado() {
        btnMorado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviar("@m;");
            }
        });

    }

    private void clickCeleste() {
        btnCeleste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviar("@c;");
            }
        });

    }

    private void clickRojo() {
        btnRojo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviar("@r;");
            }
        });

    }

    private void clickDerecha() {
        btnDerecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img.add(R.drawable.derecha);
                adapter.notifyDataSetChanged();
                gv.setAdapter(adapter);
                gv.setSelection(img.size());

                animar(btnDerecha, R.anim.anim_escala);


            }
        });


    }

    private void clickIzquierda() {
        btnIzquierda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img.add(R.drawable.izquierda);
                adapter.notifyDataSetChanged();
                gv.setAdapter(adapter);
                gv.setSelection(img.size());

                animar(btnIzquierda, R.anim.anim_escala);


            }
        });


    }

    int ciclos = 1;

    private void clickCiclos() {
        btnCiclos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animar(layCiclos, R.anim.anim_escala);
                ciclos++;
                if (ciclos > 9)
                    ciclos = 1;

                txtCiclos.setText(String.valueOf(ciclos));


            }
        });


    }


    /*Este metodo envia formatea los pasos para que pueda ser adquirido correctamente por el prototipo*/
    private void clickEjecutar() {


        btnEjecutar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ejecuntando) {
                    parar();

                } else {

                    ejecutar();
                }
                animar(btnEjecutar, R.anim.anim_escala);

            }
        });


    }

    private void longClickEjecutar() {

        btnEjecutar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {


                return false;
            }
        });

    }

    public static void enviar(String datos) {

        if (Inicio.btSocket != null && Inicio.btSocket.isConnected()) {
            try {
                vibrar.vibrate(30);
                Inicio.btSocket.getOutputStream().write(datos.toString().getBytes());
            } catch (IOException e) {
                //mensaje(e.getMessage());
            }
        }

    }

    private void clickAccion() {


        btnAccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img.add(R.drawable.luz);
                adapter.notifyDataSetChanged();
                gv.setAdapter(adapter);
                gv.setSelection(img.size());
                animar(btnAccion, R.anim.anim_escala);

            }
        });


    }


}

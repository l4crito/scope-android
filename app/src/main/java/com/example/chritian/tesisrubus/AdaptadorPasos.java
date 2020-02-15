package com.example.chritian.tesisrubus;

/**
 * Created by Chritian on 5/10/2017.
 */

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class AdaptadorPasos extends BaseAdapter {

    private static LayoutInflater inflater = null;

    Context contexto;
    ArrayList<Integer> imageId;

    Animation animation;

    public AdaptadorPasos(Programador programador, ArrayList<Integer> imagenes) {
        // TODO Auto-generated constructor stub

        contexto = programador;
        imageId = imagenes;
        ;
        inflater = (LayoutInflater) contexto.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return imageId.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder = new Holder();
        final View rowView;

        rowView = inflater.inflate(R.layout.activity_pasos_grid, null);

        holder.img = (ImageView) rowView.findViewById(R.id.imageView1);


        holder.img.setImageResource(imageId.get(position));

        rowView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                //Animar antes de eliminar
                animar(rowView,R.anim.anim_eliminar);

                //Eliminar despues de la animacion
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // Do something after 5s = 5000ms
                        Programador.EliminarItem(position);

                    }
                }, 300);
                

            }
        });

        rowView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {



                return false;
            }
        });

        return rowView;
    }

    private void animar(View obj,int i){

        animation = AnimationUtils.loadAnimation(contexto,
                i);
        obj.startAnimation(animation);



    }

    public class Holder {
        TextView tv;
        ImageView img;
    }

}
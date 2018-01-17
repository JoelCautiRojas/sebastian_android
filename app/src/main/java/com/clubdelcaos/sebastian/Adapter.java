package com.clubdelcaos.sebastian;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.snowdream.android.widget.SmartImageView;

import java.util.ArrayList;

public class Adapter extends BaseAdapter{
    private String BASE_URL;
    private String DIR_IMG;
    private LayoutInflater inflador;
    private ArrayList marcas_list;
    private ArrayList tipos_list;
    private ArrayList precios_list;
    private ArrayList imagenes_list;

    public Adapter(Context contexto, ArrayList marcas, ArrayList tipos, ArrayList precios, ArrayList imagenes, String BaseUrl, String DirImg){
        this.marcas_list = marcas;
        this.tipos_list = tipos;
        this.precios_list = precios;
        this.imagenes_list = imagenes;
        this.BASE_URL = BaseUrl;
        this.DIR_IMG = DirImg;
        inflador = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return marcas_list.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View item = view;
        ViewHolder holder;
        if(item == null)
        {
            item = inflador.inflate(R.layout.item_list_activity_main,null);
            holder = new ViewHolder();
            holder.marca = item.findViewById(R.id.marca_item);
            holder.tipo = item.findViewById(R.id.tipo_item);
            holder.precio = item.findViewById(R.id.precio_item);
            holder.imagen = item.findViewById(R.id.imagen_item);
            item.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) item.getTag();
        }
        //Establecer Datos
        holder.marca.setText(marcas_list.get(i).toString());
        holder.tipo.setText(tipos_list.get(i).toString());
        holder.precio.setText("Precio: S/. " + precios_list.get(i).toString());
        Rect rectangulo = new Rect(holder.imagen.getLeft(),holder.imagen.getTop(),holder.imagen.getRight(),holder.imagen.getBottom());
        holder.imagen.setImageUrl(DIR_IMG + imagenes_list.get(i).toString(),rectangulo);
        return item;
    }

    static class ViewHolder{
        TextView marca;
        TextView tipo;
        TextView precio;
        SmartImageView imagen;
    }
}
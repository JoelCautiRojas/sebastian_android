package com.clubdelcaos.sebastian;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import com.github.snowdream.android.widget.SmartImageView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //***************************************************************************   Elementos del layout (XML)
    LinearLayout miLayout;
    ListView lista;
    EditText nuevo_marca, nuevo_tipo, nuevo_precio;
    EditText mod_buscar_id, mod_marca, mod_precio, mod_tipo;
    ImageButton buscar;
    SmartImageView mod_imagen, nuevo_imagen;
    Button guardar, modificar, eliminar;
    //***************************************************************************   Variables de la Aplicacion
    ProgressDialog barprog;
    String BASE_URL         = "http://www.programadoresperuanos.com/test_web/sebastian/index.php";
    String DIR_IMG          = "http://www.programadoresperuanos.com/test_web/sebastian/imagenes/";
    String APP_DIRECTORY    = "MyApp/";
    String MEDIA_DIRECTORY  = APP_DIRECTORY + "MyPicture";
    File archivo;
    String ruta;
    Bitmap bitmap;
    Uri path;
    String operacion_imagen;
    TabHost tabs;
    //***************************************************************************   Declaracion de Matrices
    ArrayList matrizmarcas = new ArrayList();
    ArrayList matrizids = new ArrayList();
    ArrayList matriztipos = new ArrayList();
    ArrayList matrizprecios= new ArrayList();
    ArrayList matrizimagenes= new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //***************************************************************************   Vincular los elementos del layout
        miLayout            = findViewById(R.id.miLayout);
        lista               = findViewById(R.id.lista);
        nuevo_marca         = findViewById(R.id.nuevo_marca);
        nuevo_tipo          = findViewById(R.id.nuevo_tipo);
        nuevo_precio        = findViewById(R.id.nuevo_precio);

        mod_buscar_id       = findViewById(R.id.mod_buscar_id);
        mod_marca           = findViewById(R.id.mod_marca);
        mod_tipo            = findViewById(R.id.mod_tipo);
        mod_precio          = findViewById(R.id.mod_precio);

        nuevo_imagen        = findViewById(R.id.nuevo_imagen);
        mod_imagen          = findViewById(R.id.mod_imagen);

        buscar              = findViewById(R.id.buscar);
        guardar             = findViewById(R.id.boton_guardar);
        modificar           = findViewById(R.id.boton_modificar);
        eliminar            = findViewById(R.id.boton_eliminar);
        guardar.setOnClickListener(this);
        modificar.setOnClickListener(this);
        eliminar.setOnClickListener(this);
        buscar.setOnClickListener(this);
        nuevo_imagen.setOnClickListener(this);
        mod_imagen.setOnClickListener(this);
        guardar.setEnabled(false);
        modificar.setEnabled(false);
        eliminar.setEnabled(false);
        buscar.setEnabled(false);
        nuevo_imagen.setEnabled(false);
        mod_imagen.setEnabled(false);
        if(validarPermisos())
        {
            cargarApp();
        }
        else
        {
            solicitarPermisos();
        }
        //***************************************************************************  Programacion base de las pestañas
        tabs = (TabHost) findViewById(android.R.id.tabhost);
        tabs.setup();
        TabHost.TabSpec spec;
        spec = tabs.newTabSpec("mitab1");
        spec.setContent(R.id.tab1);
        spec.setIndicator("",getResources().getDrawable(android.R.drawable.ic_menu_gallery));
        tabs.addTab(spec);
        spec = tabs.newTabSpec("mitab2");
        spec.setContent(R.id.tab2);
        spec.setIndicator("",getResources().getDrawable(android.R.drawable.ic_menu_add));
        tabs.addTab(spec);
        spec = tabs.newTabSpec("mitab3");
        spec.setContent(R.id.tab3);
        spec.setIndicator("",getResources().getDrawable(android.R.drawable.ic_menu_edit));
        tabs.addTab(spec);
        tabs.setCurrentTab(0);
        tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                if("mitab1".equals(s))
                {
                    loadList();
                }
            }
        });
        loadList();

    }

    private void solicitarPermisos() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, CAMERA)
                || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, WRITE_EXTERNAL_STORAGE))
        {
            Snackbar.make(miLayout,"Te has olvidado de los permisos.",Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                            CAMERA,
                            READ_EXTERNAL_STORAGE,
                            WRITE_EXTERNAL_STORAGE
                    },100);
                }
            }).show();
        }
        else
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    CAMERA,
                    READ_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_STORAGE
            },100);
        }
    }

    private boolean validarPermisos() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            return true;
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        return false;
    }

    //-------------------------------------- Cargar App activa los botones
    private void cargarApp() {
        guardar.setEnabled(true);
        modificar.setEnabled(true);
        eliminar.setEnabled(true);
        buscar.setEnabled(true);
        nuevo_imagen.setEnabled(true);
        mod_imagen.setEnabled(true);
    }

    private void loadList() {
        //***************************************************************************  Cliente HTTP para obtener la vista de los registros de la base de datos
        barprog = new ProgressDialog(this);
        barprog.setCancelable(false);
        barprog.setMessage("Cargando...");
        barprog.setMax(100);
        barprog.setProgress(0);
        barprog.show();
        AsyncHttpClient cliente = new AsyncHttpClient();
        RequestParams parametros = new RequestParams();
        parametros.put("operacion","ver");
        cliente.post(getApplicationContext(), BASE_URL, parametros, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(statusCode == 200)
                {
                    barprog.dismiss();
                    matrizmarcas.clear();
                    matriztipos.clear();
                    matrizprecios.clear();
                    matrizimagenes.clear();
                    matrizids.clear();
                    try {
                        JSONArray array = new JSONArray(new String(responseBody));
                        if(array.length() > 0)
                        {
                            for(int i=0;i<array.length();i++)
                            {
                                matrizmarcas.add(array.getJSONObject(i).getString("marca"));
                                matriztipos.add(array.getJSONObject(i).getString("tipo"));
                                matrizprecios.add(array.getJSONObject(i).getString("precio"));
                                matrizimagenes.add(array.getJSONObject(i).getString("imagen"));
                                matrizids.add(array.getJSONObject(i).getString("id"));
                            }
                            Adapter adaptador = new Adapter(getApplicationContext(), matrizmarcas, matriztipos,  matrizprecios,  matrizimagenes, BASE_URL, DIR_IMG);
                            lista.setAdapter(adaptador);
                            lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    tabs.setCurrentTab(2);
                                    mod_buscar_id.setText(matrizids.get(i).toString());
                                    getRow(matrizids.get(i).toString());
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        String cadena = new String(responseBody);
                        Toast.makeText(getApplicationContext(),cadena,Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(),"Error, sin conecion con el servidor.",Toast.LENGTH_SHORT).show();
            }
        });
    }
    //***************************************************************************  Menu del Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.principal, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.Menu_Opc1:
                nuevo_marca.setText("");
                nuevo_tipo.setText("");
                nuevo_precio.setText("");
                mod_buscar_id.setText("");
                mod_marca.setText("");
                mod_tipo.setText("");
                mod_precio.setText("");
                nuevo_imagen.setImageResource(R.drawable.defecto);
                mod_imagen.setImageResource(R.drawable.defecto);
                return true;
            case R.id.Menu_Opc2:
                Intent refresh = new Intent(this, MainActivity.class);
                startActivity(refresh);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //***************************************************************************  Programacion de eventos de los botones
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            //*************************************************************************** Guardar Datos
            case R.id.boton_guardar:
                String marca       = nuevo_marca.getText().toString();
                String tipo       = nuevo_tipo.getText().toString();
                String precio       = nuevo_precio.getText().toString();
                if("".equals(marca) || "".equals(tipo) || "".equals(precio))
                {
                    Toast.makeText(getApplicationContext(),"¡ERROR!, no se pueden ingresar datos en blanco.",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    barprog = new ProgressDialog(this);
                    barprog.setCancelable(false);
                    barprog.setMessage("Cargando...");
                    barprog.setMax(100);
                    barprog.setProgress(0);
                    barprog.show();
                    AsyncHttpClient cliente1 = new AsyncHttpClient();
                    RequestParams parametros1 = new RequestParams();
                    parametros1.put("marca", marca);
                    parametros1.put("tipo", tipo);
                    parametros1.put("precio", precio);
                    parametros1.put("operacion", "insertar");
                    try {
                        parametros1.put("fotografia", archivo);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    cliente1.post(getApplicationContext(), BASE_URL, parametros1, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            if (statusCode == 200) {
                                barprog.dismiss();
                                String cadena = new String(responseBody);
                                Toast.makeText(getApplicationContext(), cadena, Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(getApplicationContext(), "¡ERROR!, sin conexion con el servidor.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            //*************************************************************************** Modificar Datos
            case R.id.boton_modificar:
                String marca_mod           = mod_marca.getText().toString();
                String tipo_mod           = mod_tipo.getText().toString();
                String precio_mod           = mod_precio.getText().toString();
                String id_mod               = mod_buscar_id.getText().toString();
                if("".equals(marca_mod) || "".equals(tipo_mod) || "".equals(precio_mod) || "".equals(id_mod))
                {
                    Toast.makeText(getApplicationContext(),"¡ERROR!, no se pueden ingresar datos en blanco.",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    barprog = new ProgressDialog(this);
                    barprog.setCancelable(false);
                    barprog.setMessage("Cargando...");
                    barprog.setMax(100);
                    barprog.setProgress(0);
                    barprog.show();
                    AsyncHttpClient cliente3 = new AsyncHttpClient();
                    RequestParams parametros3 = new RequestParams();
                    parametros3.put("marca",marca_mod);
                    parametros3.put("tipo",tipo_mod);
                    parametros3.put("precio",precio_mod);
                    parametros3.put("id",id_mod);
                    parametros3.put("operacion","modificar");
                    try {
                        parametros3.put("fotografia",archivo);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    cliente3.post(getApplicationContext(), BASE_URL, parametros3, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            if(statusCode == 200)
                            {
                                barprog.dismiss();
                                String cadena = new String(responseBody);
                                mod_marca.setText("");
                                mod_buscar_id.setText("");
                                mod_precio.setText("");
                                mod_tipo.setText("");
                                mod_imagen.setImageResource(R.drawable.defecto);
                                Toast.makeText(getApplicationContext(),cadena,Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(getApplicationContext(),"¡ERROR!, sin conexion con el servidor.",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            //***************************************************************************  Eliminar Datos
            case R.id.boton_eliminar:
                String id_eliminar = mod_buscar_id.getText().toString();
                if("".equals(id_eliminar))
                {
                    Toast.makeText(getApplicationContext(),"¡ERROR!, codigo en blanco.",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    barprog = new ProgressDialog(this);
                    barprog.setCancelable(false);
                    barprog.setMessage("Cargando...");
                    barprog.setMax(100);
                    barprog.setProgress(0);
                    barprog.show();
                    AsyncHttpClient cliente2 = new AsyncHttpClient();
                    RequestParams parametros2 = new RequestParams();
                    parametros2.put("operacion","eliminar");
                    parametros2.put("id",id_eliminar);
                    cliente2.post(getApplicationContext(), BASE_URL, parametros2, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            if(statusCode == 200)
                            {
                                barprog.dismiss();
                                String cadena = new String(responseBody);
                                mod_marca.setText("");
                                mod_buscar_id.setText("");
                                mod_precio.setText("");
                                mod_tipo.setText("");
                                mod_imagen.setImageResource(R.drawable.defecto);
                                Toast.makeText(getApplicationContext(),cadena,Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(getApplicationContext(),"¡ERROR!, sin conexion con el servidor.",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            //***************************************************************************  Cargar Imagen
            case R.id.nuevo_imagen:
                nuevo_imagen.setImageResource(R.drawable.defecto);
                operacion_imagen = "nuevo_imagen";
                getImage();
                nuevo_imagen.setImageBitmap(bitmap);
                break;
            //***************************************************************************  Modificar Imagen
            case R.id.mod_imagen:
                mod_imagen.setImageResource(R.drawable.defecto);
                operacion_imagen = "mod_imagen";
                getImage();
                mod_imagen.setImageBitmap(bitmap);
                break;
            //***************************************************************************  Buscar Registro
            case R.id.buscar:
                String id_buscar = mod_buscar_id.getText().toString();
                getRow(id_buscar);
                break;
            default:
                break;
        }
    }
    //***************************************************************************  Obtener Registro
    private void getRow(String id_buscar) {
        if("".equals(id_buscar))
        {
            Toast.makeText(getApplicationContext(),"¡ERROR!, codigo en blanco.",Toast.LENGTH_SHORT).show();
        }
        else
        {
            barprog = new ProgressDialog(this);
            barprog.setCancelable(false);
            barprog.setMessage("Cargando...");
            barprog.setMax(100);
            barprog.setProgress(0);
            barprog.show();
            AsyncHttpClient cliente4 = new AsyncHttpClient();
            RequestParams parametros4 = new RequestParams();
            parametros4.put("where","id='"+id_buscar+"'");
            parametros4.put("operacion","ver");
            cliente4.post(getApplicationContext(), BASE_URL, parametros4, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    if(statusCode==200)
                    {
                        barprog.dismiss();
                        try {
                            JSONArray producto = new JSONArray(new String(responseBody));
                            mod_marca.setText(producto.getJSONObject(0).getString("marca"));
                            mod_tipo.setText(producto.getJSONObject(0).getString("tipo"));
                            mod_precio.setText(producto.getJSONObject(0).getString("precio"));
                            Rect rectangulo = new Rect(mod_imagen.getLeft(),mod_imagen.getTop(),mod_imagen.getRight(),mod_imagen.getBottom());
                            mod_imagen.setImageUrl( DIR_IMG + producto.getJSONObject(0).getString("imagen"), rectangulo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            String cadena = new String(responseBody);
                            Toast.makeText(getApplicationContext(), cadena, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(getApplicationContext(),"¡ERROR!, sin conexion con el servidor.",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    //***************************************************************************  Obtener Imagen
    private void getImage(){
        final CharSequence[] option = {"Tomar Foto","Elegir de Galeria","Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Elige una opcion");
        builder.setItems(option, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                if(option[which]=="Tomar Foto")
                {
                    File carpeta =  new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
                    boolean isDirectoryCreated = carpeta.exists();
                    if(!isDirectoryCreated)
                    {
                        isDirectoryCreated = carpeta.mkdirs();
                    }
                    if(isDirectoryCreated)
                    {
                        Long timestamp = System.currentTimeMillis() / 1000;
                        String imageName = timestamp.toString()+".jpg";
                        ruta = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY + File.separator + imageName;
                        archivo = new File(ruta);
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(archivo));
                        startActivityForResult(intent,100);
                    }
                }
                else if(option[which]=="Elegir de Galeria")
                {
                    Intent intent =  new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent,"Selecciona app de imagen"),200);
                }
                else
                {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    //***************************************************************************  Resultado de la Imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case 100:
                    MediaScannerConnection.scanFile(this, new String[]{ruta}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {

                        }
                    });
                    ContentResolver cr = this.getContentResolver();
                    try {
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(cr, Uri.fromFile(archivo));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int rotate = 0;
                        ExifInterface exif = new ExifInterface(archivo.getAbsolutePath());
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        switch (orientation) {
                            case ExifInterface.ORIENTATION_ROTATE_90:
                                rotate = 90;
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_180:
                                rotate = 180;
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_270:
                                rotate = 270;
                                break;
                        }
                        Matrix matriz = new Matrix();
                        matriz.postRotate(rotate);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matriz, true);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if("nuevo_imagen".equals(operacion_imagen))
                    {
                        nuevo_imagen.setImageBitmap(bitmap);
                    }
                    else
                    {
                        mod_imagen.setImageBitmap(bitmap);
                    }
                    break;
                case 200:
                    path = data.getData();
                    String pathSegment[] = path.getLastPathSegment().split(":");
                    String id = pathSegment[0];
                    final String[] imageColumns = {MediaStore.Images.Media.DATA};
                    final String imageOrderBy = null;
                    String state = Environment.getExternalStorageState();
                    if(!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
                    {
                        path = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
                    }
                    else
                    {
                        path = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    }
                    Cursor imageCursor = getContentResolver().query(path, imageColumns, MediaStore.Images.Media._ID + "=" + id, null, imageOrderBy);
                    if(imageCursor.moveToFirst())
                    {
                        ruta = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    }
                    archivo = new File(ruta);
                    ContentResolver crv = this.getContentResolver();
                    bitmap = null;
                    try {
                        bitmap = android.provider.MediaStore.Images.Media.getBitmap(crv, Uri.fromFile(archivo));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if("nuevo_imagen".equals(operacion_imagen))
                    {
                        nuevo_imagen.setImageBitmap(bitmap);
                    }
                    else
                    {
                        mod_imagen.setImageBitmap(bitmap);
                    }
                    break;
            }
        }
    }
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults)
    {
        switch(requestCode)
        {
            case 100:
                if(grantResults.length == 0
                        || grantResults[0] == PackageManager.PERMISSION_DENIED
                        || grantResults[1] == PackageManager.PERMISSION_DENIED
                        || grantResults[2] == PackageManager.PERMISSION_DENIED)
                {
                    // Este codigo solo se activa si se ah rechazado alguna peticion
                    // Codigo Opcional para abrir la configuracion de la aplicacion
                    AlertDialog.Builder ventana = new AlertDialog.Builder(MainActivity.this);
                    ventana.setTitle("Permisos Negados");
                    ventana.setMessage("Necesitas otorgar los Permisos");
                    ventana.setPositiveButton("Aceptar",new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent configuracion = new Intent();
                            configuracion.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri direccion = Uri.fromParts("package",getPackageName(),null);
                            configuracion.setData(direccion);
                            startActivity(configuracion);
                        }
                    });
                    ventana.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Esta es una tostada, la app se cerro",Toast.LENGTH_LONG).show();
                        }
                    });
                    ventana.show();
                }
                else
                {
                    cargarApp();
                }
                break;
        }
    }
}
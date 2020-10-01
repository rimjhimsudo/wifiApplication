package com.example.myserverapp;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import javax.xml.transform.Result;


//dont use
public class MainActivity2 extends AppCompatActivity implements View.OnClickListener {
    Context  context;
    private ServerSocket serverSocket;
    private Socket tempClientSocket;
    Thread serverThread = null;
    public static final int SERVER_PORT = 3003;
    private LinearLayout msgList;
    private Handler handler;
    private int greenColor;
    private EditText edMessage;
    private ImageView imageView;
    //for permission
    public  static final int REQUEST_CODE=102;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        setTitle("Server");
        greenColor = ContextCompat.getColor(this, R.color.colorAccent);
        handler = new Handler();
        msgList = findViewById(R.id.msgList);
        edMessage = findViewById(R.id.edMessage);
        imageView=findViewById(R.id.imageview);
        //forr permiisssons
        String[] perms = {"android.permission.READ_EXTERNAL_STORAGE"};
        requestPermissions(perms,REQUEST_CODE);
    }




    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.start_server) {
            this.serverThread = new Thread(new ServerThread());
            this.serverThread.start();
            return;
        }
        if (view.getId() == R.id.send_data) {
            String msg = edMessage.getText().toString().trim();
            //sendMessage(msg);
        }
    }

    /*
    //this function is for writing server messgaes onto client side
    private void sendMessage(final String message) {
        try {
            Log.d("tempClientSocket",":  not null");
            if (null != tempClientSocket) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PrintWriter out = null;
                        try {
                            out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(tempClientSocket.getOutputStream())),
                                    true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        out.println(message);
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    class ServerThread implements Runnable {

        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                //findViewById(R.id.start_server).setVisibility(View.GONE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (null != serverSocket) {
                while (!Thread.currentThread().isInterrupted()) {
                    try {

                        //socket = serverSocket.accept();

                        while (true) {
                            socket = serverSocket.accept();
                            FileServerAsyncTask fileServerAsyncTask=new FileServerAsyncTask(MainActivity2.this);

                            Log.d("SOCKET value  : ",socket.toString());
                        }
                        /* this is int the case of sending text messgaes thread now we have to send images so making new thread
                        CommunicationThread commThread = new CommunicationThread(socket);
                        new Thread(commThread).start();*/
                    } catch (IOException e) {
                        e.printStackTrace();

                        //showMessage("Error Communicating to Client :" + e.getMessage(), Color.RED);
                    }
                }
            }
        }
    }
    public static class FileServerAsyncTask extends AsyncTask {

        private Context context;

        public FileServerAsyncTask(Context context) {
            this.context = context;
        }


        /**
         * Start activity that can handle the JPEG image
         */
        @Override
        protected String doInBackground(Object[] objects) {
            try {

                /**
                 * Create a server socket and wait for client connections. This
                 * call blocks until a connection is accepted from a client
                 */
                ServerSocket serverSocket = new ServerSocket(8888);
                Socket client = serverSocket.accept();

                /**
                 * If this code is reached, a client has connected and transferred data
                 * Save the input stream from the client as a JPEG file
                 */
                final File f = new File(Environment.getExternalStorageDirectory() + "/"
                        + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
                        + ".jpg");

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();
                InputStream inputstream = client.getInputStream();
                copyFile(inputstream, new FileOutputStream(f));
                serverSocket.close();
                return f.getAbsolutePath();
            } catch (IOException e) {
                Log.e("MainActivity2", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result != null){
                //statusText.setText("File copied - " + result);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                Toast.makeText(context,"in onPostExecute",Toast.LENGTH_LONG).show();
                context.startActivity(intent);
            }
        }



    }
    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d("MainActivity2", e.toString());
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                }
                return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //socket.close();

        if (null != serverThread) {
            //sendMessage("Disconnect");
            serverThread.interrupt();
            serverThread = null;
        }
    }
}

//****HELLO IMPORTANT*****working code for filethread
/*
 InputStream ims = null;
            try {
                ims = getAssets().open("board.png");
            } catch (IOException e) {
                e.printStackTrace();
            }
            // load image as Drawable
            final Drawable d = Drawable.createFromStream(ims, null);
            Log.d("DDDDD",""+d.toString());
            // set image to ImageView
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageDrawable(d);
                }
            });
 */





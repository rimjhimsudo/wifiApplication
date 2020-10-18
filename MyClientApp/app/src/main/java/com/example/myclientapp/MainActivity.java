package com.example.myclientapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.awt.image.BufferedImage;
//import javax.imageio.ImageIO;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int SERVERPORT = 3003;

    public static final String SERVER_IP = "192.168.0.101";
    private ClientThread clientThread;
    //ClientFileThread clientFileThread;
    private Thread thread;
    private LinearLayout msgList;
    private Handler handler;
    private int clientTextColor;
    private EditText edMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Client");
        clientTextColor = ContextCompat.getColor(this, R.color.colorAccent);
        handler = new Handler();
        msgList = findViewById(R.id.msgList);
        edMessage = findViewById(R.id.edMessage);
    }

    public TextView textView(String message, int color) {
        if (null == message || message.trim().isEmpty()) {
            message = "<Empty Message>";
        }
        TextView tv = new TextView(this);
        tv.setTextColor(color);
        tv.setText(message + " [" + getTime() + "]");
        tv.setTextSize(20);
        tv.setPadding(0, 5, 0, 0);
        return tv;
    }

    public void showMessage(final String message, final int color) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                msgList.addView(textView(message, color));
            }
        });
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.connect_server) {
            msgList.removeAllViews();
            showMessage("Connecting to Server...", clientTextColor);
            /*clientFileThread=new ClientFileThread();
            thread=new Thread(clientFileThread);
            thread.start();*/
            //this code for thread which was receiving and sending messages
            clientThread = new ClientThread();
            thread = new Thread(clientThread);
            thread.start();
            showMessage("Connected to Server...", clientTextColor);
            return;
        }

        if (view.getId() == R.id.send_data) {
            String clientMessage = edMessage.getText().toString().trim();
            showMessage(clientMessage, Color.BLUE);
            if (clientThread!=null) {
                clientThread.sendMessage(clientMessage);
            }
        }
    }


    class ClientThread implements Runnable {

        private Socket socket;
        private BufferedReader input;

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                Log.d("SERVER_IP",serverAddr.toString());
                socket = new Socket(serverAddr, SERVERPORT);

                while (!Thread.currentThread().isInterrupted()) {

                    this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message = input.readLine();
                    if (null == message || "Disconnect".contentEquals(message)) {
                        Thread.interrupted();
                        message = "Server Disconnected.";
                        showMessage(message, Color.RED);
                        break;
                    }
                    showMessage("Server: " + message, clientTextColor);
                }

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

        void sendMessage(final String message) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (null != socket) {
                            Log.d("MESSAGE "," client sending  : "+message);
                            PrintWriter out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())),
                                    true);
                            out.println(message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }

    String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != clientThread){
            clientThread.sendMessage("Disconnect");
            clientThread = null;
        }


    }
}


//ignore : this is for image transferring
 /*class ClientFileThread implements Runnable{
        private Socket socket;
        private FileInputStream f;
        @Override
        public void run() {
            InetAddress serverAddr = null;
            try {
                serverAddr = InetAddress.getByName(SERVER_IP);
                Log.d("SERVER_IP",serverAddr.toString());
                socket = new Socket(serverAddr, SERVERPORT);
                Log.d("Line","116"+Thread.currentThread().isInterrupted());
                while (!Thread.currentThread().isInterrupted()) {
                    Log.d("LineNO","125  : "+socket.getInputStream() + " : "+socket.getInputStream());
                    Log.d("WHILE115","inside");

                    //ObjectInputStream objectInputStream=new ObjectInputStream(socket.getInputStream());
                    DataInputStream din=new DataInputStream(socket.getInputStream());


                   *//*
                    byte[] sizear=new byte[3302900];
                    inputStream.read(sizear);
                   // int size1= ByteBuffer.wrap(sizear).asIntBuffer().get();
                    byte[] imagearray=new byte[3302900];
                    inputStream.read(sizear);

                    ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(imagearray);
                    Log.d("Line"," :  131  :"+byteArrayInputStream.available());*//*
                    //final Bitmap b = BitmapFactory.decodeStream(new BufferedInputStream(socket.getInputStream()));
                    //Log.d("Line119",""+b.getByteCount());
                    //Log.d("BITMAP",""+b.getByteCount());
                    //final Bitmap b=BitmapFactory.decodeByteArray(imagearray, 0, imagearray.length);
        *//*           MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ImageView img=findViewById(R.id.iv_showimg);
                            img.setImageBitmap(b);
                        }
                    });*//*
                    socket.close();
                }


            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }*/

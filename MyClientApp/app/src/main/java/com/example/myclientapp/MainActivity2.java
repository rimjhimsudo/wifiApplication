package com.example.myclientapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toast.makeText(this,"oncreate created",Toast.LENGTH_LONG).show();
        ClientFileThread clientFileThread=new ClientFileThread();
        Thread thread=new Thread(clientFileThread);
        thread.start();
    }

 class ClientFileThread implements Runnable{

     @Override
     public void run() {
         Context context = MainActivity2.this;
         //String host;
         //int port;
         int len;
         Socket socket = new Socket();
         byte buf[]  = new byte[1024];
         try {
             /**
              * Create a client socket with the host,
              * port, and timeout information.
              */
             socket.bind(null);
             socket.connect((new InetSocketAddress("192.168.0.102", 3003)), 500);

             /**
              * Create a byte stream from a JPEG file and pipe it to the output stream
              * of the socket. This data is retrieved by the server device.
              */
             OutputStream outputStream = socket.getOutputStream();
             ContentResolver cr = context.getContentResolver();
             InputStream inputStream = null;
             String pathtoimage= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+ "/Camera/test.jpg";
             inputStream = cr.openInputStream(Uri.parse(pathtoimage));
             while ((len = inputStream.read(buf)) != -1) {
                 outputStream.write(buf, 0, len);
             }

             outputStream.close();
             inputStream.close();
         } catch (FileNotFoundException e) {
             //catch logic
         } catch (IOException e) {
             //catch logic
         }

/**
 * Clean up any open sockets when done
 * transferring or if an exception occurred.
 */
         finally {
             if (socket != null) {
                 if (socket.isConnected()) {
                     try {
                         socket.close();
                     } catch (IOException e) {
                         //catch logic
                     }
                 }
             }
         }

     }
 }
}

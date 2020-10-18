package com.example.myserverapp;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ServerSocket serverSocket;
    private Socket tempClientSocket;
    Thread serverThread = null;
    public static final int SERVER_PORT = 3003;
    private LinearLayout msgList;
    private Handler handler;
    private int greenColor;
    private EditText edMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Server");
        greenColor = ContextCompat.getColor(this, R.color.colorAccent);
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
        if (view.getId() == R.id.start_server) {
            msgList.removeAllViews();
            this.serverThread = new Thread(new ServerThread());
            this.serverThread.start();
            showMessage("Server Started.", Color.BLACK);
            return;
        }
        if (view.getId() == R.id.send_data) {
            String msg = edMessage.getText().toString().trim();
            showMessage("Server : " + msg, Color.BLUE);
            sendMessage(msg);
        }
    }

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
    }

    class ServerThread implements Runnable {

        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                //findViewById(R.id.start_server).setVisibility(View.GONE);
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("Error Starting Server : " + e.getMessage(), Color.RED);
            }
            if (null != serverSocket) {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        socket = serverSocket.accept();
                        Log.d("SOCKET value :",socket.toString());
                        CommunicationThread commThread = new CommunicationThread(socket);
                        new Thread(commThread).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                        showMessage("Error Communicating to Client :" + e.getMessage(), Color.RED);
                    }
                }
            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            tempClientSocket = clientSocket;
            try {
                //this.input = new BufferedReader(new InputStreamReader(tempClientSocket.getInputStream()));
                 this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
                //showMessage("Error Connecting to Client!!", Color.RED);
            }
            //showMessage("Connected to Client!!", greenColor);
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    if (null == read || "Disconnect".contentEquals(read)) {
                        Thread.interrupted();
                        read = "Client Disconnected";
                        //showMessage("Client : " + read, greenColor);
                        Log.d("SERVERSIDE","CLient msg on server recived"+read);
                        break;
                    }
                    showMessage("Client : " + read, greenColor); //take msg from read coming from client side
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }

    String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != serverThread) {
            sendMessage("Disconnect");
            serverThread.interrupt();
            serverThread = null;
        }
    }
}



/*
public class FileTxThread extends Thread {
  Socket socket;

  FileTxThread(Socket socket){
   this.socket= socket;
  }

  @Override
  public void run() {
   File file = new File(
     Environment.getExternalStorageDirectory(),
     "android-er_sketch_1000.png");

   byte[] bytes = new byte[(int) file.length()];
   BufferedInputStream bis;
   try {
    bis = new BufferedInputStream(new FileInputStream(file));
    bis.read(bytes, 0, bytes.length);

    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
    oos.writeObject(bytes);
    oos.flush();

    socket.close();

    final String sentMsg = "File sent to: " + socket.getInetAddress();
    MainActivity.this.runOnUiThread(new Runnable() {

     @Override
     public void run() {
      Toast.makeText(MainActivity.this,
        sentMsg,
        Toast.LENGTH_LONG).show();
     }});

   } catch (FileNotFoundException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
   } catch (IOException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
   } finally {
    try {
     socket.close();
    } catch (IOException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
    }
   }

  }
 }
 */

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




//not working function
/*private File createFileFromInputStream(InputStream inputStream) {

            try{
                File f = new File("board.png");
                OutputStream outputStream = new FileOutputStream(f);
                byte buffer[] = new byte[1024];
                int length = 0;

                while((length=inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer,0,length);
                }

                outputStream.close();
                inputStream.close();

                return f;
            }catch (IOException e) {
                //Logging exception
            }

            return null;
        }*/

/*
public  class  FileTransthread extends Thread {
        Socket socket;

        public FileTransthread(Socket socket) {
            this.socket = socket;

        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void run() {
            //get absolute path to image
            String pathtoimage= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+ "/Camera/test.jpg";
            //file is just only a representation,you cannot it or get bytes from it.
            File file=new File(pathtoimage);
            byte[] bytes=new byte[(int)file.length()];
            Log.d("IMAGE","size : "+bytes.length + ",   canREAD  : "+file.canRead() + ",  exists: "+file.exists());
            Bitmap b = null;
            if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) {
                try {
                    Log.d("PERMISSION", "checkselfperm worked");
                    ///fileinputstream reads the data
                    b = BitmapFactory.decodeStream(new FileInputStream(file));

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                final Bitmap finalB = b;
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView img=(ImageView)findViewById(R.id.imageview);
                        img.setImageBitmap(finalB);
                    }
                });
            }
            else {
                Log.d("WTF","sorryyyy soryyy sorrry");
                requestPermissions( new String[] { Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE);
            }
            BufferedInputStream bufferedInputStream;
            try {
                //what use of next two line
                bufferedInputStream=new BufferedInputStream(new FileInputStream(file));
                bufferedInputStream.read(bytes,0,bytes.length);
                ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(bytes);
                Log.d("LINENO223",""+bufferedInputStream.toString());
                objectOutputStream.flush();
                socket.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
 */
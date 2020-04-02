package ru.romanLab.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;


// основной класс соедиениея
public class TCPConnection {

    private final Socket socket;
    private final Thread rxThread; // поток для входящих сообщений
    private final TCPConnectionListener eventListener; // слушатель событий
    private final BufferedReader in;
    private final BufferedWriter out;

    // сокет создавшийся внутри
    public TCPConnection(TCPConnectionListener eventListener, String ipAddr, int port) throws IOException {
        this(eventListener, new Socket(ipAddr, port));
    }

    // конструктор соединения и генериует исключения(IOException)
     public TCPConnection(TCPConnectionListener eventListener, Socket socket) throws IOException {
         this.eventListener = eventListener;
         this.socket = socket;
         in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8"))); // входящий поток
         out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8"))); // исходящий поток
         rxThread = new Thread(new Runnable() { // создать входящий поток
             @Override
             public void run() {
                 try {
                     eventListener.onConnectionReady(TCPConnection.this);
                     while (!rxThread.isInterrupted()) { // пока не прерван
                         eventListener.onReceiveString(TCPConnection.this, in.readLine());
                     }
                 } catch (IOException e) { // ловим и обрвбвтываем
                     eventListener.onException(TCPConnection.this, e);
                 } finally {
                    eventListener.onDisconnect(TCPConnection.this);
                 }
             }
         });
         rxThread.start();
     }

     //---- обращаемся из разных потоков -----
    // функции синхранизированны по этому безопасно обращаемся из разных потоков
     // отправить сообщение
     public synchronized void sendString(String value) {
        try {
            out.write(value + "\r\n" );
            out.flush(); // сбрасывает все в буфер и отпраляет
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
     }

     // оборвать соединение
     public synchronized void disconnect() {
         rxThread.interrupt();
         try {
             socket.close();
         } catch (IOException e) {
             eventListener.onException(TCPConnection.this, e);
         }
     }
    //---- ############################### -----

     @Override
    public String toString() { // проверяем кто подключился(отключился)
         return "TCPConnection: " + socket.getInetAddress() + ": " + socket.getPort();
     }

}

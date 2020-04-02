package ru.romanLab.chat.client;

import ru.romanLab.network.TCPConnection;
import ru.romanLab.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener { // добовляем фрейворк и интерфейс

    private static final String PI_ADD = "127.0.0.1";
    private static final int PORT = 8189;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;

    public static void main(String[] args) {

        // поток EDT для мноототочности в графике
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientWindow();
            }
        });

    }

    private final JTextArea log = new JTextArea();
    private final JTextField fieldNickname = new JTextField("roman");
    private final JTextField fieldInput = new JTextField();

    private TCPConnection connection;

    private ClientWindow() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);// что бы окно позицианировалось по середине
        setAlwaysOnTop(true); // что бы окно открывалость только поверх

        log.setEditable(false); // запредить редактирование
        log.setLineWrap(true);
        add(log, BorderLayout.CENTER);

        add(fieldInput, BorderLayout.SOUTH);
        add(fieldNickname, BorderLayout.NORTH);

        fieldInput.addActionListener(this);

        setVisible(true); // see the window
        try {
            connection = new TCPConnection(this, PI_ADD, PORT);
        } catch (IOException e) {
            printMsg("Connection exception" + e);
        }
    }


    // настройки для отправки сообщения
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String msg = fieldInput.getText();
        if(msg.equals("")) return;
        fieldInput.setText(null);
        connection.sendString(fieldNickname.getText() + ": " + msg);
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMsg("Connection ready!");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        printMsg(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMsg("Connection close!");
    }


    // проверка исключений
    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMsg("Connection exception" + e);
    }

    private synchronized void printMsg(String msg){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());// поднимает сообщение вверх
            }
        });
    }
}

package ru.romanLab.network;


// события TCP соединения
public interface TCPConnectionListener {

    void onConnectionReady(TCPConnection tcpConnection); // подключение
    void onReceiveString(TCPConnection tcpConnection, String value); // приняли строчку
    void onDisconnect(TCPConnection tcpConnection); // отключение
    void onException(TCPConnection tcpConnection, Exception e); // исключение
}

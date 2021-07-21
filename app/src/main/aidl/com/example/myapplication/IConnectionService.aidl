package com.example.myapplication;

/**
* 连接状态服务
*/
interface IConnectionService {
    //建立连接
    oneway void connect();
    //断开连接
    void disconnect();
    //判断是否已连接
    boolean isConnected();
}
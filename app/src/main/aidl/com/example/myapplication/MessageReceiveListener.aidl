package com.example.myapplication;

//导包
import com.example.myapplication.Message;

/**
* 管理消息监听
*/
interface MessageReceiveListener {

    //子进程发送消息，主进程接收消息
    void onReceiveMessage(in Message message);
}
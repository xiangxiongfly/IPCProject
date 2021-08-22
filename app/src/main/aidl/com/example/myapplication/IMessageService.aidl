package com.example.myapplication;

//导包
import com.example.myapplication.Message;
import com.example.myapplication.MessageReceiveListener;

/**
* 发送消息服务
*/
interface IMessageService {
    //发送消息
    //实体类需要标记关键字in，基本类型不用
    //主进程发送消息，子进程接收消息
    void sendMessageForServer(in Message message);

    //注册监听
    void registerMessageReceiveListener(MessageReceiveListener messageReceiveListener);

    //取消注册
    void unregisterMessageReceiveListener(MessageReceiveListener messageReceiveListener);
}
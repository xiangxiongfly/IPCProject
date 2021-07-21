package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RemoteService extends Service {

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            super.handleMessage(msg);
            receive(msg);
            send(msg);
        }

        private void receive(@NotNull android.os.Message msg) {
            Bundle data = msg.getData();
            data.setClassLoader(Message.class.getClassLoader());//防止序列化问题
            Message message = data.getParcelable("message");
            Utils.toast(message.getContent());
        }

        private void send(@NonNull android.os.Message msg) {
            try {
                Message message = new Message();
                message.setContent("Messenger回复一个消息");

                android.os.Message data = android.os.Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putParcelable("message", message);
                data.setData(bundle);

                Messenger m = msg.replyTo;
                m.send(data);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private boolean isConnected = false;

    private RemoteCallbackList<MessageReceiveListener> receiverList = new RemoteCallbackList<>();

    private ScheduledThreadPoolExecutor mScheduledThreadPoolExecutor;

    private ScheduledFuture mScheduledFuture;

    private Messenger messenger = new Messenger(handler);


    private IConnectionService.Stub connectionService = new IConnectionService.Stub() {
        @Override
        public void connect() throws RemoteException {
            handler.post(() -> Utils.toast("开始连接，等待5s"));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.post(() -> Utils.toast("连接成功"));
            isConnected = true;

            mScheduledFuture = mScheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    int size = receiverList.beginBroadcast();
                    for (int i = 0; i < size; i++) {
                        Message message = new Message();
                        message.setContent("子进程发送消息-AAA");
                        try {
                            //向客户端发送消息
                            receiverList.getBroadcastItem(i).onReceiveMessage(message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    receiverList.finishBroadcast();
                }
            }, 5000, 5000, TimeUnit.MILLISECONDS);
        }

        @Override
        public void disconnect() throws RemoteException {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Utils.toast("断开连接");
                }
            });

            isConnected = false;
            mScheduledFuture.cancel(true);
        }

        @Override
        public boolean isConnected() throws RemoteException {
            return isConnected;
        }
    };


    private IMessageService.Stub messageService = new IMessageService.Stub() {
        @Override
        public void sendMessage(Message message) throws RemoteException {
            //接收客户端的消息
            handler.post(() -> Utils.toast(message.getContent()));
            message.setSendSuccess(isConnected);
        }

        @Override
        public void registerMessageReceiveListener(MessageReceiveListener messageReceiveListener) throws RemoteException {
            receiverList.register(messageReceiveListener);
        }

        @Override
        public void unregisterMessageReceiveListener(MessageReceiveListener messageReceiveListener) throws RemoteException {
            receiverList.unregister(messageReceiveListener);
        }
    };


    private IServiceManager.Stub serviceManager = new IServiceManager.Stub() {

        @Override
        public IBinder getService(String serviceName) throws RemoteException {
            if (IConnectionService.class.getSimpleName().equals(serviceName)) {
                return connectionService.asBinder();
            } else if (IMessageService.class.getSimpleName().equals(serviceName)) {
                return messageService.asBinder();
            } else if (Messenger.class.getSimpleName().equals(serviceName)) {
                return messenger.getBinder();
            } else {
                return null;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceManager.asBinder();
    }
}

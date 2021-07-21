package com.example.myapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private IServiceManager serviceManagerProxy;

    private IConnectionService connectionServiceProxy;

    private IMessageService messageServiceProxy;

    //发送消息的Messenger
    private Messenger sendMessenger;


    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            data.setClassLoader(Message.class.getClassLoader());//防止序列化问题
            Message message = data.getParcelable("message");

            postAtTime(() -> {
                Utils.toast(message.getContent());
            }, 3000);
        }
    };
    //接收消息的Messenger
    private Messenger receiveMessenger = new Messenger(handler);

    private MessageReceiveListener.Stub messageReceiverListener = new MessageReceiveListener.Stub() {

        @Override
        public void onReceiveMessage(Message message) throws RemoteException {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    //接收服务端的消息
                    Utils.log(message.getContent());
                }
            });
        }
    };

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = new Intent(this, RemoteService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                try {
                    serviceManagerProxy = IServiceManager.Stub.asInterface(service);

                    connectionServiceProxy = IConnectionService.Stub.asInterface(serviceManagerProxy.getService(IConnectionService.class.getSimpleName()));

                    messageServiceProxy = IMessageService.Stub.asInterface(serviceManagerProxy.getService(IMessageService.class.getSimpleName()));

                    sendMessenger = new Messenger(serviceManagerProxy.getService(Messenger.class.getSimpleName()));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);


        binding.connect.setOnClickListener(v -> {
            try {
                connectionServiceProxy.connect();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        binding.disconnect.setOnClickListener(v -> {
            try {
                connectionServiceProxy.disconnect();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        binding.isconnected.setOnClickListener(v -> {
            try {
                boolean isconnected = connectionServiceProxy.isConnected();
                Utils.toast(isconnected ? "连接成功" : "连接失败");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        binding.sendMessage.setOnClickListener(v -> {
            Message message = new Message();
            message.setContent("主进程发送消息-BBB");
            try {
                //向服务端发送消息
                messageServiceProxy.sendMessage(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        binding.register.setOnClickListener(v -> {
            try {
                messageServiceProxy.registerMessageReceiveListener(messageReceiverListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        binding.unregister.setOnClickListener(v -> {
            try {
                messageServiceProxy.unregisterMessageReceiveListener(messageReceiverListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        binding.messenger.setOnClickListener(v -> {
            try {
                Message message = new Message();
                message.setContent("messenger发送了一个消息");
                android.os.Message data = android.os.Message.obtain();
                data.replyTo = receiveMessenger;//设置用于接收消息的Messenger
                Bundle bundle = new Bundle();
                bundle.putParcelable("message", message);
                data.setData(bundle);
                sendMessenger.send(data);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }
}

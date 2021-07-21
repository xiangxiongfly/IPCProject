package com.example.myapplication;

//管理类
interface IServiceManager {

    IBinder getService(String serviceName);
}
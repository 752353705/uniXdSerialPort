package com.example.xd_serialport;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.math.BigInteger;

import android_serialport_api.Device;
import android_serialport_api.SerialPortManager;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

public class XdSerialPort extends UniModule  {

    private SerialPortManager serialPortManager;
    public String serialPortData = "";
    public Boolean lockState = false;
    public Boolean isOpen = false;

    /**
     * 测试插件是否正常引入
     * @param callback
     */
    @UniJSMethod(uiThread = true)
    public void testPlugin (UniJSCallback callback) {
        if (callback != null) {
            JSONObject data = new JSONObject();
            data.put("msg", "插件引入成功");
            data.put("code", 200);
            callback.invoke(data);
        }
    }

    /**
     * 打开串口
     */
    @UniJSMethod(uiThread = true)
    public void openSerialPort(String path, int speed, UniJSCallback callback) {

        if(serialPortManager == null) {
            Device device = new Device();
            device.path = path;
            device.speed = speed;
            serialPortManager = new SerialPortManager(device);

            if(serialPortManager == null) {
                isOpen = false;
            } else {
                isOpen = true;
            }

            if (callback != null) {
                JSONObject data = new JSONObject();
                data.put("msg", "串口打开成功");
                data.put("isOpen", isOpen);
                data.put("code", 200);
                callback.invoke(data);
            }

            lockState = false;
            serialPortManager.setOnDataReceiveListener(new SerialPortManager.OnDataReceiveListener() {
                @Override
                public void onDataReceive(byte[] recvBytes, int i) {
                    if (recvBytes != null && recvBytes.length > 0) {
                        serialPortData = bytesToHexString(recvBytes, recvBytes.length);
                        lockState = true;
                    }
                }
            });
        }

    }

    /**
     * 发送数据
     */
    @UniJSMethod(uiThread = true)
    public void sendPacket(String sendStr, UniJSCallback callback) {
        byte[] bytes = hexString2Bytes(sendStr);
        lockState = false;
        serialPortManager.sendPacket(bytes);
        if (callback != null) {
            JSONObject data = new JSONObject();
            data.put("msg", "数据发送成功");
            data.put("code", 200);
            callback.invoke(data);
        }
    }

    /**
     * 判断当前数据是否正常返回了
     * 锁的状态
     */
    @UniJSMethod(uiThread = true)
    public void getLockState(UniJSCallback callback) {
        if (callback != null) {
            JSONObject data = new JSONObject();
            data.put("state", lockState);
            callback.invoke(data);
        }
    }

    /**
     * 获取串口数据
     */
    @UniJSMethod(uiThread = true)
    public void getPortData(UniJSCallback callback) {
        if (callback != null) {
            JSONObject data = new JSONObject();
            data.put("data", serialPortData);
            data.put("code", 200);
            callback.invoke(data);
        }
    }

    /**
     * 关闭当前串口
     * @return
     */
    @UniJSMethod(uiThread = true)
    public void closeSerialPort(UniJSCallback callback) {

        if (serialPortManager != null) {
            serialPortManager.closeSerialPort();
        }
        serialPortManager = null;
        isOpen = false;
        serialPortData = "";

        if (callback != null) {
            JSONObject data = new JSONObject();
            data.put("msg", "关闭串口成功");
            data.put("code", 200);
            callback.invoke(data);
        }
    }

    private byte[] hexString2Bytes(String src) {
        byte[] ret = new byte[src.length() / 2];
        byte[] tmp = src.getBytes();
        for (int i = 0; i < tmp.length / 2; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    private byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0}));
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1}));
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

    /**
     * 十六进制转换
     * @param src
     * @param size
     * @return
     */
    private String bytesToHexString(byte[] src, int size) {
        String ret = "";
        if (src == null || size <= 0) {
            return null;
        }
        for (int i = 0; i < size; i++) {
            String hex = Integer.toHexString(src[i] & 0xFF);
            if (hex.length() < 2) {
                hex = "0" + hex;
            }
            hex += " ";
            ret += hex;
        }
        return ret.toUpperCase();
    }

}
package com.webrtc.config;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * @ServerEndpoint 注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端,
 * 注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
 */
@ServerEndpoint(value = "/websocket")
@Component
public class WebSocket {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static CopyOnWriteArraySet<Map<String,WebSocket>> webSocketSet = new CopyOnWriteArraySet<Map<String,WebSocket>>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    /**
     * 连接建立成功调用的方法
     * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     * @throws EncodeException
     * @throws IOException 
     */
    @OnOpen
    public void onOpen(Session session) throws EncodeException, IOException{
        this.session = session;
        Map<String,WebSocket> map = new HashMap<String,WebSocket>();
        String name = "";
        Map<String, List<String>> listMap = session.getRequestParameterMap();
        // 非主播建立连接
        if (listMap.get("name") != null && listMap.get("receiver") != null) {
            name = listMap.get("name").get(0);
            String receiver = listMap.get("receiver").get(0);
            map.put(name,this);
            // 通知主播发送__offer指令
            this.onMessage("{\"name\": \"" + name + "\",\"receiver\": \"" + receiver + "\"}", session);
        } else {
            // 主播建立连接
            name = listMap.get("name").get(0);
            map.put(name,this);
        }
        addSocket(map, name);
    }

    // 添加map 到 webSocketSet，
    public void addSocket(Map<String,WebSocket> map, String name) {
        // 删除重复的连接
        for(Map<String,WebSocket> item: webSocketSet){
            for(String key : item.keySet()){
                if (key.toString().equals(name)) {
                    webSocketSet.remove(item);
                    subOnlineCount();           //在线数减1
                    System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
                }
            }
        }
        webSocketSet.add(map); //加入set中
        addOnlineCount();           //在线数加1
        System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(){
        for (Map<String,WebSocket> item : webSocketSet) {
            for(String key : item.keySet()){
                if(item.get(key) == this){
                    // 删除关闭的连接
                    webSocketSet.remove(item);
                    subOnlineCount();           //在线数减1
                    System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
                }
            }
        }
    }

    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     * @throws EncodeException
     */
    @OnMessage
    public void onMessage(String message, Session session) throws EncodeException {
        System.out.println("来自客户端的消息:" + message);
        Map<String,Object> map = (Map<String, Object>) JSONObject.parse(message);
        // 接收人
        String receiver = (String) map.get("receiver");

        for(Map<String,WebSocket> item: webSocketSet){
            for(String key : item.keySet()){
                if (key.toString().equals(receiver.toString())) {
                    WebSocket webSocket = item.get(key);
                    try {
                        webSocket.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }

        }
    }

    /**
     * 发生错误时调用
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error){
        System.out.println("发生错误");
        error.printStackTrace();
    }

    /**
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException{
        synchronized (this.session) {
            this.session.getBasicRemote().sendText(message);
        }
    }
    

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocket.onlineCount--;
    }
}
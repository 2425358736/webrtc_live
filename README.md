## webrtc 搭建直播平台

### 设计思路
需求： 一个直播页面，可以输入直播名。一个观看页面输入客户名个要看的直播名建立直播视频传输

思路： 
1. 直播页面输入直播名建立websocket连接，创建PeerConnection对象组存放连接本直播端的PeerConnection对象。
2. 观看页面输入客户名与直播名建立websocket连接，通知直播端发送__offer给观看端
3. 观看接收到__offer指令,将__offer中携带的ice会话描述信息加入PeerConnection中并发起一个_answer和n个__ice_candidate指令给直播端
4. 直播端收到__answer 和 __ice_candidate指定 追加至 对应的PeerConnection对象中

### 引入的maven依赖

```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.5.10.RELEASE</version>
    </parent>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!--测试类包-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- freemarker模板引擎-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-freemarker</artifactId>
        </dependency>

        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.31</version>
        </dependency>
        <!--websocket-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
    </dependencies>
```

### 直播页面

```html
<!doctype html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>播放页</title>
</head>
<body>
<video id="video" autoplay></video>

<span>主播名</span>
<input id="zhubo" type="text"/>
<button onclick="connect()">建立视频连接</button>

</body>

<script type="text/javascript">
    /**
     * socket.send 数据描述
     * event: 指令类型
     * data: 数据
     * name: 发送人
     * receiver: 接收人
     *
     * */

            //使用Google的stun服务器
    const iceServer = {
                "iceServers": [{
                    "url": "stun:stun.l.google.com:19302"
                }, {
                    "url": "turn:numb.viagenie.ca",
                    "username": "webrtc@live.com",
                    "credential": "muazkh"
                }]
            };
    //兼容浏览器的getUserMedia写法
    const getUserMedia = (navigator.getUserMedia || navigator.mozGetUserMedia || navigator.webkitGetUserMedia || navigator.msGetUserMedia);
    //兼容浏览器的PeerConnection写法
    const PeerConnection = (window.PeerConnection ||
    window.webkitPeerConnection00 ||
    window.webkitRTCPeerConnection ||
    window.RTCPeerConnection ||
    window.mozRTCPeerConnection);

    /**
     * 信令websocket
     * @type {WebSocket}
     */
    var socket;

    /**
     * 视频信息
     * */
    var stream_two;

    /**
     * 播放视频video组件
     * */
    const video = document.getElementById('video');


    /**
     * 连接的浏览器PeerConnection对象组
     * {
     *  'id':PeerConnection
     * }
     * @type {{}}
     */
    var pc = {};

    // 建立scoket连接
    function connect() {
        // 获取主播名称
        const zhubo = document.getElementById('zhubo').value;

        /**
         * 信令websocket
         * @type {WebSocket}
         */
        socket = new WebSocket("ws://192.168.31.13:6533/websocket?name=" + zhubo);

        //获取本地的媒体流，并绑定到一个video标签上输出，并且发送这个媒体流给其他客户端
        getUserMedia.call(navigator, {
            "audio": true,
            "video": true
        }, function (stream) {
            //发送offer和answer的函数，发送本地session描述
            stream_two = stream;
            video.srcObject = stream
            //向PeerConnection中加入需要发送的流

            //如果是发送方则发送一个offer信令，否则发送一个answer信令
        }, function (error) {
            //处理媒体流创建失败错误
            alert("处理媒体流创建失败错误");
        });


        socket.close = function () {
            console.log("连接关闭")
        }

        //有浏览器建立视频连接
        socket.onmessage = function (event) {
            var json = JSON.parse(event.data);
            if (json.name && json.name != null && !json.event) {
                pc[json.name] = new PeerConnection(iceServer);
                pc[json.name].addStream(stream_two);
                // 浏览器兼容
                var agent = navigator.userAgent.toLowerCase();
                if (agent.indexOf("firefox") > 0) {
                    pc[json.name].createOffer().then(function (desc) {
                        pc[json.name].setLocalDescription(desc);
                        socket.send(JSON.stringify({
                            "event": "__offer",
                            "data": {
                                "sdp": desc
                            },
                            name: zhubo,
                            receiver: json.name
                        }));
                    });
                } else if (agent.indexOf("chrome") > 0) {
                    pc[json.name].createOffer(function (desc) {
                        pc[json.name].setLocalDescription(desc);
                        socket.send(JSON.stringify({
                            "event": "__offer",
                            "data": {
                                "sdp": desc
                            },
                            name: zhubo,
                            receiver: json.name
                        }));
                    });
                } else {
                    pc[json.name].createOffer(function (desc) {
                        pc[json.name].setLocalDescription(desc);
                        socket.send(JSON.stringify({
                            "event": "__offer",
                            "data": {
                                "sdp": desc
                            },
                            name: zhubo,
                            receiver: json.name
                        }));
                    });
                }
            } else {
                if (json.event === "__ice_candidate") {
                    //如果是一个ICE的候选，则将其加入到PeerConnection中
                    pc[json.name].addIceCandidate(new RTCIceCandidate(json.data.candidate));
                } else if (json.event === "__answer") {
                    // 将远程session描述添加到PeerConnection中
                    pc[json.name].setRemoteDescription(new RTCSessionDescription(json.data.sdp));
                }
            }
        };
    }

</script>
</html>
```

### 观看页面

```html
<!doctype html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>观看页</title>
</head>
<body>
<div id="eee">
    <video id="video" autoplay></video>
</div>
<span>用户名</span><input id="userName"/>
<span>主播名</span><input id="receiver"/>
<button onclick="communication()">建立视频通信</button>

</body>
<script type="text/javascript">
    /**
     * socket.send 数据描述
     * event: 指令类型
     * data: 数据
     * name: 发送人
     * receiver: 接收人
     *
     * */


            //使用Google的stun服务器
    const iceServer = {
                "iceServers": [{
                    "url": "stun:stun.l.google.com:19302"
                }, {
                    "url": "turn:numb.viagenie.ca",
                    "username": "webrtc@live.com",
                    "credential": "muazkh"
                }]
            };
    //兼容浏览器的getUserMedia写法
    const getUserMedia = (navigator.getUserMedia || navigator.mozGetUserMedia || navigator.webkitGetUserMedia || navigator.msGetUserMedia);
    //兼容浏览器的PeerConnection写法
    const PeerConnection = (window.PeerConnection ||
    window.webkitPeerConnection00 ||
    window.webkitRTCPeerConnection ||
    window.mozRTCPeerConnection);


    /**
     * 信令websocket
     * @type {WebSocket}
     */
    var socket;


    function communication() {

        //创建PeerConnection实例
        var pc = new PeerConnection(iceServer);

        const video = document.getElementById('video');

        //如果检测到媒体流连接到本地，将其绑定到一个video标签上输出
        pc.ontrack = function async(event) {
            video.srcObject = event.streams[0]
        };


        const userName = document.getElementById('userName').value;
        const receiver = document.getElementById('receiver').value;
        /**
         * 信令websocket
         * @type {WebSocket}
         */
        socket = new WebSocket("ws://192.168.31.13:6533/websocket?name=" + userName + "&receiver=" + receiver);


        socket.close = function () {
            console.log("连接关闭")
        }
        //处理到来的信令
        socket.onmessage = function (event) {
            var json = JSON.parse(event.data);

            //如果是一个ICE的候选，则将其加入到PeerConnection中
            if (json.event === "__offer") {
                pc.setRemoteDescription(new RTCSessionDescription(json.data.sdp));
                pc.onicecandidate = function (event) {
                    if (event.candidate !== null && event.candidate !== undefined && event.candidate !== '') {
                        socket.send(JSON.stringify({
                            "event": "__ice_candidate",
                            "data": {
                                "candidate": event.candidate
                            },
                            name: userName,
                            receiver: receiver,
                        }));
                    }
                };

                var agent = navigator.userAgent.toLowerCase();
                if (agent.indexOf("firefox") > 0) {
                    pc.createAnswer().then(function (desc) {
                        pc.setLocalDescription(desc);
                        socket.send(JSON.stringify({
                            "event": "__answer",
                            "data": {
                                "sdp": desc
                            },
                            name: userName,
                            receiver: receiver,
                        }));
                    });
                } else {
                    pc.createAnswer(function (desc) {
                        pc.setLocalDescription(desc);
                        socket.send(JSON.stringify({
                            "event": "__answer",
                            "data": {
                                "sdp": desc
                            },
                            name: userName,
                            receiver: receiver,
                        }));
                    }, function (eorr) {
                        alert(eorr);
                    });
                }
            }
        };


    }


</script>


</html>
```

### WebSocket处理类

```java
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
```

源码地址：https://github.com/2425358736/webrtc_live.git


### 截图
直播页
![image](https://raw.githubusercontent.com/2425358736/webrtc_live/master/zhibo.png)
观看页
![image](https://raw.githubusercontent.com/2425358736/webrtc_live/master/guankan.png)







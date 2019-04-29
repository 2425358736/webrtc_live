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

// name 发送人 receiver 接收人
<script type="text/javascript">


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

    var stream_two;

    const video = document.getElementById('video');


    /**
     * 连接的浏览器
     * {
     *  'id':PeerConnection
     * }
     * @type {{}}
     */
    var pc = {};

    // 建立scoket连接
    function connect() {
        const zhubo = document.getElementById('zhubo').value;

        /**
         * 信令websocket
         * @type {WebSocket}
         */
        if (socket) {
            socket.close()
        }
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

        //有浏览器简历视频连接
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
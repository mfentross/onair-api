window.onload = function () {

    var transceiver = new MediaStreamTransceiver("ws://localhost:9000/ws/broadcast");
//    var videoDevice = document.getElementsByTagName("device")[ 0 ];

//    videoDevice.onchange = function (evt) {
//        var videoStream = videoDevice.data;
//        var selfView = document.getElementById("self_view");
//
//        // exclude audio from the self view
//        selfView.src = videoStream.url + "#video";
//        selfView.play();
//
//        // set the stream to share
//        transceiver.localStream = videoStream;
//    };

    video = document.getElementById("live_video");
    navigator.webkitGetUserMedia({video:true, audio:true},
        function(stream) {
            console.log(stream);
            video.src = window.webkitURL.createObjectURL(stream);
            transceiver.localStram = stram;
            console.log(stream);
        }
    );



}
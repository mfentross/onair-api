package models.pubnub;

import com.amazonaws.util.json.JSONObject;
import com.pubnub.api.Callback;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import play.api.Logger;

/**
 * Copyright: AppBuddy GmbH
 * User: maltefentross
 * Date: 29.06.14
 * Time: 19:28
 */
public class PNInit {

    private static String channel = "chat";
    private static play.Logger.ALogger log = play.Logger.of("PubNub stuff");

    public static void doInit() {

        try {
            PNConnection.connection().subscribe(channel, new Callback() {
                @Override
                public void successCallback(String s, Object o) {
//                    log.error(o.toString());
                    MessagesHandler.received(o);
                }

                @Override
                public void successCallback(String s, Object o, String s2) {
//                    log.error(o.toString());
                    MessagesHandler.received(o);
                }

                @Override
                public void errorCallback(String s, PubnubError pubnubError) {
                    super.errorCallback(s, pubnubError);    //To change body of overridden methods use File | Settings | File Templates.
                    log.error("Pubnub error: " + pubnubError.errorObject);
                }

                @Override
                public void errorCallback(String s, Object o) {
                    super.errorCallback(s, o);    //To change body of overridden methods use File | Settings | File Templates.
                }

                @Override
                public void connectCallback(String s, Object o) {
                    super.connectCallback(s, o);    //To change body of overridden methods use File | Settings | File Templates.
                    log.info("Pubnub successfully connected");
                }

                @Override
                public void reconnectCallback(String s, Object o) {
                    super.reconnectCallback(s, o);    //To change body of overridden methods use File | Settings | File Templates.
                }

                @Override
                public void disconnectCallback(String s, Object o) {
                    super.disconnectCallback(s, o);    //To change body of overridden methods use File | Settings | File Templates.
                }
            });
        } catch (PubnubException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void sendMessage(org.json.JSONObject m) {
//        log.info(m.toString());
        Callback callback = new Callback() {
            public void successCallback(String channel, Object response) {
//                super.successCallback(channel, response);
                log.info("PUBNUB sent");
            }
            public void errorCallback(String channel, PubnubError error) {
//                super.errorCallback(channel, error);
                log.error("PUBNUB error while sending: " + error.toString());
            }
        };
        PNConnection.connection().publish(channel, m, callback);
    }

}

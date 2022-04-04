package ti.vonage;

import android.Manifest;
import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

@Kroll.module(name = "TiVonage", id = "ti.vonage", propertyAccessors = {"apiKey", "token", "sessionId", "audioOnly"})
public class TiVonageModule extends KrollModule implements Session.SessionListener, PublisherKit.PublisherListener {

    // Standard Debugging variables
    private static final String LCAT = "TiVonageModule";
    private static final boolean DBG = TiConfig.LOGD;
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;
    private static String API_KEY = "";
    private static String SESSION_ID = "";
    private static String TOKEN = "";
    private Session mSession;
    private FrameLayout mPublisherViewContainer;
    private ConstraintLayout mSubscriberViewContainer;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private boolean audioOnly = false;
    private String permissionsText = "This app needs access to your camera and mic to make video calls";

    public TiVonageModule() {
        super();
    }

    @Kroll.onAppCreate
    public static void onAppCreate(TiApplication app) {
    }

    @Override
    public void handleCreationDict(KrollDict options) {
        super.handleCreationDict(options);
    }

    @Override
    public void processProperties(KrollDict d) {
        super.processProperties(d);
        if (d.containsKey("apiKey")) {
            API_KEY = (d.getString("apiKey"));
        }
        if (d.containsKey("sessionId")) {
            SESSION_ID = (d.getString("sessionId"));
        }
        if (d.containsKey("token")) {
            TOKEN = (d.getString("token"));
        }
        if (d.containsKey("audioOnly")) {
            audioOnly = (d.getBoolean("audioOnly"));
        }
    }

    @Override
    public void propertyChanged(String key, Object oldValue, Object newValue, KrollProxy proxy) {
        KrollDict d = new KrollDict();
        d.put(key, newValue);
        processProperties(d);
    }

    @Kroll.method
    public void connect() {
        Activity activity = TiApplication.getAppCurrentActivity();
        mSession = new Session.Builder(activity, API_KEY, SESSION_ID).build();
        mSession.setSessionListener(this);
        mSession.connect(TOKEN);
    }

    // Methods
    @Kroll.method
    public void disconnect() {
        if (mSession != null) {
            mSession.disconnect();
        }
    }

    @Override
    public void onConnected(Session session) {
        Log.d(LCAT, "Session Connected");
        Publisher.Builder pb = new Publisher.Builder(TiApplication.getAppCurrentActivity());
        if (audioOnly) {
            pb.videoTrack(false);
        }
        mPublisher = pb.build();
        mPublisher.setPublisherListener(this);

        KrollDict kd = new KrollDict();
        VideoProxy vp = new VideoProxy(mPublisher.getView());
        vp.createView(TiApplication.getAppCurrentActivity());

        kd.put("view", vp);
        kd.put("userType", "published");
        fireEvent("streamReceived", kd);
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.d(LCAT, "Session Disconnected");
        fireEvent("disconnected", new KrollDict());
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.d(LCAT, "Stream Received");
        mSubscriber = new Subscriber.Builder(TiApplication.getAppCurrentActivity(), stream).build();
        mSession.subscribe(mSubscriber);

        KrollDict kd = new KrollDict();
        VideoProxy vp = new VideoProxy(mSubscriber.getView());
        vp.createView(TiApplication.getAppCurrentActivity());

        kd.put("view", vp);
        kd.put("userType", "subscriber");
        kd.put("streamId", stream.getStreamId());
        kd.put("connectionData", stream.getConnection().getData());
        kd.put("connectionId", stream.getConnection().getConnectionId());
        kd.put("connectionCreationTime", stream.getConnection().getCreationTime());

        fireEvent("streamReceived", kd);
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.d(LCAT, "Stream Dropped");

        mSubscriber = new Subscriber.Builder(TiApplication.getAppCurrentActivity(), stream).build();
        KrollDict kd = new KrollDict();
        kd.put("type", "subscriber");
        kd.put("streamId", stream.getStreamId());
        fireEvent("streamDropped", kd);
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        fireEvent("sessionError", new KrollDict());
        Log.e(LCAT, "Session error: " + opentokError.getMessage());
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        fireEvent("streamCreated", new KrollDict());
        Log.d(LCAT, "Publisher onStreamCreated");
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        fireEvent("streamDestroyed", new KrollDict());
        Log.d(LCAT, "Publisher onStreamDestroyed");
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        fireEvent("error", new KrollDict());
        Log.e(LCAT, "Publisher error: " + opentokError.getMessage());
    }

    private class VideoView extends TiUIView {

        public VideoView(TiViewProxy proxy) {
            super(proxy);
        }

        public void addView(View view) {
            setNativeView(view);
        }
    }

}

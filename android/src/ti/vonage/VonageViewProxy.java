/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2017 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package ti.vonage;

import android.Manifest;
import android.app.Activity;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

@Kroll.proxy(creatableInModule = TiVonageModule.class, propertyAccessors = { "apiKey", "token", "sessionId" })
public class VonageViewProxy extends TiViewProxy implements Session.SessionListener, PublisherKit.PublisherListener
{
	private static final String LCAT = "Vonage";
	private static final boolean DBG = TiConfig.LOGD;
	private static String API_KEY = "";
	private static String SESSION_ID = "";
	private static String TOKEN = "";

	private static final int RC_SETTINGS_SCREEN_PERM = 123;
	private static final int RC_VIDEO_APP_PERM = 124;
	private Session mSession;
	private FrameLayout mPublisherViewContainer;
	private FrameLayout mSubscriberViewContainer;
	private Publisher mPublisher;
	private Subscriber mSubscriber;

	private class VonageView extends TiUIView
	{
		public VonageView(TiViewProxy proxy)
		{
			super(proxy);

			String packageName = proxy.getActivity().getPackageName();
			Resources resources = proxy.getActivity().getResources();
			View viewWrapper;

			int resId_viewHolder;
			int resIdPublish;
			int resIdSub;

			resId_viewHolder = resources.getIdentifier("layout_main", "layout", packageName);
			resIdPublish = resources.getIdentifier("publisher_container", "id", packageName);
			resIdSub = resources.getIdentifier("subscriber_container", "id", packageName);

			LayoutInflater inflater = LayoutInflater.from(proxy.getActivity());
			viewWrapper = inflater.inflate(resId_viewHolder, null);

			mPublisherViewContainer = (FrameLayout) viewWrapper.findViewById(resIdPublish);
			mSubscriberViewContainer = (FrameLayout) viewWrapper.findViewById(resIdSub);

			setNativeView(viewWrapper);
		}

		@Override
		public void processProperties(KrollDict d)
		{
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
		}

		@Override
		public void propertyChanged(String key, Object oldValue, Object newValue, KrollProxy proxy)
		{
			KrollDict d = new KrollDict();
			d.put(key, newValue);
			processProperties(d);
		}
	}

	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}

	@AfterPermissionGranted(RC_VIDEO_APP_PERM)
	private void requestPermissions()
	{
		Activity activity = TiApplication.getAppCurrentActivity();

		String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
		if (EasyPermissions.hasPermissions(activity, perms)) {
			// initialize view objects from your layout
			fireEvent("ready", new KrollDict());
		} else {
			EasyPermissions.requestPermissions(
				activity, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
		}
	}

	// Constructor
	public VonageViewProxy()
	{
		super();
	}

	@Override
	public TiUIView createView(Activity activity)
	{
		TiUIView view = new VonageView(this);
		view.getLayoutParams().autoFillsHeight = true;
		view.getLayoutParams().autoFillsWidth = true;
		return view;
	}

	// Handle creation options
	@Override
	public void handleCreationDict(KrollDict options)
	{
		Log.d(LCAT, "create");
		super.handleCreationDict(options);
	}

	// Methods
	@Kroll.method
	public void initialize()
	{
		requestPermissions();
	}

	@Kroll.method
	public void connect()
	{
		Activity activity = TiApplication.getAppCurrentActivity();
		mSession = new Session.Builder(activity, API_KEY, SESSION_ID).build();
		mSession.setSessionListener(this);
		mSession.connect(TOKEN);
	}

	// Methods
	@Kroll.method
	public void disconnect()
	{
		if (mSession != null) {
			mSession.disconnect();
		}
	}

	@Override
	public void onConnected(Session session)
	{
		Log.d(LCAT, "Session Connected");
		mPublisher = new Publisher.Builder(TiApplication.getAppCurrentActivity()).build();
		mPublisher.setPublisherListener(this);

		mPublisherViewContainer.addView(mPublisher.getView());

		if (mPublisher.getView() instanceof GLSurfaceView) {
			((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
		}

		mSession.publish(mPublisher);
	}

	@Override
	public void onDisconnected(Session session)
	{
		Log.d(LCAT, "Session Disconnected");
		fireEvent("disconnected", new KrollDict());
	}

	@Override
	public void onStreamReceived(Session session, Stream stream)
	{
		Log.d(LCAT, "Stream Received");
		fireEvent("streamReceived", new KrollDict());

		if (mSubscriber == null) {
			mSubscriber = new Subscriber.Builder(TiApplication.getAppCurrentActivity(), stream).build();
			mSession.subscribe(mSubscriber);
			mSubscriberViewContainer.addView(mSubscriber.getView());
		}
	}

	@Override
	public void onStreamDropped(Session session, Stream stream)
	{
		Log.d(LCAT, "Stream Dropped");
		fireEvent("streamDropped", new KrollDict());
		if (mSubscriber != null) {
			mSubscriber = null;
			mSubscriberViewContainer.removeAllViews();
		}
	}

	@Override
	public void onError(Session session, OpentokError opentokError)
	{
		fireEvent("sessionError", new KrollDict());
		Log.e(LCAT, "Session error: " + opentokError.getMessage());
	}
	@Override
	public void onStreamCreated(PublisherKit publisherKit, Stream stream)
	{
		fireEvent("streamCreated", new KrollDict());
		Log.d(LCAT, "Publisher onStreamCreated");
	}

	@Override
	public void onStreamDestroyed(PublisherKit publisherKit, Stream stream)
	{
		fireEvent("streamDestroyed", new KrollDict());
		Log.d(LCAT, "Publisher onStreamDestroyed");
	}

	@Override
	public void onError(PublisherKit publisherKit, OpentokError opentokError)
	{
		fireEvent("error", new KrollDict());
		Log.e(LCAT, "Publisher error: " + opentokError.getMessage());
	}
}

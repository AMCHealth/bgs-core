package com.amchealth.phonegap.plugin.backgroundservice;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class BackgroundServiceHelper {
  static String TAG = BackgroundServiceHelper.class.getSimpleName();

  static Map<Class<? extends Service>, ServiceConnection> connections = new HashMap<Class<? extends Service>, ServiceConnection>();
  static Map<Class<? extends Service>, Messenger> messengers = new HashMap<Class<? extends Service>, Messenger>();

  public static void sendMessage(Class<? extends Service> service,
      Message message) {
    Messenger messenger = messengers.get(service);
    if (messenger != null) {
      try {
        messenger.send(message);
      } catch (RemoteException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public static boolean bindService(Activity activity,
      Class<? extends Service> service, Message onConnect) {

    if (!connections.containsKey(service)) {
      ServiceConnection mConnection = connect(service, onConnect);
      connections.put(service, mConnection);
      Log.d(TAG, "Intent to bind");
      Intent intent = new Intent(activity.getApplicationContext(), service);
      boolean bound = activity.bindService(intent, mConnection,
          Context.BIND_AUTO_CREATE);
      Log.d(TAG, "Intent to bind returned " + bound);
      return bound;
    } else {
      Log.w(TAG, "Already bound to service");
    }
    return false;
  }

  private static ServiceConnection connect(
      final Class<? extends Service> service, final Message onConnect) {
    return new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName className, IBinder binder) {
        Log.d(TAG, service.getName() + "connected");
        Messenger m = new Messenger(binder);
        messengers.put(service, m);
        sendMessage(service, onConnect);
      }

      @Override
      public void onServiceDisconnected(ComponentName arg0) {
        connections.remove(service);
        messengers.remove(service);
      }
    };
  }

  public static void disconnect(Activity a, Class<?> s) {
    ServiceConnection conn = connections.get(s);
    a.unbindService(conn);
    conn.onServiceDisconnected(null);
  }
}

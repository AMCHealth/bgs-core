package com.amchealth.phonegap.plugin.backgroundservice;

import java.util.Map.Entry;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;

public class BackgroundServicePlugin extends CordovaPlugin {

  private static final String STOP_SERVICE = "stopService";
  private static final String START_SERVICE = "startService";
  private static final String DEREGISTER_FOR_BOOT_START = "deregisterForBootStart";
  private static final String REGISTER_FOR_BOOT_START = "registerForBootStart";
  private static final String GET_STATUS = "getStatus";
  /*
   * ***********************************************************************************************
   * Static values
   * ***************************************************************
   * ********************************
   */
  private static final String TAG = BackgroundServicePlugin.class
      .getSimpleName();

  /*
   * ***********************************************************************************************
   * Overriden Methods
   * ***********************************************************
   * ************************************
   */
  // Part fix for
  // https://github.com/Red-Folder/Cordova-Plugin-BackgroundService/issues/19
  // public boolean execute(String action, JSONArray data, CallbackContext
  // callback) {
  @Override
  public boolean execute(final String action, final JSONArray data,
      final CallbackContext callback) {

    Context context = this.cordova.getActivity()
        .getApplicationContext();
    String serviceName = data.optString(0);
    
    Log.d(TAG,action+" -> serviceName: "+serviceName);
    
    Class<?extends Service> serviceClass = ReflectionHelper.LoadServiceClass(serviceName);
    if(serviceClass == null){
      callback.error("Invalid service class");
      return true;
    }
    Intent serviceIntent = new Intent(context, serviceClass);
    int msg = 0;
    switch (action) {
      case REGISTER_FOR_BOOT_START:
        PropertyHelper.addBootService(context , serviceName);
        break;
      case DEREGISTER_FOR_BOOT_START:
        PropertyHelper.removeBootService(this.cordova.getActivity()
            .getApplicationContext(), serviceName);
        break;
      case GET_STATUS:
        msg = BackgroundServiceHelper.status(serviceClass)?1:0;
        break;
      case START_SERVICE:
        context.startService(serviceIntent);
        break;
      case STOP_SERVICE:
        BackgroundServiceHelper.disconnect(this.cordova.getActivity(),serviceClass);
        context.stopService(serviceIntent);
        break;
    }
    callback.success(msg);
    return true;
  }
  @Override
  public void onDestroy() {
    Log.e(TAG, "on Destroy");
    for (Entry<Class<? extends Service>, ServiceConnection> e :BackgroundServiceHelper.connections.entrySet()){
      this.cordova.getActivity().unbindService(e.getValue());
    }
    BackgroundServiceHelper.connections.clear();
    BackgroundServiceHelper.messengers.clear();
  }
}

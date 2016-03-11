package com.red_folder.phonegap.plugin.backgroundservice;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;

public class BackgroundServicePlugin extends CordovaPlugin {

  private static final String STOP_SERVICE = "stopService";
  private static final String START_SERVICE = "startService";
  private static final String DEREGISTER_FOR_BOOT_START = "deregisterForBootStart";
  private static final String REGISTER_FOR_BOOT_START = "registerForBootStart";
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
    Class<?> serviceClass = ReflectionHelper.LoadClass(serviceName);
    Intent serviceIntent = new Intent(context, serviceClass);         
    switch (action) {

    case REGISTER_FOR_BOOT_START:
      PropertyHelper.addBootService(context , serviceName);
      break;
    case DEREGISTER_FOR_BOOT_START:
      PropertyHelper.removeBootService(this.cordova.getActivity()
          .getApplicationContext(), serviceName);
      break;
    case START_SERVICE:
      context.startService(serviceIntent);
    case STOP_SERVICE:
      context.stopService(serviceIntent);
    }
    callback.success();
    return true;
  }
}

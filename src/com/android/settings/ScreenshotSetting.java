package com.android.settings;

import java.util.Timer;
import java.util.TimerTask;
//$_rbox_$_modify_$_by_cx,set storage location by mount state
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import android.os.Environment;
import android.os.storage.StorageManager;
//$_rbox_$_modify_$_end
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.SystemProperties;

public class ScreenshotSetting extends SettingsPreferenceFragment implements OnPreferenceChangeListener{
	 /** Called when the activity is first created. */
	private static final String KEY_SCREENSHOT_DELAY="screenshot_delay";
	private static final String KEY_SCREENSHOT_STORAGE_LOCATION="screenshot_storage";
	private static final String KEY_SCREENSHOT_SHOW="screenshot_show";
	private static final String KEY_SCREENSHOT_VERSION="screenshot_version";
	
	private ListPreference mDelay;
	private ListPreference mStorage;
	private CheckBoxPreference mShow;
	private Preference mVersion;
	
	private SharedPreferences mSharedPreference;
	private SharedPreferences.Editor mEdit;
	private Screenshot mScreenshot;
	
	private Context mContext;
    private Dialog dialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.screenshot);
        
        mContext=getActivity();
        mDelay=(ListPreference)findPreference(KEY_SCREENSHOT_DELAY);
        mStorage=(ListPreference)findPreference(KEY_SCREENSHOT_STORAGE_LOCATION);
        mShow=(CheckBoxPreference)findPreference(KEY_SCREENSHOT_SHOW);
        mVersion=(Preference)findPreference(KEY_SCREENSHOT_VERSION);
        
        //$_rbox_$_modify_$_by_cx,set storage location by mount state
        updateStorageLocation();
        //$_rbox_$_modify_$_end
        
        mShow.setOnPreferenceChangeListener(this);
        mDelay.setOnPreferenceChangeListener(this);
        mStorage.setOnPreferenceChangeListener(this);
        
        mSharedPreference=this.getPreferenceScreen().getSharedPreferences();
        mEdit=mSharedPreference.edit();
         
        String summary_delay =mDelay.getSharedPreferences().getString("screenshot_delay", "15");
    	mDelay.setSummary(summary_delay+getString(R.string.later));
        mDelay.setValue(summary_delay);
    	String summary_storage=mStorage.getSharedPreferences().getString("screenshot_storage", "flash");
    	//$_rbox_$_modify_$_by_cx,set storage location by mount state
    	if (mStorage.findIndexOfValue(summary_storage) < 0)
    		summary_storage = null;
    	//$_rbox_$_modify_$_end
        mStorage.setValue(summary_storage);
    	mStorage.setSummary(summary_storage);
        getPreferenceScreen().removePreference(mVersion);
        
        mScreenshot=(Screenshot)getActivity().getApplication();
    }
   
    //$_rbox_$_modify_$_by_cx,set storage location by mount state
    private void updateStorageLocation() {
    	Log.d("ScreenShot","updateStorageLocation");
    	HashMap<String, String> storagelist = new HashMap<String, String>();
    	if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
    		storagelist.put("Nand Flash", "flash");
    	} 
    	if (Environment.MEDIA_MOUNTED.equals(Environment.getSecondVolumeStorageState())) {
    		storagelist.put("SD Card", "sdcard");
    	}
    	if (Environment.MEDIA_MOUNTED.equals(Environment.getHostStorage_Extern_0_State())) {
    		storagelist.put("USB Storage0", "usb0");
    	} 
    	if (Environment.MEDIA_MOUNTED.equals(Environment.getHostStorage_Extern_1_State())) {
    		storagelist.put("USB Storage1", "usb1");
    	}
    	if (Environment.MEDIA_MOUNTED.equals(Environment.getHostStorage_Extern_2_State())) {
    		storagelist.put("USB Storage2", "usb2");
    	}
    	if (Environment.MEDIA_MOUNTED.equals(Environment.getHostStorage_Extern_3_State())) {
    		storagelist.put("USB Storage3", "usb3");
    	}
    	if (Environment.MEDIA_MOUNTED.equals(Environment.getHostStorage_Extern_4_State())) {
    		storagelist.put("USB Storage4", "usb4");
    	}
    	if (Environment.MEDIA_MOUNTED.equals(Environment.getHostStorage_Extern_5_State())) {
    		storagelist.put("USB Storage5", "usb5");
    	}

    	CharSequence[] storageEntries = new CharSequence[storagelist.size()];
    	CharSequence[] storageEntryValues = new CharSequence[storagelist.size()];
    	Iterator iterator = storagelist.entrySet().iterator();
    	for (int i = 0; iterator.hasNext(); i++) {
    		Map.Entry entry = (Map.Entry) iterator.next();
    		storageEntries[i] = (CharSequence) entry.getKey();
    		storageEntryValues[i] = (CharSequence) entry.getValue();
    	}
    	
    	mStorage.setEntries(storageEntries);
    	mStorage.setEntryValues(storageEntryValues);
    }
    
    private void setRealUsbPath(String path) {
    	if (path == null) return;
    	Runtime runtime = Runtime.getRuntime();
    	String prog = "mount | grep " + path + " | busybox awk '{print $2}'";
    	Log.d("ScreenShot","runtime exec = " + prog);
    	String[] cmds = {"sh","-c",prog};
    	try {
    		Process process = runtime.exec(cmds);
    		InputStreamReader reader = new InputStreamReader(process.getInputStream());
    		BufferedReader buffered = new BufferedReader(reader);
    		String line;
    		while ((line = buffered.readLine()) != null) {
    			Log.d("ScreenShot","read line = " + line);
    			if (line.startsWith(path)) {
    				path = line;
    				break;
    			}
    		}
    		Log.d("ScreenShot","path = " + path);
    	} catch (Exception e) {
    		Log.d("ScreenShot","runtime error = " + e);
    	}

		Settings.System.putString(getContentResolver(), Settings.System.SCREENSHOT_LOCATION,
				path);
    }
    //$_rbox_$_modify_$_end
        
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		if(preference==mDelay){
			int value=Integer.parseInt((String) newValue);
            mDelay.setSummary((String)newValue+getString(R.string.later));
            mScreenshot.startScreenshot(value);
		}else if(preference==mStorage){
			String value=(String)newValue;
			//mEdit.putString("storageLocation",value);
			//$_rbox_$_modify_$_by_cx,set storage location by mount state
			if(value.equals("flash")){
			String enableUms= SystemProperties.get("ro.factory.hasUMS","false");
			if("true".equals(enableUms))//if has UMS function,flash is primary storage
			{
					Settings.System.putString(getContentResolver(), Settings.System.SCREENSHOT_LOCATION,
                        Environment.getExternalStorageDirectory().getPath());
			}
			else
			{
					Settings.System.putString(getContentResolver(), Settings.System.SCREENSHOT_LOCATION, "/storage/emulated");
			}

				
			}else if(value.equals("sdcard")){
				Settings.System.putString(getContentResolver(), Settings.System.SCREENSHOT_LOCATION,
						Environment.getSecondVolumeStorageDirectory().getPath());
			}else if(value.equals("usb0")){
				String path = Environment.getHostStorage_Extern_0_Directory().getPath();
				setRealUsbPath(path);
			}else if(value.equals("usb1")){
				String path = Environment.getHostStorage_Extern_1_Directory().getPath();
				setRealUsbPath(path);
			}else if(value.equals("usb2")){
				String path = Environment.getHostStorage_Extern_2_Directory().getPath();
				setRealUsbPath(path);
			}else if(value.equals("usb3")){
				String path = Environment.getHostStorage_Extern_3_Directory().getPath();
				setRealUsbPath(path);
			}else if(value.equals("usb4")){
				String path = Environment.getHostStorage_Extern_4_Directory().getPath();
				setRealUsbPath(path);
			}else if(value.equals("usb5")){
				String path = Environment.getHostStorage_Extern_5_Directory().getPath();
				setRealUsbPath(path);
			}
			//$_rbox_$_modify_$_end
			mStorage.setSummary(value);
			
		}
		return true;
	}
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		// TODO Auto-generated method stub
		if(preference==mShow){
			Log.d("screenshot","onPreferenceTreeClick mShow");
			boolean show=mShow.isChecked();
			Settings.System.putInt(getContentResolver(), Settings.System.SCREENSHOT_BUTTON_SHOW, show?1:0);
		    //getWindow().getDecorView().setSystemUiVisibility(-1000);
			Intent intent=new Intent();
			intent.setAction("rk.android.screenshot.SHOW");
			intent.putExtra("show", show);
			mContext.sendBroadcast(intent);
		}
		return true;
	}

}

package com.cyc.app.myexams;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
public class SplashScreen extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        
        getActionBar().hide();
        
        Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				Intent i = new Intent(SplashScreen.this, MainActivity.class);
				startActivity(i);
				finish();
			}
		});
        thread.start();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	outState.putString("splash", "started");
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
//    	Intent i = new Intent(MainActivity.this, WindowApp.class);
//    	i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//		i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//		startActivity(i);
//    	finish();
    	
    }
}
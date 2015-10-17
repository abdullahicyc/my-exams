package com.cyc.app.myexams;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

public class About extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		ActionBar bar = getActionBar();
		bar.hide();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
	}
}

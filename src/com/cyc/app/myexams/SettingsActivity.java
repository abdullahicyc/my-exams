package com.cyc.app.myexams;


import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SettingsActivity extends Activity {

	DBAdapter myDB = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#005469")));

		setContentView(R.layout.activity_setting);
		
		ListView listview = (ListView)findViewById(R.id.list);
		String[] list = {"Delete Students", "Share with friends", "Rate Us",  "Send Feedback", "About"};
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, list);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(position == 0){
					Intent i = new Intent(SettingsActivity.this, ManageStudents.class);
					startActivity(i);
					
				} else if(position == 1){
					Intent i = new Intent("android.intent.action.SEND");
					i.setType("text/plain");
					i.putExtra("android.intent.extra.TEXT", 
							"Store your exam results in My Exams app. Download now from the Play Store" +
							":-\nhttp://play.google.com/store/apps/details?id="+ getPackageName());
					startActivity(Intent.createChooser(i, "Share Using"));
				}else if(position == 2){
					rateUs();
				} else if(position == 3){
					String to = "cyuusuf178@gmail.com";
					String m = "";
					
					Intent i = new Intent(Intent.ACTION_SEND);
					i.setType("message/rfc822"); //text/plain
					i.putExtra(Intent.EXTRA_EMAIL, new String[] {to});
					i.putExtra(Intent.EXTRA_SUBJECT, "[ My Exams ] - Feedback");
					i.putExtra(Intent.EXTRA_TEXT, m);
				
					final PackageManager pm = getPackageManager();
					final List<ResolveInfo> matches = pm.queryIntentActivities(i, 0);
					ResolveInfo best = null;
					for(final ResolveInfo info: matches){
						if(info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.toLowerCase().contains("gmail")){
							best = info;
							
							if(best != null){
								i.setClassName(best.activityInfo.packageName, best.activityInfo.name);
								startActivity(i);
							}
						}
					}
					if(best == null){
						displayDialog("No Gmail client was found in this device..", "Error");
					}
				}else if(position == 4){
					Intent i = new Intent(SettingsActivity.this, About.class);
					startActivity(i);
				}
			}
		});
	}

	private void rateUs() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("If you like the app, please rate it on the Play Store.");
		builder.setTitle("Rate this App");
		builder.setPositiveButton("5 Stars", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Uri uri = Uri.parse("market://details?id=" + getPackageName());
				Intent i = new Intent(Intent.ACTION_VIEW, uri);
				
				try {
					startActivity(i);
				} catch (ActivityNotFoundException e) {
					startActivity(new Intent(Intent.ACTION_VIEW,
			                Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
				}
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		//start dialog and show
		AlertDialog d = builder.create();
		d.show();
	}
	
	private void displayDialog(String message, String title){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(message);
    	builder.setTitle(title);
    	
    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
    	//start dialog and show
    	AlertDialog d = builder.create();
    	d.show();
	}
}

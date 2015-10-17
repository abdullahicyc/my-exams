package com.cyc.app.myexams;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddStudentActivity extends Activity {

	private DBAdapter myDB = new DBAdapter(this);
	
	EditText student_name, university_name, major;
	Spinner noOfYears;
	String name, uni, m;
	int years;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.add_student);

		
        major = (EditText) findViewById(R.id.txt_major);
        student_name = (EditText) findViewById(R.id.txt_student_name);
        university_name = (EditText) findViewById(R.id.txt_university_name);
       
        noOfYears = (Spinner) findViewById(R.id.spinner_years);
        noOfYears.setPrompt("Select your study years");
        
        final String[] y = getResources().getStringArray(R.array.school_year);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, y);
        noOfYears.setAdapter(adapter); 
        
        noOfYears.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				years = Integer.parseInt(y[arg2].substring(0,1));

			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	public void onPause() {
    	super.onPause();
    	myDB.close();
    }
	
	private void insertStudent(String n, String u, String m, int y){
		TimePicker tp = new TimePicker(getApplicationContext());
		
		int hour = tp.getCurrentHour();
		int minute = tp.getCurrentMinute();
		
		String date = new SimpleDateFormat("dd-MMM-yy", Locale.US).format(new Date());
		
		String time = setTime(hour, minute);
		
		myDB = new DBAdapter(this);
    	SQLiteDatabase db = myDB.getWritableDatabase();
    	long id = myDB.insertStudent(db, n, u, m, y, date, time);
    	db.close();
    	insertClasses(id, y*2);
    	
    	messageShow("Student Successlly saved.");
		
	}
	
	private String setTime(int hour, int minute) {
	
		hour = ((hour>= 0 && hour<24) ? hour : 0 );
		minute = ((minute>= 0 && hour<60) ? minute : 0 );
		
		String dayType = null;
		if(hour < 12){
			dayType = "AM";
		} else{
			dayType = "PM";
		}
		
		if(hour == 0 || hour == 12){
			hour = 12;
		}else {
			hour %= 12;
		}
		return String.format(Locale.US, "%d:%02d %s", hour, minute, dayType);
	}

	private void insertClasses(long id, int y){
		myDB = new DBAdapter(this);
    	SQLiteDatabase db = myDB.getWritableDatabase();
    	myDB.insertClasses(db, id, y);
    	db.close();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_student, menu);        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        int id = item.getItemId();
        if(id == R.id.action_save_student){
        	
        	name = student_name.getText().toString().trim();
			uni = university_name.getText().toString().trim();
			m = major.getText().toString().trim();
			
			if(!name.isEmpty() && !uni.isEmpty() && !m.isEmpty()){
				insertStudent(name, uni, m, years);
				finish();
			} else{
				messageShow("To add new student, you need to complete the info");
			}
        	return true;
        }
        else if(id == R.id.action_cancel_student){
        	finish();
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void messageShow(String message){
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
}


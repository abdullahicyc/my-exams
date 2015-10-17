package com.cyc.app.myexams;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.cyc.app.myexams.R;

public class AddSubjectActivity extends Activity {

	private DBAdapter myDB = new DBAdapter(this);
	
	EditText subject_name, subject_score1;
	String name, class_id, score1;
	TextView lbl_total;
	long c_id = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_subject);
		
		Intent i = getIntent();
		c_id = i.getLongExtra("class_id", 1);
		
		subject_name = (EditText) findViewById(R.id.txt_subject_name);
		subject_score1 = (EditText) findViewById(R.id.txt_score_number);
	}
	
	public void onPause() {
    	super.onPause();
    	myDB.close();
    }

	private void insertSubject(long c_id, String n, double s1, String c){
		myDB = new DBAdapter(this);
    	SQLiteDatabase db = myDB.getWritableDatabase();
    	myDB.insertSubject(db, c_id, n, s1, c);
    	db.close();
    	messageShow("Subject Successlly saved.");
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_subject, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_cancel_subject) {
			finish();
			return true;
		}
		else if (id == R.id.action_save_subject) {
			name = subject_name.getText().toString().trim();
			score1 = subject_score1.getText().toString();
			
			double t  = Double.parseDouble(score1);
			String score = String.format(Locale.US, "%.2f", t); //format it 2decimal places
			double total = Double.parseDouble(score);
			if(!name.isEmpty() && !score1.isEmpty()){
				//evaluate score number
				if(total <= 100.0){
					String c = String.valueOf(getGrade(total));
					
					insertSubject(c_id, name, total, c);
					finish();
				}
				else{
					messageShow("The [Exam Score] must be less than or equal 100.");
				}
			}
			else{
				messageShow("Make sure to complete the info.");
			}
        	return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private char getGrade(double average) {
		char result;
		if (average >= 90.0 && average <= 100.0) {
			result = 'A';
		} else if (average >= 80.0 && average <= 90.0) {
			result = 'B';
		} else if (average >= 70.0 && average <= 80.0) {
			result = 'C';
		} else if (average >= 60.0 && average <= 70.0) {
			result = 'D';
		} else {
			result = 'F';
		}
		return result;
	}
	
	private void messageShow(String message){
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
}

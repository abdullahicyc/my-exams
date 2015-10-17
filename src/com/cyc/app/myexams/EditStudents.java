package com.cyc.app.myexams;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.Toast;
import com.cyc.app.myexams.R;

public class EditStudents extends Activity {

	String[] y; boolean selectionMade = false;

	private DBAdapter myDB = new DBAdapter(this);

	EditText student_name, university_name, major;
	Spinner noOfYears;
	String name, uni, m;
	int years, pre_years;
	long _id;

	public boolean EDIT_ACTION = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.edit_student);
		
		//DATA TO BE EDITTED
		
		Intent i = getIntent();
		_id = i.getLongExtra("id", 0);
		String name = i.getStringExtra("name");
		String uni = i.getStringExtra("uni");
		String m = i.getStringExtra("major");
		years = i.getIntExtra("years", 2);
		
		//INITIALIZE CONTROLS
		major = (EditText) findViewById(R.id.txt_major);
		student_name = (EditText) findViewById(R.id.txt_student_name);
		university_name = (EditText) findViewById(R.id.txt_university_name);

		noOfYears = (Spinner) findViewById(R.id.spinner_years);

		y = getResources().getStringArray(
				R.array.school_year);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, y);
		noOfYears.setAdapter(adapter);
		
		//GET ALREADY STORED DATA TO THE CONTROLS
		
		major.setText(m);
		student_name.setText(name);
		university_name.setText(uni);

		switch (years) {
		case 2:
			noOfYears.setSelection(0);
			break;
		case 3:
			noOfYears.setSelection(1);
			break;
		case 4:
			noOfYears.setSelection(2);
			break;
		case 5:
			noOfYears.setSelection(3);
			break;
		case 6:
			noOfYears.setSelection(4);
			break;
		}
		
		//SELECTED YEARS
		
		noOfYears.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				pre_years = years;
				years = Integer.parseInt(y[arg2].substring(0, 1));
				
				selectionMade = true;
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

	private boolean updateStudent(long id, String n, String u, String mm, int y) {
		SQLiteDatabase db = myDB.getWritableDatabase();
		boolean r = myDB.updateStudent(db, id, n, u, mm, y);
		db.close();
		return r;
	}

	private boolean updateClass(long id, int ny, int oy) {
		SQLiteDatabase db = myDB.getWritableDatabase();
		boolean r = myDB.increaseClasses(db, id, ny * 2, oy * 2);
		db.close();
		return r;
	}

	private boolean decreaseClass(long id, int ny, int oy) {
		SQLiteDatabase db = myDB.getWritableDatabase();
		boolean r = myDB.decreaseClasses(db, id, ny * 2, oy * 2);
		db.close();
		return r;
	}


	private void doAction() {
		
		name = student_name.getText().toString();
		uni = university_name.getText().toString();
		m = major.getText().toString();

		if (updateStudent(_id, name, uni, m, years)) {
			//do we have to edit the student's classes
			if(selectionMade){
				
				//increase intended years
				if (years > pre_years) {
					updateClass(_id, years, pre_years);
				} else { 
					// delete some years
					decreaseClass(_id, years, pre_years);
				}
				selectionMade = false;
			}
			messageShow("Student Updated.");
			finish();
		} else {
			messageShow("Student cannot be updated now.");
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.add_student, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.action_save_student) {
			doAction();
			return true;
		} else if (id == R.id.action_cancel_student) {
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

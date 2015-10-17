package com.cyc.app.myexams;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ManageStudents extends Activity{

	Cursor cursor;
	DBAdapter myDB;
	ListView listview;
	TextView txtMessage;
	ArrayList<Long> selectedIDs  = new ArrayList<Long>();;
	ArrayList<String> list = new ArrayList<String>();
	ArrayAdapter<String> adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#005469")));

		setContentView(R.layout.manage_students);

		listview = (ListView) findViewById(R.id.students_list);
        txtMessage = (TextView) findViewById(R.id.txt_no_student);
        
        displayListview();
	}

	private void displayListview() {
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, getStudent());

        listview.setAdapter(adapter);  
        listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listview.setEmptyView(txtMessage);
	}
	
	String[] getStudent(){
		String[] columnNames = {"Name", "University", "Major", BaseColumns._ID};

		myDB = new DBAdapter(this);

		SQLiteDatabase db = myDB.getReadableDatabase();
        cursor = myDB.getStudents(db, columnNames);
        
        String[] names = new String[cursor.getCount()]; int count = 0;
        if(cursor.moveToFirst()){
        	do{
        		names[count] = cursor.getString(0);
        		count++;
        		
        	}while(cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return names;
	}
	
	private long[] getStudentIDs(){
		String[] columnNames = {"_id", "Name", "University", "Major", BaseColumns._ID};

		myDB = new DBAdapter(this);

		SQLiteDatabase db = myDB.getReadableDatabase();
        cursor = myDB.getStudents(db, columnNames);
        
        long[] names = new long[cursor.getCount()]; int count = 0;
        if(cursor.moveToFirst()){
        	do{
        		names[count] = cursor.getLong(0);
        		count++;
        		
        	}while(cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return names;
	}
	
	private boolean IsThereStudents(){
		myDB = new DBAdapter(this);
		SQLiteDatabase db = myDB.getReadableDatabase();
		return myDB.getStudents(db, new String[]{"_id", "Name"}).getCount() >= 1 ? true : false;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(IsThereStudents()){
			getMenuInflater().inflate(R.menu.manage_students, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
        if(id == R.id.action_delete_student){
        	displayDialogDelete();
        	return true;
        }
		return super.onOptionsItemSelected(item);
	}
	
	private void displayDialogDelete(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Once you delete this student, his/her data cannot be undone. ");
    	builder.setTitle("Delete Student");
    	
    	myDB = new DBAdapter(this);
		
		final SQLiteDatabase db = myDB.getReadableDatabase();
		
    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				SparseBooleanArray checkedItemPositions = listview.getCheckedItemPositions();
	    		int itemCount = listview.getCount();
	    		
				for(int i=itemCount-1; i >= 0; i--){
	    			if(checkedItemPositions.get(i)){	    				
	    				//adapter.remove(list.get(i));
	    				if(myDB.deleteStudent(db, getStudentIDs()[i])){
	    					checkedItemPositions.clear();
	    		    	    adapter.notifyDataSetChanged();
	    		    	    displayListview();
	    				}else{
	    					messageShow("Student could not be deleted.");
	    				}
	    			}
				}
			}
		});
    	//start dialog and show
    	AlertDialog d = builder.create();
    	d.show();
	}
	
	private void messageShow(String message){
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
}

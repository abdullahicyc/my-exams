package com.cyc.app.myexams;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

	public class MainActivity extends Activity {

	private DBAdapter myDB = null;
	private Cursor cursor = null;
	boolean go = false;
	EditText student_name, university_name, major;
	String name, uni, m;
	long _id;
	ListView listview;
	TextView txtMessage;
	int years, pre_years;
	Spinner noOfYears;
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#005469")));
		
        setContentView(R.layout.activity_list_student);
        
        
        myDB = new DBAdapter(this);
        getStudents();
        registerListClick();
    }
    
    @Override
    protected void onRestart() {
    	super.onRestart();
    	getStudents();
    }

    public void onPause() {
    	super.onPause();
    	myDB.close(); 
    }
    
    public void getStudents(){
    	
    	String[] columnNames = {"Name", "University", "Major", "registeredDate", "registeredTime", BaseColumns._ID};
    	int[] targetLayoutIDs = {R.id.item1, R.id.item2, R.id.item3, R.id.date, R.id.time};
        
    	listview = (ListView) findViewById(R.id.list);
        txtMessage = (TextView) findViewById(R.id.txt_no_student);
        
        SQLiteDatabase db = myDB.getReadableDatabase();
        cursor = myDB.getStudents(db, columnNames);
        
        CursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.item_layout,
                cursor, columnNames, targetLayoutIDs, 0);
        listview.setAdapter(adapter);   	
        listview.setEmptyView(txtMessage);
        registerForContextMenu(listview);
    }
    
    private void registerListClick(){
    	ListView lst = (ListView) findViewById(R.id.list);
    	lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				SQLiteDatabase db = myDB.getReadableDatabase();
				Cursor c = myDB.getStudent(db, id);
				if(c.moveToFirst()){
					long std_id = c.getLong(0); _id = std_id;
					name = c.getString(1);
					uni = c.getString(2);
					m = c.getString(3);
					years = c.getInt(4);
					
					//set context Menu for the Listview
					registerForContextMenu(listview);
					openContextMenu(listview);
					unregisterForContextMenu(listview);
				
				}
				db.close();
				c.close();
			}
		});
    	lst.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				SQLiteDatabase db = myDB.getReadableDatabase();
				Cursor c = myDB.getStudent(db, id);
				if(c.moveToFirst()){
					_id = c.getLong(0);
					name = c.getString(1);
					uni = c.getString(2);
					m = c.getString(3);
					years = c.getInt(4);
					
				}
				db.close();
				c.close();
				return false;
			}
		});
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int id = item.getItemId();
        if(id == R.id.action_add_student){
        	Intent i = new Intent(MainActivity.this, AddStudentActivity.class);
        	startActivity(i);
        	return true;
        }
        else if(id == R.id.action_owner_management){
        	Intent i = new Intent(MainActivity.this, SettingsActivity.class);
        	startActivity(i);
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
    	
    	myDB = new DBAdapter(this);
    	SQLiteDatabase db = myDB.getWritableDatabase();
    	
		if(v == listview){
			
			Cursor m = myDB.getStudentPassword(db, _id);
			if(m.getCount() >= 1){
				MenuInflater inflater = getMenuInflater();
				menu.setHeaderTitle(name);
				inflater.inflate(R.menu.list_student_context2, menu);
			}
			else{
				MenuInflater inflater = getMenuInflater();
				menu.setHeaderTitle(name);
				inflater.inflate(R.menu.list_student_context1, menu);
			}
			db.close();
			
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}
    
    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	
		switch(item.getItemId()){

		case R.id.action_change_password :
			myDB = new DBAdapter(this);
			changePassword();
			return true;
		case R.id.action_view :
			//check wheather he/she has password privacy
			openStudent();
			return true;
		case R.id.action_remove_password :
			myDB = new DBAdapter(this);
			removePassword();
			return true;
		case R.id.action_set_password :
			myDB = new DBAdapter(this);
			setPassword();
			return true;

		case R.id.action_edit_student_info :
			Intent i = new Intent(MainActivity.this, EditStudents.class);
			i.putExtra("id", _id);
			i.putExtra("name", name);
			i.putExtra("uni", uni);
			i.putExtra("major", m);
			i.putExtra("years", years);
			i.putExtra("EDIT_MODE", true);
			startActivity(i);
			return true;
			
		case R.id.action_delete_student :
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage("If you delete this student, you will also delete student's class information. \nAre you sure you want to delete??");
	    	builder.setTitle("Confirm delete!");
	    	
	    	builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					dialog.dismiss();
				}
			});
	    	builder.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					SQLiteDatabase db = myDB.getWritableDatabase();
					
					Cursor m = myDB.getStudentPassword(db, _id);
					if(m.getCount() >= 1){
						//they got password
						makeSurePasswordAndDelete();
					}
					else{
						//no password privacy
						deleteStudent(_id);
					}
					db.close();
				}
			});
	    	Dialog d = builder.create();
	    	d.show();
	    	
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private void openStudent() {
		SQLiteDatabase db = myDB.getReadableDatabase();
		Cursor m = myDB.getStudentPassword(db, _id);
		if(m.getCount() >= 1){
			//they got password
			openPassword(_id);
		}
		else{
			//no password privacy
			Intent x = new Intent(MainActivity.this, ListSubjectActivity.class);
			x.putExtra("list_name", name);
			x.putExtra("list_id", _id);
			startActivity(x);
		}
	}

    private void removePassword() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Removing a password from your data is not recomended because somebody else may read your info or delete it.\nMake sure you don't remove the password??");
    	builder.setTitle("Remove password!");
    	builder.setNegativeButton("Cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
    	builder.setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				deletePassword(_id);
			}
		});
    	Dialog d = builder.create();
    	d.show();
	}

	private void changePassword() {
		View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.change_password_layout, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(view).setTitle("Change password");
		
		final EditText current = (EditText) view.findViewById(R.id.txt_current_password);
		final EditText new_pass = (EditText) view.findViewById(R.id.txt_new_password);
		final EditText confirm_pass = (EditText) view.findViewById(R.id.txt_confirm_password);
		final EditText hint_pass = (EditText) view.findViewById(R.id.txt_hint);
		
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.dismiss();
			}
		});
		builder.setCancelable(false)
		//Submit data button
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
		    	String c = current.getText().toString();
		    	String pass = new_pass.getText().toString();
		    	String confirm = confirm_pass.getText().toString();
		    	String hint = hint_pass.getText().toString();
				String message = null;
				
				if(getPassword(_id).equals(c)){
					if(pass.length() == 4){
						if(confirm.equals(pass)){
							if(!hint.isEmpty()){
								if(updatePassword(_id, confirm, hint)){
									message = "Successfully changed password";
									displayDialog(message, "Success");
								}else{
									message = "Sorry could not change your password, try again.";
									displayDialog(message, "Failed");
								}
							} else{
								message = "Please enter hint email.";
								displayDialog(message, "Error");
							}
						}else{
							message = "Please enter the same password.";
							displayDialog(message, "Error");
						}
					} else{ 
						message = "Password is empty or is not 4 digit number.";
						displayDialog(message, "Error");
					}
				}
				else{
					displayDialog("Hey Your are not authorized to change someone else' information", "Error");
				}
			}
		});
		//start dialog and show
		AlertDialog d = builder.create();
		d.show();
	}

	protected void makeSurePasswordAndDelete() {
		View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.open_password_layout, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(view).setTitle("Open password");
		
		final EditText pass = (EditText) view.findViewById(R.id.txt_password);
		final TextView forget_passwprd = (TextView) view.findViewById(R.id.lbl_forget_password);
		
		forget_passwprd.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				displayHint();
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.dismiss();
			}
		});
		
		builder.setCancelable(false)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
		    	String p = pass.getText().toString();
				
				if(getPassword(_id).equals(p)){
					deleteStudent(_id);
				}
				else{
					messageShow("Incorrect password");
				}
			}
		});
		//start dialog and show
		AlertDialog d = builder.create();
		d.show();

	}

	private void deleteStudent(long student){
    	myDB = new DBAdapter(this);
    	SQLiteDatabase db = myDB.getReadableDatabase();
    	if(myDB.deleteStudent(db, student)){
			//delete classes of this student
			myDB.deleteClasses(db, student);
			messageShow("Deleted successfully");
			//refresh list
			getStudents();
		}
		else{
			messageShow("Couldn't delete, try again.");
		}
    	db.close();
    }
    
	private void setPassword() {
		View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.set_password_layout, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(view);
		
		
		final EditText pass = (EditText) view.findViewById(R.id.txt_password);
		final EditText hint = (EditText) view.findViewById(R.id.txt_hint);
		
		//initialize db adapter
		myDB = new DBAdapter(this);
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.dismiss();
			}
		});
		builder.setCancelable(false)
		//Submit data button
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
		    	SQLiteDatabase db = myDB.getWritableDatabase();
		    	String password = pass.getText().toString();
		    	String emailHint = hint.getText().toString();
				
		    	//is password set 
		    	if(password.length() == 4){
		    		if(!emailHint.isEmpty()){
						if(myDB.insertStudentPassword(db, _id, password, emailHint) > 0){
							displayDialog("Your password is set.", "Success");
						} else{
							displayDialog("Sorry Couldn't set password, try again.", "Failed");
						}
						db.close();
		    		} else{
			    		displayDialog("Please enter hint email.", "Error");
		    		}
		    	} else{
		    		displayDialog("Password is empty or is not 4 digit number.", "Error");
		    	}
			}
		});
		//start dialog and show
		AlertDialog d = builder.create();
		d.show();
	}

	private void openPassword(final long h) {
		View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.open_password_layout, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(view).setTitle("Open password...");
		
		
		final EditText pass = (EditText) view.findViewById(R.id.txt_password);
		final TextView forget_passwprd = (TextView) view.findViewById(R.id.lbl_forget_password);
		
		forget_passwprd.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				displayHint();
			}
		});
		
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.dismiss();
			}
		});
		builder.setCancelable(false)
		//Submit data button
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
		    	String p = pass.getText().toString();
				
				if(getPassword(h).equals(p)){
					Intent i = new Intent(MainActivity.this, ListSubjectActivity.class);
					i.putExtra("list_name", name);
					i.putExtra("list_id", _id);
					startActivity(i);
				}
				else{
					messageShow("Inorrect passwrod.");
				}
			}
		});
		//start dialog and show
		AlertDialog d = builder.create();
		d.show();
	}

	private void deletePassword(final long h){
		View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.open_password_layout, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(view).setTitle("Open password...").setMessage("Please enter your password to recognize you are the right person.");
		
		
		final EditText pass = (EditText) view.findViewById(R.id.txt_password);
		final TextView forget_passwprd = (TextView) view.findViewById(R.id.lbl_forget_password);
		
		forget_passwprd.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				displayHint();
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.dismiss();
			}
		});
		builder.setCancelable(false)
		//Submit data button
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
		    	String p = pass.getText().toString();
				SQLiteDatabase db  = myDB.getReadableDatabase();
				if(getPassword(h).equals(p)){
					boolean result = myDB.deleteStudentPassword(db, h);
					if(!result){
						messageShow("Could not remove password, please try again.");
					}
				}
				else{
					messageShow("Incorrect password.");
				}
			}
		});
		//start dialog and show
		AlertDialog d = builder.create();
		d.show();
	}
	
	private String getPassword(long id) {
		//initialize db adapter
		String pass = null;
		myDB = new DBAdapter(this);
		SQLiteDatabase db = myDB.getWritableDatabase();
		Cursor c = myDB.getStudentPassword(db, id);
		if(c.moveToFirst()){
			pass = c.getString(2);
		}
		db.close();
		return pass;
	}
	
	private boolean updatePassword(long id, String pass, String hint) {
		//initialize db adapter
		myDB = new DBAdapter(this);
		SQLiteDatabase db = myDB.getWritableDatabase();
		
		boolean result = myDB.updateStudentPassword(db, id, pass, hint);
		db.close();
		return result;
	}
	
	private void displayDialog(String message, String title){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(message);
    	builder.setTitle(title);
    	
    	builder.setPositiveButton("OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
    	//start dialog and show
    	AlertDialog d = builder.create();
    	d.show();
	}
	
	private void displayHint(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	final EditText input = new EditText(this);
    	builder.setInverseBackgroundForced(true);
    	
    	input.setHint("Your Email");
    	input.setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    	builder.setView(input);
    	
    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String hint = input.getText().toString();
				if((getHint(_id).equals(hint))){
					displayDialog("The password is: " + getPassword(_id) + "\n\nYou should change your password now.", "Recoverd password");
				} else{
					displayDialog("Incorrect Email", "Error");
				}
			}
		});
    	//start dialog and show
    	AlertDialog d = builder.create();
    	d.show();
	}
	
	String getHint(long id){
		//initialize db adapter
		String hint = null;
		myDB = new DBAdapter(this);
		SQLiteDatabase db = myDB.getWritableDatabase();
		Cursor c = myDB.getStudentPassword(db, id);
		if(c.moveToFirst()){
			hint = c.getString(3);
		}
		db.close();
		return hint;
	}
	
	private void messageShow(String message){
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
}
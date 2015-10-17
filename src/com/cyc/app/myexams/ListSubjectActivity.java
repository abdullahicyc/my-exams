package com.cyc.app.myexams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

	public class ListSubjectActivity extends Activity {

	DBAdapter myDB = new DBAdapter(this); 
	
	Cursor cursor = null;
	ListView myList = null;
	TextView txtMessage;
	long class_id, _id, std_id;
	String name, score1;
	String title;
	EditText txt_subject, subject_score1;
	long selected_class_id;
	Spinner semesters;
	String semTitle; String fileLocation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#005469")));
		
		setContentView(R.layout.activity_list_subject);
		
		title = getIntent().getStringExtra("list_name");
		setTitle(title);
		//class_id = getIntent().getLongExtra("list_id", 0);
		
		std_id = getIntent().getLongExtra("list_id", 0);
		
		semesters = (Spinner) findViewById(R.id.spinner_semesters);
		
		final String[] n = getSemesters(std_id);
		final String[] ids = getsSemestersIDS(std_id);
		
		selected_class_id = Long.parseLong(ids[0]);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, n);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		semesters.setAdapter(adapter);
		
		semesters.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				selected_class_id = Integer.parseInt(ids[position]);
				class_id = selected_class_id;
				semTitle = n[position];
				getSubjects();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});
		
		
		getSubjects();
		registerListClick();
		
		 try {
	        ViewConfiguration config = ViewConfiguration.get(this);
	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
	        if(menuKeyField != null) {
	            menuKeyField.setAccessible(true);
	            menuKeyField.setBoolean(config, false);
	        }
        } catch (Exception ex) {
	        // Ignore
	    }
	}

	private String[] getSemesters(long id) {
		
		myDB = new DBAdapter(this);
		SQLiteDatabase db = myDB.getWritableDatabase();
		Cursor c = myDB.getClassByStudentId(db, id);
		String[] semester  = new String[c.getCount()]; int i = 0;
		if(c.moveToFirst()){
			do{
				semester[i] = c.getString(2); 
				i++;
			}while(c.moveToNext());
		}
		db.close();
		
		return semester;
	}
	
	private String[] getsSemestersIDS(long id) {
		
		myDB = new DBAdapter(this);
		SQLiteDatabase db = myDB.getWritableDatabase();
		Cursor c = myDB.getClassByStudentId(db, id);
		String[] idss  = new String[c.getCount()]; int i = 0;
		if(c.moveToFirst()){
			do{
				idss[i] = String.valueOf(c.getLong(0)); 
				i++;
			}while(c.moveToNext());
		}
		db.close();
		
		return idss;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		myDB.close();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		getSubjects();
	}
	
	private void getSubjects(){
		String[] columnNames = {"Name", "Score1",  "Grade", BaseColumns._ID};
    	int[] targetLayoutIDs = {R.id.list_item1, R.id.list_item2, R.id.txt_grade};
        
    	myList = (ListView) findViewById(R.id.subject_list);
    	txtMessage = (TextView) findViewById(R.id.txt_no_subject);
        
        SQLiteDatabase db = myDB.getReadableDatabase();
        cursor = myDB.getSubjectByClassId(db, selected_class_id);
       
        CursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.list_item_layout,
                cursor, columnNames, targetLayoutIDs, 0);
     	myList.setAdapter(adapter); 
     	myList.setEmptyView(txtMessage);
	}
	
	private void registerListClick(){
    	ListView lst = (ListView) findViewById(R.id.subject_list);
    	lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				SQLiteDatabase db = myDB.getReadableDatabase();
				Cursor c = myDB.getSubject(db, id);
				if(c.moveToFirst()){
					_id = c.getLong(0); class_id = c.getInt(1);;
					name = c.getString(2);
					score1 = c.getString(3);
					
					//set context Menu for the Listview
					registerForContextMenu(myList);
					openContextMenu(myList);
					unregisterForContextMenu(myList);
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
				Cursor c = myDB.getSubject(db, id);
				if(c.moveToFirst()){
					_id = c.getLong(0);
					class_id = c.getLong(1);
					name = c.getString(2);
					score1 = c.getString(3);
				}
				db.close();
				c.close();
				return false;
			}
		});
    }
	
	private String[] getSubjects(long id) {
			
			myDB = new DBAdapter(this);
			SQLiteDatabase db = myDB.getWritableDatabase();
			Cursor c = myDB.getSubjectByClassId(db, id);
			String[] subject  = new String[c.getCount()]; int i = 0;
			if(c.moveToFirst()){
				do{
					subject[i] = c.getString(2); 
					i++;
				}while(c.moveToNext());
			}
			db.close();
			
			return subject;
		}
	
	private String[] getScores(long id) {
		
		myDB = new DBAdapter(this);
		SQLiteDatabase db = myDB.getWritableDatabase();
		Cursor c = myDB.getSubjectByClassId(db, id);
		String[] scores  = new String[c.getCount()]; int i = 0;
		if(c.moveToFirst()){
			do{
				scores[i] = c.getString(3); 
				i++;
			}while(c.moveToNext());
		}
		db.close();
		
		return scores;
	}

	  private void createPDF() {
		  
    	try {
	    	
			String path = Environment.getExternalStorageDirectory().getAbsolutePath()  + "/Exam PDF/" + String.valueOf(getTitle()).trim();
			
			File dir = new File(path);
			
	        if (!dir.exists()) {
	        	dir.mkdirs(); 
	        }
	        
	        //Create time stamp
	        Date date =  new Date();
	        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(date);
	       
	        //i need short form of the semester title as 'Sem' + number
	        String shortFormSemTitle = semTitle.substring(0, 3) + 
	        		semTitle.substring((semTitle.indexOf(" ") + 1)); 
			
	        String f = shortFormSemTitle + "_" + timeStamp + ".pdf";
	        File myFile = new File(dir, f);
	        //source location from sending email pdf attachment
	        fileLocation = myFile.toString();
	        
	        Document doc = new Document();
			
			PdfWriter.getInstance(doc, new FileOutputStream(myFile));
			
			doc.open();
			
			String dateOnPDF = new SimpleDateFormat("dd-MMM-yy", Locale.US).format(new Date());

			addTitlePage(doc, getTitle() + "\n"+ semTitle + " Exam results" + "               " + dateOnPDF);
			
			
			String[] subjects = getSubjects(class_id);
			String[] scores= getScores(class_id);
			
			PdfPTable table = new PdfPTable(new float[]{2, 2});
			
			table.setPaddingTop(5);
			table.setWidthPercentage(100);
			
			table.addCell(newHeaderRow("Subject Name"));
			table.addCell(newHeaderRow("Score"));
			table.completeRow();
			
			for (int i = 0; i < subjects.length; i++) {
				table.addCell(newElementRow((i+1) + ". "+ subjects[i]));
				table.addCell(newElementRow(scores[i]));
				table.completeRow();
			}
			
			int n = numberSubjects();
			double t = getTotalScore(class_id);
			double a = t / n;
			char g = getGrade(a);
			
			table.addCell(newElementRow(""));
			table.addCell(newElementRow("Total Score: \t\t" + String.format("%.2f", t) + "\nAverage: \t\t"
					+ String.format("%.2f", a) + "%\nYour semester Grade: \t\t" + g));
			
			doc.add(table);
			
			String currentYear = new SimpleDateFormat("yyyy", Locale.US).format(new Date());
			String footer = "Copyright © " + currentYear +" My Exams";
			
			//print dash-lines after description...
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < footer.length(); i++) {
				sb.append("_");
			}
			addDashLines(doc, sb.toString());
			
			addFooterPage(doc, footer);
			doc.close();
			
			displaypPDF("PDF successfully saved into your phone, under /Exam PDF directory.\n", "PDF Generated");
			
    	} catch (DocumentException e) {
    		Toast.makeText(getApplicationContext(), "Document Exception Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
			
		} catch (IOException e) {
    		Toast.makeText(getApplicationContext(), "I/O Exception Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	    
    private static PdfPCell newElementRow(String name) {
		PdfPCell c1 = new PdfPCell(new Phrase(name, new Font(Font.FontFamily.UNDEFINED)));
		c1.setHorizontalAlignment(Element.ALIGN_BASELINE);
		c1.setPadding(5);
		return c1;
	}
		
    private static PdfPCell newHeaderRow(String name) {
		PdfPCell c1 = new PdfPCell(new Phrase(name, new Font(Font.FontFamily.TIMES_ROMAN, 13, Font.BOLD, BaseColor.DARK_GRAY)));
		c1.setPadding(8);
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
		return c1;
	}

	private static void addFooterPage(Document doc, String footer) throws DocumentException {
		Paragraph pargraph = new Paragraph();
		addEmptyLine(pargraph, 0);
		
		pargraph.add(new Paragraph(footer, new Font(new Font(Font.FontFamily.TIMES_ROMAN, 9F, Font.BOLD, BaseColor.BLUE))));
		doc.add(pargraph);
	}
	
	private static void addDashLines(Document doc, String footer) throws DocumentException {
		Paragraph pargraph = new Paragraph();
		addEmptyLine(pargraph, 1);
		
		pargraph.add(new Paragraph(footer, new Font(new Font(Font.FontFamily.TIMES_ROMAN, 9F, Font.BOLD, BaseColor.BLUE))));
		doc.add(pargraph);
	}

	private static void addTitlePage(Document doc, String title) throws DocumentException{
		Paragraph pargraph = new Paragraph(Paragraph.ALIGN_CENTER);
		addEmptyLine(pargraph, 1);
		
		pargraph.add(new Paragraph(title, new Font(new Font(Font.FontFamily.HELVETICA, 20F, Font.BOLD))));
		pargraph.setAlignment(Paragraph.ALIGN_CENTER);
		addEmptyLine(pargraph, 2);
		
		doc.add(pargraph);
	}

	private static void addEmptyLine(Paragraph paragraph, int number) {
		for (int j = 0; j < number; j++) {
			paragraph.add(new Paragraph(" "));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list_subject, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		
		if (id == R.id.action_add_subject) {
			
			//open new window to add new subjects 
			Intent i = new Intent(ListSubjectActivity.this,
					AddSubjectActivity.class);
			i.putExtra("class_id", class_id);
			startActivity(i);
			
			return true;
		}else if(id == R.id.action_save_pdf) {
			
			if(!isSemesterEmpty()){
				//generate pdf
				createPDF();
			} else{
				displayDialog("First add subject(s) to the semester so we can make pdf document for you...", "Warning");
			}
		} else if (id == R.id.action_summery_class) {

			//summary of this semester
			int numberOfSubjects = numberSubjects();
			double totalScore = getTotalScore(class_id);
			double scoreMissed = (numberOfSubjects * 100.0) - totalScore;
			double average = totalScore / numberOfSubjects;
			double averageMissed = 100.0 - average;
			char grade = getGrade(average);
			
			String message = null;
			//make sure we have subjects to summarize
			if(!isSemesterEmpty()){
				
				message = "Number of Subjects: " + numberOfSubjects
					+ "\nTotal Score: " + String.format("%.2f", totalScore) + "\nAverage: "
					+ String.format("%.2f",average) + "%\nYour semester Grade: " + grade
					+"\n\t\t\nScores missed: " + String.format("%.2f", scoreMissed) +"\t\t\nAverage missed: " +
					String.format("%.2f",averageMissed) +"%";
				
				displayDialog(message,"Summary of this Semester");
			}else{
				message = "First add subject(s) to the semester so we can summarize your data.";
				displayDialog(message, "Warning");
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private int numberSubjects(){
		SQLiteDatabase db = myDB.getReadableDatabase();
		Cursor cc = myDB.getNoOfSubjectsInClass(db, class_id);
		db.close();
		return cc.getCount();
	}
	
	private boolean isSemesterEmpty(){
		boolean result = true;
		SQLiteDatabase db = myDB.getReadableDatabase();
		Cursor cc = myDB.getNoOfSubjectsInClass(db, class_id);
		long numberOfSubjects = cc.getCount();
		
		if(numberOfSubjects >=1){
			db.close();
			cc.close();
			result = false;
		}
		return result;
	}
	
	private void editSubject(){
		View view = (LayoutInflater.from(ListSubjectActivity.this)).inflate(R.layout.add_subject, null);
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setView(view).setTitle("Edit: "+ title);

    	txt_subject = (EditText) view.findViewById(R.id.txt_subject_name);
		subject_score1 = (EditText) view.findViewById(R.id.txt_score_number);
         
		//set texts
		txt_subject.setText(name);
		subject_score1.setText(score1);
		
        //Cancel Button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
        
        //initialize db adapter
        myDB = new DBAdapter(this);
        
        builder.setCancelable(true)
        //Submit data button
    	.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
		    	SQLiteDatabase db = myDB.getWritableDatabase();
		    	
		    	name = txt_subject.getText().toString().trim();
		    	score1 = subject_score1.getText().toString();
		    	
		    	double total = Double.parseDouble(score1);
		    	
				if(!name.isEmpty() && !score1.isEmpty()){
					//evaluate score number
					if(total <= 100.0){
						String c = String.valueOf(getGrade(total));
						if(myDB.updateSubject(db, _id, class_id, name, total, c))
						{
							messageShow("Successfully edited.");
							dialog.dismiss();
							getSubjects();
						}
					}
					else{
						messageShow("The [Exam Score] must be less than or equal 100.");
					}
				}
				else{
					messageShow("Make sure to complete the info.");
				}
				db.close();
			}
		});
        //start dialog and show
    	AlertDialog d = builder.create();
    	d.show();
    }

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if(v == myList){
			
			MenuInflater inflater = getMenuInflater();
			//set edit title
			menu.setHeaderTitle(title);
			inflater.inflate(R.menu.list_subject_context, menu);
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_edit_subject :
			//edit subject
			myDB = new DBAdapter(this);
			editSubject();
			return true;
		case R.id.action_delete_subject :
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage("Are you sure you want to delete [ "+name+" ]?");
	    	builder.setTitle("Confirm delete!");
	    	builder.setCancelable(true).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					SQLiteDatabase db = myDB.getWritableDatabase();
					if(myDB.deleteSubject(db, _id)){
						messageShow("["+name +"] deleted.");
						getSubjects();
					}
					else{
						messageShow("Could not delete this subject, please try again.");
					}
					db.close();
				}
			});
	    	builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					dialog.dismiss();
				}
			});
	    	Dialog d = builder.create();
	    	d.show();
	    	
			return true;
		}
		return super.onContextItemSelected(item);
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
	
	private double getTotalScore(long clss_id) {
    	SQLiteDatabase db = myDB.getReadableDatabase();
		Cursor cc = myDB.getSubjectByClassId(db, class_id);
		double total = 0;
		if(cc.moveToFirst()){
			do{
				total += cc.getDouble(3);
			}while(cc.moveToNext());
		}
		db.close();
		cc.close();
		
		return total;
	}
	
	private void displayDialog(String message, String title){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(message);
    	builder.setTitle(title).setCancelable(false);
    	
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
	
	private void displaypPDF(String message, String title){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(message);
    	builder.setTitle(title).setCancelable(false);
    	
    	builder.setPositiveButton("Open PDF", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				File file = new File(fileLocation);
				Intent target = new Intent(Intent.ACTION_VIEW);
				target.setDataAndType(Uri.fromFile(file), "application/pdf");
				target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

				Intent intent = Intent.createChooser(target, "PDF Document");
				
				try {
				    startActivity(intent);
				} catch (ActivityNotFoundException e) {
				    displayDialog("Please Install PDF Reader to open this file.", "PDF Reader not found");
				}  
			}
		});
    	builder.setNeutralButton("Send to Email", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				sendEmailPDF();
			}
		});
    	builder.setNegativeButton("Cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
    	//start dialog and show
    	AlertDialog d = builder.create();
    	d.show();
	}

	private void sendEmailPDF(){
		String to = ""; //empty email to send
		String m = ""; //empty text
		
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822"); //text/plain
		i.putExtra(Intent.EXTRA_EMAIL, new String[] {to});
		i.putExtra(Intent.EXTRA_SUBJECT, "PDF: " + semTitle + " Exam Results ");
		i.putExtra(Intent.EXTRA_TEXT, m);
		i.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+fileLocation));

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
			displayDialog("No Gmail client was found in this device.", "Error");
		}
	}
	
	private void messageShow(String message){
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
}

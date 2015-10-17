package com.cyc.app.myexams;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAdapter extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "myScoresDB";
	private static final int DB_VERSION = 1;
	
	
	private static final String STUDENT_TABLE_NAME = "students_table";
	private static final String CLASS_TABLE_NAME = "class_table";
	private static final String SUBJECT_TABLE_NAME = "subject_table";
	private static final String STUDENT_PRIVACY_TABLE_NAME = "student_privacy_table";
	
	DBAdapter(Context context) {
		super(context, DATABASE_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + STUDENT_TABLE_NAME + ";");
		db.execSQL("DROP TABLE IF EXISTS " + CLASS_TABLE_NAME + ";");
		db.execSQL("DROP TABLE IF EXISTS " + SUBJECT_TABLE_NAME + ";");
		createTables(db);
	}
	
	private void createTables(SQLiteDatabase db) {
		//Student table
		db.execSQL("CREATE TABLE " + STUDENT_TABLE_NAME + " (" + 
				  "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				  "Name TEXT, " +
				  "University TEXT, " +
				  "Major TEXT, " +
				  "Year INTEGER, " +
				  "registeredDate TEXT, " +
				  "registeredTime TEXT);");
		
		
		//Class table
		db.execSQL("CREATE TABLE " + CLASS_TABLE_NAME + " (" + 
				  "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				  "StudentId INTEGER, " +
				  "Name TEXT);");
		
		//Subject table
		db.execSQL("CREATE TABLE " + SUBJECT_TABLE_NAME + " (" + 
				  "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				  "ClassId INTEGER, " +
				  "Name TEXT, " +
				  "Score1 DOUBLE, " +
				  "Grade TEXT);");
		
		//student_privacy table
		db.execSQL("CREATE TABLE " + STUDENT_PRIVACY_TABLE_NAME + " (" + 
				  "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				  "student_id INTEGER, " +
				  "password TEXT, " +
				  "hint TEXT);");
	}
	
	public Cursor getStudents(SQLiteDatabase db, String[] columnNames){
		return db.query(STUDENT_TABLE_NAME, columnNames, null, null, null, null, null);
	}
	
	public Cursor getStudent(SQLiteDatabase db, long id){
		String[] columns = new String[] { "_id", "Name", "University", "Major", "Year" , "registeredDate", "registeredTime"};
		return db.query(STUDENT_TABLE_NAME, columns, "_id" + "=" + id, null, null, null, null);
	}
	
	public long insertStudent(SQLiteDatabase db,  String name, String uni, String major, int years, String date, String time){
		this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("Name", name);
		values.put("University", uni);
		values.put("Major", major);
		values.put("Year", years);
		values.put("registeredDate", date);
		values.put("registeredTime", time);
		return db.insert(STUDENT_TABLE_NAME, null, values);	
	}

	public boolean updateStudent(SQLiteDatabase db, long id, String name, String uni, String major, int years){
		ContentValues values = new ContentValues();
		values.put("Name", name);
		values.put("University", uni);
		values.put("Major", major);
		values.put("Year", years);
		
		return db.update(STUDENT_TABLE_NAME, values, "_id" + "=" + id, null) > 0;
	}
	
	public boolean deleteStudent(SQLiteDatabase db, long id) {
		return db.delete(STUDENT_TABLE_NAME, "_id" + "=" + id, null) > 0;
	}

	public long insertStudentPassword(SQLiteDatabase db,  long std_id, String pass, String hint){
		this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("student_id", std_id);
		values.put("password", pass);
		values.put("hint", hint);
		return db.insert(STUDENT_PRIVACY_TABLE_NAME, null, values);	
	}

	public boolean updateStudentPassword(SQLiteDatabase db,  long std_id, String pass, String hint){
		this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("password", pass);
		values.put("hint", hint);
		return db.update(STUDENT_PRIVACY_TABLE_NAME, values, "student_id" + "=" + std_id, null) > 0;
	}
	public boolean deleteStudentPassword(SQLiteDatabase db, long std_id) {
		return db.delete(STUDENT_PRIVACY_TABLE_NAME, "student_id" + "=" + std_id, null) > 0;
	}
	
	public Cursor getStudentPassword(SQLiteDatabase db, long id){
		String[] columns = new String[] { "_id", "student_id", "password", "hint" };
		return db.query(STUDENT_PRIVACY_TABLE_NAME, columns, "student_id" + "=" + id, null, null, null, null);
	}
	
	public void insertClasses(SQLiteDatabase db, long s_id, int year) {
		this.getWritableDatabase();
		
		for(int i = 1; i <= year; i++){
			
			ContentValues values = new ContentValues();
			values.put("StudentId", s_id);
			values.put("Name", "Semester " + i);
			db.insert(CLASS_TABLE_NAME, null, values);	
		}
	}
	
	public boolean increaseClasses(SQLiteDatabase db, long s_id, int new_year, int old_year) {
		this.getWritableDatabase();
		boolean flag = false;
		for(int i = old_year+1; i <= new_year; i++){				
			ContentValues values = new ContentValues();
			values.put("StudentId", s_id);
			values.put("Name", "Semester " + i);
			flag = db.insert(CLASS_TABLE_NAME, null, values) > 0;
		}
		return flag;	
	}

	public boolean decreaseClasses(SQLiteDatabase db, long s_id, int new_year, int old_year) {
		boolean flag = false;
		this.getWritableDatabase();
		
		//get this student's classIDs
		Cursor c = getClassByStudentId(db, s_id);
		long[] ids = new long[c.getCount()]; int count = 0;
		if(c.moveToFirst()){
			do{
				//Log.w("ids","id: " + c.getLong(0) + ", name: "+c.getString(2));
				ids[count] = c.getLong(0);
				count++;
			}while(c.moveToNext());
		}
		
		//how many rows we want delete, from last classes of the class
		int del = old_year - new_year;
		for(int i = 1; i <= del; i++){				
			//delete number of classes from the end of the class
			flag = db.delete(CLASS_TABLE_NAME, "_id" + "=" + ids[ids.length - i], null) > 0;
		}
		return flag;	
	}
	
	public Cursor getClasses(SQLiteDatabase db, String[] columnNames){
		return db.query(CLASS_TABLE_NAME, columnNames, null, null, null, null, null);
	}
	
	public Cursor getClassByStudentId(SQLiteDatabase db, long id){
		String[] columns = new String[] { "_id", "StudentId", "Name" };
		Cursor c = db.query(CLASS_TABLE_NAME, columns, "StudentId" + "=" + id, null, null, null, null);
		
		if(c.moveToFirst()){
			c.moveToNext();
		}
		return c;
	}
	
	public Cursor getClass(SQLiteDatabase db, long id){
		String[] columns = new String[] { "_id", "StudentId", "Name" };
		Cursor c = db.query(CLASS_TABLE_NAME, columns, "_id" + "=" + id, null, null, null, null);
		
		if(c.moveToFirst()){
			c.moveToNext();
		}
		return c;
	}
	
	public boolean deleteClasses(SQLiteDatabase db, long std_id) {
		return db.delete(CLASS_TABLE_NAME, "StudentId" + "=" + std_id, null) > 0;
	}
	
	public long insertSubject(SQLiteDatabase db,  long class_id, String n, double s1, String c){
		this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("ClassId", class_id);
		values.put("Name", n);
		values.put("Score1", s1);
		values.put("Grade", c);
		return db.insert(SUBJECT_TABLE_NAME, null, values);	
	}
	
	public Cursor getSubjects(SQLiteDatabase db){
		String[] columns = new String[] { "_id", "ClassId", "Name", "Score1", "Grade"};
		return db.query(SUBJECT_TABLE_NAME, columns, null, null, null, null, null);
	}
	
	public Cursor getSubjectByClassId(SQLiteDatabase db, long id){
		String[] columns = new String[] { "_id", "ClassId", "Name", "Score1", "Grade"};
		Cursor c = db.query(SUBJECT_TABLE_NAME, columns, "ClassId" + "=" + id, null, null, null, null);
		
		if(c.moveToFirst()){
			c.moveToNext();
		}
		return c;
	}
	
	public Cursor getSubject(SQLiteDatabase db, long id){
		String[] columns = new String[] { "_id", "ClassId", "Name", "Score1", "Grade"};
		Cursor c = db.query(SUBJECT_TABLE_NAME, columns, "_id" + "=" + id, null, null, null, null);
		
		if(c.moveToFirst()){
			c.moveToNext();
		}
		return c;
	}
	
	public Cursor getNoOfSubjectsInClass(SQLiteDatabase db, long class_id){
		String[] columns = new String[] { "_id", "ClassId", "Name", "Score1", "Grade"};
		String selection = " classid="+class_id;
		Cursor c = db.query(SUBJECT_TABLE_NAME, columns, selection, null, null, null, null);
		
		if(c.moveToFirst()){
			c.moveToNext();
		}
		return c;
	}

	public boolean updateSubject(SQLiteDatabase db, long id, long c_id, String name, double s1, String c){
		ContentValues values = new ContentValues();
		values.put("ClassId", c_id);
		values.put("Name", name);
		values.put("Score1", s1);
		values.put("Grade", c);
		
		
		return db.update(SUBJECT_TABLE_NAME, values, "_id" + "=" + id, null) > 0;
	}
	
	public boolean deleteSubject(SQLiteDatabase db, long _id) {
		return db.delete(SUBJECT_TABLE_NAME, "_id" + "=" + _id, null) > 0;
	}
	
}

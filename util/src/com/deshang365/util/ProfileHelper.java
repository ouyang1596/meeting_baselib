package com.deshang365.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.telephony.TelephonyManager;

public class ProfileHelper {
	private static final String TABLE_NAME = "profiles";

	private static String getIMEI(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = telephonyManager.getDeviceId();
		if (imei == null || imei.length() == 0) {
			imei = "123s@#23)(J";
		}
		return imei;
	}

	public static void insertOrUpdate(Context context, String key, String value) {
		ProfileDBHelper dbHelper = new ProfileDBHelper(context);
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		String keyCrypt = key;
		String valueCrypt = value;
		try {
			Encrypt.encrypt(key, getIMEI(context));
			Encrypt.encrypt(value, getIMEI(context));
		} catch (Exception e) {
		}

		ContentValues contentValues = new ContentValues();
		contentValues.put("primary_key", keyCrypt);
		contentValues.put("value", valueCrypt);

		Cursor c = database.query(TABLE_NAME, null, "primary_key=?", new String[] { key }, null, null, null);
		if (c.getCount() <= 0) {
			database.insert(TABLE_NAME, null, contentValues);
		} else {
			database.update(TABLE_NAME, contentValues, "primary_key=?", new String[] { key });
		}
		c.close();
		database.close();
	}

	public static String read(Context context, String key, String defaultValue) {
		ProfileDBHelper dbHelper = new ProfileDBHelper(context);
		SQLiteDatabase database = dbHelper.getReadableDatabase();

		String keyCrypt = key;
		try {
			Encrypt.encrypt(key, getIMEI(context));
		} catch (Exception e) {
		}

		String value = defaultValue;
		Cursor c = database.query(TABLE_NAME, null, "primary_key=?", new String[] { keyCrypt }, null, null, null);
		if (c.getCount() > 0) {
			c.moveToFirst();
			value = c.getString(c.getColumnIndex("value"));
		}
		c.close();
		database.close();

		String valueDecrypt = value;
		try {
			valueDecrypt = Encrypt.decrypt(value, getIMEI(context));
		} catch (Exception e) {
		}

		return valueDecrypt;
	}

	public static void delete(Context context, String key) {
		ProfileDBHelper dbHelper = new ProfileDBHelper(context);
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(TABLE_NAME, "primary_key=?", new String[] { key });
		database.close();
	}

	public static void deleteAll(Context context) {
		ProfileDBHelper dbHelper = new ProfileDBHelper(context);
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(TABLE_NAME, null, null);
		database.close();
	}

	public static boolean hasRecord(Context context, String key) {
		ProfileDBHelper dbHelper = new ProfileDBHelper(context);
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor c = database.query(TABLE_NAME, null, "primary_key=?", new String[] { key }, null, null, null);
		int queryCount = c.getCount();
		c.close();
		database.close();
		return queryCount > 0;
	}

	private static class ProfileDBHelper extends SQLiteOpenHelper {
		private static final String DATABASE_NAME = "com.deshang365.meeting.Profile";
		private static final int DATABASE_VERSION = 1;

		public ProfileDBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME + " (primary_key text primary key,value text);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}
}

package com.moreno.fartbomb;

import java.util.*;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

public class SQLActivity extends Activity {
    private static final String LOG_TAG = SQLActivity.class.getSimpleName();
    private TextView txtResults;
    private Button btnSubmit;
    private EditText edtQuery;
    private SQLiteDatabase db;

    private static final String NEWLINE = "\n";

    public void clickHandler(View view) {
        if (view.equals(btnSubmit)) {
            String query = edtQuery.getText().toString();

            if (query.toUpperCase(Locale.US).startsWith("SELECT")) {
                updateDisplay(runSelect(query));
                return;
            } else if (query.toUpperCase(Locale.US).startsWith("DELETE")) {
                updateDisplay(runDelete(query));
                return;
            } else if (query.toUpperCase(Locale.US).startsWith("UPDATE")) {
                updateDisplay(runUpdate(query));
                return;
            } else if (query.toUpperCase(Locale.US).startsWith(".TABLES")) {
                updateDisplay(getTables());
                return;
            } else if (query.toUpperCase(Locale.US).startsWith(".SCHEMA")) {
                updateDisplay(getSchema(query));
                return;
            } else if (isTable(query.toUpperCase(Locale.US))) {
                updateDisplay(selectAllFromTable(query.trim()));
                return;
            }

            Toast.makeText(this, "Command Not Supported", Toast.LENGTH_SHORT).show();
        }
    }

    public void closeDB() {
        db.close();
    }

    private String getSchema(String query) {
        String tableName = query.substring(7).trim();
        Log.d(LOG_TAG, "SELECT sql FROM sqlite_master WHERE type='table' AND name='" + tableName + "'");
        if (isTable(tableName)) {
            return runSelect("SELECT sql FROM sqlite_master WHERE type='table' AND name='" + tableName + "'");
        }

        return tableName + " is not a valid table.";
    }

    private String getTables() {
        String query = "SELECT name FROM sqlite_master WHERE type='table'";
        return runSelect(query);
    }

    private boolean isTable(String query) {
        String tableQuery = "SELECT name FROM sqlite_master WHERE type='table'";
        ArrayList<String> tables = new ArrayList<String>();
        Cursor c;
        try {
            c = db.rawQuery(tableQuery, null);
        } catch (Exception e) {
            return false;
        }
        if (c != null && c.moveToFirst()) {
            while (!c.isAfterLast()) {
                tables.add(c.getString(0).toUpperCase(Locale.US).trim());
                c.moveToNext();
            }
        }
        c.close();
        for (String table : tables) {
            if (query.toUpperCase(Locale.US).trim().equals(table)) {
                return true;
            }
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openDB();
        setContentView(R.layout.sql_viewer);
        txtResults = (TextView) findViewById(R.id.txtResults);
        btnSubmit = (Button) findViewById(R.id.btnGo);
        edtQuery = (EditText) findViewById(R.id.edtQuery);
    }

    @Override
    public void onDestroy() {
        closeDB();
        super.onDestroy();
    }

    public void openDB() {
        db = openOrCreateDatabase("fartbomb.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
    }

    private String runDelete(String query) {
        try {
            db.execSQL(query);
        } catch (Exception e) {
            return e.getMessage();
        }
        return "Delete Complete.";
    }

    private String runSelect(String query) {
        ArrayList<String> rows = new ArrayList<String>();
        Cursor c;
        try {
            c = db.rawQuery(query, null);
        } catch (Exception e) {
            return e.getMessage();
        }

        if (c != null && c.moveToFirst()) {
            do {
                StringBuilder sbRow = new StringBuilder();
                for (int i = 0; i < c.getColumnCount(); i++) {
                    if (i > 0) {
                        sbRow.append(", ");
                    }
                    switch (c.getType(i)) {
                    case Cursor.FIELD_TYPE_STRING:
                        sbRow.append(c.getString(i));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        sbRow.append(c.getFloat(i));
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        sbRow.append(c.getInt(i));
                        break;
                    default:
                        sbRow.append("unknownFieldType");
                        break;
                    }
                }

                rows.add(sbRow.toString());
            } while (c.moveToNext());
        }

        c.close();

        StringBuilder sbResponse = new StringBuilder(rows.size());
        for (String row : rows) {
            sbResponse.append(NEWLINE);
            sbResponse.append(row);
        }

        return sbResponse.toString();
    }

    private String runUpdate(String query) {
        try {
            db.execSQL(query);
        } catch (Exception e) {
            return e.getMessage();
        }

        return "UPDATE COMPLETE";
    }

    private String selectAllFromTable(String query) {
        query = "SELECT * FROM " + query;
        return runSelect(query);
    }

    private void updateDisplay(String response) {
        txtResults.setText(response);
    }
}
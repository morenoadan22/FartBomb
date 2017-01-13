package com.moreno.fartbomb;

import java.util.ArrayList;

import org.apache.http.Header;
import org.json.*;

import android.app.*;
import android.content.*;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.os.*;
import android.provider.ContactsContract;
import android.text.InputFilter;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;

import com.loopj.android.http.*;
import com.moreno.fartbomb.data.FartBombDb;
import com.moreno.fartbomb.network.*;
import com.moreno.fartbomb.util.*;
import com.moreno.fartbomb.widget.*;

public class FindFriendsActivity extends Activity implements OnClickListener {
    private static final String LOG_TAG = FindFriendsActivity.class.getSimpleName();
    private final FartBombDb mFbHelper = FartBombDb.getInstance();
    protected static final String PARAM_FRIEND = "friend";
    protected static final String PARAM_USER_ID = "userId";
    protected static final String PARAM_CONTACTS = "contacts";

    private EditText etxSearchFriends;
    private ListView listPotentialFriends;
    private SearchResultAdapter mAdapter;
    private ArrayList<JSONObject> mFriendList;
    private ArrayList<JSONObject> mContactList;
    private boolean contactsSynced;
    private TextView txtMessage;

    public ArrayList<String> getContacts() {
        Cursor rawContacts = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        ArrayList<String> contacts = new ArrayList<String>();
        if (rawContacts.moveToFirst()) {

            do {
                String id = rawContacts.getString(rawContacts.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                if (Integer.parseInt(rawContacts.getString(rawContacts.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phoneCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
                    while (phoneCursor.moveToNext()) {
                        String contactNumber = phoneCursor.getString(phoneCursor
                                .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contacts.add(contactNumber);
                        break;
                    }
                    phoneCursor.close();
                }

            } while (rawContacts.moveToNext());

        }

        rawContacts.close();
        return contacts;
    }

    protected void hideSoftKeyboard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void importContacts() {
        AlertDialog.Builder dialog = FartBombTools.buildDialog(this, "Import Contacts",
                "Would you like to allow Fartbomb to sync your contacts?");
        dialog.setPositiveButton("Yes", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        dialog.setNegativeButton("No", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (contactsSynced && mContactList != null) {
                    mAdapter.updateEntries(mContactList);
                    if (mAdapter.getCount() == 0) {
                        txtMessage.setVisibility(View.VISIBLE);
                        txtMessage.setText("S");
                    }
                } else {
                    searchPeople(true);
                }
            }
        });
        dialog.create();
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btnHome:
            startActivity(new Intent(this, PlaybackActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            break;
        case R.id.btnContacts:
            importContacts();
            break;
        }

    }

    private boolean onClickSearch(final String string) {
        listPotentialFriends.setVisibility(View.GONE);
        txtMessage.setVisibility(View.VISIBLE);
        txtMessage.setText("Searching Friends...");
        etxSearchFriends.setClickable(false);
        int userId = mFbHelper.getActiveUser(getContentResolver()).getUserId();
        RequestParams params = new RequestParams();
        params.add(PARAM_FRIEND, etxSearchFriends.getText().toString().trim());
        params.add(PARAM_USER_ID, String.valueOf(userId));
        FartBombRestClient.post(Servlet.FIND_FRIENDS.toString(), params, new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
                etxSearchFriends.setClickable(true);

            }

            @Override
            public void onSuccess(JSONObject jObject) {
                txtMessage.setVisibility(View.GONE);
                etxSearchFriends.setClickable(true);
                listPotentialFriends.setVisibility(View.VISIBLE);
                try {
                    if (jObject.getBoolean("status")) {
                        mFriendList = parseJSONArray(jObject.getJSONArray("results"));
                        mAdapter.updateEntries(mFriendList);
                        if (mAdapter.getCount() == 0) {
                            txtMessage.setVisibility(View.VISIBLE);
                            txtMessage.setText("Sorry, no users matched your search \"" + string + "\". Please try again.");
                        }
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error searching for friends " + etxSearchFriends.getText().toString(), e);
                }
            }
        });
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        ImageView btnContacts = (ImageView) findViewById(R.id.btnContacts);
        btnContacts.setOnClickListener(this);
        ImageView btnHome = (ImageView) findViewById(R.id.btnHome);
        btnHome.setOnClickListener(this);

        txtMessage = (TextView) findViewById(R.id.txtMessage);
        etxSearchFriends = (EditText) findViewById(R.id.etxSearchFriends);
        etxSearchFriends.setFilters(new InputFilter[] { new EmailFilter() });

        etxSearchFriends.setOnTouchListener(new RightDrawableOnTouchListener(etxSearchFriends) {

            @Override
            public boolean onDrawableTouch(MotionEvent event) {
                boolean ret = onClickSearch(etxSearchFriends.getText().toString());
                hideSoftKeyboard();
                return ret;
            }

        });

        etxSearchFriends.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    onClickSearch(v.getText().toString());
                    hideSoftKeyboard();
                    return true;
                }
                return false;
            }
        });

        listPotentialFriends = (ListView) findViewById(R.id.listPotentialFriends);
        mAdapter = new SearchResultAdapter(FindFriendsActivity.this);
        listPotentialFriends.setAdapter(mAdapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        // searchPeople(true);
    }

    private ArrayList<JSONObject> parseJSONArray(JSONArray jArray) throws JSONException {
        ArrayList<JSONObject> friends = new ArrayList<JSONObject>();
        int userId = mFbHelper.getActiveUser(getContentResolver()).getUserId();
        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jFriend = jArray.getJSONObject(i);
            if (jFriend.getInt("friendId") != userId) {
                friends.add(jFriend);
            }
        }
        return friends;
    }

    protected void searchPeople(final boolean updateView) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                StringBuilder sb = new StringBuilder();
                ArrayList<String> phoneNumbers = getContacts();

                for (String phone : phoneNumbers) {
                    sb.append(phone + "|");
                }
                return sb.toString();
            }

            @Override
            protected void onPostExecute(String sb) {
                RequestParams params = new RequestParams();
                params.put(PARAM_USER_ID, "" + mFbHelper.getActiveUser(getContentResolver()).getUserId());
                params.put(PARAM_FRIEND, "");
                params.put(PARAM_CONTACTS, sb);
                FartBombRestClient.post(Servlet.FIND_FRIENDS.toString(), params, new JsonHttpResponseHandler() {

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        super.onFailure(statusCode, headers, responseBody, error);
                    }

                    @Override
                    public void onSuccess(JSONObject jObject) {
                        txtMessage.setVisibility(View.GONE);
                        listPotentialFriends.setVisibility(View.VISIBLE);
                        try {
                            if (updateView) {
                                mFriendList = parseJSONArray(jObject.getJSONArray("results"));
                                mAdapter.updateEntries(mFriendList);
                                if (mAdapter.getCount() == 0) {
                                    txtMessage.setVisibility(View.VISIBLE);
                                    txtMessage.setText("S");
                                }
                                mContactList = mFriendList;
                            } else {
                                mContactList = parseJSONArray(jObject.getJSONArray("results"));
                            }
                            contactsSynced = true;
                        } catch (JSONException je) {
                            Log.e(LOG_TAG, "exception syncing contacts", je);
                        }
                    }
                });
            }

            @Override
            protected void onPreExecute() {
                listPotentialFriends.setVisibility(View.GONE);
                if (updateView) {
                    txtMessage.setVisibility(View.VISIBLE);
                    txtMessage.setText("Syncing contacts with our servers...");
                }
            }

        }.execute((Void[]) null);

    }
}

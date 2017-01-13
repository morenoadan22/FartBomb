package com.moreno.fartbomb;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

public class ContactActivity extends Activity implements OnClickListener {
    EditText etxSubject, etxBody;

    /**
     * Remove the input from the text fields.
     */
    private void clearTextFields() {
        etxSubject.setText("");
        etxBody.setText("");
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btnHome:
            Intent intent = new Intent(this, PlaybackActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            break;
        case R.id.btnBack:
            finish();
            break;
        case R.id.btnSend:
            if (validateEntry()) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[] { "support@fartbomb.net" });
                i.putExtra(Intent.EXTRA_SUBJECT, etxSubject.getText().toString().trim());
                i.putExtra(Intent.EXTRA_TEXT, etxBody.getText().toString().trim());
                try {
                    startActivity(Intent.createChooser(i, "Send email..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
                clearTextFields();
            }
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        ImageView btnHome = (ImageView) findViewById(R.id.btnHome);
        ImageView btnBack = (ImageView) findViewById(R.id.btnBack);
        TextView btnSend = (TextView) findViewById(R.id.btnSend);
        etxSubject = (EditText) findViewById(R.id.etxSubject);
        etxBody = (EditText) findViewById(R.id.etxText);

        btnHome.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnSend.setOnClickListener(this);

    }

    /**
     * Checks the user entry for valid entries.
     * 
     * @return {@code true} if the textfields contain valid strings
     */
    private boolean validateEntry() {
        boolean isValid = true;
        etxSubject.setError(null);
        etxBody.setError(null);

        if (TextUtils.isEmpty(etxSubject.getText().toString().trim()) && TextUtils.isEmpty(etxBody.getText().toString().trim())) {
            etxSubject.setError("Both of these fields cannot be empty");
            etxBody.setError("Both of these fields cannot be empty");
            isValid = false;
        }
        return isValid;
    }

}

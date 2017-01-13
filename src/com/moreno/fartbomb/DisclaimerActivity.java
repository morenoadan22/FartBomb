package com.moreno.fartbomb;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.moreno.fartbomb.util.FartBombTools;

public class DisclaimerActivity extends Activity implements OnClickListener {

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
        default:
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
        setContentView(R.layout.activity_disclaimer);

        TextView txtTerms = (TextView) findViewById(R.id.txtTerms);
        ImageView btnHome = (ImageView) findViewById(R.id.btnHome);
        ImageView btnBack = (ImageView) findViewById(R.id.btnBack);
        String text = FartBombTools.readTextResource(this, R.raw.terms);
        txtTerms.setText(text);
        txtTerms.setMovementMethod(new ScrollingMovementMethod());
        btnBack.setOnClickListener(this);
        btnHome.setOnClickListener(this);
    }
}

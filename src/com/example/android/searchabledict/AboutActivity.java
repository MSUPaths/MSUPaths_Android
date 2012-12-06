package com.example.android.searchabledict;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

// This activity is for showing information about the app
public class AboutActivity extends Activity
{
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
 
        Button btnBack = (Button) findViewById(R.id.back_button);

 
        // Binding Click event to Button
        btnBack.setOnClickListener(new View.OnClickListener() 
        {
 
            @Override
			public void onClick(View arg0) 
            {
                //Closing About Activity
                finish();
            }
        });
 
    }

}

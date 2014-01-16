package com.ouroboros.ctharacter;

import com.ouroboros.ctharacter.CtharacterActivity.AboutDialogFragment;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

//Provides a list of sample occupations and their required skills
public class OccupationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_occupation);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.occupation, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_about:
			AboutDialogFragment about = new AboutDialogFragment();
			about.show(getFragmentManager(), "About");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}

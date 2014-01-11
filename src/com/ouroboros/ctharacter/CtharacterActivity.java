package com.ouroboros.ctharacter;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.EditText;
import com.ouroboros.ctharacter.R;

import java.text.NumberFormat;
import java.util.Random;

public class CtharacterActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stat);

		setupPreferences();

		findStatViews();
		findDiceViews();
		assignTags();

		setupStatOnTextChangedListeners();
		setupStatDragListeners();

		initializeMoneySpinner();
		initializeMoneyArray();
	}

	@Override
	protected void onResume() {
		super.onResume();

		setupDiceLongClickListeners();

		if (sharedPref.getBoolean(KEY_HINTS_ON, false)) {
			hintsOn();
		} else {
			hintsOff();
		}

	}

	private void findStatViews() {
		str = (EditText) findViewById(R.id.str_edit);
		dex = (EditText) findViewById(R.id.dex_edit);
		ntl = (EditText) findViewById(R.id.int_edit);
		idea = (EditText) findViewById(R.id.idea_edit);
		con = (EditText) findViewById(R.id.con_edit);
		app = (EditText) findViewById(R.id.app_edit);
		pow = (EditText) findViewById(R.id.pow_edit);
		luck = (EditText) findViewById(R.id.luck_edit);
		siz = (EditText) findViewById(R.id.siz_edit);
		san = (EditText) findViewById(R.id.san_edit);
		edu = (EditText) findViewById(R.id.edu_edit);
		know = (EditText) findViewById(R.id.know_edit);
		myth = (EditText) findViewById(R.id.myth_edit);
		dam = (EditText) findViewById(R.id.dam_edit);
		mp = (EditText) findViewById(R.id.mp_edit);
		hp = (EditText) findViewById(R.id.hp_edit);
		money = (EditText) findViewById(R.id.money_edit);

		// 3D6 Stat EditTexts
		stat3D6 = new EditText[] { str, con, dex, app, pow };
		// 2D6+6 Stat EditTexts
		stat2D6 = new EditText[] { ntl, siz };

		years = (Spinner) findViewById(R.id.spinner_year);
	}

	private void findDiceViews() {
		threeD1 = (EditText) findViewById(R.id.three_d_stat1);
		threeD2 = (EditText) findViewById(R.id.three_d_stat2);
		threeD3 = (EditText) findViewById(R.id.three_d_stat3);
		threeD4 = (EditText) findViewById(R.id.three_d_stat4);
		threeD5 = (EditText) findViewById(R.id.three_d_stat5);

		twoD1 = (EditText) findViewById(R.id.two_d_stat1);
		twoD2 = (EditText) findViewById(R.id.two_d_stat2);

		// 3D6 Dice EditTexts
		dice3D6 = new EditText[] { threeD1, threeD2, threeD3, threeD4, threeD5 };
		// 2D6+6 Dice EditTexts
		dice2D6 = new EditText[] { twoD1, twoD2 };

		occPoints = (TextView) findViewById(R.id.occ_points);
		hobPoints = (TextView) findViewById(R.id.hob_points);
	}

	// Assigns the tags that the drag system depends upon
	private void assignTags() {
		for (int i = 0; i < NUMBER_3D6_ELEMENTS; i++) {
			stat3D6[i].setTag(THREE_D6_TAG);
			dice3D6[i].setTag(THREE_D6_TAG);
		}

		for (int j = 0; j < NUMBER_2D6_ELEMENTS; j++) {
			stat2D6[j].setTag(TWO_D6_TAG);
			dice2D6[j].setTag(TWO_D6_TAG);
		}
	}
	
	//Sets up specific listeners for each EditText
	private void setupStatOnTextChangedListeners() {
		setPowListener();
		setIntListener();
		setStrListener();
		setSizListener();
		setConListener();
		setEduListener();
	}
	
	private void setPowListener() {
		pow.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				mp.setText(s.toString());
				if (s.toString().equals("")) {
					luck.setText("");
					san.setText("");
				} else {
					luck.setText(CHAR_MULTIPLIER * Integer.parseInt(s.toString()) + "");
					san.setText(CHAR_MULTIPLIER * Integer.parseInt(s.toString()) + "");
				}
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
		});
	}

	private void setIntListener() {
		ntl.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().equals("")) {
					idea.setText("");
					hobPoints.setText(R.string.initial_stat);
				} else {
					idea.setText(CHAR_MULTIPLIER * Integer.parseInt(s.toString()) + "");
					hobPoints.setText(HOB_MULTIPLIER * Integer.parseInt(s.toString()) + "");
				}
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}
		});
	}

	private void setSizListener() {
		siz.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().equals("")) {
					dam.setText("");
					hp.setText("");
				} else {
					if (str.getText().toString().equals("")) {
						dam.setText("");
					} else {
						dam.setText(lookupDamBonus(Integer.parseInt(siz
								.getText().toString())
								+ Integer.parseInt(str.getText().toString())));
					}

					if (con.getText().toString().equals("")) {
						hp.setText("");
					} else {
						double conVal = Double.parseDouble(s.toString());
						double sizVal = Double.parseDouble(con.getText()
								.toString());
						double average = (conVal + sizVal) / 2.0;
						hp.setText(Math.round(average) + "");
					}
				}

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}
		});
	}

	private void setConListener() {
		con.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().equals("")) {
					hp.setText("");
				} else {
					if (siz.getText().toString().equals("")) {
						hp.setText("");
					} else {
						double conVal = Double.parseDouble(s.toString());
						double sizVal = Double.parseDouble(siz.getText()
								.toString());
						double average = (conVal + sizVal) / 2.0;
						hp.setText(Math.round(average) + "");
					}
				}
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}
		});
	}

	private void setEduListener() {
		edu.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().equals("")) {
					know.setText("");
					occPoints.setText(R.string.initial_stat);
				} else {
					know.setText(CHAR_MULTIPLIER * Integer.parseInt(s.toString()) + "");
					occPoints.setText(OCC_MULTIPLIER * Integer.parseInt(s.toString()) + "");
				}
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}
		});
	}

	private void setStrListener() {
		str.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().equals("")) {
					dam.setText("");
				} else {
					if (siz.getText().toString().equals("")) {
						dam.setText("");
					} else {
						dam.setText(lookupDamBonus(Integer.parseInt(siz
								.getText().toString())
								+ Integer.parseInt(str.getText().toString())));
					}
				}
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}
		});
	}

	private void setupStatDragListeners() {
		TagDragListener threeEditListener = new TagDragListener(THREE_D6_TAG);
		TagDragListener twoEditListener = new TagDragListener(TWO_D6_TAG);
		TagDragListener eduEditListener = new TagDragListener(EDU_TAG);

		for (EditText e : stat3D6) {
			e.setOnDragListener(threeEditListener);
		}

		for (EditText f : stat2D6) {
			f.setOnDragListener(twoEditListener);
		}

		edu.setOnDragListener(eduEditListener);
	}

	private void setupDiceLongClickListeners() {
		View.OnLongClickListener diceListener = new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				ClipData dragData = ClipData.newPlainText(
						(CharSequence) v.getTag(), ((TextView) v).getText());
				DragShadowBuilder myShadow = new DragShadowBuilder(v);
				v.startDrag(dragData, myShadow, v, 0);
				return true;
			}
		};

		for (int i = 0; i < NUMBER_3D6_ELEMENTS; i++) {
			stat3D6[i].setOnLongClickListener(diceListener);

			// User shouldn't be able to drag values from the dice
			// when the auto-assign setting is on
			if (!sharedPref.getBoolean(KEY_AUTO_ASSIGN, false)) {
				dice3D6[i].setOnLongClickListener(diceListener);
			} else {
				dice3D6[i].setOnLongClickListener(null);
			}
		}

		for (int j = 0; j < NUMBER_2D6_ELEMENTS; j++) {
			stat2D6[j].setOnLongClickListener(diceListener);

			if (!sharedPref.getBoolean(KEY_AUTO_ASSIGN, false)) {
				dice2D6[j].setOnLongClickListener(diceListener);
			} else {
				dice2D6[j].setOnLongClickListener(null);
			}
		}

	}

	private void initializeMoneySpinner() {
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.year_choices, R.layout.spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		years.setAdapter(adapter);

		// Default roll value for money
		moneyRoll = 0;
		// Default year: 1920s
		selectedYear = 1;
		years.setSelection(selectedYear);
		years.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				selectedYear = pos;
				if (moneyRoll != 0) {
					money.setText(getMoneyAmount(moneyRoll, (int) id));
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	// 2D Array of money values for quick lookup
	private void initializeMoneyArray() {
		moneyArray = new int[][] { { 500, 1000, 1500, 2000, 2500, 3000, 4000, 5000, 5000, 10000 },
				{ 1500, 2500, 3500, 3500, 4500, 5500, 6500, 7500, 10000, 20000 },
				{ 15000, 25000, 35000, 45000, 55000, 75000, 100000, 200000, 300000, 500000 } };

	}

	private void setupPreferences() {
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPref.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				if (key.equals(KEY_HINTS_ON)) {
					if (sharedPref.getBoolean(
							KEY_HINTS_ON, false)) {
						hintsOn();
					} else {
						hintsOff();
					}
				}

				if (key.equals(KEY_AUTO_ASSIGN)) {
					setupDiceLongClickListeners();
				}
			}
		});
	}

	private String lookupDamBonus(int score) {
		if (score <= 12) {
			return "-1D6";
		} else if (score <= 16) {
			return "-1D4";
		} else if (score <= 24) {
			return "+0";
		} else if (score <= 32) {
			return "+1D4";
		} else {
			return "+1D6";
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.creator, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.action_about:
			AboutDialogFragment about = new AboutDialogFragment();
			about.show(getFragmentManager(), "About");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void roll(View v) {
		clear(null);

		int eduVal = rollDieValue(6) + rollDieValue(6) + rollDieValue(6) + 3;

		for (int i = 0; i < NUMBER_3D6_ELEMENTS; i++) {
			dice3D6[i].setText("" + (rollDieValue(6) + rollDieValue(6) + rollDieValue(6)));

			// Automatically fills in values if auto assign is enabled
			if (sharedPref.getBoolean(KEY_AUTO_ASSIGN, false)) {
				stat3D6[i].setText(dice3D6[i].getText());
			}
		}

		for (int j = 0; j < NUMBER_2D6_ELEMENTS; j++) {
			dice2D6[j].setText("" + (rollDieValue(6) + rollDieValue(6) + 6));

			if (sharedPref.getBoolean(KEY_AUTO_ASSIGN, false)) {
				stat2D6[j].setText(dice2D6[j].getText());
			}
		}

		moneyRoll = rollDieValue(10);

		edu.setText("" + eduVal);
		myth.setText("" + 99);

		money.setText(getMoneyAmount(moneyRoll, selectedYear));
	}

	public void clear(View v) {

		for (int i = 0; i < NUMBER_3D6_ELEMENTS; i++) {
			stat3D6[i].setText("");
			dice3D6[i].setText("");
		}

		for (int j = 0; j < NUMBER_2D6_ELEMENTS; j++) {
			stat2D6[j].setText("");
			dice2D6[j].setText("");
		}

		idea.setText("");
		luck.setText("");
		san.setText("");
		edu.setText("");
		know.setText("");
		myth.setText("");
		dam.setText("");
		mp.setText("");
		hp.setText("");

		occPoints.setText(R.string.initial_stat);
		hobPoints.setText(R.string.initial_stat);

		years.setSelection(1);
		moneyRoll = 0;
		money.setText("");
	}

	private void hintsOn() {
		for (int i = 0; i < NUMBER_3D6_ELEMENTS; i++) {
			stat3D6[i].setHint(R.string.thd_stat);
		}

		for (int j = 0; j < NUMBER_2D6_ELEMENTS; j++) {
			stat2D6[j].setHint(R.string.twd_stat);
		}

	}

	private void hintsOff() {
		for (int i = 0; i < NUMBER_3D6_ELEMENTS; i++) {
			stat3D6[i].setHint("");
		}

		for (int j = 0; j < NUMBER_2D6_ELEMENTS; j++) {
			stat2D6[j].setHint("");
		}
	}

	private String getMoneyAmount(int roll, int selected) {
		int moneyAmount = moneyArray[selected][roll - 1];
		return NumberFormat.getInstance().format(moneyAmount);
	}

	private int rollDieValue(int n) {
		Random random = new Random();
		return random.nextInt(n) + 1;
	}

	protected class TagDragListener implements View.OnDragListener {
		protected String tag;

		public TagDragListener(String tag) {
			this.tag = tag;
		}

		@Override
		public boolean onDrag(View v, DragEvent event) {
			EditText originView = (EditText) event.getLocalState();
			EditText destinationView = (EditText) v;

			// If the DragEvent is a drop
			if (event.getAction() == DragEvent.ACTION_DROP) {
				String originVal = originView.getText().toString();
				// And the tags match up
				if (originView.getTag().equals(tag)) {
					String destinationText = destinationView.getText()
							.toString();
					if (destinationText.equals("")) {
						originView.setText(destinationText);
						return false;
					} else {

						destinationView
								.setText(originView.getText().toString());
						originView.setText(destinationText);
						return true;
					}

				} else {
					originView.setText(originVal);
					return true;
				}

				// Enables hints when the drag starts
			} else if (event.getAction() == DragEvent.ACTION_DRAG_STARTED
					&& !sharedPref.getBoolean("toggle_hints_preference", false)) {
				hintsOn();
				// Disables them when it ends
			} else if (event.getAction() == DragEvent.ACTION_DRAG_ENDED
					&& !sharedPref.getBoolean("toggle_hints_preference", false)) {
				hintsOff();
			}

			return true;
		}
	}

	public static class AboutDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.action_about)
					.setMessage("Ctharacter - Version 2.2\nbrown.tim.lee@gmail.com\nOuroboros Software")
					.setPositiveButton(R.string.review, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Uri marketUri = Uri.parse("market://details?id=com.ouroboros.ctharacter");
							Intent i = new Intent(Intent.ACTION_VIEW, marketUri);
							startActivity(i);
						}
					})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {

						}
					});

			return builder.create();
		}
	}
	
	private static final String THREE_D6_TAG = "3D6";
	private static final String TWO_D6_TAG = "2D6+6";
	private static final String EDU_TAG = "Edu";
	private static final String KEY_HINTS_ON = "toggle_hints_preference";
	private static final String KEY_AUTO_ASSIGN = "toggle_auto";
	private static final int NUMBER_3D6_ELEMENTS = 5;
	private static final int NUMBER_2D6_ELEMENTS = 2;
	private final int OCC_MULTIPLIER = 20;
	private final int HOB_MULTIPLIER = 10;
	private final int CHAR_MULTIPLIER = 5;

	private EditText threeD1, threeD2, threeD3, threeD4, threeD5, twoD1, twoD2;
	private TextView hobPoints, occPoints;
	private EditText str, dex, ntl, idea, con, app, pow, luck, siz, san, edu,
			know, myth, dam, hp, mp, money;
	private EditText[] stat3D6, stat2D6, dice3D6, dice2D6;
	private Spinner years;
	private int[][] moneyArray;
	private int moneyRoll, selectedYear;
	private SharedPreferences sharedPref;
}

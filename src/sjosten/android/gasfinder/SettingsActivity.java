package sjosten.android.gasfinder;

import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class SettingsActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction().replace(
			android.R.id.content, new SettingsFragment()
		).commit();
	}

	private class SettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences);
			Bundle extras = getIntent().getExtras();
			int counter = extras.getInt("COUNTER");
			PreferenceScreen mPreferenceScreen = getPreferenceScreen();
			
			for(int i = 0; i < counter; i++) {
				String name = extras.getString("STATION_NAME_" + i);
				CheckBoxPreference box = new CheckBoxPreference(SettingsActivity.this);
				box.setDefaultValue(true);
				switch(name.toLowerCase(Locale.getDefault())) {
				case "preem":
					box.setKey(getString(R.string.preem_key));
					box.setSummary(R.string.preem_summary);
					box.setTitle(R.string.preem_title);
					break;
				case "st1":
					box.setKey(getString(R.string.st1_key));
					box.setSummary(R.string.st1_summary);
					box.setTitle(R.string.st1_title);
					break;
				case "shell":
					box.setKey(getString(R.string.shell_key));
					box.setSummary(R.string.shell_summary);
					box.setTitle(R.string.shell_title);
					break;
				case "gasmackar":
					box.setKey(getString(R.string.natural_gas_key));
					box.setSummary(R.string.natural_gas_summary);
					box.setTitle(R.string.natural_gas_title);
					break;
				}
				
				mPreferenceScreen.addPreference(box);
			}
		}
	}
}

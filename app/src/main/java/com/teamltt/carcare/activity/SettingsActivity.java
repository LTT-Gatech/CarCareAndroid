/*
 * Copyright 2017, Team LTT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teamltt.carcare.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;

import static com.github.pires.obd.enums.AvailableCommandNames.*;
import com.teamltt.carcare.R;

import java.util.List;


public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_SYNC_CONN = "pref_syncConnectionType";
    public static final String[] staticPreferenceTitles = new String[]{
            SPEED.getValue(),
            ENGINE_RUNTIME.getValue(),
            AIR_INTAKE_TEMP.getValue(),
            BAROMETRIC_PRESSURE.getValue(),
            FUEL_CONSUMPTION_RATE.getValue()
            };

    public static final String[] dynamicPreferenceTitles = new String[]{
            SPEED.getValue(),
            ENGINE_RPM.getValue(),
            FUEL_CONSUMPTION_RATE.getValue()
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            PreferenceScreen preferenceScreen = this.getPreferenceScreen();

            // Static Category
            PreferenceCategory preferenceCategory = new PreferenceCategory(preferenceScreen.getContext());
            preferenceCategory.setTitle(getString(R.string.static_data));
            preferenceScreen.addPreference(preferenceCategory);

            for (String title : staticPreferenceTitles) {
                Preference preference = new CheckBoxPreference(preferenceScreen.getContext());
                preference.setTitle(title);
                preference.setKey("static " + title);
                preference.setDefaultValue(false);
                preferenceCategory.addPreference(preference);
            }

            // Dynamic Category
            preferenceCategory = new PreferenceCategory(preferenceScreen.getContext());
            preferenceCategory.setTitle(getString(R.string.dynamic_data));
            preferenceScreen.addPreference(preferenceCategory);

            for (String title : dynamicPreferenceTitles) {
                Preference preference = new CheckBoxPreference(preferenceScreen.getContext());
                preference.setTitle(title);
                preference.setKey("dynamic " + title);
                preferenceCategory.addPreference(preference);
                preference.setDefaultValue(false);
            }

        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(KEY_PREF_SYNC_CONN)) {
            Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            connectionPref.setSummary(sharedPreferences.getString(key, ""));
        }
    }
}

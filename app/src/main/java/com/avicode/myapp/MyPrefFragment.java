package com.avicode.myapp;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by spike on 8/7/2016.
 */

    public  class MyPrefFragment extends PreferenceFragment {

        public MyPrefFragment(){}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
    }



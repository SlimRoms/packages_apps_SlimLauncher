package com.slim.slimlauncher.settings;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by gmillz on 9/7/14.
 */
public class SettingsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();
    }
}

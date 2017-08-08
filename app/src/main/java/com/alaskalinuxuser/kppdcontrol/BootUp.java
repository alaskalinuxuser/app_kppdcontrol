package com.alaskalinuxuser.kppdcontrol;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/*  Copyright 2017 by AlaskaLinuxUser (https://thealaskalinuxuser.wordpress.com)
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

public class BootUp extends Activity {

    SharedPreferences autoPrefs;
    Boolean boolStart;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.auto_start);

        // Set up my shared preferences
        autoPrefs = this.getSharedPreferences("com.alaskalinuxuser.kppdcontrol", Context.MODE_PRIVATE);
        // Set the boolean for starting to false.
        boolStart = false;

        try {
            // Let's import our preference
            boolStart = autoPrefs.getBoolean("prBoot", false);
            Log.i("kppd", String.valueOf(boolStart)); // Testing only //
        } catch (Exception a) {
            // What to log if it fails.
            Log.i("kppd", "No pref." + a);
        } // end of try catch for importing preferences.

        if (boolStart) {
            // Do something.
            // starting kppd.
            String[] chmodpost = {"su", "-c", "kppd "+
                    Environment.getExternalStorageDirectory().getPath()+
                    "/documents/kppd.conf"};
            try {
                Runtime.getRuntime().exec(chmodpost);
                Log.i("kppd", "Auto start kppd.");
                //Toast.makeText(getBaseContext(), "Auto started kppd", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            } // End of try catch for starting kppd.
        } else {
            // Do nothing.
            Log.i("kppd", "Do not auto start kppd.");
        } // End of bool start.

        // And we are done with the auto start sequence.
        finish();
    }
}
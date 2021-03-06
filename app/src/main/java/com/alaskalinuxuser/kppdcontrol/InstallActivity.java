package com.alaskalinuxuser.kppdcontrol;

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

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InstallActivity extends AppCompatActivity {

    Context context;
    Boolean rebootOK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = getApplicationContext();
        rebootOK = true;

        // Get super user permission.
        String[] getSu = {"su", "-c", "ls"};
        try {
            Runtime.getRuntime().exec(getSu);
        } catch (IOException e) {
            e.printStackTrace();
        } // End of getting super user permission.

    } // End of onCreate.

    public void installKPPD (View view) {

        // Name of the sdcard.
        String directoryString = Environment.getExternalStorageDirectory().getPath();

        // Open your asset file as the input stream
        InputStream myInput;

        try { // Surround with try and catch in case of failure.

            myInput = this.getResources().openRawResource(R.raw.kppd);
                    //Resources.openRawResource(R.raw.kppd);
                    //getApplicationContext().getResources().openRawResource();
                    //getAssets().open("kppd");

            // Path to the just created empty file
            String outFileName = directoryString + "/kppd";

            // Open the empty file as the output stream
            OutputStream myOutput = new FileOutputStream(outFileName);

            // transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            // Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();
            rebootOK = true;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error: Please check permissions....",
                    Toast.LENGTH_LONG).show();
            rebootOK = false;
        } // End of try and catch to copy kppd.

        /*
         * Now that they are on the sdcard, let's move them to the appropriate location.
         */

        // Copy the files.
        String[] remountFS = {"su", "-c", "mount -o rw,remount /system"};
        try {
            Runtime.getRuntime().exec(remountFS);
            Toast.makeText(getApplicationContext(), "remounting",
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        } // End of try catch for Copy files.

        // Copy the files.
        String[] installkppd = {"su", "-c", "cp "+directoryString+"/kppd /system/bin/"};
        try {
            Runtime.getRuntime().exec(installkppd);
            Toast.makeText(getApplicationContext(), "installing",
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        } // End of try catch for Copy files.

        // Copy the files.
        String[] chmodkppd = {"su", "-c", "chmod 755 /system/bin/kppd"};
        try {
            Runtime.getRuntime().exec(chmodkppd);
            Toast.makeText(getApplicationContext(), "modding",
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        } // End of try catch for Copy files.

        // Copy the files.
        String[] remOldkppd = {"su", "-c", "rm "+directoryString+"/kppd"};
        try {
            Runtime.getRuntime().exec(remOldkppd);
            Toast.makeText(getApplicationContext(), "removing old",
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        } // End of try catch for Copy files.

                finish();

        /*
         * We don't worry about remounting system as RO, since the last command is to reboot.
         * Now it is installed. However, the daemon has not started yet.
         */

    } // End of installkppd method.

    @Override // To prevent going back to main screen.
    public void onBackPressed() {
        moveTaskToBack(true); // So, instead of going back, go to home screen.
    } // END back pressed.

} // End of install activity.

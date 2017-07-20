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
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        whereTo();

    } // End of onCreate.

   public void whereTo () {

       // Get super user permision.
       String[] getSu = {"su", "-c", "ls"};
       try {
           Runtime.getRuntime().exec(getSu);
       } catch (IOException e) {
           e.printStackTrace();
       } // End of getting super user permission.

       Intent myIntent;

       File file = new File("/system/bin/kppd");

       if(file.exists()) {

           // Make our intent to go to the install activity/class.
           myIntent = new Intent(getApplicationContext(), ControllingActivity.class);

       } else {

           // Since it exists, let's go to the controlling activity.
           myIntent = new Intent(getApplicationContext(), InstallActivity.class);

       }

       // And start that intent.
       startActivity(myIntent);


   } // End of whereTo

} // End of main activity.

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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ControllingActivity extends AppCompatActivity {

    String directoryString;
    SeekBar SBR, SBG, SBB, SBSat, SBVal, SBCon, SBHue;
    int RedBar, GreenBar, BlueBar, SatBar, ValBar, ConBar, HueBar, invertEd;
    Boolean onBoot, grayScale, inverTED, kStart, kStop;
    Switch SonBoot, SGrayScale, SInverted;

    // Get the application context for the notification.
    public Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controlling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Make your adjustments and tap the enable button.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Define our context for our notification.
        context = getApplicationContext();

        // Define our seekbars.
        SBR = (SeekBar) findViewById(R.id.seekBarRed);
        SBG = (SeekBar) findViewById(R.id.seekBarGreen);
        SBB = (SeekBar) findViewById(R.id.seekBarBlue);
        SBSat = (SeekBar) findViewById(R.id.seekBarSaturation);
        SBVal = (SeekBar) findViewById(R.id.seekBarValue);
        SBCon = (SeekBar) findViewById(R.id.seekBarContrast);
        SBHue = (SeekBar) findViewById(R.id.seekBarHue);

        // Set our maximums for our seekbars.
        SBR.setMax(255);
        SBG.setMax(255);
        SBB.setMax(255);
        SBSat.setMax(160);
        SBVal.setMax(255);
        SBCon.setMax(255);
        SBHue.setMax(1535);

        // Define our initial integers.
        RedBar = 0;
        GreenBar = 0;
        BlueBar = 0;
        SatBar = 0;
        ValBar = 0;
        ConBar = 0;
        HueBar = 0;
        invertEd = 0;

        // Define our initial booleans.
        onBoot = false;
        grayScale = false;
        inverTED = false;
        kStart = false;
        kStop = false;

        // Define our initial switches.
        SonBoot = (Switch) findViewById(R.id.switchBoot);
        SGrayScale = (Switch) findViewById(R.id.switchGray);
        SInverted = (Switch) findViewById(R.id.switchInverted);

        // And check for our config file.
        configFileCheck();

        // And read it.
        readConfig();

        // And set the seekbar sliders.
        setSliders();

        // And set up the listeners.
        seekbarListeners();

    }// End of onCreate

    // Check for the config file, if it doesn't exist, create it.
    public void configFileCheck () {

        // Name of the sdcard.
        directoryString = Environment.getExternalStorageDirectory().getPath();

        File file = new File(directoryString + "/documents/kppd.conf");

        if(file.exists()) {

            Toast.makeText(getApplicationContext(), "Configuration file found!", Toast.LENGTH_SHORT).show();

        } else {

            Toast.makeText(getApplicationContext(), "Configuration file not found, creating!", Toast.LENGTH_SHORT).show();

            try { // Surround with try and catch in case of failure.

                // Open your asset file as the input stream
                InputStream myInput;

                myInput = getApplicationContext().getAssets().open("postproc.conf");

                // Path to the just created empty file
                String outFileName = directoryString + "/documents/kppd.conf";

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
            } catch (IOException e) {
                e.printStackTrace();
            } // End of try and catch to copy kppd.conf.

            // Copy the files.
            String[] chmodpost = {"su", "-c", "chmod 666 "+directoryString+"/documents/kppd.conf"};
            try {
                Runtime.getRuntime().exec(chmodpost);
            } catch (IOException e) {
                e.printStackTrace();
            } // End of try catch for Copy files.

        } // End of file exists check.

    } // End of config file check method.

    // Read the config file.
    public void readConfig () {

        // Read the config file and turn it into our values.
        // Try this, in case it fails.
        try {

            // Load the file location.
            File file = new File(directoryString+"/documents/kppd.conf");

            // Get the length of the file.
            int length = (int) file.length();

            // Set your bytes to length.
            byte[] bytes = new byte[length];

            // Open an input stream.
            FileInputStream in = new FileInputStream(file);

            // And try to read it, when done, close it.
            try {
                in.read(bytes);
            } finally {
                in.close();
            }

            // Set those bytes to a string.
            String contents = new String(bytes);

            // Testing only.// Log.i("WJH", contents);

            String[] splitString = contents.split("[\\=\\[]");

            // Now we create an array and split it by the magic symbols we input when we exported it.
            // String[] splitString = contents.split("=");

            for (int j=0; j < splitString.length;j++) {

                // Testing only.// Log.i("WJH", splitString[j]);

                if (j == 6) {
                    RedBar = Integer.parseInt(splitString[j].replaceAll("[\\D]",""));
                } else if (j == 8) {
                    GreenBar = Integer.parseInt(splitString[j].replaceAll("[\\D]",""));
                } else if (j == 10) {
                    BlueBar = Integer.parseInt(splitString[j].replaceAll("[\\D]",""));
                } else if (j == 12) {
                    HueBar = Integer.parseInt(splitString[j].replaceAll("[\\D]",""));
                } else if (j == 14) {
                    SatBar = Integer.parseInt(splitString[j].replaceAll("[\\D]",""));
                } else if (j == 16) {
                    ValBar = Integer.parseInt(splitString[j].replaceAll("[\\D]",""));
                } else if (j == 18) {
                    ConBar = Integer.parseInt(splitString[j].replaceAll("[\\D]",""));
                } else if (j == 20) {
                    invertEd = Integer.parseInt(splitString[j].replaceAll("[\\D]",""));
                }

            }


        } catch (Exception e) {
            Log.i("WJH", "Can not read file: " + e.toString());
        }

    } // End of Read config file.

    // Set the seekbar sliders.
    public void setSliders () {

        SBR.setProgress(RedBar);
        SBG.setProgress(GreenBar);
        SBB.setProgress(BlueBar);
        SBVal.setProgress(ValBar);
        SBCon.setProgress(ConBar);
        SBHue.setProgress(HueBar);

        if (grayScale) {
            SBSat.setProgress(160);
            SBSat.setEnabled(false);
        } else {
            SBSat.setProgress(SatBar);
            SBSat.setEnabled(true);
        }

    } // End of set the seekbar sliders.

    // Turn on the seekbar listeners.
    public void seekbarListeners () {

        // Set up our seekbar red.
        SBR.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Set variable to i.
                RedBar = i;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // What to do when they touch the seekbar. In this case, nothing.
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // What to do when they release the seekbar. In this case, nothing.
            }
        }); // End seekbar red.

        // Set up our seekbar green.
        SBG.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Set variable to i.
                GreenBar = i;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // What to do when they touch the seekbar. In this case, nothing.
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // What to do when they release the seekbar. In this case, nothing.
            }
        }); // End seekbar green.

        // Set up our seekbar blue.
        SBB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Set variable to i.
                BlueBar = i;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // What to do when they touch the seekbar. In this case, nothing.
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // What to do when they release the seekbar. In this case, nothing.
            }
        }); // End seekbar Blue

        // Set up our seekbar sat.
        SBSat.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Set variable to i.
                SatBar = i;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // What to do when they touch the seekbar. In this case, nothing.
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // What to do when they release the seekbar. In this case, nothing.
            }
        }); // End seekbar sat.

        // Set up our seekbar val.
        SBVal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Set variable to i.
                ValBar = i;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // What to do when they touch the seekbar. In this case, nothing.
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // What to do when they release the seekbar. In this case, nothing.
            }
        }); // End seekbar val.

        // Set up our seekbar con.
        SBCon.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Set variable to i.
                ConBar = i;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // What to do when they touch the seekbar. In this case, nothing.
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // What to do when they release the seekbar. In this case, nothing.
            }
        }); // End seekbar con.

        // Set up our seekbar hue.
        SBHue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Set variable to i.
                HueBar = i;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // What to do when they touch the seekbar. In this case, nothing.
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // What to do when they release the seekbar. In this case, nothing.
            }
        }); // End seekbar hue.

        SonBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // If the slider is checked, set our boolean to true.
                onBoot = isChecked;
                nowBoot();
            }
        }); // End onboot.

        SInverted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // If the slider is checked, set our boolean to true.
                inverTED = isChecked;
                nowInvert();
            }
        }); // end inverted.

        SGrayScale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // If the slider is checked, set our boolean to true.
                grayScale = isChecked;
                nowGrayScale();
            }
        }); // end gray scale.

    } // End seekbar listeners.

    // KPPD start or stop script.
    public void kppdState () {

        if (kStop) {
            // stopping kppd.
            String[] stopKPPD = {"su", "-c", "pkill -2 kppd"};
            try {
                Runtime.getRuntime().exec(stopKPPD);
            } catch (IOException e) {
                e.printStackTrace();
            } // End of try catch for stopping kppd.
        } // end if stop.

        if (kStart) {
            // starting kppd.
            String[] chmodpost = {"su", "-c", "kppd " + directoryString + "/documents/kppd.conf"};
            try {
                Runtime.getRuntime().exec(chmodpost);
            } catch (IOException e) {
                e.printStackTrace();
            } // End of try catch for starting kppd.
        }// end if start.

    } // end kppd state.

    public void nowBoot () {

        // ?
    } // End now Boot.

    public void nowGrayScale () {

        if (grayScale) {
            SBSat.setProgress(160);
            SBSat.setEnabled(false);
        } else {
            SBSat.setProgress(SatBar);
            SBSat.setEnabled(true);
        }
    } // End now grayscale.

    public void nowInvert () {

       if (inverTED) {
            invertEd = 1;
        } else {
            invertEd = 0;
        }

    } // End now invert.

    // How to stop kppd.
    public void clickDisable (View view) {
        kStart = false;
        kStop = true;
        kppdState();
    } // End click disable.

    // What to do when we click enable.
    public void clickEnable (View view) {

        kStart = true;

        String exportConfig = "[mdp_version]=5\n[pa_version]=2 \n[red]="+
                RedBar+"\n[green]="+
                GreenBar+"\n[blue]="+
                BlueBar+"\n[hue]="+
                HueBar+"\n[saturation]="+
                SatBar+"\n[value]="+
                ValBar+"\n[contrast]="+
                ConBar+"\n[invert]="+
                invertEd;

        // Log.i("WJH", exportConfig); // Testing only.

        FileOutputStream fos = null;

        try {

            // Put our file together, we will name it the current date and time.
            final File myFile = new File(directoryString+"/documents/kppd.conf");

            // If it exists?
            if (!myFile.exists())
            {
                // Make a new one.
                myFile.createNewFile();
            }

            // Start our file output stream.
            fos = new FileOutputStream(myFile);

            // Put our export notes into it.
            fos.write(exportConfig.getBytes());

            // And close the stream.
            fos.close();

            // Tell the user it worked.
            Toast.makeText(getApplicationContext(), "Settings Changed!", Toast.LENGTH_SHORT).show();

            // Catch any exception.
        } catch (Exception eX) {

            eX.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error: Please check permissions....",
                    Toast.LENGTH_SHORT).show();

        }

        // And make sure kppd is on.
        kppdState();

    } // End of click enable.

    // What to do when we click restore.
    public void clickRestore (View view) {

        readConfig();
        setSliders();

    } // End of click restore.

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_controlling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_about) {

            aboutDialog();

            return true;

        } else if (id == R.id.action_website) {

            // Launch the website.
            Uri uriUrl = Uri.parse("https://thealaskalinuxuser.wordpress.com");
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);

            return true;

        } else if (id == R.id.action_swebsite) {

            // Launch the website.
            Uri uriUrl = Uri.parse("https://forum.xda-developers.com/g4/orig-development/kppd-configure-post-processing-settings-t3165247");
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);

            return true;

        } else if (id == R.id.action_gwebsite) {

            // Launch the website.
            Uri uriUrl = Uri.parse("https://github.com/alaskalinuxuser/app_kppdcontrol");
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);

            return true;

        }

        return super.onOptionsItemSelected(item);

    }// END On items selected menu.

    // Okay, so here we build the popup to tell them about the app.
    public void aboutDialog () {

        // Here is what the popup will do.
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("About")
                .setMessage(getString(R.string.about_app))
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // Testing only //
                        Log.i("WJH", "Chose OK.");// Testing only //
                    }
                })
                .show(); // Make sure you show your popup or it wont work very well!

    } // END About Dialog builder.

    @Override // To prevent going back to main screen.
    public void onBackPressed() {
        moveTaskToBack(true); // So, instead of going back, go to home screen.
    } // END back pressed.




}// End of controlling activity.

/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.samples.vision.barcodereader;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Main activity demonstrating how to pass extra parameters to an activity that
 * reads barcodes.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    // use a compound button so either checkbox or switch widgets work.
    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private TextView statusMessage;
    private TextView barcodeValue;
    private EditText targetNumber;
    private TextView targetName;
    private TextView targetState;
    private EditText targetUser;
    private Handler handler;


    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    String data_str = "";
    String log_str = "";
    String inventory_url = "http://192.168.0.249/inventory/";
    String tmp_state;
    String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusMessage = (TextView)findViewById(R.id.status_message);
        barcodeValue = (TextView)findViewById(R.id.barcode_value);
        targetNumber = (EditText)findViewById(R.id.target_number);
        targetName = (TextView)findViewById(R.id.target_name);
        targetState = (TextView)findViewById(R.id.target_state);
        targetUser = (EditText)findViewById(R.id.target_username);

        autoFocus = (CompoundButton) findViewById(R.id.auto_focus);
        useFlash = (CompoundButton) findViewById(R.id.use_flash);

        handler = new Handler();

        findViewById(R.id.read_barcode).setOnClickListener(this);
        findViewById(R.id.check_data).setOnClickListener(this);
        findViewById(R.id.update_ok).setOnClickListener(this);
        findViewById(R.id.update_note).setOnClickListener(this);
        findViewById(R.id.update_not_found).setOnClickListener(this);
//        ((EditText) findViewById(R.id.target_number)).addTextChangedListener(this);

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_barcode) {
            // launch barcode activity.
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
            intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());
            startActivityForResult(intent, RC_BARCODE_CAPTURE);

            this.getData();
        }
        if (v.getId() == R.id.check_data) {
            this.getData();
        }
        if (v.getId() == R.id.update_ok){
            tmp_state = "1";
            this.updateData();
        }
        if (v.getId() == R.id.update_note){
            tmp_state = "2";
            this.updateData();
        }
        if (v.getId() == R.id.update_not_found) {
            tmp_state = "4";
            this.updateData();
        }
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    statusMessage.setText(R.string.barcode_success);
                    barcodeValue.setText(barcode.displayValue.replaceAll(" ", ""));

                    targetNumber.setText(barcode.displayValue.replaceAll(" ", ""));
//                    this.getData();

                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    static String stateToString(String state) {
        String str;
        int int_state;
        try {
            int_state = Integer.parseInt(state.replaceAll(" ", ""));
        }catch(NumberFormatException e){
            return "Error";
        }

        if (state.length() == 0) {
            str = "Not Checked";
        } else if (int_state == 0) {
            str = "Not Checked";
        } else if (int_state == 1) {
            str = "OK";
        } else if (int_state == 2) {
            str = "Note";
        } else if (int_state == 4) {
            str = "Warning";
        } else {
            str = "Not Checked";
        }
        return str;

    }

    static String InputStreamToString(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    public void getData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                TextView textViewScannedData = (TextView) findViewById(R.id.textViewScannedData);

                String number = targetNumber.getText().toString();
//                String number = "0";

                String send_string = "";
                try {
                    send_string = "?number=" + URLEncoder.encode(number, "UTF8");
                    Log.d("TAG", send_string);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                try {
                    URL url = new URL(inventory_url +"getinfo.php" + send_string);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    data_str = InputStreamToString(con.getInputStream());
                    con.disconnect();
                    Log.d("TAG", data_str);

                } catch (Exception ex) {
                    System.out.println(ex);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView targetName = (TextView) findViewById(R.id.target_name);
                        TextView targetTeam = (TextView) findViewById(R.id.target_team);
                        TextView targetState = (TextView) findViewById(R.id.target_state);

                        targetName.setText(data_str);
                        String[] record = data_str.split(",", 0);

                        if (record.length < 4 || record[0].isEmpty()) {
                            targetName.setText("Error!");
                            targetTeam.setText("");
                            targetState.setText("");
                        }else {
                            targetName.setText(record[1]);
                            targetTeam.setText(record[2]);
                            targetState.setText(stateToString(record[3]));
                        }
                    }
                });
            }
        }).start();
    }

    private void updateData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String number = targetNumber.getText().toString();
                String user = targetUser.getText().toString();
                String checked = tmp_state;

                String send_string = new String();
                try {
                    send_string = "update.php?cmd=update&number=" + URLEncoder.encode(number, "UTF8") + "&checked=" + URLEncoder.encode(checked, "UTF8") + "&user=" + URLEncoder.encode(user, "UTF8");
                    Log.d("TAG", send_string);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                try {
                    URL url = new URL(inventory_url + send_string);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    msg = InputStreamToString(con.getInputStream());
                    con.disconnect();
                    Log.d("TAG", msg);
                    log_str = send_string;
                } catch (Exception ex) {
                    System.out.println(ex);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                    }
                });
                getData();
            }
        }).start();
    }

}

package com.thetratruoc.vn.token;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
import android.widget.*;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    ArrayList arTokens;
    ArrayList arSteps;
    private DBAdapter mDB;
    private Cursor mCursor;

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDB = new DBAdapter(getApplicationContext());
        mDB.open();
        mCursor = mDB.getAllToken();
        arTokens = new ArrayList();
        TokenItem tokenItem;
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    String strSerialID = mCursor.getString(mCursor.getColumnIndex("s_serial_id"));
                    String strKey = mCursor.getString(mCursor.getColumnIndex("s_key"));
                    String strPlatform = mCursor.getString(mCursor.getColumnIndex("s_platform"));
                    tokenItem = new TokenItem();
                    tokenItem.setSerialID(strSerialID);
                    tokenItem.setKey(strKey);
                    tokenItem.setPlatform(strPlatform);
                    arTokens.add(tokenItem);
                } while (mCursor.moveToNext());
            }
        }
        mCursor.close();
        mDB.close();
        tokenItem = new TokenItem();
        tokenItem.setPlatform("+ Tạo Token mới");
        arTokens.add(tokenItem);
        final ListView listView = (ListView) findViewById(R.id.custom_list);
        View headerView = ((LayoutInflater) this.getSystemService(this.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.header, null, false);
        listView.addHeaderView(headerView);
        listView.setAdapter(new ListTokenAdapter(MainActivity.this, arTokens));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == arTokens.size()) {
                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    View dialogview = inflater.inflate(R.layout.login, null);
                    AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(MainActivity.this);
                    dialogbuilder.setTitle("Đăng nhập");
                    dialogbuilder.setView(dialogview);
                    final AlertDialog dialogDetails = dialogbuilder.create();
                    dialogDetails.show();
                    Button cancelButton = (Button) dialogDetails.findViewById(R.id.btn_cancel);
                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogDetails.dismiss();
                        }
                    });
                    Button loginButton = (Button) dialogDetails.findViewById(R.id.btn_login);
                    final EditText email = (EditText) dialogDetails.findViewById(R.id.txt_email);
                    final EditText password = (EditText) dialogDetails.findViewById(R.id.txt_password);
                    loginButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogDetails.dismiss();
                            String[] params = new String[]{email.getText().toString(), password.getText().toString()};
                            new TokenTaskCreate().execute(params);
                        }
                    });
                } else if (position >= 0) {
                    Intent intent = new Intent(getApplicationContext(), TokenActivity.class);
                    TokenItem token = (TokenItem) listView.getItemAtPosition(position);
                    intent.putExtra("TOKEN_PLATFORM", token.getPlatform());
                    intent.putExtra("TOKEN_SERIAL_ID", token.getSerialID());
                    intent.putExtra("TOKEN_KEY", token.getKey());
                    startActivity(intent);
                }
            }
        });
    }

    private class TokenTaskCreate extends AsyncTask<String, Void, Integer> {
        ProgressDialog progDialog;
        String accountID;
        String serialID;
        String key;
        String platForm;

        @Override
        protected Integer doInBackground(String... params) {
            int result;
            String serverUrl = getResources().getString(R.string.server_url) + Constant.LOGIN;
            Log.i("serverUrl", serverUrl);
            Map<String, String> pr = new HashMap<String, String>();
            pr.put("e", params[0]);
            pr.put("p", params[1]);
            pr.put("l", "vn");
            Log.i("URL", serverUrl);
            try {
                String postResponse = Common.postCtx(serverUrl, pr,getBaseContext());
                Log.i("Post return", postResponse);
                if (!"ERROR".equals(postResponse)) {
                    JSONObject map = new JSONObject(postResponse);
                    if ("SUCCESS".equals(map.getString("ACK"))) {
                        accountID = String.valueOf(map.get("s_account_id"));
                        serverUrl = getResources().getString(R.string.server_url) + Constant.TOKEN_CREATE;
                        Log.i("serverUrl", serverUrl);
                        String uuid = Settings.Secure.getString(MainActivity.this.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                        String name = android.os.Build.MODEL;
                        String version = Build.VERSION.RELEASE;
                        Date today = new Date();
                        String date = Common.dateToString(today, "yyyyMMddHHmmss", "");
                        pr = new HashMap<String, String>();
                        pr.put("acc", accountID);
                        pr.put("uuid", uuid);
                        pr.put("name", name);
                        pr.put("version", version);
                        pr.put("d_date", date);
                        Log.i("URL", serverUrl);
                        postResponse = Common.postCtx(serverUrl, pr,getBaseContext());
                        Log.i("Post return", postResponse);
                        if (!"ERROR".equals(postResponse)) {
                            map = new JSONObject(postResponse);
                            if ("SUCCESS".equals(map.getString("ACK"))) {
                                serialID = String.valueOf(map.get("S_SERIAL_ID"));
                                key = String.valueOf(map.get("S_KEY"));
                                Log.i("accID", accountID);
                                Log.i("serialID", serialID);
                                Log.i("key", key);
                                mDB = new DBAdapter(getApplicationContext());
                                mDB.open();
                                mCursor = mDB.getAllToken();
                                platForm = "New Token " + (mCursor.getCount() + 1);
                                Log.i("platForm", platForm);
                                mCursor.close();
                                mDB.createToken(serialID, key, platForm);
                                mDB.close();
                                result = 1;
                            } else {
                                result = 2;
                            }
                        } else {
                            result = 3;
                        }
                    } else {
                        result = 4;
                    }
                } else {
                    result = 5;
                }
            } catch (Exception e) {
                result = 6;
                Log.e("Loi", e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            progDialog = new ProgressDialog(MainActivity.this);
            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDialog.setMessage("Loading...");
            progDialog.setCanceledOnTouchOutside(false);
            progDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected void onPostExecute(Integer result) {
            progDialog.hide();
            if (result == 1) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.succ_3), Toast.LENGTH_LONG)
                        .show();
                Intent intent = new Intent(getApplicationContext(), TokenActivity.class);
                intent.putExtra("TOKEN_PLATFORM", platForm);
                intent.putExtra("TOKEN_SERIAL_ID", serialID);
                intent.putExtra("TOKEN_KEY", key);
                startActivity(intent);
            } else if (result == 2) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.err_2), Toast.LENGTH_LONG)
                        .show();
            } else if (result == 3) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.err_3), Toast.LENGTH_LONG)
                        .show();
            } else if (result == 4) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.err_8), Toast.LENGTH_LONG)
                        .show();
            } else if (result == 5) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.err_3), Toast.LENGTH_LONG)
                        .show();
            } else if (result == 6) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.err_5), Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    public void lunchApp() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tutorial:
                arSteps = new ArrayList();
                Step step = new Step(getResources().getString(R.string.tutorial_step));
                arSteps.add(step);
                step = new Step(getResources().getString(R.string.r_1));
                arSteps.add(step);
                step = new Step(getResources().getString(R.string.r_2));
                arSteps.add(step);
                step = new Step(getResources().getString(R.string.r_3));
                arSteps.add(step);
                step = new Step(getResources().getString(R.string.r_4));
                arSteps.add(step);
                step = new Step(getResources().getString(R.string.r_5));
                arSteps.add(step);
                step = new Step(getResources().getString(R.string.r_6));
                arSteps.add(step);
                Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.tutorial);
                dialog.setTitle(R.string.tutorial);
                final ListView listView = (ListView) dialog.findViewById(R.id.list_tutorial);
                listView.setAdapter(new ListTutorialAdapter(MainActivity.this, arSteps));
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                return true;
            case R.id.change_pin:
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                View dialogview = inflater.inflate(R.layout.change_pin, null);
                AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(MainActivity.this);
                dialogbuilder.setTitle(getResources().getString(R.string.change_pin));
                dialogbuilder.setView(dialogview);
                final AlertDialog dialogDetails = dialogbuilder.create();
                dialogDetails.setCanceledOnTouchOutside(false);
                dialogDetails.show();
                Button cancelButton = (Button) dialogDetails.findViewById(R.id.btn_cancel);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogDetails.dismiss();
                    }
                });
                Button loginButton = (Button) dialogDetails.findViewById(R.id.btn_confirm);
                final EditText pin = (EditText) dialogDetails.findViewById(R.id.id_pin);
                final EditText newPin = (EditText) dialogDetails.findViewById(R.id.id_new_pin);
                final EditText reNewPin = (EditText) dialogDetails.findViewById(R.id.id_re_new_pin);
                loginButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDB = new DBAdapter(getApplicationContext());
                        mDB.open();
                        mCursor = mDB.login(pin.getText().toString());
                        if (mCursor != null && mCursor.moveToFirst()) {
                            if ("".equals(newPin.getText().toString()) || newPin.getText().toString().length() < 4) {
                                mCursor.close();
                                mDB.close();
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.alert_pin_length), Toast.LENGTH_LONG)
                                        .show();
                                newPin.setFocusable(true);
                            } else if (!newPin.getText().toString().equals(reNewPin.getText().toString())) {
                                mCursor.close();
                                mDB.close();
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.txt_re_new_pin), Toast.LENGTH_LONG)
                                        .show();
                                reNewPin.setFocusable(true);
                            } else {
                                mCursor.close();
                                mDB.updatePass(pin.getText().toString());
                                mDB.close();
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.change_pin_succ), Toast.LENGTH_LONG)
                                        .show();
                                dialogDetails.dismiss();
                            }


                        } else {
                            mCursor.close();
                            mDB.close();
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.alert_old_pin_invalid), Toast.LENGTH_LONG)
                                    .show();
                            pin.setFocusable(true);
                        }
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

}

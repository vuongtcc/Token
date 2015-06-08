package com.thetratruoc.vn.token;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
import android.widget.*;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Vuong
 * Date: 30/07/2013
 * Time: 10:37
 */
public class TokenActivity extends Activity {
    private String serialID;
    private String platform;
    private String key;
    TextView textNumber;
    TextView textPlatform;
    DBAdapter mDB;
    ProgressBar processBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.token);
        Intent intent = getIntent();
        serialID = intent.getStringExtra("TOKEN_SERIAL_ID");
        platform = intent.getStringExtra("TOKEN_PLATFORM");
        key = intent.getStringExtra("TOKEN_KEY");
        textPlatform = (TextView) findViewById(R.id.token_platform);
        textNumber = (TextView) findViewById(R.id.token_number);
        processBar = (ProgressBar) findViewById(R.id.token_process);
        processBar.setMax(59999);
        textPlatform.setText(platform);
        String otp = zenOTP();
        textNumber.setText(otp);
        new RefreshOTPTask().execute(new Date());
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    LayoutInflater inflater = LayoutInflater.from(TokenActivity.this);
                    View dialogview = inflater.inflate(R.layout.login, null);
                    AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(TokenActivity.this);
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
                            new TokenTaskDelete().execute(params);
                        }
                    });
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.cancel();
                    break;
            }
        }
    };

    private class TokenTaskDelete extends AsyncTask<String, Void, Integer> {
        ProgressDialog progDialog;
        String accountID;

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
                        serverUrl = getResources().getString(R.string.server_url) + Constant.TOKEN_DELETE;
                        Log.i("serverUrl", serverUrl);
                        pr = new HashMap<String, String>();
                        pr.put("acc", accountID);
                        pr.put("serial", serialID);
                        Log.i("URL", serverUrl);
                        postResponse = Common.postCtx(serverUrl, pr,getBaseContext());
                        Log.i("Post return", postResponse);
                        if (!"ERROR".equals(postResponse)) {
                            map = new JSONObject(postResponse);
                            if ("SUCCESS".equals(map.getString("ACK"))) {
                                mDB = new DBAdapter(getApplicationContext());
                                mDB.open();
                                boolean bl = mDB.deleteToken(serialID);
                                if (bl) {
                                    result = 1;
                                } else {
                                    result = 2;
                                }
                                mDB.close();
                            } else {
                                result = 3;
                            }
                        } else {
                            result = 4;
                        }
                    } else {
                        result = 5;
                    }
                } else {
                    result = 6;
                }
            } catch (Exception e) {
                result = 7;
                Log.e("Loi", e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            progDialog = new ProgressDialog(TokenActivity.this);
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
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.succ_1), Toast.LENGTH_LONG)
                        .show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            } else if (result == 2) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.err_1), Toast.LENGTH_LONG)
                        .show();
            } else if (result == 3) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.err_2), Toast.LENGTH_LONG)
                        .show();
            } else if (result == 4) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.err_3), Toast.LENGTH_LONG)
                        .show();
            } else if (result == 5) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.err_4), Toast.LENGTH_LONG)
                        .show();
            } else if (result == 6) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.err_3), Toast.LENGTH_LONG)
                        .show();
            } else if (result == 7) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.err_5), Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    private String zenOTP() {
        String uuid = Settings.Secure.getString(TokenActivity.this.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        Date today = new Date();
        String date = Common.dateToString(today, "yyyyMMddHHmm", "");
        String otp = Common.zenToken(uuid + date, key);
        return otp;
    }

    private class RefreshOTPTask extends AsyncTask<Date, Integer, Void> {
        @Override
        protected Void doInBackground(Date... params) {
            try {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                publishProgress((int) (params[0].getTime() - cal.getTime().getTime()));
                Thread.sleep(1);
            } catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            processBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            String otp = zenOTP();
            textNumber.setText(otp);
            new RefreshOTPTask().execute(new Date());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.token, menu);
        return true;
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return super.onCreatePanelMenu(featureId, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.token_serial:
                AlertDialog.Builder builder = new AlertDialog.Builder(TokenActivity.this);
                builder.setMessage("Số: " + serialID);
                builder.setTitle("Serial");
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case R.id.token_edit:
                AlertDialog.Builder alert = new AlertDialog.Builder(TokenActivity.this);
                alert.setTitle(textPlatform.getText().toString());
                alert.setMessage("Sửa thành");
                final EditText inputText = new EditText(TokenActivity.this);
                alert.setView(inputText);
                alert.setPositiveButton("Sửa", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = inputText.getText().toString();
                        if (!"".equals(value) && value != null) {
                            mDB = new DBAdapter(getApplicationContext());
                            mDB.open();
                            boolean bl = mDB.updateToken(serialID, value);
                            if (bl) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.succ_2), Toast.LENGTH_LONG)
                                        .show();
                                textPlatform.setText(value);
                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.err_6), Toast.LENGTH_LONG)
                                        .show();
                            }
                            mDB.close();
                        } else {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.err_7), Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
                alert.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });
                alert.show();
                return true;
            case R.id.token_delete:
                builder = new AlertDialog.Builder(TokenActivity.this);
                builder.setMessage("Xóa Token?").setPositiveButton("Xóa", dialogClickListener)
                        .setNegativeButton("Hủy", dialogClickListener).show();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

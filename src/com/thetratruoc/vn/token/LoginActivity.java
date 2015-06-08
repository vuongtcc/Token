package com.thetratruoc.vn.token;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created with IntelliJ IDEA.
 * User: Vuong
 * Date: 07/08/2013
 * Time: 22:35
 */
public class LoginActivity extends Activity {
    private DBAdapter mDB;
    private Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
        mDB = new DBAdapter(getApplicationContext());
        mDB.open();
        mCursor = mDB.getAllPass();
        if (mCursor == null || !mCursor.moveToFirst()) {
            mCursor.close();
            mDB.close();
            createPin();
        } else {
            mCursor.close();
            mDB.close();
            loginPin();
        }
    }

    public void loginPin() {
        LayoutInflater inflater = LayoutInflater.from(LoginActivity.this);
        View dialogview = inflater.inflate(R.layout.pin_login, null);
        AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(LoginActivity.this);
        dialogbuilder.setTitle(getResources().getString(R.string.title_pin));
        dialogbuilder.setView(dialogview);
        final AlertDialog dialogDetails = dialogbuilder.create();
        dialogDetails.setCanceledOnTouchOutside(false);
        dialogDetails.show();
        Button cancelButton = (Button) dialogDetails.findViewById(R.id.pin_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTaskToBack(true);
            }
        });
        Button loginButton = (Button) dialogDetails.findViewById(R.id.pin_login);
        final EditText pin = (EditText) dialogDetails.findViewById(R.id.txt_pin_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            int countLogin = 0;

            @Override
            public void onClick(View v) {
                mDB = new DBAdapter(getApplicationContext());
                mDB.open();
                mCursor = mDB.login(pin.getText().toString());
                if (mCursor != null && mCursor.moveToFirst()) {
                    countLogin = 0;
                    dialogDetails.dismiss();
                    mCursor.close();
                    mDB.close();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                } else {
                    mCursor.close();
                    mDB.close();
                    countLogin += 1;
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.pin_invalid), Toast.LENGTH_LONG)
                            .show();
                    pin.setText("");
                    pin.setFocusable(true);
                    if (countLogin == 3) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.alert_block_app), Toast.LENGTH_LONG)
                                .show();
                        pin.setText("");
                        pin.setFocusable(true);
                    } else if (countLogin == 5) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.alert_block_final), Toast.LENGTH_LONG)
                                .show();
                    } else if (countLogin > 5) {
                        mDB = new DBAdapter(getApplicationContext());
                        mDB.open();
                        mDB.deleteAllPass();
                        mDB.deleteAllToken();
                        mDB.close();
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.alert_data_deleted), Toast.LENGTH_LONG)
                                .show();
                        moveTaskToBack(true);
                    }
                }
            }
        });
    }

    public void createPin() {
        LayoutInflater inflater = LayoutInflater.from(LoginActivity.this);
        View dialogview = inflater.inflate(R.layout.create_pin, null);
        AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(LoginActivity.this);
        dialogbuilder.setTitle(getResources().getString(R.string.title_pin_create));
        dialogbuilder.setView(dialogview);
        final AlertDialog dialogDetails = dialogbuilder.create();
        dialogDetails.setCanceledOnTouchOutside(false);
        dialogDetails.show();
        Button cancelButton = (Button) dialogDetails.findViewById(R.id.btn_pin_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTaskToBack(true);
            }
        });
        Button loginButton = (Button) dialogDetails.findViewById(R.id.btn_pin_login);
        final EditText pin = (EditText) dialogDetails.findViewById(R.id.txt_pin);
        final EditText rePin = (EditText) dialogDetails.findViewById(R.id.txt_re_pin);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("".equals(pin.getText().toString()) || pin.getText().toString().length() < 4) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.alert_pin_length), Toast.LENGTH_LONG)
                            .show();
                } else if (!pin.getText().toString().equals(rePin.getText().toString())) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.pin_not_match), Toast.LENGTH_LONG)
                            .show();
                } else {
                    dialogDetails.dismiss();
                    mDB = new DBAdapter(getApplicationContext());
                    mDB.open();
                    long ln = mDB.createPass(pin.getText().toString());
                    if (ln > 0) {
                        mDB.close();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.pin_not_match), Toast.LENGTH_LONG)
                                .show();
                        moveTaskToBack(true);
                    }
                }
            }
        });
    }
}

package com.example.cxor;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import java.io.*;


public class DecodeActivity extends AppCompatActivity  {

    private static final String TAG = "DECODE_ACTIVITY";
    private static final int REQUEST_PERMISSIONS = 1;
    private static final int READ_REQUEST_CODE = 42;

    // UI references.
    private EditText mPasswordView;
    private EditText mDataView;
    private Button mOpenFileButton;
    private Button mDecodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);
        mayRequestContacts();

        mPasswordView = (EditText) findViewById(R.id.password);
        mDataView = (EditText) findViewById(R.id.data);

        mOpenFileButton = (Button) findViewById(R.id.open_file_button);
        mOpenFileButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                openFile();
            }
        });

        mDecodeButton = (Button) findViewById(R.id.decode_button);
        mDecodeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptDecode();
            }
        });

    }


    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Snackbar.make(mPasswordView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
                        }
                    });
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mayRequestContacts();
            }
        }
    }

    private void attemptDecode() {
        mPasswordView.setError(null);
        String password = mPasswordView.getText().toString();
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            mPasswordView.requestFocus();
            return;
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Открыть файл
     */
    private void openFile() {
        // ACTION_OPEN_DOCUMENT - открыть документ на редатирование
        // ACTION_GET_CONTENT - получить содержимое документа
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        // Фильтр, показывающий только те результаты, которые можно «открыть», например
        // файл (в отличие от списка контактов или часовых поясов)
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        try {
            if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                Uri uri = null;
                if (resultData != null) {
                    uri = resultData.getData();
                    Log.i(TAG, "Uri: " + uri.toString());

                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    mDataView.setText(stringBuilder.toString());
                    mDecodeButton.setEnabled(true);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error: ", e);
        }
    }
}

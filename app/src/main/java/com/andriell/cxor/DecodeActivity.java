package com.andriell.cxor;

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
import android.widget.*;
import com.andriell.cxor.file.CryptoFileInterface;
import com.andriell.cxor.file.CryptoFiles;

import java.io.*;


public class DecodeActivity extends AppCompatActivity {

    private static final String TAG = "DECODE_ACTIVITY";
    private static final int REQUEST_PERMISSIONS = 1;
    private static final int READ_REQUEST_CODE = 42;

    // UI references.
    private EditText mPasswordView;
    private Spinner mEncodeTypeSpinner;
    private EditText mDataEdit;
    private Button mOpenFileButton;
    private Button mDecodeButton;
    private Button mClearButton;
    private Button mSaveButton;
    private Button mEditButton;

    private Uri fileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);
        mayRequestContacts();

        mPasswordView = (EditText) findViewById(R.id.password);
        mEncodeTypeSpinner = (Spinner) findViewById(R.id.encode_type);

        String[] descriptions = CryptoFiles.getInstance().getDescriptions();
        ArrayAdapter adapter = new ArrayAdapter(this,  android.R.layout.simple_spinner_item, descriptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEncodeTypeSpinner.setAdapter(adapter);

        mDataEdit = (EditText) findViewById(R.id.data_edit);
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
                decodeAction();
            }
        });

        mClearButton = (Button) findViewById(R.id.clear_button);
        mClearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                clearAction();
            }
        });

        mSaveButton = (Button) findViewById(R.id.save_button);
        mSaveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //attemptDecode();
            }
        });

        mEditButton = (Button) findViewById(R.id.edit_button);
        mEditButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //attemptDecode();
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
                fileUri = null;
                if (resultData != null) {
                    fileUri = resultData.getData();
                    Log.i(TAG, "Uri: " + fileUri.toString());
                }
            }
            updateUi();
        } catch (Exception e) {
            Log.e(TAG, "Error: ", e);
        }
    }

    void decodeAction() {
        if (fileUri == null) {
            return;
        }
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            CryptoFileInterface cryptoFile = CryptoFiles.getInstance().getCryptoFile(mEncodeTypeSpinner.getSelectedItem().toString());
            cryptoFile.setPassword(mPasswordView.getText().toString().getBytes());
            cryptoFile.setInputStream(inputStream);
            mDataEdit.setText(new String(cryptoFile.read()));
            updateUi();
        } catch (Exception e) {
            Log.e(TAG, "Error: ", e);
        }
    }

    void clearAction() {
        fileUri = null;
        mPasswordView.setText("");
        mEncodeTypeSpinner.setSelection(0, true);
        mDataEdit.setText("");
        updateUi();
    }

    void updateUi() {
        mOpenFileButton.setEnabled(true);
        mDecodeButton.setEnabled(fileUri != null);
        mClearButton.setEnabled(true);
        mSaveButton.setEnabled(fileUri != null);
        mEditButton.setEnabled(fileUri != null);
    }
}

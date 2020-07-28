package com.andriell.cxor;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.andriell.cxor.crypto.HiddenString;
import com.andriell.cxor.file.CryptoFileInterface;
import com.andriell.cxor.file.CryptoFiles;
import com.andriell.cxor.ui.ShowDialogFragment;

import java.io.*;
import java.net.URLDecoder;


public class DecodeActivity extends AppCompatActivity {

    private static final String TAG = "DECODE_ACTIVITY";
    private static final String STATE_HIDDEN_STRING = "STATE_HIDDEN_STRING";
    private static final String STATE_IS_HIDE_MODE = "STATE_IS_HIDE_MODE";

    private static final int REQUEST_PERMISSIONS = 1;
    private static final int READ_REQUEST_CODE = 42;
    private static final int SAVE_FOLDER_RESULT_CODE = 43;

    private static final int DEFAULT_ENCODE_INDEX = 3;

    // UI references.
    private EditText mPasswordView;
    private Spinner mEncodeTypeSpinner;
    private Button mOpenFileButton;
    private Button mDecodeButton;
    private Button mClearButton;
    private Button mSaveButton;
    private Button mEditButton;
    private TextView mFileName;
    private EditText mDataEdit;

    private HiddenString stateHiddenString = null;
    private boolean stateHideMode = true;

    private Uri fileUri = null;
    private ShowDialogFragment showDialogFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //скроет заголовок
        getSupportActionBar().hide(); // скрыть строку заголовка
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); // Если фокус есть, скрыть клавиатуру до клика
        // this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // Включить полноэкранный режим
        setContentView(R.layout.activity_decode);
        mayRequestContacts();

        restoreState(savedInstanceState);

        showDialogFragment = new ShowDialogFragment();

        mPasswordView = (EditText) findViewById(R.id.password);
        mEncodeTypeSpinner = (Spinner) findViewById(R.id.encode_type);

        String[] descriptions = CryptoFiles.getInstance().getDescriptions();
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, descriptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEncodeTypeSpinner.setAdapter(adapter);
        mEncodeTypeSpinner.setSelection(DEFAULT_ENCODE_INDEX, true);


        mOpenFileButton = (Button) findViewById(R.id.open_file_button);
        mDecodeButton = (Button) findViewById(R.id.decode_button);
        mClearButton = (Button) findViewById(R.id.clear_button);
        mSaveButton = (Button) findViewById(R.id.save_button);
        mEditButton = (Button) findViewById(R.id.edit_button);
        mFileName = (TextView) findViewById(R.id.file_name);
        mDataEdit = (EditText) findViewById(R.id.data_edit);

        updateUi();
    }

    //<editor-fold desc="button actions">
    public void buttonOpenFileClick(View view) {
        openOpenFileDialog();
        hideKeyboardFrom(mPasswordView);
    }

    public void buttonDecodeClick(View view) {
        decodeAction();
        hideKeyboardFrom(mPasswordView);
    }

    public void buttonClearClick(View view) {
        clearAction();
        hideKeyboardFrom(mPasswordView);
    }

    public void buttonSaveClick(View view) {
        if (fileUri == null) {
            openSaveNewDialog();
        } else {
            saveFile();
        }
        hideKeyboardFrom(mPasswordView);
        updateUi();
    }

    public void buttonEditClick(View view) {
        stateHideMode = !stateHideMode;
        if (stateHideMode) {
            stateHiddenString.setData(mDataEdit.getText().toString());
            mDataEdit.setText(getSpannableString());
        } else {
            mDataEdit.setText(stateHiddenString.getString());
        }
        hideKeyboardFrom(mPasswordView);
        updateUi();
    }
    //</editor-fold>

    public SpannableString getSpannableString() {
        if (stateHiddenString == null) {
            return new SpannableString("");
        }
        int[] groups = stateHiddenString.getGroups();
        SpannableString ss = new SpannableString(stateHiddenString.getStringHidden());

        for (int i = 0; i < groups.length; i += 2) {
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    int start = mDataEdit.getSelectionStart();
                    int end = mDataEdit.getSelectionEnd();
                    String password = stateHiddenString.copy(start, end - start);
                    showDialogFragment.setMessage(password);
                    showDialogFragment.show(getSupportFragmentManager(), "missiles");
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.BLUE);
                    ds.setUnderlineText(false);
                }
            };
            ss.setSpan(clickableSpan, groups[i], groups[i + 1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return ss;
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            stateHiddenString = (HiddenString) savedInstanceState.getSerializable(STATE_HIDDEN_STRING);
            stateHideMode = savedInstanceState.getBoolean(STATE_IS_HIDE_MODE);
        }
        if (stateHiddenString == null) {
            stateHiddenString = new HiddenString();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(STATE_HIDDEN_STRING, stateHiddenString);
        savedInstanceState.putBoolean(STATE_IS_HIDE_MODE, stateHideMode);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreState(savedInstanceState);
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

    private void saveFile() {
        if (fileUri == null) {
            Log.e(TAG, "Error: fileUri is null");
            return;
        }
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(fileUri);
            CryptoFileInterface cryptoFile = getCryptoFile(outputStream);
            cryptoFile.save(mDataEdit.getText().toString().getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Error: ", e);
        }
    }

    /**
     * Сохранить как
     */
    private void openSaveNewDialog() {
        CryptoFileInterface cryptoFile = this.getCryptoFile();
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_TITLE, "key." + cryptoFile.getExtensions()[0]);
        intent.setType(cryptoFile.getMimeType());
        startActivityForResult(intent, SAVE_FOLDER_RESULT_CODE);
    }

    /**
     * Открыть файл
     */
    private void openOpenFileDialog() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        fileUri = null;
        if (resultCode != RESULT_OK || resultData == null || resultData.getData() == null) {
            return;
        }
        try {
            fileUri = resultData.getData();
            String fileUriString = fileUri == null ? "" : fileUri.toString();
            Log.i(TAG, "Uri: " + fileUriString);
            if (requestCode == READ_REQUEST_CODE) { // Открытие файла
                String fileExtension = fileUriString.substring(fileUriString.lastIndexOf('.') + 1).toLowerCase();
                int i = CryptoFiles.getInstance().getCryptoFileIndex(fileExtension);
                mEncodeTypeSpinner.setSelection(i);
                mDataEdit.setText("");
                stateHideMode = true;
            } else if (requestCode == SAVE_FOLDER_RESULT_CODE) { // Сохранение файла
                saveFile();
            }
            updateUi();
        } catch (Exception e) {
            Log.e(TAG, "Error: ", e);
        }
    }

    private void decodeAction() {
        if (fileUri == null) {
            return;
        }
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            CryptoFileInterface cryptoFile = getCryptoFile(inputStream);
            stateHiddenString.setData(new String(cryptoFile.read()));
            mDataEdit.setText(getSpannableString());
            updateUi();
        } catch (Exception e) {
            Log.e(TAG, "Error: ", e);
        }
    }

    private void clearAction() {
        fileUri = null;
        stateHiddenString = new HiddenString();
        stateHideMode = true;
        mPasswordView.setText("");
        mEncodeTypeSpinner.setSelection(DEFAULT_ENCODE_INDEX, true);
        mDataEdit.setText("");
        updateUi();
    }

    private void hideKeyboardFrom(View view) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void updateUi() {
        mDecodeButton.setEnabled(fileUri != null);
        mSaveButton.setEnabled(!(fileUri != null && stateHideMode));
        mSaveButton.setText(fileUri == null ? getString(R.string.s_new) : getString(R.string.save));
        mEditButton.setText(stateHideMode ? getString(R.string.edit) : getString(R.string.hide));
        mDataEdit.setMovementMethod(stateHideMode ? LinkMovementMethod.getInstance() : ArrowKeyMovementMethod.getInstance());
        mDataEdit.setFocusableInTouchMode(!stateHideMode);
        mDataEdit.setFocusable(!stateHideMode);
        mDataEdit.setBackgroundColor(stateHideMode ? Color.argb(16, 128, 128, 128) : Color.TRANSPARENT);

        try {
            if (fileUri != null) {
                String fileName = fileUri.toString();
                fileName = URLDecoder.decode(fileName, "UTF-8");
                int i = fileName.lastIndexOf("/");
                fileName = fileName.substring(i + 1);
                mFileName.setText(fileName);
            } else {
                mFileName.setText("");
            }

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Error: ", e);
        }
    }

    CryptoFileInterface getCryptoFile(OutputStream outputStream) {
        return getCryptoFile(null, outputStream);
    }

    CryptoFileInterface getCryptoFile(InputStream inputStream) {
        return getCryptoFile(inputStream, null);
    }

    CryptoFileInterface getCryptoFile() {
        return getCryptoFile(null, null);
    }

    CryptoFileInterface getCryptoFile(InputStream inputStream, OutputStream outputStream) {
        CryptoFileInterface cryptoFile = CryptoFiles.getInstance().getCryptoFile(mEncodeTypeSpinner.getSelectedItem().toString());
        cryptoFile.setPassword(mPasswordView.getText().toString().getBytes());
        if (inputStream != null) {
            cryptoFile.setInputStream(inputStream);
        }
        if (outputStream != null) {
            cryptoFile.setOutputStream(outputStream);
        }
        return cryptoFile;
    }
}

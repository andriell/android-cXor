package com.andriell.cxor;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
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


        mOpenFileButton = (Button) findViewById(R.id.open_file_button);
        mOpenFileButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                openOpenFileDialog();
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
                if (fileUri == null) {
                    openSaveNewDialog();
                } else {
                    saveFile();
                }
                updateUi();
            }
        });

        mEditButton = (Button) findViewById(R.id.edit_button);
        mEditButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                stateHideMode = !stateHideMode;
                if (stateHideMode) {
                    stateHiddenString.setData(mDataEdit.getText().toString());
                    mDataEdit.setText(stateHiddenString.getStringHidden());
                } else {
                    mDataEdit.setText(stateHiddenString.getString());
                }
                updateUi();
            }
        });

        mFileName = (TextView) findViewById(R.id.file_name);

        mDataEdit = (EditText) findViewById(R.id.data_edit);
        mDataEdit.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuItem menuItem;
                menu.clear();

                menuItem = menu.add(getString(R.string.show));
                menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (stateHiddenString != null) {
                            String password = getSelectedText();
                            showDialogFragment.setMessage(password);
                            showDialogFragment.show(getSupportFragmentManager(), "missiles");
                        }
                        return true;
                    }
                });
                menuItem = menu.add(getString(android.R.string.copy));
                menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        String password = getSelectedText();
                        ClipData clip = ClipData.newPlainText("label", password);
                        clipboard.setPrimaryClip(clip);
                        return true;
                    }
                });
                menuItem = menu.add(getString(R.string.share));
                menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        String password = getSelectedText();
                        sendIntent.putExtra(Intent.EXTRA_TEXT, password);
                        sendIntent.setType("text/plain");
                        Intent shareIntent = Intent.createChooser(sendIntent, null);
                        startActivity(shareIntent);
                        return true;
                    }
                });
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }

            private String getSelectedText() {
                int start = mDataEdit.getSelectionStart();
                int end = mDataEdit.getSelectionEnd();
                String password = stateHiddenString.copy(start, end - start);
                return password;
            }
        });

        updateUi();
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            stateHiddenString = (HiddenString) savedInstanceState.getSerializable(STATE_HIDDEN_STRING);
            stateHideMode = savedInstanceState.getBoolean(STATE_IS_HIDE_MODE);
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
            stateHiddenString = new HiddenString(new String(cryptoFile.read()));
            mDataEdit.setText(stateHiddenString.getStringHidden());
            updateUi();
        } catch (Exception e) {
            Log.e(TAG, "Error: ", e);
        }
    }

    private void clearAction() {
        fileUri = null;
        stateHiddenString = null;
        stateHideMode = true;
        mPasswordView.setText("");
        mEncodeTypeSpinner.setSelection(0, true);
        mDataEdit.setText("");
        updateUi();
    }

    private void updateUi() {
        mDecodeButton.setEnabled(fileUri != null);
        mSaveButton.setText(fileUri == null ? getString(R.string.s_new) : getString(R.string.save));
        mEditButton.setText(stateHideMode ? getString(R.string.edit) : getString(R.string.hide));

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

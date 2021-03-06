package com.mobapde.vince.mobapde;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupAccountActivity extends AppCompatActivity {

    ImageButton imgBtnProfile;
    EditText etName;
    Button btnFinishSetup;
    ImageView imgPlus;

    private Uri profileUri = null;

    private static final int GALLERY_REQUEST = 1;

    private DatabaseReference mDatabaseUsers;
    private StorageReference mStorageImage;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
    public Boolean editting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_account);

        Toolbar toolbar = (Toolbar) findViewById(R.id.setupToolbar);
        toolbar.setTitle("Setup Account");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        mStorageImage = FirebaseStorage.getInstance().getReference().child("profile_images");

        mProgress = new ProgressDialog(SetupAccountActivity.this);

        imgBtnProfile = (ImageButton) findViewById(R.id.img_btn_profile);
        etName = (EditText) findViewById(R.id.et_name);
        btnFinishSetup = (Button) findViewById(R.id.btn_finish_setup);
        imgPlus = (ImageView) findViewById(R.id.img_plus);

        imgBtnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        btnFinishSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSetupAccount();
            }
        });

        if (getIntent().getStringExtra("EDIT_NAME") != null) {
            editting = true;

            btnFinishSetup.setText("save changes");
            toolbar.setTitle("Edit Account");

            String curName = getIntent().getStringExtra("EDIT_NAME");
            String profileUrl = getIntent().getStringExtra("EDIT_PROFILE_URL");

            etName.setText(curName);
            Picasso.with(SetupAccountActivity.this).load(profileUrl).into(imgBtnProfile);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editting)
                    finish();
                else if (mAuth.getCurrentUser() != null) {
                    //mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).removeValue();
                    mAuth.signOut();
                }
            }
        });
    }

    private void startSetupAccount() {
        final String name = etName.getText().toString().trim();
        final String userId = mAuth.getCurrentUser().getUid();

        if ((!TextUtils.isEmpty(name) && profileUri != null) || (editting && !TextUtils.isEmpty(name))) {

            if (!editting)
                mProgress.setMessage("Finishing setup...");
            else
                mProgress.setMessage("Saving changes...");

            try {
                mProgress.show();
            } catch (Exception e) {

            }

            try {
                StorageReference filePath = mStorageImage.child(profileUri.getLastPathSegment());
                filePath.putFile(profileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests") String downloadUri = taskSnapshot.getDownloadUrl().toString();

                        mDatabaseUsers.child(userId).child("name").setValue(name);
                        mDatabaseUsers.child(userId).child("image").setValue(downloadUri);

                        if (!editting)
                            mDatabaseUsers.child(userId).child("rotationId").setValue("_");

                        mProgress.dismiss();

                        Intent mainIntent = new Intent(SetupAccountActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        finish();
                        startActivity(mainIntent);
                        MainActivity.setupAccount = true;

                    }
                });
            } catch (Exception e) {
                mDatabaseUsers.child(userId).child("name").setValue(name);

                Intent mainIntent = new Intent(SetupAccountActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(mainIntent);
                MainActivity.setupAccount = true;
            }
        } else if (profileUri == null && !editting)
            Toast.makeText(SetupAccountActivity.this, "Please choose a photo.", Toast.LENGTH_SHORT).show();
        else if (TextUtils.isEmpty(name))
            Toast.makeText(SetupAccountActivity.this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                profileUri = result.getUri();
                imgBtnProfile.setImageURI(profileUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (editting)
            finish();
        else if (mAuth.getCurrentUser() != null) {
            //mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).removeValue();
            mAuth.signOut();
        }
        /*Intent loginIntent = new Intent(SetupAccountActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(loginIntent);*/
    }
}

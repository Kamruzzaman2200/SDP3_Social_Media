package com.example.simplesocialmediaapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionListenerAdapter;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.simplesocialmediaapp.Models.ProfileModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SignInActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;
    CollectionReference collectionReference;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    Bitmap bitmap;

    ConstraintLayout layout_main;
    CardView cv_signin,cv_register,cv_register_image;
    ImageView imv_register;
    EditText et_signin_email,et_signin_passwd,et_register_username,et_register_email,et_register_passwd,et_register_confpasswd;
    Button btn_signin,btn_register;
    TextView tv_register,tv_signin;

    ActivityResultLauncher<PickVisualMediaRequest> pickVisualMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null)
                {
                    imv_register.setImageURI(uri);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                else
                {
                    if (imv_register.getDrawable() == null)
                    {
                        Toast.makeText(this, "Please Select an Image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initvar();

        checklogin();

        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Transition transition = new Slide(Gravity.LEFT);
                transition.addTarget(cv_signin);
                transition.setDuration(1000);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    transition.addListener(new TransitionListenerAdapter() {
                        @Override
                        public void onTransitionStart(Transition transition) {
                            super.onTransitionStart(transition);

                            Transition transition1 = new Slide(Gravity.RIGHT);
                            transition1.addTarget(cv_register);
                            transition1.setDuration(1000);

                            TransitionManager.beginDelayedTransition(layout_main,transition1);
                            cv_register.setVisibility(View.VISIBLE);
                        }
                    });
                }

                TransitionManager.beginDelayedTransition(layout_main,transition);
                cv_signin.setVisibility(View.GONE);
            }
        });

        tv_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Transition transition = new Slide(Gravity.RIGHT);
                transition.addTarget(cv_register);
                transition.setDuration(1000);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    transition.addListener(new TransitionListenerAdapter() {
                        @Override
                        public void onTransitionStart(Transition transition) {
                            super.onTransitionStart(transition);

                            Transition transition1 = new Slide(Gravity.LEFT);
                            transition1.addTarget(cv_signin);
                            transition1.setDuration(1000);

                            TransitionManager.beginDelayedTransition(layout_main,transition1);
                            cv_signin.setVisibility(View.VISIBLE);
                        }
                    });
                }

                TransitionManager.beginDelayedTransition(layout_main,transition);
                cv_register.setVisibility(View.GONE);
            }
        });

        cv_register_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickVisualMedia.launch(new PickVisualMediaRequest.
                        Builder().
                        setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).
                        build());
            }
        });

        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et_signin_email.getText().toString().equals(""))
                {
                    et_signin_email.setError("Please Enter Email ID");
                }
                else if(et_signin_passwd.getText().toString().equals(""))
                {
                    et_signin_passwd.setError("Please Enter Password");
                }
                else
                {
                    mAuth.signInWithEmailAndPassword(et_signin_email.getText().toString(),et_signin_passwd.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                if (!mAuth.getCurrentUser().isEmailVerified())
                                {
                                    Intent intent = new Intent(SignInActivity.this,HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else
                                {
                                    mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task1) {
                                            if (task1.isSuccessful())
                                            {
                                                Toast.makeText(SignInActivity.this, "Email Verification link sent. " +
                                                        "Please verify email to login", Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                Toast.makeText(SignInActivity.this, task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                }
                            }
                            else
                            {
                                Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imv_register.getDrawable() == null)
                {
                    Toast.makeText(SignInActivity.this, "Please select display picture", Toast.LENGTH_SHORT).show();
                }
                else if(et_register_username.getText().toString().equals(""))
                {
                    et_register_username.setError("Please Enter Display Name");
                }
                else if(et_register_email.getText().toString().equals(""))
                {
                    et_register_email.setError("Please Enter Email ID");
                }
                else if(et_register_passwd.getText().toString().equals(""))
                {
                    et_register_passwd.setError("Please Enter Password");
                }
                else if(et_register_confpasswd.getText().toString().equals(""))
                {
                    et_register_confpasswd.setError("Please Confirm Your Password");
                }
                else if(!et_register_passwd.getText().toString().equals(et_register_confpasswd.getText().toString()))
                {
                    Toast.makeText(SignInActivity.this, "Both passwords are not same", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                    builder.setMessage("User Registration in process, Please Wait!!!");
                    builder.setCancelable(false);

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    mAuth.createUserWithEmailAndPassword(et_register_email.getText().toString(),
                            et_register_passwd.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] image = baos.toByteArray();

                                UploadTask uploadTask = storageReference.child(mAuth.getCurrentUser().getUid()).putBytes(image);
                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task1) {
                                        if (task1.isSuccessful())
                                        {
                                            String path = task1.getResult().getMetadata().getPath();
                                            ProfileModel model = new ProfileModel(mAuth.getCurrentUser().getUid(),
                                                    et_register_username.getText().toString(),et_register_email.getText().toString(),
                                                    et_register_passwd.getText().toString(),path);
                                            collectionReference.document(mAuth.getCurrentUser().getUid()).set(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task2) {
                                                    if (task2.isSuccessful())
                                                    {
                                                        alertDialog.dismiss();
                                                        Toast.makeText(SignInActivity.this, "User Registration Successful", Toast.LENGTH_SHORT).show();
                                                    }
                                                    else
                                                    {
                                                        alertDialog.dismiss();
                                                        mAuth.getCurrentUser().delete();
                                                        Toast.makeText(SignInActivity.this, task2.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                        else
                                        {
                                            alertDialog.dismiss();
                                            mAuth.getCurrentUser().delete();
                                            Toast.makeText(SignInActivity.this, task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                alertDialog.dismiss();
                                Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    void initvar()
    {
        mAuth = FirebaseAuth.getInstance();

        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection("Profiles");

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference("Profiles");

        layout_main = findViewById(R.id.main);

        cv_signin = findViewById(R.id.cv_signin);
        cv_register = findViewById(R.id.cv_register);
        cv_register_image = findViewById(R.id.cv_register_image);

        et_signin_email = findViewById(R.id.et_signin_email);
        et_signin_passwd = findViewById(R.id.et_signin_passwd);
        et_register_username = findViewById(R.id.et_register_uname);
        et_register_email = findViewById(R.id.et_register_email);
        et_register_passwd = findViewById(R.id.et_register_passwd);
        et_register_confpasswd = findViewById(R.id.et_register_confpasswd);

        imv_register = findViewById(R.id.imv_register);

        btn_signin = findViewById(R.id.btn_signin);
        btn_register = findViewById(R.id.btn_register);

        tv_register = findViewById(R.id.tv_register);
        tv_signin = findViewById(R.id.tv_signin);
    }

    void checklogin()
    {
        if (mAuth.getCurrentUser() != null)
        {
//            if (mAuth.getCurrentUser().isEmailVerified())
//            {
//                Intent intent = new Intent(SignInActivity.this,HomeActivity.class);
//                startActivity(intent);
//                finish();
//            }
            Intent intent = new Intent(SignInActivity.this,HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
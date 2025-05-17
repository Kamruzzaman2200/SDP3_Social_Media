package com.example.simplesocialmediaapp.Fragments;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.simplesocialmediaapp.Models.CommentsModel;
import com.example.simplesocialmediaapp.Models.PostModel;
import com.example.simplesocialmediaapp.Models.PostReferenceModel;
import com.example.simplesocialmediaapp.Models.PostSearchModel;
import com.example.simplesocialmediaapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateFragment extends Fragment {

    ConstraintLayout cl_create;
    EditText et_create_content;
    ImageButton imb_image,imb_delete_post,imb_image_delete;
    ImageView imv_post_image;
    Button btn_post;

    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference1,databaseReference2;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    Bitmap bitmap;

    ActivityResultLauncher<PickVisualMediaRequest> pickVisualMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null)
                {
                    imv_post_image.setImageURI(uri);
                    imv_post_image.setVisibility(View.VISIBLE);
                    imb_image_delete.setVisibility(View.VISIBLE);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(),uri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

    public static CreateFragment newInstance(String param1, String param2) {
        CreateFragment fragment = new CreateFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imb_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickVisualMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });

        imb_image_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imv_post_image.setImageDrawable(null);
                imv_post_image.setVisibility(View.GONE);
                imb_image_delete.setVisibility(View.GONE);
            }
        });

        imb_delete_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_create_content.setText("");
                imv_post_image.setImageDrawable(null);
                imv_post_image.setVisibility(View.GONE);
                imb_image_delete.setVisibility(View.GONE);
            }
        });

        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_create_content.getText().toString().equals("") && imv_post_image.getDrawable() == null)
                {
                    Toast.makeText(getContext(), "Cannot Create Post, No Content Entered", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Long timestamp = System.currentTimeMillis();
                    ArrayList<CommentsModel> comments = new ArrayList<>();
                    ArrayList<String> likes = new ArrayList<>();
                    PostModel model = new PostModel(mAuth.getCurrentUser().getUid(),
                            et_create_content.getText().toString(),"",timestamp.toString(),comments,likes);

                    databaseReference2.push().setValue(model, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error != null)
                            {
                                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String key = ref.getKey();
                                String postpath = ref.toString();
                                if (imv_post_image.getDrawable() != null)
                                {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setMessage("Post Being Uploaded, Please Wait!!!!");
                                    builder.setCancelable(false);

                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                    byte[] image = baos.toByteArray();

                                    UploadTask uploadTask = storageReference.child(key).putBytes(image);
                                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if (task.isSuccessful())
                                            {
                                                String path = task.getResult().getMetadata().getPath();
                                                model.setPath(path);
                                                databaseReference2.child(key).child("path").setValue(model.getPath(), new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError error1, @NonNull DatabaseReference ref1) {
                                                        if (error1 != null)
                                                        {
                                                            Toast.makeText(getContext(), error1.toString(), Toast.LENGTH_SHORT).show();
                                                        }
                                                        else
                                                        {
                                                            PostReferenceModel postReferenceModel = new PostReferenceModel(postpath,timestamp.toString());
                                                            databaseReference1.child(key).setValue(postReferenceModel);

                                                            if (!et_create_content.getText().toString().equals(""))
                                                            {
                                                                String text = et_create_content.getText().toString().toLowerCase();
                                                                ArrayList<String> content = new ArrayList<>();
                                                                for (int i=0; i<text.length(); i++)
                                                                {
                                                                    content.add(String.valueOf(text.charAt(i)));
                                                                }
                                                                String text1 = text;
                                                                for (int k=text.length() - 1; k>=0; k--)
                                                                {
                                                                    StringBuilder sb = new StringBuilder(text1);
                                                                    text1 = String.valueOf(sb.deleteCharAt(k));
                                                                    content.add(text1);
                                                                }
                                                                content.addAll(Arrays.asList(text.split(" ")));

                                                                PostSearchModel postSearchModel = new PostSearchModel(key,content);

                                                                CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Posts");
                                                                collectionReference.document(key).set(postSearchModel);
                                                            }

                                                            et_create_content.setText("");
                                                            imv_post_image.setImageDrawable(null);
                                                            imv_post_image.setVisibility(View.GONE);
                                                            imb_image_delete.setVisibility(View.GONE);
                                                            alertDialog.dismiss();

                                                            Toast.makeText(getContext(), "Post Created", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                            else
                                            {
                                                alertDialog.dismiss();
                                                Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                                else
                                {
                                    PostReferenceModel postReferenceModel = new PostReferenceModel(postpath,timestamp.toString());
                                    databaseReference1.child(key).setValue(postReferenceModel);

                                    if (!et_create_content.getText().toString().equals(""))
                                    {
                                        String text = et_create_content.getText().toString().toLowerCase();
                                        ArrayList<String> content = new ArrayList<>();
                                        for (int i=0; i<text.length(); i++)
                                        {
                                            content.add(String.valueOf(text.charAt(i)));
                                        }
                                        String text1 = text;
                                        for (int k=text.length() - 1; k>=0; k--)
                                        {
                                            StringBuilder sb = new StringBuilder(text1);
                                            text1 = String.valueOf(sb.deleteCharAt(k));
                                            content.add(text1);
                                        }
                                        content.addAll(Arrays.asList(text.split(" ")));

                                        PostSearchModel postSearchModel = new PostSearchModel(key,content);

                                        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Posts");
                                        collectionReference.document(key).set(postSearchModel);
                                    }

                                    et_create_content.setText("");
                                    imv_post_image.setImageDrawable(null);

                                    Toast.makeText(getContext(), "Post Created", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View item = inflater.inflate(R.layout.fragment_create, container, false);

        cl_create = item.findViewById(R.id.cl_create);

        et_create_content = item.findViewById(R.id.et_create_content);

        imb_image = item.findViewById(R.id.imb_image);
        imb_delete_post = item.findViewById(R.id.imb_delete_post);
        imb_image_delete = item.findViewById(R.id.imb_delete_image);

        imv_post_image = item.findViewById(R.id.imv_post_image);

        btn_post = item.findViewById(R.id.btn_post);

        mAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference2 = firebaseDatabase.getReference("Posts");
        databaseReference1 = firebaseDatabase.getReference("Users").child(mAuth.getCurrentUser().getUid());

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference("Posts").child(mAuth.getCurrentUser().getUid());

        return item;

    }
}
package com.example.simplesocialmediaapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplesocialmediaapp.Adapters.CommentAdapter;
import com.example.simplesocialmediaapp.Models.CommentsModel;
import com.example.simplesocialmediaapp.Models.PostModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CommentsActivity extends AppCompatActivity {

    ImageButton likes;
    TextView post_comments,post_likes;
    String name,postid;
    RecyclerView rv_comments;
    EditText et_comment;
    Button btn_comment;

    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    PostModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comments);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initvar();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                model = snapshot.getValue(PostModel.class);
                if (model != null) {
                    if (model.getLikes() != null) {
                        if (model.getLikes().contains(mAuth.getCurrentUser().getUid())) {
                            likes.setImageResource(R.drawable.like_filled);
                        }
                        post_likes.setText(String.valueOf(model.getLikes().size()));
                    } else {
                        post_likes.setText("0");
                        likes.setImageResource(R.drawable.like);
                    }

                    if (model.getComments() != null) {
                        post_comments.setText(String.valueOf(model.getComments().size()));
                    } else {
                        post_comments.setText("0");
                    }
                } else {
                    Toast.makeText(CommentsActivity.this, "Error loading post data", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommentsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> likeslist = new ArrayList<>();
                if(likes.getDrawable().getConstantState().equals(ResourcesCompat.getDrawable(
                        getResources(),R.drawable.like,null).getConstantState()))
                {
                    likes.setImageResource(R.drawable.like_filled);
                    if (model.getLikes()!=null)
                    {
                        likeslist = model.getLikes();
                    }
                    likeslist.add(mAuth.getCurrentUser().getUid());
                    post_likes.setText(String.valueOf(likeslist.size()));
                    model.setLikes(likeslist);
                    databaseReference.setValue(model);
                }
                else if(likes.getDrawable().getConstantState().equals(ResourcesCompat.getDrawable(
                        getResources(),R.drawable.like_filled,null).getConstantState()))
                {
                    likes.setImageResource(R.drawable.like);
                    likeslist = model.getLikes();
                    likeslist.remove(mAuth.getCurrentUser().getUid());
                    post_likes.setText(String.valueOf(likeslist.size()));
                    model.setLikes(likeslist);
                    databaseReference.setValue(model);
                }
            }
        });

        ArrayList<CommentsModel> commentsModelArrayList = new ArrayList<>();

        CommentAdapter adapter = new CommentAdapter(commentsModelArrayList,postid);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        rv_comments.setLayoutManager(layoutManager);
        rv_comments.setAdapter(adapter);

        databaseReference.child("comments").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                commentsModelArrayList.add(snapshot.getValue(CommentsModel.class));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btn_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_comment.getText().toString().equals(""))
                {
                    et_comment.setError("Please Enter a comment");
                }
                else
                {

                    ArrayList<String> likes = new ArrayList<>();

                    Long timestamp = System.currentTimeMillis();
                    CommentsModel comment = new CommentsModel(mAuth.getCurrentUser().getUid(),et_comment.getText().toString(),String.valueOf(timestamp),likes);
                    commentsModelArrayList.add(comment);
                    model.setComments(commentsModelArrayList);

                    databaseReference.setValue(model);

                    commentsModelArrayList.remove(comment);

                    et_comment.setText("");
                }
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    void initvar()
    {
        name = getIntent().getStringExtra("name");
        postid = getIntent().getStringExtra("key");

        likes = findViewById(R.id.likes);

        post_comments = findViewById(R.id.post_comments);
        post_likes = findViewById(R.id.post_likes);

        rv_comments = findViewById(R.id.rv_comments);
        et_comment = findViewById(R.id.et_comment);

        btn_comment = findViewById(R.id.btn_comment);

        mAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Posts").child(postid);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(name);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
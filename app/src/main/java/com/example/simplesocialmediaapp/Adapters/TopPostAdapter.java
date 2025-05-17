package com.example.simplesocialmediaapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.simplesocialmediaapp.CommentsActivity;
import com.example.simplesocialmediaapp.Models.PostModel;
import com.example.simplesocialmediaapp.Models.ProfileModel;
import com.example.simplesocialmediaapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class TopPostAdapter extends RecyclerView.Adapter<TopPostAdapter.viewholder> {

    private Context context;
    private ArrayList<PostModel> posts;

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public TopPostAdapter(Context context, ArrayList<PostModel> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.postcontent, parent, false);
        return new viewholder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        PostModel model = posts.get(position);

        holder.imv_post_uid.setImageDrawable(null);
        holder.imv_post_content.setImageDrawable(null);
        holder.et_post_content.setText("");
        holder.tv_post_uname.setText("");
        holder.tv_post_date.setText("");
        holder.tv_post_likes.setText("0");
        holder.tv_post_comments.setText("0");
        holder.imb_post_likes.setImageResource(R.drawable.like);

        holder.imb_content_delete.setVisibility(View.GONE);

        // Load profile
        firestore.collection("Profiles").document(model.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ProfileModel profile = documentSnapshot.toObject(ProfileModel.class);
                        if (profile != null) {
                            holder.tv_post_uname.setText(profile.getUsername());

                            StorageReference profileRef = storage.getReference(profile.getPath());
                            profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                Glide.with(context)
                                        .load(uri)
                                        .into(holder.imv_post_uid);
                            });
                        }
                    }
                });

        // Load post image
        if (model.getPath() != null && !model.getPath().isEmpty()) {
            StorageReference postRef = storage.getReference(model.getPath());
            postRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(context)
                        .load(uri)
                        .into(holder.imv_post_content);
                holder.imv_post_content.setVisibility(View.VISIBLE);
            });
        } else {
            holder.imv_post_content.setVisibility(View.GONE);
        }

        holder.et_post_content.setText(model.getContent());

        String date = DateFormat.format("hh:mm aa   dd-MM-yyyy", Long.parseLong(model.getTimestamp())).toString();
        holder.tv_post_date.setText(date);

        if (model.getLikes() != null) {
            holder.tv_post_likes.setText(String.valueOf(model.getLikes().size()));
            if (model.getLikes().contains(auth.getCurrentUser().getUid())) {
                holder.imb_post_likes.setImageResource(R.drawable.like_filled);
            }
        }

        if (model.getComments() != null) {
            holder.tv_post_comments.setText(String.valueOf(model.getComments().size()));
        }

        // Add click listener for likes button
        holder.imb_post_likes.setOnClickListener(v -> {
            ArrayList<String> likes = new ArrayList<>();
            if (model.getLikes() != null) {
                likes.addAll(model.getLikes());
            }
            
            if (likes.contains(auth.getCurrentUser().getUid())) {
                // Unlike
                likes.remove(auth.getCurrentUser().getUid());
                holder.imb_post_likes.setImageResource(R.drawable.like);
            } else {
                // Like
                likes.add(auth.getCurrentUser().getUid());
                holder.imb_post_likes.setImageResource(R.drawable.like_filled);
            }
            
            holder.tv_post_likes.setText(String.valueOf(likes.size()));
            model.setLikes(likes);
            
            // Update in Firestore
            firestore.collection("Posts").document(model.getUid()).set(model);
        });

        // Add click listener for comments button
        holder.imb_post_comments.setOnClickListener(v -> {
            // Get the post key from Realtime Database
            FirebaseDatabase.getInstance().getReference("Posts")
                .orderByChild("timestamp")
                .equalTo(model.getTimestamp())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Get the first (and should be only) matching post
                            DataSnapshot postSnapshot = snapshot.getChildren().iterator().next();
                            Intent intent = new Intent(context, CommentsActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("key", postSnapshot.getKey());
                            bundle.putString("name", holder.tv_post_uname.getText().toString());
                            intent.putExtras(bundle);
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "Post not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        });

        // Handle click to open comments
        holder.itemView.setOnClickListener(v -> {
            // Get the post key from Realtime Database
            FirebaseDatabase.getInstance().getReference("Posts")
                .orderByChild("timestamp")
                .equalTo(model.getTimestamp())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Get the first (and should be only) matching post
                            DataSnapshot postSnapshot = snapshot.getChildren().iterator().next();
                            Intent intent = new Intent(context, CommentsActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("key", postSnapshot.getKey());
                            bundle.putString("name", holder.tv_post_uname.getText().toString());
                            intent.putExtras(bundle);
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "Post not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class viewholder extends RecyclerView.ViewHolder {
        ImageView imv_post_uid, imv_post_content;
        TextView tv_post_uname, tv_post_date, tv_post_comments, tv_post_likes;
        EditText et_post_content;
        ImageButton imb_post_comments, imb_post_likes, imb_content_delete;

        public viewholder(@NonNull View itemView) {
            super(itemView);

            imv_post_uid = itemView.findViewById(R.id.imv_post_uid);
            imv_post_content = itemView.findViewById(R.id.imv_post_content);

            tv_post_uname = itemView.findViewById(R.id.tv_post_uname);
            tv_post_date = itemView.findViewById(R.id.tv_post_date);
            tv_post_comments = itemView.findViewById(R.id.tv_post_comments);
            tv_post_likes = itemView.findViewById(R.id.tv_post_likes);

            et_post_content = itemView.findViewById(R.id.et_post_content);

            imb_content_delete = itemView.findViewById(R.id.imb_content_delete);
            imb_post_comments = itemView.findViewById(R.id.imb_post_comments);
            imb_post_likes = itemView.findViewById(R.id.imb_post_likes);
        }
    }
}

package com.example.simplesocialmediaapp.Adapters;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.simplesocialmediaapp.CommentsActivity;
import com.example.simplesocialmediaapp.Models.CommentsModel;
import com.example.simplesocialmediaapp.Models.PostModel;
import com.example.simplesocialmediaapp.Models.PostReferenceModel;
import com.example.simplesocialmediaapp.Models.ProfileModel;
import com.example.simplesocialmediaapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class PostContentAdapter extends RecyclerView.Adapter<PostContentAdapter.viewholder>{

    ArrayList<PostReferenceModel> postReferenceModels;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    CollectionReference collectionReference = firebaseFirestore.collection("Profiles");

    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;

    public PostContentAdapter(ArrayList<PostReferenceModel> postReferenceModels)
    {
        this.postReferenceModels = postReferenceModels;
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.postcontent,parent,false);
        return new viewholder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {

        holder.imv_post_uid.setImageDrawable(null);
        holder.imv_post_content.setImageDrawable(null);
        holder.et_post_content.setText("");
        holder.tv_post_uname.setText("");
        holder.tv_post_date.setText("");
        holder.tv_post_likes.setText("0");
        holder.tv_post_comments.setText("0");
        holder.imb_post_likes.setImageResource(R.drawable.like);

        DatabaseReference databaseReference = firebaseDatabase.getReferenceFromUrl(postReferenceModels.get(position).getPostpath());
        String key1 = databaseReference.getKey();
        final String[] uid = new String[1];

        PostModel model = new PostModel();

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getKey().equals("content"))
                {
                    model.setContent(snapshot.getValue(String.class));
                    holder.et_post_content.setText(model.getContent());
                }
                else if (snapshot.getKey().equals("path"))
                {
                    model.setPath(snapshot.getValue(String.class));
                    if (!model.getPath().equals(""))
                    {
                        firebaseStorage.getReference(model.getPath()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(holder.imv_post_content.getContext())
                                        .load(uri)
                                        .into(holder.imv_post_content);
                            }
                        });
                    }
                    else
                    {
                        holder.imv_post_content.setVisibility(View.GONE);
                    }
                }
                else if (snapshot.getKey().equals("uid"))
                {
                    model.setUid(snapshot.getValue(String.class));
                    uid[0] = model.getUid();
                    if (mAuth.getCurrentUser().getUid().equals(uid[0]));
                    {
                        holder.imb_content_delete.setVisibility(View.VISIBLE);
                    }

                    storageReference = firebaseStorage.getReference("Profiles").child(model.getUid());
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(holder.imv_post_uid.getContext())
                                    .load(uri)
                                    .into(holder.imv_post_uid);
                        }
                    });

                    collectionReference.document(uid[0]).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            holder.tv_post_uname.setText(task.getResult().toObject(ProfileModel.class).getUsername());
                        }
                    });
                }
                else if (snapshot.getKey().equals("timestamp"))
                {
                    model.setTimestamp(snapshot.getValue(String.class));

                    String date = DateFormat.format("hh:mm aa   dd-MM-yyyy",Long.parseLong(model.getTimestamp())).toString();
                    holder.tv_post_date.setText(date);
                }
                else if (snapshot.getKey().equals("comments"))
                {
                    model.setComments((ArrayList<CommentsModel>) snapshot.getValue());
                    holder.tv_post_comments.setText(String.valueOf(model.getComments().size()));
                }
                else if (snapshot.getKey().equals("likes"))
                {
                    model.setLikes((ArrayList<String>) snapshot.getValue());
                    if (model.getLikes().contains(mAuth.getCurrentUser().getUid()))
                    {
                        holder.imb_post_likes.setImageResource(R.drawable.like_filled);
                    }
                    holder.tv_post_likes.setText(String.valueOf(model.getLikes().size()));
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getKey().equals("likes"))
                {
                    model.setLikes((ArrayList<String>) snapshot.getValue());
                    if (model.getLikes().contains(mAuth.getCurrentUser().getUid()))
                    {
                        holder.imb_post_likes.setImageResource(R.drawable.like_filled);
                    }
                    holder.tv_post_likes.setText(String.valueOf(model.getLikes().size()));
                }
                else if (snapshot.getKey().equals("comments"))
                {
                    model.setComments((ArrayList<CommentsModel>) snapshot.getValue());
                    holder.tv_post_comments.setText(String.valueOf(model.getComments().size()));
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.getKey().equals("likes"))
                {
                    model.setLikes(new ArrayList<String>());
                    holder.tv_post_likes.setText("0");
                    holder.imb_post_likes.setImageResource(R.drawable.like);
                }
                else if (snapshot.getKey().equals("comments"))
                {
                    model.setComments(new ArrayList<CommentsModel>());
                    holder.tv_post_comments.setText("0");
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.imb_content_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference databaseReference1 = firebaseDatabase.getReference("Users").child(uid[0]).child(key1);
                databaseReference1.removeValue();
                databaseReference.removeValue();
                if (!model.getPath().equals(""))
                {
                    firebaseStorage.getReference(model.getPath()).delete();
                }

                CollectionReference collectionReference1 = firebaseFirestore.collection("Posts");
                collectionReference1.document(key1).delete();
            }
        });

        holder.imb_post_likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> likes = new ArrayList<>();
                if(holder.imb_post_likes.getDrawable().getConstantState().equals(ResourcesCompat.getDrawable(
                        holder.itemView.getResources(),R.drawable.like,null).getConstantState()))
                {
                    holder.imb_post_likes.setImageResource(R.drawable.like_filled);
                    if (model.getLikes()!=null)
                    {
                        likes = model.getLikes();
                    }
                    likes.add(mAuth.getCurrentUser().getUid());
                    holder.tv_post_likes.setText(String.valueOf(likes.size()));
                    model.setLikes(likes);
                    databaseReference.setValue(model);
                }
                else if(holder.imb_post_likes.getDrawable().getConstantState().equals(ResourcesCompat.getDrawable(
                        holder.itemView.getResources(),R.drawable.like_filled,null).getConstantState()))
                {
                    holder.imb_post_likes.setImageResource(R.drawable.like);
                    likes = model.getLikes();
                    likes.remove(mAuth.getCurrentUser().getUid());
                    holder.tv_post_likes.setText(String.valueOf(likes.size()));
                    model.setLikes(likes);
                    databaseReference.setValue(model);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), CommentsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("key",key1);
                bundle.putString("name",holder.tv_post_uname.getText().toString());
                intent.putExtras(bundle);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postReferenceModels.size();
    }

    public static class viewholder extends RecyclerView.ViewHolder {
        ImageView imv_post_uid,imv_post_content;
        TextView tv_post_uname,tv_post_date,tv_post_comments,tv_post_likes;
        EditText et_post_content;
        ImageButton imb_post_comments,imb_post_likes,imb_content_delete;
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

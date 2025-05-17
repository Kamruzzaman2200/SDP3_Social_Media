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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.simplesocialmediaapp.CommentsActivity;
import com.example.simplesocialmediaapp.Models.PostModel;
import com.example.simplesocialmediaapp.Models.ProfileModel;
import com.example.simplesocialmediaapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class PostSearchAdapter extends RecyclerView.Adapter<PostSearchAdapter.viewholder> {

    ArrayList<String> postids;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    CollectionReference collectionReference = firebaseFirestore.collection("Profiles");

    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;

    public PostSearchAdapter(ArrayList<String> postids)
    {
        this.postids = postids;
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

        holder.imb_content_delete.setVisibility(View.GONE);

        final PostModel[] model = {new PostModel()};
        final String[] key = {new String()};

        String key1 = postids.get(position);

        databaseReference = firebaseDatabase.getReference("Posts").child(postids.get(position));

        if (!postids.get(position).equals(null))
        {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    model[0] = snapshot.getValue(PostModel.class);
                    key[0] = snapshot.getKey();

                    collectionReference.document(model[0].getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            ProfileModel profileModel = task.getResult().toObject(ProfileModel.class);

                            holder.tv_post_uname.setText(profileModel.getUsername());

                            storageReference = firebaseStorage.getReference(profileModel.getPath());

                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(holder.imv_post_uid.getContext())
                                            .load(uri)
                                            .into(holder.imv_post_uid);
                                }
                            });

                            if (!model[0].getPath().equals(""))
                            {
                                StorageReference storageReference1 = firebaseStorage.getReference(model[0].getPath());
                                storageReference1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Glide.with(holder.imv_post_content.getContext())
                                                .load(uri)
                                                .into(holder.imv_post_content);
                                    }
                                });
                                holder.imv_post_content.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                holder.imv_post_content.setVisibility(View.GONE);
                            }

                            holder.et_post_content.setText(model[0].getContent());

                            String date = DateFormat.format("hh:mm aa   dd-MM-yyyy",Long.parseLong(model[0].getTimestamp())).toString();
                            holder.tv_post_date.setText(date);

                            if (model[0].getLikes() != null)
                            {
                                if (model[0].getLikes().contains(mAuth.getCurrentUser().getUid()))
                                {
                                    holder.imb_post_likes.setImageResource(R.drawable.like_filled);
                                }
                                holder.tv_post_likes.setText(String.valueOf(model[0].getLikes().size()));
                            }

                            if (model[0].getComments() != null)
                            {
                                holder.tv_post_comments.setText(String.valueOf(model[0].getComments().size()));
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            holder.imb_post_likes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue()==null)
                            {
                                Toast.makeText(holder.itemView.getContext(), "Post Deleted", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                DatabaseReference databaseReference1 = firebaseDatabase.getReference("Posts").child(key[0]);

                                ArrayList<String> likes = new ArrayList<>();
                                if(holder.imb_post_likes.getDrawable().getConstantState().equals(ResourcesCompat.getDrawable(
                                        holder.itemView.getResources(),R.drawable.like,null).getConstantState()))
                                {
                                    holder.imb_post_likes.setImageResource(R.drawable.like_filled);
                                    if (model[0].getLikes()!=null)
                                    {
                                        likes = model[0].getLikes();
                                    }
                                    likes.add(mAuth.getCurrentUser().getUid());
                                    holder.tv_post_likes.setText(String.valueOf(likes.size()));
                                    model[0].setLikes(likes);
                                    databaseReference1.setValue(model[0]);
                                }
                                else if(holder.imb_post_likes.getDrawable().getConstantState().equals(ResourcesCompat.getDrawable(
                                        holder.itemView.getResources(),R.drawable.like_filled,null).getConstantState()))
                                {
                                    holder.imb_post_likes.setImageResource(R.drawable.like);
                                    likes = model[0].getLikes();
                                    likes.remove(mAuth.getCurrentUser().getUid());
                                    holder.tv_post_likes.setText(String.valueOf(likes.size()));
                                    model[0].setLikes(likes);
                                    databaseReference1.setValue(model[0]);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue()==null)
                        {
                            Toast.makeText(holder.itemView.getContext(), "Post Deleted", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Intent intent = new Intent(holder.itemView.getContext(), CommentsActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("key",key1);
                            bundle.putString("name",holder.tv_post_uname.getText().toString());
                            intent.putExtras(bundle);
                            holder.itemView.getContext().startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return postids.size();
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

package com.example.simplesocialmediaapp.Adapters;

import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.simplesocialmediaapp.Models.CommentsModel;
import com.example.simplesocialmediaapp.Models.ProfileModel;
import com.example.simplesocialmediaapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.viewholder> {

    ArrayList<CommentsModel> commentsModelArrayList = new ArrayList<>();

    String postid;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference("Posts");

    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    CollectionReference collectionReference = firebaseFirestore.collection("Profiles");

    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;

    public CommentAdapter(ArrayList<CommentsModel> commentsModelArrayList,String postid)
    {
        this.commentsModelArrayList = commentsModelArrayList;
        this.postid = postid;
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.postcomment,parent,false);
        return new viewholder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {

        int pos = position;

        CommentsModel model = commentsModelArrayList.get(position);

        if (model.getLikes()!=null)
        {
            holder.tv_comment_likes.setText(String.valueOf(model.getLikes().size()));
            if (model.getLikes().contains(mAuth.getCurrentUser().getUid()))
            {
                holder.imb_comment_likes.setImageResource(R.drawable.like_filled);
            }
        }

        String date = DateFormat.format("hh:mm aa   dd-MM-yyyy",Long.parseLong(model.getTimestamp())).toString();
        holder.tv_comment_date.setText(date);

        holder.et_post_comment.setText(model.getComment());

        collectionReference.document(model.getCommentuid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                ProfileModel profileModel = task.getResult().toObject(ProfileModel.class);

                storageReference = firebaseStorage.getReference(profileModel.getPath());

                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(holder.imv_comment_uid.getContext())
                                .load(uri)
                                .into(holder.imv_comment_uid);
                    }
                });

                holder.tv_comment_uname.setText(profileModel.getUsername());
            }
        });

        holder.imb_comment_likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> likes = new ArrayList<>();
                if(holder.imb_comment_likes.getDrawable().getConstantState().equals(ResourcesCompat.getDrawable(
                        holder.itemView.getResources(),R.drawable.like,null).getConstantState()))
                {
                    holder.imb_comment_likes.setImageResource(R.drawable.like_filled);
                    if (model.getLikes()!=null)
                    {
                        likes = model.getLikes();
                    }
                    likes.add(mAuth.getCurrentUser().getUid());
                    holder.tv_comment_likes.setText(String.valueOf(likes.size()));
                    model.setLikes(likes);
                    commentsModelArrayList.set(pos,model);
                    databaseReference.child(postid).child("comments").setValue(commentsModelArrayList);
                }
                else if(holder.imb_comment_likes.getDrawable().getConstantState().equals(ResourcesCompat.getDrawable(
                        holder.itemView.getResources(),R.drawable.like_filled,null).getConstantState()))
                {
                    holder.imb_comment_likes.setImageResource(R.drawable.like);
                    likes = model.getLikes();
                    likes.remove(mAuth.getCurrentUser().getUid());
                    holder.tv_comment_likes.setText(String.valueOf(likes.size()));
                    model.setLikes(likes);
                    commentsModelArrayList.set(pos,model);
                    databaseReference.child(postid).child("comments").setValue(commentsModelArrayList);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentsModelArrayList.size();
    }

    public static class viewholder extends RecyclerView.ViewHolder {
        ImageView imv_comment_uid;
        TextView tv_comment_uname,tv_comment_date,tv_comment_likes;
        ImageButton imb_comment_likes;
        EditText et_post_comment;
        public viewholder(@NonNull View itemView) {
            super(itemView);

            imv_comment_uid = itemView.findViewById(R.id.imv_comment_uid);

            tv_comment_uname = itemView.findViewById(R.id.tv_comment_uname);
            tv_comment_date = itemView.findViewById(R.id.tv_comment_date);
            tv_comment_likes = itemView.findViewById(R.id.tv_comment_likes);

            imb_comment_likes = itemView.findViewById(R.id.imb_comment_likes);
            et_post_comment = itemView.findViewById(R.id.et_post_comment);
        }
    }
}

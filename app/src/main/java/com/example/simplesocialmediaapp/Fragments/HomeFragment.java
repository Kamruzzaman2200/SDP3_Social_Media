package com.example.simplesocialmediaapp.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import com.example.simplesocialmediaapp.Adapters.NewPostAdapter;
import com.example.simplesocialmediaapp.Adapters.PostSearchAdapter;
import com.example.simplesocialmediaapp.Adapters.TopPostAdapter;
import com.example.simplesocialmediaapp.Models.PostModel;
import com.example.simplesocialmediaapp.Models.PostSearchModel;
import com.example.simplesocialmediaapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.*;
import com.google.firebase.firestore.*;

import java.util.*;

public class HomeFragment extends Fragment {

    TabLayout tl_home;
    SearchView sv_searchpost;
    RecyclerView rv_top, rv_new, rv_search;

    DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Posts");
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    ArrayList<String> postids = new ArrayList<>();
    ArrayList<PostModel> topPostsList = new ArrayList<>();
    ArrayList<PostModel> newPostsList = new ArrayList<>();

    PostSearchAdapter postSearchAdapter;
    TopPostAdapter topPostAdapter;
    NewPostAdapter newPostAdapter;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupAdapters();
        setupTabs();
        loadNewPosts();
        loadTopPosts();
        setupSearchListener();
        setupRealtimeUpdateListener();
    }

    private void initViews(View view) {
        tl_home = view.findViewById(R.id.tl_home);
        rv_top = view.findViewById(R.id.rv_top);
        rv_new = view.findViewById(R.id.rv_new);
        rv_search = view.findViewById(R.id.rv_search);
        sv_searchpost = view.findViewById(R.id.sv_searchpost);
    }

    private void setupAdapters() {
        postSearchAdapter = new PostSearchAdapter(postids);
        rv_search.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_search.setAdapter(postSearchAdapter);

        topPostAdapter = new TopPostAdapter(getContext(), topPostsList);
        rv_top.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_top.setAdapter(topPostAdapter);

        newPostAdapter = new NewPostAdapter(postids);
        rv_new.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_new.setAdapter(newPostAdapter);
    }

    private void setupTabs() {
        tl_home.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                rv_top.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                rv_new.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
                boolean showSearch = position == 2;
                rv_search.setVisibility(showSearch ? View.VISIBLE : View.GONE);
                View cardSearch = getView().findViewById(R.id.card_search);
                if (cardSearch != null) {
                    cardSearch.setVisibility(showSearch ? View.VISIBLE : View.GONE);
                }
                if (showSearch) {
                    sv_searchpost.setIconified(false);
                    sv_searchpost.requestFocus();
                    // Clear previous search results when switching to search tab
                    postids.clear();
                    postSearchAdapter.notifyDataSetChanged();
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadNewPosts() {
        postsRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                postids.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    postids.add(snap.getKey());
                }
                Collections.reverse(postids);
                newPostAdapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadTopPosts() {
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                topPostsList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    PostModel post = snap.getValue(PostModel.class);
                    if (post != null) topPostsList.add(post);
                }
                Collections.sort(topPostsList, (o1, o2) -> {
                    int l1 = o1.getLikes() != null ? o1.getLikes().size() : 0;
                    int l2 = o2.getLikes() != null ? o2.getLikes().size() : 0;
                    return Integer.compare(l2, l1);
                });
                topPostAdapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupSearchListener() {
        sv_searchpost.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPosts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 2) {
                    searchPosts(newText);
                } else if (newText.isEmpty()) {
                    postids.clear();
                    postSearchAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });

        // Set search view properties
        sv_searchpost.setIconifiedByDefault(false);
        sv_searchpost.setFocusable(true);
        sv_searchpost.setIconified(false);
        sv_searchpost.requestFocusFromTouch();
    }

    private void searchPosts(String query) {
        postids.clear();
        postSearchAdapter.notifyDataSetChanged();

        String text = query.trim().toLowerCase();
        if (!text.isEmpty()) {
            // Show loading state
            Toast.makeText(getContext(), "Searching...", Toast.LENGTH_SHORT).show();

            // Search in Realtime Database
            postsRef.orderByChild("content")
                    .startAt(text)
                    .endAt(text + "\uf8ff")
                    .limitToFirst(20) // Limit results for better performance
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    postids.add(postSnapshot.getKey());
                                }
                                if (postids.isEmpty()) {
                                    Toast.makeText(getContext(), "No posts found", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), postids.size() + " posts found", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "No posts found", Toast.LENGTH_SHORT).show();
                            }
                            postSearchAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void setupRealtimeUpdateListener() {
        postsRef.addChildEventListener(new ChildEventListener() {
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String key = snapshot.getKey();
                int index = postids.indexOf(key);
                if (index >= 0) {
                    postSearchAdapter.notifyItemChanged(index);
                }
            }

            @Override public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}

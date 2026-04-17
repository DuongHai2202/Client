package vn.duonghai.client.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.adapters.ReviewAdapter;
import vn.duonghai.client.models.Review;

public class AdminReviewsFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<Review> list;
    private ReviewAdapter adapter;
    private DatabaseReference reviewRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_reviews, container, false);

        recyclerView = view.findViewById(R.id.recyclerReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        list = new ArrayList<>();
        adapter = new ReviewAdapter(getContext(), list, true);
        recyclerView.setAdapter(adapter);

        reviewRef = FirebaseDatabase.getInstance().getReference("reviews");

        reviewRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                list.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Review r = data.getValue(Review.class);
                    list.add(r);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });

        return view;
    }
}
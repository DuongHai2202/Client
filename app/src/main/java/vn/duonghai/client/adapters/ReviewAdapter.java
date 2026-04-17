package vn.duonghai.client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.models.Review;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private Context context;
    private List<Review> list;
    private boolean isAdmin;

    public ReviewAdapter(Context context, List<Review> list, boolean isAdmin) {
        this.context = context;
        this.list = list;
        this.isAdmin = isAdmin;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RatingBar ratingBar;
        TextView txtComment;
        Button btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            txtComment = itemView.findViewById(R.id.txtComment);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Review r = list.get(position);

        holder.ratingBar.setRating(r.getRating());
        holder.txtComment.setText(r.getComment());

        if (isAdmin) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                FirebaseDatabase.getInstance()
                        .getReference("reviews")
                        .child(r.getReviewId())
                        .removeValue();
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }
}
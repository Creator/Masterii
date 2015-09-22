package kenshii.masterii;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by shane- on 11/09/2015.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private List<Map<String,String>> mDataset;
    private Context mContext;
    Typeface font;
    int recentOrOther;

    public RecyclerAdapter(Context context, int recentOrOther) {
        mDataset = new ArrayList<>();
        mContext = context;
        this.recentOrOther = recentOrOther;
        font = Typeface.createFromAsset(mContext.getAssets(), "FontAwesome.otf");
    }

    // Not use static
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView ep_title;
        public TextView ep_number;
        public TextView ep_queue;
        public TextView ep_play;
        public TextView ep_desc;
        public ImageView ep_thumb;
        public TextView ep_rating;

        public ViewHolder(View itemView) {
            super(itemView);
            ep_title = (TextView) itemView.findViewById(R.id.ep_title);
            ep_number = (TextView) itemView.findViewById(R.id.ep_number);
            ep_desc = (TextView) itemView.findViewById(R.id.ep_desc);
            ep_thumb = (ImageView) itemView.findViewById(R.id.ep_thumb);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(
                            mContext,
                            "onItemClick - " + getAdapterPosition() + " - "
                                    + ep_number.getText().toString() + " - "
                                    + mDataset.get(getAdapterPosition()).get("ep_title"), Toast.LENGTH_SHORT).show();
                }
            });
            if (recentOrOther == 1) {
                ep_rating = (TextView) itemView.findViewById(R.id.ep_number);
                return;
            }

            ep_queue = (TextView) itemView.findViewById(R.id.ep_queue);
            ep_play = (TextView) itemView.findViewById(R.id.ep_play);
            ep_queue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ep_queue.getCurrentTextColor() == 0xffadd8e6) {
                        ep_queue.setTextColor(Color.parseColor("#458ac6"));
                        Toast.makeText(mContext, "Added episode to queue.", Toast.LENGTH_SHORT).show();
                    } else {
                        ep_queue.setTextColor(Color.parseColor("#add8e6"));
                        Toast.makeText(mContext, "Removed episode from queue.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.ep_title.setText(mDataset.get(position).get("ep_title"));
        holder.ep_desc.setText(mDataset.get(position).get("ep_desc"));
        Picasso.with(mContext)
                .load(mDataset.get(position).get("ep_thumb"))
                .into(holder.ep_thumb);
        if (recentOrOther == 0) {
            holder.ep_number.setText("Episode " + mDataset.get(position).get("ep_number"));
            holder.ep_queue.setTypeface(font);
            holder.ep_play.setTypeface(font);
            return;
        }
        if (mDataset.get(position).get("ep_rating").equals("0.00"))
            holder.ep_rating.setText("Not Rated");
        else
            holder.ep_rating.setText("Rated " + mDataset.get(position).get("ep_rating") + " / 5");
    }

    public void addItemToList(Map<String,String> stringMap) {
        mDataset.add(stringMap);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
        (recentOrOther == 0) ?
                R.layout.anime_recent
                :R.layout.anime_other, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }
}
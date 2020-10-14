package com.e.rhino.sessions;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.e.rhino.R;
import com.e.rhino.sessions.SessionsFragment.OnListFragmentInteractionListener;
import com.e.rhino.sessions.content.SessionContent.SessionItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link SessionItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class SessionsRecyclerViewAdapter extends RecyclerView.Adapter<SessionsRecyclerViewAdapter.ViewHolder> {

    private final List<SessionItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    // array of session card background graphic ids for quick access
    private static int bg_graphic_ids[] = {
            R.drawable.bg_sessions_gradient_blue,
            R.drawable.bg_sessions_gradient_red,
            R.drawable.bg_sessions_gradient_green,
            R.drawable.bg_sessions_gradient_yellow,
            R.drawable.bg_sessions_gradient_purple,
            R.drawable.bg_sessions_gradient_orange,
            R.drawable.bg_sessions_gradient_gray
    };

    public SessionsRecyclerViewAdapter(List<SessionItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_sessions, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.dayLabel.setText(holder.mItem.name);
        //holder.sessionNumber.setText(position + 1);

        holder.programName.setText(holder.mItem.parent);

        String description = (holder.mItem.description.length() > 0) ? holder.mItem.description : "No Description";
        holder.programDescription.setText(description);

        // show a different bg color every day of the week
        int color = (position % bg_graphic_ids.length);
        holder.cardLayout.setBackgroundResource(bg_graphic_ids[color]);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onListFragmentInteraction(holder.mItem);
            }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final View mView;
        SessionItem mItem;

        CardView card_view;
        TextView programName;
        TextView programDescription;
        TextView dayLabel;
        RelativeLayout cardLayout;
        TextView sessionNumber;

        ViewHolder(View view) {
            super(view);
            mView = view;

            card_view = (CardView) view.findViewById(R.id.card_view);
            programName = (TextView)itemView.findViewById(R.id.program_name);
            programDescription = (TextView)itemView.findViewById(R.id.program_description);
            dayLabel = (TextView)itemView.findViewById(R.id.day_label);
            cardLayout = (RelativeLayout) view.findViewById(R.id.card_layout);
            //sessionNumber = (TextView) itemView.findViewById(R.id.textViewSessionNumber);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }
}

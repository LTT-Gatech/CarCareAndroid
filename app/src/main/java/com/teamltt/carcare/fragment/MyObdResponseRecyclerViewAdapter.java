package com.teamltt.carcare.fragment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teamltt.carcare.R;
import com.teamltt.carcare.fragment.ObdResponseFragment.OnListFragmentInteractionListener;
import com.teamltt.carcare.model.ObdContent;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ObdContent.ObdResponse} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyObdResponseRecyclerViewAdapter extends RecyclerView.Adapter<MyObdResponseRecyclerViewAdapter.ViewHolder> {

    private final List<ObdContent.ObdResponse> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyObdResponseRecyclerViewAdapter(List<ObdContent.ObdResponse> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_obdresponse, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ObdContent.ObdResponse item = mValues.get(position);
        holder.mItem = item;
        holder.mRequestView.setText(item.request);
        holder.mResponseView.setText(item.response);

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

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mRequestView;
        public final TextView mResponseView;
        public ObdContent.ObdResponse mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mRequestView = (TextView) view.findViewById(R.id.request_text);
            mResponseView = (TextView) view.findViewById(R.id.response_text);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mResponseView.getText() + "'";
        }
    }
}

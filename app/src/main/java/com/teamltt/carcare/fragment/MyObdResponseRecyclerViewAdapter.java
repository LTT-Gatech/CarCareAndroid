/*
 * Copyright 2017, Team LTT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teamltt.carcare.fragment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teamltt.carcare.R;
import com.teamltt.carcare.fragment.ResponseFragment.OnListFragmentInteractionListener;
import com.teamltt.carcare.model.Response;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Response} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyObdResponseRecyclerViewAdapter extends RecyclerView.Adapter<MyObdResponseRecyclerViewAdapter.ViewHolder> {

    private final List<Response> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyObdResponseRecyclerViewAdapter(List<Response> items, OnListFragmentInteractionListener listener) {
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
        Response item = mValues.get(position);
        holder.mItem = item;
        holder.mRequestView.setText(item.name);
        holder.mResponseView.setText(item.value);

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
        public Response mItem;

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

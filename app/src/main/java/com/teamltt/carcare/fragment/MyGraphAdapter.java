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

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.teamltt.carcare.R;
import com.teamltt.carcare.database.IObservable;
import com.teamltt.carcare.database.IObserver;
import com.teamltt.carcare.database.contract.ResponseContract;
import com.teamltt.carcare.fragment.GraphFragment.OnGraphFragmentInteractionListener;
import com.teamltt.carcare.model.Response;

import java.util.List;
import java.util.Random;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Response} and makes a call to the
 * specified {@link OnGraphFragmentInteractionListener}.
 */
public class MyGraphAdapter extends RecyclerView.Adapter<MyGraphAdapter.ViewHolder> {

    private final List<String> mPIds;
    private final OnGraphFragmentInteractionListener mListener;
    private final IObservable mObservable;

    /**
     * @param pIds     a list of parameter identifiers
     * @param listener
     */
    public MyGraphAdapter(List<String> pIds, OnGraphFragmentInteractionListener listener, IObservable observable) {
        mPIds = pIds;
        mListener = listener;
        mObservable = observable;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_graph, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        String pId = mPIds.get(position);
        holder.mPId = pId;

        mObservable.addObserver(holder);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onGraphFragmentInteraction(holder.mPId);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPIds.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements IObserver {
        public final View mView;
        public final GraphView mGraphView;
        public String mPId;

        private LineGraphSeries<DataPoint> mSeries;

        // TODO delete this when update method is complete
        private final Handler mHandler = new Handler();
        private Runnable mTimer;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mGraphView = (GraphView) view.findViewById(R.id.graph);

            mSeries = new LineGraphSeries<>(generateData());
            mGraphView.addSeries(mSeries);

            // TODO delete this when update method is complete
            mTimer = new Runnable() {
                @Override
                public void run() {
                    mSeries.resetData(generateData());
                    mHandler.postDelayed(this, 300);
                }
            };
            mHandler.postDelayed(mTimer, 300);
        }

        // TODO delete this when update method is complete
        private DataPoint[] generateData() {
            int count = 30;
            DataPoint[] values = new DataPoint[count];
            for (int i = 0; i < count; i++) {
                double x = i;
                double f = mRand.nextDouble() * 0.15 + 0.3;
                double y = Math.sin(i * f + 2) + mRand.nextDouble() * 0.3;
                DataPoint v = new DataPoint(x, y);
                values[i] = v;
            }
            return values;
        }

        // TODO delete this when update method is complete
        double mLastRandom = 2;
        Random mRand = new Random();

        private double getRandom() {
            return mLastRandom += mRand.nextDouble() * 0.5 - 0.25;
        }

        @Override
        public void update(IObservable o, Bundle args) {
            Response response = args.getParcelable(ResponseContract.ResponseEntry.COLUMN_NAME_PID + "_" + mPId);
            if (response == null) {
                // no response with this pid in this update, continue
                return;
            }
            // TODO append this response to mSeries
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mGraphView.toString() + "'";
        }
    }
}

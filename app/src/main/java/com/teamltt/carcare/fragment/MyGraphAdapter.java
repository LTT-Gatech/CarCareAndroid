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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.teamltt.carcare.R;
import com.teamltt.carcare.database.IObservable;
import com.teamltt.carcare.database.IObserver;
import com.teamltt.carcare.database.contract.ResponseContract;
import com.teamltt.carcare.fragment.GraphFragment.OnGraphFragmentInteractionListener;
import com.teamltt.carcare.model.Response;

import java.util.ArrayList;
import java.util.List;

//import static com.teamltt.carcare.activity.SettingsActivity.dynamicPreferenceTitles;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Response} and makes a call to the
 * specified {@link OnGraphFragmentInteractionListener}.
 */
public class MyGraphAdapter extends RecyclerView.Adapter<MyGraphAdapter.ViewHolder> {

    private final List<String> mNames;
    private final OnGraphFragmentInteractionListener mListener;
    private final IObservable mObservable;

    /**
     * @param listener a class which implements the {@link OnGraphFragmentInteractionListener} interface.
     */
    public MyGraphAdapter(OnGraphFragmentInteractionListener listener, IObservable observable, List<String> names) {
        mNames = new ArrayList<>(names);
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
//        String pId = mPIds.get(position);
        String name = mNames.get(position);
//        holder.mPId = pId;
        holder.mName = name;
//        holder.mGraphView.setTitle(pId);
        holder.mGraphView.setTitle(name);

        if (mObservable != null) {
            mObservable.addObserver(holder);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onGraphFragmentInteraction(holder.mName);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNames.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements IObserver {
        final View mView;
        final GraphView mGraphView;
        private int lastXValue;
        String mName;

        private LineGraphSeries<DataPoint> mSeries;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mGraphView = (GraphView) view.findViewById(R.id.graph);

            GridLabelRenderer gridLabelRenderer = mGraphView.getGridLabelRenderer();
            gridLabelRenderer.setTextSize(gridLabelRenderer.getTextSize() - 4);
            // Show a blank label so the axis numbers will have room
            gridLabelRenderer.setVerticalAxisTitle("  ");
            gridLabelRenderer.setHorizontalLabelsVisible(true);
            lastXValue = 0;

            Viewport viewport = mGraphView.getViewport();
            viewport.setXAxisBoundsManual(true);
            viewport.setMinX(0);
            viewport.setMaxX(40);
            viewport.setMaxXAxisSize(40);

            mSeries = new LineGraphSeries<>();
            mGraphView.addSeries(mSeries);
        }

        @Override
        public void update(IObservable o, Bundle args) {
            boolean reset = args.getBoolean("RESET", false);
            if (reset) {
                List<Response> responses = args.getParcelableArrayList(ResponseContract.ResponseEntry.COLUMN_NAME_NAME + "_LIST_" + mName);
                if (responses != null) {
                    mGraphView.removeSeries(mSeries);
                    mSeries = new LineGraphSeries<>();
                    mGraphView.addSeries(mSeries);
                    lastXValue = 0;
                    for (Response response : responses) {
                        addNewDataPoint(response);
                    }

                }
            } else {
                Response response = args.getParcelable(ResponseContract.ResponseEntry.COLUMN_NAME_NAME + "_" + mName);
                addNewDataPoint(response);
//                if (response != null && response.name.equals(mName)) {
//                    String value = response.value;
//                    // For some reason, trying this with dates clutters the bottom axis with long strings
////                mSeries.appendData(new DataPoint(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(response.timestamp), Double.parseDouble(value)), true, 20);
//                    mSeries.appendData(new DataPoint(lastXValue++, Double.parseDouble(value)), true, 20);
//
//                }
            }
        }

        private void addNewDataPoint(Response response) {
            if (response != null && response.name.equals(mName)) {
                String value = response.value;
                // For some reason, trying this with dates clutters the bottom axis with long strings
//                mSeries.appendData(new DataPoint(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(response.timestamp), Double.parseDouble(value)), true, 20);
                mSeries.appendData(new DataPoint(lastXValue++, Double.parseDouble(value)), true, 20);
            }
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mGraphView.toString() + "'";
        }
    }
}

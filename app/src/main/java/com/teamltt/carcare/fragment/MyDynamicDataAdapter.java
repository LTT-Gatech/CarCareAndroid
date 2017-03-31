///*
// * Copyright 2017, Team LTT
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.teamltt.carcare.fragment;
//
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import com.jjoe64.graphview.GraphView;
//import com.teamltt.carcare.R;
//
//public class MyDynamicDataAdapter extends RecyclerView.Adapter<MyDynamicDataAdapter.ViewHolder> {
//
//    // Provide a reference to the views for each data item
//    // Complex data items may need more than one view per item, and
//    // you provide access to all the views for a data item in a view holder
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        // each data item is just a string in this case
//        public GraphView gvRandomData;
//        public ViewHolder(GraphView v) {
//            super(v);
//            gvRandomData = v;
//        }
//
//
//    }
//
//    // Provide a suitable constructor (depends on the kind of dataset)
//    // Random graph doesn't need a dataset provided
////    public MyDynamicDataAdapter(String[] myDataset) {
////        mDataset = myDataset;
////    }
//
//    // Create new views (invoked by the layout manager)
//    @Override
//    public MyDynamicDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
//                                                   int viewType) {
//        // create a new view
////        GraphView v = (GraphView) LayoutInflater.from(parent.getContext())
////                .inflate(R.layout.my_text_view, parent, false);
//        // set the view's size, margins, paddings and layout parameters
//
//        ViewHolder vh = new ViewHolder(v);
//        return vh;
//    }
//
//    // Replace the contents of a view (invoked by the layout manager)
//    @Override
//    public void onBindViewHolder(ViewHolder holder, int position) {
//        // - get element from your dataset at this position
//        // - replace the contents of the view with that element
////        holder.mTextView.setText(mDataset[position]);
//
//    }
//
//    // Return the size of your dataset (invoked by the layout manager)
//    @Override
//    public int getItemCount() {
//        // I don't really know how to do this with only the one graph...
//        return 1;
//    }
//}
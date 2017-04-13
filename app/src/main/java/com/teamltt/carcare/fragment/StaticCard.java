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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.teamltt.carcare.R;
import com.teamltt.carcare.database.DbHelper;
import com.teamltt.carcare.database.IObservable;
import com.teamltt.carcare.database.IObserver;
import com.teamltt.carcare.database.contract.ResponseContract;
import com.teamltt.carcare.model.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.teamltt.carcare.activity.SettingsActivity.staticPreferenceTitles;

/**
 * This class is used to encapsulate some processing login for the static data card on the Home Activity.
 */
public class StaticCard implements IObserver {

    private Map<String, LinearLayout> shownData;

    private CardView card;
    private LinearLayout staticDataList;
    private Context context;

    public StaticCard(CardView card, Context context) {
        this.card = card;
        staticDataList = (LinearLayout) card.findViewById(R.id.static_data);
        this.context = context;
        shownData = new HashMap<>();
    }

    @Override
    public void update(IObservable o, Bundle args) {
        if (args != null && o instanceof DbHelper) {
            for (String name : shownData.keySet()) {
                Response response = args.getParcelable(ResponseContract.ResponseEntry.COLUMN_NAME_NAME + name);
                if (response != null) {
                    if (response.id != -1) {
                        // A response for this preference was found during this update
                        TextView valueText = (TextView) shownData.get(response.name).getChildAt(2);
                        valueText.setText(response.getFormattedResult());
                    } else {
                        // The response is not supported on this vehicle
                        TextView valueText = (TextView) shownData.get(response.name).getChildAt(2);
                        valueText.setText(R.string.unsupported_pid_static);
                    }
                }
            }
        }
    }


    public void displayStaticData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        staticDataList.removeAllViews();

        boolean haveStatic = false;
        for (String preferenceKey : staticPreferenceTitles) {
            if (preferences.getBoolean("static " + preferenceKey, false)) {
                LinearLayout dataView = createDataView(createTextView(preferenceKey + ":"), createTextView("-"));
                shownData.put(preferenceKey, dataView);
                staticDataList.addView(dataView);
                haveStatic = true;
            }
        }

        if (!haveStatic) {
            card.setVisibility(View.INVISIBLE);
        } else {
            card.setVisibility(View.VISIBLE);
        }
    }

        /**
         * Creates a horizontal Linear layout for static data so that it can be put in the static data Card
         *
         * @param title TextView of the content on the left side of the line
         * @param value TextView of the content on the right side of the line
         * @return A LinearLayout to be added to the static CardView's vertical LinearLayout
         */
    private LinearLayout createDataView(TextView title, TextView value) {
        // Default orientation is horizontal
        LinearLayout dataLine = new LinearLayout(context);
        // Add title/name of data
        dataLine.addView(title);

        // Add spacer to push the title to the left and the value to the right
        Space space = new Space(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        dataLine.addView(space);

        // Add value of data
        dataLine.addView(value);
        return dataLine;
    }

    /**
     * Creates a TextView out of text that needs to go in the static CardView
     *
     * @param text text of the text view
     * @return A TextView with LinearLayout LayoutParams
     */
    private TextView createTextView(String text) {
        TextView newTextView = new TextView(context);
        newTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 0f));
        newTextView.setText(text);
        return newTextView;
    }
}

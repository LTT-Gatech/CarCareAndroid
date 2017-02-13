/*
 ** Copyright 2017, Team LTT
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */

package com.teamltt.carcare.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 */
public class ObdContent {

    public static final List<ObdResponse> ITEMS = new ArrayList<>();

    public static final Map<String, ObdResponse> ITEM_MAP = new HashMap<>();

    public static void setItems(List<ObdResponse> items) {
        ITEMS.clear();
        ITEMS.addAll(items);
        for (ObdResponse item : items) {
            ITEM_MAP.put(item.id, item);
        }
    }

    public static void addItem(ObdResponse item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static ObdResponse createItemWithResponse(int position, String request, String response) {
        return new ObdResponse(String.valueOf(position), request, response);
    }

    public static class ObdResponse {
        public final String id;
        public final String request;
        public final String response;

        public ObdResponse(String id, String request, String response) {
            this.id = id;
            this.request = request;
            this.response = response;
        }

        @Override
        public String toString() {
            return request + " : " + response;
        }
    }
}

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

package com.teamltt.carcare.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 */
public class ObdContent {

    public static final List<Response> ITEMS = new ArrayList<>();

    public static final Map<Long, Response> ITEM_MAP = new HashMap<>();

    public static void setItems(List<Response> items) {
        ITEMS.clear();
        ITEM_MAP.clear();
        addItems(items);
    }

    public static void addItems(List<Response> items) {
        for (Response item : items) {
            addItem(item);
        }
    }

    public static void addItem(Response item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static Response createItemWithResponse(long id, String request, String response) {
        return new Response(id, request, response);
    }
}

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

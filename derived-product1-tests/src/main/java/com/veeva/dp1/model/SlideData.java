package com.veeva.dp1.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SlideData - POJO representing a single slide entry from sixers_tickets_testdata.json.
 *
 * Maps to:
 * {
 *   "title": "76ers vs. Boston Celtics",
 *   "expectedDurationMs": 5000
 * }
 */
public class SlideData {

    @JsonProperty("title")
    private String title;

    @JsonProperty("expectedDurations")
    private long expectedDurations;

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String getTitle() {
        return title;
    }

    public long getExpectedDurations() {
        return expectedDurations;
    }

    @Override
    public String toString() {
        return "SlideData{title='" + title + "', expectedDurations=" + expectedDurations + "}";
    }
}

package com.veeva.dp1.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SixersTicketsTestData - Root POJO for sixers_tickets_testdata.json.
 *
 * Maps to:
 * {
 *   "slides": [ { "title": "...", "expectedDurationMs": 5000 }, ... ],
 *   "expectedMinSlideCount": 2
 * }
 */
public class SixersTicketsTestData {

    @JsonProperty("slides")
    private List<SlideData> slides;

    @JsonProperty("expectedMinSlideCount")
    private int expectedMinSlideCount;

    // ─── Getters ──────────────────────────────────────────────────────────────

    public List<SlideData> getSlides() {
        return slides;
    }

    public int getExpectedMinSlideCount() {
        return expectedMinSlideCount;
    }

    /**
     * Convenience — returns just the list of expected titles in order.
     */
    public List<String> getExpectedTitles() {
        return slides.stream()
                .map(SlideData::getTitle)
                .collect(Collectors.toList());
    }
}
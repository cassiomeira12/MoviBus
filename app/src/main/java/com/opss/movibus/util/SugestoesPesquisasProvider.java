package com.opss.movibus.util;

import android.content.SearchRecentSuggestionsProvider;

public class SugestoesPesquisasProvider extends SearchRecentSuggestionsProvider {

    public final static String AUTHORITY = "com.opss.movibus.util.SugestoesPesquisasProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SugestoesPesquisasProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

}

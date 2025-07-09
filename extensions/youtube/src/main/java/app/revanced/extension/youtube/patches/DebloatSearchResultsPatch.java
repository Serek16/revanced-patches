package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;
import android.os.Bundle;

@SuppressWarnings("unused")
public class DebloatSearchResultsPatch {

    private static final String[] KEYWORDS = {"before:", "after:", ", last hour", ", today", ", this week", ", this month", ", this year"};

    private static boolean containsDateOperatorOrFilter(String searchQuery) {
        for (String keyword : KEYWORDS) {
            if (searchQuery.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Injection point.
     */
    public static String appendSearchQuery(String searchQuery) {
        if (Settings.DEBLOAT_SEARCH_RESULTS.get()) {
            // Don't apply if user already added date search operator or filter
            if (containsDateOperatorOrFilter(searchQuery)) {
                return searchQuery;
            }
            return searchQuery + " before:2099";
        }
        return searchQuery;
    }

    /**
     * Injection point.
     */
    public static String appendSearchQuerySkipVoiceSearch(String searchQuery, Bundle bundle) {
        if (bundle.getBoolean("from_voice_search")) {
            return searchQuery;
        }
        return appendSearchQuery(searchQuery);
    }

    /**
     * Injection point.
     */
    public static CharSequence trimSearchQuery(CharSequence searchQuery) {
        if (Settings.DEBLOAT_SEARCH_RESULTS.get()) {
            if (searchQuery == null) return "";
            return searchQuery.toString().replace(" before:2099", "");
        }
        return searchQuery;
    }
}

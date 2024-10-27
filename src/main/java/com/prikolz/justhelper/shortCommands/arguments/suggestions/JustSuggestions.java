package com.prikolz.justhelper.shortCommands.arguments.suggestions;

import java.util.Collection;

public class JustSuggestions implements ShortSuggestions{

    public final Collection<String> suggestions;

    public JustSuggestions(Collection<String> suggestions) {
        this.suggestions = suggestions;
    }

    public void add(String suggestion) {
        this.suggestions.add(suggestion);
    }

    @Override
    public Collection<String> get() {
        return this.suggestions;
    }
}

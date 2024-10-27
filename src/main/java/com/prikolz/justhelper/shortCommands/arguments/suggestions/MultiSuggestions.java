package com.prikolz.justhelper.shortCommands.arguments.suggestions;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class MultiSuggestions implements Iterable<String>, Iterator<String>{

    public final ArrayList<ShortSuggestions> suggestions = new ArrayList<>();
    public ArrayList<String> general;
    public int index;

    public MultiSuggestions() {}

    public void add(ShortSuggestions suggestions) {
        this.suggestions.add(suggestions);
    }

    @Override
    public @NotNull Iterator<String> iterator() {
        this.index = 0;
        this.general = new ArrayList<>();
        for(ShortSuggestions c : suggestions) {
            this.general.addAll( c.get() );
        }
        return this;
    }

    @Override
    public boolean hasNext() {
        return index < this.general.size();
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return this.general.get(index++);
    }
}

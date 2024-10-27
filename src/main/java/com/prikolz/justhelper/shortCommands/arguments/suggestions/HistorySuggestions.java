package com.prikolz.justhelper.shortCommands.arguments.suggestions;

import com.prikolz.justhelper.vars.VarHistory;

import java.util.*;

public class HistorySuggestions implements ShortSuggestions {

    public final byte type;

    public HistorySuggestions(byte type) {
        this.type = type;
    }

    @Override
    public Collection<String> get() {
        return switch (type) {
            case 1 -> VarHistory.getVarListGame();
            case 2 -> VarHistory.getVarListSave();
            case 3 -> VarHistory.getVarListLocal();
            default -> List.of();
        };
    }
}

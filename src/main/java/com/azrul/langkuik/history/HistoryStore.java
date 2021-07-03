/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.history;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author azrul
 */
public class HistoryStore {
    private Map<Object,PojoMomento> history = new HashMap<>();

    /**
     * @return the history
     */
    public Map<Object,PojoMomento> getHistory() {
        return history;
    }

    /**
     * @param history the history to set
     */
    public void setHistory(Map<Object,PojoMomento> history) {
        this.history = history;
    }
}

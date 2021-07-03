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
public class PojoMomento {
    private Map<String,TableMomento> tableMomentos = new HashMap<>();

    /**
     * @return the tableMomentos
     */
    public Map<String,TableMomento> getTableMomentos() {
        return tableMomentos;
    }

    /**
     * @param tableMomentos the tableMomentos to set
     */
    public void setTableMomentos(Map<String,TableMomento> tableMomentos) {
        this.tableMomentos = tableMomentos;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.views.main;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author azrul
 */
public class RoleContainer {
    private String name;
    private Map<String,String> activities;

    
    public RoleContainer(){
        activities = new HashMap<>();
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the activities
     */
    public Map<String,String> getActivities() {
        return activities;
    }

    /**
     * @param activities the activities to set
     */
    public void setActivities(Map<String,String> activities) {
        this.activities = activities;
    }
}

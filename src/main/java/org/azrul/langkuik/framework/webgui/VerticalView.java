/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.framework.webgui;

import com.vaadin.navigator.View;
import com.vaadin.server.Resource;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author azrulm
 */
public abstract class VerticalView extends GridLayout implements View {

    private String currentView;

    /**
     * @return the currentView
     */
    public String getCurrentView() {
        return currentView;
    }

    /**
     * @param currentView the currentView to set
     */
    public void setCurrentView(String currentView) {
        if (currentView != null) {
            this.currentView = currentView;
        }
    }
    
   
}

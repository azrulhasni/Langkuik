/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.azrul.langkuik.framework.webgui.breadcrumb;

import java.io.Serializable;

/**
 *
 * @author azrulm
 */
public class History implements Serializable {
    private String viewHandle;
    private String displayName;
    
    public History(){
        
    }
    
    public History(String viewHandle, String displayName){
        this.viewHandle = viewHandle;
        this.displayName = displayName;
    }

    /**
     * @return the viewHandle
     */
    public String getViewHandle() {
        return viewHandle;
    }

    /**
     * @param viewHandle the viewHandle to set
     */
    public void setViewHandle(String viewHandle) {
        this.viewHandle = viewHandle;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
}

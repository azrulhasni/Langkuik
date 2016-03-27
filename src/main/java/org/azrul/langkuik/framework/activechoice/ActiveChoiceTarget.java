/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.azrul.langkuik.framework.activechoice;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author azrulm
 */
public class ActiveChoiceTarget implements Serializable{
    private String sourceHierarchy;
    private List<String> sourceChoices;
    private String targetHierarchy;
    private Map<String,List<String>> targets; //source value=>{all possible target values given source value}

    /**
     * @return the sourceHierarchy
     */
    public String getSourceHierarchy() {
        return sourceHierarchy;
    }

    /**
     * @param sourceHierarchy the sourceHierarchy to set
     */
    public void setSourceHierarchy(String sourceHierarchy) {
        this.sourceHierarchy = sourceHierarchy;
    }

    /**
     * @return the sourceChoices
     */
    public List<String> getSourceChoices() {
        return sourceChoices;
    }

    /**
     * @param sourceChoices the sourceChoices to set
     */
    public void setSourceChoices(List<String> sourceChoices) {
        this.sourceChoices = sourceChoices;
    }

    /**
     * @return the targetHierarchy
     */
    public String getTargetHierarchy() {
        return targetHierarchy;
    }

    /**
     * @param targetHierarchy the targetHierarchy to set
     */
    public void setTargetHierarchy(String targetHierarchy) {
        this.targetHierarchy = targetHierarchy;
    }

    /**
     * @return the targets
     */
    public Map<String,List<String>> getTargets() {
        return targets;
    }

    /**
     * @param targets the targets to set
     */
    public void setTargets(Map<String,List<String>> targets) {
        this.targets = targets;
    }
    
}

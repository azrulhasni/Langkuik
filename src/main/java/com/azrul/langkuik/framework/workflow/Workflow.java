/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.workflow;

import com.azrul.langkuik.framework.workflow.model.Activity;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author azrul
 */
public interface Workflow<T> {
    T run(T currentEntity, boolean isError);
    String getFirstStep();
    Collection<String> getAllSteps();
    Map<String,List<Activity>> getRoleActivityMap();
    String getRootClass();
    
}

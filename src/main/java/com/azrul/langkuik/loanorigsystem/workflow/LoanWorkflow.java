/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.loanorigsystem.workflow;

import com.azrul.langkuik.framework.standard.Status;
import com.azrul.langkuik.framework.workflow.Workflow;
import com.azrul.langkuik.framework.workflow.model.Activity;
import com.azrul.langkuik.framework.workflow.model.Bizprocess;
import com.azrul.langkuik.framework.workflow.model.Branch;
import com.azrul.langkuik.loanorigsystem.model.Application;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.server.VaadinSession;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

/**
 *
 * @author azrul
 */
@Service
public class LoanWorkflow implements Workflow<Application> {

    ExpressionParser expressionParser = new SpelExpressionParser();
    ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("groovy");

    @Autowired
    private Bizprocess bizProcess;

    @Autowired
    private Map<String, Activity> activities;
    
    public String getRootClass(){
        return bizProcess.getRoot();
    }
    
    public Map<String,List<Activity>> getRoleActivityMap(){
       Map<String,List<Activity>> roles = new HashMap<>();
       for (String canBeStartedBy:bizProcess.getStartEvent().getCanBeStartedBy()){
           roles.put(canBeStartedBy,new ArrayList<Activity>());
       }
       
       for (Map.Entry<String, Activity> e : getActivities().entrySet()) {
            roles.putIfAbsent(e.getValue().getHandledBy(),new ArrayList<Activity>());
            roles.get(e.getValue().getHandledBy()).add(e.getValue());
       }
       return roles;
    }

    public Map<String, Boolean> runTransition(Application root) {
        Map<String, Boolean> nextSteps = new HashMap<>();
        Container container = new Container(root);
        Optional<String> worklist = root.getWorklist();
        if (worklist.isEmpty()) {//just being created
            String nextId = getBizProcess().getStartEvent().getNext();
            nextSteps.put(nextId, Boolean.TRUE);
        } else {
            for (Map.Entry<String, Activity> e : getActivities().entrySet()) {
                if (worklist.get().equals(e.getValue().getId())) {
                    if ("human".equals(e.getValue().getType())
                            || "service".equals(e.getValue().getType())) { //only current activity ids in wait states
                        if ("END".equals(e.getValue().getNext())){
                            nextSteps.put(e.getValue().getNext(), Boolean.TRUE);
                        }
                        else if ("xor".equals(getActivities().get(e.getValue().getNext()).getType())) {  // sand next activity is xor
                            //see which condition triggers and follow that branch
                            boolean conditionTriggered = false;
                            for (Branch branch : getActivities().get(e.getValue().getNext()).getBranches()) {
                                
                                Expression expression = expressionParser.parseExpression(branch.getCondition());
                                EvaluationContext context = new StandardEvaluationContext(container);
                                
                                Boolean result = (Boolean) expression.getValue(context);
                                if (result == true) {
                                    conditionTriggered = true;
                                    nextSteps.put(branch.getNext(), Boolean.TRUE);
                                    break;
                                }
                            }
                            if (conditionTriggered == false) {//if no condition triggered, the default branch is executed
                                nextSteps.put(getActivities().get(e.getValue().getNext()).getDefaultBranch(), Boolean.TRUE);
                            }
                        } else {
                            nextSteps.put(e.getValue().getNext(), Boolean.TRUE); //if the next step is just another wait state, make it active
                        }
                    }
                }
            }
        }
        
        
        return nextSteps;
    }

    public Application run(Application application, boolean isError) {
        application.setStatus(Status.IN_PROGRESS);
        Map<String, Boolean> nextSteps = runTransition(application);
        
        for (Map.Entry<String,Boolean> e:nextSteps.entrySet()){
            
            Activity activity = getActivities().get(e.getKey());
            application.setWorklist(e.getKey());
            if (e.getKey().equals("END")){
                return  application;
            }
            
            if (activity.getType().equals("service")){
                try {
                    scriptEngine.put("root", application);
                    if (VaadinSession.getCurrent()!=null){
                        scriptEngine.put("session",VaadinSession.getCurrent().getSession());
                    }
                    scriptEngine.eval(activity.getScript());
                    //if (!activity.getNext().equals("END")){
                        application =  run(application,isError);
                    //}
                    return application;
                } catch (ScriptException ex) {
                    Logger.getLogger(LoanWorkflow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                return  application;
            }
        }
        return application;
//        boolean nextStepIsWaitState =false;
//        while (nextStepIsWaitState==false){
//            
//            inner:for (Map.Entry<String,Boolean> e:nextSteps.entrySet()){
//               Activity activity = getActivities().get(e.getKey());
//               if ("service".equals(activity.getType())){
//                   try {
//                       //run service
//                       scriptEngine.put("root", application);
//                       if (VaadinSession.getCurrent()!=null){
//                        scriptEngine.put("session",VaadinSession.getCurrent().getSession());
//                       }
//                       scriptEngine.eval(activity.getScript());
//                       nextSteps.remove(e.getKey());
//                       nextSteps.put(key, isError)
//                       break inner;
//                   } catch (ScriptException ex) {
//                       Logger.getLogger(LoanWorkflow.class.getName()).log(Level.SEVERE, null, ex);
//                   }
//               }else{
//                   //save human wait state
//                   application.setWorklist(activity.getId());
//                   nextStepIsWaitState=true;
//               }
//            }
//        }
//        return application;
    }

//    private Activity runUntilWaitState(String currentActivityId){
//        
//        Activity currentActivity = activities.get(currentActivityId);
//        if ("human".equals(currentActivity.getType())){
//            Activity nextActivity = activities.get(currentActivity.getNext());
//            while (!"human".equals(nextActivity.getType())){
//               if ("xor".equals(nextActivity.getType())){
//                   List<Branch> branches = nextActivity.getBranches();
//                   for (Branch branch:branches){
//                       Expression expression = expressionParser.parseExpression(branch.getCondition());
//                       Boolean result = (Boolean) expression.getValue();
//                       if (result==true){
//                           
//                       }
//                   }
//               } 
//                //nextActivity = activities.get(nextActivity.getNext());
//            }
//            return nextActivity;
//        }
//    }
//    private Optional<Activity> findNextActivity(String currentActivityId){
//        for (Activity currentActivity:bizProcess.getWorkflow()){
//            if (currentActivityId.equals(currentActivity.getId())){
//                for (Activity nextActivity:bizProcess.getWorkflow()){
//                    if (currentActivity.getNext().equals(nextActivity.getId())){
//                        return Optional.ofNullable(nextActivity);
//                    }
//                }
//            }
//        }
//        return Optional.empty();
//    }
    @Override
    public String getFirstStep() {
        return getAllSteps().iterator().next(); //get first
    }

    @Override
    public Collection<String> getAllSteps() {
        return Arrays.asList("Lead", "Ready for credit bureau", "Ready for approval");
    }

    /**
     * @return the bizProcess
     */
    public Bizprocess getBizProcess() {
        return bizProcess;
    }

    /**
     * @param bizProcess the bizProcess to set
     */
    public void setBizProcess(Bizprocess bizProcess) {
        this.bizProcess = bizProcess;
    }

    /**
     * @return the activities
     */
    public Map<String, Activity> getActivities() {
        return activities;
    }

    /**
     * @param activities the activities to set
     */
    public void setActivities(Map<String, Activity> activities) {
        this.activities = activities;
    }

}

class Container{
    public Object root;
    
    public Container(Object root){
        this.root=root;
    }
}


package com.azrul.langkuik.framework.workflow.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "version",
    "root",
    "variables",
    "startEvent",
    "workflow",
    "endEvent"
})
@Generated("jsonschema2pojo")
public class Bizprocess implements Serializable
{
    @JsonProperty("name")
    private String name;
    @JsonProperty("version")
    private String version;
    @JsonProperty("root")
    private String root;
    
    @JsonProperty("variables")
    @Valid
    private Variables variables;
    
    @JsonProperty("startEvent")
    @Valid
    private StartEvent startEvent;
    
    @JsonProperty("workflow")
    @Valid
    private List<Activity> workflow = new ArrayList<Activity>();
   
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 6947793155253756046L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Bizprocess() {
    }

    /**
     * 
     * @param variables
     * @param endEvent
     * @param workflow
     * @param startEvent
     * @param root
     */
    public Bizprocess(String name, String version,String root, Variables variables, StartEvent startEvent, List<Activity> workflow) {
        super();
        this.name = name;
        this.version = version;
        this.root = root;
        this.variables = variables;
        this.startEvent = startEvent;
        this.workflow = workflow;
    }
    
     @JsonProperty("name")
    public String getName() {
        return name;
    }
    
    public Bizprocess withName(String name) {
        this.name = name;
        return this;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }
    
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }
    
    public Bizprocess withVersion(String version) {
        this.version = version;
        return this;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("root")
    public String getRoot() {
        return root;
    }

    @JsonProperty("root")
    public void setRoot(String root) {
        this.root = root;
    }

    public Bizprocess withRoot(String root) {
        this.root = root;
        return this;
    }

    @JsonProperty("variables")
    public Variables getVariables() {
        return variables;
    }

    @JsonProperty("variables")
    public void setVariables(Variables variables) {
        this.variables = variables;
    }

    public Bizprocess withVariables(Variables variables) {
        this.variables = variables;
        return this;
    }

    @JsonProperty("startEvent")
    public StartEvent getStartEvent() {
        return startEvent;
    }

    @JsonProperty("startEvent")
    public void setStartEvent(StartEvent startEvent) {
        this.startEvent = startEvent;
    }

    public Bizprocess withStartEvent(StartEvent startEvent) {
        this.startEvent = startEvent;
        return this;
    }

    @JsonProperty("workflow")
    public List<Activity> getWorkflow() {
        return workflow;
    }

    @JsonProperty("workflow")
    public void setWorkflow(List<Activity> workflow) {
        this.workflow = workflow;
    }

    public Bizprocess withWorkflow(List<Activity> workflow) {
        this.workflow = workflow;
        return this;
    }

  

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Bizprocess withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}

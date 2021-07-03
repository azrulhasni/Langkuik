
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
    "id",
    "type",
    "next",
    "handledBy",
    "branches",
    "defaultBranch"
})
@Generated("jsonschema2pojo")
public class Activity implements Serializable
{

    @JsonProperty("id")
    private String id;
    @JsonProperty("type")
    private String type;
    @JsonProperty("next")
    private String next;
    @JsonProperty("handledBy")
    private String handledBy;
    @JsonProperty("description")
    private String description;
    @JsonProperty("script")
    private String script;
    
    @JsonProperty("branches")
    @Valid
    private List<Branch> branches = new ArrayList<Branch>();
    @JsonProperty("defaultBranch")
    private String defaultBranch;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 8695824382266942798L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Activity() {
    }

    /**
     * 
     * @param next
     * @param handledBy
     * @param defaultBranch
     * @param id
     * @param type
     * @param branches
     */
    public Activity(String id, String type, String next, String handledBy, List<Branch> branches, String defaultBranch) {
        super();
        this.id = id;
        this.type = type;
        this.next = next;
        this.handledBy = handledBy;
        this.branches = branches;
        this.defaultBranch = defaultBranch;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public Activity withId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    public Activity withType(String type) {
        this.type = type;
        return this;
    }

    @JsonProperty("next")
    public String getNext() {
        return next;
    }

    @JsonProperty("next")
    public void setNext(String next) {
        this.next = next;
    }

    public Activity withNext(String next) {
        this.next = next;
        return this;
    }

    @JsonProperty("handledBy")
    public String getHandledBy() {
        return handledBy;
    }

    @JsonProperty("handledBy")
    public void setHandledBy(String handledBy) {
        this.handledBy = handledBy;
    }

    public Activity withHandledBy(String handledBy) {
        this.handledBy = handledBy;
        return this;
    }
    
     @JsonProperty("script")
    public String getScript() {
        return script;
    }

    @JsonProperty("script")
    public void setScript(String script) {
        this.script = script;
    }

    public Activity withScript(String script) {
        this.script = script;
        return this;
    }

    @JsonProperty("branches")
    public List<Branch> getBranches() {
        return branches;
    }

    @JsonProperty("branches")
    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }

    public Activity withBranches(List<Branch> branches) {
        this.branches = branches;
        return this;
    }

    @JsonProperty("defaultBranch")
    public String getDefaultBranch() {
        return defaultBranch;
    }

    @JsonProperty("defaultBranch")
    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public Activity withDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
        return this;
    }
    
     @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public Activity withDescription(String description) {
        this.description = description;
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

    public Activity withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}

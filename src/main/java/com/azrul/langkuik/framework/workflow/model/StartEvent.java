
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
    "canBeStartedBy",
    "next"
})
@Generated("jsonschema2pojo")
public class StartEvent implements Serializable
{

    @JsonProperty("id")
    private String id;
    @JsonProperty("canBeStartedBy")
    @Valid
    private List<String> canBeStartedBy = new ArrayList<String>();
    @JsonProperty("next")
    private String next;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -6874812109085012007L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public StartEvent() {
    }

    /**
     * 
     * @param next
     * @param id
     * @param canBeStartedBy
     */
    public StartEvent(String id, List<String> canBeStartedBy, String next) {
        super();
        this.id = id;
        this.canBeStartedBy = canBeStartedBy;
        this.next = next;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public StartEvent withId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("canBeStartedBy")
    public List<String> getCanBeStartedBy() {
        return canBeStartedBy;
    }

    @JsonProperty("canBeStartedBy")
    public void setCanBeStartedBy(List<String> canBeStartedBy) {
        this.canBeStartedBy = canBeStartedBy;
    }

    public StartEvent withCanBeStartedBy(List<String> canBeStartedBy) {
        this.canBeStartedBy = canBeStartedBy;
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

    public StartEvent withNext(String next) {
        this.next = next;
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

    public StartEvent withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}


package com.azrul.langkuik.framework.workflow.model;

import java.io.Serializable;
import java.util.HashMap;
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
    "a",
    "b"
})
@Generated("jsonschema2pojo")
public class Variables implements Serializable
{

    @JsonProperty("a")
    private String a;
    @JsonProperty("b")
    private String b;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -4644030447494437678L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Variables() {
    }

    /**
     * 
     * @param a
     * @param b
     */
    public Variables(String a, String b) {
        super();
        this.a = a;
        this.b = b;
    }

    @JsonProperty("a")
    public String getA() {
        return a;
    }

    @JsonProperty("a")
    public void setA(String a) {
        this.a = a;
    }

    public Variables withA(String a) {
        this.a = a;
        return this;
    }

    @JsonProperty("b")
    public String getB() {
        return b;
    }

    @JsonProperty("b")
    public void setB(String b) {
        this.b = b;
    }

    public Variables withB(String b) {
        this.b = b;
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

    public Variables withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}


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
    "name",
    "version",
    "bizprocess"
})
@Generated("jsonschema2pojo")
public class Example implements Serializable
{

    @JsonProperty("name")
    private String name;
    @JsonProperty("version")
    private String version;
    @JsonProperty("bizprocess")
    @Valid
    private Bizprocess bizprocess;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -2135893527863610363L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Example() {
    }

    /**
     * 
     * @param bizprocess
     * @param name
     * @param version
     */
    public Example(String name, String version, Bizprocess bizprocess) {
        super();
        this.name = name;
        this.version = version;
        this.bizprocess = bizprocess;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public Example withName(String name) {
        this.name = name;
        return this;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    public Example withVersion(String version) {
        this.version = version;
        return this;
    }

    @JsonProperty("bizprocess")
    public Bizprocess getBizprocess() {
        return bizprocess;
    }

    @JsonProperty("bizprocess")
    public void setBizprocess(Bizprocess bizprocess) {
        this.bizprocess = bizprocess;
    }

    public Example withBizprocess(Bizprocess bizprocess) {
        this.bizprocess = bizprocess;
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

    public Example withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}

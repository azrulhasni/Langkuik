/*
 * Copyright 2014 azrulm.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.azrul.langkuik.framework.audit;

import java.io.Serializable;
import java.lang.reflect.Field;
import org.azrul.langkuik.annotations.WebField;


public class AuditedField<T> implements Serializable{
    private WebField field;
    private T value;

    /**
     * @return the field
     */
    public WebField getWebField() {
        return field;
    }

    /**
     * @param field the field to set
     */
    public void setWebField(WebField field) {
        this.field = field;
    }

    /**
     * @return the value
     */
    public T getValue() {
        return value;
    }

    /**
     * @param value the oldValue to set
     */
    public void setValue(T value) {
        this.value = value;
    }

    
}

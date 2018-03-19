/*
 * Copyright 2017 Azrul.
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
package org.azrul.langkuik.framework.webgui;

import java.lang.reflect.Method;
import org.azrul.langkuik.annotations.FieldUserMap;
import org.azrul.langkuik.annotations.DerivedField;

/**
 *
 * @author Azrul
 */
public class DerivedFieldContainer implements DataElementContainer {

    private DerivedField derivedField;
    private Method pojoMethod;

    public DerivedFieldContainer(DerivedField myField, Method pojoMethod) {
        this.derivedField = myField;
        this.pojoMethod = pojoMethod;
    }

    public DerivedField getDerivedField() {
        return derivedField;
    }

    public void setDerivedField(DerivedField derivedField) {
        this.derivedField = derivedField;
    }

    public Method getPojoMethod() {
        return pojoMethod;
    }

    public void setPojoMethod(Method pojoMethod) {
        this.pojoMethod = pojoMethod;
    }
    
     public FieldUserMap[] getFieldUserMaps(){
         return derivedField.userMap();
     }
}

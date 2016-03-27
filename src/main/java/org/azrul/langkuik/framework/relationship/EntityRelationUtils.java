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
package org.azrul.langkuik.framework.relationship;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author azrulm
 */
public class EntityRelationUtils {

    public static List<Field> getParentChildFields(Class parentClass, Class currentClass) {
        List<Field> fields = new ArrayList<>();
        if (parentClass == null) {
            return fields;
        }
        if (currentClass == null) {
            return fields;
        }
        try {
            for (Field parentField : parentClass.getDeclaredFields()) {
                parentField.setAccessible(true);

                if (Collection.class.isAssignableFrom(parentField.getType())) {
                    Class fieldClass = (Class) ((ParameterizedType) parentField.getGenericType()).getActualTypeArguments()[0];
                    if (fieldClass.equals(currentClass)) {
                        fields.add(parentField);
                    }
                } else {
                    if (parentField.getType().equals(currentClass)) {
                        fields.add(parentField);
                    }
                }
            }
            return fields;
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(DefaultRelationManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fields;

    }
}

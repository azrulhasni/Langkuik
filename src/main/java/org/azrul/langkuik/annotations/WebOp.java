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
package org.azrul.langkuik.annotations;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.azrul.langkuik.framework.EventType;
import org.azrul.langkuik.security.role.FieldRight;
import org.azrul.langkuik.security.role.OpRight;

/**
 *
 * @author Azrul
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WebOp {
    String name() default "";

    OpUserMap[] userMap() default {@OpUserMap(role = "*", right=OpRight.NONE)};
    boolean tenantId() default false;
    EventType onEvent() default EventType.None;
}

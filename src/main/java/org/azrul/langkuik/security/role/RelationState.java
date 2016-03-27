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

package org.azrul.langkuik.security.role;

/**
 *
 * @author azrulm
 */
public enum RelationState {
    EDIT_RELATION, //root to root relation
    CREATE_ADD_DELETE_CHILDREN, //root to non-root, non-root to non-root
    DELETE_CHILDREN, //root to non-root, non-root to non-root
    EDIT_CHILDREN,  //root to non-root, non-root to non-root
    READ_ONLY,
    INVISIBLE
            
}

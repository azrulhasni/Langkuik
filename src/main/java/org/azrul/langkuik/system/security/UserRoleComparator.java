/*
 * Copyright 2018 Azrul.
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
package org.azrul.langkuik.system.security;

import java.util.Comparator;



/**
 *
 * @author Azrul
 */
public class UserRoleComparator implements Comparator<UserRole>{

    @Override
    public int compare(UserRole o1,UserRole o2) {
        if (o1==null){
            return -1;
        }
        if (o2==null){
            return -1;
        }
        if (o1.getRoleName()==null){
            return -1;
        }
        if (o2.getRoleName()==null){
            return -1;
        }
       if (o1.getRoleName().equals(o2.getRoleName())){
           return 0;
       }else{
           return -1;
       }
    }

   
}

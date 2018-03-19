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
package org.azrul.langkuik.framework;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import org.azrul.langkuik.system.security.User;

/**
 *
 * @author Azrul
 */
@MappedSuperclass
public abstract class Work<T> {
    @Column(name = "STATUS")
    protected T status;
    
    @Column(name = "PREVIOUS_STATUS")
    protected T previousStatus;
    
    @Column(name = "CURRENT_OWNER")
    protected User currentOwner;
    
    @Column(name= "PREVIOUS_MESSAGE")
    protected String previousMessage;
    
    

    protected T getStatus() {
        return status;
    }

    protected void setStatus(T status) {
        this.status = status;
    }

    protected User getCurrentOwner() {
        return currentOwner;
    }

    protected void setCurrentOwner(User currentOwner) {
        this.currentOwner = currentOwner;
    }

    public T getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(T previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getPreviousMessage() {
        return previousMessage;
    }

    public void setPreviousMessage(String previousMessage) {
        this.previousMessage = previousMessage;
    }
    
    public class Result{

        public Result() {
        }
        protected T nextWorklist;
        protected String message;

        public T getNextWorklist() {
            return nextWorklist;
        }

        public void setNextWorklist(T nextWorklist) {
            this.nextWorklist = nextWorklist;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
        
        public void run(){
            
        }
    }
    
}



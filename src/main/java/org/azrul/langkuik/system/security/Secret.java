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
package org.azrul.langkuik.system.security;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import org.azrul.langkuik.annotations.AutoIncrementConfig;
import org.azrul.langkuik.annotations.FieldUserMap;
import org.azrul.langkuik.annotations.WebEntity;
import org.azrul.langkuik.annotations.WebField;
import org.azrul.langkuik.framework.customtype.secret.SecretCustomType;
import org.azrul.langkuik.security.role.FieldRight;
import org.azrul.langkuik.system.model.identity.LangkuikIdGenerator;
import org.hibernate.search.annotations.Indexed;

/**
 *
 * @author Azrul
 */

@Entity
@Table(schema = "SYSTEM")
@XmlRootElement
@Indexed
@WebEntity(name="secret")
public class Secret implements SecretCustomType{

    @Id
    @Column(name = "id")
    @WebField(name="id", rank=0, displayInTable=true, userMap = {
        @FieldUserMap(right = FieldRight.VIEW)
    },
    autoIncrement = @AutoIncrementConfig(generator = LangkuikIdGenerator.class))
    private Long id;
    
    @Column(name = "hashedPassword")
    private String hashedPassword=null;
    
    @Column(name = "salt")
    private String salt=null;

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

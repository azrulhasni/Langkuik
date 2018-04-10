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
package org.azrul.langkuik.system.model.worklist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.azrul.langkuik.annotations.AutoIncrementConfig;
import org.azrul.langkuik.annotations.DerivedField;
import org.azrul.langkuik.annotations.EntityUserMap;
import org.azrul.langkuik.annotations.FieldUserMap;
import org.azrul.langkuik.annotations.WebEntity;
import org.azrul.langkuik.annotations.WebField;
import org.azrul.langkuik.security.role.EntityRight;
import org.azrul.langkuik.security.role.FieldRight;
import org.azrul.langkuik.system.model.identity.LangkuikIdGenerator;
import org.azrul.langkuik.system.model.role.Role;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

@Entity
@Table(name = "Worklist",schema = "SYSTEM")
@XmlRootElement
@Indexed
@WebEntity(name="worklist", isRoot = true, userMap={
    @EntityUserMap(role="ROLE_ADMIN",right=EntityRight.UPDATE)
})
public class UserWorklist implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4656759219348212715L;

    @Id
    @WebField(name="worklist id", rank=0, displayInTable=true, userMap = {
        @FieldUserMap(right = FieldRight.VIEW)
    },
    autoIncrement = @AutoIncrementConfig(generator = LangkuikIdGenerator.class))
    protected Long id;

    @Column(unique =true )
    @Field(analyze = Analyze.YES)
    @Size(max = 30)
    @Audited
    @WebField(name="worklist name",rank=1, displayInTable=true, required=true,userMap = {
                @FieldUserMap(role = "*", right = FieldRight.VIEW)
            })
    protected String worklistName;

    @DerivedField(name="Roles", rank=2)
    public String getRoles(){
        StringBuilder sb = new StringBuilder();
//        if (rolesCollection.size()==1){
//            return ((Role)rolesCollection.iterator().next()).getRoleName();
//        }
       List<Role> roles = new ArrayList<>();
       roles.addAll(rolesCollection);
       Collections.sort(roles);
        for (Role role:roles){
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append(role.getRoleName());
        }
        return sb.toString();
    }
     
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            schema = "SYSTEM",
            name = "WORKLIST_ROLES",
            joinColumns = @JoinColumn(name = "WORKLIST_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "ROLE_ID", referencedColumnName = "ID"))
    @WebField(name = "roles", rank = 3,
            userMap = {
                @FieldUserMap(role = "*", right = FieldRight.VIEW),
                @FieldUserMap(role = "ROLE_ADMIN", right = FieldRight.UPDATE),
            }
    )
    protected Set<Role> rolesCollection;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWorklistName() {
        return worklistName;
    }

    public void setWorklistName(String worklistName) {
        this.worklistName = worklistName;
    }

    public Set<Role> getRolesCollection() {
        return rolesCollection;
    }

    public void setRolesCollection(Set<Role> rolesCollection) {
        this.rolesCollection = rolesCollection;
    }
    
    

}

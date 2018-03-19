/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.system.security;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;
import org.azrul.langkuik.annotations.AutoIncrementConfig;
import org.azrul.langkuik.annotations.EntityUserMap;
import org.azrul.langkuik.annotations.FieldUserMap;
import org.azrul.langkuik.annotations.WebEntity;
import org.azrul.langkuik.annotations.WebField;
import org.azrul.langkuik.security.role.EntityRight;
import org.azrul.langkuik.security.role.FieldRight;
import org.azrul.langkuik.system.choices.SystemData;
import org.azrul.langkuik.system.model.identity.LangkuikIdGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

@Entity
@Table(schema = "SYSTEM",
        uniqueConstraints=
        @UniqueConstraint(columnNames={"rolescollection_id", "rolename"})
        )
@Indexed
@WebEntity(name="user role", userMap={
    @EntityUserMap(role="*",right=EntityRight.VIEW),
    @EntityUserMap(role="ROLE_ADMIN",right=EntityRight.CREATE_UPDATE_DELETE)    
})
public class UserRole implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 8432414340180447723L;

  @Id
  @WebField(name="id", rank=0, displayInTable=true, userMap = {
        @FieldUserMap(right = FieldRight.VIEW)
  },autoIncrement = @AutoIncrementConfig(generator = LangkuikIdGenerator.class))
  private Long id;
  
  @Column
    @Field(analyze = Analyze.YES)
    @Size(max = 30)
    @Audited
    @WebField(name="roleName",rank=1, displayInTable=true, required=true, systemChoice = SystemData.ROLE_WITHOUT_STAR)
  private String roleName;


  // ... getters and setters ...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

   

}
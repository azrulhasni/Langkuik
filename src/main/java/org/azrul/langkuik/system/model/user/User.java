/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.system.model.user;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.azrul.langkuik.annotations.AutoIncrementConfig;
import org.azrul.langkuik.annotations.Choice;
import org.azrul.langkuik.annotations.EntityUserMap;
import org.azrul.langkuik.annotations.FieldUserMap;
import org.azrul.langkuik.annotations.WebEntity;
import org.azrul.langkuik.annotations.WebField;
import org.azrul.langkuik.security.role.EntityRight;
import org.azrul.langkuik.security.role.FieldRight;
import org.azrul.langkuik.system.model.identity.LangkuikIdGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.azrul.langkuik.annotations.DerivedField;
import org.azrul.langkuik.system.model.role.Role;

@Entity
@Table(name = "_User", schema = "SYSTEM")
@XmlRootElement
@Indexed
@WebEntity(name = "user", isRoot = true, userMap = {
    @EntityUserMap(role = "*", right = EntityRight.UPDATE),
    @EntityUserMap(role = "ROLE_ADMIN", right = EntityRight.CREATE_UPDATE)
})
public class User implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4656759219348212715L;

    @Id
    @WebField(name = "user id", rank = 0, displayInTable = true, userMap = {
        @FieldUserMap(right = FieldRight.VIEW)
    },
            autoIncrement = @AutoIncrementConfig(generator = LangkuikIdGenerator.class))
    private Long id;

    @Column(unique = true)
    @Field(analyze = Analyze.YES)
    @Size(max = 30)
    @Audited
    @WebField(name = "username", rank = 1, displayInTable = true, required = true)
    private String username;

    @Column(unique = true)
    @Field(analyze = Analyze.YES)
    @Size(max = 30)
    @Audited
    @WebField(name = "email", rank = 2, displayInTable = true, required = true)
    private String email;

    @Column
    @Field(analyze = Analyze.YES)
    @Size(max = 30)
    @Audited
    @WebField(name = "status", rank = 3, displayInTable = true, required = true,
            choices = {
                @Choice(display = "Active", textValue = "3"),
                @Choice(display = "Inactive", textValue = "5")
            }
    )
    private String status;

    //@OneToMany(fetch = FetchType.EAGER)
    //@JoinColumn()
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            schema = "SYSTEM",
            name = "USER_ROLES",
            joinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "ROLE_ID", referencedColumnName = "ID"))
    @WebField(name = "roles", rank = 5,
            userMap = {
                @FieldUserMap(role = "*", right = FieldRight.VIEW),
                @FieldUserMap(role = "ROLE_ADMIN", right = FieldRight.UPDATE),}
    )
    private Set<Role> rolesCollection;

    @WebField(name = "password", rank = 6)
    @OneToOne(/*mappedBy = "applicationId", */fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn()
    private Secret password;

    // ... getters and setters ...
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Secret getPassword() {
        return password;
    }

    public void setPassword(Secret password) {
        this.password = password;
    }

    public Set<Role> getRolesCollection() {
        return rolesCollection;
    }

    public void setRolesCollection(Set<Role> rolesCollection) {
        this.rolesCollection = rolesCollection;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @DerivedField(name = "Roles", rank = 4)
    public String getRoles() {
        StringBuilder sb = new StringBuilder();

        if (rolesCollection != null) {
            if (rolesCollection.size() == 1) {
                return ((Role) rolesCollection.iterator().next()).getRoleName();
            }
            for (Role role : rolesCollection) {
                sb.append(role.getRoleName() + ",");
            }
        }
        return sb.toString();
    }

}

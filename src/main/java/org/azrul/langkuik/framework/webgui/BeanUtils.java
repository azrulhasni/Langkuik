/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.framework.webgui;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.azrul.langkuik.annotations.EntityUserMap;
import org.azrul.langkuik.annotations.FieldUserMap;
import org.azrul.langkuik.annotations.WebEntity;
import org.azrul.langkuik.annotations.WebField;
import org.azrul.langkuik.security.role.FieldState;
import org.azrul.langkuik.security.role.EntityOperation;
import org.azrul.langkuik.security.role.FieldOperation;
import org.azrul.langkuik.security.role.RelationState;
import org.azrul.langkuik.dao.EntityUtils;
import org.azrul.langkuik.security.role.UserSecurityUtils;

/**
 *
 * @author azrulm
 */
public class BeanUtils implements Serializable {

    public BeanUtils() {

    }

    public <T> Map<Integer, FieldContainer> getOrderedFieldsByRank(Class<T> classOfBean) throws SecurityException {
        //loop collecting annotation info
        Map<Integer, FieldContainer> group = new TreeMap<>();
        for (Field field : classOfBean.getDeclaredFields()) {
            if (field.isAnnotationPresent(WebField.class)) {
                WebField webField = field.getAnnotation(WebField.class);

                FieldContainer fieldContainer = new FieldContainer(webField, field);
                group.put(webField.rank(), fieldContainer);

            }
        }
        return group;
    }

    public <T> Map<String, Map<Integer, FieldContainer>> createGroupsFromBean(Class<T> classOfBean) throws SecurityException {
        //loop collecting annotation info
        Map<String, Map<Integer, FieldContainer>> groups = new HashMap<>();
        for (Field field : classOfBean.getDeclaredFields()) {
            if (field.isAnnotationPresent(WebField.class)) {
                WebField webField = field.getAnnotation(WebField.class);
                String group = webField.group();
                if (!groups.containsKey(group)) {
                    groups.put(group, new TreeMap<Integer, FieldContainer>());
                }
                FieldContainer fieldContainer = new FieldContainer(webField, field);
                groups.get(group).put(webField.rank(), fieldContainer);

            }
        }
        return groups;
    }

    public <T> boolean isEditable(Class<T> classOfBean/*, Set<String> currentUserRoles*/) {
        EntityOperation entityRight = UserSecurityUtils.getEntityRight(classOfBean/*, currentUserRoles*/);
        
        Map<String, Map<Integer, FieldContainer>> groups = createGroupsFromBean(classOfBean);
        for (Map<Integer, FieldContainer> group : groups.values()) {
            for (FieldContainer fieldContainer : group.values()) {
                FieldState effectiveFieldState = calculateEffectiveFieldState(fieldContainer.getPojoField()/*, currentUserRoles*/, entityRight);
                if (effectiveFieldState.equals(FieldState.EDITABLE)) { //if any field is editable, then you can save the object
                    return true;
                }
            }
        }
        return false;
    }

    public <T> boolean isViewable(Class<T> classOfBean/*, Set<String> currentUserRoles*/) {
        EntityOperation entityRight = UserSecurityUtils.getEntityRight(classOfBean/*, currentUserRoles*/);
        Map<String, Map<Integer, FieldContainer>> groups = createGroupsFromBean(classOfBean);
        for (Map<Integer, FieldContainer> group : groups.values()) {
            for (FieldContainer fieldContainer : group.values()) {
                FieldState effectiveFieldState = calculateEffectiveFieldState(fieldContainer.getPojoField()/*, currentUserRoles*/, entityRight);
                if (effectiveFieldState.equals(FieldState.READ_ONLY)) { //if any field is editable, then you can save the object
                    return true;
                }
            }
        }
        return false;
    }

   

    public <T> boolean isCreatable(Class<T> classOfBean/*, Set<String> currentUserRoles*/) {
        EntityOperation entityRight = UserSecurityUtils.getEntityRight(classOfBean/*, currentUserRoles*/);
        return entityRight.equals(EntityOperation.CREATE_UPDATE);
    }

    public RelationState calculateEffectiveRelationState(Field pojoField/*, Set<String> currentUserRoles*/, EntityOperation currentEntityRight, EntityOperation targetEntityRight) {
        FieldState currentFieldState = calculateEffectiveFieldState(pojoField/*, currentUserRoles*/, currentEntityRight);
        RelationState effectiveRelationState = null;
        boolean targetIsRoot = isFieldTypeRoot(pojoField);

        effectiveRelationState = RelationState.READ_ONLY;
        if (FieldState.INVISIBLE.equals(currentFieldState)) {
            effectiveRelationState = RelationState.INVISIBLE;
        } else if (EntityOperation.RESTRICTED.equals(targetEntityRight)) {
            effectiveRelationState = RelationState.INVISIBLE;
        } else if (FieldState.READ_ONLY.equals(currentFieldState)) {
            effectiveRelationState = RelationState.READ_ONLY;
        } else if (FieldState.EDITABLE.equals(currentFieldState)) { //current right=CREATE,UPDATE,DELETE
            if (targetIsRoot == true) {
                effectiveRelationState = RelationState.EDIT_RELATION;
            } else {
                if (EntityOperation.CREATE_UPDATE.equals(targetEntityRight)) {
                    effectiveRelationState = RelationState.CREATE_ADD_DELETE_CHILDREN;
                } else if (EntityOperation.DELETE.equals(targetEntityRight)) {
                    effectiveRelationState = RelationState.DELETE_CHILDREN;
                } else if (EntityOperation.UPDATE.equals(targetEntityRight)) {
                    effectiveRelationState = RelationState.EDIT_CHILDREN;
                } else {
                    effectiveRelationState = RelationState.READ_ONLY;
                }
            }
        }
        return effectiveRelationState;

    }

    public boolean isFieldTypeRoot(Field pojoField) {
        if (pojoField.getType().equals(Collection.class)) {
            Class targetClass = (Class) ((ParameterizedType) pojoField.getGenericType()).getActualTypeArguments()[0];
            return EntityUtils.isClassRoot(targetClass);
        } else {
            return EntityUtils.isClassRoot(pojoField.getType());
        }
    }

    

    public FieldState calculateEffectiveFieldState(Field pojoField/*, Set<String> currentUserRoles*/, EntityOperation currentEntityRight) {
        WebField webField = pojoField.getAnnotation(WebField.class);
        return calculateEffectiveFieldState(webField/*, currentUserRoles*/, currentEntityRight);
    }

    public FieldState calculateEffectiveFieldState(WebField webField/*, Set<String> currentUserRoles*/, EntityOperation currentEntityRight) {
        if (webField == null) {
            return null;
        } else {
            FieldUserMap[] fieldUserMaps = webField.userMap();

            FieldOperation fieldRight = null;
            FieldState effectiveFieldState = null;
            for (FieldUserMap e : fieldUserMaps) {
                if (UserSecurityUtils.hasRole(e.role()) || ("*").equals(e.role())) {
                    fieldRight = e.right();
                    break;
                }
            }
            if (fieldRight == null) {
                fieldRight = FieldOperation.INHERITED; //if a role is not specified, then the right falls to INHERITED by default
            }

            if (fieldRight.equals(FieldOperation.INHERITED)) { //inherit entity level rights
                if (EntityOperation.RESTRICTED.equals(currentEntityRight)) {
                    effectiveFieldState = FieldState.INVISIBLE;
                } else if (EntityOperation.UPDATE.equals(currentEntityRight)
                        || EntityOperation.CREATE_UPDATE.equals(currentEntityRight)) {
                    effectiveFieldState = FieldState.EDITABLE;
                } else if (EntityOperation.VIEW.equals(currentEntityRight)) {
                    effectiveFieldState = FieldState.READ_ONLY;
                }
            } else {
                if (FieldOperation.RESTRICTED.equals(fieldRight)) {
                    effectiveFieldState = FieldState.INVISIBLE;
                } else if (FieldOperation.UPDATE.equals(fieldRight)) {
                    effectiveFieldState = FieldState.EDITABLE;
                } else if (FieldOperation.VIEW.equals(fieldRight)) {
                    effectiveFieldState = FieldState.READ_ONLY;
                }
            }
            return effectiveFieldState;
        }
    }
    
    public String getName(Class<?> classOfBean){
        WebEntity webEntity = classOfBean.getAnnotation(WebEntity.class);
        String beanName = null;
        if (webEntity!= null){
            beanName = webEntity.name();
            if (beanName==null){
                beanName=classOfBean.getSimpleName().toLowerCase();
            }
        }
        return beanName;
    }

//    public ComponentState calculateEffectiveComponentState(FieldUserMap[] fieldUserMaps, Set<String> currentUserRoles, EntityRight entityRight) {
//        FieldRight fieldRight = null;
//        ComponentState effectiveFieldState = null;
//        for (FieldUserMap e : fieldUserMaps) {
//            if (currentUserRoles.contains(e.role()) || ("*").equals(e.role())) {
//                fieldRight = e.right();
//                break;
//            }
//        }
//        
//        
//        if (fieldRight.equals(FieldRight.INHERITED)) { //inherit entity level rights
//            if (EntityRight.RESTRICTED.equals(entityRight)) {
//                effectiveFieldState = ComponentState.INVISIBLE;
//            } else if (EntityRight.UPDATE.equals(entityRight)
//                    || EntityRight.CREATE.equals(entityRight)) {
//                effectiveFieldState = ComponentState.EDITABLE;
//            } else if (EntityRight.VIEW.equals(entityRight)) {
//                effectiveFieldState = ComponentState.READ_ONLY;
//            }
//        } else {
//            if (FieldRight.RESTRICTED.equals(fieldRight)) {
//                effectiveFieldState = ComponentState.INVISIBLE;
//            } else if (FieldRight.UPDATE.equals(fieldRight)) {
//                effectiveFieldState = ComponentState.EDITABLE;
//            } else if (FieldRight.VIEW.equals(fieldRight)) {
//                effectiveFieldState = ComponentState.READ_ONLY;
//            }
//        }
//        return effectiveFieldState;
//    }
}

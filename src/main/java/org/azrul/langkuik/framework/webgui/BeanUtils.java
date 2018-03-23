/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.framework.webgui;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.azrul.langkuik.annotations.EntityUserMap;
import org.azrul.langkuik.annotations.FieldUserMap;
import org.azrul.langkuik.annotations.WebEntity;
import org.azrul.langkuik.annotations.WebField;
import org.azrul.langkuik.security.role.FieldState;
import org.azrul.langkuik.security.role.EntityRight;
import org.azrul.langkuik.security.role.FieldRight;
import org.azrul.langkuik.security.role.RelationState;
import org.azrul.langkuik.dao.EntityUtils;
import org.azrul.langkuik.security.role.UserSecurityUtils;
import org.azrul.langkuik.annotations.DerivedField;
import org.azrul.langkuik.annotations.OpUserMap;
import org.azrul.langkuik.annotations.WebOp;
import org.azrul.langkuik.security.role.OpState;

/**
 *
 * @author azrulm
 */
public class BeanUtils implements Serializable {

    public BeanUtils() {

    }

    public <T> Map<Integer, DataElementContainer> getOrderedFieldsByRank(Class<T> classOfBean) throws SecurityException {
        //loop collecting annotation info
        Map<Integer, DataElementContainer> group = new TreeMap<>();
        for (Field field : classOfBean.getDeclaredFields()) {
            if (field.isAnnotationPresent(WebField.class)) {
                WebField webField = field.getAnnotation(WebField.class);

                FieldContainer fieldContainer = new FieldContainer(webField, field);
                group.put(webField.rank(), fieldContainer);

            }
        }
        return group;
    }

    public <T> Map<String, Map<Integer, DataElementContainer>> createGroupsFromBean(Class<T> classOfBean) throws SecurityException {
        //loop collecting annotation info
        Map<String, Map<Integer, DataElementContainer>> groups = new HashMap<>();
        for (Field field : classOfBean.getDeclaredFields()) {
            if (field.isAnnotationPresent(WebField.class)) {
                WebField webField = field.getAnnotation(WebField.class);
                String group = webField.group();
                if (!groups.containsKey(group)) {
                    groups.put(group, new TreeMap<Integer, DataElementContainer>());
                }
                FieldContainer fieldContainer = new FieldContainer(webField, field);
                groups.get(group).put(webField.rank(), fieldContainer);

            }
        }
        for (Method method : classOfBean.getDeclaredMethods()) {
            if (method.isAnnotationPresent(DerivedField.class)) {
                DerivedField derField = method.getAnnotation(DerivedField.class);
                String group = derField.group();
                if (!groups.containsKey(group)) {
                    groups.put(group, new TreeMap<Integer, DataElementContainer>());
                }
                DataElementContainer methodContainer = new DerivedFieldContainer(derField, method);
                groups.get(group).put(derField.rank(), methodContainer);

            }
        }
        return groups;
    }
    
//    public <T> List<MethodContainer> getOps(Class<T> classOfBean){
//        List<MethodContainer> ops = new ArrayList<>();
//    
//         for (Method method : classOfBean.getDeclaredMethods()) {
//            if(method.isAnnotationPresent(WebOp.class)){
//                WebOp op = method.getAnnotation(WebOp.class);
//                
//                MethodContainer methodContainer = new MethodContainer(op, method);
//                ops.add(methodContainer);
//                
//            }
//         }
//         return ops;
//         
//    }

    public <T> boolean isEditable(Class<T> classOfBean/*, Set<String> currentUserRoles*/) {
        EntityRight entityRight = UserSecurityUtils.getEntityRight(classOfBean/*, currentUserRoles*/);

        Map<String, Map<Integer, DataElementContainer>> groups = createGroupsFromBean(classOfBean);
        for (Map<Integer, DataElementContainer> group : groups.values()) {

            for (DataElementContainer container : group.values()) {
                if (container instanceof FieldContainer) {
                    FieldContainer fieldContainer = (FieldContainer) container;
                    FieldState effectiveFieldState = calculateEffectiveFieldState(fieldContainer.getWebField().userMap()/*, currentUserRoles*/, entityRight);
                    if ((FieldState.EDITABLE).equals(effectiveFieldState)) { //if any field is editable, then you can save the object
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public <T> boolean isViewable(Class<T> classOfBean/*, Set<String> currentUserRoles*/) {
        EntityRight entityRight = UserSecurityUtils.getEntityRight(classOfBean/*, currentUserRoles*/);
        Map<String, Map<Integer, DataElementContainer>> groups = createGroupsFromBean(classOfBean);
        for (Map<Integer, DataElementContainer> group : groups.values()) {
            for (DataElementContainer container : group.values()) {
                if (container instanceof FieldContainer) {
                    FieldContainer fieldContainer = (FieldContainer) container;
                    FieldState effectiveFieldState = calculateEffectiveFieldState(fieldContainer.getWebField().userMap()/*, currentUserRoles*/, entityRight);
                    if ((FieldState.READ_ONLY).equals(effectiveFieldState)) { //if any field is editable, then you can save the object
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public <T> boolean isCreatable(Class<T> classOfBean/*, Set<String> currentUserRoles*/) {
        EntityRight entityRight = UserSecurityUtils.getEntityRight(classOfBean/*, currentUserRoles*/);
        return (EntityRight.CREATE_UPDATE).equals(entityRight) || (EntityRight.CREATE_UPDATE_DELETE).equals(entityRight);
    }

    public RelationState calculateEffectiveRelationState(Field pojoField, FieldUserMap[] fieldUserMaps, EntityRight currentEntityRight, EntityRight targetEntityRight) {
        
        FieldState currentFieldState = calculateEffectiveFieldState(fieldUserMaps, currentEntityRight);
        RelationState effectiveRelationState = null;
        boolean targetIsRoot = isFieldTypeRoot(pojoField);

        effectiveRelationState = RelationState.READ_ONLY;
        if (FieldState.INVISIBLE.equals(currentFieldState)) {
            effectiveRelationState = RelationState.INVISIBLE;
        } else if (EntityRight.RESTRICTED.equals(targetEntityRight)) {
            effectiveRelationState = RelationState.INVISIBLE;
        } else if (FieldState.READ_ONLY.equals(currentFieldState)) {
            effectiveRelationState = RelationState.READ_ONLY;
        } else if (FieldState.EDITABLE.equals(currentFieldState)) { //current right=CREATE,UPDATE,DELETE
            if (targetIsRoot == true) {
                effectiveRelationState = RelationState.EDIT_RELATION;
            } else if (EntityRight.CREATE_UPDATE_DELETE.equals(targetEntityRight)) {
                effectiveRelationState = RelationState.CREATE_ADD_DELETE_CHILDREN;
            } else if (EntityRight.CREATE_UPDATE.equals(targetEntityRight)) {
                effectiveRelationState = RelationState.CREATE_ADD_CHILDREN;
            } else if (EntityRight.DELETE.equals(targetEntityRight)) {
                effectiveRelationState = RelationState.DELETE_CHILDREN;
            } else if (EntityRight.UPDATE.equals(targetEntityRight)) {
                effectiveRelationState = RelationState.EDIT_CHILDREN;
            } else {
                effectiveRelationState = RelationState.READ_ONLY;
            }
        }
        return effectiveRelationState;

    }
    
    public OpState calculateEffectiveOpState(Method method,OpUserMap[] opUserMaps){
        for (OpUserMap oum:opUserMaps){
                if (UserSecurityUtils.hasRole(oum.role()) || ("*").equals(oum.role())) {
                    return OpState.EXECUTABLE;
                }
        }
        return OpState.RESTRTICTED;
    }

    public boolean isFieldTypeRoot(Field pojoField) {
        if (Collection.class.isAssignableFrom(pojoField.getType())) {
            Class targetClass = (Class) ((ParameterizedType) pojoField.getGenericType()).getActualTypeArguments()[0];
            return EntityUtils.isClassRoot(targetClass);
        } else {
            return EntityUtils.isClassRoot(pojoField.getType());
        }
    }

//    public FieldState calculateEffectiveFieldState(Field pojoField/*, Set<String> currentUserRoles*/, EntityRight currentEntityRight) {
//        WebField webField = pojoField.getAnnotation(WebField.class);
//        return calculateEffectiveFieldState(webField/*, currentUserRoles*/, currentEntityRight);
//    }

    public FieldState calculateEffectiveFieldState(FieldUserMap[] fieldUserMaps/*, Set<String> currentUserRoles*/, EntityRight currentEntityRight) {
        

            FieldRight fieldRight = null;
            FieldState effectiveFieldState = null;
            //Prioritize exact role
            for (FieldUserMap e : fieldUserMaps) {
                if (UserSecurityUtils.hasRole(e.role())) {
                    fieldRight = e.right();
                    break;
                }
            }
            //if no exact role, see if * is applicable
            if (fieldRight==null){
                for (FieldUserMap e : fieldUserMaps) {
                    if (("*").equals(e.role())) {
                        fieldRight = e.right();
                        break;
                    }
                }
            }
            
            if (fieldRight == null) {
                fieldRight = FieldRight.INHERITED; //if a role is not specified, then the right falls to INHERITED by default
            }

            if (fieldRight.equals(FieldRight.INHERITED)) { //inherit entity level rights
                if (EntityRight.RESTRICTED.equals(currentEntityRight)) {
                    effectiveFieldState = FieldState.INVISIBLE;
                } else if (EntityRight.UPDATE.equals(currentEntityRight)
                        || EntityRight.CREATE_UPDATE.equals(currentEntityRight)
                        || EntityRight.CREATE_UPDATE_DELETE.equals(currentEntityRight)) {
                    effectiveFieldState = FieldState.EDITABLE;
                } else if (EntityRight.VIEW.equals(currentEntityRight)) {
                    effectiveFieldState = FieldState.READ_ONLY;
                }
            } else if (FieldRight.RESTRICTED.equals(fieldRight)) {
                effectiveFieldState = FieldState.INVISIBLE;
            } else if (FieldRight.UPDATE.equals(fieldRight)) {
                effectiveFieldState = FieldState.EDITABLE;
            } else if (FieldRight.VIEW.equals(fieldRight)) {
                effectiveFieldState = FieldState.READ_ONLY;
            }
            return effectiveFieldState;
        
    }

    public String getName(Class<?> classOfBean) {
        WebEntity webEntity = classOfBean.getAnnotation(WebEntity.class);
        String beanName = null;
        if (webEntity != null) {
            beanName = webEntity.name();
            if (beanName == null) {
                beanName = classOfBean.getSimpleName().toLowerCase();
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.framework.activechoice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.azrul.langkuik.annotations.ActiveChoiceHierarchy;

/**
 *
 * @author azrulm
 */
public class ActiveChoiceUtils implements Serializable {

    public static List<String> getChildrenByParent(Class<? extends ActiveChoiceEnum> enumTree, String parent) {
        List<String> children = new ArrayList<>();
        if (enumTree.isEnum() == false) {
            return children;
        }
        if (parent == null) {
            for (ActiveChoiceEnum ace : enumTree.getEnumConstants()) {
                if (ace.getParent() == null) {
                    children.add(ace.toString());
                }
            }
        } else {
            for (ActiveChoiceEnum ace : enumTree.getEnumConstants()) {
                if (ace.getParent().toString().equals(parent)) {
                    children.add(ace.toString());
                }
            }
        }
        return children;
    }

    public static List<String> getElementByHierarchy(Class<? extends ActiveChoiceEnum> enumTree, String hierarchy) {
        List<String> children = new ArrayList<>();
        if (enumTree.isEnum() == false) {
            return children;
        }
        String[] hierarchies = enumTree.getAnnotation(ActiveChoiceHierarchy.class).value();
        int pos = Arrays.binarySearch(hierarchies, hierarchy);
        for (ActiveChoiceEnum choice : enumTree.getEnumConstants()) {
            ActiveChoiceEnum c = choice;
            int i = 0;
            for (i = 0; i <= pos; i++) {
                if (c.getParent() != null) {
                    c = (ActiveChoiceEnum) c.getParent();
                } else {
                    break;
                }
            }
            if (pos == (i)) {
                children.add(choice.toString());
            }
        }
        return children;
    }

    public static Map<String, List<String>> getChildrenAndParentByHierarchy(Class<? extends ActiveChoiceEnum> enumTree, String hierarchy) {
        Map<String, List<String>> result = new HashMap<>();
        if (enumTree.isEnum() == false) {
            return result;
        }
        String[] hierarchies = enumTree.getAnnotation(ActiveChoiceHierarchy.class).value();
        int pos = Arrays.binarySearch(hierarchies, hierarchy);
        for (ActiveChoiceEnum choice : enumTree.getEnumConstants()) {
            ActiveChoiceEnum c = choice;
            int i = 0;
            for (i = 0; i <= pos; i++) {
                if (c.getParent() != null) {
                    c = (ActiveChoiceEnum) c.getParent();
                } else {
                    break;
                }
            }
            if (pos == (i)) {
                if (choice.getParent() == null) {

                } else {
                    if (result.get(choice.getParent().toString()) == null) {
                        result.put(choice.getParent().toString(), new ArrayList<String>());
                    }
                    result.get(choice.getParent().toString()).add(choice.toString());
                }
            }
        }
        return result;
    }

    public static ActiveChoiceTarget build(Class<? extends ActiveChoiceEnum> enumTree, String parent) {
        ActiveChoiceTarget activeChoice = null;
        String[] hierarchies = enumTree.getAnnotation(ActiveChoiceHierarchy.class).value();
        if (hierarchies.length == 1) {

        } else {
            String parentHierarchy = null;
            String childHierarchy = null;
            for (int i = 0; i < hierarchies.length - 1; i++) {
                if (hierarchies[i].equals(parent)) {
                    parentHierarchy = hierarchies[i];
                    childHierarchy = hierarchies[i + 1];
                }
            }
            if (parentHierarchy != null && childHierarchy != null) {
                activeChoice = new ActiveChoiceTarget();
                List<String> parentValues = getElementByHierarchy(enumTree, parentHierarchy);
                Map<String, List<String>> parentChildrenValues = getChildrenAndParentByHierarchy(enumTree, childHierarchy);

                activeChoice.setSourceHierarchy(parentHierarchy);
                activeChoice.setTargetHierarchy(childHierarchy);
                activeChoice.setSourceChoices(parentValues);
                activeChoice.setTargets(parentChildrenValues);
            }
        }
        return activeChoice;
    }

}

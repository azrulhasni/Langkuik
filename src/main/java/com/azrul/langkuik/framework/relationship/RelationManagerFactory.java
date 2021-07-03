/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.azrul.langkuik.framework.relationship;

/**
 *
 * @author azrulm
 */
public interface RelationManagerFactory {
    public <P,C> RelationManager<P,C> create(Class<P> parentClass, Class<C> currentClass);
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.azrul.langkuik.dao;

import java.util.Collection;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author azrulm
 * @param <P>
 * @param <C>
 */
public interface DAOQuery<P,C> {
    Collection doQuery(EntityManagerFactory emf, String orderBy, boolean asc, int startIndex, int offset);
    Long count(EntityManagerFactory emf);
}

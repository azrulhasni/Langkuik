/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.dao;

/**
 *
 * @author azrul
 */
public interface Rule1Input<C> {
    Boolean runPredicate(C childBean);
}

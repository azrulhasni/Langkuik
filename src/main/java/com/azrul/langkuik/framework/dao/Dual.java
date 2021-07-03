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
public class Dual<F,S> {
    private F first; 
    private S second;
    
    public F getFirst(){
        return first;
    }
    public S getSecond(){
        return second;
    }
    public Dual(F first,S second){
        this.first=first;
        this.second=second;
    }
}

/*
 * To change this template, choose Tools | Templates
 * buddhika.ari@gmail.com
 */
package com.divudi.data;

/**
 *
 * @author Buddhika
 */
public enum DepartmentType {
    Pharmacy,
    Lab,
    Store,
    Theatre,
    Kitchen,
    Opd,
    Inventry,
    Inward;
    
    public String getLabel(){
        return this.toString();
    }
}

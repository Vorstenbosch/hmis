/*
 * To change this template, choose Tools | Templates
 * buddhika.ari@gmail.com
 */
package com.divudi.entity.pharmacy;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Inheritance;

/**
 *
 * @author Buddhika
 */
@Entity
@Inheritance
public class PurchasePrice extends Price implements Serializable {
    private static final long serialVersionUID = 1L;

}

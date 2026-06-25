/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 *
 * @author tranp
 */
@Entity
@Table(name = "sapo_order_items")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SapoOrderItems.findAll", query = "SELECT s FROM SapoOrderItems s"),
    @NamedQuery(name = "SapoOrderItems.findById", query = "SELECT s FROM SapoOrderItems s WHERE s.id = :id"),
    @NamedQuery(name = "SapoOrderItems.findBySapoProductId", query = "SELECT s FROM SapoOrderItems s WHERE s.sapoProductId = :sapoProductId"),
    @NamedQuery(name = "SapoOrderItems.findBySapoVariantId", query = "SELECT s FROM SapoOrderItems s WHERE s.sapoVariantId = :sapoVariantId"),
    @NamedQuery(name = "SapoOrderItems.findBySku", query = "SELECT s FROM SapoOrderItems s WHERE s.sku = :sku"),
    @NamedQuery(name = "SapoOrderItems.findByQuantity", query = "SELECT s FROM SapoOrderItems s WHERE s.quantity = :quantity")})
public class SapoOrderItems implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 36)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "sapo_product_id")
    private String sapoProductId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "sapo_variant_id")
    private String sapoVariantId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "sku")
    private String sku;
    @Basic(optional = false)
    @NotNull
    @Column(name = "quantity")
    private int quantity;
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private SapoOrders orderId;

    public SapoOrderItems() {
    }

    public SapoOrderItems(String id) {
        this.id = id;
    }

    public SapoOrderItems(String id, String sapoProductId, String sapoVariantId, String sku, int quantity) {
        this.id = id;
        this.sapoProductId = sapoProductId;
        this.sapoVariantId = sapoVariantId;
        this.sku = sku;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSapoProductId() {
        return sapoProductId;
    }

    public void setSapoProductId(String sapoProductId) {
        this.sapoProductId = sapoProductId;
    }

    public String getSapoVariantId() {
        return sapoVariantId;
    }

    public void setSapoVariantId(String sapoVariantId) {
        this.sapoVariantId = sapoVariantId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public SapoOrders getOrderId() {
        return orderId;
    }

    public void setOrderId(SapoOrders orderId) {
        this.orderId = orderId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SapoOrderItems)) {
            return false;
        }
        SapoOrderItems other = (SapoOrderItems) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.SapoOrderItems[ id=" + id + " ]";
    }
    
}

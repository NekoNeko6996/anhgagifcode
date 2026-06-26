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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author tranp
 */
@Entity
@Table(name = "kiotviet_order_items")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "KiotvietOrderItems.findAll", query = "SELECT k FROM KiotvietOrderItems k"),
    @NamedQuery(name = "KiotvietOrderItems.findById", query = "SELECT k FROM KiotvietOrderItems k WHERE k.id = :id"),
    @NamedQuery(name = "KiotvietOrderItems.findByKvProductId", query = "SELECT k FROM KiotvietOrderItems k WHERE k.kvProductId = :kvProductId"),
    @NamedQuery(name = "KiotvietOrderItems.findByQuantity", query = "SELECT k FROM KiotvietOrderItems k WHERE k.quantity = :quantity"),
    @NamedQuery(name = "KiotvietOrderItems.findByLastSyncedAt", query = "SELECT k FROM KiotvietOrderItems k WHERE k.lastSyncedAt = :lastSyncedAt")})
public class KiotvietOrderItems implements Serializable {

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
    @Column(name = "kv_product_id")
    private String kvProductId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "quantity")
    private int quantity;
    @Column(name = "last_synced_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastSyncedAt;
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private KiotvietOrders orderId;

    public KiotvietOrderItems() {
    }

    public KiotvietOrderItems(String id) {
        this.id = id;
    }

    public KiotvietOrderItems(String id, String kvProductId, int quantity) {
        this.id = id;
        this.kvProductId = kvProductId;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKvProductId() {
        return kvProductId;
    }

    public void setKvProductId(String kvProductId) {
        this.kvProductId = kvProductId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Date lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public KiotvietOrders getOrderId() {
        return orderId;
    }

    public void setOrderId(KiotvietOrders orderId) {
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
        if (!(object instanceof KiotvietOrderItems)) {
            return false;
        }
        KiotvietOrderItems other = (KiotvietOrderItems) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.KiotvietOrderItems[ id=" + id + " ]";
    }
    
}

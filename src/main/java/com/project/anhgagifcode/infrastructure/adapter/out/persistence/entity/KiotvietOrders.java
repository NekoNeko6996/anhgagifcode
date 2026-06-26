/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author tranp
 */
@Entity
@Table(name = "kiotviet_orders")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "KiotvietOrders.findAll", query = "SELECT k FROM KiotvietOrders k"),
    @NamedQuery(name = "KiotvietOrders.findById", query = "SELECT k FROM KiotvietOrders k WHERE k.id = :id"),
    @NamedQuery(name = "KiotvietOrders.findByOrderCode", query = "SELECT k FROM KiotvietOrders k WHERE k.orderCode = :orderCode"),
    @NamedQuery(name = "KiotvietOrders.findByDeliveryStatus", query = "SELECT k FROM KiotvietOrders k WHERE k.deliveryStatus = :deliveryStatus"),
    @NamedQuery(name = "KiotvietOrders.findByLastSyncedAt", query = "SELECT k FROM KiotvietOrders k WHERE k.lastSyncedAt = :lastSyncedAt"),
    @NamedQuery(name = "KiotvietOrders.findByCreatedAt", query = "SELECT k FROM KiotvietOrders k WHERE k.createdAt = :createdAt"),
    @NamedQuery(name = "KiotvietOrders.findByUpdatedAt", query = "SELECT k FROM KiotvietOrders k WHERE k.updatedAt = :updatedAt")})
public class KiotvietOrders implements Serializable {

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
    @Column(name = "order_code")
    private String orderCode;
    @Size(max = 50)
    @Column(name = "delivery_status")
    private String deliveryStatus;
    @Basic(optional = false)
    @NotNull
    @Column(name = "last_synced_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastSyncedAt;
    @Basic(optional = false)
    @NotNull
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "orderId")
    private Collection<Eggs> eggsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "orderId")
    private Collection<KiotvietOrderItems> kiotvietOrderItemsCollection;
    @JoinColumn(name = "customer_code", referencedColumnName = "customer_code")
    @ManyToOne(optional = false)
    private Customers customerCode;

    public KiotvietOrders() {
    }

    public KiotvietOrders(String id) {
        this.id = id;
    }

    public KiotvietOrders(String id, String orderCode, Date lastSyncedAt, Date createdAt) {
        this.id = id;
        this.orderCode = orderCode;
        this.lastSyncedAt = lastSyncedAt;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public Date getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Date lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @XmlTransient
    public Collection<Eggs> getEggsCollection() {
        return eggsCollection;
    }

    public void setEggsCollection(Collection<Eggs> eggsCollection) {
        this.eggsCollection = eggsCollection;
    }

    @XmlTransient
    public Collection<KiotvietOrderItems> getKiotvietOrderItemsCollection() {
        return kiotvietOrderItemsCollection;
    }

    public void setKiotvietOrderItemsCollection(Collection<KiotvietOrderItems> kiotvietOrderItemsCollection) {
        this.kiotvietOrderItemsCollection = kiotvietOrderItemsCollection;
    }

    public Customers getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(Customers customerCode) {
        this.customerCode = customerCode;
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
        if (!(object instanceof KiotvietOrders)) {
            return false;
        }
        KiotvietOrders other = (KiotvietOrders) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.KiotvietOrders[ id=" + id + " ]";
    }
    
}

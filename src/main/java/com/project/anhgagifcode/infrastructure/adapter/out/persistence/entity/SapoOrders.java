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
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author tranp
 */
@Entity
@Table(name = "sapo_orders")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SapoOrders.findAll", query = "SELECT s FROM SapoOrders s"),
    @NamedQuery(name = "SapoOrders.findById", query = "SELECT s FROM SapoOrders s WHERE s.id = :id"),
    @NamedQuery(name = "SapoOrders.findByOrderCode", query = "SELECT s FROM SapoOrders s WHERE s.orderCode = :orderCode"),
    @NamedQuery(name = "SapoOrders.findBySourceName", query = "SELECT s FROM SapoOrders s WHERE s.sourceName = :sourceName"),
    @NamedQuery(name = "SapoOrders.findByTotalPrice", query = "SELECT s FROM SapoOrders s WHERE s.totalPrice = :totalPrice"),
    @NamedQuery(name = "SapoOrders.findByFinancialStatus", query = "SELECT s FROM SapoOrders s WHERE s.financialStatus = :financialStatus"),
    @NamedQuery(name = "SapoOrders.findByFulfillmentStatus", query = "SELECT s FROM SapoOrders s WHERE s.fulfillmentStatus = :fulfillmentStatus"),
    @NamedQuery(name = "SapoOrders.findByStatus", query = "SELECT s FROM SapoOrders s WHERE s.status = :status"),
    @NamedQuery(name = "SapoOrders.findByCreatedAt", query = "SELECT s FROM SapoOrders s WHERE s.createdAt = :createdAt"),
    @NamedQuery(name = "SapoOrders.findByUpdatedAt", query = "SELECT s FROM SapoOrders s WHERE s.updatedAt = :updatedAt")})
public class SapoOrders implements Serializable {

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
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "source_name")
    private String sourceName;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "total_price")
    private BigDecimal totalPrice;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "financial_status")
    private String financialStatus;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "fulfillment_status")
    private String fulfillmentStatus;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "status")
    private String status;
    @Basic(optional = false)
    @NotNull
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Basic(optional = false)
    @NotNull
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "orderId")
    private Collection<Eggs> eggsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "orderId")
    private Collection<SapoOrderItems> sapoOrderItemsCollection;

    public SapoOrders() {
    }

    public SapoOrders(String id) {
        this.id = id;
    }

    public SapoOrders(String id, String orderCode, String sourceName, BigDecimal totalPrice, String financialStatus, String fulfillmentStatus, String status, Date createdAt, Date updatedAt) {
        this.id = id;
        this.orderCode = orderCode;
        this.sourceName = sourceName;
        this.totalPrice = totalPrice;
        this.financialStatus = financialStatus;
        this.fulfillmentStatus = fulfillmentStatus;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getFinancialStatus() {
        return financialStatus;
    }

    public void setFinancialStatus(String financialStatus) {
        this.financialStatus = financialStatus;
    }

    public String getFulfillmentStatus() {
        return fulfillmentStatus;
    }

    public void setFulfillmentStatus(String fulfillmentStatus) {
        this.fulfillmentStatus = fulfillmentStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
    public Collection<SapoOrderItems> getSapoOrderItemsCollection() {
        return sapoOrderItemsCollection;
    }

    public void setSapoOrderItemsCollection(Collection<SapoOrderItems> sapoOrderItemsCollection) {
        this.sapoOrderItemsCollection = sapoOrderItemsCollection;
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
        if (!(object instanceof SapoOrders)) {
            return false;
        }
        SapoOrders other = (SapoOrders) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.SapoOrders[ id=" + id + " ]";
    }
    
}

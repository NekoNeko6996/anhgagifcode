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
import jakarta.persistence.OneToOne;
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
@Table(name = "eggs")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Eggs.findAll", query = "SELECT e FROM Eggs e"),
    @NamedQuery(name = "Eggs.findById", query = "SELECT e FROM Eggs e WHERE e.id = :id"),
    @NamedQuery(name = "Eggs.findByEggType", query = "SELECT e FROM Eggs e WHERE e.eggType = :eggType"),
    @NamedQuery(name = "Eggs.findByStatus", query = "SELECT e FROM Eggs e WHERE e.status = :status"),
    @NamedQuery(name = "Eggs.findByHatchAt", query = "SELECT e FROM Eggs e WHERE e.hatchAt = :hatchAt"),
    @NamedQuery(name = "Eggs.findByCreatedAt", query = "SELECT e FROM Eggs e WHERE e.createdAt = :createdAt"),
    @NamedQuery(name = "Eggs.findByUpdatedAt", query = "SELECT e FROM Eggs e WHERE e.updatedAt = :updatedAt")})
public class Eggs implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 36)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "egg_type")
    private int eggType;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "status")
    private String status;
    @Basic(optional = false)
    @NotNull
    @Column(name = "hatch_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date hatchAt;
    @Basic(optional = false)
    @NotNull
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    @OneToOne
    private GiftAccounts accountId;
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private SapoOrders orderId;
    @JoinColumn(name = "gift_pool_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private GiftPools giftPoolId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "eggId")
    private Collection<EggOpeningLogs> eggOpeningLogsCollection;

    public Eggs() {
    }

    public Eggs(String id) {
        this.id = id;
    }

    public Eggs(String id, int eggType, String status, Date hatchAt, Date createdAt) {
        this.id = id;
        this.eggType = eggType;
        this.status = status;
        this.hatchAt = hatchAt;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getEggType() {
        return eggType;
    }

    public void setEggType(int eggType) {
        this.eggType = eggType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getHatchAt() {
        return hatchAt;
    }

    public void setHatchAt(Date hatchAt) {
        this.hatchAt = hatchAt;
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

    public GiftAccounts getAccountId() {
        return accountId;
    }

    public void setAccountId(GiftAccounts accountId) {
        this.accountId = accountId;
    }

    public SapoOrders getOrderId() {
        return orderId;
    }

    public void setOrderId(SapoOrders orderId) {
        this.orderId = orderId;
    }

    public GiftPools getGiftPoolId() {
        return giftPoolId;
    }

    public void setGiftPoolId(GiftPools giftPoolId) {
        this.giftPoolId = giftPoolId;
    }

    @XmlTransient
    public Collection<EggOpeningLogs> getEggOpeningLogsCollection() {
        return eggOpeningLogsCollection;
    }

    public void setEggOpeningLogsCollection(Collection<EggOpeningLogs> eggOpeningLogsCollection) {
        this.eggOpeningLogsCollection = eggOpeningLogsCollection;
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
        if (!(object instanceof Eggs)) {
            return false;
        }
        Eggs other = (Eggs) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.Eggs[ id=" + id + " ]";
    }
    
}

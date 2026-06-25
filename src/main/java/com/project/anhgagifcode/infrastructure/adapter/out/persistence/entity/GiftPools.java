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
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author tranp
 */
@Entity
@Table(name = "gift_pools")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "GiftPools.findAll", query = "SELECT g FROM GiftPools g"),
    @NamedQuery(name = "GiftPools.findById", query = "SELECT g FROM GiftPools g WHERE g.id = :id"),
    @NamedQuery(name = "GiftPools.findByPoolName", query = "SELECT g FROM GiftPools g WHERE g.poolName = :poolName"),
    @NamedQuery(name = "GiftPools.findByTier", query = "SELECT g FROM GiftPools g WHERE g.tier = :tier"),
    @NamedQuery(name = "GiftPools.findByCreatedAt", query = "SELECT g FROM GiftPools g WHERE g.createdAt = :createdAt")})
public class GiftPools implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 36)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "pool_name")
    private String poolName;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    @Column(name = "tier")
    private String tier;
    @Basic(optional = false)
    @NotNull
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "giftPoolId")
    private Collection<ProductEggMappings> productEggMappingsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "giftPoolId")
    private Collection<Eggs> eggsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "poolId")
    private Collection<PoolAccountMappings> poolAccountMappingsCollection;

    public GiftPools() {
    }

    public GiftPools(String id) {
        this.id = id;
    }

    public GiftPools(String id, String poolName, String tier, Date createdAt) {
        this.id = id;
        this.poolName = poolName;
        this.tier = tier;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @XmlTransient
    public Collection<ProductEggMappings> getProductEggMappingsCollection() {
        return productEggMappingsCollection;
    }

    public void setProductEggMappingsCollection(Collection<ProductEggMappings> productEggMappingsCollection) {
        this.productEggMappingsCollection = productEggMappingsCollection;
    }

    @XmlTransient
    public Collection<Eggs> getEggsCollection() {
        return eggsCollection;
    }

    public void setEggsCollection(Collection<Eggs> eggsCollection) {
        this.eggsCollection = eggsCollection;
    }

    @XmlTransient
    public Collection<PoolAccountMappings> getPoolAccountMappingsCollection() {
        return poolAccountMappingsCollection;
    }

    public void setPoolAccountMappingsCollection(Collection<PoolAccountMappings> poolAccountMappingsCollection) {
        this.poolAccountMappingsCollection = poolAccountMappingsCollection;
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
        if (!(object instanceof GiftPools)) {
            return false;
        }
        GiftPools other = (GiftPools) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.GiftPools[ id=" + id + " ]";
    }
    
}

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
@Table(name = "product_egg_mappings")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ProductEggMappings.findAll", query = "SELECT p FROM ProductEggMappings p"),
    @NamedQuery(name = "ProductEggMappings.findById", query = "SELECT p FROM ProductEggMappings p WHERE p.id = :id"),
    @NamedQuery(name = "ProductEggMappings.findByEggTier", query = "SELECT p FROM ProductEggMappings p WHERE p.eggTier = :eggTier"),
    @NamedQuery(name = "ProductEggMappings.findByCreatedAt", query = "SELECT p FROM ProductEggMappings p WHERE p.createdAt = :createdAt"),
    @NamedQuery(name = "ProductEggMappings.findByUpdatedAt", query = "SELECT p FROM ProductEggMappings p WHERE p.updatedAt = :updatedAt")})
public class ProductEggMappings implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 36)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    @Column(name = "egg_tier")
    private String eggTier;
    @Basic(optional = false)
    @NotNull
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Basic(optional = false)
    @NotNull
    @Column(name = "rate")
    private double rate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "mappings_type")
    private int mappingsType;
    @JoinColumn(name = "gift_pool_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private GiftPools giftPoolId;
    @JoinColumn(name = "kv_product_id", referencedColumnName = "kv_product_id")
    @ManyToOne(optional = false)
    private KiotvietProducts kvProductId;

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public int getMappingsType() {
        return mappingsType;
    }

    public void setMappingsType(int mappingsType) {
        this.mappingsType = mappingsType;
    }

    public ProductEggMappings() {
    }

    public ProductEggMappings(String id) {
        this.id = id;
    }

    public ProductEggMappings(String id, String eggTier, Date createdAt) {
        this.id = id;
        this.eggTier = eggTier;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEggTier() {
        return eggTier;
    }

    public void setEggTier(String eggTier) {
        this.eggTier = eggTier;
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

    public GiftPools getGiftPoolId() {
        return giftPoolId;
    }

    public void setGiftPoolId(GiftPools giftPoolId) {
        this.giftPoolId = giftPoolId;
    }

    public KiotvietProducts getKvProductId() {
        return kvProductId;
    }

    public void setKvProductId(KiotvietProducts kvProductId) {
        this.kvProductId = kvProductId;
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
        if (!(object instanceof ProductEggMappings)) {
            return false;
        }
        ProductEggMappings other = (ProductEggMappings) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.ProductEggMappings[ id=" + id + " ]";
    }
    
}

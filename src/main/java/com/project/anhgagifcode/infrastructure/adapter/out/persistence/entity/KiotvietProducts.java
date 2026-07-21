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
import jakarta.persistence.Lob;
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
@Table(name = "kiotviet_products")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "KiotvietProducts.findAll", query = "SELECT k FROM KiotvietProducts k"),
    @NamedQuery(name = "KiotvietProducts.findByKvProductId", query = "SELECT k FROM KiotvietProducts k WHERE k.kvProductId = :kvProductId"),
    @NamedQuery(name = "KiotvietProducts.findByCode", query = "SELECT k FROM KiotvietProducts k WHERE k.code = :code"),
    @NamedQuery(name = "KiotvietProducts.findByName", query = "SELECT k FROM KiotvietProducts k WHERE k.name = :name"),
    @NamedQuery(name = "KiotvietProducts.findByFullName", query = "SELECT k FROM KiotvietProducts k WHERE k.fullName = :fullName"),
    @NamedQuery(name = "KiotvietProducts.findByBasePrice", query = "SELECT k FROM KiotvietProducts k WHERE k.basePrice = :basePrice"),
    @NamedQuery(name = "KiotvietProducts.findByLastSyncedAt", query = "SELECT k FROM KiotvietProducts k WHERE k.lastSyncedAt = :lastSyncedAt")})
public class KiotvietProducts implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "kv_product_id")
    private Long kvProductId;
    @Size(max = 255)
    @Column(name = "name")
    private String name;
    @Size(max = 500)
    @Column(name = "full_name")
    private String fullName;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "base_price")
    private BigDecimal basePrice;
    @Lob
    @Size(max = 65535)
    @Column(name = "image_url")
    private String imageUrl;
    @Size(max = 255)
    @Column(name = "code")
    private String code;
    @Basic(optional = false)
    @NotNull
    @Column(name = "last_synced_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastSyncedAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "kvProductId")
    private Collection<ProductEggMappings> productEggMappingsCollection;

    @Column(name = "egg_type1_qty")
    private Integer eggType1Qty = 1;

    @Column(name = "egg_type2_qty")
    private Integer eggType2Qty = 1;

    public Integer getEggType1Qty() {
        return eggType1Qty;
    }

    public void setEggType1Qty(Integer eggType1Qty) {
        this.eggType1Qty = eggType1Qty;
    }

    public Integer getEggType2Qty() {
        return eggType2Qty;
    }

    public void setEggType2Qty(Integer eggType2Qty) {
        this.eggType2Qty = eggType2Qty;
    }

    public KiotvietProducts() {
    }

    public KiotvietProducts(Long kvProductId) {
        this.kvProductId = kvProductId;
    }

    public KiotvietProducts(Long kvProductId, Date lastSyncedAt) {
        this.kvProductId = kvProductId;
        this.lastSyncedAt = lastSyncedAt;
    }

    public Long getKvProductId() {
        return kvProductId;
    }

    public void setKvProductId(Long kvProductId) {
        this.kvProductId = kvProductId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Date lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    @XmlTransient
    public Collection<ProductEggMappings> getProductEggMappingsCollection() {
        return productEggMappingsCollection;
    }

    public void setProductEggMappingsCollection(Collection<ProductEggMappings> productEggMappingsCollection) {
        this.productEggMappingsCollection = productEggMappingsCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (kvProductId != null ? kvProductId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof KiotvietProducts)) {
            return false;
        }
        KiotvietProducts other = (KiotvietProducts) object;
        if ((this.kvProductId == null && other.kvProductId != null) || (this.kvProductId != null && !this.kvProductId.equals(other.kvProductId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.KiotvietProducts[ kvProductId=" + kvProductId + " ]";
    }
    
}

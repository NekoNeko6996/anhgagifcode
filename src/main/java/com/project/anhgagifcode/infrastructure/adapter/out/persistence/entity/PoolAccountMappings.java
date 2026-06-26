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
@Table(name = "pool_account_mappings")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PoolAccountMappings.findAll", query = "SELECT p FROM PoolAccountMappings p"),
    @NamedQuery(name = "PoolAccountMappings.findById", query = "SELECT p FROM PoolAccountMappings p WHERE p.id = :id")})
public class PoolAccountMappings implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 36)
    @Column(name = "id")
    private String id;
    @JoinColumn(name = "pool_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private GiftPools poolId;
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private GiftAccounts accountId;

    public PoolAccountMappings() {
    }

    public PoolAccountMappings(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GiftPools getPoolId() {
        return poolId;
    }

    public void setPoolId(GiftPools poolId) {
        this.poolId = poolId;
    }

    public GiftAccounts getAccountId() {
        return accountId;
    }

    public void setAccountId(GiftAccounts accountId) {
        this.accountId = accountId;
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
        if (!(object instanceof PoolAccountMappings)) {
            return false;
        }
        PoolAccountMappings other = (PoolAccountMappings) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.PoolAccountMappings[ id=" + id + " ]";
    }
    
}

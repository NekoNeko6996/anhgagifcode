/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
@Table(name = "gift_accounts")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "GiftAccounts.findAll", query = "SELECT g FROM GiftAccounts g"),
    @NamedQuery(name = "GiftAccounts.findById", query = "SELECT g FROM GiftAccounts g WHERE g.id = :id"),
    @NamedQuery(name = "GiftAccounts.findByUsername", query = "SELECT g FROM GiftAccounts g WHERE g.username = :username"),
    @NamedQuery(name = "GiftAccounts.findByPassword", query = "SELECT g FROM GiftAccounts g WHERE g.password = :password"),
    @NamedQuery(name = "GiftAccounts.findByStatus", query = "SELECT g FROM GiftAccounts g WHERE g.status = :status"),
    @NamedQuery(name = "GiftAccounts.findByTier", query = "SELECT g FROM GiftAccounts g WHERE g.tier = :tier"),
    @NamedQuery(name = "GiftAccounts.findByPlatform", query = "SELECT g FROM GiftAccounts g WHERE g.platform = :platform"),
    @NamedQuery(name = "GiftAccounts.findByCreatedAt", query = "SELECT g FROM GiftAccounts g WHERE g.createdAt = :createdAt"),
    @NamedQuery(name = "GiftAccounts.findByAssignedAt", query = "SELECT g FROM GiftAccounts g WHERE g.assignedAt = :assignedAt")})
public class GiftAccounts implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 36)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "username")
    private String username;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "password")
    private String password;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "status")
    private String status;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    @Column(name = "tier")
    private String tier;
    @Size(max = 50)
    @Column(name = "platform")
    private String platform;
    @Lob
    @Size(max = 65535)
    @Column(name = "token")
    private String token;
    @Basic(optional = false)
    @NotNull
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "assigned_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date assignedAt;

    public GiftAccounts() {
    }

    public GiftAccounts(String id) {
        this.id = id;
    }

    public GiftAccounts(String id, String username, String password, String status, String tier, Date createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.status = status;
        this.tier = tier;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Date assignedAt) {
        this.assignedAt = assignedAt;
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
        if (!(object instanceof GiftAccounts)) {
            return false;
        }
        GiftAccounts other = (GiftAccounts) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.GiftAccounts[ id=" + id + " ]";
    }
    
}

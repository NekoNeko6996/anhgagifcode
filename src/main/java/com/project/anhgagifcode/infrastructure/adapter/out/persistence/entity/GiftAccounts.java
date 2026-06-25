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
@Table(name = "gift_accounts")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "GiftAccounts.findAll", query = "SELECT g FROM GiftAccounts g"),
    @NamedQuery(name = "GiftAccounts.findById", query = "SELECT g FROM GiftAccounts g WHERE g.id = :id"),
    @NamedQuery(name = "GiftAccounts.findByUsername", query = "SELECT g FROM GiftAccounts g WHERE g.username = :username"),
    @NamedQuery(name = "GiftAccounts.findByPassword", query = "SELECT g FROM GiftAccounts g WHERE g.password = :password"),
    @NamedQuery(name = "GiftAccounts.findByStatus", query = "SELECT g FROM GiftAccounts g WHERE g.status = :status"),
    @NamedQuery(name = "GiftAccounts.findByPlatfrom", query = "SELECT g FROM GiftAccounts g WHERE g.platfrom = :platfrom"),
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
    @Size(max = 50)
    @Column(name = "platfrom")
    private String platfrom;
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
    @OneToOne(mappedBy = "accountId")
    private Eggs eggs;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "accountId")
    private Collection<PoolAccountMappings> poolAccountMappingsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "accountId")
    private Collection<EggOpeningLogs> eggOpeningLogsCollection;

    public GiftAccounts() {
    }

    public GiftAccounts(String id) {
        this.id = id;
    }

    public GiftAccounts(String id, String username, String password, String status, Date createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.status = status;
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

    public String getPlatfrom() {
        return platfrom;
    }

    public void setPlatfrom(String platfrom) {
        this.platfrom = platfrom;
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

    public Eggs getEggs() {
        return eggs;
    }

    public void setEggs(Eggs eggs) {
        this.eggs = eggs;
    }

    @XmlTransient
    public Collection<PoolAccountMappings> getPoolAccountMappingsCollection() {
        return poolAccountMappingsCollection;
    }

    public void setPoolAccountMappingsCollection(Collection<PoolAccountMappings> poolAccountMappingsCollection) {
        this.poolAccountMappingsCollection = poolAccountMappingsCollection;
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

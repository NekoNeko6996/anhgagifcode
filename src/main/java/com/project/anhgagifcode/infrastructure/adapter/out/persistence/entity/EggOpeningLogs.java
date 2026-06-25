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
@Table(name = "egg_opening_logs")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "EggOpeningLogs.findAll", query = "SELECT e FROM EggOpeningLogs e"),
    @NamedQuery(name = "EggOpeningLogs.findById", query = "SELECT e FROM EggOpeningLogs e WHERE e.id = :id"),
    @NamedQuery(name = "EggOpeningLogs.findByActionType", query = "SELECT e FROM EggOpeningLogs e WHERE e.actionType = :actionType"),
    @NamedQuery(name = "EggOpeningLogs.findByTriggeredBy", query = "SELECT e FROM EggOpeningLogs e WHERE e.triggeredBy = :triggeredBy"),
    @NamedQuery(name = "EggOpeningLogs.findByIpAddress", query = "SELECT e FROM EggOpeningLogs e WHERE e.ipAddress = :ipAddress"),
    @NamedQuery(name = "EggOpeningLogs.findByCreatedAt", query = "SELECT e FROM EggOpeningLogs e WHERE e.createdAt = :createdAt")})
public class EggOpeningLogs implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 36)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "action_type")
    private String actionType;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "triggered_by")
    private String triggeredBy;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "ip_address")
    private String ipAddress;
    @Basic(optional = false)
    @NotNull
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private GiftAccounts accountId;
    @JoinColumn(name = "egg_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Eggs eggId;

    public EggOpeningLogs() {
    }

    public EggOpeningLogs(String id) {
        this.id = id;
    }

    public EggOpeningLogs(String id, String actionType, String triggeredBy, String ipAddress, Date createdAt) {
        this.id = id;
        this.actionType = actionType;
        this.triggeredBy = triggeredBy;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public GiftAccounts getAccountId() {
        return accountId;
    }

    public void setAccountId(GiftAccounts accountId) {
        this.accountId = accountId;
    }

    public Eggs getEggId() {
        return eggId;
    }

    public void setEggId(Eggs eggId) {
        this.eggId = eggId;
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
        if (!(object instanceof EggOpeningLogs)) {
            return false;
        }
        EggOpeningLogs other = (EggOpeningLogs) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.EggOpeningLogs[ id=" + id + " ]";
    }
    
}

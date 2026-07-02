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
@Table(name = "customers")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Customers.findAll", query = "SELECT c FROM Customers c"),
    @NamedQuery(name = "Customers.findById", query = "SELECT c FROM Customers c WHERE c.id = :id"),
    @NamedQuery(name = "Customers.findByCustomerCode", query = "SELECT c FROM Customers c WHERE c.customerCode = :customerCode"),
    @NamedQuery(name = "Customers.findByCustomerName", query = "SELECT c FROM Customers c WHERE c.customerName = :customerName"),
    @NamedQuery(name = "Customers.findByStatus", query = "SELECT c FROM Customers c WHERE c.status = :status"),
    @NamedQuery(name = "Customers.findBySuccessCount", query = "SELECT c FROM Customers c WHERE c.successCount = :successCount"),
    @NamedQuery(name = "Customers.findByReturnStreak", query = "SELECT c FROM Customers c WHERE c.returnStreak = :returnStreak"),
    @NamedQuery(name = "Customers.findByWarningCount", query = "SELECT c FROM Customers c WHERE c.warningCount = :warningCount"),
    @NamedQuery(name = "Customers.findByCreatedAt", query = "SELECT c FROM Customers c WHERE c.createdAt = :createdAt"),
    @NamedQuery(name = "Customers.findByUpdatedAt", query = "SELECT c FROM Customers c WHERE c.updatedAt = :updatedAt")})
public class Customers implements Serializable {

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
    @Column(name = "customer_code")
    private String customerCode;
    @Size(max = 255)
    @Column(name = "customer_name")
    private String customerName;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "status")
    private String status;
    @Basic(optional = false)
    @NotNull
    @Column(name = "success_count")
    private int successCount;
    @Basic(optional = false)
    @NotNull
    @Column(name = "return_streak")
    private int returnStreak;
    @Basic(optional = false)
    @NotNull
    @Column(name = "warning_count")
    private int warningCount;
    @Basic(optional = false)
    @NotNull
    @Column(name = "early_hatch_credits")
    private int earlyHatchCredits;
    @Basic(optional = false)
    @NotNull
    @Column(name = "return_count")
    private int returnCount;
    @Basic(optional = false)
    @NotNull
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "customerCode")
    private Collection<KiotvietOrders> kiotvietOrdersCollection;

    public Customers() {
    }

    public Customers(String id) {
        this.id = id;
    }

    public Customers(String id, String customerCode, String status, int successCount, int returnStreak, int warningCount, Date createdAt) {
        this.id = id;
        this.customerCode = customerCode;
        this.status = status;
        this.successCount = successCount;
        this.returnStreak = returnStreak;
        this.warningCount = warningCount;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getReturnStreak() {
        return returnStreak;
    }

    public void setReturnStreak(int returnStreak) {
        this.returnStreak = returnStreak;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(int warningCount) {
        this.warningCount = warningCount;
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
    public Collection<KiotvietOrders> getKiotvietOrdersCollection() {
        return kiotvietOrdersCollection;
    }

    public void setKiotvietOrdersCollection(Collection<KiotvietOrders> kiotvietOrdersCollection) {
        this.kiotvietOrdersCollection = kiotvietOrdersCollection;
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
        if (!(object instanceof Customers)) {
            return false;
        }
        Customers other = (Customers) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    public int getEarlyHatchCredits() {
        return earlyHatchCredits;
    }

    public void setEarlyHatchCredits(int earlyHatchCredits) {
        this.earlyHatchCredits = earlyHatchCredits;
    }

    public int getReturnCount() {
        return returnCount;
    }

    public void setReturnCount(int returnCount) {
        this.returnCount = returnCount;
    }

    @Override
    public String toString() {
        return "com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.Customers[ id=" + id + " ]";
    }
    
}

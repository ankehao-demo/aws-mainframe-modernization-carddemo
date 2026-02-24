package com.carddemo.transactiontype.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "TRANSACTION_TYPE", schema = "CARDDEMO")
public class TransactionType {

    @Id
    @Column(name = "TR_TYPE", length = 2)
    private String trType;

    @Column(name = "TR_DESCRIPTION", length = 50, nullable = false)
    private String trDescription;

    @OneToMany(mappedBy = "transactionType", cascade = CascadeType.ALL)
    private List<TransactionTypeCategory> categories = new ArrayList<>();

    public TransactionType() {
    }

    public TransactionType(String trType, String trDescription) {
        this.trType = trType;
        this.trDescription = trDescription;
    }

    public String getTrType() {
        return trType;
    }

    public void setTrType(String trType) {
        this.trType = trType;
    }

    public String getTrDescription() {
        return trDescription;
    }

    public void setTrDescription(String trDescription) {
        this.trDescription = trDescription;
    }

    public List<TransactionTypeCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<TransactionTypeCategory> categories) {
        this.categories = categories;
    }
}

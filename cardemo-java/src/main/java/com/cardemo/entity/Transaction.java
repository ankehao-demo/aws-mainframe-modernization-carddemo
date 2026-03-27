package com.cardemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Transaction entity derived from CVTRA05Y.cpy (TRAN-RECORD, RECLN 350).
 *
 * COBOL field mappings:
 *   TRAN-ID              PIC X(16)       -> String (primary key)
 *   TRAN-TYPE-CD         PIC X(02)       -> String
 *   TRAN-CAT-CD          PIC 9(04)       -> Integer
 *   TRAN-SOURCE          PIC X(10)       -> String
 *   TRAN-DESC            PIC X(100)      -> String
 *   TRAN-AMT             PIC S9(09)V99   -> BigDecimal
 *   TRAN-MERCHANT-ID     PIC 9(09)       -> Long
 *   TRAN-MERCHANT-NAME   PIC X(50)       -> String
 *   TRAN-MERCHANT-CITY   PIC X(50)       -> String
 *   TRAN-MERCHANT-ZIP    PIC X(10)       -> String
 *   TRAN-CARD-NUM        PIC X(16)       -> String
 *   TRAN-ORIG-TS         PIC X(26)       -> String
 *   TRAN-PROC-TS         PIC X(26)       -> String
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(name = "tran_id", length = 16)
    private String tranId;

    @Column(name = "tran_type_cd", length = 2)
    private String typeCd;

    @Column(name = "tran_cat_cd")
    private Integer categoryCd;

    @Column(name = "tran_source", length = 10)
    private String source;

    @Column(name = "tran_desc", length = 100)
    private String description;

    @Column(name = "tran_amt", precision = 11, scale = 2)
    private BigDecimal amount;

    @Column(name = "tran_merchant_id")
    private Long merchantId;

    @Column(name = "tran_merchant_name", length = 50)
    private String merchantName;

    @Column(name = "tran_merchant_city", length = 50)
    private String merchantCity;

    @Column(name = "tran_merchant_zip", length = 10)
    private String merchantZip;

    @Column(name = "tran_card_num", length = 16)
    private String cardNum;

    @Column(name = "tran_orig_ts", length = 26)
    private String originTimestamp;

    @Column(name = "tran_proc_ts", length = 26)
    private String processedTimestamp;

    public Transaction() {
    }

    public String getTranId() {
        return tranId;
    }

    public void setTranId(String tranId) {
        this.tranId = tranId;
    }

    public String getTypeCd() {
        return typeCd;
    }

    public void setTypeCd(String typeCd) {
        this.typeCd = typeCd;
    }

    public Integer getCategoryCd() {
        return categoryCd;
    }

    public void setCategoryCd(Integer categoryCd) {
        this.categoryCd = categoryCd;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantCity() {
        return merchantCity;
    }

    public void setMerchantCity(String merchantCity) {
        this.merchantCity = merchantCity;
    }

    public String getMerchantZip() {
        return merchantZip;
    }

    public void setMerchantZip(String merchantZip) {
        this.merchantZip = merchantZip;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public String getOriginTimestamp() {
        return originTimestamp;
    }

    public void setOriginTimestamp(String originTimestamp) {
        this.originTimestamp = originTimestamp;
    }

    public String getProcessedTimestamp() {
        return processedTimestamp;
    }

    public void setProcessedTimestamp(String processedTimestamp) {
        this.processedTimestamp = processedTimestamp;
    }
}

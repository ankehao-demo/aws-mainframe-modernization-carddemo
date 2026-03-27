package com.carddemo.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CompositeIdTest {

    // DisclosureGroupId tests
    @Test
    void disclosureGroupIdConstructorAndGetters() {
        DisclosureGroupId id = new DisclosureGroupId("GRP001", "01", 1);
        assertThat(id.getGroupId()).isEqualTo("GRP001");
        assertThat(id.getTranTypeCd()).isEqualTo("01");
        assertThat(id.getTranCatCd()).isEqualTo(1);
    }

    @Test
    void disclosureGroupIdSetters() {
        DisclosureGroupId id = new DisclosureGroupId();
        id.setGroupId("GRP002");
        id.setTranTypeCd("02");
        id.setTranCatCd(3);
        assertThat(id.getGroupId()).isEqualTo("GRP002");
        assertThat(id.getTranTypeCd()).isEqualTo("02");
        assertThat(id.getTranCatCd()).isEqualTo(3);
    }

    @Test
    void disclosureGroupIdEquals() {
        DisclosureGroupId id1 = new DisclosureGroupId("GRP001", "01", 1);
        DisclosureGroupId id2 = new DisclosureGroupId("GRP001", "01", 1);
        DisclosureGroupId id3 = new DisclosureGroupId("GRP002", "01", 1);

        assertThat(id1).isEqualTo(id2);
        assertThat(id1).isNotEqualTo(id3);
        assertThat(id1).isEqualTo(id1);
        assertThat(id1).isNotEqualTo(null);
        assertThat(id1).isNotEqualTo("string");
    }

    @Test
    void disclosureGroupIdHashCode() {
        DisclosureGroupId id1 = new DisclosureGroupId("GRP001", "01", 1);
        DisclosureGroupId id2 = new DisclosureGroupId("GRP001", "01", 1);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void disclosureGroupIdEqualsDifferentFields() {
        DisclosureGroupId id1 = new DisclosureGroupId("GRP001", "01", 1);
        DisclosureGroupId id2 = new DisclosureGroupId("GRP001", "02", 1);
        DisclosureGroupId id3 = new DisclosureGroupId("GRP001", "01", 2);
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1).isNotEqualTo(id3);
    }

    // TranCatBalanceId tests
    @Test
    void tranCatBalanceIdConstructorAndGetters() {
        TranCatBalanceId id = new TranCatBalanceId(1L, "01", 1);
        assertThat(id.getAcctId()).isEqualTo(1L);
        assertThat(id.getTypeCd()).isEqualTo("01");
        assertThat(id.getCatCd()).isEqualTo(1);
    }

    @Test
    void tranCatBalanceIdSetters() {
        TranCatBalanceId id = new TranCatBalanceId();
        id.setAcctId(2L);
        id.setTypeCd("02");
        id.setCatCd(3);
        assertThat(id.getAcctId()).isEqualTo(2L);
        assertThat(id.getTypeCd()).isEqualTo("02");
        assertThat(id.getCatCd()).isEqualTo(3);
    }

    @Test
    void tranCatBalanceIdEquals() {
        TranCatBalanceId id1 = new TranCatBalanceId(1L, "01", 1);
        TranCatBalanceId id2 = new TranCatBalanceId(1L, "01", 1);
        TranCatBalanceId id3 = new TranCatBalanceId(2L, "01", 1);

        assertThat(id1).isEqualTo(id2);
        assertThat(id1).isNotEqualTo(id3);
        assertThat(id1).isEqualTo(id1);
        assertThat(id1).isNotEqualTo(null);
        assertThat(id1).isNotEqualTo("string");
    }

    @Test
    void tranCatBalanceIdHashCode() {
        TranCatBalanceId id1 = new TranCatBalanceId(1L, "01", 1);
        TranCatBalanceId id2 = new TranCatBalanceId(1L, "01", 1);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void tranCatBalanceIdEqualsDifferentFields() {
        TranCatBalanceId id1 = new TranCatBalanceId(1L, "01", 1);
        TranCatBalanceId id2 = new TranCatBalanceId(1L, "02", 1);
        TranCatBalanceId id3 = new TranCatBalanceId(1L, "01", 2);
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1).isNotEqualTo(id3);
    }

    // TranCategoryId tests
    @Test
    void tranCategoryIdConstructorAndGetters() {
        TranCategoryId id = new TranCategoryId("01", 1);
        assertThat(id.getTypeCd()).isEqualTo("01");
        assertThat(id.getCatCd()).isEqualTo(1);
    }

    @Test
    void tranCategoryIdSetters() {
        TranCategoryId id = new TranCategoryId();
        id.setTypeCd("02");
        id.setCatCd(3);
        assertThat(id.getTypeCd()).isEqualTo("02");
        assertThat(id.getCatCd()).isEqualTo(3);
    }

    @Test
    void tranCategoryIdEquals() {
        TranCategoryId id1 = new TranCategoryId("01", 1);
        TranCategoryId id2 = new TranCategoryId("01", 1);
        TranCategoryId id3 = new TranCategoryId("02", 1);

        assertThat(id1).isEqualTo(id2);
        assertThat(id1).isNotEqualTo(id3);
        assertThat(id1).isEqualTo(id1);
        assertThat(id1).isNotEqualTo(null);
        assertThat(id1).isNotEqualTo("string");
    }

    @Test
    void tranCategoryIdHashCode() {
        TranCategoryId id1 = new TranCategoryId("01", 1);
        TranCategoryId id2 = new TranCategoryId("01", 1);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void tranCategoryIdEqualsDifferentCatCd() {
        TranCategoryId id1 = new TranCategoryId("01", 1);
        TranCategoryId id2 = new TranCategoryId("01", 2);
        assertThat(id1).isNotEqualTo(id2);
    }
}

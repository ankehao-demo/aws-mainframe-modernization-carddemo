# CardDemo COBOL Dependency Map

## Program Inventory

| Program | Type | Copybooks | VSAM Files | BMS Maps | CICS TxnID | Calls To | Called By |
|---------|------|-----------|------------|----------|------------|----------|-----------|
| COSGN00C | Online | COCOM01Y, COSGN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | USRSEC | COSGN00 / COSGN0A | CC00 | COADM01C (admin), COMEN01C (user) via XCTL | Entry point |
| COADM01C | Online | COCOM01Y, COADM02Y, COADM01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | USRSEC | COADM01 / COADM0A | CA00 | COUSR00C, COUSR01C, COUSR02C, COUSR03C, COTRTLIC, COTRTUPC via XCTL | COSGN00C |
| COMEN01C | Online | COCOM01Y, COMEN02Y, COMEN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | USRSEC | COMEN01 / COMEN0A | CM00 | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COPAUS0C via XCTL | COSGN00C |
| COACTVWC | Online | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COACTVW, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSSTRPFY | ACCTDAT, CARDDAT, CUSTDAT, CARDAIX | COACTVW / CACTVWA | CAVW | COMEN01C via XCTL | COMEN01C |
| COACTUPC | Online | CSUTLDWY, CVCRD01Y, CSLKPCDY, DFHBMSCA, DFHAID, COTTL01Y, COACTUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT03Y, CVCUS01Y, COCOM01Y, CSSETATY, CSSTRPFY, CSUTLDPY | ACCTDAT, CUSTDAT, CARDDAT, CARDAIX | COACTUP / CACTUPA | CAUP | COMEN01C via XCTL | COMEN01C |
| COCRDLIC | Online | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDLI, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, CSSTRPFY | CARDDAT, CARDAIX | COCRDLI / CCRDLIA | CCLI | COCRDSLC, COCRDUPC via XCTL | COMEN01C |
| COCRDSLC | Online | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDSL, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY | CARDDAT, CARDAIX | COCRDSL / CCRDSLA | CCDL | COMEN01C via XCTL | COCRDLIC, COMEN01C |
| COCRDUPC | Online | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY | CARDDAT, CARDAIX | COCRDUP / CCRDUPA | CCUP | COMEN01C via XCTL | COCRDLIC, COMEN01C |
| COTRN00C | Online | COCOM01Y, COTRN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA | TRANSACT | COTRN00 / COTRN0A | CT00 | COTRN01C, COTRN02C via XCTL | COMEN01C |
| COTRN01C | Online | COCOM01Y, COTRN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA | TRANSACT | COTRN01 / COTRN1A | CT01 | COMEN01C via XCTL | COTRN00C |
| COTRN02C | Online | COCOM01Y, COTRN02, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, CVACT01Y, CVACT03Y, DFHAID, DFHBMSCA | TRANSACT, ACCTDAT, CCXREF | COTRN02 / COTRN2A | CT02 | COMEN01C via XCTL | COTRN00C, COMEN01C |
| CORPT00C | Online | COCOM01Y, CORPT00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA | TRANSACT | CORPT00 / CORPT0A | CR00 | COMEN01C via XCTL | COMEN01C |
| COBIL00C | Online | COCOM01Y, COBIL00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT03Y, CVTRA05Y, DFHAID, DFHBMSCA | TRANSACT, ACCTDAT | COBIL00 / COBIL0A | CB00 | COMEN01C via XCTL | COMEN01C |
| COUSR00C | Online | COCOM01Y, COUSR00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | USRSEC | COUSR00 / CUSR00A | CU00 | COUSR01C, COUSR02C, COUSR03C via XCTL | COADM01C |
| COUSR01C | Online | COCOM01Y, COUSR01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | USRSEC | COUSR01 / CUSR01A | CU01 | COADM01C via XCTL | COUSR00C, COADM01C |
| COUSR02C | Online | COCOM01Y, COUSR02, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | USRSEC | COUSR02 / CUSR02A | CU02 | COADM01C via XCTL | COUSR00C, COADM01C |
| COUSR03C | Online | COCOM01Y, COUSR03, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | USRSEC | COUSR03 / CUSR03A | CU03 | COADM01C via XCTL | COUSR00C, COADM01C |
| COBSWAIT | Online | (none) | (none) | (none) | — | — | — |
| CSUTLDTC | Online | (none) | (none) | (none) | — | — | — |
| CBACT01C | Batch | CVACT01Y, CODATECN | ACCTFILE | — | — | — | JCL |
| CBACT02C | Batch | CVACT02Y | CARDFILE | — | — | — | JCL |
| CBACT03C | Batch | CVACT03Y | XREFFILE | — | — | — | JCL |
| CBACT04C | Batch | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y | TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANSACT | — | — | — | JCL |
| CBCUS01C | Batch | CVCUS01Y | CUSTFILE | — | — | — | JCL |
| CBTRN01C | Batch | CVTRA06Y, CVCUS01Y, CVACT03Y, CVACT02Y, CVACT01Y, CVTRA05Y | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE | — | — | — | JCL |
| CBTRN02C | Batch | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y | DALYTRAN, TRANFILE, XREFFILE, DALYREJS, ACCTFILE, TCATBALF | — | — | — | JCL |
| CBTRN03C | Batch | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, TRANREPT, DATEPARM | — | — | — | JCL |
| CBSTM03A | Batch | COSTM01, CVACT03Y, CUSTREC, CVACT01Y | (statement gen) | — | — | — | JCL |
| CBSTM03B | Batch | (none) | (statement gen) | — | — | — | JCL |

## Copybook Reference

| Copybook | Description | Used By |
|----------|-------------|---------|
| COCOM01Y | COMMAREA — inter-program communication | All online programs |
| CVACT01Y | Account record layout (RECLN 300) | COACTVWC, COACTUPC, COTRN02C, COBIL00C, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03A |
| CVACT02Y | Card record layout (RECLN 150) | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C |
| CVACT03Y | Card cross-reference record (RECLN 50) | COACTVWC, COACTUPC, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, COBIL00C, COTRN02C, CBSTM03A |
| CVCRD01Y | Card work areas (AID keys, navigation) | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC |
| CVCUS01Y | Customer record layout (RECLN 500) | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, CBCUS01C, CBTRN01C |
| CVTRA01Y | Transaction category balance record (RECLN 50) | CBACT04C, CBTRN02C |
| CVTRA02Y | Disclosure/discount group record (RECLN 50) | CBACT04C |
| CVTRA03Y | Transaction type record | CBTRN03C |
| CVTRA04Y | Transaction category record | CBTRN03C |
| CVTRA05Y | Transaction record layout (RECLN 350) | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C |
| CVTRA06Y | Daily transaction record layout (RECLN 350) | CBTRN01C, CBTRN02C |
| CVTRA07Y | Transaction report layout | CBTRN03C |
| COSTM01 | Transaction altered layout for reporting | CBSTM03A |
| CUSTREC | Customer record (alternate) | CBSTM03A |
| CSUSR01Y | User security record | COSGN00C, COADM01C, COMEN01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00C-03C |
| COADM02Y | Admin menu options (6 items) | COADM01C |
| COMEN02Y | User menu options (11 items) | COMEN01C |
| CSDAT01Y | Date/time structures | All online programs |
| CSMSG01Y | Message area | All online programs |
| CSMSG02Y | Abend data | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC |
| COTTL01Y | Title line | All online programs |
| CSLKPCDY | Lookup codes (state, area codes) | COACTUPC |
| CSSTRPFY | PF key mapping procedure | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC |
| CSUTLDPY | Date validation procedure | COACTUPC |
| CSUTLDWY | Date validation work areas | COACTUPC |
| CSSETATY | Set attribute procedure (COPY REPLACING) | COACTUPC |
| CODATECN | Date conversion | CBACT01C |

## VSAM File Reference

| CICS File Name | Batch DD Name | Description | Key | Programs |
|----------------|---------------|-------------|-----|----------|
| USRSEC | — | User security records | SEC-USR-ID (8 chars) | COSGN00C, COADM01C, COMEN01C, COUSR00C-03C |
| ACCTDAT | ACCTFILE | Account master records | ACCT-ID 9(11) | COACTVWC, COACTUPC, COTRN02C, COBIL00C, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C |
| CARDDAT | CARDFILE | Card records | CARD-NUM X(16) | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C |
| CARDAIX | — | Card alternate index (by account) | CARD-ACCT-ID | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC |
| CUSTDAT | CUSTFILE | Customer records | CUST-ID 9(09) | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, CBCUS01C, CBTRN01C |
| TRANSACT | TRANFILE | Transaction records | TRAN-CARD-NUM + TRAN-ID (composite) | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C |
| CCXREF | XREFFILE / CARDXREF | Card cross-reference | XREF-CARD-NUM X(16) | COTRN02C, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C |
| — | TCATBALF | Transaction category balance | ACCT-ID + TYPE-CD + CAT-CD (composite) | CBACT04C, CBTRN02C |
| — | DALYTRAN | Daily transaction input (sequential) | — | CBTRN01C, CBTRN02C |
| — | DALYREJS | Daily rejects output (sequential) | — | CBTRN02C |
| — | DISCGRP | Discount group rates | GROUP-ID + TYPE-CD + CAT-CD (composite) | CBACT04C |
| — | TRANTYPE | Transaction type reference | — | CBTRN03C |
| — | TRANCATG | Transaction category reference | — | CBTRN03C |
| — | TRANREPT | Transaction report output | — | CBTRN03C |
| — | DATEPARM | Date parameters input | — | CBTRN03C |

## BMS Maps

| Mapset | Map | Program | Description |
|--------|-----|---------|-------------|
| COSGN00 | COSGN0A | COSGN00C | Sign-on screen |
| COADM01 | COADM0A | COADM01C | Admin menu screen |
| COMEN01 | COMEN0A | COMEN01C | User menu screen |
| COACTVW | CACTVWA | COACTVWC | Account view screen |
| COACTUP | CACTUPA | COACTUPC | Account update screen |
| COCRDLI | CCRDLIA | COCRDLIC | Credit card list screen |
| COCRDSL | CCRDSLA | COCRDSLC | Credit card detail view screen |
| COCRDUP | CCRDUPA | COCRDUPC | Credit card update screen |
| COTRN00 | COTRN0A | COTRN00C | Transaction list screen |
| COTRN01 | COTRN1A | COTRN01C | Transaction view screen |
| COTRN02 | COTRN2A | COTRN02C | Transaction add screen |
| CORPT00 | CORPT0A | CORPT00C | Reports screen |
| COBIL00 | COBIL0A | COBIL00C | Bill payment screen |
| COUSR00 | CUSR00A | COUSR00C | User list screen |
| COUSR01 | CUSR01A | COUSR01C | User add screen |
| COUSR02 | CUSR02A | COUSR02C | User update screen |
| COUSR03 | CUSR03A | COUSR03C | User delete screen |

## CICS Transaction IDs

| TxnID | Program | Description |
|-------|---------|-------------|
| CC00 | COSGN00C | Sign-on |
| CA00 | COADM01C | Admin menu |
| CM00 | COMEN01C | User main menu |
| CAVW | COACTVWC | Account view |
| CAUP | COACTUPC | Account update |
| CCLI | COCRDLIC | Credit card list |
| CCDL | COCRDSLC | Credit card detail |
| CCUP | COCRDUPC | Credit card update |
| CT00 | COTRN00C | Transaction list |
| CT01 | COTRN01C | Transaction view |
| CT02 | COTRN02C | Transaction add |
| CR00 | CORPT00C | Transaction reports |
| CB00 | COBIL00C | Bill payment |
| CU00 | COUSR00C | User list (admin) |
| CU01 | COUSR01C | User add (admin) |
| CU02 | COUSR02C | User update (admin) |
| CU03 | COUSR03C | User delete (admin) |

## Java Module Mapping

| Java Module | COBOL Programs | CICS TxnIDs | VSAM Files | Description |
|-------------|---------------|-------------|------------|-------------|
| carddemo-auth | COSGN00C | CC00 | USRSEC | Sign-on and authentication |
| carddemo-account | COACTVWC, COACTUPC | CAVW, CAUP | ACCTDAT, CUSTDAT, CARDDAT, CARDAIX | Account view and update |
| carddemo-card | COCRDLIC, COCRDSLC, COCRDUPC | CCLI, CCDL, CCUP | CARDDAT, CARDAIX, CUSTDAT | Card list, detail view, and update |
| carddemo-transaction | COTRN00C, COTRN01C, COTRN02C | CT00, CT01, CT02 | TRANSACT, ACCTDAT, CCXREF | Transaction list, view, and add |
| carddemo-admin | COADM01C, COUSR00C-03C, COTRTLIC, COTRTUPC | CA00, CU00-CU03 | USRSEC | Admin menu and user management |
| carddemo-batch | CBACT01C-04C, CBCUS01C, CBTRN01C-03C, CBSTM03A/B | — | All batch files | Batch processing (interest, statements, posting) |
| carddemo-common | — | — | — | Shared entities, repositories, DTOs, exceptions |
| carddemo-web | COMEN01C (routing only) | CM00 | — | Web application entrypoint and configuration |
| carddemo-migration | — | — | — | EBCDIC/ASCII data migration utility |

## Program Call Graph

```
COSGN00C (CC00 - Sign-on)
├── [Admin] COADM01C (CA00 - Admin Menu)
│   ├── COUSR00C (CU00 - User List)
│   │   ├── COUSR01C (CU01 - User Add)
│   │   ├── COUSR02C (CU02 - User Update)
│   │   └── COUSR03C (CU03 - User Delete)
│   ├── COUSR01C (CU01 - User Add)
│   ├── COUSR02C (CU02 - User Update)
│   ├── COUSR03C (CU03 - User Delete)
│   ├── COTRTLIC (Transaction Type List - Db2)
│   └── COTRTUPC (Transaction Type Maintenance - Db2)
└── [User] COMEN01C (CM00 - User Menu)
    ├── COACTVWC (CAVW - Account View)
    ├── COACTUPC (CAUP - Account Update)
    ├── COCRDLIC (CCLI - Credit Card List)
    │   ├── COCRDSLC (CCDL - Card Detail View)
    │   └── COCRDUPC (CCUP - Card Update)
    ├── COCRDSLC (CCDL - Credit Card Detail View)
    ├── COCRDUPC (CCUP - Credit Card Update)
    ├── COTRN00C (CT00 - Transaction List)
    │   ├── COTRN01C (CT01 - Transaction View)
    │   └── COTRN02C (CT02 - Transaction Add)
    ├── COTRN01C (CT01 - Transaction View)
    ├── COTRN02C (CT02 - Transaction Add)
    ├── CORPT00C (CR00 - Reports)
    ├── COBIL00C (CB00 - Bill Payment)
    └── COPAUS0C (Pending Authorization View)

Batch Programs (JCL-driven):
├── CBACT01C - Account file processing
├── CBACT02C - Card file processing
├── CBACT03C - Cross-reference file processing
├── CBACT04C - Interest calculation (TCATBAL, XREF, ACCT, DISCGRP, TRANSACT)
├── CBCUS01C - Customer file processing
├── CBTRN01C - Daily transaction validation (DALYTRAN → TRANFILE)
├── CBTRN02C - Daily transaction posting (DALYTRAN → TRANFILE, ACCTFILE, TCATBALF)
├── CBTRN03C - Transaction reporting (TRANFILE, CARDXREF → TRANREPT)
├── CBSTM03A - Statement generation
└── CBSTM03B - Statement generation (alternate)
```

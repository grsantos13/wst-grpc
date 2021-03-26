package br.com.gn.order

enum class Status {
    PENDING_APPROVAL,
    PENDING_DOCUMENTATION,
    WORKING_ON,
    FUTURE,
    PENDING_DEPARTURE_CONFIRMATION,
    PENDING_ARRIVAL_CONFIRMATION,
    PENDING_CUSTOMS_CLEARANCE,
    PENDING_SAP_CHECK,
    PENDING_MATTERS,
    PENDING_FINALIZATION,
    CONCLUDED
}

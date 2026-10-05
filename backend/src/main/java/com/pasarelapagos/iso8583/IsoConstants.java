package com.pasarelapagos.iso8583;

public final class IsoConstants {

    private IsoConstants() {
    }

    // MTI
    public static final String MTI_FINANCIAL_REQUEST = "0200";
    public static final String MTI_FINANCIAL_RESPONSE = "0210";

    // Processing Code
    public static final String PROCESSING_CODE_PURCHASE = "000000";

    // Response Codes
    public static final String RESPONSE_APPROVED = "00";
    public static final String RESPONSE_INVALID_TRANSACTION = "12";
    public static final String RESPONSE_INVALID_AMOUNT = "13";
    public static final String RESPONSE_INVALID_ACCOUNT = "14";
    public static final String RESPONSE_INSUFFICIENT_FUNDS = "51";
    public static final String RESPONSE_ACCOUNT_NOT_ACTIVE = "57";

    // Currency
    public static final String CURRENCY_GUATEMALA = "320";
}
package workshop.demo.DomainLayer.Exceptions;

public class ErrorCodes {
    public static final int INVALID_TOKEN = 1001;
    public static final int USER_NOT_FOUND = 1002;
    public static final int WRONG_PASSWORD = 1003;
    public static final int NO_PERMISSION = 1004;
    public static final int STORE_NOT_FOUND = 1005;
    public static final int PRODUCT_NOT_FOUND = 1006;
    public static final int GUEST_NOT_FOUND = 1007;
    public static final int RECEIPT_NOT_FOUND = 1008;
    public static final int CART_NOT_FOUND = 1009;
    public static final int PAYMENT_ERROR = 1010;
    public static final int SUPPLY_ERROR = 1011;
    public static final int SUSPENSION_ALREADY_EXISTS = 1012;
    public static final int USER_NOT_LOGGED_IN = 1013;
    public static final int INVALID_AUCTION_PARAMETERS = 1014;
    public static final int INVALID_BID_PARAMETERS = 1015;
    public static final int INVALID_RANDOM_PARAMETERS = 1016;
    public static final int RANDOM_NOT_FOUND = 1017;
    public static final int RANDOM_FINISHED = 1018;
    public static final int AUCTION_FINISHED = 1019;
    public static final int BID_TOO_LOW = 1020;
    public static final int BID_FINISHED = 1021;
    public static final int DUPLICATE_RANDOM_ENTRY = 1022;
    public static final int INSUFFICIENT_STOCK = 1023;
    public static final int INVALID_RANK = 1024;

    // ✅ NEW ADDED CODES
    public static final int PRODUCT_NOT_AVAILABLE = 1025;
    public static final int INSUFFICIENT_PAYMENT = 1026;
    public static final int USERNAME_ALREADY_USED = 1027;
}

package workshop.demo.DomainLayer.Store;

public final class CouponContext {
    private static final ThreadLocal<String> CODE = new ThreadLocal<>();
    private CouponContext() { }
    public static void set(String code) { CODE.set(code); }
    public static String get()      { return CODE.get(); }
    public static void clear()      { CODE.remove(); }
}

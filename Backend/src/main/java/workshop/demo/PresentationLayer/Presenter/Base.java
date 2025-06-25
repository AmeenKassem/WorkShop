package workshop.demo.PresentationLayer.Presenter;

public class Base {
    //public static String url = "http://10.0.0.6:8080";

    public static String url = "http://localhost:8080";

    // Admin initialization and status
    public static String IS_INITIALIZED = url + "/api/appsettings/isInitialized";
    public static String INIT_SITE = url + "/api/appsettings/init";

    // Admin page path
    public static String ADMIN_INIT_PAGE = "/admin/init"; // Vaadin Route
}

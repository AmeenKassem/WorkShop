package workshop.demo.ApplicationLayer;

public class Response<T> {

    public T data;
    public String errorMsg;

    public Response(T data, String errorMsg) {
        this.data = data;
        this.errorMsg = errorMsg;
    }
    // Getter for data

    public T getData() {
        return data;
    }

    // Getter for errorMsg
    public String getErrorMsg() {
        return errorMsg;
    }
}

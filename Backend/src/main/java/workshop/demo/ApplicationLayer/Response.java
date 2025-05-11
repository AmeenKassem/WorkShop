package workshop.demo.ApplicationLayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Response<T> {

    private T data;
    private String errorMsg;
    private int errNumber;

    public Response(T data, String errorMsg) {
        this.data = data;
        this.errorMsg = errorMsg;
    }

    public Response(T data, String errorMsg, int errNumber) {
        this.data = data;
        this.errorMsg = errorMsg;
        this.errNumber = errNumber;
    }

    // Getters
    public T getData() {
        return data;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public int getErrNumber() {
        return errNumber;
    }

    // Convert this Response object to a JSON string
    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Failed to serialize response\"}";
        }
    }

    
}

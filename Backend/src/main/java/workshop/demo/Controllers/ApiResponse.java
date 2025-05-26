package workshop.demo.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiResponse<T> {

    private T data;
    private String errorMsg;
    private int errNumber = -1;

    public ApiResponse() {
    } // Required for deserialization

    public ApiResponse(T data, String errorMsg) {
        this.data = data;
        this.errorMsg = errorMsg;
    }

    public ApiResponse(T data, String errorMsg, int errNumber) {
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

package workshop.demo.PresentationLayer.Handlers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;

import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.PresentationLayer.View.NotificationView;

public class ExceptionHandlers {

    private static final Map<Integer, String> errorMessages = new HashMap<>();

    static {
        errorMessages.put(ErrorCodes.INVALID_TOKEN, "Invalid session. Please log in again.");
        errorMessages.put(ErrorCodes.USER_NOT_FOUND, "User not found, MUST register first.");
        errorMessages.put(ErrorCodes.WRONG_PASSWORD, "Incorrect password. Please try again.");
        errorMessages.put(ErrorCodes.NO_PERMISSION, "You do not have permission to perform this action.");
        errorMessages.put(ErrorCodes.STORE_NOT_FOUND, "Store could not be found.");
        errorMessages.put(ErrorCodes.PRODUCT_NOT_FOUND, "Product does not exist.");
        errorMessages.put(ErrorCodes.GUEST_NOT_FOUND, "Guest session not found.");
        errorMessages.put(ErrorCodes.RECEIPT_NOT_FOUND, "Receipt not found.");
        errorMessages.put(ErrorCodes.CART_NOT_FOUND, "Your cart is empty or missing.");
        errorMessages.put(ErrorCodes.PAYMENT_ERROR, "There was a problem processing the payment.");
        errorMessages.put(ErrorCodes.SUPPLY_ERROR, "Supply error occurred. Please try again.");
        errorMessages.put(ErrorCodes.SUSPENSION_ALREADY_EXISTS, "User is already suspended.");
        errorMessages.put(ErrorCodes.USER_NOT_LOGGED_IN, "You must be logged in to continue.");
        errorMessages.put(ErrorCodes.INVALID_AUCTION_PARAMETERS, "Invalid auction setup parameters.");
        errorMessages.put(ErrorCodes.INVALID_BID_PARAMETERS, "Invalid bid. Please check your input.");
        errorMessages.put(ErrorCodes.INVALID_RANDOM_PARAMETERS, "Invalid parameters for random draw.");
        errorMessages.put(ErrorCodes.RANDOM_NOT_FOUND, "Random draw not found.");
        errorMessages.put(ErrorCodes.RANDOM_FINISHED, "Random draw has already ended.");
        errorMessages.put(ErrorCodes.AUCTION_FINISHED, "The auction has ended.");
        errorMessages.put(ErrorCodes.BID_TOO_LOW, "Your bid is too low.");
        errorMessages.put(ErrorCodes.BID_FINISHED, "Bidding is closed.");
        errorMessages.put(ErrorCodes.DUPLICATE_RANDOM_ENTRY, "You have already entered this random draw.");
        errorMessages.put(ErrorCodes.INSUFFICIENT_STOCK, "Not enough stock available.");
        errorMessages.put(ErrorCodes.INVALID_RANK, "Invalid rank provided.");
        errorMessages.put(ErrorCodes.PRODUCT_NOT_AVAILABLE, "This product is currently unavailable.");
        errorMessages.put(ErrorCodes.INSUFFICIENT_PAYMENT, "Payment amount is insufficient.");
        errorMessages.put(ErrorCodes.STOCK_NOT_FOUND, "Stock information is missing.");
        errorMessages.put(ErrorCodes.DEACTIVATED_STORE, "This store is currently deactivated.");
        errorMessages.put(ErrorCodes.INSUFFICIENT_ITEM_QUANTITY_TO_RANDOM, "Not enough items to enter the draw.");
        errorMessages.put(ErrorCodes.SUSPENSION_NOT_FOUND, "Suspension record not found.");
        errorMessages.put(ErrorCodes.USERNAME_USED, "This username is already in use.");
        errorMessages.put(ErrorCodes.USER_SUSPENDED, "This user is currently suspended.");
    }

    public static String getErrorMessage(int code) {
        return errorMessages.getOrDefault(code, "An unexpected error occurred (code: " + code + ").");
    }

    //more flexible:
    public static void handleException(Exception e) {
        if (e instanceof HttpClientErrorException httpEx) {
            try {
                String responseBody = httpEx.getResponseBodyAsString();
                ApiResponse errorBody = new ObjectMapper().readValue(responseBody, ApiResponse.class);

                if (errorBody.getErrNumber() != -1) {
                    NotificationView.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
                } else {
                    NotificationView.showError("FAILED: " + errorBody.getErrorMsg());
                }

            } catch (Exception parsingEx) {
                System.err.println("Failed to parse error response: " + parsingEx.getMessage());
                NotificationView.showError("Something went wrong (status " + httpEx.getStatusCode() + ").");
            }

        } else if (e instanceof org.springframework.web.client.UnknownContentTypeException unknownTypeEx) {
            // Handles cases where the server returns HTML instead of JSON
            System.err.println("Unexpected content type: " + unknownTypeEx.getContentType());
            e.printStackTrace();
            NotificationView.showError("Server returned an unexpected response format. Try again later.");

        } else {
            // Fallback: for everything else
            e.printStackTrace();
            System.err.println("Exception type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            NotificationView.showError("HTTP error: " + e.getMessage());
        }
    }

}

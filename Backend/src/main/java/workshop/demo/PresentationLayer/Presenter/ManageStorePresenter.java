package workshop.demo.PresentationLayer.Presenter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Controllers.ApiResponse;
import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.ManageStoreView;
import workshop.demo.PresentationLayer.View.NotificationView;

public class ManageStorePresenter {

    private final ManageStoreView view;
    private final RestTemplate restTemplate;

    public ManageStorePresenter(ManageStoreView view) {
        this.view = view;
        this.restTemplate = new RestTemplate();
    }

    public void fetchStore(String token, int storeId) {
        String url = String.format(
                "http://localhost:8080/api/store/getstoreDTO?token=%s&storeId=%d",
                token,
                storeId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ApiResponse.class
            );

            ApiResponse body = response.getBody();
            System.out.println("Response: " + new ObjectMapper().writeValueAsString(body));

            if (body != null && body.getErrorMsg() == null && body.getErrNumber() == -1) {
                ObjectMapper mapper = new ObjectMapper();
                StoreDTO store = mapper.convertValue(body.getData(), StoreDTO.class);
                view.buildManageUI(store);
            } else if (body != null && body.getErrNumber() != -1) {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            }

        } catch (HttpClientErrorException e) {
            try {
                String responseBody = e.getResponseBodyAsString();
                ApiResponse errorBody = new ObjectMapper().readValue(responseBody, ApiResponse.class);
                if (errorBody.getErrNumber() != -1) {
                    NotificationView.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
                } else {
                    NotificationView.showError("FAILED: " + errorBody.getErrorMsg());
                }
            } catch (Exception parsingEx) {
                NotificationView.showError("HTTP error: " + e.getMessage());
            }

        } catch (Exception e) {
            NotificationView.showError("UNEXPECTED ERROR: " + e.getMessage());
        }
    }

    public void viewStoreReviews(int storeId) {
        if (storeId == -1) {
            NotificationView.showError("FAILED: store not loaded!");
        }
        String url = String.format(
                "http://localhost:8080/api/Review/getStoreReviews?storeId=%d",
                storeId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ApiResponse.class
            );

            ApiResponse body = response.getBody();
            System.out.println("Response: " + new ObjectMapper().writeValueAsString(body));

            if (body != null && body.getErrorMsg() == null && body.getErrNumber() == -1) {
                ObjectMapper mapper = new ObjectMapper();
                List<?> rawList = (List<?>) body.getData();

                List<String> reviews = rawList.stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());

                view.showDialog(reviews);
            } else if (body != null && body.getErrNumber() != -1) {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            }

        } catch (HttpClientErrorException e) {
            try {
                String responseBody = e.getResponseBodyAsString();
                ApiResponse errorBody = new ObjectMapper().readValue(responseBody, ApiResponse.class);
                if (errorBody.getErrNumber() != -1) {
                    NotificationView.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
                } else {
                    NotificationView.showError("FAILED: " + errorBody.getErrorMsg());
                }
            } catch (Exception parsingEx) {
                NotificationView.showError("HTTP error: " + e.getMessage());
            }

        } catch (Exception e) {
            NotificationView.showError("UNEXPECTED ERROR: " + e.getMessage());
        }

    }

    public void fetchOrdersByStore(int storeId) {
        String url = String.format("http://localhost:8080/api/history/getOrdersByStore?storeId=%d", storeId);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ApiResponse<List<OrderDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<List<OrderDTO>>>() {
            }
            );

            ApiResponse<List<OrderDTO>> body = response.getBody();

            if (body != null && body.getErrorMsg() == null && body.getErrNumber() == -1) {
                List<OrderDTO> orders = body.getData();

                List<String> formattedOrders = new ArrayList<>();

                for (OrderDTO order : orders) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("🧾 Order\n");
                    sb.append("User ID: ").append(order.getUserId()).append("\n");
                    sb.append("Store ID: ").append(order.getStoreId()).append("\n");
                    sb.append("Date: ").append(order.getDate()).append("\n");
                    sb.append("Final Price: ").append(order.getFinalPrice()).append("\n");
                    sb.append("Products:\n");

                    for (ReceiptProduct p : order.getProductsList()) {
                        sb.append("- ").append(p.toString()).append("\n");
                    }

                    formattedOrders.add(sb.toString());
                }

                view.showDialog(formattedOrders);
            } else if (body != null && body.getErrNumber() == ErrorCodes.STORE_NOT_FOUND) {
                NotificationView.showError("EMPTY HISTORY you got no orders yet!");
            }

        } catch (HttpClientErrorException e) {
            try {
                String responseBody = e.getResponseBodyAsString();
                ApiResponse errorBody = new ObjectMapper().readValue(responseBody, ApiResponse.class);
                if (errorBody.getErrNumber() != -1) {
                    NotificationView.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
                } else {
                    NotificationView.showError("FAILED: " + errorBody.getErrorMsg());
                }
            } catch (Exception parsingEx) {
                NotificationView.showError("HTTP error: " + e.getMessage());
            }

        } catch (Exception e) {
            NotificationView.showError("UNEXPECTED ERROR: " + e.getMessage());
        }
    }

    public void deactivateStore(int storeId) {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        String url = String.format(
                "http://localhost:8080/api/store/deactivate?storeId=%d&token=%s",
                storeId,
                UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ApiResponse.class
            );

            ApiResponse body = response.getBody();
            if (body != null && body.getErrorMsg() == null && body.getErrNumber() == -1) {
                NotificationView.showSuccess("Store deactivated succesfully");  // success message from backend
            } else if (body != null && body.getErrNumber() != -1) {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            }

        } catch (HttpClientErrorException e) {
            try {
                String responseBody = e.getResponseBodyAsString();
                ApiResponse errorBody = new ObjectMapper().readValue(responseBody, ApiResponse.class);
                if (errorBody.getErrNumber() != -1) {
                    NotificationView.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
                } else {
                    NotificationView.showError("FAILED: " + errorBody.getErrorMsg());
                }
            } catch (Exception parsingEx) {
                NotificationView.showError("HTTP error: " + e.getMessage());
            }

        } catch (Exception e) {
            NotificationView.showError("UNEXPECTED ERROR: " + e.getMessage());
        }
    }

    public List<WorkerDTO> viewEmployees(int storeId) {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (token == null) {
            NotificationView.showError("You must be logged in.");
            return null;
        }

        try {
            String url = String.format(
                    "http://localhost:8080/api/store/viewRolesAndPermissions?storeId=%d&token=%s",
                    storeId,
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<List<WorkerDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<List<WorkerDTO>>>() {
            }
            );

            ApiResponse<List<WorkerDTO>> responseBody = response.getBody();

            if (responseBody != null && responseBody.getErrNumber() == -1) {
                return responseBody.getData() != null ? responseBody.getData() : Collections.emptyList();
            } else {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(responseBody.getErrNumber()));
                return Collections.emptyList();
            }
        } catch (HttpClientErrorException e) {
            try {
                String responseBody = e.getResponseBodyAsString();
                ApiResponse errorBody = new ObjectMapper().readValue(responseBody, ApiResponse.class);
                if (errorBody.getErrNumber() != -1) {
                    NotificationView.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
                } else {
                    NotificationView.showError("FAILED: " + errorBody.getErrorMsg());
                }
            } catch (Exception parsingEx) {
                NotificationView.showError("HTTP error: " + e.getMessage());
            }
            return null;

        } catch (Exception e) {
            NotificationView.showError("UNEXPECTED ERROR: " + e.getMessage());
        }
        return null;
    }
}

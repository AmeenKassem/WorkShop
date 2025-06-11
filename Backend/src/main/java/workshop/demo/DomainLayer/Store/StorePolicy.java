package workshop.demo.DomainLayer.Store;

import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.UserDTO;

import java.util.List;

public interface StorePolicy {
    boolean isSatisfied(UserDTO userDTO, List<ItemStoreDTO> cartDTOS);
    String violationMessage();
}

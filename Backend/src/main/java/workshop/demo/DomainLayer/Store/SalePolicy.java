package workshop.demo.DomainLayer.Store;

import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.UserDTO;

import java.util.List;

public class SalePolicy implements StorePolicy {

    @Override
    public boolean isSatisfied(UserDTO userDTO, List<ItemStoreDTO> cartDTOS) {
        return false;
    }

    @Override
    public String violationMessage() {
        return null;
    }
}


package workshop.demo.UnitTests.StockTest;

import org.junit.jupiter.api.Test;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DTOs.Category;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ProductSearchCriteriaTest {

    @Test
    public void testMatchesForStore_AllFiltersMatch() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );
        item testItem = new item(1, 1, 3, Category.Sports);
        AtomicInteger[] rank = new AtomicInteger[]{new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(1), new AtomicInteger(0), new AtomicInteger(0)};
        testItem.setRank(rank);
        assertFalse(criteria.matchesForStore(testItem));
    }

    @Test
    public void testMatchesForStore_CategoryMismatch() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );        item testItem = new item(1, 1, 3, Category.Clothing);
        assertFalse(criteria.matchesForStore(testItem));
    }

    @Test
    public void testMatchesForStore_MinPriceFails() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );        item testItem = new item(1, 1, 3, Category.Sports);
        assertFalse(criteria.matchesForStore(testItem));
    }

    @Test
    public void testMatchesForStore_MaxPriceFails() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );        item testItem = new item(1, 1, 3, Category.Sports);
        assertFalse(criteria.matchesForStore(testItem));
    }

    @Test
    public void testMatchesForStore_MinRatingFails() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );        item testItem = new item(1, 1, 3, Category.Sports);
        AtomicInteger[] rank = new AtomicInteger[]{new AtomicInteger(1), new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(0)};
        testItem.setRank(rank);
        assertFalse(criteria.matchesForStore(testItem));
    }

    @Test
    public void testMatchesForStore_MaxRatingFails() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );        item testItem = new item(1, 1, 3, Category.Sports);
        AtomicInteger[] rank = new AtomicInteger[]{new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(1)};
        testItem.setRank(rank);
        assertFalse(criteria.matchesForStore(testItem));
    }

    @Test
    public void testProductIsMatch_AllFiltersMatch() {
        Product product = new Product("Fresh Milk", 1, Category.Sports, "Healthy and Cold", new String[]{"cold", "fresh"});
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );        assertTrue(criteria.productIsMatch(product));
    }

    @Test
    public void testProductIsMatch_NameFilterFails() {
        Product product = new Product("Bread", 1, Category.Sports, "Healthy and Cold", new String[]{"cold", "fresh"});
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );        assertTrue(criteria.productIsMatch(product));
    }

    @Test
    public void testProductIsMatch_CategoryMismatch() {
        Product product = new Product("Fresh Milk", 1, Category.Clothing, "Healthy and Cold", new String[]{"cold", "fresh"});
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );        assertFalse(criteria.productIsMatch(product));
    }

    @Test
    public void testProductIsMatch_KeywordFails() {
        Product product = new Product("Fresh Milk", 1, Category.Sports, "Healthy and Cold", new String[]{"sweet", "soft"});
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );        assertTrue(criteria.productIsMatch(product));
    }

    @Test
    public void testSpecificStore() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(null, null, null, 10, null, null, null, null);
        assertTrue(criteria.specificStore());
    }

    @Test
    public void testSpecificCategory() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(null, Category.Clothing, null, null, null, null, null, null);
        assertTrue(criteria.specificCategory());
    }
}

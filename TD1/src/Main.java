import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DBConnection dbConn = new DBConnection();
        DataRetriever dr = new DataRetriever(dbConn);

        System.out.println("----- All categories -----");
        List<Category> cats = dr.getAllCategories();
        cats.forEach(System.out::println);

        System.out.println("\n----- Product list tests -----");
        // page,size tests:
        System.out.println("page=1,size=10");
        dr.getProductList(1,10).forEach(System.out::println);

        System.out.println("\npage=1,size=5");
        dr.getProductList(1,5).forEach(System.out::println);

        System.out.println("\npage=1,size=3");
        dr.getProductList(1,3).forEach(System.out::println);

        System.out.println("\npage=2,size=2");
        dr.getProductList(2,2).forEach(System.out::println);

        System.out.println("\n----- getProductsByCriteria (no pagination) -----");
        // Use helper to parse dates for tests where needed
        Instant d2024_02_01 = LocalDate.parse("2024-02-01").atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant d2024_03_01 = LocalDate.parse("2024-03-01").atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant d2024_01_01 = LocalDate.parse("2024-01-01").atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant d2024_12_01 = LocalDate.parse("2024-12-01").atStartOfDay().toInstant(ZoneOffset.UTC);

        // Tests from spec:
        System.out.println("\nproductName='Dell'");
        dr.getProductsByCriteria("Dell", null, null, null).forEach(System.out::println);

        System.out.println("\nproductName='info' (search)");
        dr.getProductsByCriteria("info", null, null, null).forEach(System.out::println);

        System.out.println("\nproductName='iphone', categoryName='mobile'");
        dr.getProductsByCriteria("iphone", "mobile", null, null).forEach(System.out::println);

        System.out.println("\ncreation range 2024-02-01 to 2024-03-01");
        dr.getProductsByCriteria(null, null, d2024_02_01, d2024_03_01).forEach(System.out::println);

        System.out.println("\nproductName='Samsung', categoryName='bureau'");
        dr.getProductsByCriteria("Samsung", "bureau", null, null).forEach(System.out::println);

        System.out.println("\nproductName='Sony', categoryName='informatique'");
        dr.getProductsByCriteria("Sony", "informatique", null, null).forEach(System.out::println);

        System.out.println("\ncategoryName='audio', creation range 2024-01-01 to 2024-12-01");
        dr.getProductsByCriteria(null, "audio", d2024_01_01, d2024_12_01).forEach(System.out::println);

        System.out.println("\nnull criteria (all products)");
        dr.getProductsByCriteria(null, null, null, null).forEach(System.out::println);

        System.out.println("\n----- getProductsByCriteria (with pagination) -----");
        System.out.println("\nnull criteria page=1 size=10");
        dr.getProductsByCriteria(null, null, null, null, 1, 10).forEach(System.out::println);

        System.out.println("\nproductName='dell' page=1 size=5");
        dr.getProductsByCriteria("dell", null, null, null, 1, 5).forEach(System.out::println);

        System.out.println("\ncategoryName='informatique' page=1 size=10");
        dr.getProductsByCriteria(null, "informatique", null, null, 1, 10).forEach(System.out::println);
    }
}

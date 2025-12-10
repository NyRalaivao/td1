import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private final DBConnection dbConnection;

    public DataRetriever(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name, product_id FROM product_category ORDER BY id";
        try (Connection conn = dbConnection.getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Category c = new Category(rs.getInt("id"), rs.getString("name"));
                categories.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public List<Product> getProductList(int page, int size) {
        List<Product> products = new ArrayList<>();
        if (page < 1 || size < 1) return products;

        int offset = (page - 1) * size;

        // On récupère le produit et une catégorie associée (la première si plusieurs)
        String sql = ""
                + "SELECT p.id, p.name, p.price, p.creation_datetime, "
                + "  (SELECT id FROM product_category pc WHERE pc.product_id = p.id ORDER BY id LIMIT 1) AS c_id, "
                + "  (SELECT name FROM product_category pc WHERE pc.product_id = p.id ORDER BY id LIMIT 1) AS c_name "
                + "FROM product p "
                + "ORDER BY p.id "
                + "LIMIT ? OFFSET ?";

        try (Connection conn = dbConnection.getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, size);
            ps.setInt(2, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int pid = rs.getInt("id");
                    String name = rs.getString("name");
                    double price = rs.getDouble("price");
                    Timestamp ts = rs.getTimestamp("creation_datetime");
                    Instant creation = ts != null ? ts.toInstant() : null;

                    Integer cid = rs.getObject("c_id") != null ? rs.getInt("c_id") : null;
                    String cname = rs.getString("c_name");
                    Category cat = cid != null ? new Category(cid, cname) : null;

                    products.add(new Product(pid, name, price, creation, cat));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    public List<Product> getProductsByCriteria(String productName, String categoryName,
                                               Instant creationMin, Instant creationMax) {
        // Delegate to the paginated version with page/size = fetch all (no pagination)
        return getProductsByCriteria(productName, categoryName, creationMin, creationMax, -1, -1);
    }


    public List<Product> getProductsByCriteria(String productName, String categoryName,
                                               Instant creationMin, Instant creationMax,
                                               int page, int size) {
        List<Product> products = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.id, p.name, p.price, p.creation_datetime, ")
                .append("  (SELECT id FROM product_category pc WHERE pc.product_id = p.id ORDER BY id LIMIT 1) AS c_id, ")
                .append("  (SELECT name FROM product_category pc WHERE pc.product_id = p.id ORDER BY id LIMIT 1) AS c_name ")
                .append("FROM product p ");

        List<Object> params = new ArrayList<>();
        boolean whereAdded = false;

        if (productName != null && !productName.isBlank()) {
            sql.append(!whereAdded ? "WHERE " : "AND ");
            sql.append("p.name ILIKE ? ");
            params.add("%" + productName + "%");
            whereAdded = true;
        }

        if (categoryName != null && !categoryName.isBlank()) {
            sql.append(!whereAdded ? "WHERE " : "AND ");
            sql.append("EXISTS (SELECT 1 FROM product_category pc WHERE pc.product_id = p.id AND pc.name ILIKE ?) ");
            params.add("%" + categoryName + "%");
            whereAdded = true;
        }

        if (creationMin != null) {
            sql.append(!whereAdded ? "WHERE " : "AND ");
            sql.append("p.creation_datetime >= ? ");
            params.add(Timestamp.from(creationMin));
            whereAdded = true;
        }

        if (creationMax != null) {
            sql.append(!whereAdded ? "WHERE " : "AND ");
            sql.append("p.creation_datetime <= ? ");
            params.add(Timestamp.from(creationMax));
            whereAdded = true;
        }

        sql.append("ORDER BY p.id ");

        boolean doPagination = page > 0 && size > 0;
        if (doPagination) {
            sql.append("LIMIT ? OFFSET ? ");
        }

        try (Connection conn = dbConnection.getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            for (Object p : params) {
                if (p instanceof String) {
                    ps.setString(idx++, (String) p);
                } else if (p instanceof Timestamp) {
                    ps.setTimestamp(idx++, (Timestamp) p);
                } else {
                    ps.setObject(idx++, p);
                }
            }

            if (doPagination) {
                int offset = (page - 1) * size;
                ps.setInt(idx++, size);
                ps.setInt(idx++, offset);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int pid = rs.getInt("id");
                    String name = rs.getString("name");
                    double price = rs.getDouble("price");
                    Timestamp ts = rs.getTimestamp("creation_datetime");
                    Instant creation = ts != null ? ts.toInstant() : null;

                    Integer cid = rs.getObject("c_id") != null ? rs.getInt("c_id") : null;
                    String cname = rs.getString("c_name");
                    Category cat = cid != null ? new Category(cid, cname) : null;

                    products.add(new Product(pid, name, price, creation, cat));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }
}

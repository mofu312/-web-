import java.sql.*;

public class SeedBooks {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:data/library.db");

        String[][] books = {
            {"978-7-100-17109-2", "红楼梦", "曹雪芹", "5"},
            {"978-7-100-17110-8", "三国演义", "罗贯中", "5"},
            {"978-7-100-17111-5", "水浒传", "施耐庵", "5"},
            {"978-7-100-17112-2", "西游记", "吴承恩", "5"},
            {"978-7-02-000220-7", "活着", "余华", "3"},
            {"978-7-5321-3800-7", "三体", "刘慈欣", "4"},
            {"978-7-5447-4254-7", "百年孤独", "加西亚·马尔克斯", "3"},
            {"978-7-02-007066-4", "围城", "钱钟书", "3"},
            {"978-7-5321-4632-3", "平凡的世界", "路遥", "4"},
            {"978-7-5447-4608-5", "小王子", "圣埃克苏佩里", "6"}
        };

        PreparedStatement ps = conn.prepareStatement(
            "INSERT OR IGNORE INTO books VALUES (?,?,?,?,?)"
        );

        for (String[] row : books) {
            ps.setString(1, row[0]);
            ps.setString(2, row[1]);
            ps.setString(3, row[2]);
            ps.setInt(4, Integer.parseInt(row[3]));
            ps.setInt(5, Integer.parseInt(row[3]));
            ps.executeUpdate();
        }

        ps.close();
        conn.close();

        System.out.println("Done. 10 books inserted.");
    }
}

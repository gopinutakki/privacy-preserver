package privacychecker;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

	static Connection conn = null;

	public Database() throws ClassNotFoundException, SQLException {
		conn = DatabaseConnection.getConnection();
	}

	public void addURL(String domain, String url, String content)
			throws SQLException {
		Statement stmt = conn.createStatement();
		stmt.executeUpdate("insert into dps (domain, url, content) values ('" + domain + "', '" + url
				+ "', '" + content + "');");
		stmt.close();
	}

	public String getWebsiteData(String website) throws SQLException {
		String content = "";
		Statement stmt = conn.createStatement();
		ResultSet result = stmt.executeQuery("select * from dps where domain='"
				+ website + "'");

		while (result.next()) {
			content += result.getString(3);
		}

		result.close();
		stmt.close();

		return content;
	}

	public void destroy() throws SQLException {
		conn.close();
	}
}

class DatabaseConnection {

	public static Connection getConnection() throws ClassNotFoundException,
			SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection connection = null;
		connection = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/privacy", "root", "root");

		return connection;
	}
}

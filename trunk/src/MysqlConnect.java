import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlConnect {

	String url = "jdbc:mysql://localhost:3306/";
	String dbName = "cemla";
	String driver = "com.mysql.jdbc.Driver";
	String userName = "cemla";
	String password = "cemla";

	private final Connection conn;

	public MysqlConnect() {
		Connection conn = null;
		try {
			Class.forName(this.driver).newInstance();
			conn = DriverManager.getConnection(this.url + this.dbName, this.userName, this.password);
		} catch (final Exception e) {
			e.printStackTrace();
		}

		this.conn = conn;
	}

	public void disconnect() throws SQLException {
		this.conn.close();
	}

	public BigDecimal getUrlId(final String url, final Integer totalRecords) throws SQLException {
		// Search
		final Statement st = this.conn.createStatement();
		final ResultSet res = st.executeQuery("SELECT * FROM Url U WHERE U.url = '" + url + "' AND U.totalRecords = " + totalRecords);
		if (res.next()) {
			return res.getBigDecimal("id");
		}

		return null;
	}

	public void insertUrl(final String url, final Integer totalRecords) throws SQLException {
		if (this.getUrlId(url, totalRecords) == null) {
			// Insert
			final Statement st = this.conn.createStatement();
			st.executeUpdate("INSERT Url (url, totalRecords) VALUES ('" + url + "'," + totalRecords + ")");
		}
	}

	public void insertPassengerRecord(final String surname, final String name, final String age, final String civilStatus, final String profession, final String religion, final String nationality, final String ship, final String departure, final String arrivalDate, final String arrivalPort, final String placeOfBirth, final String url, final Integer totalRecords) throws SQLException {
		BigDecimal urlId = this.getUrlId(url, totalRecords);
		if (urlId == null) {
			this.insertUrl(url, totalRecords);
			urlId = this.getUrlId(url, totalRecords);
		}

		if (!this.checkAlreadyCompleted(urlId, totalRecords)) {
			// Insert
			final Statement st = this.conn.createStatement();
			st.executeUpdate("INSERT PassengerRecord (surname, name, age, civilStatus, profession, religion, nationality, ship, departure, arrivalDate, arrivalPort, placeOfBirth, urlId) VALUES ('" + surname + "', '" + name + "', '" + age + "', '" + civilStatus + "', '" + profession + "', '" + nationality + "', '" + ship + "', '" + departure + "', '" + arrivalDate + "', '" + arrivalPort + "', '" + placeOfBirth + "', " + urlId + ")");
		}
	}

	private boolean checkAlreadyCompleted(final BigDecimal urlId, final Integer totalRecords) throws SQLException {
		final Statement st = this.conn.createStatement();
		final ResultSet res = st.executeQuery("SELECT count(*) c FROM PassengerRecord P WHERE P.urlId = " + urlId);
		if (res.next()) {
			final Integer count = res.getInt("c");
			return count.equals(totalRecords);
		}

		return false;
	}

	public boolean checkAlreadyProcessed(final String url) throws SQLException {
		final Statement st = this.conn.createStatement();
		final ResultSet res = st.executeQuery("SELECT * FROM Url U WHERE U.url = '" + url + "'");
		if (res.next()) {
			return res.getBigDecimal("id") != null;
		}

		return false;
	}

	public void cleanNotCompleted() throws SQLException {
		final Statement st = this.conn.createStatement();
		final ResultSet res = st.executeQuery("SELECT U.id id FROM Url U, PassengerRecord P WHERE U.id = P.urlId AND U.totalRecords <> (SELECT COUNT(1) FROM PassengerRecord P1 WHERE P1.urlId = U.ID)");
		while (res.next()) {
			final BigDecimal id = res.getBigDecimal("id");

			final Statement stDelete = this.conn.createStatement();
			stDelete.executeUpdate("DELETE FROM PassengerRecord WHERE urlId = " + id);
		}

		final Statement stDelete = this.conn.createStatement();
		stDelete.executeUpdate("DELETE FROM Url WHERE id NOT IN (SELECT DISTINCT P.urlId FROM PassengerRecord P) AND totalRecords <> 0");
	}
}
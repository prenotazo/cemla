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

	public void insertUrl(final String url, final Integer totalRecords, final String lastNameInitial, final String day, final String month, final String year) throws SQLException {
		if (this.getUrlId(url, totalRecords) == null) {
			// Insert
			final Statement st = this.conn.createStatement();
			st.executeUpdate("INSERT Url (url, groupUrl, totalRecords) VALUES ('" + url + "', '" + lastNameInitial + day + month + year + "', " + totalRecords + ")");
		}
	}

	public void insertPassengerRecord(final String surname, final String name, final String age, final String civilStatus, final String profession, final String religion, final String nationality, final String ship, final String departure, final String arrivalDate, final String arrivalPort, final String placeOfBirth, final String url, final Integer totalRecords, final String lastNameInitial, final String day, final String month, final String year) throws SQLException {
		BigDecimal urlId = this.getUrlId(url, totalRecords);
		if (urlId == null) {
			this.insertUrl(url, totalRecords, lastNameInitial, day, month, year);
			urlId = this.getUrlId(url, totalRecords);
		}

		if (!this.checkAlreadyCompleted(totalRecords, lastNameInitial, day, month, year)) {
			// Insert
			final Statement st = this.conn.createStatement();
			st.executeUpdate("INSERT PassengerRecord (surname, name, age, civilStatus, profession, religion, nationality, ship, departure, arrivalDate, arrivalPort, placeOfBirth, urlId, groupUrl) VALUES ('" + surname + "', '" + name + "', '" + age + "', '" + civilStatus + "', '" + profession + "', '" + religion + "', '" + nationality + "', '" + ship + "', '" + departure + "', '" + arrivalDate + "', '" + arrivalPort + "', '" + placeOfBirth + "', " + urlId + ", '" + lastNameInitial + day + month + year + "')");
		}
	}

	public boolean checkAlreadyProcessed(final String url) throws SQLException {
		final Statement st = this.conn.createStatement();
		final ResultSet res = st.executeQuery("SELECT * FROM Url U WHERE U.url = '" + url + "'");
		if (res.next()) {
			return res.getBigDecimal("id") != null;
		}

		return false;
	}

	private boolean checkAlreadyCompleted(final Integer totalRecords, final String lastNameInitial, final String day, final String month, final String year) throws SQLException {
		final Statement st = this.conn.createStatement();
		final ResultSet res = st.executeQuery("SELECT count(*) c FROM PassengerRecord P WHERE P.groupUrl = '" + lastNameInitial + day + month + year + "'");
		if (res.next()) {
			final Integer count = res.getInt("c");
			return count.equals(totalRecords);
		}

		return false;
	}

	public void cleanNotCompleted() throws SQLException {
		final Statement st = this.conn.createStatement();
		final ResultSet res = st.executeQuery("SELECT DISTINCT U.groupUrl groupUrl FROM Url U, PassengerRecord P WHERE U.groupUrl = P.groupUrl AND U.totalRecords <> (SELECT COUNT(1) FROM PassengerRecord P1 WHERE P1.groupUrl = U.groupUrl)");
		while (res.next()) {
			final String groupUrl = res.getString("groupUrl");

			final Statement stDelete = this.conn.createStatement();
			stDelete.executeUpdate("DELETE FROM PassengerRecord WHERE groupUrl = '" + groupUrl + "'");
		}

		final Statement stDelete = this.conn.createStatement();

		stDelete.executeUpdate("DELETE FROM Url WHERE id NOT IN (SELECT DISTINCT P.urlId FROM PassengerRecord P) AND totalRecords <> 0");

	}
}
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlConnect {

	// String url = "jdbc:mysql://instance13459.db.xeround.com:6975/";
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

	public BigDecimal getInsertUrl(final String url, final Integer totalRecords, final String lastNameInitial, final String day, final String month, final String year) throws SQLException {
		BigDecimal urlId = null;

		// Search
		Statement st = this.conn.createStatement();
		final ResultSet res = st.executeQuery("SELECT U.id id FROM Url U WHERE U.url = " + this.adaptDB(url) + " AND U.totalRecords = " + totalRecords);
		if (res.next()) {
			urlId = res.getBigDecimal("id");
		}

		// Insert
		if (urlId == null) {
			st = this.conn.createStatement();
			st.executeUpdate("INSERT Url (url, groupUrl, totalRecords) VALUES (" + this.adaptDB(url) + ", " + this.adaptDB(lastNameInitial + day + month + year) + ", " + totalRecords + ")");
			urlId = this.getInsertUrl(url, totalRecords, lastNameInitial, day, month, year);
		}

		return urlId;
	}

	public BigDecimal getInsertPassengerRecord(final String surname, final String name, final String age, final String civilStatus, final String profession, final String religion, final String nationality, final String ship, final String departure, final String arrivalDate, final String arrivalPort, final String placeOfBirth, final String url, final Integer totalRecords, final String lastNameInitial, final String day, final String month, final String year) throws SQLException {
		if (surname.equals(",")) {
			return null;
		}

		// Passenger Record
		BigDecimal prId = null;

		// Search
		Statement st = this.conn.createStatement();
		final ResultSet res = st.executeQuery("SELECT PR.id id FROM PassengerRecord PR WHERE PR.surname = " + this.adaptDB(surname) + " AND PR.name = " + this.adaptDB(name) + " AND PR.age = " + this.adaptDB(age) + " AND PR.civilStatus = " + this.adaptDB(civilStatus) + " AND PR.profession = " + this.adaptDB(profession) + " AND PR.religion = " + this.adaptDB(religion) + " AND PR.nationality = " + this.adaptDB(nationality) + " AND PR.ship = " + this.adaptDB(ship) + " AND PR.departure = " + this.adaptDB(departure) + " AND PR.arrivalDate = " + this.adaptDB(arrivalDate) + " AND PR.arrivalPort = " + this.adaptDB(arrivalPort) + " AND PR.placeOfBirth = " + this.adaptDB(placeOfBirth));
		if (res.next()) {
			prId = res.getBigDecimal("id");
		}

		// Insert
		if (prId == null) {
			// Insert
			st = this.conn.createStatement();
			st.executeUpdate("INSERT PassengerRecord (surname, name, age, civilStatus, profession, religion, nationality, ship, departure, arrivalDate, arrivalPort, placeOfBirth) VALUES (" + this.adaptDB(surname) + ", " + this.adaptDB(name) + ", " + this.adaptDB(age) + ", " + this.adaptDB(civilStatus) + ", " + this.adaptDB(profession) + ", " + this.adaptDB(religion) + ", " + this.adaptDB(nationality) + ", " + this.adaptDB(ship) + ", " + this.adaptDB(departure) + ", " + this.adaptDB(arrivalDate) + ", " + this.adaptDB(arrivalPort) + ", " + this.adaptDB(placeOfBirth) + ")");
			prId = this.getInsertPassengerRecord(surname, name, age, civilStatus, profession, religion, nationality, ship, departure, arrivalDate, arrivalPort, placeOfBirth, url, totalRecords, lastNameInitial, day, month, year);
		}

		// Url
		final BigDecimal urlId = this.getInsertUrl(url, totalRecords, lastNameInitial, day, month, year);

		// Passenger Record - Url
		this.getInsertPassengerRecordUrl(prId, urlId, lastNameInitial + day + month + year);

		return prId;
	}

	private BigDecimal getInsertPassengerRecordUrl(final BigDecimal prId, final BigDecimal urlId, final String groupUrl) throws SQLException {
		BigDecimal pruId = null;

		// Search
		Statement st = this.conn.createStatement();
		final ResultSet res = st.executeQuery("SELECT PRU.id id FROM PassengerRecord_Url PRU WHERE PRU.passengerRecordId = " + this.adaptDB(prId) + " AND PRU.urlId = " + this.adaptDB(urlId) + " AND PRU.groupUrl = " + this.adaptDB(groupUrl));
		if (res.next()) {
			pruId = res.getBigDecimal("id");
		}

		// Insert
		if (pruId == null) {
			st = this.conn.createStatement();
			st.executeUpdate("INSERT PassengerRecord_Url (passengerRecordId, urlId, groupUrl) VALUES (" + this.adaptDB(prId) + ", " + this.adaptDB(urlId) + ", " + this.adaptDB(groupUrl) + ")");
			pruId = this.getInsertPassengerRecordUrl(prId, urlId, groupUrl);
		}

		return pruId;
	}

	private String adaptDB(final String s) {
		if (s == null) {
			return "'-'";
		}

		return "'" + s + "'";
	}

	private BigDecimal adaptDB(final BigDecimal bd) {
		return bd;
	}

	public boolean checkAlreadyCompleted(final String url) throws SQLException {
		// Search
		Statement st = this.conn.createStatement();
		ResultSet res = st.executeQuery("SELECT U.id id FROM Url U WHERE U.url = " + this.adaptDB(url));
		if (res.next()) {
			st = this.conn.createStatement();
			res = st.executeQuery("SELECT DISTINCT COUNT(1) AS amount FROM PassengerRecord_Url PRU, Url U WHERE PRU.urlId = U.id AND U.totalRecords <> (SELECT COUNT(1) FROM PassengerRecord_Url PRU1 WHERE PRU1.groupUrl = U.groupUrl) AND U.url = " + this.adaptDB(url));
			if (res.next()) {
				return res.getBigDecimal("amount").equals(new BigDecimal(0));
			}
		}

		return false;
	}

	public void cleanNotCompleted() throws SQLException {
		// Delete Unlinked Passenger Record
		Statement st = this.conn.createStatement();
		st.executeUpdate("DELETE FROM PassengerRecord WHERE id NOT IN (SELECT DISTINCT PRU.passengerRecordId FROM PassengerRecord_Url PRU)");

		// Delete Unlinked Url
		st = this.conn.createStatement();
		st.executeUpdate("DELETE FROM Url WHERE id NOT IN (SELECT DISTINCT PRU.urlId FROM PassengerRecord_Url PRU) AND totalRecords <> 0");

		// Delete non completed final URLs and all final the related final data.
		final ResultSet res = st.executeQuery("SELECT DISTINCT PRU.passengerRecordId AS passengerRecordId, PRU.urlId AS urlId FROM PassengerRecord_Url PRU, Url U WHERE PRU.urlId = U.id AND U.totalRecords <> (SELECT COUNT(1) FROM PassengerRecord_Url PRU1 WHERE PRU1.groupUrl = U.groupUrl)");
		while (res.next()) {
			final BigDecimal passengerRecordId = res.getBigDecimal("passengerRecordId");
			final BigDecimal urlId = res.getBigDecimal("urlId");

			st = this.conn.createStatement();
			st.executeUpdate("DELETE FROM PassengerRecord_Url WHERE passengerRecordId = " + this.adaptDB(passengerRecordId) + " AND urlId = " + this.adaptDB(urlId));

			st = this.conn.createStatement();
			st.executeUpdate("DELETE FROM PassengerRecord WHERE id = " + this.adaptDB(passengerRecordId) + " AND id NOT IN (SELECT DISTINCT PRU.passengerRecordId FROM PassengerRecord_Url PRU)");

			st = this.conn.createStatement();
			st.executeUpdate("DELETE FROM Url WHERE id = " + this.adaptDB(urlId) + " AND id NOT IN (SELECT DISTINCT PRU.urlId FROM PassengerRecord_Url PRU)");
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
}
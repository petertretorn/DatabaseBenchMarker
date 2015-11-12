import java.sql.Connection;


interface MySQLConnectionFactory {
	Connection createConnection();
}

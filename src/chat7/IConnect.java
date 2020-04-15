package chat7;

public interface IConnect {
	String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
	String ORACLE_URL ="jdbc:oracle:thin://@localhost:1521:orcl";
	
	//DB연결
	void connect(String user, String pass);
	void close();
	
	
	
}

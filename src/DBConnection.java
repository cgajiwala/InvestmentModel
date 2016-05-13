import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

	private static Connection conn;
	
	public static final Connection getInstance(){
		//System.out.println("INSIDE CONNECTION");
		try{
			//System.out.println("INSIDE TRY");
			if(conn == null){
				//System.out.println("INSIDE IF");
				Class.forName("com.mysql.jdbc.Driver");
				conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/InvestmentModel","root", "");
				//System.out.println("CONNECTION CREATED");
				return conn;
			}
		}catch(Exception e){
			System.out.println("*** Exception: " + e.getMessage());
		}
		return conn;
	}
	
	public static final void close(){
		if(conn!=null){
			conn = null;
		}
	}
}

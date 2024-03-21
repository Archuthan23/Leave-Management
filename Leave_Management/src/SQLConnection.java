import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class SQLConnection {
	Scanner scanner = new Scanner(System.in);
	Connection connection = null;
	
	//establish sql connection
	SQLConnection (){
		try {
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/LEAVE_MANAGEMENT","root","root");
		}catch(Exception e) {
			System.out.println(e);
		}
		LocalDate today = LocalDate.now();
		try {
			PreparedStatement st = connection.prepareStatement("Select * from leave_request");
			ResultSet result = st.executeQuery();
			while(result.next()){
				LocalDate endDate = result.getDate("end_date").toLocalDate();
				int request_id = result.getInt("request_id");
				if(today.compareTo(endDate)>=0){
					st = connection.prepareStatement("Delete from leave_request where request_id = ?");
					st.setInt(1, request_id);
					st.executeUpdate();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void closeConnection() {
		scanner.close();
		try {
			connection.close();
		}catch(Exception exception) {
			System.out.println(exception);
		}
		
		
	}
}

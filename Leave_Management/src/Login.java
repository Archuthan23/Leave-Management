import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Login extends SQLConnection{
	Scanner sc = new Scanner(System.in);
	String userName;
	String password;
	int userId;
	
	// fun to get username and password
	String getLoginDetails() {
		System.out.println("Enter the Username:");
		userName = sc.nextLine();
		System.out.println("Enter the Password:");
		password = sc.nextLine();
		return LoginValidation();
	}
	
	//function to validate username and password
	String LoginValidation() {
		String role = null;
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM User WHERE username = ? && password = ?");
			statement.setString(1, userName);
			statement.setString(2, password);
			ResultSet resultSet = statement.executeQuery();
			if(resultSet.next()) {
				role = resultSet.getString("role");
				userId = resultSet.getInt("user_id");
			}else {
				System.out.println("Enter Valid Login Details");
				return getLoginDetails();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return role;
	}
}

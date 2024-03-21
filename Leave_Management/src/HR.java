import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

//import javax.swing.text.html.HTMLDocument.HTMLReader.PreAction;

public class HR extends SQLConnection {
	int request_id;
	int user_id;
	LocalDate startDate;
	LocalDate endDate;
	String status;
	float noOfDays;
	String leave_type;
	String userName;
	
	HR(int u_id){
		user_id = u_id;
	}
	
	void generateMail(String review) {
		String empMail ="";
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT u.email FROM Leave_request lr JOIN User u ON lr.user_id = u.user_id WHERE lr.request_id = ?");
			statement.setInt(1, request_id);
			ResultSet result = statement.executeQuery();
			while(result.next()) {
				empMail = result.getString("email");
			}
		}catch(Exception exception) {
			exception.printStackTrace();
		}
		try {
        	EmailSender emailSender = new EmailSender("sriharishr105@gmail.com", "czwympvajwawowbh", "smtp.gmail.com", 587);
        	MimeMessage message = new MimeMessage(emailSender.getSession());
        	PreparedStatement st = connection.prepareStatement("Select username from User where user_id = ?");
        	st.setInt(1, user_id);
        	ResultSet result = st.executeQuery();
        	if(result.next()) {
        		userName = result.getString("username");
        	}
        	String MessageContent = "<html lang=en>"
        			+ "<head>"
        			+ "  <meta charset=\"UTF-8\" />"
        			+ "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />"
        			+ "  <link rel=\"stylesheet\" href=\"style.css\" />"
        			+ "  <title>Browser</title>"
        			+ "</head>"
        			+ "<body>"
        			+ "  <p>Your Request for leave from " + startDate + " to " +endDate+" has been " + review +"</p>"
        			+ "  <br>"
        			+ "    <p> Best Regards </p>"
        			+ "    <p>"+ userName +"</p>"
        			+ "  <script src=\"script.js\"></script>\r\n"
        			+ "</body>\r\n"
        			+ "\r\n"
        			+ "</html>";
        	message.setContent(MessageContent, "text/html");        	
        	message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress("archuthan_a@solartis.com"));
        	emailSender.sendEmail(empMail, "Subject", message);
        } catch(Exception e) {
        	System.out.println(e.getMessage());
        }
	}
	
	//function to fetch all records in leave_request table
	void ViewLeaveRequest() {
		System.out.println("==========================================================================");
		System.out.println("\t\t\tLeaveRequests");
		System.out.printf("%-5s %-10s %-12s %-12s %-11s %-12s %-6s%n","S.No","Username","Type","From","TO","No.of.Days","Status");
		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery("Select u.username,lr.* from User u join Leave_request lr on u.user_id = lr.user_id where status = 'pending'");
			while(result.next()) {
				request_id = result.getInt("request_id");
				user_id = result.getInt("user_id");
				startDate = result.getDate("start_date").toLocalDate();
				endDate = result.getDate("end_date").toLocalDate();
				status = result.getString("status");
				noOfDays = result.getFloat("noOfDays");
				leave_type = result.getString("leave_type");
				userName = result.getString("username");
				DisplayViewRequest();
			}
		}catch(Exception e) {
			System.out.println(e);
		}
		System.out.println("==========================================================================");
	}
	
	//function to format display of leave request
	void DisplayViewRequest() {
		System.out.printf("%-5d %-12s %-10s %-12s %-12s %-11.1f %-10s%n",request_id,userName,leave_type,startDate,endDate,noOfDays,status);
	}
	
	//function to approve or reject leave
	void LeaveReview() {
		System.out.println("Enter the request number to approve or reject");
		int request = scanner.nextInt();
		String sqlStatement = "Select u.username,lr.* from User u join Leave_request lr on u.user_id = lr.user_id where status = 'pending'";
		InputValidation inputValidation = new InputValidation();
		//to validate request id
		if(!inputValidation.checkRequestId(request,sqlStatement)) {
			System.out.println("Enter a valid request_id");
			LeaveReview();
		}

		//fetch details of the entered request_id
		try {
			PreparedStatement st = connection.prepareStatement("Select * from Leave_request where request_id = ?");
			st.setInt(1,request);
			ResultSet result = st.executeQuery();
			while(result.next()) {
				request_id = result.getInt("request_id");
				user_id = result.getInt("user_id");
				startDate = result.getDate("start_date").toLocalDate();
				endDate = result.getDate("end_date").toLocalDate();
				status = result.getString("status");
				noOfDays = result.getInt("noOfDays");
				leave_type = result.getString("leave_type");
				DisplayViewRequest();
				System.out.println("1.Approve\n2.Reject");
				int review = scanner.nextInt();
				if(review == 1) {
					st = connection.prepareStatement("Update Leave_request set status = 'approved' where request_id = ?");
					st.setInt(1, request_id);
					st.executeUpdate();
					generateMail("approved");
					
					if(leave_type.equals("SL")){
						PreparedStatement statement = connection.prepareStatement("Update User set sick_leave_count = sick_leave_count - ? where user_id = ?");
						statement.setInt(1, (int)noOfDays);
						statement.setInt(2, user_id);
						statement.executeUpdate();
					}else if(leave_type.equals("CL")){
						PreparedStatement statement = connection.prepareStatement("Update User set casual_leave_count = casual_leave_count - ? where user_id = ?");
						statement.setInt(1, (int)noOfDays);
						statement.setInt(2, user_id);
						statement.executeUpdate();
					}

				}else if(review == 2) {
					st = connection.prepareStatement("Update Leave_request set status = 'rejected' where request_id = ?");
					st.setInt(1, request_id);
					st.executeUpdate();
					generateMail("rejected");
				}
				
				
			}
		}catch(Exception e) {
			e.printStackTrace();;
		}
	}
}

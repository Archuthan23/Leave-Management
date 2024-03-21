import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.mysql.cj.x.protobuf.MysqlxPrepare.Prepare;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Employee extends SQLConnection {
	String name;
	LocalDate startDate;
	LocalDate endDate;
	String status="pending";	
	String leaveType;
	String userName;
	float noOfDays;
	int userId;
	int requestId;
	DayOfWeek sDay;
	DayOfWeek eDay;
	Employee(int userId){
		this.userId = userId;
	}
	
	void generateMail() {
		
		InputValidation inputValidation = new InputValidation();
		String hrMail = scanner.next();
		if(!inputValidation.checkMailAddress(hrMail)) {
			System.out.println("Enter a valid mail address");
			generateMail();
		}
		
		try {
			
        	EmailSender emailSender = new EmailSender("sriharishr105@gmail.com", "czwympvajwawowbh", "smtp.gmail.com", 587);
        	MimeMessage message = new MimeMessage(emailSender.getSession());
        	PreparedStatement statement = connection.prepareStatement("Select username from User where user_id = ?");
        	statement.setInt(1, userId);
        	ResultSet result = statement.executeQuery();
        	if(result.next()) {
        		userName = result.getString("username");
        	}
        	String messageContent = "<html lang=en>"
        			+ "<head>"
        			+ "  <meta charset=\"UTF-8\" />"
        			+ "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />"
        			+ "  <link rel=\"stylesheet\" href=\"style.css\" />"
        			+ "  <title>Browser</title>"
        			+ "</head>"
        			+ "<body>"
        			+ "  <p>Hi I am requesting for leave from "+ startDate + " to " +endDate+" for "+ noOfDays + " as " +leaveType+ "</p>"
        			+ "  <br>"
        			+ "    <p> Best Regards </p>"
        			+ "    <p>"+ userName +"</p>"
        			+ "  <script src=\"script.js\"></script>\r\n"
        			+ "</body>\r\n"
        			+ "\r\n"
        			+ "</html>";
        	message.setContent(messageContent, "text/html");        	
        	message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(hrMail));
        	emailSender.sendEmail(hrMail, "Subject", message);
        } catch(Exception e) {
        	System.out.println("Failed to send Mail: "+e.getMessage());
        }
	}

	//function to View leave status
	void viewRequestStatus() {
		InputValidation inputValidation = new InputValidation();
		System.out.println("=======================================================================");
		System.out.println("\t\t\tLeaveRequests");
		System.out.printf("%-12s %-10s %-12s %-12s %-11s %-10s%n","request id","Type","From","TO","No.of.Days","Status");
		try {
			PreparedStatement st = connection.prepareStatement("Select * from leave_request where user_id = ?");
			st.setInt(1, userId);
			ResultSet result = st.executeQuery();
				while(result.next()) {
					requestId = result.getInt("request_id");
					userId = result.getInt("user_id");
					startDate = result.getDate("start_date").toLocalDate();
					endDate = result.getDate("end_date").toLocalDate();
					status = result.getString("status");
					noOfDays = result.getFloat("noOfDays");
					leaveType = result.getString("leave_type");
					displayViewRequest();
					
				}
		}catch (Exception e) {
			System.out.println(e);
			System.out.println("You have not applied any leave");
        }
		System.out.println("======================================================================");
		inputValidation.deleteRejectedLeave();
	}
	
	
	//function to display leave details
	void displayViewRequest() {
		
		System.out.printf("%-12d %-10s %-12s %-12s %-11.1f %-10s%n",requestId,leaveType,startDate,endDate,noOfDays,status);
	}
	
	//function to get leaveDetails
	void getLeaveDetails() {
		
		InputValidation inputValidation = new InputValidation();
		System.out.print("Enter the start date (YYYY-MM-DD):");
        String stDate = scanner.nextLine();
        try {
        	startDate = LocalDate.parse(stDate);
        	sDay = startDate.getDayOfWeek();
        }catch (Exception e) {
        	//System.out.println(e);
        	System.out.println("Enter Valid date or in the correct format");
        	getLeaveDetails();
        	return;
        }
        System.out.print("Enter the end date (YYYY-MM-DD):");
        String enDate = scanner.nextLine();
        try {
        	endDate = LocalDate.parse(enDate);
        	eDay = endDate.getDayOfWeek();
        }catch (Exception e) {
        	//System.out.println(e);
        	System.out.println("Enter Valid date or in the correct format");
        	getLeaveDetails();
        	return;
        }
        if(!inputValidation.checkPastDate(startDate)) {
        	System.out.println("You have entered a date that is in the past");
        	getLeaveDetails();
			return;
		}
        
        
        //Check if the given dates are valid or not
        int compare = endDate.compareTo(startDate);
		if (compare < 0) {
			//If the given end date is less than the end date
            System.out.println("Enter valid Dates");
            getLeaveDetails();
            return;
        } else if (compare > 0) {
        	Long start = startDate.toEpochDay();
            Long end = endDate.toEpochDay();
            noOfDays = (int)(end-start)+1;
			//If there is already a leave request in the requested date
			HashMap<LocalDate,LocalDate> local = inputValidation.checkExistingLeaveRequest(userId,startDate,endDate);
			if(local.get(endDate) != null) {
				if(local.get(endDate).equals(startDate)) {
					return;
				}
			}
			for(Map.Entry<LocalDate, LocalDate> entry:local.entrySet()) {
				startDate = entry.getKey();
				endDate = entry.getValue();
			}


			//If the given dates are valid
        	LocalDate tempSDate = startDate;
        	LocalDate tempEDate = endDate.plusDays(1);
			int leaveDays = 0;
        	//If the date fall under sunday, saturday or any holidays
        	while(!tempSDate.equals(tempEDate)) {
        		if(!inputValidation.validateLeaveRequest(tempSDate)){
        			leaveDays++;
        		}
        		tempSDate = tempSDate.plusDays(1);
        	}
        	
        	//calculating noOfDays excluding holidays
        	if((noOfDays-leaveDays) <= 0) {
        		System.out.println("The date you have entered falls on holidays");
        		return;
        	}else {
        		noOfDays = noOfDays-leaveDays;
        	}

        } else {
        	//If both the dates are equal
        	//checks if the date fall under sunday, saturday or any holidays
        	if(!inputValidation.validateLeaveRequest(startDate)){
    			System.out.println("Enter Valid dates\n");
        		getLeaveDetails();
                return;
    		}
        	System.out.println("Do you want to apply for full day or half a day");
        	System.out.println("1.Full Day\n2.Half Day");
        	int opt = scanner.nextInt();
        	switch(opt){
        	case 1:
        		noOfDays = 1.0f;
        		break;
        	case 2:
        		noOfDays = 0.5f;
        		break;
        	}
           
        }

		//input for type of leave
        System.out.println("Enter the type of leave SL/CL/WFH/LOP");
        leaveType = scanner.next();

		//check if the leave type entered is valid or not
		if(!leaveType.equals("SL") && !leaveType.equals("CL") && !leaveType.equals("LOP") && !leaveType.equals("WFH")){
			System.out.println("Enter a valid leave type\n");
			getLeaveDetails();
			return;
		}

        //Check the availablity of casual leave
        if(leaveType.equals("CL")) {
        	if(inputValidation.checkCasualLeave(userId,noOfDays)) {
        		System.out.println("You don't have enough casual leave left do you like to apply on loss of pay");
        		System.out.println("1.Yes\n2.No");
        		int choice = scanner.nextInt();
        		switch(choice) {
        		case 1:
        			leaveType = "LOP";
        			break;
        		case 2:
        			return;
        		}
        	}
        }else if(leaveType.equals("SL")) {
        	//Check the availablity of sick leave
        	if(inputValidation.checkSickLeave(userId,noOfDays)) {
        		System.out.println("You don't have enough sick leave left do you like to apply on loss of pay");
        		System.out.println("1.Yes\n2.No");
        		int choice = scanner.nextInt();
        		switch(choice) {
        		case 1:
        			leaveType = "LOP";
        			break;
        		case 2:
        			return;
        		}
        	}
        }
        generateMail();
        LeaveRequest();
	}
	
	
	
	
	//function to send leave request
	void LeaveRequest(){
		try {
			PreparedStatement st = connection.prepareStatement("insert into Leave_request(user_id,start_date,end_date,status,noOfDays,leave_type) values (?,?,?,?,?,?)");
			st.setInt(1,userId);
			st.setDate(2, java.sql.Date.valueOf(startDate));
			st.setDate(3, java.sql.Date.valueOf(endDate));
			st.setString(4,status);
			st.setFloat(5,noOfDays);
			st.setString(6,leaveType);
			st.executeUpdate();
			System.out.println("LeaveRequest successful");
		} catch(Exception exception) {
			System.out.println(exception);
		}
	}

	//function to cancel leave request
	void cancelLeaveRequest(){
		System.out.println("Enter the request id of the leave you want to cancel");
		int request = scanner.nextInt();
		String sqlStatement = "select * from leave_request where user_id =1 and status <> 'rejected'";
		InputValidation inputValidation = new InputValidation();
		if(!inputValidation.checkRequestId(request,sqlStatement)) {
			System.out.println("Enter a valid request_id");
			cancelLeaveRequest();
		}
		try{
			PreparedStatement  st = connection.prepareStatement("select request_id from leave_request");
			ResultSet result = st.executeQuery();
			ArrayList<Integer> req_id = new ArrayList<>(); 
			while(result.next()){
				req_id.add(result.getInt("request_id"));
			}
			if(!req_id.contains(requestId)){
				System.out.println("Enter valid request id");
				cancelLeaveRequest();
			}
			st = connection.prepareStatement("Delete from leave_request where request_id = ?");
			st.setInt(1, requestId);
			st.executeUpdate();
			System.out.println("Leave successfuly cancelled");
		}catch(Exception exception){
			System.out.println(exception.getMessage());
		}
	}
	
	void displayCancelRequest() {
		InputValidation inputValidation = new InputValidation();
		System.out.println("=======================================================================");
		System.out.println("\t\t\tLeaveRequests");
		System.out.printf("%-12s %-10s %-12s %-12s %-11s %-10s%n","request id","Type","From","TO","No.of.Days","Status");
		try {
			PreparedStatement st = connection.prepareStatement("Select * from leave_request where user_id = ? and status <> 'rejected'");
			st.setInt(1, userId);
			ResultSet result = st.executeQuery();
				while(result.next()) {
					requestId = result.getInt("request_id");
					userId = result.getInt("user_id");
					startDate = result.getDate("start_date").toLocalDate();
					endDate = result.getDate("end_date").toLocalDate();
					status = result.getString("status");
					noOfDays = result.getFloat("noOfDays");
					leaveType = result.getString("leave_type");
					displayViewRequest();
				}
		}catch (Exception exception) {
			System.out.println(exception);
			System.out.println("You don't have any leave request to cancel");
        }
		System.out.println("=======================================================================");
	}
	
	void displayLeaveCount() {
		int sickLeaveCount = 0;
		int casualLeaveCount = 0;
		try{
			PreparedStatement statement = connection.prepareStatement("select sick_leave_count,casual_leave_count from User where user_id = ?");
			statement.setInt(1,userId);
			ResultSet result = statement.executeQuery();
			while(result.next()) {
				sickLeaveCount = result.getInt("sick_leave_count");
				casualLeaveCount = result.getInt("casual_leave_count");
			}
		}catch(Exception exception) {
			System.out.println(exception);
		}
		System.out.println("Sick leave: "+sickLeaveCount);
		System.out.println("Casual leave: "+casualLeaveCount);
	}
}

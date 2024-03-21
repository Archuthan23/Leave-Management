import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class InputValidation extends SQLConnection {
	
	//checks if the entered date is in past
	boolean checkPastDate(LocalDate startDate) {
		if((startDate.compareTo(LocalDate.now())) < 0){
			return false;
		}
		return true;
	}
	
	
	//checks if there is an existing Leave in the requested date
	HashMap<LocalDate,LocalDate> checkExistingLeaveRequest(int userId,LocalDate startDate,LocalDate endDate) {
		HashMap<LocalDate,LocalDate> datesMap = new HashMap<>();
		try{
			PreparedStatement st = connection.prepareStatement("Select * from Leave_request where user_id = ?");
			st.setInt(1,userId);
			ResultSet  result = st.executeQuery();
			HashMap<LocalDate,LocalDate> dates = new HashMap<>();
			while(result.next()){
				LocalDate sDate = result.getDate("start_date").toLocalDate();
				LocalDate eDate = result.getDate("end_date").toLocalDate();
				if(startDate.compareTo(sDate) >= 0 && startDate.compareTo(eDate) <=0){
					if(endDate.compareTo(sDate) >=0 && endDate.compareTo(eDate)<=0){
						System.out.println("You have already applied leave for the given dates");
						datesMap.put(endDate,startDate);
						return datesMap;
					}
					else{
						System.out.println("You have already applied leave from " + startDate + " to " + eDate);
						System.out.println("1.Request leave from " + eDate.plusDays(1) + " to " + endDate + "\n2.exit");
						int opt = scanner.nextInt();
						switch(opt){
							case 1:
							datesMap.put(startDate = eDate.plusDays(1), endDate);
							break;
							case 2:
							datesMap.put(endDate,startDate);
							return datesMap;
							default:
							datesMap.put(endDate,startDate);
							return datesMap;
						}
						break;
					}
				}else if(startDate.isBefore(sDate)){
					if(endDate.compareTo(sDate) >=0 && endDate.compareTo(eDate)<=0){
						System.out.println("You have already applied leave from " + sDate + " to " + endDate);
						System.out.println("1.Request leave from " + startDate + " to " + sDate.minusDays(1) + "\n2.exit");
						int opt = scanner.nextInt();
						switch(opt){
							case 1:
							datesMap.put(startDate, endDate = sDate.minusDays(1));
							break;
							case 2:
								datesMap.put(endDate,startDate);
								return datesMap;
							default:
								datesMap.put(endDate,startDate);
								return datesMap;
						}
						break;
					}else if(endDate.compareTo(eDate) >0) {
						System.out.println("You have already requested for a leave that lies in the interval of the current request");
						datesMap.put(endDate,startDate);
						return datesMap;
					}
				}
			}
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		return datesMap;
	}
	
	//function to validate leave if it falls on holidays
	boolean validateLeaveRequest(LocalDate st) {
		ArrayList<LocalDate> holidays = new ArrayList<>(Arrays.asList(
			    LocalDate.of(2024, 1, 26),  
			    LocalDate.of(2024, 3, 29),  
			    LocalDate.of(2024, 5, 1),   
			    LocalDate.of(2024, 8, 15),  
			    LocalDate.of(2024, 9, 7),   
			    LocalDate.of(2024, 10, 2), 
			    LocalDate.of(2024, 10, 31), 
			    LocalDate.of(2024, 11, 29), 
			    LocalDate.of(2024, 12, 25)  
			));
		//Checks if an employee applies leave for a finished Date
		
		//Check if the Date falls on sunday or saturday
		if(st.getDayOfWeek() == DayOfWeek.SUNDAY ||st.getDayOfWeek() ==  DayOfWeek.SATURDAY) {
        	return false;
        }
		//Check if the date falls on holidays
		for(LocalDate dt:holidays) {
			 if(st.equals(dt)){ return false; }
		}
		
		
		return true;
	}
	
	//check for availablity of casual leave
	boolean checkCasualLeave(int user_id,float noOfDays) {
		int casual_leave = 0;
		int sick_leave = 0;
		try {
    		PreparedStatement st = connection.prepareStatement("Select * from User where user_id = ?");
			st.setInt(1, user_id);
			ResultSet result = st.executeQuery();
			if(result.next()) {
				casual_leave = result.getInt("casual_leave_count");
			}
    	}catch(Exception e) {
    		System.out.println(e);
    	}
    	if(casual_leave < noOfDays) {
    		return true;
    	}else {
    		return false;
    	}
	}
	
	//check for availablity of sick leave
	boolean checkSickLeave(int user_id,float noOfDays) {
		int sick_leave = 0;
		try {
    		PreparedStatement st = connection.prepareStatement("Select * from User where user_id = ?");
			st.setInt(1, user_id);
			ResultSet result = st.executeQuery();
			if(result.next()) {
				sick_leave = result.getInt("sick_leave_count");
			}
    	}catch(Exception e) {
    		System.out.println(e);
    	}
    	if(sick_leave < noOfDays) {
    		return true;
    	}else {
    		return false;
    	}
	}
	
	//function to validate request number
	boolean checkRequestId(int request,String sqlStatement) {
		try{
			PreparedStatement st = connection.prepareStatement(sqlStatement);
			ResultSet result = st.executeQuery();
			ArrayList<Integer> req_id = new ArrayList<>(); 
			while(result.next()){
				req_id.add(result.getInt("request_id"));
			}
			if(req_id.contains(request)){
				return true;
			}else {
				return false;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	//check if the given address is in the database or not
	boolean checkMailAddress(String email) {
		try {
			PreparedStatement statement = connection.prepareStatement("Select email from User");
			ResultSet result = statement.executeQuery();
			ArrayList<String> emails = new ArrayList<>();
			while(result.next()) {
				emails.add(result.getString("email"));
			}
			if(emails.contains(email)) {
				return true;
			}else {
				return false;
			}
			}catch(Exception exception) {
			exception.printStackTrace();
		}
		return true;
	}
	
	void deleteRejectedLeave(){
		try {
			PreparedStatement statement = connection.prepareStatement("delete from leave_request where user_id = 1 and status = 'rejected'");
		}catch(Exception exception) {
			exception.printStackTrace();
		}
	}
	
}

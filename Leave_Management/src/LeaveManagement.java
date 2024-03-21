import java.util.Scanner;



public class LeaveManagement {
	public static void main(String[] args) throws ClassNotFoundException {
		Scanner scanner = new Scanner(System.in);
		String loginStatus = "Logged In";
		SQLConnection sqlConnection = new SQLConnection();
		while(loginStatus == "Logged In") {
			Login lg = new Login();
			String role = lg.getLoginDetails();
			switch(role) {
			case "Emp":
				Employee emp = new Employee(lg.userId);
				System.out.println("1.View your Requests\n2.Request leave\n3.Cancel leave request\n4.View leave balance");
				//emp.generateMail();
				int choice = scanner.nextInt();
				while(choice ==1 || choice ==2 || choice ==3 || choice == 4) {
					switch(choice) {
					case 1:
						emp.viewRequestStatus();
						break;
					case 2:
						emp.getLeaveDetails();
						break;
					case 3:
						emp.displayCancelRequest();
						emp.cancelLeaveRequest();
						break;
					case 4:
						emp.displayLeaveCount();
						break;
					}
					System.out.println("1.View your leave requests\n2.Request for Leave\n3.Cancel leave request\n4.View leave balance\n5.logout");
					choice = scanner.nextInt();
					scanner.nextLine();
				}
				break;
			case "HR":
				HR hr = new HR(lg.userId);
				System.out.println("Welcome HR");
				hr.ViewLeaveRequest();
				System.out.println("1.Review Leave Request\n2.Logout");
				int hrLoginStatus = scanner.nextInt();
				while(hrLoginStatus == 1) {
					hr.ViewLeaveRequest();
					hr.LeaveReview();
					System.out.println("1.Review Leave Request\n2.Logout");
					hrLoginStatus = scanner.nextInt();
				}
				
				break;
			}
			System.out.println("1.login again\n2.logout");
			int loginstatus = scanner.nextInt();
			if(loginstatus == 1) {
				continue;
			}else if(loginstatus == 2) {
				loginStatus = "Logged out";
			}else {
				System.out.println("wrong option");
			}
		}
		scanner.close();
		sqlConnection.closeConnection();
	}
	
}


CREATE DATABASE LEAVE_MANAGEMENT;
use LEAVE_MANAGEMENT;
CREATE TABLE User (user_id int auto_increment primary key, username varchar(50),password varchar(50),role varchar(50),casual_leave_count int default 12,sick_leave_count int default 12,email varchar(50));
Create table Leave_request (request_id int auto_increment primary key,user_id int,start_date date,end_date date,status varchar(50),noOfDays float,leave_type varchar(50),foreign key(user_id) references User(user_id));
insert into User(username,password,role,email) values("Sivaprasad","123","Emp","sivaprasad_s@solartis.com");
insert into User(username,password,role,email) values("Archuthan","123","Emp","archuthan_a@solartis.com");
insert into User(username,password,role,casual_leave_count,sick_leave_count,email) values("Kishore","123","HR",0,0,"ragavi_r@solartis.com");
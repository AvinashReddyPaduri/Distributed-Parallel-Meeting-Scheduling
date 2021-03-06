# Distributed-Parallel-Meeting-Scheduling
With the rapid adoption of location-enabled smart phones and the proliferation of mobile Internet services, we have seen the emergence of a large number of applications that can sense and share users’ location information with others. One objective of our study was to see to what extent this might be the case for location sharing. Our results are based on tracking users over a certain period of time and collecting detailed information about their willingness to share their locations with others. This project uses Google API with which one must be able to precisely locate a group of employees of a company/team so that it is possible to schedule the meeting at one of the hotels near to the central location of the selected employees which can minimize the cost (time/fuel) of travel of every other individual of the peer group.
# Scope
Consider scheduling an appointment with the employees working in different places and want to conduct a meeting as soon as possible in n good position such that most of the people can make it to the meeting without much travelling, then this android application works good in such a scenario. This application can be modified/extended for conducting parties and varity of tasks, for now it only considers scheduling meetings for employees.
# Architecture
This application is divided as:
1) Client Side Application: This is an Android application with GUIs for use to both employees and managers
2) Application Server: The application server is responsible for all the CRUD (create, read, update and delete) operations on the application database. It stores the employee’s or manager’s details on registration, deletes the details of the employee’s or manager’s on deregister, updates the employee’s location and reads the clients locations when manager wants to schedule the meeting.
3) Application Database: Application Database (MySql) is used to store the details of the employees and managers persistently.
4) GCMServer: The GCM Server is used to get the device registration id or GCM id for employees. This id is used to send the notification to the employees by the manager. From client side application, we have used methods of Google APIs (gcm.jar in lib folder) to get the GCM id and to send push notification.
# Client Side Application
I) UML Diagrams of Client Side Application
  1) Class Diagram:
  <img src="./Class Diagram.jpg">
  2) Sequence Diagram for Employee:
  <img src="./Sequence diagram for employee.png">
  3) Sequence Diagram for Manager:
<img src="./Sequence diagram for manager.jpg">

II) Source Code for Client Side Application (Android Application): https://github.com/AvinashReddyPaduri/Distributed-Parallel-Meeting-Scheduling/tree/master/src/com/example/project_samples

III) Required Libraries for Android Application
https://github.com/AvinashReddyPaduri/Distributed-Parallel-Meeting-Scheduling/tree/master/libs

IV) Setting Up Required Permissions for Android Application (Using Manifest File)
https://github.com/AvinashReddyPaduri/Distributed-Parallel-Meeting-Scheduling/blob/master/AndroidManifest.xml

V) Software Installation and Usage (Java and Adt Bundle) to Develop Android Application
https://github.com/AvinashReddyPaduri/Distributed-Parallel-Meeting-Scheduling/blob/master/Software%20installation%20and%20usage.pdf

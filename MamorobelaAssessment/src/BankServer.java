package src;

import java.io.*;

import java.net.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.awt.*;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

/**
 * @author Sello Mamorobela
 * 
 * Class BankServer server that implements Runnable
 * 
 */

public class BankServer extends JFrame implements Runnable {

	static InetAddress inetAddress;
	private boolean clientRegistered;
	
	// Text area for displaying contents
	private static JTextArea jta;
	
	//IO variables
	int reciveAccNum;
	double reciveRate; 
	double reciveYears;
	double reciveAmt ;
	double totalAmount;
	double monthlyPayment;
	int number;
	String firstName;
	String lastName;
	
	Socket socket;
	
	public BankServer(Socket socket){
		this.socket = socket;
	}
	
	/**
	 * class constructor
	 */
	public BankServer(){
		jta = new JTextArea();
		jta.setEditable(false);
		DefaultCaret c = (DefaultCaret)jta.getCaret();
		c.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		// Place text area on the frame
	    setLayout(new BorderLayout());
	    add(new JScrollPane(jta), BorderLayout.CENTER);

	    setTitle("Server");
	    setSize(500, 300);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setVisible(true); // It is necessary to show the frame here!
		
	}

	public static void main(String[] args) throws ClassNotFoundException {
		
		ServerSocket serverSocket;
		int port = Integer.parseInt(args[0]);

		new BankServer();
		
		//setting up connections and threads
	 try {
			// Create a server socket
		 	Class.forName("com.mysql.jdbc.Driver");
		 	serverSocket = new ServerSocket(port);
			inetAddress = serverSocket.getInetAddress();
			System.out.println("Listening " + "\n");
			jta.append("Server started at " + new Date() + '\n' + " Listening on port: " + port + "\n");

			while (true) {

				Socket cSocket = serverSocket.accept();							// Connect a new client
				inetAddress = cSocket.getInetAddress();							//Clients address
				Thread thread = new Thread(new BankServer(cSocket)); //thread created
				thread.start();	
				jta.append("Client at " +inetAddress + " Connected " + "\n");	//thread started
				System.out.println("Connected");
				System.out.println("thread started");
			}
		} catch (IOException ex) {
			System.err.println(ex);
			System.out.println("Check PORT 800,  Port may be in use");
		}
	}

	
	/**
	 * Calculate monthly loan and total payments
	 * This method takes in three parameter from the clients output stream
	 *  which are used to calculate the monthly and total loan amounts
	 */
	private void calculateLoan(double rate, double amount, double years){
		
		double monthsInOneYear = 12;
		double numMonths = years*monthsInOneYear;
	
		//monthly payment with interest applied using formula below
		monthlyPayment = (amount * rate / 1200) /
				(1 - Math.pow(1 / (1 + rate / 1200), numMonths));
		
		//Monthly payments * by 12 months and * total years to calculate total payment with interest applied
		totalAmount = (monthlyPayment * monthsInOneYear )* years; 
		
		//Keep doubles values to one decimal place
		DecimalFormat decimalFor = new DecimalFormat("#.##");
		monthlyPayment = Double.valueOf(decimalFor.format(monthlyPayment));
		totalAmount = Double.valueOf(decimalFor.format(totalAmount));
	}
	

	/**
	 *  If a user is not in database it returns boolean type false is returned;
	 */
	private Boolean queryDataBase() {
		
		Connection connection; 
		
		try {
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bank", "admin",
				"");
			
			if (!connection.isClosed()){
				System.out.println("Successfully connected to MySQL server..."); 
			}
			
			//Query database statement for matching account number as client
			PreparedStatement statment = connection.prepareStatement("SELECT * FROM loancustomer WHERE accountNumber = ('"+reciveAccNum+"') " );
			ResultSet resultSet = statment.executeQuery();
			
			System.out.println(resultSet);
			
			if(resultSet.next()){
				clientRegistered = true;
				jta.append("Client at " + inetAddress + " is a registered account holder " + "\n\n");
				
			}else 
				clientRegistered = false;
			
			statment = connection.prepareStatement("SELECT accountNumber, firstName, lastName FROM loancustomer WHERE accountNumber = ('"+reciveAccNum+"') " );
			resultSet = statment.executeQuery();
			if(resultSet.next()){
				
				number = resultSet.getInt("accountNumber");
				firstName = resultSet.getString("firstName");
				lastName = resultSet.getString("lastName");
				jta.append("Client at " + inetAddress + " details ->"  + " Account Number: " + number + ", Name: " + firstName + " " + lastName + " \n\n");
				
			}
			
			resultSet.close();
			statment.close();
			connection.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return clientRegistered;
	}
	
	
	private void addRequest(int accountNumber, double requestedAmount, double rate,  double numberOfYears, double monthlyPayment,java.sql.Date dateRequested, String firstName, String lastName) {
		
		Connection connection2; 
	
	try {
		connection2 = DriverManager.getConnection("jdbc:mysql://localhost:3306/bank", "admin","");
		
	    
	      
	      String query = " insert into userrequests (accountNumber, requestedAmount, rate, numberOfYears, monthlyPayment ,dateRequested, firstName, lastName)"
	    	        + " values (?, ?, ?, ?, ?, ?, ?, ?)";
	      PreparedStatement preparedStmt = connection2.prepareStatement(query);
	      preparedStmt.setInt(1, accountNumber);
	      preparedStmt.setDouble(2, requestedAmount);
	      preparedStmt.setDouble(3, rate);
	      preparedStmt.setDouble(4, numberOfYears);
	      preparedStmt.setDouble(5, monthlyPayment);
	      preparedStmt.setDate(6, dateRequested);
	      preparedStmt.setString(7, firstName);
	      preparedStmt.setString(8, lastName);
		
	      preparedStmt.execute();
		
	      connection2.close();
		
	} catch (Exception e) {
		
		e.printStackTrace();
		
	}

	
}
	
	
	public void dataTransfer(){
			try {
				//Server Data input output streams
				DataInputStream serverIn = new DataInputStream(socket.getInputStream());
				DataOutputStream serverOut = new DataOutputStream(socket.getOutputStream());
				
				while(true){
					//data received from client
					reciveAccNum = serverIn.readInt();
					reciveRate = serverIn.readDouble();
					reciveYears =  serverIn.readDouble();
					reciveAmt = serverIn.readDouble();
					//queryDataBase();
				
					
					//if queryDataBase() returns true, Client number exists.....(send data to client)
					if(queryDataBase()){
							calculateLoan(reciveRate, reciveAmt, reciveYears);
							
							//Send computed data and information retrieved from Bank DataBase back to client
							serverOut.writeDouble(reciveRate);
							serverOut.writeDouble(totalAmount);
							serverOut.writeDouble(monthlyPayment);
							serverOut.writeUTF(firstName);
							serverOut.writeUTF(lastName);
							serverOut.writeBoolean(clientRegistered);
							
							
							//*********Debugging received info Delete later*************
							jta.append("Account Number: " + reciveAccNum + "\n");
							jta.append("Years: " + reciveYears + "\n");
							jta.append("Rate: " + reciveRate + "\n");
							jta.append("Amount: " + reciveAmt + "\n\n");
							jta.append("Monthly payment is: " + monthlyPayment + "  Total Amount to pay: " + totalAmount);	
							
							Calendar calendar = Calendar.getInstance();
							java.sql.Date dateRequested = new java.sql.Date(calendar.getTime().getTime());
							
							//Store All requests to the database
							addRequest(reciveAccNum, totalAmount, reciveRate, reciveYears, monthlyPayment, dateRequested, firstName, lastName);
							
					}
					else{
						//Client waiting for reply even if not registered, Send 0 values and message
						
						serverOut.writeInt(0);
						serverOut.writeDouble(0);
						serverOut.writeDouble(0);
						serverOut.writeDouble(0);
						serverOut.writeBoolean(clientRegistered);
						jta.append("Client is not registered " + "\n");
						
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	/**
	 * implemented method for Runnable
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		dataTransfer();
	}
	
}
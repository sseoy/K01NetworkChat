package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ConnectDB implements IConnect {
	 public static Connection con;
	 public static PreparedStatement psmt;
	 public ResultSet rs;
	
	public  ConnectDB() {
	}
	public ConnectDB(String user, String pass) {
	      //System.out.println("ConnectDB 인자생성자 호출");
	      try {
	    	  //드라이버로드
	         Class.forName(ORACLE_DRIVER);
	         //DB연결
	         connect(user, pass);
	      } 
	      catch (ClassNotFoundException e) {
	         System.out.println("드라이버 로딩 실패");
	         e.printStackTrace();
	      }
	   }
	//DB에 실제 접속
	@Override
	public void connect(String user, String pass) {
		try {
			con = DriverManager.getConnection(ORACLE_URL, user, pass);
			
		}catch(SQLException e) {
			System.out.println("데이터베이스 연결오류");
			e.printStackTrace();
		}
	}
	
	@Override
	public void close() {
		try {
			if(con != null) con.close();
			if(psmt != null) psmt.close();
			if(rs != null) rs.close();
		}catch(Exception e) {
			System.out.println("자원 반납 시 오류발생");
			e.printStackTrace();
		}
	}
	
	
}

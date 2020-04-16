package chat7;

import java.net.Socket;
import java.util.Scanner;

public class MultiClient extends ConnectDB{
	
	//생성자
	public MultiClient () {
		super("kosmo","1234");
	}

   public static void main(String[] args) {
	   Scanner scanner = new Scanner(System.in);
	   String s_name;
	   do {    
			   System.out.println("이름을 써주세요 : ");
			   s_name = scanner.nextLine();
//			   //이름 DB에 넣기
//			   String query = "INSERT into member_tb values ( ?, ''))"; 
//			   
//			   psmt = con.prepareStatement(query);
//	       	
//			   psmt.setString(1, s_name);
//	           int affected = psmt.executeUpdate();

	           
		   }while(s_name.isEmpty());
	
	  
    
      
      //Sender가 기능을 가져가므로 여기서는 필요없음
//      PrintWriter out = null;
      //Receiver가 기능을 가져가므로 여기서는 필요없음
//      BufferedReader in = null;
      
      try {
         String ServerIP = "localhost";
         if(args.length > 0) {
            ServerIP = args[0];
         }
         Socket socket = new Socket(ServerIP, 9999);
         //System.out.println("서버와 연결되었습니다..");
         
         //서버에서 보내는 Echo메세지를 클라이언트에 출력하기 위한 쓰레드 생성
         Thread receiver = new Receiver(socket);
         receiver.start();
         
         //클라이언트의 메세지를 서버로 전송해주는 쓰레드 생성
         Thread sender = new Sender(socket, s_name);
         sender.start();
      } 
      catch (Exception e) {
         System.out.println("예외발생[MultiClient]" + e);
      }
      
   }
   
 

}
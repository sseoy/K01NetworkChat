package chat7;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class MultiServer extends ConnectDB{
   
   static ServerSocket serverSocket = null;
   static Socket socket = null;
   static //클라이언트 정보 저장을 위한 Map컬렉션정의
    Map<String, PrintWriter> clientMap;
   
   //생성자 
   public MultiServer() {
	  
	   super("kosmo","1234");
	   //클라이언트의 이름과 출력스트림을 저장할 HashMap생성
      clientMap = new HashMap<String, PrintWriter>();
      //HashMap동기화 설정, 쓰레드가 사용자정보에 동시에 접근하는것을 차단한다.
      Collections.synchronizedMap(clientMap);
   }
   
   //서버초기화
   public void init() {
	   Scanner scan = new Scanner(System.in);
	   boolean check = false;
      
      try {
         serverSocket = new ServerSocket(9999);
         System.out.println("서버가 시작되었습니다.");
         System.out.println("블랙리스트작성하시겠습니까? 네:1, 아니오:2");
         
        int choice = scan.nextInt();
         scan.nextLine();
         System.out.println("블랙리스트 할 대상을 적어주세요:");
         
         while(check==false) {
        	 String b_name = scan.nextLine();
        	 
        	 if(choice == 1) {
        		 check=blacklistCheck(b_name, check);
            	 
            	 if(check==false) {
            		 System.out.println("입력한 이름이 없습니다. 다시 입력해주세요!");
                 }else {
                	 break;
                	 
                 }
             }
         }
         
         /*
	         클라이언트의 메세지를 모든 클라이언트에게 전달하기 위한 
	         쓰레드 생성 및 start.
	     */
         while (true) {
            socket = serverSocket.accept();
            Thread mst = new MultiServerT(socket);
            mst.start();
         }
      } 
      catch (Exception e) {
         e.printStackTrace();
      }
      finally {
         try {
            serverSocket.close();
         } 
         catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
   


   //메인메소드 : Server객체를 생성한 후 초기화한다.
   public static void main(String[] args) {
      
      MultiServer ms = new MultiServer();
      ms.init();
   }
   
   
   //접속된 모든 클라이언트에게 메세지를 전달하는 역할의 메소드
   public void sendAllMsg(String name, String msg) {
	   
	   //Map에 저장된 객체의 키값(이름)을 먼저 얻어온다.
       Iterator<String> it = clientMap.keySet().iterator();
       
       //저장된 객체(클라이언트)의 갯수만큼 반복한다.
       while(it.hasNext()) {
    	   try {
    		   //각 클라이언트의 PrintWriter객체를 얻어온다.
    		   PrintWriter it_out = 
    				   (PrintWriter)clientMap.get(it.next());
    		   //클라이언트에게 메세지를 전달한다.
    		   /*
	    		    매개변수name이 있는 경우에는 이름+메세지
	    		    없는경우에는 메세지만 클라이언트로 전달한다.
    		    */
    		   if(name.equals("")) {
    			   it_out.println(URLDecoder.decode(msg, "UTF-8"));
    		   }
    		   else {
    			   it_out.println(URLDecoder.decode("["+name+"]:"+msg,"UTF-8"));
    		   }
    	   }catch(Exception e) {
    		   System.out.println("예외:"+e);
    	   }
       }
   }
   
   //귓속말 보내기(일회성)
   public void sendMsg(String msg, String w_name) {
	   int start = msg.indexOf(" ")+1;
	   int end = msg.indexOf(" ", start);
	   
	   if(end != -1) {
		   String to = msg.substring(start, end);
		   String msg2 = msg.substring(end+1);
		   Object obj = clientMap.get(to);
		   
		   if(obj != null) {
			   PrintWriter name = (PrintWriter)obj;
			   name.println(w_name +"님이 귓속말을 보내셨습니다.:"+ msg2);
		   }
	   }
   }
   
   //귓속말 보내기(고정)
   public void sendMsgWhisper(String msg, String w_name, String w2_name) {
	   Object obj = clientMap.get(w_name);
	   if(msg != null) {
		   PrintWriter name = (PrintWriter)obj;
		   name.println(w2_name +"님이 귓속말을 보내셨습니다.:"+ msg);
	   }
   }
   
   //중복 이름 처리
   public boolean doubleCheck(String name, boolean check) {
	 
	   try {
		   String query = "INSERT into member_td values ( ?, '')"; 
		   
		   psmt = con.prepareStatement(query);
	   	
		   psmt.setString(1, name);
	       psmt.executeUpdate();//입력한 행갯수 반환
	       
	       check = false; 
	       return check;
		   
		   
	   }catch (Exception e) {
		//e.printStackTrace();
		check = true;
		
	}
	return check;
   }
   
   //블랙리스트 이름 확인
   public boolean blacklistCheck(String name, boolean check) {
	   try {
		   String sql = "SELECT * FROM member_td "
					+ "WHERE name LIKE '%'||?||'%'";
		   psmt = con.prepareStatement(sql);
		   psmt.setString(1, name);
		   rs= psmt.executeQuery();
		   check = true;
		   
		   if(psmt.executeUpdate()==0) {
			   System.out.println("테스트");
			   check = false;
		   }
		   
	   }catch (Exception e) {
		   
	}
	return check;
	   
   }


//내부클래스
class MultiServerT extends Thread {
   
	//멤버변수
   Socket socket;
   PrintWriter out = null;
   BufferedReader in = null;
   //생성자 : Socket을 기반으로 입출력 스트림을 생성한다.
   public MultiServerT() {}
   public MultiServerT(Socket socket) {
      this.socket = socket;
      try {
         out = new PrintWriter
            (this.socket.getOutputStream(), true);
         in = new BufferedReader(new 
         InputStreamReader(this.socket.getInputStream(), "UTF-8"));
      } 
      catch (Exception e) {
         System.out.println("예외 : " + e);
      }
   }
   
   
	
  
   
   @Override
   public void run() {
	   
      //클라이언트로부터 전송된 "대화명"을 저장할 변수
      String name = "";
      //귓속말 저장할 변수
      String whisper = "";
      //메세지 저장용 변수
      String s = "";
      boolean whisperSelection = false;
      
      boolean check  = true;
      
      try {
    	  while(check==true) {
    		//클라이언트의 이름을 읽어와서 저장
              name = in.readLine();
              
              //boolean whisper = false;
              name = URLDecoder.decode(name, "UTF-8");
              //접속한 클라이언트에게 새로운 사용자의 입장을 알림.
              //접속자를 제외한 나머지 클라이언트만 입장메세지를 받는다.
              
              check = doubleCheck(name, check);
              if(check==false) {
            	  out.println("테스트");
              }else {
            	  out.println("중복됩니다. 다시 이름 입력해주세요!");
              }
    	  }
            
            sendAllMsg("", name+"님이 입장하셨습니다.");
            
            //현재 접속한 클라이언트를 HashMap에 저장한다.
            clientMap.put(name, out);
            
            //HashMap에 저장된 객체의 수로 접속자수를 파악할수 있다.
            System.out.println(name+"접속");
            System.out.println("현재 접속사 수는 "+clientMap.size()+"명 입니다.");
            
            //입력한 메세지는 모든 클라이언트에게 Echo된다.
            //      /list
            
            while (in!=null) {
            	
            	s= in.readLine();
            	s = URLDecoder.decode(s, "UTF-8");
            	Iterator<String> keys = clientMap.keySet().iterator();
            	if(s.startsWith("/")) {
            		
            		if(s.substring(1).equals("list")) {
            			
                		out.println("※접속List※");
                		while(keys.hasNext()) {
                			
                			String key = keys.next();
                			out.println(key+" 님");
                		}
                		out.println("접속되어있습니다.");
            		}
            		//귓속말 일회성, 고정구분하기
            		else if(s.substring(1,3).equals("to")){
	            			
	            		String[] whisperArr = s.split(" ");
	            		String w_name = whisperArr[1];
	            		//일회성
	            		if(whisperArr.length>2) {
	            			//out.println("확인");
	            			sendMsg(s, name);
	            			
	            			
	            		}else if(whisperArr.length==2){
	            			while(true) {
	            				
	            				whisper=in.readLine();
	            				out.println("고정귓속말이 설정되었습니다.");
	            				if(whisper.equals("end")) {
	            					out.println("고정귓속말이 해제되었습니다.");
	            					break;
	            				}
	            				sendMsgWhisper(whisper, w_name, name);
	            			}
	            		}
    			}
				
            }//첫번째 if문 
            	else {
            		System.out.println(name+" >> "+s);
                    //DB처리는 여기서 클라이언트에게 Echo해준다.
                    String query = "INSERT into chating_tb values (seq_chating.nextval, ?, ?, to_char(sysdate,'mm/dd hh:mi:ss'))";
                    			
                    	
                    psmt = con.prepareStatement(query);
                    	
                    psmt.setString(1, name);
                    psmt.setString(2, s);
                    	
                    	
                    sendAllMsg(name, s);
                    int affected = psmt.executeUpdate();
            		
            	}
            	
            }
      }catch(NullPointerException e) {
    	
      }
      catch (Exception e) {
         System.out.println("예외 : " + e);
         
      }
      finally {
         /*
	          클라이언트가 접속을 종료하면 예외가 발생하게 되어 finally로 넘어오게된다.
	          이떄 "대화명:을 통해 remove()시켜준다.
          */
    	 clientMap.remove(name);
    	 sendAllMsg("", name+"님이 퇴장하셨습니다..");
    	 //퇴장하는 클라이언트의 쓰레드명을 보여준다.
    	 System.out.println(name+" ["+
    			 Thread.currentThread().getName()+"] 퇴장");
    	 System.out.println("현재 접속사 수는"+clientMap.size()+"명 입니다.");
         
         try {
            in.close();
            out.close();
            socket.close();
            close();
         } 
         catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
  }   
}

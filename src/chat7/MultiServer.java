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
	   
	   final int NO=1;
	   final int BLACK=2;
	   final int Prohibition=3;
	   
      
      try {
        System.out.println("설정하시겠습니까? 1.아니오, 2.블랙리스트설정 3.대화금칙어설정");
         
        int choice = scan.nextInt();
         scan.nextLine();
        
         
         while(check==false) {
        	
        	 //블랙리스트 설정
        	 if(choice == BLACK) {
        		 System.out.println("블랙리스트 할 대상을 적어주세요:");
        		 String b_name = scan.nextLine();
        		 check=blacklistCheck(b_name, check);
        		 //blackList=b_name;
        		 
            	 if(check==false) {
            		 System.out.println("입력한 이름이 없습니다. 다시 입력해주세요!");
                 }else {
                	 break;
                 }
             }else if(choice==Prohibition){
            	 break;
             }else if(choice==NO){
            	 break;
             }
         }
         serverSocket = new ServerSocket(9999);
         System.out.println("서버가 시작되었습니다.");
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
   public void sendMsg(String msg, String w_name, String w2_name) {
	   int start = msg.indexOf(" ")+1;
	   int end = msg.indexOf(" ", start);
	   
	   if(end != -1) {
		   String to = msg.substring(start, end);
		   String msg2 = msg.substring(end+1);
		   Object obj = clientMap.get(to);
		   
		   if(obj != null) {
			   PrintWriter name = (PrintWriter)obj;
			   name.println(w2_name +"님이 귓속말을 보내셨습니다.:"+ msg2);
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
   
   //중복 이름 처리(등록)
   public boolean doubleCheck(String name, boolean check) {
	   try {
		   String query = "INSERT into member_td values ( ?, '일반')"; 
		   
		   psmt = con.prepareStatement(query);
		   psmt.setString(1, name);
	       psmt.executeUpdate();//입력한 행갯수 반환
	       
	       if(psmt.executeUpdate()==0) {
			   check = true;
	       }
	       
		   
	   }catch (Exception e) {
		//e.printStackTrace();
		   check = false;
	}
	return check;
   }
   
   //로그인(이름 유무 확인), 중복
   public boolean nameLogin(String name, boolean check) {
	   try {
		   String sql = "SELECT * FROM member_td "
				+ "WHERE name LIKE '%'||?||'%'";
		   psmt = con.prepareStatement(sql);
		   psmt.setString(1, name);
		   rs= psmt.executeQuery();
		   
		   if(psmt.executeUpdate()==0) {
			   check = true;
		   }else {
			   check = false;
		   }
		   
	   }
	   catch (Exception e) {
		   //check = false;
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
		   
		   String sql_02 =
				   "UPDATE member_td set state='블랙' where name LIKE '%'||?||'%'";
		   psmt = con.prepareStatement(sql_02);
		   psmt.setString(1, name);
		   rs= psmt.executeQuery();
		   
		   if(psmt.executeUpdate()==0) {
			   check = false;
		   }else {
			   System.out.println("※"+name+"님이 블랙되었습니다.※");
		   }
		   
	   }catch (Exception e) {
		   
	}
	return check;
	   
   }
   
   //블랙리스트 체크
   public boolean blackCheck(String name, boolean check){
	   try {
		   String sql = "SELECT * FROM member_td "
					+ "WHERE name LIKE '%'||?||'%' and state='블랙'";
		   psmt = con.prepareStatement(sql);
		   psmt.setString(1, name);
		   rs= psmt.executeQuery();
		   if(psmt.executeUpdate()==0) {
			   check = false;
		   }else {
			   
		   }
		   
	   }catch (Exception e) {
		   check = true;
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
	   Scanner scanner = new Scanner(System.in);
      //클라이언트로부터 전송된 "대화명"을 저장할 변수
      String name = "";
      //귓속말 저장할 변수
      String whisper = "";
      //메세지 저장용 변수
      String s = "";
      
      
      boolean check  = true;
      boolean check_lo  = true;
      boolean black_check = true;
      
      try {
    	  while(check==true) {
    		//클라이언트의 등록 여부, 이름을 읽어와서 저장
    		
    		//out.println("테스트");
    		name = in.readLine();
    		name = URLDecoder.decode(name, "UTF-8");
    		
    		black_check=blackCheck(name, black_check);
    		
    		if(black_check==true) {
    			out.println("블랙리스트 이름입니다. 접속할수 없습니다.");
    			return;
    			
    		}else {
    			
    		}
    		while(check_lo==true) {
    			
    			check_lo = nameLogin(name, check_lo);//처음에 DB에 이름이 있는지 확인
    			
    			
    			
    			if(check_lo==false) {
    				out.println("로그인합니다.");
    				check = false;//등록할필요 없으니깐 false를 그냥 지정해줌
    				
        		}else if(check_lo==true){
        			out.println("등록된 이름이없습니다. 등록을 하세요");
        			out.println("등록할 이름: ");
        			name=in.readLine();
        			check = doubleCheck(name, check);//아이디 중복체크(없으면 false, 있으면 true반환)
        			//out.println(check);
        			if(check==true) {
        				out.println("중복됩니다. 다시 이름 입력해주세요!");
                    }else if(check==false){
                    	out.println("※등록되었습니다.※");
                    	//break;
                    }
        		}
    		}
              if(check==false && check_lo==false) {
            	  out.println("채팅 시작!");
              }
    	  }
            
            sendAllMsg("", name+"님이 입장하셨습니다.");
            
            //현재 접속한 클라이언트를 HashMap에 저장한다.
            clientMap.put(name, out);
            
            //HashMap에 저장된 객체의 수로 접속자수를 파악할수 있다.
            System.out.println(name+"접속");
            System.out.println("현재 접속자 수는 "+clientMap.size()+"명 입니다.");
            
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
	            			sendMsg(s, w_name , name);
	            			
	            			
	            		}else if(whisperArr.length==2){
	            			out.println("고정귓속말이 설정되었습니다.");
	            			while(true) {
	            				
	            				whisper=in.readLine();
	            				
	            				if(whisper.equals("/end")) {
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
                    psmt.executeUpdate();
            		
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
    	 System.out.println(name+" 퇴장");
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
   }//end of run
  }   
}

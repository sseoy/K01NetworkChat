package chat2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiServer {

	public static void main(String[] args) {
		
		ServerSocket  serverSoket=null;//클라이언트의 요청을 받기 위해 준비
		Socket socket = null;//서버에 접속 요청을 한다.
		PrintWriter out = null;
		BufferedReader in = null;
		String s="";//클라이언트의 메세지를 저장
		String name = "";//클라이언트의 이름을 저장sdlfksjd
		
	
		
		try {
			//localhost대신 127.0.0.1로 접속해도 무방하다.
			serverSoket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");
			
			socket = serverSoket.accept();
			
			
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new
					InputStreamReader(socket.getInputStream()));
			
			/*
			 클라이언트가 서버로 전송하는 최초의 메세지는 "대화명" 이므로 
			 메세지를 읽은후 변수에 저장하고 클라이언트쪽으로 Echo해준다.
			 */
			
			if(in !=null) {
				name=in.readLine();
				System.out.println(name+"접속");
				out.println(">" +name+"님이 접속했습니다.");
			}
			/*
			 두번째 메세지부터는 실제 대화내용이므로 읽어와서 로그로 출력하고 동시에
			 클라이언트로 Echo한다.
			 */
			while(in !=null) {
				s=in.readLine();
				if(s==null) {
					break;
				}
				System.out.println(name+"==>"+s);
				out.println("> "+name+" ==>"+ s);
			}
			System.out.println("Bye....!!!");
			
		}
		catch (Exception e) {
			System.out.println("예외1:"+e);
			//e.printStackTrace();	
		}
		finally {
			try {
				//입출력스트림 종료
				in.close();
				out.close();
				socket.close();
				serverSoket.close();
			}
			catch(Exception e) {
				System.out.println("예외2:"+e);
				e.printStackTrace();
			}
		}
	}

}

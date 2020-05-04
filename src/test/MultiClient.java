package test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MultiClient {

	public static void main(String[] args) {

		System.out.print("이름을 입력하세요:");
		Scanner scanner = new Scanner(System.in);
		String s_name = scanner.nextLine();

		PrintWriter out = null;
		BufferedReader in = null;

		try {
			String ServerIP = "localhost";
			if(args.length > 0) {
				ServerIP = args[0];
			}
			Socket socket = new Socket(ServerIP, 9999);
			System.out.println("서버와 연결되었습니다...");

			/*** ②  ***/
			//입출력스트림이다. 바이트를 읽어서 문자인코딩에 따라 문자로 변환에 사용.
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			out.println(s_name);

			while(out!=null) {
				try {
					if(in!=null) {
						System.out.println("Receive : "+ in.readLine());
					}

					String s2 = scanner.nextLine();
					if(s2.equals("q") || s2.equals("Q")) {
						break;
					}
					else {
						out.println(s2);
					}
					if(!(s2.indexOf("광고")==-1)) {
						System.out.println("금지단어이므로 출력되지 않습니다");
						break;
					}
				}
				catch (Exception e) {
					System.out.println("예외:"+ e);
				}
			}

			in.close();
			out.close();
			socket.close();
		}
		catch (Exception e) {
			System.out.println("예외발생[MultiClient]"+ e);
		}
	}
}

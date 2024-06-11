import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket client = new Socket("127.0.0.1", 8888);

       // Scanner scanner = new Scanner(client.getInputStream());
       Scanner input = new Scanner(System.in);
//        PrintWriter print = new PrintWriter(client.getOutputStream());

        DataOutputStream dou = new DataOutputStream(client.getOutputStream());
        DataInputStream din = new DataInputStream(client.getInputStream());
        String answer = "", server = "" ;
        //System.out.println("salam");
        while (true){
            server = din.readUTF();
            if(!server.equals("Exit")){

                System.out.println("server : ");
                System.out.println(server);

                answer = input.nextLine();
                dou.writeUTF(answer);
            }else {
                break;
            }


             }
    }
}

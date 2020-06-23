package bin;

import java.io.*;
import java.net.*;
import java.util.*;

public class MusicServer {
    ArrayList<ObjectOutputStream> clientOutputStreams;

    public static void main(String[] args){
        new MusicServer().go();
    }

    public class ClientHandler implements Runnable{
        ObjectInputStream input;
        Socket clientSocket;

        public ClientHandler(Socket socket){
            try{
                clientSocket = socket;
                input = new ObjectInputStream(clientSocket.getInputStream());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void run(){
            Object obj2 = null;
            Object obj1 = null;

            try{
                while ((obj1 = input.readObject()) != null){
                    obj2 = input.readObject();
                    System.out.println("read two objects");
                    tellEveryone(obj1,obj2);
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    public void go(){
     clientOutputStreams = new ArrayList<ObjectOutputStream>();

     try {
         ServerSocket serverSocket = new ServerSocket(4242);

         while(true){
             Socket clientSocket = serverSocket.accept();
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             clientOutputStreams.add(out);

             Thread thread = new Thread(new ClientHandler(clientSocket));
             thread.start();

             System.out.println("got a connection");
         }
     } catch (Exception ex){
         ex.printStackTrace();
     }
    }

    public void tellEveryone(Object one, Object two){
        Iterator it = clientOutputStreams.iterator();
        while (it.hasNext()){
            try{
                ObjectOutputStream out = (ObjectOutputStream) it.next();
                out.writeObject(one);
                out.writeObject(two);
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}

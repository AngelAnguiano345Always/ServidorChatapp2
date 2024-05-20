import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
public class Main {
    private static final int puerto = 1998;
    private static List<PrintWriter> clientWriters = new ArrayList<>();
    public static void main(String[] args) {
        System.out.println("Servidor de chatapp iniciado...");
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private String name;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                out = new PrintWriter(socket.getOutputStream(), true);
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }
                name = in.readLine();
                broadcast("ChatApp: " + name + " se ha unido al chat");
                String message;
                while ((message = in.readLine()) != null) {
                    broadcast(name + ": " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    synchronized (clientWriters) {
                        clientWriters.remove(out);
                    }
                }
                if (name != null) {
                    broadcast("ChatApp: " + name + " ha dejado el chat");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcast(String message) {
            System.out.println(message);
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}

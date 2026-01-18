import java.util.Scanner;

public class Main {
    static Scanner scanner;
    static String command;
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage
        scanner = new Scanner(System.in);
        boolean alive = true;
        do {
            System.out.print("$ ");
            String command = scanner.nextLine();
            switch (command) {
                case "exit":
                    alive = false;
                    break;
                default:
                    System.out.println(command + ": command not found");
                    break;
            }
        }while(alive);
    }
}

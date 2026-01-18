import java.util.Scanner;

public class Main {
    static Scanner scanner;
    static String command;
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage
        scanner = new Scanner(System.in);
        System.out.print("$ ");
        String command = scanner.nextLine();
        System.out.println(command + ": command not found");
    }
}

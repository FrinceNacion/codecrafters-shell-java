import java.util.Scanner;

public class Main {
    static Scanner scanner;
    static String input;
    static String command;
    static String parameter;

    // For the type command
    public static void match_command(String command){
        switch (command) {
            case "exit":
                System.out.println("exit is a shell builtin");
                break;
            case "echo":
                System.out.println("echo is a shell builtin");
                break;
            default:
                System.out.println(command + ": command not found");
                break;
        }
    }

    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage
        scanner = new Scanner(System.in);
        boolean alive = true;
        do {
            System.out.print("$ ");
            input = scanner.nextLine();
            String[] input_raw = input.split(" ", 2);
            command = input_raw[0].toLowerCase();
            parameter = (input_raw.length != 1) ? input_raw[1]: "";
            switch (command) {
                case "exit":
                    alive = false;
                    break;
                case "echo":
                    System.out.println(parameter);
                    break;
                case "type":
                    match_command(parameter.toLowerCase());
                    break;
                default:
                    System.out.println(command + ": command not found");
                    break;
            }
        }while(alive);
    }
}

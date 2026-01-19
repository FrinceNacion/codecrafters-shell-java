import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Stream;

public class Main {
    static String[] path_environment_directories = System.getenv("PATH").split(File.pathSeparator);
    static Scanner scanner;
    static String input;
    static String command;
    static String parameter;

    private static Optional<Path> find_executable_file_in_PATH(String file_name){
        Optional<Path> result = Optional.empty();
        for(String current_path_raw : path_environment_directories){
            Path current_path = Path.of(current_path_raw);
            if (!Files.exists(current_path) && !Files.isDirectory(current_path)) {
                continue;
            }

            result = find_executable_file(file_name, current_path);
            if (!result.equals(Optional.empty())){break;}
        }
        return result;
    }

    private static Optional<Path> find_executable_file(String file_name, Path directory){
        try (Stream<Path> stream = Files.list(directory)) {
            Optional<Path> file = stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(file_name))
                    .filter(Files::isExecutable)
                    .findFirst();
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // used in the type command
    private static String type_command_file_finder(String command) {
        Optional<Path> file = find_executable_file_in_PATH(command);
        return (!file.equals(Optional.empty())) ? command + " is " +file.get().toString() : command + ": not found";
    }

    // For the type command
    private static void type_command(String command){
        switch (command) {
            case "exit":
                System.out.println("exit is a shell builtin");
                break;
            case "echo":
                System.out.println("echo is a shell builtin");
                break;
            case "type":
                System.out.println("type is a shell builtin");
                break;
            default:
                System.out.println(type_command_file_finder(command));
                break;
        }
    }

    private static Process run_program(String program_name, String program_params){
        Optional<Path> file = find_executable_file_in_PATH(program_name);
        if (file.equals(Optional.empty())){
            return null;
        }

        List<String> command = new ArrayList<>();
        command.add(program_name);
        command.add(program_params);
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void main(String[] args) throws Exception {
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
                    type_command(parameter.toLowerCase());
                    break;
                default:
                    if(run_program(command, parameter) == null){
                        System.out.println(command + ": command not found");
                    }
                    break;
            }
        }while(alive);
    }
}

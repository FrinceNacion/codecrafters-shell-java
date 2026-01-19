import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Stream;

public class Main {
    static String[] path_environment_directories = System.getenv("PATH").split(File.pathSeparator);
    static Scanner scanner;
    static String input;
    static String command;
    static String parameter;

    static void list_files(){
        for (int i = 0; i < path_environment_directories.length; i++) {
            Path current_directory = Path.of(path_environment_directories[i]);
            if (Files.exists(current_directory) && Files.isDirectory(current_directory)) {
                try (Stream<Path> stream = Files.list(current_directory)) {
                    stream.filter(Files::isRegularFile)
                            .filter(Files::isExecutable)
                            .map(Path::getFileName)
                            .forEach(System.out::println);
                } catch (IOException e) {
                    System.out.println("Error");
                    throw new RuntimeException(e);
                }
            }
        }
    }

        static String check_file_in_path(String command) {
            Optional<Path> result = null;
            for (String pathEnvironmentDirectory : path_environment_directories) {
                Path current_directory = Path.of(pathEnvironmentDirectory);
                if (!Files.exists(current_directory) && !Files.isDirectory(current_directory)) {
                    continue;
                }

                try (Stream<Path> stream = Files.list(current_directory)) {
                    Optional<Path> first = stream.filter(Files::isRegularFile)
                            .filter(path -> path.getFileName().toString().equals(command))
                            .filter(Files::isExecutable)
                            .findFirst();
                    if (!first.equals(Optional.empty())) {
                        result = first;
                        break;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return (result != null) ? command + " is " +result.get().toString() : command + ": not found";
        }

    // For the type command
    public static void match_command(String command){
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
                System.out.println(check_file_in_path(command));
                break;
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
                    match_command(parameter.toLowerCase());
                    break;
                default:
                    System.out.println(command + ": command not found");
                    break;
            }
        }while(alive);
    }
}

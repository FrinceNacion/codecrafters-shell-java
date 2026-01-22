import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Main {
    static String[] path_environment_directories = System.getenv("PATH").split(File.pathSeparator);
    static Scanner scanner;
    static String input;
    static String command;
    static String parameter;
    static Path current_directory = Paths.get("").toAbsolutePath().normalize();

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
            case "pwd":
                System.out.println("pwd is a shell builtin");
                break;
            case "cd":
                System.out.println("cd is a shell builtin");
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
        splitter(program_params).stream().filter(str -> !str.isBlank()).forEach(command::add);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line); // Output: Hello from Java
                }
            }
            return process;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String print_working_directory(){
         return current_directory.toString();
    }

    private static void change_directory(String path){
        Path new_current_path = Path.of(path).normalize();

        // Handle user's home directory
        if (new_current_path.toString().equals("~")){
            current_directory = Path.of(System.getenv("HOME"));
            return;
        }

        if (!new_current_path.isAbsolute()){
            new_current_path = current_directory.resolve(new_current_path).normalize();
        }

        if (!Files.isDirectory(new_current_path) && !Files.exists(new_current_path)){
            System.out.println("cd: "+new_current_path.toString()+": No such file or directory");
            return;
        }

        // Handle absolute paths
        if (new_current_path.isAbsolute() || new_current_path.startsWith("/")){
            current_directory = new_current_path.toAbsolutePath();
            return;
        }

        // Handle relative paths
        current_directory = current_directory.resolve(new_current_path).normalize();
    }

    private static LinkedList<String> splitter(String str){
        LinkedList<String> to_print = new LinkedList<>();
        boolean in_qoutes = false;
        StringBuilder inside = new StringBuilder();
        StringBuilder outside = new StringBuilder();

        for(char character : str.toCharArray()){
            if (character == '\''){
                if (in_qoutes && !inside.isEmpty()){
                    to_print.add(inside.toString());
                    inside.setLength(0);
                }
                if (!outside.isEmpty()){
                    to_print.add(outside.toString().replaceAll("\\s+", " "));
                    outside.setLength(0);
                }
                in_qoutes = !in_qoutes;
            } else if (in_qoutes) {
                inside.append(character);
            }else{
                outside.append(character);
            }
        }
        if (!outside.isEmpty()){
            to_print.add(outside.toString().replaceAll("\\s+", " "));
            outside.setLength(0);
        }
        if (in_qoutes){
            to_print.add(inside.toString());
        }
        return to_print;
    }


    private static String parse_single_qoute(String str){
        StringBuilder to_print = new StringBuilder();
        boolean in_qoutes = false;
        StringBuilder inside = new StringBuilder();
        StringBuilder outside = new StringBuilder();

        for(char character : str.toCharArray()){
            if (character == '\''){
                if (in_qoutes){
                    to_print.append(inside.toString());
                    inside.setLength(0);
                }
                if (outside.length() > 0){
                    to_print.append(outside.toString().replaceAll("\\s+", " "));
                    outside.setLength(0);
                }
                in_qoutes = !in_qoutes;
            } else if (in_qoutes) {
                inside.append(character);
            }else{
                outside.append(character);
            }
        }
        if (outside.length() > 0){
            to_print.append(outside.toString().replaceAll("\\s+", " "));
            outside.setLength(0);
        }
        if (in_qoutes){
            to_print.append(inside.toString());
        }
        return to_print.toString();
    }

    private static void echo_command(String params) {
        System.out.println(parse_single_qoute(params));
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
                case "pwd":
                    System.out.println(print_working_directory());
                    break;
                case "cd":
                    change_directory(parameter);
                    break;
                case "echo":
                    echo_command(parameter);
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

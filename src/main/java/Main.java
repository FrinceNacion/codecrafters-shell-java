import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;


public class Main {
    static Scanner scanner;
    static String input;
    static ParameterParser parameter_parser = new ParameterParser();
    static ParameterParser command_parser = new ParameterParser();
    static String command;
    static String parameter;
    static Path current_directory = Paths.get("").toAbsolutePath().normalize();


    // used in the type command
    private static String type_command_file_finder(String command) {
        Optional<Path> file = FileProcessor.find_executable_file_in_PATH(command);
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

    private static void echo_command() {
        String[] parameter_array = null;
        try{
            parameter_array = ParameterParser.split_redirection_parameter(parameter_parser.getParameterString().toString());
            FileProcessor.redirect_stdout(parameter_array);
        } catch (NoSuchFileException e){
            System.out.println(command +": "+ e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println(parameter_parser.getParameterString());
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
        //parameter_parser.getParameterList().stream().forEach(System.out::println);
    }

    static void main(String[] args) throws Exception {
        scanner = new Scanner(System.in);
        boolean alive = true;
        do {
            System.out.print("$ ");
            input = scanner.nextLine();
            //ParameterParser.split_raw_input(input);
            String[] input_raw = ParameterParser.split_raw_input(input);
            command = input_raw[0];
            parameter = (input_raw.length != 1) ? input_raw[1]: "";
            parameter_parser.parse(parameter);
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
                    echo_command();
                    break;
                case "type":
                    type_command(parameter.toLowerCase());
                    break;
                default:
                    command_parser.parse(command);
                    command = command_parser.getParameterString().toString();
                    try{
                        String[] parameter_array = ParameterParser.split_redirection_parameter(parameter_parser.getParameterString().toString());
                        FileProcessor.redirect_stdout(parameter_array);
                    }catch (NoSuchFileException e) {
                        System.out.println(command +": "+ e.getMessage());
                    }catch (IOException e){
                        System.out.println(e.getMessage());
                    }  catch (RuntimeException e) {
                        Optional<Path> file = FileProcessor.find_executable_file_in_PATH(command);
                        if (file.equals(Optional.empty())) {
                            System.out.println(command + ": command not found");
                            continue;
                        }
                        Process process = FileProcessor.run_program(command, parameter_parser.getParameterList());
                        FileProcessor.print_output_from_file(process);
                    }
                    break;
            }
        }while(alive);
    }
}

import org.jline.builtins.Completers;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


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
        return file.map(path -> command + " is " + path).orElseGet(() -> command + ": not found");
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

    private static String working_directory(){
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
            System.out.println("cd: "+new_current_path+": No such file or directory");
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
            parameter_array = ParameterParser.split_redirection_parameter(parameter);
            FileProcessor.redirect(command ,parameter_array);
        } catch (NoSuchFileException e){
            System.out.println(command +": "+ e.getMessage());
        } catch (IllegalThreadStateException e){
            System.out.println(e.getMessage());
        } catch (InterruptedException e){
            System.out.println("Interrupted");
        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println(parameter_parser.getParameterString());
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
        //parameter_parser.getParameterList().stream().forEach(System.out::println);
    }

    static void handle_main_command() throws IOException {
        switch (command) {
            case "pwd":
                System.out.println(working_directory());
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
                    String[] parameter_array = ParameterParser.split_redirection_parameter(parameter);
                    FileProcessor.redirect(command, parameter_array);
                } catch (IllegalThreadStateException e){
                    System.out.println(e.getMessage());
                } catch (InterruptedException e){
                    System.out.println("Interrupted");
                } catch (NoSuchElementException e){
                    System.out.println(e.getMessage());
                } catch (RuntimeException e) {
                    Optional<Path> file = FileProcessor.find_executable_file_in_PATH(command);
                    if (file.equals(Optional.empty())) {
                        System.out.println(command + ": command not found");
                        break;
                    }
                    Process process = FileProcessor.run_program(command, parameter_parser.getParameterList());
                    FileProcessor.print_output_from_file(process);
                    break;
                }
        }
    }

    void main(String[] args){
        try {
            DefaultParser parser = new DefaultParser();
            parser.setEscapeChars(new char[0]);
            List<String> completer_string = new LinkedList<>();
            completer_string.add("echo");
            completer_string.add("exit");
            FileProcessor.get_executable_files().stream().map(file -> file.getFileName().toString()).forEach(completer_string::add);
            AggregateCompleter completer = new AggregateCompleter(
                    new StringsCompleter(completer_string),
                    new Completers.FilesCompleter(Paths.get("PATH")));

            Terminal terminal = TerminalBuilder.builder().system(true).build();
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(completer)
                    .parser(parser)
                    .build();

            do {
                input = reader.readLine("$ ");
                String[] input_raw = ParameterParser.split_raw_input(input);
                command = input_raw[0];
                parameter = (input_raw.length != 1) ? input_raw[1] : "";
                parameter_parser.parse(parameter);

                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }

                handle_main_command();

                terminal.flush();
            } while (true);
                terminal.close();
        } catch (IOException e) {
            System.err.println("Error creating terminal: " + e.getMessage());
        }
    }
}

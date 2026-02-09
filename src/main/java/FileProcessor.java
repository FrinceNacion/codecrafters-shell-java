import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FileProcessor {
    static String[] path_environment_directories = System.getenv("PATH").split(File.pathSeparator);
    // TODO: migrate file-related functions here
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

    // Finds executable file in the PATH variable
    // if nothing, return Optional.empty()
    public static Optional<Path> find_executable_file_in_PATH(String file_name){
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

    public static Process run_program(String program_name, LinkedList<String> parameters){

        List<String> command = new ArrayList<>();

        command.add(program_name);
        parameters.stream()
                .filter(str -> !str.isBlank())
                .forEach(command::add);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            Process process = processBuilder.start();
            return process;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void print_output_from_file(Process process){
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public static void handle_redirection(){

    }

    public static void redirect_stdout(String[] parameter_array) throws IOException {

        String executable_parameter = parameter_array[0];
        String output_parameter = parameter_array[1];
        String command = parameter_array[2];

        // check for executable
        Optional<Path> executable_file = find_executable_file_in_PATH(executable_parameter);
        if (executable_file.equals(Optional.empty())){
            throw new NoSuchFileException(executable_parameter+": No such file or directory");
        }
        Process process = run_program(executable_file.get().toString(), new LinkedList<>());

        //Optional<Path> output_file = find_executable_file_in_PATH(output_parameter);
        Files.write(Path.of(output_parameter), process.getInputStream().readAllBytes());
    }

}


import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class FileProcessor {
    static String[] path_environment_directories = System.getenv("PATH").split(File.pathSeparator);

    private static List<Path> find_executable_files_by_prefix(Path directory, String prefix){
        List<Path> result = new LinkedList<>();
        try (Stream<Path> stream = Files.list(directory)) {
            stream.filter(Files::isRegularFile)
                    .filter(Files::isExecutable)
                    .filter(files -> files.getFileName().toString().startsWith(prefix))
                    .forEach(result::add);
            return  result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Path> find_all_executable_files(Path directory){
        List<Path> result = new LinkedList<>();
        try (Stream<Path> stream = Files.list(directory)) {
            stream.filter(Files::isRegularFile)
                    .filter(Files::isExecutable)
                    .forEach(result::add);
            return  result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Path> get_executable_files(){
        List<Path> result = new LinkedList<>();
        for(String current_path_raw : path_environment_directories){
            Path current_path = Path.of(current_path_raw);
            if (!Files.exists(current_path) && !Files.isDirectory(current_path)) {
                continue;
            }

            find_all_executable_files(current_path).stream().forEach(result::add);
            if (!result.equals(Optional.empty())){break;}
        }
        return result;
    }

    private static Optional<Path> find_executable_file(String file_name, Path directory){
        try (Stream<Path> stream = Files.list(directory)) {
            Optional<Path> file = stream.filter(Files::isRegularFile)
                    .filter(files -> files.getFileName().toString().equals(file_name))
                    .filter(Files::isExecutable)
                    .findFirst();
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Finds executable file in the PATH variable
    // if nothing, return Optional.empty()
    public static Optional<Path> get_executable_file_in_PATH(String file_name){
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

    public static void print_error_from_file(Process process){
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e){
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
}

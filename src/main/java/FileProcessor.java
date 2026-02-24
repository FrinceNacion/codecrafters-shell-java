
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class FileProcessor {
    static String[] path_environment_directories = System.getenv("PATH").split(File.pathSeparator);

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

    private static void stdout(Process process, Path output_path, Optional<String> string_output, boolean is_append) throws IOException, InterruptedException {
        ByteArrayOutputStream to_write = new ByteArrayOutputStream();
        to_write.write("".getBytes());


        if (is_append && Files.exists(output_path)){
            to_write.write(Files.readAllBytes(output_path));
            if (!to_write.toString().endsWith("\n")){
                to_write.write("\n".getBytes());
            }
        }

        if (string_output.isPresent()){
            to_write.write(string_output.get().getBytes());
            Files.write(output_path,to_write.toByteArray());
            return;
        }

        to_write.write(process.getInputStream().readAllBytes());
        Files.write(output_path,to_write.toByteArray());
        int exit_value = process.waitFor();
        if (exit_value != 0){
            String error = new String(process.getErrorStream().readAllBytes());
            if (error.isEmpty()){
                return;
            }
            throw new IllegalThreadStateException(error.strip());
        }

    }

    public static void redirect_stdout(String command,String[] parameter_array, boolean is_append) throws IOException, IllegalThreadStateException, InterruptedException {
        if (parameter_array == null){
            throw new NullPointerException();
        }
        LinkedList<String> parameters = new LinkedList<>();
        ParameterParser parameterParser = new ParameterParser();
        Process process;

        String executable_parameter = parameter_array[0];
        String output_parameter = parameter_array[1].strip();

        Path output_path = Path.of(output_parameter);
        Path output_parent = Main.current_directory, output_file = output_path.getFileName();

        if (output_path.getParent() != null){
            output_parent = output_path.getParent();
        }

        if (output_parent != null && !Files.exists(output_parent)){
            Files.createDirectories(output_parent);
        }

        output_path = output_parent.resolve(output_file);

        boolean in_qoutes = (executable_parameter.startsWith("'") && executable_parameter.endsWith("'")) || (executable_parameter.startsWith("\"") && executable_parameter.endsWith("\""));
        if (in_qoutes){
            parameterParser.parse(executable_parameter);
            String output_string = parameterParser.getParameterString().toString();
            stdout(null, output_path, Optional.of(output_string), is_append);
            return;
        }

        // check for executable
        if (command.equals("echo")){
            Optional<Path> executable_file = find_executable_file_in_PATH(executable_parameter);
            try{
                command = executable_file.get().toString();
            } catch (NoSuchElementException e){

            }
        }else{
            parameterParser.parse(executable_parameter);
            parameters = parameterParser.getParameterList();
        }

        process = run_program(command, parameters);
        stdout(process, output_path, Optional.empty(), is_append);
    }

    private static void stderr(Process process, Path output_path, Optional<String> string_output, boolean is_append) throws IOException, InterruptedException {
        if (string_output.isPresent()){
            System.out.println(string_output.get());
            Files.writeString(output_path, "");
            return;
        }

        ByteArrayOutputStream to_write = new ByteArrayOutputStream();
        to_write.write("".getBytes());
        if (is_append && Files.exists(output_path)){
            to_write.write(Files.readAllBytes(output_path));
        }

        int exit_value = process.waitFor();
        if (exit_value != 0){
            to_write.write(process.getErrorStream().readAllBytes());
            Files.write(output_path, to_write.toByteArray());
        }
        print_output_from_file(process);
    }

    private static void redirect_stderr(String command,String[] parameter_array, boolean is_append) throws IOException, InterruptedException {
        if (parameter_array == null){
            throw new NullPointerException();
        }
        LinkedList<String> parameters = new LinkedList<>();
        ParameterParser parameterParser = new ParameterParser();
        Process process;

        String executable_parameter = parameter_array[0];
        String output_parameter = parameter_array[1].strip();

        Path output_path = Path.of(output_parameter);
        Path output_parent = Main.current_directory, output_file = output_path.getFileName();

        if (output_path.getParent() != null){
            output_parent = output_path.getParent();
        }

        if (output_parent != null && !Files.exists(output_parent)){
            Files.createDirectories(output_parent);
        }

        output_path = output_parent.resolve(output_file);

        boolean in_qoutes = (executable_parameter.startsWith("'") && executable_parameter.endsWith("'")) || (executable_parameter.startsWith("\"") && executable_parameter.endsWith("\""));
        if (in_qoutes){
            parameterParser.parse(executable_parameter);
            String output_string = parameterParser.getParameterString().toString();
            stderr(null, output_path, Optional.of(output_string), is_append);
            return;
        }

        // check for executable
        if (command.equals("echo")){
            Optional<Path> executable_file = find_executable_file_in_PATH(executable_parameter);
            command = executable_file.get().toString();
        }else{
            parameterParser.parse(executable_parameter);
            parameters = parameterParser.getParameterList();
        }

        process = run_program(command, parameters);
        stderr(process, output_path, Optional.empty(), is_append);
    }

    public static void redirect(String command, String[] parameter_array) throws IOException, InterruptedException {
        String redirect_type = parameter_array[2];
        if (redirect_type.equals(">") || redirect_type.equals("1>")){
            redirect_stdout(command, parameter_array, false);
        } else if (redirect_type.equals("2>")){
            redirect_stderr(command, parameter_array, false);
        } else if (redirect_type.equals("1>>") || redirect_type.equals(">>")){
            redirect_stdout(command, parameter_array, true);
        } else if (redirect_type.equals("2>>")){
            redirect_stderr(command, parameter_array, true);
        } else{
            System.out.println("Type of redirect not detected");
        }
    }
}

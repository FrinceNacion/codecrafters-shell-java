import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Optional;

public class RedirectionHandler {

    private static void stdout(Process process, OutputStream output_target, Optional<String> string_output, boolean is_append) throws IOException, InterruptedException {
        if (string_output.isPresent()){
            output_target.write(string_output.get().getBytes(StandardCharsets.UTF_8));
            output_target.flush();
            return;
        }

        try(InputStream input_stream = process.getInputStream()){
            input_stream.transferTo(output_target);
        }

        int exit_value = process.waitFor();

        if (exit_value != 0){
            String error = new String(process.getErrorStream().readAllBytes());
            if (!error.isEmpty()){
                throw new IllegalThreadStateException(error.strip());
            }
        }

        output_target.flush();
    }

    public static void redirect_stdout(String command,String[] parameter_array, boolean is_append) throws IOException, IllegalThreadStateException, InterruptedException {
        if (parameter_array == null){
            throw new NullPointerException();
        }
        LinkedList<String> parameters = new LinkedList<>();
        ParameterParser parameterParser = new ParameterParser();
        Process process;

        String executable_parameter = parameter_array[ParameterParser.EXECUTABLE_COMMAND_PARAMETER];
        String output_parameter = parameter_array[ParameterParser.EXECUTABLE_OUTPUT_PARAMETER].strip();

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
            try (OutputStream file = Files.newOutputStream(
                    output_path,
                    is_append ? StandardOpenOption.APPEND : StandardOpenOption.CREATE
            )) {
                stdout(null, file, Optional.of(output_string), is_append);
            }
            return;
        }

        // check for executable
        if (command.equals("echo")){
            Optional<Path> executable_file = FileProcessor.get_executable_file_in_PATH(executable_parameter);
            try{
                command = executable_file.get().toString();
            } catch (NoSuchElementException e){

            }
        }else{
            parameterParser.parse(executable_parameter);
            parameters = parameterParser.getParameterList();
        }

        process = FileProcessor.run_program(command, parameters);
        try (OutputStream file = Files.newOutputStream(
                output_path,
                is_append ? StandardOpenOption.APPEND : StandardOpenOption.CREATE
        )) {
            stdout(process, file, Optional.empty(), is_append);
        }
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
        FileProcessor.print_output_from_file(process);
    }

    private static void redirect_stderr(String command,String[] parameter_array, boolean is_append) throws IOException, InterruptedException {
        if (parameter_array == null){
            throw new NullPointerException();
        }
        LinkedList<String> parameters = new LinkedList<>();
        ParameterParser parameterParser = new ParameterParser();
        Process process;

        String executable_parameter = parameter_array[ParameterParser.EXECUTABLE_COMMAND_PARAMETER];
        String output_parameter = parameter_array[ParameterParser.EXECUTABLE_OUTPUT_PARAMETER].strip();

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
            Optional<Path> executable_file = FileProcessor.get_executable_file_in_PATH(executable_parameter);
            command = executable_file.get().toString();
        }else{
            parameterParser.parse(executable_parameter);
            parameters = parameterParser.getParameterList();
        }

        process = FileProcessor.run_program(command, parameters);
        stderr(process, output_path, Optional.empty(), is_append);
    }

    public static void redirect(String command, String[] parameter_array) throws IOException, InterruptedException {
        String redirect_type = parameter_array[ParameterParser.COMMAND_OPERATOR];
        if (redirect_type.equals(">") || redirect_type.equals("1>")){
            redirect_stdout(command, parameter_array, false);
        } else if (redirect_type.equals("2>")){
            redirect_stderr(command, parameter_array, false);
        } else if (redirect_type.equals("1>>") || redirect_type.equals(">>")){
            redirect_stdout(command, parameter_array, true);
        } else if (redirect_type.equals("2>>")){
            redirect_stderr(command, parameter_array, true);
        } else if (redirect_type.equals("|")){
            PipelineHandler.pipe(command, parameter_array);
        } else{
            System.out.println("Type of redirect not detected");
        }
    }
}

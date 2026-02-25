import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Optional;

public class RedirectionHandler {

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
        FileProcessor.print_output_from_file(process);
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

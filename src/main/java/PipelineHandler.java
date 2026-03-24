import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Optional;

public class PipelineHandler {
    private static final ParameterParser parameter_parser = new ParameterParser();

    public static void pipe(String command, String[] parameter_array) throws NoSuchElementException {
        ParameterParser parameterParser = new ParameterParser();
        LinkedList<String> parameters = new LinkedList<>();

        String executable_parameter = parameter_array[ParameterParser.EXECUTABLE_COMMAND_PARAMETER];
        String output_parameter = parameter_array[ParameterParser.EXECUTABLE_OUTPUT_PARAMETER].strip();

        String pipe = parameter_array[ParameterParser.COMMAND_OPERATOR];

        Optional<Path> executable_command = FileProcessor.get_executable_file_in_PATH(command);
        Optional<Path> pipeline_command = FileProcessor.get_executable_file_in_PATH(parameter_array[1]);

        if (command.equals("echo")){
            Optional<Path> executable_file = FileProcessor.get_executable_file_in_PATH(executable_parameter);
            command = executable_file.get().toString();
        }else{
            parameterParser.parse(executable_parameter);
            parameters = parameterParser.getParameterList();
        }

        Process process = FileProcessor.run_program(command, parameters);

    }
}

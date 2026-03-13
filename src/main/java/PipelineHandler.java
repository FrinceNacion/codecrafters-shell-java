import java.nio.file.Path;
import java.util.Optional;

public class PipelineHandler {
    private static final ParameterParser parameter_parser = new ParameterParser();
    private static void pipe(String command, String[] parameter_array){
        String pipe = parameter_array[ParameterParser.COMMAND_OPERATOR];
        if (!pipe.equals("|")){
            System.out.println("No pipe detected");
            return;
        }

        Optional<Path> executable_command = FileProcessor.get_executable_file_in_PATH(command);
        Optional<Path> pipeline_command = FileProcessor.get_executable_file_in_PATH(parameter_array[1]);

        if (executable_command.isEmpty() || pipeline_command.isEmpty()){
            System.out.println("Command/s not found");
            return;
        }

        parameter_parser.parse(parameter_array[ParameterParser.EXECUTABLE_COMMAND_PARAMETER]);
        Process main_command_process = FileProcessor.run_program(executable_command.toString(), parameter_parser.getParameterList());

        // TODO: Handle pipeline.
    }
}

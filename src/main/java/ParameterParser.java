import java.util.*;

public class ParameterParser {
    private final LinkedList<String> parameterList = new LinkedList<>();
    private final StringBuilder parameterString = new StringBuilder();
    private static final int id_num = new Random().nextInt();
    private static final String[] escapable_characters = {"\\", "$", "\"", "`", "\n"};
    private static final String[] redirection_commands = {"1>", ">", "2>", ">>"};

    // Splits the parameters to 3 section and validate for redirection
    // if validation fails, it returns null or an error if encountered.
    public static String[] split_redirection_parameter(String parameter) throws ArrayIndexOutOfBoundsException{
        String[] parameter_list = new String[3];
        int index = Math.max(parameter.indexOf(" > "), parameter.indexOf(" 1> "));

        if (index == -1){
            throw new ArrayIndexOutOfBoundsException();
        }

        String executable_parameter = parameter.substring(0, index);
        String output_parameter = parameter.substring(index+3);

        /**boolean is_command_present_or_valid = Arrays.asList(redirection_commands).contains(command);
        if (!is_command_present_or_valid){
            return null;
        }**/

        parameter_list[0] = executable_parameter;
        parameter_list[1] = output_parameter;

        return parameter_list;
    }


    public static String[] split_raw_input(String raw_input){
        StringBuilder raw = new StringBuilder(raw_input);
        String qoute_type = encode_break();
        boolean is_escaped = false;
        boolean in_qoutes = false;
        for (int i = 0; i < raw_input.toCharArray().length; i++) {
            char character = raw.charAt(i);
            if (is_escaped){
                is_escaped = false;
                continue;
            }
            if ((character == '\'' || character == '"') && qoute_type != encode_break()){
                // To insert double qoute character inside a single qoute block without escaping
                if (qoute_type.equals("'") && character != '\''){
                    continue;
                }
                // To insert single qoute character inside a double qoute block
                // w/o escaping and establishing a single qoute block
                if (qoute_type.equals("\"") && character == '\''){
                    continue;
                }

                qoute_type = String.valueOf(character);
            }
            if (character == '\\' && !qoute_type.equals("'")) {
                is_escaped = true;
                continue;
            }
            // For encountering the first qoute
            if (character == qoute_type.charAt(0)){
                // Encountering the second qoute of the same type
                if (in_qoutes){
                    qoute_type = encode_break();
                }
                in_qoutes = !in_qoutes;
                continue;
            }
            if (character == ' ' && !in_qoutes){
                raw.deleteCharAt(i);
                raw.insert(i, encode_break());
                break;
            }
        }
        //System.out.println(raw.toString());
        return raw.toString().split("\\[Space-"+get_space_id()+"\\]", 2);
    }

    private static String encode_break(){
        return String.format("[Space-%d]", id_num).toString();
    }

    public static int get_space_id(){
        return id_num;
    }

    public static boolean is_escapable_within_double_qoute(char character){
        return Arrays.stream(escapable_characters).anyMatch( c-> c.contains(character + ""));
    }

    public void parse(String parameter){
        if (parameter.isBlank()){
            return;
        }

        getParameterList().clear();
        getParameterString().setLength(0);
        String qoute_type = encode_break();
        boolean is_escaped = false;
        boolean in_qoutes = false;
        String temp_string = "";
        StringBuilder temp_escaped_container = new StringBuilder();
        StringBuilder inside = new StringBuilder();
        StringBuilder outside = new StringBuilder();


        for(char character : parameter.toCharArray()){
            // For escaped characters
            if (is_escaped){
                if (in_qoutes){
                    if (is_escapable_within_double_qoute(character)){
                        temp_escaped_container.setLength(0);
                        temp_escaped_container.append(character);
                    }else{
                        temp_escaped_container.append(character);
                    }
                    inside.append(temp_escaped_container);
                    temp_escaped_container.setLength(0);
                }else{
                    outside.append((character == ' ') ? "\\ " : character);
                }
                is_escaped = false;
                continue;
            }
            // For establishing the encountering qoute character outside and inside the qoute
            if ((character == '\'' || character == '"') && qoute_type != encode_break()){
                // To insert double qoute character inside a single qoute block without escaping
                if (qoute_type.equals("'") && character != '\''){
                    inside.append(character);
                    continue;
                }
                // To insert single qoute character inside a double qoute block
                // w/o escaping and establishing a single qoute block
                if (qoute_type.equals("\"") && character == '\''){
                    inside.append(character);
                    continue;
                }

                qoute_type = String.valueOf(character);
            }
            // For backslash to escape the following character
            if (character == '\\' && !qoute_type.equals("'")) {
                temp_escaped_container.append(character);
                is_escaped = true;
                continue;
            }
            // For encountering the first qoute
            if (character == qoute_type.charAt(0)){
                // Encountering the second qoute of the same type
                if (in_qoutes){
                    getParameterString().append(inside.toString());
                    qoute_type = encode_break();
                    inside.setLength(0);
                }
                // Append the outside string before the first qoute
                if (outside.length() > 0){
                    temp_string = outside.toString().replaceAll("(?<!\\\\)\\s+", encode_break());
                    temp_string = temp_string.replaceAll("\\\\ ", " ");
                    getParameterString().append(temp_string);
                    //Arrays.stream(temp_string.split("(?<!\\\\)\\s+")).forEach(getParameterList()::add);
                    outside.setLength(0);
                }
                in_qoutes = !in_qoutes;
            } else if (in_qoutes) {
                inside.append(character);
            }else{
                if(is_escaped){
                    continue;
                }
                outside.append(character);
            }
        }
        if (outside.length() > 0){
            temp_string = outside.toString().replaceAll("(?<!\\\\)\\s+", encode_break());
            temp_string = temp_string.replaceAll("\\\\ ", " ");
            getParameterString().append(temp_string);
            //Arrays.stream(temp_string.split("(?<!\\\\)\\s+")).forEach(getParameterList()::add);
            outside.setLength(0);
        }
        if (in_qoutes){
            getParameterString().append(inside.toString());
        }

        Arrays.stream(getParameterString().toString()
                .split(String.format("\\[Space-"+get_space_id()+"\\]")))
                .forEach(this.getParameterList()::add);
        temp_string =  getParameterString().toString().replaceAll(String.format("\\[Space-"+get_space_id()+"\\]"), "\s");
        getParameterString().setLength(0);
        getParameterString().append(temp_string);
    }


    public static String parse_string(String parameter){
        if (parameter.isBlank()){
            return "";
        }

        String qoute_type = encode_break();
        boolean is_escaped = false;
        boolean in_qoutes = false;
        String temp_string = "";
        StringBuilder temp_escaped_container = new StringBuilder();
        StringBuilder result = new StringBuilder();
        StringBuilder inside = new StringBuilder();
        StringBuilder outside = new StringBuilder();


        for(char character : parameter.toCharArray()){
            // For escaped characters
            if (is_escaped){
                if (in_qoutes){
                    if (is_escapable_within_double_qoute(character)){
                        temp_escaped_container.setLength(0);
                        temp_escaped_container.append(character);
                    }else{
                        temp_escaped_container.append(character);
                    }
                    inside.append(temp_escaped_container);
                    temp_escaped_container.setLength(0);
                }else{
                    outside.append((character == ' ') ? "\\ " : character);
                }
                is_escaped = false;
                continue;
            }
            // For establishing the encountering qoute character outside and inside the qoute
            if ((character == '\'' || character == '"') && qoute_type != encode_break()){
                // To insert double qoute character inside a single qoute block without escaping
                if (qoute_type.equals("'") && character != '\''){
                    inside.append(character);
                    continue;
                }
                // To insert single qoute character inside a double qoute block
                // w/o escaping and establishing a single qoute block
                if (qoute_type.equals("\"") && character == '\''){
                    inside.append(character);
                    continue;
                }

                qoute_type = String.valueOf(character);
            }
            // For backslash to escape the following character
            if (character == '\\' && !qoute_type.equals("'")) {
                temp_escaped_container.append(character);
                is_escaped = true;
                continue;
            }
            // For encountering the first qoute
            if (character == qoute_type.charAt(0)){
                // Encountering the second qoute of the same type
                if (in_qoutes){
                    result.append(inside.toString());
                    qoute_type = encode_break();
                    inside.setLength(0);
                }
                // Append the outside string before the first qoute
                if (outside.length() > 0){
                    temp_string = outside.toString().replaceAll("(?<!\\\\)\\s+", encode_break());
                    temp_string = temp_string.replaceAll("\\\\ ", " ");
                    result.append(temp_string);
                    //Arrays.stream(temp_string.split("(?<!\\\\)\\s+")).forEach(getParameterList()::add);
                    outside.setLength(0);
                }
                in_qoutes = !in_qoutes;
            } else if (in_qoutes) {
                inside.append(character);
            }else{
                if(is_escaped){
                    continue;
                }
                outside.append(character);
            }
        }
        if (outside.length() > 0){
            temp_string = outside.toString().replaceAll("(?<!\\\\)\\s+", encode_break());
            temp_string = temp_string.replaceAll("\\\\ ", " ");
            result.append(temp_string);
            outside.setLength(0);
        }
        if (in_qoutes){
            result.append(inside.toString());
        }

        return temp_string.toString();
    }

    public LinkedList<String> getParameterList() {
        return parameterList;
    }

    public StringBuilder getParameterString() {
        return parameterString;
    }
}

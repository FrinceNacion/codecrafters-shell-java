import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Random;

public class ParameterParser {
    private static final LinkedList<String> parameterList = new LinkedList<>();
    private static final StringBuilder parameterString = new StringBuilder();
    private static final int id_num = new Random().nextInt();
    private static final String[] escapable_characters = {"\\", "$", "\"", "`", "\n"};

    private static String encode_break(){
        return String.format("[Space-%d]", id_num).toString();
    }

    public static int get_space_id(){
        return id_num;
    }

    public static void parse(String parameter){
        if (parameter.isBlank()){
            return;
        }

        getParameterList().clear();
        getParameterString().setLength(0);
        char qoute_type = '0';
        boolean is_escaped = false;
        boolean in_qoutes = false;
        String temp_string = "";
        StringBuilder temp_escaped_container = new StringBuilder();
        StringBuilder inside = new StringBuilder();
        StringBuilder outside = new StringBuilder();


        for(char character : parameter.toCharArray()){
            if (is_escaped){
                if (in_qoutes){
                    boolean is_present =  Arrays.stream(escapable_characters).filter(c -> c.contains(character + "")).findFirst().isPresent();
                    System.out.println(is_present);
                    if (is_present){
                        temp_escaped_container.setLength(0);
                        temp_escaped_container.append(character);
                    }
                    inside.append(temp_escaped_container);
                }else{
                    outside.append((character == ' ') ? "\\ " : character);
                }
                is_escaped = false;
                continue;
            }
            if ((character == '\'' || character == '"') && qoute_type == '0'){
                qoute_type = character;
                if (in_qoutes){
                    temp_escaped_container.append(character);
                    continue;
                }
            }
            if (character == '\\' && qoute_type != '\'') {
                is_escaped = true;
                continue;
            }
            if (character == qoute_type){
                if (in_qoutes){
                    getParameterString().append(inside.toString());
                    qoute_type = '0';
                    inside.setLength(0);
                }
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
                .forEach(getParameterList()::add);
        temp_string =  getParameterString().toString().replaceAll(String.format("\\[Space-"+get_space_id()+"\\]"), "\s");
        getParameterString().setLength(0);
        getParameterString().append(temp_string);
    }

    public static LinkedList<String> getParameterList() {
        return parameterList;
    }

    public static StringBuilder getParameterString() {
        return parameterString;
    }
}

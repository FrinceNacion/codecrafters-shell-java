import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Pattern;

public class ParameterParser {
    private static final LinkedList<String> parameterList = new LinkedList<>();
    private static final StringBuilder parameterString = new StringBuilder();
    private static final int id_num = new Random().nextInt();

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
        boolean is_break = false;
        boolean in_qoutes = false;
        String temp = "";
        StringBuilder inside = new StringBuilder();
        StringBuilder outside = new StringBuilder();


        for(char character : parameter.toCharArray()){
            if (is_escaped){
                outside.append((character == ' ') ? "\\ " : character);
                is_escaped = false;
                continue;
            }
            if ((character == '\'' || character == '"') && qoute_type == '0'){
                qoute_type = character;
            }
            if (character == '\\' && !in_qoutes) {
                is_escaped = true;
            }
            if (character == qoute_type){
                if (in_qoutes){
                    getParameterString().append(inside.toString());
                    qoute_type = '0';
                    inside.setLength(0);
                }
                if (outside.length() > 0){
                    temp = outside.toString().replaceAll("(?<!\\\\)\\s+", encode_break());
                    getParameterString().append(temp);
                    //Arrays.stream(temp.split("(?<!\\\\)\\s+")).forEach(getParameterList()::add);
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
            temp = outside.toString().replaceAll("(?<!\\\\)\\s+", encode_break());
            getParameterString().append(temp);
            //Arrays.stream(temp.split("(?<!\\\\)\\s+")).forEach(getParameterList()::add);
            outside.setLength(0);
        }
        if (in_qoutes){
            getParameterString().append(inside.toString());
        }

        Arrays.stream(getParameterString().toString().split(String.format("\\[Space-"+get_space_id()+"\\]"))).forEach(getParameterList()::add);

    }

    public static LinkedList<String> getParameterList() {
        return parameterList;
    }

    public static StringBuilder getParameterString() {
        return parameterString;
    }
}

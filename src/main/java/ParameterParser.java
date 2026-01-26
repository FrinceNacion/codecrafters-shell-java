import java.util.LinkedList;

public class ParameterParser {
    private static final LinkedList<String> parameterList = new LinkedList<>();
    private static final StringBuilder parameterString = new StringBuilder();

    public static void parse(String parameter){
        if (parameter.isEmpty() || parameter.isBlank()){
            return;
        }

        getParameterList().clear();
        getParameterString().setLength(0);
        char qoute_type = '0';
        boolean is_escaped = false;
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
            if((character == '\'' || character == '"') && qoute_type == '0'){
                qoute_type = character;
            }
            if (character == '\\' && !in_qoutes) {
                is_escaped = true;
            }
            if (character == qoute_type){
                if (in_qoutes){
                    getParameterString().append(inside.toString());
                    getParameterList().add(inside.toString());
                    qoute_type = '0';
                    inside.setLength(0);
                }
                if (outside.length() > 0){
                    temp = outside.toString().replaceAll("(?<!\\\\)\\s+", " ");
                    temp = temp.replaceAll("\\\\\\s", " ");
                    getParameterString().append(temp);
                    getParameterList().add(temp);
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
            temp = outside.toString().replaceAll("(?<!\\\\)\\s+", " ");
            temp = temp.replaceAll("\\\\\\s", " ");
            getParameterString().append(temp);
            getParameterList().add(temp);
            outside.setLength(0);
        }
        if (in_qoutes){
            getParameterString().append(inside.toString());
        }
    }

    public static LinkedList<String> getParameterList() {
        return parameterList;
    }

    public static StringBuilder getParameterString() {
        return parameterString;
    }
}

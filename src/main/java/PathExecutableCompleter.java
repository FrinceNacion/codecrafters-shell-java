import org.jline.reader.*;
import java.util.*;

public class PathExecutableCompleter implements Completer {

    private String lastPrefix = null;
    private boolean firstTab = true;
    private List<String> cachedMatches = new ArrayList<>();

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {

        String prefix = line.word();

        if (!prefix.equals(lastPrefix)) {
            firstTab = true;
        }

        List<String> matches = FileProcessor.find_executable_files_by_prefix(prefix);


        if (matches.size() <= 1) {
            // Normal completion
            for (String m : matches) {
                candidates.add(new Candidate(m));
            }
            firstTab = true;
            lastPrefix = prefix;
            return;
        }

        String longest_common_prefix = "";
        for (String m : matches){
            if (longest_common_prefix.isEmpty()){
                longest_common_prefix = m;
            }
        }


        if (firstTab) {
            // Ring bell
            reader.getTerminal().writer().print("\u0007");
            reader.getTerminal().flush();

            cachedMatches = matches;
            firstTab = false;
            lastPrefix = prefix;

            return;
        }
        Collections.sort(cachedMatches);

        reader.getTerminal().writer().println();

        for (int i = 0; i < cachedMatches.size(); i++) {
            reader.getTerminal().writer().print(cachedMatches.get(i));

            if (i < cachedMatches.size() - 1) {
                reader.getTerminal().writer().print("  ");
            }
        }

        reader.getTerminal().writer().println();
        reader.getTerminal().flush();

        reader.callWidget(LineReader.REDRAW_LINE);
        reader.callWidget(LineReader.REDISPLAY);

        firstTab = true;
        lastPrefix = prefix;
    }
}
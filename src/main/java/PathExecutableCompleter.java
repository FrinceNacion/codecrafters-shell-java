import org.jline.reader.*;
import java.util.*;

public class PathExecutableCompleter implements Completer {

    private String last_prefix = "";
    private boolean first_tab = true;
    private List<String> cached_matches = new ArrayList<>();

    private String parseLCP(List<String> matches, String prefix){
        if (matches.isEmpty()) return prefix;
        String longest_common_prefix = matches.get(0);
        for (int i = 1; i < matches.size(); i++) {
            String match = matches.get(i);
            int min_len = Math.min(longest_common_prefix.length(), match.length());
            int j = 0;
            while (min_len > j && longest_common_prefix.charAt(j) == match.charAt(j)) j++;
            longest_common_prefix = longest_common_prefix.substring(0, j);
        }
        return longest_common_prefix;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {

        String prefix = line.word();

        if (!prefix.equals(last_prefix)) {
            first_tab = true;
        }

        List<String> matches = FileProcessor.find_executable_files_by_prefix(prefix);

        if (matches.size() <= 1) {
            // Normal completion
            for (String match : matches) {
                candidates.add(new Candidate(match));
            }
            first_tab = true;
            last_prefix = prefix;
            return;
        }

        String longest_common_prefix = parseLCP(matches, prefix).stripTrailing();
        candidates.add(new Candidate(
                longest_common_prefix,
                longest_common_prefix,
                null,
                null,
                null,
                null,
                false
        ));
        //prefix = line.word();

        if (first_tab) {
            // Ring bell
            reader.getTerminal().writer().print("\u0007");
            reader.getTerminal().flush();

            cached_matches = matches;
            first_tab = false;
            last_prefix = prefix;

            return;
        }
        Collections.sort(cached_matches);

        reader.getTerminal().writer().println();

        for (int i = 0; i < cached_matches.size(); i++) {
            reader.getTerminal().writer().print(cached_matches.get(i));

            if (i < cached_matches.size() - 1) {
                reader.getTerminal().writer().print("  ");
            }
        }

        reader.getTerminal().writer().println();
        reader.getTerminal().flush();

        reader.callWidget(LineReader.REDRAW_LINE);
        reader.callWidget(LineReader.REDISPLAY);

        first_tab = true;
        last_prefix = prefix;
    }
}
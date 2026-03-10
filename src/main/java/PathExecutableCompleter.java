import org.jline.reader.*;
import java.util.*;

public class PathExecutableCompleter implements Completer {

    private String lastPrefix = "";
    private boolean firstTab = true;
    private List<String> cachedMatches = new ArrayList<>();

    private String lcp(List<String> matches, String prefix){
        String c = lastPrefix;
        for (String m : matches){
            if (c.isEmpty()){
                c = m;
                continue;
            }

            int prev_pref = (c.length()-prefix.length());
            int curr_pref = (m.length()-prefix.length());
            //System.out.println("Last prefix: "+c);
            //System.out.println("Current prefix: "+m);
            //System.out.println("Prev length: "+prev_pref);
            //System.out.println("Curr length: "+curr_pref);

            if (curr_pref <= 0){
                continue;
            }
            if (prev_pref == 0){
                prev_pref = c.length();
            }
            // if prev lcp is greater than current lcp, then curr lcp is next candidate
            // problem: prev lcp is 0
            if (prev_pref > curr_pref){
                c = m;
                //System.out.println("New: "+c);
            }
        }
        lastPrefix = c;
        return c;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {

        String prefix = line.word();

        if (!prefix.equals(lastPrefix)) {
            firstTab = true;
        }

        List<String> matches = FileProcessor.find_executable_files_by_prefix(prefix);

        if (matches.size() <= 1) {
            // Normal completion
            for (String match : matches) {
                candidates.add(new Candidate(match));
            }
            firstTab = true;
            lastPrefix = prefix;
            return;
        }

        if (firstTab) {
            // Ring bell
            reader.getTerminal().writer().print("\u0007");
            reader.getTerminal().flush();

            String lcp = lcp(matches, prefix).stripTrailing();
            candidates.add(new Candidate(
                    lcp,
                    lcp,
                    null,
                    null,
                    null,
                    null,
                    false
            ));;

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
import org.jline.reader.*;
import java.io.File;
import java.util.*;

public class PathExecutableCompleter implements Completer {

    private String lastPrefix = null;
    private boolean firstTab = true;

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {

        String prefix = line.word();
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

        // Multiple matches
        if (!prefix.equals(lastPrefix)) {
            firstTab = true;
        }

        if (firstTab) {
            // Ring bell
            reader.getTerminal().writer().print("\u0007");
            reader.getTerminal().flush();

            firstTab = false;
            lastPrefix = prefix;

            // Do NOT return candidates yet
            return;
        }

        // Second TAB → show matches
        Collections.sort(matches);
        for (String m : matches) {
            candidates.add(new Candidate(m));
        }

        firstTab = true;
        lastPrefix = prefix;
    }

    /*
    @Override
    public void complete(LineReader reader,
                         ParsedLine line,
                         List<Candidate> candidates) {

        String prefix = line.word();

        Set<String> matches = new HashSet<>();

        String pathEnv = System.getenv("PATH");
        String[] paths = pathEnv.split(File.pathSeparator);

        for (String path : paths) {

            File dir = new File(path);
            File[] files = dir.listFiles();

            if (files == null) continue;

            for (File f : files) {

                if (f.canExecute() && f.getName().startsWith(prefix)) {
                    matches.add(f.getName());
                }

            }
        }

        List<String> sorted = new ArrayList<>(matches);
        Collections.sort(sorted);

        for (String match : sorted) {
            candidates.add(new Candidate(match));
        }
    }*/
}
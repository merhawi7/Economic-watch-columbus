import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists EconomicSnapshot history to a local file, one line per snapshot.
 * New snapshots are appended, not overwritten — if a snapshot for the same
 * year already exists, it is replaced (so re-running an update for "this
 * year" corrects it) while all other years remain untouched.
 */
public class DataStore {

    private final Path filePath;

    public DataStore(String fileName) {
        this.filePath = Paths.get(fileName);
    }

    public List<EconomicSnapshot> loadAll() {
        List<EconomicSnapshot> snapshots = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return snapshots;
        }
        try {
            for (String line : Files.readAllLines(filePath)) {
                if (line.isBlank()) continue;
                snapshots.add(EconomicSnapshot.fromDataLine(line));
            }
        } catch (IOException e) {
            System.out.println("Warning: could not read history file: " + e.getMessage());
        }
        return snapshots;
    }

    /** Saves a snapshot, replacing any existing entry for the same year. */
    public void save(EconomicSnapshot snapshot) {
        List<EconomicSnapshot> all = loadAll();
        all.removeIf(s -> s.getYear() == snapshot.getYear());
        all.add(snapshot);
        all.sort((a, b) -> Integer.compare(a.getYear(), b.getYear()));

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            for (EconomicSnapshot s : all) {
                writer.write(s.toDataLine());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Warning: could not save history file: " + e.getMessage());
        }
    }

    public EconomicSnapshot getMostRecent() {
        List<EconomicSnapshot> all = loadAll();
        if (all.isEmpty()) return null;
        return all.get(all.size() - 1);
    }
}

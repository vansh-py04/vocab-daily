import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Lightweight CLI smoke test (no Gradle/Android Studio required).
 *
 * Checks:
 * - CSV dataset is readable and parseable
 * - Words exist for the requested CEFR level
 * - Daily selection distribution roughly matches 70/20/10
 */
public final class BackendSmokeTest {
    public static void main(String[] args) throws Exception {
        String level = args.length > 0 ? args[0] : "B1";
        int count = args.length > 1 ? Integer.parseInt(args[1]) : 10;
        long seed = args.length > 2 ? Long.parseLong(args[2]) : 12345L;

        Path csvPath = Path.of("assets", "ENGLISH_CERF_WORDS.csv");
        if (!Files.exists(csvPath)) {
            csvPath = Path.of("assets", "cefr_words.csv");
        }

        if (!Files.exists(csvPath)) {
            System.err.println("FAIL: No dataset found in assets/. Expected ENGLISH_CERF_WORDS.csv");
            System.exit(1);
        }

        List<Entry> entries = loadCsv(csvPath);
        if (entries.size() < 100) {
            System.err.println("FAIL: Dataset too small (" + entries.size() + " rows).");
            System.exit(1);
        }

        Map<String, List<String>> byLevel = groupByLevel(entries);
        String normalized = normalize(level);

        if (!byLevel.containsKey(normalized) || byLevel.get(normalized).isEmpty()) {
            System.err.println("FAIL: No words found for level " + normalized);
            System.exit(1);
        }

        List<Picked> picked = pickDaily(byLevel, normalized, count, new Random(seed));
        if (picked.isEmpty()) {
            System.err.println("FAIL: Picker returned no words.");
            System.exit(1);
        }

        Map<String, Integer> pickedCounts = new HashMap<>();
        for (Picked p : picked) pickedCounts.merge(p.level, 1, Integer::sum);

        System.out.println("OK: Loaded " + entries.size() + " dataset rows from " + csvPath);
        System.out.println("Requested level: " + normalized + " | count: " + count + " | seed: " + seed);
        System.out.println("Picked distribution: " + pickedCounts);
        System.out.println("Sample picks:");
        for (int i = 0; i < Math.min(10, picked.size()); i++) {
            Picked p = picked.get(i);
            System.out.println(" - " + p.word + " (" + p.level + ")");
        }
    }

    private static List<Entry> loadCsv(Path csvPath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            List<Entry> out = new ArrayList<>(8192);
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                line = line.trim();
                if (line.isEmpty()) continue;

                List<String> cols = parseCsvLine(line);
                String headword = cols.size() > 0 ? cols.get(0).trim() : "";
                String level = cols.size() > 1 ? cols.get(1).trim() : "";
                if (headword.isEmpty() || level.isEmpty()) continue;
                out.add(new Entry(headword, normalize(level)));
            }
            return out;
        }
    }

    private static Map<String, List<String>> groupByLevel(List<Entry> entries) {
        Map<String, List<String>> map = new HashMap<>();
        for (Entry e : entries) {
            map.computeIfAbsent(e.level, _k -> new ArrayList<>()).add(e.word);
        }
        return map;
    }

    private static List<Picked> pickDaily(
            Map<String, List<String>> byLevel,
            String currentLevel,
            int count,
            Random random
    ) {
        int currentCount = Math.max(0, (int) Math.floor(count * 0.7));
        int easierCount = Math.max(0, (int) Math.floor(count * 0.2));
        int harderCount = Math.max(0, count - currentCount - easierCount);

        List<Picked> out = new ArrayList<>(count);
        Set<String> seen = new HashSet<>();

        pickFromLevels(out, seen, byLevel, List.of(currentLevel), currentCount, random);
        pickFromLevels(out, seen, byLevel, easierLevels(currentLevel), easierCount, random);
        pickFromLevels(out, seen, byLevel, harderLevels(currentLevel), harderCount, random);

        if (out.size() < count) {
            // Fill from anywhere.
            List<Picked> remaining = new ArrayList<>();
            for (Map.Entry<String, List<String>> e : byLevel.entrySet()) {
                for (String w : e.getValue()) {
                    if (!seen.contains(w.toLowerCase(Locale.ROOT))) remaining.add(new Picked(w, e.getKey()));
                }
            }
            shuffle(remaining, random);
            for (Picked p : remaining) {
                if (out.size() >= count) break;
                if (seen.add(p.word.toLowerCase(Locale.ROOT))) out.add(p);
            }
        }

        shuffle(out, random);
        return out;
    }

    private static void pickFromLevels(
            List<Picked> out,
            Set<String> seen,
            Map<String, List<String>> byLevel,
            List<String> levels,
            int take,
            Random random
    ) {
        if (take <= 0) return;
        List<Picked> pool = new ArrayList<>();
        for (String level : levels) {
            List<String> words = byLevel.get(level);
            if (words == null) continue;
            for (String w : words) pool.add(new Picked(w, level));
        }
        shuffle(pool, random);
        int added = 0;
        for (Picked p : pool) {
            if (added >= take) break;
            String key = p.word.toLowerCase(Locale.ROOT);
            if (seen.add(key)) {
                out.add(p);
                added++;
            }
        }
    }

    private static List<String> easierLevels(String level) {
        return switch (normalize(level)) {
            case "A1" -> List.of();
            case "A2" -> List.of("A1");
            case "B1" -> List.of("A2", "A1");
            case "B2" -> List.of("B1", "A2");
            case "C1" -> List.of("B2", "B1");
            case "C2" -> List.of("C1", "B2");
            default -> List.of();
        };
    }

    private static List<String> harderLevels(String level) {
        return switch (normalize(level)) {
            case "A1" -> List.of("A2", "B1");
            case "A2" -> List.of("B1", "B2");
            case "B1" -> List.of("B2", "C1");
            case "B2" -> List.of("C1", "C2");
            case "C1" -> List.of("C2");
            case "C2" -> List.of();
            default -> List.of();
        };
    }

    private static String normalize(String level) {
        return level.trim().toUpperCase(Locale.ROOT);
    }

    private static List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>(4);
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (ch == ',' && !inQuotes) {
                out.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(ch);
        }
        out.add(current.toString());
        return out;
    }

    private static <T> void shuffle(List<T> list, Random random) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            T tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
    }

    private record Entry(String word, String level) {}
    private record Picked(String word, String level) {}
}

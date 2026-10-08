package com.smartcampus.route;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "${app.frontend-url}")
public class RouteController {

    private final Map<String, List<String>> graph = new LinkedHashMap<>();

    public RouteController() {
        connect("Main Entrance", "A Block");
        connect("A Block", "B Block");
        connect("A Block", "E Block");
        connect("B Block", "C Block");
        connect("C Block", "Football Court");
        connect("C Block", "ABJ Auditorium");
        connect("C Block", "D Block");
        connect("D Block", "H Block");
        connect("H Block", "Botanical Park");
        connect("H Block", "Maker Lab");
        connect("H Block", "Fruit Juice Shop");
        connect("H Block", "Parking");
        connect("Botanical Park", "Sports Block");
        connect("Sports Block", "E Block");
        connect("Sports Block", "F Block");
        connect("Sports Block", "G Block");
        connect("E Block", "F Block");
        connect("E Block", "I Block");
        connect("F Block", "G Block");
        connect("G Block", "I Block");
        connect("ABJ Auditorium", "Parking");
    }

    private void connect(String a, String b) {
        graph.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
        graph.computeIfAbsent(b, k -> new ArrayList<>()).add(a);
    }

    @GetMapping
    public List<String> findRoute(@RequestParam String from, @RequestParam String to) {
        if (from.equals(to)) return List.of(from);

        Queue<List<String>> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(List.of(from));
        visited.add(from);

        while (!queue.isEmpty()) {
            List<String> path = queue.poll();
            String current = path.get(path.size() - 1);

            for (String next : graph.getOrDefault(current, List.of())) {
                if (visited.contains(next)) continue;

                List<String> newPath = new ArrayList<>(path);
                newPath.add(next);

                if (next.equals(to)) return newPath;

                visited.add(next);
                queue.add(newPath);
            }
        }

        return List.of();
    }
}

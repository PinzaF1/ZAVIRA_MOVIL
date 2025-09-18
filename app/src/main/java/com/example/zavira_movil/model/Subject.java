package com.example.zavira_movil.model;

import java.io.Serializable;
import java.util.List;

public class Subject implements Serializable {
    public final String id;
    public final String title;
    public final int done;
    public final int total;
    public final int iconRes;
    public final int headerDrawableRes;
    public final List<Level> levels;

    public Subject(String id, String title, int done, int total, int iconRes,
                   int headerDrawableRes, List<Level> levels) {
        this.id = id;
        this.title = title;
        this.done = done;
        this.total = total;
        this.iconRes = iconRes;
        this.headerDrawableRes = headerDrawableRes;
        this.levels = levels;
    }

    public int percent() {
        return (int) Math.round((done * 100.0) / Math.max(total, 1));
    }

    public static class Level implements Serializable {
        public final String name;                 // Ej: "Nivel 2: Álgebra básica"
        public final String status;               // "Completado | En curso | Bloqueado"
        public final List<Subtopic> subtopics;    // NUEVO

        public Level(String name, String status, List<Subtopic> subtopics) {
            this.name = name;
            this.status = status;
            this.subtopics = subtopics;
        }

        public int subtopicsDone() {
            if (subtopics == null) return 0;
            int c = 0;
            for (Subtopic s : subtopics) if (s.done) c++;
            return c;
        }

        public int subtopicsPercent() {
            if (subtopics == null || subtopics.isEmpty()) return 0;
            return (int) Math.round(subtopicsDone() * 100.0 / subtopics.size());
        }
    }

    public static class Subtopic implements Serializable {
        public final String title;    // Ej: "Ecuaciones lineales"
        public final boolean done;    // datos quemados
        public Subtopic(String title, boolean done) {
            this.title = title;
            this.done = done;
        }
    }
}

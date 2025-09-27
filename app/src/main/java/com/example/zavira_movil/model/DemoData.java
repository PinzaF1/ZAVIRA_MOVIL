package com.example.zavira_movil.model;

import com.example.zavira_movil.R;
import java.util.*;

public class DemoData {

    public static List<Subject> getSubjects() {
        List<Subject> list = new ArrayList<>();

        // ========================= MATEMÁTICAS
        Subject mate = subjectBase("Matemáticas", R.drawable.ic_math_24, R.drawable.bg_header_math);
        mate.levels = Arrays.asList(
                level("Nivel 1", "Aritmética"),
                level("Nivel 2", "Álgebra"),
                level("Nivel 3", "Geometría"),
                level("Nivel 4", "Estadística y Probabilidad"),
                level("Nivel 5", "Funciones y Gráficas"),
                level("Simulacro final", "SIMULACRO FINAL")
        );
        mate.total = mate.levels.size(); // 6
        list.add(mate);

        // ========================= LECTURA CRÍTICA
        Subject lect = subjectBase("Lectura crítica", R.drawable.ic_read_24, R.drawable.bg_header_reading);
        lect.levels = Arrays.asList(
                level("Nivel 1", "Comprensión lectora"),
                level("Nivel 2", "Cohesión textual"),
                level("Nivel 3", "Relaciones semánticas"),
                level("Nivel 4", "Conectores lógicos"),
                level("Nivel 5", "Propósito del autor"),
                level("Simulacro final", "SIMULACRO FINAL")
        );
        lect.total = lect.levels.size(); // 6
        list.add(lect);

        // ========================= SOCIALES
        Subject soc = subjectBase("Sociales y ciudadanas", R.drawable.ic_social_24, R.drawable.bg_header_social);
        soc.levels = Arrays.asList(
                level("Nivel 1", "Geografía"),
                level("Nivel 2", "Historia"),
                level("Nivel 3", "Economía"),
                level("Nivel 4", "Ciudadanía"),
                level("Nivel 5", "Pensamiento social"),
                level("Simulacro final", "SIMULACRO FINAL")
        );
        soc.total = soc.levels.size(); // 6
        list.add(soc);

        // ========================= CIENCIAS NATURALES
        Subject cie = subjectBase("Ciencias naturales", R.drawable.ic_science_24, R.drawable.bg_header_science);
        cie.levels = Arrays.asList(
                level("Nivel 1", "Biología"),
                level("Nivel 2", "Química"),
                level("Nivel 3", "Física"),
                level("Nivel 4", "Ciencias de la Tierra"),
                level("Nivel 5", "Método científico"),
                level("Simulacro final", "SIMULACRO FINAL")
        );
        cie.total = cie.levels.size(); // 6
        list.add(cie);

        // ========================= INGLÉS
        Subject eng = subjectBase("Inglés", R.drawable.ic_english_24, R.drawable.bg_header_english);
        eng.levels = Arrays.asList(
                level("Nivel 1", "Reading básico"),
                level("Nivel 2", "Reading intermedio"),
                level("Nivel 3", "Grammar y uso"),
                level("Nivel 4", "Listening y contexto"),
                level("Nivel 5", "Writing"),
                level("Simulacro final", "SIMULACRO FINAL")
        );
        eng.total = eng.levels.size(); // 6
        list.add(eng);

        return list;
    }

    // helpers
    private static Subject subjectBase(String title, int icon, int header) {
        Subject s = new Subject();
        s.title = title; s.iconRes = icon; s.headerDrawableRes = header;
        s.done = 0; s.total = 6; // por defecto 6 niveles (incluye simulacro)
        return s;
    }
    private static Subject.Level level(String name, String subtopic) {
        Subject.Level l = new Subject.Level(name);
        l.subtopics.add(new Subject.Subtopic(subtopic));
        return l;
    }
}

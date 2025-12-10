package com.example.zavira_movil.util;

import android.content.Context;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.example.zavira_movil.R;

import java.text.Normalizer;

public class AreaColorManager {

    private static String normalize(String area) {
        if (area == null) return "";
        String s = area.trim().toLowerCase();
        // eliminar tildes y caracteres diacríticos
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return s;
    }

    @ColorRes
    public static int getColorResId(String area) {
        String a = normalize(area);

        if (a.contains("conocimiento") || a.contains("isla") || (a.contains("todas") && (a.contains("area") || a.contains("área")))) {
            return R.color.area_conocimiento;
        }

        if (a.contains("matem")) return R.color.area_matematicas;
        // Cambiado: lenguaje/lectura -> AZUL (user expects lectura azul)
        if (a.contains("lengua") || a.contains("lectura") || a.contains("lenguaje") || a.contains("espa") || a.contains("critica"))
            return R.color.azul; // usar recurso azul en vez del morado anterior
        // Cambiado: sociales -> NARANJA (user expects sociales naranja)
        if (a.contains("social") || a.contains("ciudad")) return R.color.naranja;
        if (a.contains("cien") || a.contains("natural") || a.contains("biolo") || a.contains("fis") || a.contains("quim"))
            return R.color.area_ciencias;
        // Cambiado: ingles -> MORADO (user esperaba morado para inglés)
        if (a.contains("ingl")) return R.color.morado;

        return R.color.subject_default;
    }

    @ColorRes
    public static int getSoftColorResId(String area) {
        String a = normalize(area);

        if (a.contains("matem")) return R.color.area_matematicas_pastel;
        // Lenguaje pastel -> usar pastel azul
        if (a.contains("lengua") || a.contains("lectura") || a.contains("lenguaje") || a.contains("espa") || a.contains("critica"))
            return R.color.pastel_azul;
        // Sociales pastel -> usar area_sociales_pastel (o similar)
        if (a.contains("social") || a.contains("ciudad")) return R.color.area_sociales_pastel;
        if (a.contains("cien") || a.contains("natural") || a.contains("biolo") || a.contains("fis") || a.contains("quim"))
            return R.color.area_ciencias_pastel;
        // Ingles pastel -> usar lenguaje pastel morado para contraste
        if (a.contains("ingl")) return R.color.area_lenguaje_pastel;

        return R.color.subject_default;
    }

    @ColorInt
    public static int getColor(Context context, String area) {
        return ContextCompat.getColor(context, getColorResId(area));
    }

    @ColorInt
    public static int getSoftColor(Context context, String area) {
        return ContextCompat.getColor(context, getSoftColorResId(area));
    }

    /**
     * Opcional: retornar resource id de ícono por área si se necesita
     */
    public static int getIconResId(String area) {
        String a = normalize(area);
        if (a.contains("matem")) return R.drawable.calculator;
        if (a.contains("lengua") || a.contains("lectura") || a.contains("lenguaje")) return R.drawable.lectu;
        if (a.contains("social") || a.contains("ciudad")) return R.drawable.sociale;
        if (a.contains("cien") || a.contains("natural")) return R.drawable.naturales;
        if (a.contains("ingl")) return R.drawable.english;
        return R.drawable.lectu; // default
    }
}

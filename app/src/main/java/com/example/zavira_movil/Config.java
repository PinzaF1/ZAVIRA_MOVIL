package com.example.zavira_movil;

/**
 * Archivo de configuración (referencia).
 * NOTA: Actualmente NO se usa como límite global para quizzes.
 * El requisito del cliente indica que SOLO los retos deben tener 5 preguntas.
 * Por compatibilidad se deja este archivo como referencia, pero no se utiliza.
 */
@Deprecated
public final class Config {
    private Config() {}

    /**
     * Valor de ejemplo (NO USADO): número máximo de preguntas por quiz en cliente.
     * No cambies este valor esperando que afecte a los retos — los retos usan literal 5.
     */
    public static final int QUIZ_MAX_QUESTIONS = 5;
}

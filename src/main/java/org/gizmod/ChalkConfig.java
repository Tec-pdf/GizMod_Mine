package org.gizmod;

public final class ChalkConfig {
    private ChalkConfig() {}


    public static final int CHALK_DURABILITY = 256; //Durabilidade de cada giz
    public static final int PIXELS_PER_DAMAGE = 4; //Pixeis desenhados para -1 durabilidade

    public static final long TEMPORARY_LIFETIME_TICKS = 3 * 60 * 20L; // Timing desenho temporário

    public static final int WATER_STEPS = 3; //Em quantas etapas a água apaga o desenho
    public static final int WATER_STEP_INTERVAL_TICKS = 1; //Ticks

    //Segurança pra amarrar player loco

    public static final double MAX_DRAW_DISTANCE = 6.0; //Alcance max para desenhar
    public static final int DRAW_SESSION_TICKS = 6000; //Timing servidor->desenho, 5min = 6000 ticks
    public static final int MIN_TICKS_BETWEEN_DRAWINGS = 4; //Intervalo em ticks entre um desenho e outro
    public static final int MAX_FACES_PER_CHUNK = 256; //Máximo de faces desenhadas por chunk [Memory Management]
    public static final int MAX_FACES_PER_LEVEL = 10_000;
    public static final double RENDER_DISTANCE = 64.0; //Distãncia max para renderizar desenhos

    //Estojos
    public static final int CASE_SLOTS_COPPER = 8;
    public static final int CASE_SLOTS_IRON = 10;
    public static final int CASE_SLOTS_GOLD = 12;

    public static final boolean SHOW_VARIANT_GLINT = true;
}

package edu.co.icesi.imus.model

enum class TestType(val displayName: String) {
    GAIT("Análisis de Marcha"),
    TOPOLOGICAL_GAIT_ANALYSIS("Análisis Topológico de Marcha"),
    DYNAMIC_GAIT_INDEX("Índice Dinámico de Marcha"),
    TIMED_UP_AND_GO("Prueba Timed Up and Go"),
    ONLY_RIGHT_HAND("Solo Mano Derecha")
}
package com.lendora.users.enums;

/**
 * Tipo de dependencia entre dos permisos.
 *
 * REQUIRES: para que `permission` funcione en el sistema, el rol también
 *           debe traer `dependsOn` (regla dura, aplicada al guardar).
 * IMPLIES:  si el rol trae `permission`, conviene también traer `dependsOn`
 *           (sugerencia, también aplicada al guardar para coherencia).
 *
 * En la práctica el front consume ambos mapas y los expande sobre la
 * selección del admin, filtrando contra el catálogo de permisos existente.
 */
public enum DependencyKind {
    REQUIRES,
    IMPLIES
}

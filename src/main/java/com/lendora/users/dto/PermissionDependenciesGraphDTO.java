package com.lendora.users.dto;

import java.util.List;
import java.util.Map;

/**
 * Vista pre-agregada del grafo de dependencias, lista para que el front
 * la consuma sin transformar.
 *
 *   requires["customers.create"] = ["parties.read","parties.create"]
 *   implies["customers.read"]    = ["parties.read"]
 */
public record PermissionDependenciesGraphDTO(
    Map<String, List<String>> requires,
    Map<String, List<String>> implies
) {}

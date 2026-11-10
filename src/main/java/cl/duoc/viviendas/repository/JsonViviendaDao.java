package cl.duoc.viviendas.repository;

import cl.duoc.viviendas.model.Casa;
import cl.duoc.viviendas.model.Departamento;
import cl.duoc.viviendas.model.Vivienda;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class JsonViviendaDao implements ViviendaDao {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path departamentos = Path.of("departamentos.json");
    private final Path casas = Path.of("casas.json");

    @Override
    public List<Vivienda> cargar() throws IOException {
        List<Vivienda> resultado = new ArrayList<>();
        resultado.addAll(leer(departamentos, new TypeToken<List<Departamento>>() {}.getType()));
        resultado.addAll(leer(casas, new TypeToken<List<Casa>>() {}.getType()));
        return resultado;
    }

    private <T> List<T> leer(Path archivo, Type tipo) throws IOException {
        if (!Files.exists(archivo)) {
            return new ArrayList<>();
        }
        try (Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            List<T> datos = gson.fromJson(lector, tipo);
            if (datos == null) {
                throw new IOException("El archivo " + archivo + " está vacío o no contiene una lista.");
            }
            return datos;
        } catch (com.google.gson.JsonParseException e) {
            throw new IOException("No se pudo leer " + archivo + ": JSON no válido.", e);
        }
    }

    @Override
    public void guardar(List<Vivienda> viviendas, Vivienda modificada) throws IOException {
        List<Departamento> listaDepartamentos = new ArrayList<>();
        List<Casa> listaCasas = new ArrayList<>();
        for (Vivienda vivienda : viviendas) {
            if (vivienda instanceof Departamento d) listaDepartamentos.add(d);
            if (vivienda instanceof Casa c) listaCasas.add(c);
        }
        if (modificada instanceof Departamento) escribir(departamentos, listaDepartamentos);
        else escribir(casas, listaCasas);
    }

    private void escribir(Path archivo, Object datos) throws IOException {
        Path temporal = Path.of(archivo.toString() + ".tmp");
        try {
            try (Writer escritor = Files.newBufferedWriter(temporal, StandardCharsets.UTF_8)) {
                gson.toJson(datos, escritor);
            }
            Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporal);
        }
    }
}

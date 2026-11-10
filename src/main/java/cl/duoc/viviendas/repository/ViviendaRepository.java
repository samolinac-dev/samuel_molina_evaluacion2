package cl.duoc.viviendas.repository;

import cl.duoc.viviendas.model.Vivienda;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ViviendaRepository {
    private final ViviendaDao dao = new JsonViviendaDao();
    private final List<Vivienda> viviendas = new ArrayList<>();
    private boolean cargaCorrecta;

    public List<Vivienda> cargar() throws IOException {
        List<Vivienda> leidas = dao.cargar();
        viviendas.clear();
        viviendas.addAll(leidas);
        cargaCorrecta = true;
        return new ArrayList<>(viviendas);
    }

    public void registrar(Vivienda vivienda) throws IOException {
        verificarCarga();
        List<Vivienda> nuevaLista = new ArrayList<>(viviendas);
        nuevaLista.add(vivienda);
        dao.guardar(nuevaLista, vivienda);
        viviendas.add(vivienda);
    }

    public void eliminar(Vivienda vivienda) throws IOException {
        verificarCarga();
        List<Vivienda> nuevaLista = new ArrayList<>(viviendas);
        nuevaLista.remove(vivienda);
        dao.guardar(nuevaLista, vivienda);
        viviendas.remove(vivienda);
    }

    private void verificarCarga() throws IOException {
        if (!cargaCorrecta) {
            throw new IOException("No se pueden modificar datos porque falló la lectura inicial.");
        }
    }
}

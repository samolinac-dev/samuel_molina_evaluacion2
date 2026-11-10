package cl.duoc.viviendas.repository;

import cl.duoc.viviendas.model.Vivienda;
import java.io.IOException;
import java.util.List;

public interface ViviendaDao {
    List<Vivienda> cargar() throws IOException;
    void guardar(List<Vivienda> viviendas, Vivienda modificada) throws IOException;
}

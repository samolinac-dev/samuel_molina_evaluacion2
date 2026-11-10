package cl.duoc.viviendas.controller;

import cl.duoc.viviendas.model.Casa;
import cl.duoc.viviendas.model.Departamento;
import cl.duoc.viviendas.model.Vivienda;
import cl.duoc.viviendas.repository.ViviendaRepository;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

public class MainController {
    @FXML private ComboBox<String> tipo;
    @FXML private TextField codigo;
    @FXML private TextField superficie;
    @FXML private TextField habitaciones;
    @FXML private TextField piso;
    @FXML private TextField buscar;
    @FXML private ComboBox<String> gastoComun;
    @FXML private ComboBox<String> estacionamiento;
    @FXML private ComboBox<String> patio;
    @FXML private TableView<Vivienda> tabla;
    @FXML private TableColumn<Vivienda, String> colTipo;
    @FXML private TableColumn<Vivienda, String> colCodigo;
    @FXML private TableColumn<Vivienda, String> colSuperficie;
    @FXML private TableColumn<Vivienda, String> colHabitaciones;
    @FXML private TableColumn<Vivienda, String> colCosto;
    @FXML private Label mensaje;

    private final ViviendaRepository repository = new ViviendaRepository();
    private final ObservableList<Vivienda> datos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        tipo.getItems().addAll("Departamento", "Casa");
        gastoComun.getItems().addAll("Sí", "No");
        estacionamiento.getItems().addAll("Sí", "No");
        patio.getItems().addAll("Sí", "No");
        tipo.setOnAction(evento -> actualizarCampos());
        actualizarCampos();

        colTipo.setCellValueFactory(fila -> new ReadOnlyStringWrapper(fila.getValue().getTipo()));
        colCodigo.setCellValueFactory(fila -> new ReadOnlyStringWrapper(fila.getValue().getCodigo()));
        colSuperficie.setCellValueFactory(fila -> new ReadOnlyStringWrapper(String.valueOf(fila.getValue().getSuperficie())));
        colHabitaciones.setCellValueFactory(fila -> new ReadOnlyStringWrapper(String.valueOf(fila.getValue().getHabitaciones())));
        NumberFormat formato = NumberFormat.getIntegerInstance(new Locale("es", "CL"));
        colCosto.setCellValueFactory(fila -> new ReadOnlyStringWrapper("$" + formato.format(fila.getValue().calcularCostoArriendo())));
        FilteredList<Vivienda> filtrados = new FilteredList<>(datos, vivienda -> true);
        buscar.textProperty().addListener((obs, anterior, nuevo) -> {
            String texto = nuevo.trim().toLowerCase();
            filtrados.setPredicate(vivienda -> texto.isEmpty()
                    || vivienda.getCodigo().toLowerCase().contains(texto)
                    || vivienda.getTipo().toLowerCase().contains(texto));
        });
        tabla.setItems(filtrados);

        try {
            datos.setAll(repository.cargar());
        } catch (IOException e) {
            mensaje.setText("Error al leer los archivos: " + e.getMessage());
        }
    }

    private void actualizarCampos() {
        boolean departamento = "Departamento".equals(tipo.getValue());
        boolean casa = "Casa".equals(tipo.getValue());
        piso.setDisable(!departamento);
        gastoComun.setDisable(!departamento);
        estacionamiento.setDisable(!departamento);
        patio.setDisable(!casa);
    }

    @FXML
    private void registrar() {
        try {
            String elegido = tipo.getValue();
            if (elegido == null) throw new IllegalArgumentException("Selecciona el tipo de vivienda.");
            String clave = codigo.getText().trim();
            if (clave.isEmpty()) throw new IllegalArgumentException("El código de propiedad no puede estar vacío.");
            double metros = leerSuperficie();
            int piezas = leerEntero(habitaciones, "habitaciones");
            Vivienda nueva;

            if (elegido.equals("Departamento")) {
                int numeroPiso = leerEntero(piso, "piso");
                if (gastoComun.getValue() == null) throw new IllegalArgumentException("Indica si el gasto común está al día.");
                if (estacionamiento.getValue() == null) throw new IllegalArgumentException("Indica si tiene estacionamiento.");
                Departamento d = new Departamento(clave, metros, piezas, numeroPiso, "Sí".equals(gastoComun.getValue()));
                if ("Sí".equals(estacionamiento.getValue())) d.asignarEstacionamiento();
                nueva = d;
            } else {
                if (patio.getValue() == null) throw new IllegalArgumentException("Indica si la casa tiene patio.");
                nueva = new Casa(clave, metros, piezas, "Sí".equals(patio.getValue()));
            }

            repository.registrar(nueva);
            datos.add(nueva);
            mensaje.setText(nueva.getCodigo() + " (" + nueva.getTipo() + ") registrada correctamente.");
            limpiarFormulario();
        } catch (IllegalArgumentException e) {
            mensaje.setText(e.getMessage());
        } catch (IOException e) {
            mensaje.setText("Error al guardar los archivos: " + e.getMessage());
        }
    }

    private double leerSuperficie() {
        try {
            double valor = Double.parseDouble(superficie.getText().trim().replace(',', '.'));
            if (!Double.isFinite(valor) || valor < 20 || valor > 500) throw new NumberFormatException();
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La superficie debe ser un número entre 20 y 500 m².");
        }
    }

    private int leerEntero(TextField campo, String nombre) {
        try {
            int valor = Integer.parseInt(campo.getText().trim());
            if (valor <= 0) throw new NumberFormatException();
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + nombre + " debe ser un número entero mayor que cero.");
        }
    }

    @FXML
    private void eliminar() {
        Vivienda seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mensaje.setText("Selecciona una vivienda en la tabla para eliminarla.");
            return;
        }
        try {
            repository.eliminar(seleccionada);
            datos.remove(seleccionada);
            mensaje.setText(seleccionada.getCodigo() + " eliminada correctamente.");
        } catch (IOException e) {
            mensaje.setText("Error al guardar los archivos: " + e.getMessage());
        }
    }

    private void limpiarFormulario() {
        tipo.setValue(null);
        codigo.clear();
        superficie.clear();
        habitaciones.clear();
        piso.clear();
        gastoComun.setValue(null);
        estacionamiento.setValue(null);
        patio.setValue(null);
        actualizarCampos();
    }
}

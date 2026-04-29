// InstanciaService.java
package com.workflow.modules.workflowinstance.service;

import com.workflow.common.Exceptions;
import com.workflow.model.enums.InstanceStatus;
import com.workflow.modules.engine.service.WorkflowEngine;
import com.workflow.modules.forms.model.FieldDefinition;
import com.workflow.modules.tasks.model.ActiveTask;
import com.workflow.modules.tasks.repository.ActiveTaskRepository;
import com.workflow.modules.departments.model.Department;
import com.workflow.modules.departments.repository.DepartmentRepository;
import com.workflow.modules.users.dto.UserRequest;
import com.workflow.modules.users.dto.UserResponse;
import com.workflow.modules.users.model.User;
import com.workflow.modules.users.service.UserService;
import com.workflow.modules.users.repository.UserRepository;
import com.workflow.model.enums.UserRole;
import com.workflow.modules.workflowdefinition.model.Node;
import com.workflow.modules.workflowdefinition.model.WorkflowDefinition;
import com.workflow.modules.workflowdefinition.repository.WorkflowDefinitionRepository;
import com.workflow.modules.workflowinstance.model.ProcessInstance;
import com.workflow.modules.workflowinstance.repository.ProcessInstanceRepository;
import com.workflow.modules.workflowinstance.dto.CrearClienteRequest;
import com.workflow.modules.workflowinstance.dto.CrearClienteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.Instant;

@Service
public class InstanciaService {

    private final ProcessInstanceRepository repository;
    private final ActiveTaskRepository activeTaskRepository;
    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final WorkflowEngine workflowEngine;
    private final DepartmentRepository departmentRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public InstanciaService(ProcessInstanceRepository repository,
                            ActiveTaskRepository activeTaskRepository,
                            WorkflowDefinitionRepository workflowDefinitionRepository,
                            @Lazy WorkflowEngine workflowEngine,
                            DepartmentRepository departmentRepository,
                            UserService userService,
                            UserRepository userRepository) {
        this.repository = repository;
        this.activeTaskRepository = activeTaskRepository;
        this.workflowDefinitionRepository = workflowDefinitionRepository;
        this.workflowEngine = workflowEngine;
        this.departmentRepository = departmentRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public Page<ProcessInstance> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public ProcessInstance findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Instancia no encontrada: " + id));
    }

    public ProcessInstance findByRut(String rut) {
        return repository.findByIndiceBusquedaRut(rut)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Instancia no encontrada con RUT: " + rut));
    }

    public ProcessInstance findByCodigoCaso(String codigoCaso) {
        return repository.findByIndiceBusquedaCodigoCaso(codigoCaso)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Instancia no encontrada con código: " + codigoCaso));
    }

    public List<ProcessInstance> findByEstado(InstanceStatus estado) {
        return repository.findByEstado(estado);
    }

    public List<ProcessInstance> findByPolitica(String idPolitica) {
        return repository.findByIdPolitica(idPolitica);
    }

    public List<ProcessInstance> findByCliente(String idCliente) {
        return repository.findByIdCliente(idCliente);
    }

    public List<ProcessInstance> findMisInstanciasCliente(String correo) {
        String correoNorm = correo == null ? "" : correo.trim().toLowerCase();
        String idCliente = userRepository.findByCorreoIgnoreCase(correoNorm)
                .map(u -> u.getId())
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Usuario no encontrado: " + correoNorm));
        return repository.findByIdCliente(idCliente);
    }

    public ProcessInstance create(ProcessInstance instancia) {
        return repository.save(instancia);
    }

    public ProcessInstance update(String id, ProcessInstance updated) {
        ProcessInstance existing = findById(id);
        existing.setIdPolitica(updated.getIdPolitica());
        existing.setIdCliente(updated.getIdCliente());
        existing.setIdDepartamentoActual(updated.getIdDepartamentoActual());
        existing.setNodoActualId(updated.getNodoActualId());
        existing.setEstado(updated.getEstado());
        existing.setIndiceBusqueda(updated.getIndiceBusqueda());
        return repository.save(existing);
    }

    public ProcessInstance patch(String id, ProcessInstance partial) {
        ProcessInstance existing = findById(id);
        if (partial.getEstado() != null) existing.setEstado(partial.getEstado());
        if (partial.getNodoActualId() != null) existing.setNodoActualId(partial.getNodoActualId());
        if (partial.getIdDepartamentoActual() != null) existing.setIdDepartamentoActual(partial.getIdDepartamentoActual());
        if (partial.getIndiceBusqueda() != null) existing.setIndiceBusqueda(partial.getIndiceBusqueda());
        return repository.save(existing);
    }

    public void delete(String id) {
        if (!repository.existsById(id))
            throw new Exceptions.ResourceNotFoundException("Instancia no encontrada: " + id);
        repository.deleteById(id);
    }

    public ProcessInstance tomarTarea(String tareaId, String idUsuario, String nombreUsuario) {
        workflowEngine.claimarTarea(tareaId, idUsuario, nombreUsuario);
        ActiveTask tarea = activeTaskRepository.findById(tareaId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Tarea no encontrada: " + tareaId));
        return findById(tarea.getIdInstancia());
    }

    public Map<String, Object> obtenerFormulario(String tareaId) {
        ActiveTask tarea = activeTaskRepository.findById(tareaId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Tarea no encontrada: " + tareaId));

        WorkflowDefinition politica = workflowDefinitionRepository.findById(tarea.getIdPolitica())
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Política no encontrada: " + tarea.getIdPolitica()));

        Node nodo = politica.getNodos().stream()
                .filter(n -> tarea.getIdNodo().equals(n.getId()))
                .findFirst()
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Nodo no encontrado: " + tarea.getIdNodo()));

        List<Map<String, Object>> campos = new ArrayList<>();
        if (nodo.getFormSchema() != null && nodo.getFormSchema().getFields() != null) {
            for (FieldDefinition field : nodo.getFormSchema().getFields()) {
                Map<String, Object> campo = new HashMap<>();
                String nombre = (field.getLabel() != null && !field.getLabel().isBlank())
                        ? field.getLabel()
                        : labelDesdeTipo(field.getType());
                campo.put("nombre", nombre);
                campo.put("tipo", mapFieldType(field.getType()));
                campo.put("requerido", field.isRequired());
                if (field.getAyuda() != null && !field.getAyuda().isBlank()) {
                    campo.put("ayuda", field.getAyuda());
                }
                if (field.getDefaultValue() != null) campo.put("valor", field.getDefaultValue());
                campos.add(campo);
            }
        }
        if (campos.isEmpty()) {
            campos.add(Map.of("nombre", "observaciones", "tipo", "texto", "requerido", false));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("campos", campos);
        result.put("tareaId", tarea.getId());
        result.put("nodoLabel", nodo.getLabel());
        result.put("datosEntrada", tarea.getDatos() == null ? Map.of() : tarea.getDatos());
        List<String> destCodigos = obtenerDestinos(nodo);
        if (!destCodigos.isEmpty()) {
            List<String> destNombres = destCodigos.stream()
                    .map(this::resolverNombreDepartamento)
                    .toList();
            result.put("departamentosDestino", destNombres);
            result.put("departamentosDestinoCodigo", destCodigos);
            if (destCodigos.size() == 1) {
                result.put("departamentoDestino", destNombres.get(0));
                result.put("departamentoDestinoCodigo", destCodigos.get(0));
            }
        }
        result.put("crearUsuarioCliente", nodo.isCrearUsuarioCliente());
        ProcessInstance instancia = findById(tarea.getIdInstancia());
        result.put("clienteCreado", instancia.getIdCliente() != null);
        if (instancia.getIdCliente() != null) {
            userRepository.findById(instancia.getIdCliente()).ifPresent(u -> {
                result.put("clienteId", u.getId());
                result.put("clienteNombre", u.getNombreCompleto());
                result.put("clienteCorreo", u.getCorreo());
                result.put("clienteUsuario", u.getNombreUsuario());
            });
        }
        String sugerido = sugerirCorreoCliente(obtenerValorCampo(tarea.getDatos(), "nombre completo").orElse(null));
        if (sugerido != null) result.put("correoSugerido", sugerido);
        return result;
    }

    private String mapFieldType(String type) {
        if (type == null) return "texto";
        return switch (type.toLowerCase()) {
            case "image", "imagen" -> "imagen";
            case "file", "archivo" -> "pdf";
            case "boolean", "sino", "checkbox", "check" -> "sino";
            case "date", "fecha" -> "fecha";
            default -> "texto";
        };
    }

    private String labelDesdeTipo(String type) {
        if (type == null) return "Campo";
        return switch (type.toLowerCase()) {
            case "image", "imagen" -> "Foto / Imagen";
            case "file", "archivo" -> "Documento PDF";
            case "boolean", "sino", "checkbox", "check" -> "Sí / No";
            case "date", "fecha" -> "Fecha";
            case "number", "numero" -> "Número";
            default -> "Dato de texto";
        };
    }

    private List<String> obtenerDestinos(Node nodo) {
        List<String> destinos = nodo.getIdDepartamentosDestino() == null
                ? new ArrayList<>()
                : new ArrayList<>(nodo.getIdDepartamentosDestino());
        if (destinos.isEmpty() && nodo.getIdDepartamentoDestino() != null && !nodo.getIdDepartamentoDestino().isBlank()) {
            destinos.add(nodo.getIdDepartamentoDestino());
        }
        return destinos;
    }

    private String resolverNombreDepartamento(String codigoOrId) {
        if (codigoOrId == null || codigoOrId.isBlank()) return null;
        return departmentRepository.findByCodigoIgnoreCase(codigoOrId)
                .or(() -> departmentRepository.findById(codigoOrId))
                .map(Department::getNombre)
                .orElse(codigoOrId);
    }

    public ProcessInstance avanzarTarea(String tareaId, Map<String, Object> datos) {
        // Save instanciaId before task gets deleted by completarTarea
        ActiveTask tarea = activeTaskRepository.findById(tareaId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Tarea no encontrada: " + tareaId));
        String instanciaId = tarea.getIdInstancia();
        workflowEngine.completarTarea(tareaId, datos);
        return repository.findById(instanciaId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Instancia no encontrada: " + instanciaId));
    }

    public CrearClienteResponse crearClienteDesdeTarea(String tareaId, CrearClienteRequest request) {
        ActiveTask tarea = activeTaskRepository.findById(tareaId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Tarea no encontrada: " + tareaId));

        WorkflowDefinition politica = workflowDefinitionRepository.findById(tarea.getIdPolitica())
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Política no encontrada: " + tarea.getIdPolitica()));

        Node nodo = politica.getNodos().stream()
                .filter(n -> tarea.getIdNodo().equals(n.getId()))
                .findFirst()
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Nodo no encontrado: " + tarea.getIdNodo()));

        if (!nodo.isCrearUsuarioCliente()) {
            throw new Exceptions.BadRequestException("Esta tarea no tiene habilitada la creación de usuario cliente.");
        }

        validarDepartamentoAdm(tarea.getIdDepartamentoAsignado());

        ProcessInstance instancia = findById(tarea.getIdInstancia());
        if (instancia.getIdCliente() != null) {
            return CrearClienteResponse.builder()
                    .idCliente(instancia.getIdCliente())
                    .build();
        }

        String nombreCompleto = obtenerValorCampo(tarea.getDatos(), "nombre completo")
                .orElse(request.getNombreCompleto());
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new Exceptions.BadRequestException("No se encontró 'nombre completo' en los datos del flujo.");
        }

        String correo = sugerirCorreoCliente(nombreCompleto);
        if (correo == null) {
            throw new Exceptions.BadRequestException("No se pudo generar el correo del cliente.");
        }

        Department deptCliente = resolverDepartamentoCliente();
        UserRequest userRequest = new UserRequest();
        userRequest.setNombreCompleto(nombreCompleto.trim());
        userRequest.setCorreo(correo);
        userRequest.setNombreUsuario(correo.split("@")[0]);
        userRequest.setContrasena(request.getContrasena());
        userRequest.setIdDepartamento(deptCliente.getId());

        UserResponse creado = userService.createByAdmin(userRequest);

        instancia.setIdCliente(creado.getId());
        repository.save(instancia);

        return CrearClienteResponse.builder()
                .idCliente(creado.getId())
                .nombreCompleto(creado.getNombreCompleto())
                .correo(creado.getCorreo())
                .nombreUsuario(creado.getNombreUsuario())
                .build();
    }

    public CrearClienteResponse asignarClienteDesdeTarea(String tareaId, String idCliente) {
        ActiveTask tarea = activeTaskRepository.findById(tareaId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Tarea no encontrada: " + tareaId));

        ProcessInstance instancia = findById(tarea.getIdInstancia());
        if (instancia.getIdCliente() != null && !instancia.getIdCliente().isBlank()) {
            if (!instancia.getIdCliente().equals(idCliente)) {
                throw new Exceptions.BadRequestException("La instancia ya tiene un cliente asignado.");
            }
            User existente = userRepository.findById(instancia.getIdCliente())
                    .orElseThrow(() -> new Exceptions.ResourceNotFoundException(
                            "Usuario no encontrado: " + instancia.getIdCliente()));
            return CrearClienteResponse.builder()
                    .idCliente(existente.getId())
                    .nombreCompleto(existente.getNombreCompleto())
                    .correo(existente.getCorreo())
                    .nombreUsuario(existente.getNombreUsuario())
                    .build();
        }

        User cliente = userRepository.findById(idCliente)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Usuario no encontrado: " + idCliente));
        if (cliente.getRol() != UserRole.CLIENT) {
            throw new Exceptions.BadRequestException("El usuario seleccionado no es un cliente.");
        }

        instancia.setIdCliente(cliente.getId());
        repository.save(instancia);

        return CrearClienteResponse.builder()
                .idCliente(cliente.getId())
                .nombreCompleto(cliente.getNombreCompleto())
                .correo(cliente.getCorreo())
                .nombreUsuario(cliente.getNombreUsuario())
                .build();
    }

    private Department resolverDepartamentoCliente() {
        return departmentRepository.findByCodigoIgnoreCase("CLIENTES")
                .or(() -> departmentRepository.findByCodigoIgnoreCase("CLIENTE"))
                .orElseGet(() -> departmentRepository.findAll().stream()
                        .filter(d -> d.getRolAsignado() == UserRole.CLIENT)
                        .findFirst()
                        .orElseThrow(() -> new Exceptions.ResourceNotFoundException(
                                "No existe un departamento con rol CLIENT. Crea uno y asigna rol CLIENT.")));
    }

    private void validarDepartamentoAdm(String idDepartamento) {
        if (idDepartamento == null || idDepartamento.isBlank()) return;
        Department dep = departmentRepository.findById(idDepartamento)
                .or(() -> departmentRepository.findByCodigoIgnoreCase(idDepartamento))
                .orElse(null);
        if (dep != null && dep.getRolAsignado() != null && dep.getRolAsignado() != UserRole.ADM_DISENADOR) {
            throw new Exceptions.BadRequestException("Solo el departamento ADM_DISENADOR puede crear usuarios cliente.");
        }
    }

    private Optional<String> obtenerValorCampo(Map<String, Object> datos, String esperado) {
        if (datos == null || datos.isEmpty()) return Optional.empty();
        String esperadoNorm = normalizarTexto(esperado);
        for (Map.Entry<String, Object> entry : datos.entrySet()) {
            String key = normalizarTexto(entry.getKey());
            if (key.equals(esperadoNorm) || key.contains(esperadoNorm)) {
                Object val = entry.getValue();
                if (val != null && !val.toString().isBlank()) {
                    return Optional.of(val.toString().trim());
                }
            }
        }
        return Optional.empty();
    }

    private String sugerirCorreoCliente(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) return null;
        String limpio = normalizarTexto(nombreCompleto).replaceAll("[^a-z\\s]", "");
        String[] partes = limpio.trim().split("\\s+");
        if (partes.length == 0) return null;
        String nombre = partes[0];
        String inicialPaterno = partes.length >= 2 ? partes[partes.length - 2].substring(0, 1) : "";
        String inicialMaterno = partes.length >= 3 ? partes[partes.length - 1].substring(0, 1) : "";
        return (nombre + inicialPaterno + inicialMaterno + "@cliente.cre").toLowerCase();
    }

    private String normalizarTexto(String input) {
        if (input == null) return "";
        String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        return normalized.toLowerCase().trim();
    }

    /**
     * Inicia un nuevo flujo de trabajo: crea la instancia y genera la primera tarea
     * para el departamento correspondiente al primer nodo de usuario.
     */
    public ProcessInstance iniciarFlujo(
            String idPolitica,
            String idCliente,
            String nodoInicioId,
            String nombreFlujo,
            String idDepartamentoUsuario) {
        WorkflowDefinition politica = workflowDefinitionRepository.findById(idPolitica)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Política no encontrada: " + idPolitica));

        validarDepartamentoInicio(politica, idDepartamentoUsuario);

        if (politica.getNodos() == null || politica.getNodos().isEmpty()) {
            throw new Exceptions.ResourceNotFoundException(
                "La política '" + politica.getTitulo() + "' no tiene nodos definidos. " +
                "Abre el editor de pizarra, guarda el diagrama y vuelve a intentarlo.");
        }

        if (politica.getConexiones() == null || politica.getConexiones().isEmpty()) {
            throw new Exceptions.ResourceNotFoundException(
                "La política '" + politica.getTitulo() + "' no tiene conexiones definidas. " +
                "Verifica que el diagrama tenga flechas entre los nodos y guárdalo.");
        }

        // Find the START node if nodoInicioId is not provided
        String startNodeId = nodoInicioId;
        if (startNodeId == null || startNodeId.isBlank()) {
            startNodeId = politica.getNodos().stream()
                    .filter(n -> "START".equalsIgnoreCase(n.getType())
                            || "startEvent".equalsIgnoreCase(n.getType())
                            || (n.getType() != null && n.getType().toLowerCase().contains("start")))
                    .findFirst()
                    .map(Node::getId)
                    .orElseThrow(() -> new Exceptions.ResourceNotFoundException(
                            "No se encontró nodo START en la política: " + idPolitica));
        }

        long secuencia = obtenerSecuenciaDiaria();
        String nombreFlujoFinal = (nombreFlujo != null && !nombreFlujo.isBlank())
            ? nombreFlujo.trim()
            : "Flujo " + secuencia;
        String codigoCaso = generarCodigoCaso(secuencia);
        Map<String, Object> datosProceso = new HashMap<>();
        datosProceso.put("nombreFlujo", nombreFlujoFinal);
        datosProceso.put("codigoCaso", codigoCaso);

        ProcessInstance instancia = ProcessInstance.builder()
                .idPolitica(idPolitica)
                .idCliente(idCliente)
                .nodoActualId(startNodeId)
                .estado(InstanceStatus.EN_PROCESO)
            .datosProceso(datosProceso)
            .indiceBusqueda(com.workflow.model.embedded.SearchIndex.builder()
                .codigoCaso(codigoCaso)
                .build())
                .build();
        instancia = repository.save(instancia);

        // Advance from START to first user task (creates ActiveTask for first dept)
        workflowEngine.derivarTramite(instancia.getId(), Collections.emptyMap());

        return instancia;
    }

    private long obtenerSecuenciaDiaria() {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate hoy = LocalDate.now(zone);
        Instant inicio = hoy.atStartOfDay(zone).toInstant();
        Instant fin = hoy.plusDays(1).atStartOfDay(zone).toInstant();
        long count = repository.countByFechaCreacionBetween(inicio, fin);
        return count + 1;
    }

    private String generarCodigoCaso(long secuencia) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate hoy = LocalDate.now(zone);
        String fecha = hoy.format(DateTimeFormatter.BASIC_ISO_DATE);
        return "FL-" + fecha + "-" + String.format("%03d", secuencia);
    }

    private void validarDepartamentoInicio(WorkflowDefinition politica, String idDepartamentoUsuario) {
        List<Node> nodos = politica.getNodos() == null ? List.of() : politica.getNodos();
        List<Node> starters = nodos.stream().filter(Node::isInicioFlujo).toList();
        if (starters.isEmpty()) {
            throw new Exceptions.BadRequestException(
                    "El flujo no tiene un departamento habilitado para iniciar.");
        }

        if (idDepartamentoUsuario == null || idDepartamentoUsuario.isBlank()) {
            throw new Exceptions.ForbiddenException("No se pudo validar tu departamento para iniciar el flujo.");
        }

        String deptCodigo = departmentRepository.findById(idDepartamentoUsuario)
                .map(Department::getCodigo)
                .orElse(idDepartamentoUsuario);

        boolean permitido = starters.stream().anyMatch(n -> {
            String dep = n.getIdDepartamento();
            return dep != null && (dep.equalsIgnoreCase(deptCodigo) || dep.equalsIgnoreCase(idDepartamentoUsuario));
        });

        if (!permitido) {
            throw new Exceptions.ForbiddenException("Tu departamento no esta autorizado para iniciar este flujo.");
        }
    }
}

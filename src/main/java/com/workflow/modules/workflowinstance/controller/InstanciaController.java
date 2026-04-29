// InstanciaController.java
package com.workflow.modules.workflowinstance.controller;

import com.workflow.common.ApiResponse;
import com.workflow.model.enums.InstanceStatus;
import com.workflow.modules.workflowinstance.model.ProcessInstance;
import com.workflow.modules.workflowinstance.service.InstanciaService;
import com.workflow.modules.workflowinstance.dto.CrearClienteRequest;
import com.workflow.modules.workflowinstance.dto.CrearClienteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/v1/instancias")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Instancias de Proceso", description = "Gestión de instancias activas de procesos (trámites)")
@RequiredArgsConstructor
public class InstanciaController {

    private final InstanciaService instanciaService;

    @Operation(summary = "Listar instancias", description = "Retorna lista paginada de todas las instancias de proceso")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProcessInstance>>> findAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(instanciaService.findAll(pageable)));
    }

    @Operation(summary = "Obtener instancia por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcessInstance>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(instanciaService.findById(id)));
    }

    @Operation(summary = "Buscar instancia por RUT o código de caso",
               description = "Búsqueda por índice de búsqueda. Use el parámetro 'rut' para búsqueda por RUT.")
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<ProcessInstance>> buscar(
            @RequestParam(required = false) String rut,
            @RequestParam(required = false) String codigoCaso) {
        if (rut != null) {
            return ResponseEntity.ok(ApiResponse.ok(instanciaService.findByRut(rut)));
        }
        if (codigoCaso != null) {
            return ResponseEntity.ok(ApiResponse.ok(instanciaService.findByCodigoCaso(codigoCaso)));
        }
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "Debe especificar 'rut' o 'codigoCaso' como parámetro de búsqueda"));
    }

    @Operation(summary = "Listar instancias por estado")
    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<ProcessInstance>>> findByEstado(
            @PathVariable InstanceStatus estado) {
        return ResponseEntity.ok(ApiResponse.ok(instanciaService.findByEstado(estado)));
    }

    @Operation(summary = "Listar instancias por política")
    @GetMapping("/politica/{idPolitica}")
    public ResponseEntity<ApiResponse<List<ProcessInstance>>> findByPolitica(@PathVariable String idPolitica) {
        return ResponseEntity.ok(ApiResponse.ok(instanciaService.findByPolitica(idPolitica)));
    }

    @Operation(summary = "Mis instancias (cliente)")
    @GetMapping("/mis")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<List<ProcessInstance>>> misInstancias() {
        String correo = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.ok(instanciaService.findMisInstanciasCliente(correo)));
    }

    @Operation(summary = "Crear nueva instancia de proceso")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'JEFE_DEPARTAMENTO', 'ANALISTA', 'EJECUTIVO')")
    public ResponseEntity<ApiResponse<ProcessInstance>> create(@Valid @RequestBody ProcessInstance instancia) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Instancia creada exitosamente", instanciaService.create(instancia)));
    }

    @Operation(summary = "Actualizar instancia completa")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'JEFE_DEPARTAMENTO')")
    public ResponseEntity<ApiResponse<ProcessInstance>> update(
            @PathVariable String id, @Valid @RequestBody ProcessInstance instancia) {
        return ResponseEntity.ok(ApiResponse.ok("Instancia actualizada", instanciaService.update(id, instancia)));
    }

    @Operation(summary = "Actualizar instancia parcialmente",
               description = "Permite actualizar campos específicos como estado, nodo actual o departamento")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'JEFE_DEPARTAMENTO', 'ANALISTA')")
    public ResponseEntity<ApiResponse<ProcessInstance>> patch(
            @PathVariable String id, @RequestBody ProcessInstance partial) {
        return ResponseEntity.ok(ApiResponse.ok("Instancia actualizada parcialmente", instanciaService.patch(id, partial)));
    }

    @Operation(summary = "Eliminar instancia de proceso")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        instanciaService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Instancia eliminada", null));
    }

    @Operation(summary = "Tomar tarea", description = "Un OFFICER reclama una tarea (ROJO → AMARILLO)")
    @PostMapping("/{id}/tomar")
    @PreAuthorize("hasAnyRole('OFFICER', 'SUPERVISOR', 'ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<ProcessInstance>> tomarTarea(
            @PathVariable String id,
            @RequestParam String idUsuario,
            @RequestParam String nombreUsuario) {
        return ResponseEntity.ok(ApiResponse.ok("Tarea tomada exitosamente",
            instanciaService.tomarTarea(id, idUsuario, nombreUsuario)));
    }

    @Operation(summary = "Obtener formulario del nodo actual",
               description = "Recibe el ID de la tarea activa y retorna los campos del formulario del nodo BPMN")
    @GetMapping("/{id}/formulario")
    @PreAuthorize("hasAnyRole('OFFICER', 'SUPERVISOR', 'ADMIN', 'ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerFormulario(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(instanciaService.obtenerFormulario(id)));
    }

    @Operation(summary = "Crear usuario cliente",
               description = "Crea un usuario CLIENT vinculado a la instancia usando datos del flujo")
    @PostMapping("/{id}/crear-cliente")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<CrearClienteResponse>> crearCliente(
            @PathVariable String id,
            @Valid @RequestBody CrearClienteRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
            "Usuario cliente creado",
            instanciaService.crearClienteDesdeTarea(id, request)));
    }

    @Operation(summary = "Asignar cliente existente",
               description = "Asocia un usuario CLIENT existente a la instancia del trámite")
    @PostMapping("/{id}/asignar-cliente")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<CrearClienteResponse>> asignarCliente(
            @PathVariable String id,
            @Valid @RequestBody AsignarClienteRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
            "Cliente asignado",
            instanciaService.asignarClienteDesdeTarea(id, request.getIdCliente())));
    }

    @Operation(summary = "Avanzar tarea", description = "Completar la tarea actual (AMARILLO → VERDE) y avanzar al siguiente nodo")
    @PostMapping("/{id}/avanzar")
    @PreAuthorize("hasAnyRole('OFFICER', 'SUPERVISOR', 'ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<ProcessInstance>> avanzarTarea(
            @PathVariable String id,
            @RequestBody Map<String, Object> datoformulario) {
        ProcessInstance instancia = instanciaService.avanzarTarea(id, datoformulario);
        if (instancia.getEstado() == InstanceStatus.COMPLETADO) {
            return ResponseEntity.ok(ApiResponse.ok("Flujo finalizado exitosamente", instancia));
        }
        if (instancia.getEstado() == InstanceStatus.CANCELADO) {
            return ResponseEntity.ok(ApiResponse.ok("Flujo cancelado", instancia));
        }
        return ResponseEntity.ok(ApiResponse.ok("Tarea avanzada exitosamente",
            instancia));
    }

    @Operation(summary = "Iniciar nuevo flujo de trabajo",
               description = "Crea una instancia y dispara automáticamente la primera tarea del proceso")
    @PostMapping("/iniciar")
    @PreAuthorize("hasAnyRole('OFFICER', 'SUPERVISOR', 'ADMIN', 'ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<ProcessInstance>> iniciarFlujo(
            @RequestBody IniciarFlujoRequest request,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails principal) {
        String idDepartamento = null;
        if (principal instanceof com.workflow.modules.users.model.User u) {
            idDepartamento = u.getIdDepartamento();
        }
        ProcessInstance instancia = instanciaService.iniciarFlujo(
            request.getIdPolitica(), request.getIdCliente(), request.getNodoInicioId(), request.getNombreFlujo(), idDepartamento);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Flujo iniciado exitosamente", instancia));
    }

    @Data
    public static class IniciarFlujoRequest {
        private String idPolitica;
        private String idCliente;
        private String nodoInicioId; // optional; auto-detected if null
        private String nombreFlujo;  // optional; nombre visible del trámite
    }

    @Data
    public static class AsignarClienteRequest {
        @NotBlank(message = "El idCliente es requerido")
        private String idCliente;
    }
}

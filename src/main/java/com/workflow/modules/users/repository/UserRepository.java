package com.workflow.modules.users.repository;

import com.workflow.modules.users.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByCorreo(String correo);

    Optional<User> findByCorreoIgnoreCase(String correo);

    Optional<User> findByNombreUsuario(String nombreUsuario);

    Optional<User> findByNombreUsuarioIgnoreCase(String nombreUsuario);

    boolean existsByCorreo(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByNombreUsuario(String nombreUsuario);

    boolean existsByNombreUsuarioIgnoreCase(String nombreUsuario);

    List<User> findByIdDepartamento(String idDepartamento);

    List<User> findByActivo(boolean activo);
}

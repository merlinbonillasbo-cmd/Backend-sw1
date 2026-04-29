package com.workflow.modules.quejas.repository;

import com.workflow.modules.quejas.model.Queja;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuejaRepository extends MongoRepository<Queja, String> {
    List<Queja> findByIdUsuarioOrderByFechaCreacionDesc(String idUsuario);
    List<Queja> findByDepartamentoCodigoOrderByFechaCreacionDesc(String departamentoCodigo);
}

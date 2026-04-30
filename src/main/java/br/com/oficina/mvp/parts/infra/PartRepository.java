package br.com.oficina.mvp.parts.infra;

import br.com.oficina.mvp.parts.domain.Part;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartRepository extends JpaRepository<Part, Long> {}

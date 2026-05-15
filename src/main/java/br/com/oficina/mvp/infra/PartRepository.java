package br.com.oficina.mvp.infra;

import br.com.oficina.mvp.domains.Part;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartRepository extends JpaRepository<Part, Long> {}

package br.com.oficina.mvp.part.adapter.out.persistence;

import br.com.oficina.mvp.part.domain.Part;
import org.springframework.data.jpa.repository.JpaRepository;

interface PartJpaRepository extends JpaRepository<Part, Long> {}

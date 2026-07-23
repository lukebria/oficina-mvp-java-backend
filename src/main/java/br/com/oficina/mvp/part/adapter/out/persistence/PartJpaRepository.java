package br.com.oficina.mvp.part.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface PartJpaRepository extends JpaRepository<PartJpaEntity, Long> {}

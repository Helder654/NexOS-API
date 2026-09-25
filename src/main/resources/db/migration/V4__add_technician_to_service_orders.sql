ALTER TABLE tb_ordens_servico
    ADD COLUMN id_tecnico BIGINT;

ALTER TABLE tb_ordens_servico
    ADD CONSTRAINT fk_ordens_servico_tecnico
    FOREIGN KEY (id_tecnico) REFERENCES tb_usuarios (id_usuario);

CREATE INDEX idx_ordens_servico_tecnico ON tb_ordens_servico (id_tecnico);

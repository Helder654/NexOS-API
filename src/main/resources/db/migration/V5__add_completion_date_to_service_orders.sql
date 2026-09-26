ALTER TABLE tb_ordens_servico
    ADD COLUMN data_finalizacao TIMESTAMP;

CREATE INDEX idx_ordens_servico_data_finalizacao ON tb_ordens_servico (data_finalizacao);

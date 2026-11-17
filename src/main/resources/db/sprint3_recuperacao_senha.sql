-- Execute uma vez no phpMyAdmin com o banco caredrop selecionado.
-- Armazena apenas o hash do código; códigos vencem em 10 minutos.
USE caredrop;

CREATE TABLE IF NOT EXISTS recuperacoes_senha (
 email VARCHAR(254) PRIMARY KEY,
 codigo_hash CHAR(64) NOT NULL,
 expira_em TIMESTAMP NOT NULL,
 tentativas INT NOT NULL DEFAULT 0,
 criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

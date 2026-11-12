-- Execute uma vez no phpMyAdmin com o banco caredrop selecionado.
-- As tabelas existentes do CareDrop não são apagadas nem alteradas.
USE caredrop;

CREATE TABLE IF NOT EXISTS usuarios (
 id INT PRIMARY KEY AUTO_INCREMENT,
 nome VARCHAR(100) NOT NULL,
 telefone VARCHAR(25) NOT NULL,
 email VARCHAR(254) NOT NULL UNIQUE,
 senha_hash VARCHAR(255) NOT NULL,
 perfil ENUM('ADMIN','CUIDADOR') NOT NULL,
 ativo BOOLEAN NOT NULL DEFAULT TRUE,
 criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cadastros_pendentes (
 email VARCHAR(254) PRIMARY KEY,
 nome VARCHAR(100) NOT NULL,
 telefone VARCHAR(25) NOT NULL,
 senha_hash VARCHAR(255) NOT NULL,
 perfil ENUM('ADMIN','CUIDADOR') NOT NULL,
 codigo_hash CHAR(64) NOT NULL,
 expira_em TIMESTAMP NOT NULL,
 tentativas INT NOT NULL DEFAULT 0,
 criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS prescricoes_medicamentos (
 id INT PRIMARY KEY AUTO_INCREMENT,
 idoso_id INT NOT NULL,
 medicamento_id INT NOT NULL,
 dosagem VARCHAR(80) NOT NULL,
 horario TIME NOT NULL,
 data_inicio DATE NOT NULL,
 data_fim DATE NULL,
 instrucoes VARCHAR(255) NULL,
 ativo BOOLEAN NOT NULL DEFAULT TRUE,
 criado_por INT NULL,
 criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_prescricao_idoso FOREIGN KEY (idoso_id) REFERENCES idosos(id),
 CONSTRAINT fk_prescricao_medicamento FOREIGN KEY (medicamento_id) REFERENCES medicamentos(id),
 CONSTRAINT fk_prescricao_usuario FOREIGN KEY (criado_por) REFERENCES usuarios(id),
 INDEX idx_prescricao_ativa_data (ativo,data_inicio,data_fim,horario),
 INDEX idx_prescricao_idoso (idoso_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS agenda_doses (
 id INT PRIMARY KEY AUTO_INCREMENT,
 prescricao_id INT NOT NULL,
 data_agendada DATE NOT NULL,
 data_hora DATETIME NOT NULL,
 responsavel VARCHAR(100) NOT NULL,
 UNIQUE KEY uq_agenda_dose (prescricao_id,data_agendada),
 CONSTRAINT fk_agenda_prescricao FOREIGN KEY (prescricao_id) REFERENCES prescricoes_medicamentos(id),
 INDEX idx_agenda_data (data_agendada)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

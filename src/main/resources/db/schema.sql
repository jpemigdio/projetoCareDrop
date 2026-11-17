CREATE DATABASE IF NOT EXISTS caredrop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE caredrop;

CREATE TABLE IF NOT EXISTS idosos (
 id INT PRIMARY KEY AUTO_INCREMENT, nome VARCHAR(100) NOT NULL,
 data_nascimento DATE NOT NULL, quarto VARCHAR(30), condicoes VARCHAR(255), ativo BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS medicamentos (
 id INT PRIMARY KEY AUTO_INCREMENT, nome VARCHAR(100) NOT NULL,
 dosagem VARCHAR(50), frequencia VARCHAR(50), ativo BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS administracoes_medicamentos (
 id INT PRIMARY KEY AUTO_INCREMENT, idoso_id INT NOT NULL, medicamento_id INT NOT NULL,
 data_hora DATETIME NOT NULL, responsavel VARCHAR(100) NOT NULL, observacao VARCHAR(255),
 FOREIGN KEY (idoso_id) REFERENCES idosos(id), FOREIGN KEY (medicamento_id) REFERENCES medicamentos(id)
);
CREATE TABLE IF NOT EXISTS doadores (
 id INT PRIMARY KEY AUTO_INCREMENT, nome VARCHAR(100) NOT NULL, cpf VARCHAR(14) NOT NULL UNIQUE,
 tipo_sanguineo ENUM('A+','A-','B+','B-','AB+','AB-','O+','O-') NOT NULL,
 telefone VARCHAR(20), ativo BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS doacoes (
 id INT PRIMARY KEY AUTO_INCREMENT, doador_id INT NOT NULL, tipo_sanguineo ENUM('A+','A-','B+','B-','AB+','AB-','O+','O-') NOT NULL,
 quantidade_bolsas INT NOT NULL CHECK (quantidade_bolsas > 0), data_doacao DATE NOT NULL,
 FOREIGN KEY (doador_id) REFERENCES doadores(id)
);
CREATE TABLE IF NOT EXISTS estoque_sangue (
 id INT PRIMARY KEY AUTO_INCREMENT, tipo_sanguineo ENUM('A+','A-','B+','B-','AB+','AB-','O+','O-') NOT NULL UNIQUE,
 quantidade INT NOT NULL DEFAULT 0 CHECK (quantidade >= 0), ultima_atualizacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS consumos_sangue (
 id INT PRIMARY KEY AUTO_INCREMENT, tipo_sanguineo ENUM('A+','A-','B+','B-','AB+','AB-','O+','O-') NOT NULL,
 quantidade INT NOT NULL CHECK (quantidade > 0), data_consumo DATE NOT NULL, motivo VARCHAR(180)
);
INSERT IGNORE INTO estoque_sangue(tipo_sanguineo, quantidade) VALUES
('A+',0),('A-',0),('B+',0),('B-',0),('AB+',0),('AB-',0),('O+',0),('O-',0);

-- Acesso seguro com confirmação de e-mail
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

CREATE TABLE IF NOT EXISTS recuperacoes_senha (
 email VARCHAR(254) PRIMARY KEY,
 codigo_hash CHAR(64) NOT NULL,
 expira_em TIMESTAMP NOT NULL,
 tentativas INT NOT NULL DEFAULT 0,
 criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Uma prescrição representa um horário diário. Para mais horários, cadastre
-- uma prescrição para cada horário do medicamento.
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

-- Somente as doses administradas precisam ser persistidas. Pendente/atrasada
-- são calculadas pela hora agendada e pela data atual.
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

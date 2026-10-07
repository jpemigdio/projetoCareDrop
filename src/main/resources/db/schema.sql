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

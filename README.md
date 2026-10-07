# Care Drop — MediCuidar

Aplicação acadêmica em Java 17, JSP, Servlets, JDBC e MySQL, organizada em MVC/DAO. A interface toma como referência a imagem enviada: navegação lateral verde, cartões de resumo e painéis claros.

## Pré-requisitos

- JDK 17
- Maven 3.9 ou superior
- MySQL 8
- Tomcat 10.1 (Jakarta Servlet 6)

## Configurar e executar

1. No MySQL Workbench ou terminal, execute `src/main/resources/db/schema.sql`.
2. Configure a conexão, se necessário, nas variáveis de ambiente `DB_URL`, `DB_USER` e `DB_PASSWORD`. Os valores padrão apontam para `jdbc:mysql://localhost:3306/caredrop`, usuário `root` e senha vazia.
3. Na pasta do projeto, gere o WAR com `mvn clean package`.
4. Copie `target/care-drop.war` para `webapps` do Tomcat 10.1 e inicie o servidor.
5. Abra `http://localhost:8080/care-drop/`.

## Módulos do MVP

- Dashboard com contagem de residentes, medicamentos, doações e bolsas em estoque.
- Cadastro e consulta de residentes, medicamentos e doadores; edição e arquivamento lógico dos cadastros estão disponíveis na camada DAO/Servlet.
- Registro e histórico de administração de medicamentos.
- Registro de doações com atualização transacional do estoque por tipo sanguíneo.
- Registro de consumo com baixa transacional e proteção contra estoque negativo.
- Visão do estoque dos oito tipos sanguíneos e histórico de consumo.
- Previsão simples usando a média mensal dos registros dos últimos seis meses.

## Estrutura

`model/` contém as sete entidades; `dao/SistemaDAO.java` concentra as consultas JDBC do MVP; `controller/SistemaServlet.java` recebe as ações e encaminha os dados para a JSP em `WEB-INF/views/app.jsp`. A conexão fica em `util/ConnectionFactory.java`.

> Para entrega acadêmica, cadastros básicos são persistidos no MySQL. O sistema não inclui autenticação, autorização por perfil nem rotina de expiração/validade de bolsas, pois esses pontos não foram especificados no documento recebido.

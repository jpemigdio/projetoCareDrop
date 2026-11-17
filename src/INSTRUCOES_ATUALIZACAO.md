# Atualizar o CareDrop com as Sprints e o login

Estas alterações continuam usando Java 17, JSP, Servlets, Maven, MySQL/MariaDB e Tomcat 10.1.

## 1. Aplicar os arquivos ao projeto

1. Feche o VS Code ou deixe aberto o projeto C:\Users\joaop\Documents\projetoCareDrop.
2. Extraia o pacote de atualização na pasta raiz do projeto, mantendo a estrutura de pastas. Autorize substituir os arquivos que já existem.
3. No VS Code, confira que os arquivos novos aparecem em src\main\java e src\main\webapp.

## 2. Atualizar o banco existente

1. Deixe o MySQL do XAMPP rodando.
2. Abra http://localhost/phpmyadmin.
3. Clique no banco caredrop à esquerda.
4. Clique em **Importar**, escolha src\main\resources\db\sprint1_sprint2_login_migration.sql e clique em **Importar/Executar**.
5. Aguarde a mensagem de sucesso. A migração cria as novas tabelas de usuários, códigos de confirmação, prescrições e doses sem apagar as tabelas atuais.

## 3. Configurar o envio de códigos por e-mail

O cadastro inicial e o convite de cuidadores enviam um código real. O remetente precisa fornecer SMTP com STARTTLS na porta 587.

1. No Windows, pesquise **Editar as variáveis de ambiente da conta**.
2. Crie estas variáveis para sua conta do Windows:
   - SMTP_HOST: servidor SMTP do seu provedor.
   - SMTP_PORT: 587.
   - SMTP_USER: conta usada para enviar.
   - SMTP_PASSWORD: senha de aplicativo/SMTP fornecida pelo provedor.
   - SMTP_FROM: endereço remetente, normalmente igual a SMTP_USER.
3. Salve as variáveis e feche completamente o Tomcat.
4. Inicie o Tomcat novamente para ele carregar as variáveis.

Não coloque a senha SMTP no código, em arquivo versionado ou no GitHub.

## 4. Gerar e implantar o WAR

1. Abra o terminal do VS Code na pasta raiz do projeto, onde está pom.xml.
2. Execute mvn package e aguarde BUILD SUCCESS.
3. O arquivo atualizado será target\care-drop.war.
4. Pare o Tomcat, substitua care-drop.war na pasta webapps por esse arquivo e renomeie a pasta extraída care-drop para care-drop-backup.
5. Inicie o Tomcat e abra http://localhost:8080/care-drop/.

## 5. Criar as contas e usar as Sprints

1. No primeiro acesso, crie o administrador com nome, telefone, e-mail e senha.
2. Abra o e-mail, use o link e digite o código de seis dígitos. O código expira em 10 minutos.
3. Entre como **Administrador** e cadastre residentes e medicamentos no menu.
4. Em **Prescrições**, associe residente, medicamento, dosagem e um horário diário. Cadastre uma linha para cada horário recorrente.
5. Em **Usuários**, o administrador cadastra cuidadores com nome, telefone, e-mail e senha inicial. Cada cuidador confirma o e-mail antes de entrar.
6. Entre como **Cuidador** para consultar residentes e perfis, filtrar a agenda por residente, turno e status e marcar as doses administradas.
7. Na agenda, clique em **Ativar alertas sonoros** e mantenha a página aberta. O navegador só libera som depois de uma ação do usuário.

As doses pendentes e atrasadas são calculadas conforme a data e o horário da prescrição. Ao marcar uma dose, o CareDrop grava a administração e o responsável no histórico.

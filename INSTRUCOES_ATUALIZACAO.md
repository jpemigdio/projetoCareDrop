# Atualização CareDrop — medicamentos, equipe e Sprints 1 e 2

## Aplicar no projeto

1. Feche o Tomcat.
2. Extraia este ZIP na pasta raiz do projeto: C:\Users\joaop\Documents\projetoCareDrop (a pasta onde está o pom.xml). Autorize substituir os arquivos.
3. No terminal do VS Code, confirme que o caminho atual é a raiz e execute mvn package.
4. Aguarde BUILD SUCCESS. O arquivo gerado é target\care-drop.war.
5. Na pasta webapps da mesma instalação do Tomcat que você inicia, pare o Tomcat e renomeie a pasta antiga care-drop para care-drop-anterior (se esse nome já existir, escolha outro).
6. Substitua care-drop.war pelo arquivo gerado em target.
7. Inicie essa mesma instalação do Tomcat e abra http://localhost:8080/care-drop/auth.

## Como demonstrar os cadastros

- O primeiro administrador já criado continua usando o e-mail e a senha dele.
- Depois de entrar como administrador, abra Usuários.
- Escolha Cuidador ou Administrador, preencha nome, telefone, e-mail e senha inicial, e envie.
- A pessoa confirma o e-mail pelo código recebido e depois entra pelo perfil correspondente na tela de login.
- O administrador inicial não é removido. Novos administradores são cadastrados dentro da área administrativa.

## Itens das Sprints

- O administrador cadastra residentes, medicamentos e prescrições com horário.
- O cuidador consulta residentes e perfis, filtra a agenda por residente, turno e situação e marca doses administradas.
- A agenda exibe doses pendentes/atrasadas e tem alerta sonoro ativado pelo botão da própria tela.
- O painel mostra residentes, medicamentos e doses administradas.
- A navegação da aplicação não inclui mais a área de sangue.
- O botão Encerrar sessão aparece no topo à direita.

## Banco de dados e e-mail

- Não importe o SQL novamente: as tabelas das Sprints e de login já foram importadas. A atualização preserva o banco atual.
- As tabelas antigas de sangue não são apagadas; a aplicação deixa de exibi-las e usá-las, preservando os dados existentes.
- Mantenha o MySQL e as variáveis SMTP configuradas. O Tomcat precisa ser reiniciado depois de qualquer alteração nessas variáveis.
- Cada conta precisa de um e-mail exclusivo. Não use o e-mail do administrador para criar outro usuário.

## Se aparecer novamente o painel antigo

Confirme que você substituiu o WAR na pasta webapps pertencente ao mesmo Tomcat cujo startup.bat está sendo iniciado. Pare o Tomcat antes de trocar o WAR e a pasta extraída care-drop.

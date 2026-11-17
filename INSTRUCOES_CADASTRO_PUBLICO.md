# CareDrop — cadastro público e login

## Atualizar

1. Extraia este ZIP na raiz C:\Users\joaop\Documents\projetoCareDrop, onde está o pom.xml. Substitua os arquivos existentes.
2. No terminal do VS Code, nessa pasta, execute mvn package e aguarde BUILD SUCCESS.
3. Pare o Tomcat.
4. Na pasta webapps da mesma instalação do Tomcat, renomeie a pasta extraída care-drop para care-drop-anterior (se o nome já estiver ocupado, escolha outro).
5. Copie target\care-drop.war para essa pasta webapps e substitua o WAR anterior.
6. Inicie o Tomcat e abra http://localhost:8080/care-drop/auth.

## Demonstrar cadastro e login

1. Na tela de acesso, clique em “Não tem cadastro? Criar cadastro”.
2. Escolha o perfil Cuidador ou Administrador da casa de repouso.
3. Preencha nome, telefone, e-mail, senha e confirmação da senha. Cada conta precisa de um e-mail ainda não cadastrado.
4. Clique em “Criar cadastro e enviar código”.
5. Digite o código de seis dígitos recebido por e-mail. O código expira em dez minutos.
6. Volte ao acesso, selecione o mesmo perfil, e entre com o e-mail e a senha cadastrados.

A inscrição pública permite que visitantes escolham Cuidador ou Administrador, conforme solicitado para a demonstração acadêmica. O primeiro cadastro, caso ainda não exista nenhuma conta, precisa criar o administrador inicial.

## Itens do projeto

- A área de sangue foi removida da navegação e do painel.
- O administrador cadastra residentes, medicamentos e prescrições.
- O cuidador consulta residentes e perfis, filtra a agenda por residente, turno e situação e marca doses administradas.
- A agenda tem alertas sonoros ativados pelo botão da própria tela.
- O botão Encerrar sessão permite voltar ao acesso.

## Banco e e-mail

- Não importe o SQL novamente; as tabelas de login e das Sprints já foram importadas.
- As tabelas antigas de sangue permanecem no banco para preservar os dados, mas a interface não as exibe nem as usa.
- O Tomcat precisa estar reiniciado depois de configurar as variáveis SMTP.
- Não compartilhe a senha de app e não a coloque no código ou no GitHub.

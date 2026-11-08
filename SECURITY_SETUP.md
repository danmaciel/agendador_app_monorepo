# Configuracao segura (desenvolvimento e producao)

- Configuracoes de producao requerem `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `APP_ADMIN_LOGIN` e `APP_ADMIN_SENHA` no ambiente.
- Para Docker Compose, defina `POSTGRES_PASSWORD` localmente (por exemplo em um `.env` nao versionado). Copie `.env.example` e preencha com valores fortes antes de iniciar.
- O perfil padrao `application.properties` ainda contem valores de desenvolvimento para H2 e administrador: nunca o exponha como ambiente de producao.
- A API no Compose permanece comentada como no projeto original; essa mudanca nao a ativa.
- Qualquer senha, conta inicial ou segredo JWT anteriormente publicado que tenha sido utilizado deve ser rotacionado ou revogado. Remover valores da branch atual nao apaga o historico Git.
- Antes de mesclar, verifique inicializacao com `SPRING_PROFILES_ACTIVE=prod` e as variaveis exigidas, sem imprimir segredos nos logs.

## Limites de autenticacao ainda abertos

- A regra de autorizacao HTTP permite **somente** `POST /api/v1/usuario` sem autenticacao para cadastro. Leitura e listagem de usuarios exigem JWT; uma regra de proprietario (ou admin) para leitura por ID ainda deve ser definida. Autenticacao por si so nao impede que uma conta leia o perfil de outra.
- A tabela de refresh tokens armazena o UUID opaco em texto, nao um fingerprint. A rotacao atual nao implementa familias de token e detecao de replay robusta; nao documentar como revogacao distribuida ou protecao de sessao pronta para producao.
- O perfil de desenvolvimento possui login/senha inicial conhecidos. Nunca utilizar esse perfil em servidor acessivel externamente. O perfil `prod` exige credenciais fornecidas fora do codigo.
- Antes de ativar qualquer uso real, adicionar testes de autorizacao por usuario, revisar gestao de sessao, rate limiting e tratamento de erros. Valores antigos publicados precisam ser rotacionados se tiverem sido usados fora de laboratorio.

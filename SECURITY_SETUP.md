# Configuracao segura (desenvolvimento e producao)

- Configuracoes de producao requerem `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `APP_ADMIN_LOGIN` e `APP_ADMIN_SENHA` no ambiente.
- Para Docker Compose, defina `POSTGRES_PASSWORD` localmente (por exemplo em um `.env` nao versionado). Copie `.env.example` e preencha com valores fortes antes de iniciar.
- O perfil padrao `application.properties` ainda contem valores de desenvolvimento para H2 e administrador: nunca o exponha como ambiente de producao.
- A API no Compose permanece comentada como no projeto original; essa mudanca nao a ativa.
- Qualquer senha, conta inicial ou segredo JWT anteriormente publicado que tenha sido utilizado deve ser rotacionado ou revogado. Remover valores da branch atual nao apaga o historico Git.
- Antes de mesclar, verifique inicializacao com `SPRING_PROFILES_ACTIVE=prod` e as variaveis exigidas, sem imprimir segredos nos logs.

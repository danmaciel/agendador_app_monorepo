# Configuracao segura (desenvolvimento e producao)

- Configuracoes de producao requerem `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `APP_ADMIN_LOGIN` e `APP_ADMIN_SENHA` no ambiente.
- Para Docker Compose, defina `POSTGRES_PASSWORD` localmente (por exemplo em um `.env` nao versionado). Copie `.env.example` e preencha com valores fortes antes de iniciar.
- O perfil padrao `application.properties` ainda contem valores de desenvolvimento para H2 e administrador: nunca o exponha como ambiente de producao.
- A API no Compose permanece comentada como no projeto original; essa mudanca nao a ativa.
- Qualquer senha, conta inicial ou segredo JWT anteriormente publicado que tenha sido utilizado deve ser rotacionado ou revogado. Remover valores da branch atual nao apaga o historico Git.
- Antes de mesclar, verifique inicializacao com `SPRING_PROFILES_ACTIVE=prod` e as variaveis exigidas, sem imprimir segredos nos logs.

## Limites de autenticacao ainda abertos

- As rotas publicas incluem cadastro `POST /api/v1/usuario`, autenticacao `/api/v1/auth/**`, documentacao Swagger e console H2 no perfil de desenvolvimento. O console H2 fica desativado no perfil `prod`, mas o matcher da cadeia de seguranca continua `permitAll`: nao exponha o perfil de desenvolvimento externamente. Perfis por ID, agendamentos por ID, listagens pessoais e historicos usam verificacoes de proprietario ou administrador; criacao forcada, aprovacao, rejeicao, exclusao logica e listagens administrativas exigem `ROLE_ADMIN`. Esses testes nao substituem uma revisao completa de seguranca de todos os fluxos.
- Novos refresh tokens sao armazenados apenas como fingerprint SHA-256. A compatibilidade transitoria com tokens UUIDv4 legados mantem registros antigos em texto ate rotacao ou expiracao; ver [plano de transicao](docs/REFRESH_TOKEN_TRANSITION.md). A rotacao usa bloqueio pessimista, com testes H2 e PostgreSQL cobrindo disputas especificas entre refreshes e entre login e refresh. Ainda nao implementa familias, deteccao robusta de replay ou garantia para falhas e concorrencias arbitrarias. Nao anunciar como sessao pronta para producao.
- O perfil de desenvolvimento possui login/senha inicial conhecidos. Nunca utilizar esse perfil em servidor acessivel externamente. O perfil `prod` exige credenciais fornecidas fora do codigo.
- O controle administrativo de listagem, promocao e desativacao tem testes de acesso permitido e negado via MockMvc. Antes de qualquer uso real, revisar os demais recursos e a gestao de sessao, rate limiting e tratamento de erros. Valores antigos publicados precisam ser rotacionados se tiverem sido usados fora de laboratorio.

- O caso de uso de refresh agora recusa explicitamente a emissao de novos access JWTs quando a conta vinculada esta desativada. Isto nao revoga access JWTs previamente emitidos nem substitui verificacao atomica de estado da conta com a rotacao; conferir essa corrida numa futura revisao do armazenamento.

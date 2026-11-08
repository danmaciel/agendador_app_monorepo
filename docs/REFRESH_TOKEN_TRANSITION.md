# Transição de refresh tokens em texto para fingerprints SHA-256

**Escopo:** backend de demonstração local; não é uma implantação de produção.

## Contrato após a transição

- O login gera um segredo opaco de **256 bits aleatórios** por `SecureRandom`, codificado em Base64 URL-safe sem padding.
- O segredo cru é entregue somente ao cliente em `TokenResponse.refreshToken`. A entidade persiste `sha256:<digest hexadecimal>` na coluna existente `refresh_token.token`, que já comporta o formato.
- O serviço calcula o fingerprint do segredo recebido e consulta a coluna. Nenhum hash do banco é aceito como credencial Bearer.
- A rotação continua invalidando o token anterior e criando outro. O hash protege o segredo em repouso contra leitura direta de banco, **mas não** implementa famílias de token, detecção robusta de replay, concorrência atômica de consumo ou revogação imediata de JWTs já emitidos.

## Compatibilidade temporária

O formato antigo era um UUID versão 4 em texto puro na mesma coluna. Durante a transição, depois de falhar a consulta do fingerprint, a aplicação admite **apenas o formato canônico UUIDv4 legado** para lookup direto. Uma renovação bem-sucedida elimina os tokens anteriores do usuário e grava um sucessor com fingerprint. Um novo login também substitui os registros anteriores do usuário.

**Não executar um UPDATE indiscriminado sobre a coluna `token` nem reescrever a migration V5**. O schema permanece o mesmo; não há transformação destrutiva de dados. Faça backup e teste uma base de cópia antes de qualquer rollout persistente.

A compatibilidade não tem prazo automático para ser removida. Um planejamento real deve definir janela máxima de refresh, medir registros legados **sem registrar tokens**, encerrar a aceitação de UUIDs antigos e remover o fallback em PR próprio. Enquanto houver registros legados, quem obtiver acesso à coluna poderá usar esses valores até a rotação ou expiração: a migração é **gradual**, não retroativa.

## Riscos remanescentes

1. O refresh consulta o registro com **`PESSIMISTIC_WRITE`** dentro da mesma transação de validação, exclusão e emissão do sucessor. Um teste com **duas transações H2 independentes** verifica um único vencedor; **isso não substitui validação em PostgreSQL**. Não há token-family replay detection e uma tentativa posterior com token já consumido é recusada, sem revogar automaticamente o sucessor. Login concorrente, falhas de rede e outras operações de sessão ainda requerem avaliação.
2. A verificação de `ativo` não está serializada com uma mudança concorrente do cadastro.
3. O frontend/cliente que possui o token continua responsável por protegê-lo. O hash no banco não elimina XSS nem captura de resposta.
4. Segredos anteriormente expostos em logs, backups ou commits devem ser considerados comprometidos e revogados; não são corrigidos por este PR.
5. Refresh inválido/expirado, limpeza de sessões e logout ainda exigem testes adicionais para produção.

## Evidências

- `RefreshTokenFingerprintTest`: entropia/formato, derivação de hash e não persistência do segredo na entidade.
- `LoginUseCaseTest`: resposta contém o segredo cru, mas entidade contém somente o fingerprint.
- `RefreshTokenUseCaseTest`: consulta por fingerprint, aceitação do UUIDv4 legado, recusa de fingerprint como Bearer, expiração e conta desativada.
- `RefreshTokenConcurrentIntegrationTest`: duas transações simultâneas em H2, com uma única renovação bem-sucedida (sujeito a confirmação do CI).
- `.github/workflows/ci.yml`: Maven `verify` executado no GitHub Actions.

Os testes com H2 exercitam bloqueio de linha e transações independentes, mas **não provam as semânticas de bloqueio de PostgreSQL**, proteção universal contra replay ou ausência completa de segredos em todos os logs. Use [SECURITY_SETUP.md](../SECURITY_SETUP.md) para as restrições do ambiente.

# Laboratório I — Computação Gráfica

Implementação em Java das transformações entre coordenadas do mundo, coordenadas normalizadas do dispositivo (NDC) e coordenadas do display, com ativação de **um único pixel verde (`#00FF00`)**. Inclui o exercício complementar da aula G2 sobre **paleta RGB de 24 bits endereçada por índices de 12 bits**.

Disciplina: Computação Gráfica — UEPB. Professor: Robson Pequeno de Sousa.

## Pré-requisitos

- JDK instalado (por exemplo, Java 21).
- Windows, Linux ou macOS com ambiente gráfico.
- Apenas bibliotecas nativas do Java: Swing e AWT.

No terminal, verifique: `java -version` e `javac -version`.

## 1. Transformações de coordenadas

Arquivo: `TransformacoesCoordenadas.java`.

Execute no terminal, a partir da pasta do repositório:

```powershell
javac TransformacoesCoordenadas.java
java TransformacoesCoordenadas
```

Na interface:

1. Defina a janela do mundo (`xmin`, `xmax`, `ymin`, `ymax`), com `xmin < xmax` e `ymin < ymax`.
2. Informe o ponto `(x, y)` da janela.
3. Selecione um dos dois cenários NDC: `[0,1] × [0,1]` ou `[-1,1] × [-1,1]`.
4. Clique em **Ativar pixel**. A área preta mostra um pixel verde na posição calculada; os resultados numéricos são exibidos abaixo dela.
5. Opcionalmente, clique na área preta para experimentar a transformação inversa (dispositivo de entrada → NDC → mundo).

O desenho usa `BufferedImage.setRGB()`, sem ampliar o ponto ou adicionar grade/eixos. As dimensões exibidas correspondem à **área de desenho** (não necessariamente à resolução total do monitor).

### Procedimentos solicitados na aula G2

| Procedimento do slide | Método Java | Transformação |
| --- | --- | --- |
| `user_to_ndc` | `userToNdc` | Mundo → NDC |
| `ndc_to_user` | `ndcToUser` | NDC → Mundo |
| `ndc_to_dc` | `ndcToDc` | NDC → dispositivo |
| `inp_to_ndc` | `inpToNdc` | Dispositivo de entrada (mouse) → NDC |

Para a janela de mundo `[xmin, xmax] × [ymin, ymax]`, primeiro calculam-se:

```text
u = (x - xmin) / (xmax - xmin)
v = (y - ymin) / (ymax - ymin)
```

- Cenário `[0,1]`: `NDC = (u, v)`.
- Cenário `[-1,1]`: `NDC = (2u - 1, 2v - 1)`.

O mapeamento final para o display de largura `W` e altura `H` é:

```text
DCx = round(u * (W - 1))
DCy = round((1 - v) * (H - 1))
```

A inversão de Y é necessária porque a área de desenho do Swing tem origem no canto superior esquerdo.

### Teste rápido

Use `xmin=-10`, `xmax=30`, `ymin=5`, `ymax=25` e ponto `(10,15)`:

| Cenário | NDC esperado | Posição final |
| --- | --- | --- |
| `[0,1] × [0,1]` | `(0.5, 0.5)` | Centro |
| `[-1,1] × [-1,1]` | `(0, 0)` | Centro |

O ponto deve permanecer na mesma posição do display ao alternar o cenário.

Teste automático, sem abrir a interface:

```powershell
java TransformacoesCoordenadas --test
```

## 2. Exercício complementar — Tabela de cores

Arquivo: `TabelaCores.java`.

```powershell
javac TabelaCores.java
java TabelaCores
```

O exercício distingue o **índice armazenado no pixel (12 bits)** da **cor armazenada na paleta (24 bits)**:

- 12 bits permitem `2^12 = 4096` índices, de `0x000` a `0xFFF`.
- Cada índice acessa uma entrada da paleta contendo `RRGGBB`: 8 bits por canal, 24 bits ao todo.
- A paleta inicial é construída com os dígitos `RGB444` do índice expandidos para `RGB888` (`canal8 = canal4 × 17`); isso é uma **inicialização escolhida para demonstrar o mapeamento**, não uma limitação da paleta a apenas quatro bits por canal.
- Para demonstrar que a paleta contém cores RGB completas, altere a cor hexadecimal de uma entrada para qualquer valor de seis dígitos, por exemplo `#123456`.

A janela mostra 256 amostras por página (16 páginas), permite selecionar índices, editar cores e restaurar a paleta original. A grade representa **entradas da paleta**, não pixels ampliados da atividade principal.

Exemplos iniciais:

| Índice de 12 bits | Cor RGB de 24 bits |
| --- | --- |
| `000` | `#000000` (preto) |
| `F00` | `#FF0000` (vermelho) |
| `0F0` | `#00FF00` (verde) |
| `00F` | `#0000FF` (azul) |
| `FFF` | `#FFFFFF` (branco) |

Teste automático:

```powershell
java TabelaCores --test
```
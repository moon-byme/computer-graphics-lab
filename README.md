# Laboratório I — Computação Gráfica

Implementação em Java das transformações entre coordenadas do mundo, coordenadas normalizadas do dispositivo (NDC) e coordenadas do display, com desenho de pontos em verde (`#00FF00`) e estrutura de rasterização preparada para retas e circunferências. Inclui o exercício complementar da aula G2 sobre **paleta RGB de 24 bits endereçada por índices de 12 bits**.

Disciplina: Computação Gráfica — UEPB. Professor: Robson Pequeno de Sousa.

## Pré-requisitos

- JDK instalado (por exemplo, Java 21).
- Windows, Linux ou macOS com ambiente gráfico.
- Apenas bibliotecas nativas do Java: Swing e AWT.

No terminal, verifique: `java -version` e `javac -version`.

## Estrutura do projeto

O código segue o padrão **MVC**, com um pacote por aplicação:

```text
src/
├── transformacoes/                      # 1. Transformações de coordenadas
│   ├── TransformacoesCoordenadas.java   # ponto de entrada (main)
│   ├── model/
│   │   ├── Ponto.java                   # coordenadas reais (mundo/NDC)
│   │   ├── Pixel.java                   # posição no dispositivo
│   │   └── TransformadorCoordenadas.java# userToNdc, ndcToUser, ndcToDc, inpToNdc
│   ├── view/
│   │   ├── TransformacoesView.java      # janela e campos (sem regras de negócio)
│   │   └── Tela.java                    # frame buffer raster (BufferedImage)
│   ├── controller/
│   │   └── TransformacoesController.java# eventos, validação e orquestração
│   └── teste/
│       └── TransformacoesCoordenadasTeste.java   # --test
└── tabelacores/                         # 2. Exercício complementar
    ├── TabelaCores.java                 # ponto de entrada (main)
    ├── model/
    │   └── Paleta.java                  # 4096 entradas RGB de 24 bits
    ├── view/
    │   └── TabelaCoresView.java         # janela, grade de amostras e detalhes
    ├── controller/
    │   └── TabelaCoresController.java   # eventos, paginação e edição da paleta
    ├── util/
    │   └── Hexadecimal.java             # leitura/validação de valores hexadecimais
    └── teste/
        └── TabelaCoresTeste.java        # --test
```

- **Model**: dados e regras (transformações, paleta), sem dependência de Swing.
- **View**: componentes Swing; expõe getters/setters e registro de listeners.
- **Controller**: recebe os eventos da View, valida a entrada e atualiza Model e View.

## Algoritmos de rasterização

A interface `AlgoritmoRasterizacao` define uma saída comum em pixels DC para que os algoritmos não precisem conhecer nem alterar o renderer (`Tela.drawPixels`). A interface aceita as primitivas ponto, reta e circunferência. O ponto usa diretamente `drawPixel`; o renderer também aceita uma coleção de pixels para as formas compostas. O seletor lista os cinco algoritmos planejados: DDA e Bresenham para retas; polinomial, trigonométrico e ponto médio para circunferências. As implementações ainda serão feitas e devem ser registradas no controller com `registrarAlgoritmo`.

Cada algoritmo receberá uma `FormaRasterizacao` com coordenadas DC (ponto, reta com início/fim ou circunferência com centro/raio inteiro em pixels DC) e retornará os pixels a ativar. A interface confere se a forma corresponde ao tipo do algoritmo. Os campos de ponto inicial/centro e de reta na interface são informados em coordenadas do mundo e convertidos para DC antes da rasterização. Até as implementações serem registradas, a interface exibe um aviso e não desenha as retas/circunferências. Ao implementar Bresenham para reta, trate os demais octantes por simetria, refletindo/trocando os eixos e restaurando as coordenadas ao final.

## Compilação

Na raiz do repositório:

```powershell
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
```

No Linux/macOS: `javac -d out $(find src -name "*.java")`.

## 1. Transformações de coordenadas

Pacote: `transformacoes` (ponto de entrada: `src/transformacoes/TransformacoesCoordenadas.java`).

Após compilar, execute a partir da pasta do repositório:

```powershell
java -cp out transformacoes.TransformacoesCoordenadas
```

Na interface:

1. Defina a janela do mundo (`xmin`, `xmax`, `ymin`, `ymax`), com `xmin < xmax` e `ymin < ymax`.
2. Selecione **Ponto**, **Reta** ou **Circunferência**. Informe o ponto ou centro `(x, y)`; para reta, informe também o ponto final `(x final, y final)`; para circunferência, informe o raio em pixels DC.
3. Selecione um dos dois cenários NDC: `[0,1] × [0,1]` ou `[-1,1] × [-1,1]`.
4. Para ponto, clique em **Desenhar** para ativar o pixel. Para reta ou circunferência, selecione o algoritmo correspondente e clique em **Desenhar**; até a implementação e registro do algoritmo, a interface informa que o desenho está pendente.
5. Opcionalmente, clique na área de desenho para experimentar a transformação inversa (dispositivo de entrada → NDC → mundo).

O framebuffer mantém a resolução original; pontos e primitivas são armazenados como pixels verdes. Use **Ampliação** (1×, 2×, 4× ou 8×) para inspecionar os pixels; a opção **Grade** desenha limites entre pixels quando a ampliação é maior que 1×. A ampliação e a grade afetam apenas a visualização, não alteram os pixels armazenados nem as coordenadas DC. Cliques na imagem ampliada são convertidos de volta às coordenadas do display original antes da transformação inversa. As dimensões exibidas correspondem à **área de desenho** (não necessariamente à resolução total do monitor).

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
java -cp out transformacoes.TransformacoesCoordenadas --test
```

## 2. Exercício complementar — Tabela de cores

Pacote: `tabelacores` (ponto de entrada: `src/tabelacores/TabelaCores.java`).

```powershell
java -cp out tabelacores.TabelaCores
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
java -cp out tabelacores.TabelaCores --test
```
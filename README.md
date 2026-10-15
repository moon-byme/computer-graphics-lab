# Laboratórios — Computação Gráfica

Implementações em Java das atividades da disciplina:

1. **Transformações de coordenadas** (aula G2): coordenadas do mundo → coordenadas normalizadas do dispositivo (NDC) → coordenadas do display.
2. **Tabela de cores** (exercício complementar da aula G2): **paleta RGB de 24 bits endereçada por índices de 12 bits**.
3. **Rasterização** (aula G3): retas (**DDA** e **Bresenham**) e circunferências (**polinomial**, **trigonométrico** e **ponto médio**), com uma interface gráfica que permite desenhar pontos e está preparada para integrar algoritmos de reta e circunferência.

O framebuffer desenha pixels em verde (`#00FF00`); a interface também oferece ampliação e grade opcionais para inspecionar a imagem.


## Pré-requisitos

- JDK 25 ou superior (veja a observação abaixo).
- Windows, Linux ou macOS com ambiente gráfico, para as aplicações com interface. Os testes automáticos rodam sem interface.
- Apenas bibliotecas nativas do Java: Swing e AWT.

No terminal, verifique: `java -version` e `javac -version`.

> **Versão do Java:** os pontos de entrada `TransformacoesCoordenadas` e `TabelaCores` declaram `static void main` sem `public`, o que o Java só aceita sem flags a partir do JDK 25. No JDK 21, execute essas duas classes com `java --enable-preview -cp out ...`. Os testes de rasterização têm `public static void main` e rodam em qualquer JDK a partir do 17.

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
├── tabelacores/                         # 2. Exercício complementar
│   ├── TabelaCores.java                 # ponto de entrada (main)
│   ├── model/
│   │   └── Paleta.java                  # 4096 entradas RGB de 24 bits
│   ├── view/
│   │   └── TabelaCoresView.java         # janela, grade de amostras e detalhes
│   ├── controller/
│   │   └── TabelaCoresController.java   # eventos, paginação e edição da paleta
│   ├── util/
│   │   └── Hexadecimal.java             # leitura/validação de valores hexadecimais
│   └── teste/
│       └── TabelaCoresTeste.java        # --test
└── rasterizacao/                        # 3. Rasterização (ainda sem View)
    ├── model/
    │   ├── PlotadorPixel.java           # destino de cada pixel calculado
    │   ├── DDA.java                     # reta: método do declive
    │   ├── Bresenham.java               # reta: ponto médio, 8 octantes por simetria
    │   ├── CircunferenciaPolinomial.java
    │   ├── CircunferenciaTrigonometrica.java
    │   ├── CircunferenciaPontoMedio.java
    │   ├── SimetriaCircunferencia.java  # simetria de 8 pontos
    │   └── AlgoritmoCircunferencia.java # enum com os três algoritmos
    ├── controller/
    │   └── CircunferenciaController.java# validação e escolha do algoritmo
    ├── integracao/
    │   └── RenderizadorRetas.java       # liga DDA/Bresenham à Tela
    └── teste/
        ├── AlgoritmosRetaTeste.java
        └── AlgoritmosCircunferenciaTeste.java
```

- **Model**: dados e regras (transformações, paleta, algoritmos de rasterização), sem dependência de Swing.
- **View**: componentes Swing; expõe getters/setters e registro de listeners.
- **Controller**: recebe os eventos da View, valida a entrada e atualiza Model e View.

## Algoritmos de rasterização

A interface `AlgoritmoRasterizacao` define uma saída comum em pixels DC para que os algoritmos não precisem conhecer nem alterar o renderer (`Tela.drawPixels`). A interface aceita as primitivas ponto, reta e circunferência. O ponto usa diretamente `drawPixel`; o renderer também aceita uma coleção de pixels para as formas compostas. O seletor lista cinco algoritmos: DDA e Bresenham para retas; polinomial, trigonométrico e ponto médio para circunferências. Os algoritmos do pacote `rasterizacao` estão implementados e testados de forma independente; para desenhá-los pela interface, ainda é necessário registrá-los no controller com `registrarAlgoritmo`.

Cada algoritmo registrado recebe uma `FormaRasterizacao` com coordenadas DC (ponto, reta com início/fim ou circunferência com centro/raio inteiro em pixels DC) e retorna os pixels a ativar. A interface confere se a forma corresponde ao tipo do algoritmo. Os campos de ponto inicial/centro e de reta na interface são informados em coordenadas do mundo e convertidos para DC antes da rasterização. Enquanto os algoritmos não forem registrados no controller, a interface exibe um aviso e não desenha as retas/circunferências.

## Compilação

Na raiz do repositório:

```powershell
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
```

No Linux/macOS: `javac -d out $(find src -name "*.java")`.

A pasta `out/` não é versionada. Recompile depois de atualizar o código, senão as classes novas (como as de `rasterizacao`) não existirão em `out/`.

## 1. Transformações de coordenadas

Pacote: `transformacoes` (ponto de entrada: `src/transformacoes/TransformacoesCoordenadas.java`).

Após compilar, execute a partir da pasta do repositório:

```powershell
java -cp out transformacoes.TransformacoesCoordenadas
```

Na interface:

1. No painel **Resolução do display**, escolha uma resolução pré-definida (de 320 × 240 a 1920 × 1080; a inicial é 640 × 480) ou **Personalizada**, com largura e altura entre 2 e 4096 px, confirmadas em **Aplicar**.
2. Defina a janela do mundo (`xmin`, `xmax`, `ymin`, `ymax`), com `xmin < xmax` e `ymin < ymax`.
3. Selecione **Ponto**, **Reta** ou **Circunferência**. Informe o ponto ou centro `(x, y)`; para reta, informe também o ponto final `(x final, y final)`; para circunferência, informe o raio em pixels DC.
4. Selecione um dos dois cenários NDC: `[0,1] × [0,1]` ou `[-1,1] × [-1,1]`.
5. Clique em **Desenhar**. Para ponto, o display mostra um pixel verde na posição calculada. Para reta ou circunferência, selecione o algoritmo correspondente; enquanto não for registrado no controller, a interface informa que o desenho está pendente. O painel **Resultados** exibe as coordenadas calculadas.
6. Opcionalmente, clique na área de desenho para experimentar a transformação inversa (dispositivo de entrada → NDC → mundo).

O framebuffer mantém a resolução original; os pixels são armazenados em verde. Use **Ampliação** (1×, 2×, 4× ou 8×) para inspecioná-los; **Grade** desenha limites entre pixels quando a ampliação é maior que 1×. Esses recursos afetam apenas a visualização, não alteram os pixels armazenados nem as coordenadas DC. Cliques na imagem ampliada são convertidos de volta às coordenadas do display original. As dimensões exibidas correspondem à **área de desenho** (não necessariamente à resolução total do monitor).

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

## 3. Rasterização de retas e circunferências

Pacote: `rasterizacao`. Ainda não há interface gráfica nem ponto de entrada próprio; os algoritmos são verificados pelos testes automáticos.

Todos os algoritmos trabalham em coordenadas inteiras do dispositivo, sem a conversão mundo/NDC. Cada pixel calculado é entregue a um `PlotadorPixel`, interface com o método `desenhar(int x, int y)`. Assim, o mesmo algoritmo serve tanto para desenhar na `Tela` quanto para guardar os pixels numa lista durante os testes.

### Retas (aula G3)

| Algoritmo | Classe | Ideia |
| --- | --- | --- |
| DDA (método do declive) | `DDA` | `passos = max(abs(dx), abs(dy))`; a cada passo soma `dx/passos` em x e `dy/passos` em y e arredonda. Usa ponto flutuante. |
| Bresenham (ponto médio) | `Bresenham` | Só inteiros: `d = 2dy - dx`. Se `d <= 0`, escolhe E e soma `incE = 2dy`; senão, escolhe NE e soma `incNE = 2(dy - dx)`. |

O Bresenham resolve o caso do 1º octante e cobre os outros sete por **simetria**: inverte o sentido de x e/ou de y (reflexão nos eixos) e troca x com y quando a reta é mais inclinada que 45° (reflexão em `y = x`). Os dois algoritmos incluem ambos os extremos.

A classe `integracao/RenderizadorRetas` liga os dois algoritmos à `Tela` do módulo de transformações (`tela::drawPixel`) e ficará disponível para a futura interface.

Exemplos dos slides, conferidos no teste:

| Algoritmo | Reta | Pixels |
| --- | --- | --- |
| DDA | (6,9) → (11,12) | (6,9), (7,10), (8,10), (9,11), (10,11), (11,12) |
| Bresenham | (20,10) → (30,18) | (20,10), (21,11), (22,12), (23,12), (24,13), (25,14), (26,15), (27,16), (28,16), (29,17), (30,18) |

Teste automático:

```powershell
java -cp out rasterizacao.teste.AlgoritmosRetaTeste
```

Além dos exemplos, o teste cobre retas horizontais, verticais, invertidas e de um único pixel. Também percorre 1681 direções (`dx` e `dy` de -20 a 20) verificando extremos, quantidade de pixels, ausência de repetições e de interrupções, e confere a simetria do Bresenham nos 8 octantes.

### Circunferências

Os três algoritmos calculam apenas o **2º oitante**, o arco de 90° (ponto `(0, r)`) até 45° (diagonal `x = y`), onde `0 <= x <= y`, e param ao cruzar a diagonal. A classe `SimetriaCircunferencia` gera os outros sete oitantes:

```text
( x,  y)  ( y,  x)  (-x,  y)  (-y,  x)
( x, -y)  ( y, -x)  (-x, -y)  (-y, -x)
```

Quando `x = 0` ou `x = y`, alguns desses pontos coincidem; cada pixel é ativado uma única vez.

| Algoritmo | Classe | Cálculo no oitante |
| --- | --- | --- |
| Polinomial | `CircunferenciaPolinomial` | Para x = 0, 1, 2…: `y = round(sqrt(r² - x²))`. |
| Trigonométrico | `CircunferenciaTrigonometrica` | `x = round(r·cos θ)`, `y = round(r·sen θ)`, com θ indo de 90° em direção a 45° em passos de `1/r` radiano. Amostras que caem no mesmo pixel são descartadas. |
| Ponto médio | `CircunferenciaPontoMedio` | Só inteiros: `d = 1 - r`. Se `d < 0`, escolhe E e soma `2x + 3`; senão, escolhe SE (y diminui) e soma `2(x - y) + 5`. |

- O passo de `1/r` radiano deixa amostras consecutivas a menos de 1 pixel de distância, então a curva trigonométrica não tem buracos.
- O ponto médio chega aos mesmos pixels que o polinomial, mas sem raiz quadrada nem arredondamento. O teste confere isso para todo raio até 1000.
- **Raio 0** desenha só o centro. O **centro** pode estar em qualquer posição, inclusive com coordenadas negativas; a `Tela` ignora pixels fora da área de desenho. Raio negativo, plotador nulo ou circunferência que ultrapasse o intervalo de `int` são recusados antes de qualquer pixel ser ativado.

Exemplo do ponto médio para `r = 10` (os três algoritmos geram o mesmo oitante):

| Pixel atual | d | Escolha | Próximo pixel |
| --- | --- | --- | --- |
| (0,10) | -9 | E | (1,10) |
| (1,10) | -6 | E | (2,10) |
| (2,10) | -1 | E | (3,10) |
| (3,10) | 6 | SE | (4,9) |
| (4,9) | -3 | E | (5,9) |
| (5,9) | 8 | SE | (6,8) |
| (6,8) | 5 | SE | (7,7) |

Em `(7,7)` o oitante chega à diagonal e termina; a simetria completa a circunferência.

#### Controller

`CircunferenciaController` valida a entrada, escolhe o algoritmo e envia os pixels ao destino informado no construtor. Ele aceita os valores como texto, do jeito que virão dos campos da futura View, ou já convertidos:

- **Algoritmo**: `polinomial`, `trigonometrico` ou `ponto medio`, sem diferenciar maiúsculas, acentos, espaços, `_` ou `-`.
- **Centro**: quaisquer inteiros.
- **Raio**: inteiro de 0 a 10 000 (`RAIO_MAXIMO`).

O método `desenhar` retorna a quantidade de pixels ativados. Na futura tela, basta criar `new CircunferenciaController(tela::drawPixel)`.

#### Testes

Todos os testes de circunferência:

```powershell
java -cp out rasterizacao.teste.AlgoritmosCircunferenciaTeste
```

Testes individuais (pode passar mais de um, separados por espaço):

```powershell
java -cp out rasterizacao.teste.AlgoritmosCircunferenciaTeste polinomial
java -cp out rasterizacao.teste.AlgoritmosCircunferenciaTeste trigonometrico
java -cp out rasterizacao.teste.AlgoritmosCircunferenciaTeste pontomedio
java -cp out rasterizacao.teste.AlgoritmosCircunferenciaTeste simetria
java -cp out rasterizacao.teste.AlgoritmosCircunferenciaTeste controller
```

Cada algoritmo é testado separadamente:

- Sequência exata do exemplo `r = 10`, raio 1 e raio nulo.
- Para todo raio de 0 a 200:
  - nenhum pixel repetido;
  - os quatro extremos presentes;
  - simetria completa;
  - curva fechada e contínua (8-vizinhança);
  - erro máximo em relação à circunferência real: 0,5 px no polinomial e no ponto médio, e √2/2 ≈ 0,71 px no trigonométrico, que arredonda x e y.
- Mudar o centro apenas desloca o desenho.
- Entradas inválidas são recusadas.

Se tudo estiver correto, cada teste imprime uma linha `OK: ...`, seguida de `Testes de circunferencia concluidos com sucesso.`
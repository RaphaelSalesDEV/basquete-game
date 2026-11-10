import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/** Classe principal: organiza menu, entrada do mouse, jogo, desenho e sons. */
public class JogoBasquete extends GameApplication {
    private static final int LARGURA = 1920;
    private static final int ALTURA = 1080;

    private Bola bola;
    private Cesta cesta;
    private Pontuacao pontuacao;

    private boolean emMenu = true;
    private boolean arrastando = false;
    private boolean lancada = false;
    private double tempoReinicio = 0;
    private double offsetX = 0;
    private double offsetY = 0;

    private Image imagemFundo;
    private Image imagemBola;
    private MediaPlayer somBounce;
    private MediaPlayer somCesta;

    private Canvas canvas;
    private GraphicsContext gc;
    private VBox menu;
    private long ultimoBounce;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(LARGURA);
        settings.setHeight(ALTURA);
        settings.setTitle("All Starz Up!");
        settings.setIntroEnabled(false);
        settings.setMainMenuEnabled(false);
        settings.setGameMenuEnabled(false);
        settings.setFullScreenAllowed(true);
        settings.setFullScreenFromStart(true);
    }

    @Override
    protected void initInput() {
        // ESC volta para o menu enquanto estiver jogando
        FXGL.onKeyDown(KeyCode.ESCAPE, "Voltar ao menu", this::voltarAoMenu);
    }

    @Override
    protected void initGame() {
        bola = new Bola();
        cesta = new Cesta();
        pontuacao = new Pontuacao();

        imagemFundo = carregarImagem("/assets/background.png");
        imagemBola = carregarImagem("/assets/ball.png");
        somBounce = carregarSom("/assets/bounce_sound.mp3");
        somCesta = carregarSom("/assets/cesta_feita.mp3");
    }

    @Override
    protected void initUI() {
        canvas = new Canvas(LARGURA, ALTURA);
        gc = canvas.getGraphicsContext2D();

        // Acesso correto a cena pelo FXGL (evita erro no getGameScene()).
        FXGL.getGameScene().addUINode(canvas);

        canvas.setOnMousePressed(event -> {
            double distancia = Math.hypot(
                    event.getX() - bola.getX(),
                    event.getY() - bola.getY());

            if (!emMenu && !lancada && tempoReinicio <= 0 && distancia <= Bola.RAIO) {
                arrastando = true;
                offsetX = bola.getX() - event.getX();
                offsetY = bola.getY() - event.getY();
            }
        });

        canvas.setOnMouseDragged(event -> {
            if (arrastando) {
                bola.arrastar(event.getX() + offsetX, event.getY() + offsetY);
            }
        });

        canvas.setOnMouseReleased(event -> {
            if (!arrastando) return;
            arrastando = false;
            lancada = bola.arremessar();
            if (!lancada) bola.reiniciar();
        });

        criarMenu();
        desenhar();
    }

    @Override
    protected void onUpdate(double tpf) {
        if (tempoReinicio > 0) {
            tempoReinicio -= tpf;
            if (tempoReinicio <= 0) reiniciarArremesso();
        } else if (lancada) {
            // Simulacao simplificada para atualizar com frames variados.
            double passo = Math.min(tpf, 1.0 / 30.0) * 60;
            double yAnterior = bola.getY();
            bola.atualizar(passo);

            if (cesta.marcouPonto(bola, yAnterior)) {
                pontuacao.acertou();
                tocar(somCesta);
                lancada = false;
                tempoReinicio = 0.8;
            } else {
                if (cesta.verificarColisoes(bola)) tocarBounce();
                if (bola.quicarNoChao()) tocarBounce();

                if (bola.getQuicadas() >= 3
                        || (bola.getQuicadas() > 0 && Math.abs(bola.getVelocidadeY()) < 2.5)
                        || bola.getX() < -Bola.RAIO
                        || bola.getX() > LARGURA + Bola.RAIO
                        || bola.getY() > ALTURA + Bola.RAIO) {
                    // errou: a sequencia volta para zero
                    pontuacao.errou();
                    lancada = false;
                    tempoReinicio = 0.6;
                }
            }
        }
        desenhar();
    }

    private void criarMenu() {
        Text titulo1 = criarTitulo("ALL STARZ", Color.WHITE, 130);
        Text titulo2 = criarTitulo("UP!", Color.ORANGE, 150);

        Button botaoStreak = criarBotao("Streak Mode", "#a53bff", "#6f1bd1");
        Button botaoSair = criarBotao("Exit", "#ff4538", "#c9190f");

        botaoStreak.setOnAction(event -> iniciarStreak());
        botaoSair.setOnAction(event -> FXGL.getGameController().exit());

        menu = new VBox(22, titulo1, titulo2, botaoStreak, botaoSair);
        VBox.setMargin(botaoStreak, new Insets(40, 0, 0, 0));
        menu.setAlignment(Pos.CENTER);
        menu.setPrefSize(LARGURA, ALTURA);

        FXGL.getGameScene().addUINode(menu);
    }

    private Text criarTitulo(String texto, Color cor, int tamanho) {
        Text titulo = new Text(texto);
        titulo.setFont(Font.font("Arial Black", FontWeight.BOLD, FontPosture.ITALIC, tamanho));
        titulo.setFill(cor);
        titulo.setStroke(Color.web("#14213d"));
        titulo.setStrokeType(StrokeType.OUTSIDE);
        titulo.setStrokeWidth(10);
        titulo.setEffect(new DropShadow(12, Color.BLACK));
        return titulo;
    }

    private Button criarBotao(String texto, String corCima, String corBaixo) {
        Button botao = new Button(texto);
        botao.setPrefSize(520, 95);
        botao.setStyle("-fx-background-color: linear-gradient(" + corCima + ", " + corBaixo + ");"
                + "-fx-background-radius: 22;"
                + "-fx-border-radius: 22;"
                + "-fx-border-color: #141414;"
                + "-fx-border-width: 5;"
                + "-fx-text-fill: white;"
                + "-fx-font-family: Arial;"
                + "-fx-font-size: 42px;"
                + "-fx-font-weight: bold;"
                + "-fx-font-style: italic;"
                + "-fx-cursor: hand;");
        botao.setEffect(new DropShadow(12, Color.BLACK));

        // efeito de hover e de clique
        botao.setOnMouseEntered(event -> escalar(botao, 1.06));
        botao.setOnMouseExited(event -> escalar(botao, 1.0));
        botao.setOnMousePressed(event -> escalar(botao, 0.97));
        botao.setOnMouseReleased(event -> escalar(botao, 1.06));
        return botao;
    }

    private void escalar(Button botao, double escala) {
        botao.setScaleX(escala);
        botao.setScaleY(escala);
    }

    private void iniciarStreak() {
        menu.setVisible(false);
        emMenu = false;
        reiniciarArremesso();
    }

    private void voltarAoMenu() {
        if (emMenu) return;
        pontuacao.errou();
        reiniciarArremesso();
        emMenu = true;
        menu.setVisible(true);
    }

    private void desenhar() {
        gc.clearRect(0, 0, LARGURA, ALTURA);
        if (imagemFundo != null) {
            gc.drawImage(imagemFundo, 0, 0, LARGURA, ALTURA);
        } else {
            gc.setFill(Color.DARKSLATEBLUE);
            gc.fillRect(0, 0, LARGURA, ALTURA);
            gc.setStroke(Color.LIGHTGRAY);
            gc.setLineWidth(4);
            gc.strokeLine(0, Bola.CHAO, LARGURA, Bola.CHAO);
        }

        if (emMenu) return;

        if (arrastando) desenharPontilhado();

        if (imagemBola != null) {
            // gira a imagem em volta do centro da bola
            gc.save();
            gc.translate(bola.getX(), bola.getY());
            gc.rotate(bola.getAngulo());
            gc.drawImage(imagemBola, -Bola.RAIO, -Bola.RAIO,
                    Bola.RAIO * 2, Bola.RAIO * 2);
            gc.restore();
        } else {
            gc.setFill(Color.ORANGE);
            gc.fillOval(bola.getX() - Bola.RAIO,
                    bola.getY() - Bola.RAIO, Bola.RAIO * 2, Bola.RAIO * 2);
        }

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 45));
        desenharTexto("STREAK: " + pontuacao.getPontos(), 1450, 95);
        desenharTexto("RECORDE: " + pontuacao.getRecorde(), 1450, 155);
    }

    // Texto branco com contorno escuro para aparecer bem em cima do fundo.
    private void desenharTexto(String texto, double x, double y) {
        gc.setLineWidth(6);
        gc.setStroke(Color.web("#14213d"));
        gc.strokeText(texto, x, y);
        gc.setFill(Color.WHITE);
        gc.fillText(texto, x, y);
    }

    private void desenharPontilhado() {
        double vx = (Bola.INICIO_X - bola.getX()) * Bola.FORCA;
        double vy = (Bola.INICIO_Y - bola.getY()) * Bola.FORCA;
        gc.setFill(Color.WHITE);
        for (int frame = 3; frame <= 90; frame += 3) {
            double proximoX = bola.getX() + vx * frame;
            double proximoY = bola.getY() + vy * frame
                    + Bola.GRAVIDADE * frame * (frame + 1) / 2;
            if (proximoY + Bola.RAIO > Bola.CHAO) break;
            gc.fillOval(proximoX - 4, proximoY - 4, 8, 8);
        }
    }

    private void reiniciarArremesso() {
        bola.reiniciar();
        lancada = false;
        arrastando = false;
        tempoReinicio = 0;
    }

    private Image carregarImagem(String caminho) {
        var url = getClass().getResource(caminho);
        if (url == null) {
            System.out.println("Imagem nao encontrada: " + caminho);
            return null;
        }
        return new Image(url.toExternalForm());
    }

    private MediaPlayer carregarSom(String caminho) {
        var url = getClass().getResource(caminho);
        if (url == null) return null;
        try {
            return new MediaPlayer(new Media(url.toExternalForm()));
        } catch (Exception e) {
            System.out.println("Som indisponivel: " + caminho);
            return null;
        }
    }

    private void tocar(MediaPlayer som) {
        if (som != null) {
            som.stop();
            som.play();
        }
    }

    private void tocarBounce() {
        long agora = System.nanoTime();
        if (agora - ultimoBounce > 150_000_000L) {
            tocar(somBounce);
            ultimoBounce = agora;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

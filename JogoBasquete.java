import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/** Classe principal: organiza entrada do mouse, jogo, desenho e sons. */
public class JogoBasquete extends GameApplication {
    private static final int LARGURA = 1920;
    private static final int ALTURA = 1080;

    private Bola bola;
    private Cesta cesta;
    private Pontuacao pontuacao;

    private boolean arrastando = false;
    private boolean lancada = false;
    private double tempoReinicio = 0;
    private double offsetX = 0;
    private double offsetY = 0;

    private Image imagemFundo;
    private Image imagemBola;
    private Image imagemCesta;
    private MediaPlayer somBounce;
    private MediaPlayer somCesta;

    private Canvas canvas;
    private GraphicsContext gc;
    private long ultimoBounce;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(LARGURA);
        settings.setHeight(ALTURA);
        settings.setTitle("Basquete - Meu Recorde");
        settings.setIntroEnabled(false);
        settings.setMainMenuEnabled(false);
        settings.setGameMenuEnabled(false);
        settings.setFullScreenAllowed(true);
        settings.setFullScreenFromStart(true);
    }

    @Override
    protected void initGame() {
        bola = new Bola();
        cesta = new Cesta();
        pontuacao = new Pontuacao();

        imagemFundo = carregarImagem("/background.png");
        imagemBola = carregarImagem("/bola.png");
        imagemCesta = carregarImagem("/cesta.png");
        somBounce = carregarSom("/bounce_sound.mp3");
        somCesta = carregarSom("/cesta_feita.mp3");
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

            if (!lancada && tempoReinicio <= 0 && distancia <= Bola.RAIO) {
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
                    pontuacao.errou();
                    lancada = false;
                    tempoReinicio = 0.6;
                }
            }
        }
        desenhar();
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

        if (imagemCesta != null) {
            gc.drawImage(imagemCesta, 85, 295, 410, 375);
        } else {
            gc.setStroke(Color.ORANGE);
            gc.setLineWidth(10);
            gc.strokeLine(Cesta.ARO_ESQUERDO, Cesta.ALTURA_ARO,
                    Cesta.ARO_DIREITO, Cesta.ALTURA_ARO);
        }

        if (arrastando) desenharPontilhado();

        if (imagemBola != null) {
            gc.drawImage(imagemBola,
                    bola.getX() - Bola.RAIO, bola.getY() - Bola.RAIO,
                    Bola.RAIO * 2, Bola.RAIO * 2);
        } else {
            gc.setFill(Color.ORANGE);
            gc.fillOval(bola.getX() - Bola.RAIO,
                    bola.getY() - Bola.RAIO, Bola.RAIO * 2, Bola.RAIO * 2);
        }

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 45));
        gc.fillText("PONTOS: " + pontuacao.getPontos(), 1450, 95);
        gc.fillText("RECORDE: " + pontuacao.getRecorde(), 1450, 155);
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
        return url == null ? null : new Image(url.toExternalForm());
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

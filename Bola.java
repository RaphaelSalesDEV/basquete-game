/** Guarda os dados e a fisica simples da bola. */
public class Bola {
    public static  double INICIO_X = 1450;
    public static  double INICIO_Y = 780;
    public static  double RAIO = 35;
    public static  double GRAVIDADE = 0.38;
    public static  double FORCA = 0.13;
    public static  double CHAO = 960;

    private double x = INICIO_X;
    private double y = INICIO_Y;
    private double velocidadeX = 0;
    private double velocidadeY = 0;
    private int quicadas = 0;

    public double getX() { return x; }
    public double getY() { return y; }
    public double getVelocidadeX() { return velocidadeX; }
    public double getVelocidadeY() { return velocidadeY; }
    public int getQuicadas() { return quicadas; }

    // Mantem a bola dentro de um limite enquanto e arrastada.
    public void arrastar(double mouseX, double mouseY) {
        double dx = mouseX - INICIO_X;
        double dy = mouseY - INICIO_Y;
        double distancia = Math.hypot(dx, dy);
        if (distancia > 300) {
            dx = dx * 300 / distancia;
            dy = dy * 300 / distancia;
        }
        x = INICIO_X + dx;
        y = INICIO_Y + dy;
    }

    // O impulso vai no sentido contrario ao arrasto.
    public boolean arremessar() {
        velocidadeX = (INICIO_X - x) * FORCA;
        velocidadeY = (INICIO_Y - y) * FORCA;
        return Math.hypot(velocidadeX, velocidadeY) >= 2;
    }

    public void atualizar(double passo) {
        velocidadeY += GRAVIDADE * passo;
        x += velocidadeX * passo;
        y += velocidadeY * passo;
    }

    public boolean quicarNoChao() {
        if (y + RAIO < CHAO) return false;
        y = CHAO - RAIO;
        if (velocidadeY <= 0) return false;
        velocidadeY *= -0.55;
        velocidadeX *= 0.8;
        quicadas++;
        return true;
    }

    // Colisao elastica simples contra um ponto do aro.
    public boolean quicarNoAro(double aroX, double aroY) {
        double dx = x - aroX;
        double dy = y - aroY;
        double distancia = Math.hypot(dx, dy);
        double limite = RAIO + 10;
        if (distancia == 0 || distancia >= limite) return false;

        double nx = dx / distancia;
        double ny = dy / distancia;
        double impacto = velocidadeX * nx + velocidadeY * ny;
        if (impacto >= 0) return false;

        x = aroX + nx * limite;
        y = aroY + ny * limite;
        velocidadeX -= 1.65 * impacto * nx;
        velocidadeY -= 1.65 * impacto * ny;
        return true;
    }

    public void reiniciar() {
        x = INICIO_X;
        y = INICIO_Y;
        velocidadeX = 0;
        velocidadeY = 0;
        quicadas = 0;
    }
}
